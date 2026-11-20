package tw.com.tymbackend.module.palais;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/palais-access")
@ConditionalOnProperty(name = "palais.access.enabled", havingValue = "true")
class PalaisAccessController {
    private final JdbcTemplate db;
    private final ApplicationEventPublisher events;
    private final TransactionTemplate transactions;
    private final PalaisKeycloak keycloak;
    private final PalaisReviewPolicy policy;
    private final int approvals;
    private final String issuer;
    private final String audience;
    private static final int MAX_ATTEMPTS = 5;
    private static final String SUMMARY = """
            select id, display_name, reason, status, created_at, approvals_required,
                   (select count(distinct e.actor) from palais_access_event e where e.request_id = r.id
                      and e.action = 'APPROVE' and not exists (
                      select 1 from palais_access_event x where x.request_id = e.request_id
                      and x.action = 'APPROVAL_INVALIDATED' and x.detail = cast(e.id as text))) as approvals,
                   (select detail from palais_access_event e where e.request_id = r.id
                      and e.action = 'REJECT' order by e.id desc limit 1) as rejection
            from palais_access_request r
            """;

    PalaisAccessController(JdbcTemplate db, PalaisKeycloak keycloak, ApplicationEventPublisher events, PlatformTransactionManager transactionManager,
                           @Value("${palais.access.initial-checker-id}") String initialChecker,
                           @Value("${palais.access.approvals:1}") int approvals,
                           @Value("${palais.access.issuer}") String issuer,
                           @Value("${palais.access.audience}") String audience) {
        if (initialChecker.isBlank() || issuer.isBlank() || audience.isBlank() || (approvals != 1 && approvals != 2)) {
            throw new IllegalArgumentException("Invalid Palais review configuration");
        }
        this.db = db;
        this.events = events;
        this.transactions = new TransactionTemplate(transactionManager);
        this.keycloak = keycloak;
        this.policy = new PalaisReviewPolicy(initialChecker);
        this.approvals = approvals;
        this.issuer = issuer;
        this.audience = audience;
    }

    record Reason(String reason) {}

    @GetMapping("/me")
    Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        String actor = subject(jwt);
        boolean checker = currentChecker(actor);
        boolean administrator = checker && actor.equals(policy.initialCheckerId());
        return Map.of("checker", checker, "administrator", administrator, "canDecide", checker && (administrator || approvals == 2),
                "requests", db.queryForList(SUMMARY + " where applicant = ? order by created_at desc limit 20", actor));
    }

    @PostMapping("/requests")
    @Transactional
    Map<String, Object> submit(@AuthenticationPrincipal Jwt jwt, @RequestBody Reason input) {
        String actor = subject(jwt);
        if (currentChecker(actor)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already a Checker");
        }
        String name = jwt.getClaimAsString("preferred_username");
        UUID id = UUID.randomUUID();
        int inserted = db.update("""
                insert into palais_access_request(id, applicant, display_name, reason, status, approvals_required)
                values (?, ?, ?, ?, 'PENDING', ?) on conflict do nothing
                """, id, actor, name == null ? actor : name, reason(input), approvals);
        if (inserted != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An application is already open");
        }
        event(id, actor, "SUBMIT", null);
        return Map.of("id", id);
    }

    @PostMapping("/requests/{id}/withdraw")
    @Transactional
    void withdraw(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        String actor = subject(jwt);
        int updated = db.update("""
                update palais_access_request set status = 'WITHDRAWN'
                where id = ? and applicant = ? and status = 'PENDING'
                """, id, actor);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        event(id, actor, "WITHDRAW", null);
    }

    @GetMapping("/verify-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void verifyRead(@AuthenticationPrincipal Jwt jwt) {
        requireChecker(subject(jwt));
    }

    @GetMapping("/review-queue")
    List<Map<String, Object>> queue(@AuthenticationPrincipal Jwt jwt,
                                   @RequestParam(defaultValue = "0") int offset) {
        requireChecker(subject(jwt));
        return db.queryForList(SUMMARY + " where status = 'PENDING' order by created_at, id limit 30 offset ?",
                Math.max(0, offset));
    }

    @GetMapping("/managed-requests")
    List<Map<String, Object>> managedRequests(@AuthenticationPrincipal Jwt jwt,
                                              @RequestParam(defaultValue = "0") int offset) {
        requireAdministrator(jwt);
        return db.queryForList(SUMMARY + """
                 where status in ('FAILED', 'ACTIVE')
                 order by case when status = 'FAILED' then 0 else 1 end, created_at desc, id
                 limit 30 offset ?
                """, Math.clamp(offset, 0, 10000));
    }

    @GetMapping("/audit")
    List<Map<String, Object>> audit(@AuthenticationPrincipal Jwt jwt,
                                   @RequestParam(defaultValue = "0") int offset) {
        requireAdministrator(jwt);
        return db.queryForList("""
                select e.id, e.request_id, e.actor, e.action, e.detail, e.created_at, r.display_name, r.status
                from palais_access_event e join palais_access_request r on r.id = e.request_id
                order by e.id desc limit 50 offset ?
                """, Math.max(0, offset));
    }

    @PostMapping("/requests/{id}/approve")
    void approve(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        decide(subject(jwt), id, null);
    }

    @PostMapping("/requests/{id}/reject")
    void reject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody Reason input) {
        decide(subject(jwt), id, reason(input));
    }

    private void decide(String actor, UUID id, String rejection) {
        var rows = db.queryForList("""
                select applicant, approvals_required from palais_access_request
                where id = ? and status = 'PENDING'
                """, id);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        var row = rows.getFirst();
        String applicant = (String) row.get("applicant");
        int required = (int) row.get("approvals_required");
        Map<String, Long> prior = votes(id);
        var valid = new HashSet<>(prior.keySet());
        try {
            String token = keycloak.token();
            if (!policy.canReview(applicant, actor, keycloak.hasRole(token, actor), required, java.util.Set.of())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            for (String voter : prior.keySet()) {
                if (!keycloak.hasRole(token, voter)) {
                    valid.remove(voter);
                }
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Checker verification unavailable");
        }
        if (valid.contains(actor)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already submitted");
        }
        transactions.executeWithoutResult(transaction -> {
            if (db.queryForList("""
                    select id from palais_access_request where id = ? and status = 'PENDING' for update
                    """, UUID.class, id).isEmpty() || !votes(id).equals(prior)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Application changed; refresh and retry");
            }
            prior.forEach((voter, voteId) -> {
                if (!valid.contains(voter)) {
                    event(id, "system", "APPROVAL_INVALIDATED", voteId.toString());
                }
            });
            if (rejection != null) {
                event(id, actor, "REJECT", rejection);
                db.update("update palais_access_request set status = 'REJECTED' where id = ?", id);
                return;
            }
            event(id, actor, "APPROVE", null);
            valid.add(actor);
            if (policy.hasEnoughApprovals(required, valid)) {
                db.update("update palais_access_request set status = 'SYNCING', next_attempt = now() where id = ?", id);
            }
        });
    }

    private Map<String, Long> votes(UUID id) {
        return db.query("""
                select e.actor, max(e.id) as vote_id from palais_access_event e
                where e.request_id = ? and e.action = 'APPROVE'
                and not exists (select 1 from palais_access_event x where x.request_id = e.request_id
                    and x.action = 'APPROVAL_INVALIDATED' and x.detail = cast(e.id as text))
                group by e.actor
                """, result -> {
            var votes = new java.util.HashMap<String, Long>();
            while (result.next()) {
                votes.put(result.getString("actor"), result.getLong("vote_id"));
            }
            return votes;
        }, id);
    }

    @PostMapping("/requests/{id}/retry")
    @Transactional
    void retry(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        requireAdministrator(jwt);
        if (db.update("""
                update palais_access_request set status = 'SYNCING', attempts = 0, next_attempt = now()
                where id = ? and status = 'FAILED'
                """, id) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        event(id, subject(jwt), "RETRY", null);
    }

    @PostMapping("/requests/{id}/revoke")
    @Transactional
    void revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        requireAdministrator(jwt);
        List<String> users = db.queryForList("""
                select applicant from palais_access_request where id = ? and status = 'ACTIVE' for update
                """, String.class, id);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        try {
            keycloak.setRole(keycloak.token(), users.getFirst(), false);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Revocation unavailable");
        }
        db.update("update palais_access_request set status = 'REVOKED' where id = ?", id);
        event(id, subject(jwt), "REVOKE", null);
    }

    @Scheduled(fixedDelayString = "${palais.access.worker-delay-ms:10000}")
    public void syncOne() {
        UUID lease = UUID.randomUUID();
        var rows = transactions.execute(transaction -> db.queryForList("""
                with candidate as (
                    select id from palais_access_request
                    where status = 'SYNCING' and next_attempt <= now()
                    order by next_attempt, id limit 1 for update skip locked
                )
                update palais_access_request r set lease = ?, next_attempt = now() + interval '2 minutes'
                from candidate where r.id = candidate.id
                returning r.id, r.applicant, r.attempts
                """, lease));
        if (rows == null || rows.isEmpty()) {
            return;
        }
        var row = rows.getFirst();
        UUID id = (UUID) row.get("id");
        String failure = null;
        try {
            keycloak.setRole(keycloak.token(), (String) row.get("applicant"), true);
        } catch (RuntimeException error) {
            failure = error.getClass().getSimpleName();
        }
        String result = failure;
        transactions.executeWithoutResult(transaction -> finalizeSync(id, lease, (int) row.get("attempts"), result));
    }

    private void finalizeSync(UUID id, UUID lease, int previousAttempts, String failure) {
        int attempts = previousAttempts + 1;
        boolean exhausted = failure != null && attempts >= MAX_ATTEMPTS;
        String status = failure == null ? "ACTIVE" : exhausted ? "FAILED" : "SYNCING";
        int updated = db.update("""
                update palais_access_request
                set attempts = ?, status = ?, lease = null, next_attempt = now() + (? * interval '1 second')
                where id = ? and lease = ? and status = 'SYNCING'
                """, attempts, status, 15 * (1 << attempts), id, lease);
        if (updated != 1) {
            return;
        }
        event(id, "system", failure == null ? "ACTIVATE" : exhausted ? "SYNC_FAILED" : "SYNC_RETRY", failure);
        if (exhausted) {
            LoggerFactory.getLogger(getClass()).error("Palais role provisioning exhausted retries for request {}", id);
            events.publishEvent(new PalaisFailureNotification.Exhausted(id));
        }
    }

    private void requireChecker(String actor) {
        if (!currentChecker(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private void requireAdministrator(Jwt jwt) {
        String actor = subject(jwt);
        if (!actor.equals(policy.initialCheckerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        requireChecker(actor);
    }

    private boolean currentChecker(String actor) {
        try {
            return keycloak.hasRole(keycloak.token(), actor);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Checker verification unavailable");
        }
    }

    private String subject(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()
                || jwt.getIssuer() == null || !issuer.equals(jwt.getIssuer().toString())
                || jwt.getAudience() == null || !jwt.getAudience().contains(audience)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return jwt.getSubject();
    }

    private static String reason(Reason input) {
        String value = input == null || input.reason() == null ? "" : input.reason().strip();
        if (value.isEmpty() || value.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason must contain 1-1000 characters");
        }
        return value;
    }

    private void event(UUID id, String actor, String action, String detail) {
        db.update("insert into palais_access_event(request_id, actor, action, detail) values (?, ?, ?, ?)",
                id, actor, action, detail);
    }
}
