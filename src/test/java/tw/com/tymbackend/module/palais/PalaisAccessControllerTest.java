package tw.com.tymbackend.module.palais;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PalaisAccessControllerTest {
    private final JdbcTemplate db = mock(JdbcTemplate.class);
    private final PalaisKeycloak keycloak = mock(PalaisKeycloak.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final PalaisAccessController controller = new PalaisAccessController(
            db, keycloak, events, transactions, "chiaki", 1, "https://sso.example/realms/test", "palais-api");

    @BeforeEach
    void permitTransactions() {
        when(transactions.getTransaction(any())).thenAnswer(call -> new SimpleTransactionStatus());
    }

    @Test
    void rejectsWrongAudienceBeforeLookingUpRequests() {
        var error = assertThrows(ResponseStatusException.class, () -> controller.me(jwt("maker", "other")));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(db, keycloak);
    }

    @Test
    void rejectsRevokedCheckerEvenWithOldRoleClaim() {
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "chiaki")).thenReturn(false);
        var error = assertThrows(ResponseStatusException.class, () -> controller.queue(jwt("chiaki", "palais-api"), 0));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(db);
    }

    @Test
    void returnsUnavailableWhenLiveRoleVerificationFails() {
        when(keycloak.token()).thenThrow(new IllegalStateException());
        var error = assertThrows(ResponseStatusException.class, () -> controller.queue(jwt("chiaki", "palais-api"), 0));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
        verifyNoInteractions(db);
    }

    @Test
    void successfulSynchronizationWritesActivationAfterKeycloakConfirmation() {
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), any(UUID.class))).thenReturn(List.of(Map.of("id", id, "applicant", "maker", "attempts", 0)));
        when(keycloak.token()).thenReturn("service");
        when(db.update(anyString(), eq(1), eq("ACTIVE"), anyInt(), eq(id), any(UUID.class))).thenReturn(1);
        controller.syncOne();
        var order = inOrder(keycloak, db);
        order.verify(keycloak).setRole("service", "maker", true);
        order.verify(db).update(anyString(), eq(1), eq("ACTIVE"), anyInt(), eq(id), any(UUID.class));
        order.verify(db).update(anyString(), eq(id), eq("system"), eq("ACTIVATE"), isNull());
    }

    @Test
    void exhaustedSynchronizationWritesFailureWithoutActivation() {
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), any(UUID.class))).thenReturn(List.of(Map.of("id", id, "applicant", "maker", "attempts", 4)));
        when(keycloak.token()).thenReturn("service");
        when(db.update(anyString(), eq(5), eq("FAILED"), anyInt(), eq(id), any(UUID.class))).thenReturn(1);
        doThrow(new IllegalStateException()).when(keycloak).setRole("service", "maker", true);
        controller.syncOne();
        verify(db).update(anyString(), eq(5), eq("FAILED"), anyInt(), eq(id), any(UUID.class));
        verify(db).update(anyString(), eq(id), eq("system"), eq("SYNC_FAILED"), eq("IllegalStateException"));
        verify(events).publishEvent(new PalaisFailureNotification.Exhausted(id));
        verify(db, never()).update(anyString(), anyInt(), eq("ACTIVE"), anyInt(), eq(id), any(UUID.class));
    }

    @Test
    void secondOpenApplicationFromTheSameAccountIsRefused() {
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "maker")).thenReturn(false);
        // The partial unique index on open applications makes the insert a no-op the second time.
        when(db.update(anyString(), any(UUID.class), eq("maker"), eq("maker"), eq("need access"), eq(1))).thenReturn(0);
        var error = assertThrows(ResponseStatusException.class,
                () -> controller.submit(jwt("maker", "palais-api"), new PalaisAccessController.Reason("need access")));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(db, never()).update(anyString(), any(UUID.class), eq("maker"), eq("SUBMIT"), any());
    }

    @Test
    void existingCheckerCannotApplyAgain() {
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "chiaki")).thenReturn(true);
        var error = assertThrows(ResponseStatusException.class,
                () -> controller.submit(jwt("chiaki", "palais-api"), new PalaisAccessController.Reason("again")));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(db, never()).update(anyString(), any(UUID.class), any(), any(), any(), anyInt());
    }

    @Test
    void emptyOrOversizedReasonIsRejectedBeforeAnyWrite() {
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "maker")).thenReturn(false);
        for (String bad : new String[] {"  ", "x".repeat(1001)}) {
            var error = assertThrows(ResponseStatusException.class,
                    () -> controller.submit(jwt("maker", "palais-api"), new PalaisAccessController.Reason(bad)));
            assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        }
        verify(db, never()).update(anyString(), any(UUID.class), any(), any(), any(), anyInt());
    }

    @Test
    void applicantCannotApproveTheirOwnApplication() {
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), eq(id))).thenReturn(List.of(Map.of("applicant", "chiaki", "approvals_required", 1)));
        stubNoVotes();
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "chiaki")).thenReturn(true);
        var error = assertThrows(ResponseStatusException.class, () -> controller.approve(jwt("chiaki", "palais-api"), id));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(db, never()).update(contains("SYNCING"), any(Object[].class));
    }

    @Test
    void anotherCheckerCannotDecideInSingleReviewMode() {
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), eq(id))).thenReturn(List.of(Map.of("applicant", "maker", "approvals_required", 1)));
        stubNoVotes();
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "someone-else")).thenReturn(true);
        var error = assertThrows(ResponseStatusException.class, () -> controller.approve(jwt("someone-else", "palais-api"), id));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void decisionLosesWhenTheApplicationChangedUnderneathIt() {
        // Two reviewers open the same request; by the time the second one writes, the row is no longer PENDING.
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), eq(id))).thenReturn(List.of(Map.of("applicant", "maker", "approvals_required", 1)));
        stubNoVotes();
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "chiaki")).thenReturn(true);
        when(db.queryForList(anyString(), eq(UUID.class), eq(id))).thenReturn(List.of());
        var error = assertThrows(ResponseStatusException.class, () -> controller.approve(jwt("chiaki", "palais-api"), id));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(db, never()).update(contains("palais_access_event"), any(Object[].class));
        verify(db, never()).update(contains("SYNCING"), any(Object[].class));
    }

    @Test
    void approvalMovesTheRequestToSyncingNotStraightToActive() {
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), eq(id))).thenReturn(List.of(Map.of("applicant", "maker", "approvals_required", 1)));
        stubNoVotes();
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "chiaki")).thenReturn(true);
        when(db.queryForList(anyString(), eq(UUID.class), eq(id))).thenReturn(List.of(id));
        controller.approve(jwt("chiaki", "palais-api"), id);
        verify(db).update(contains("'SYNCING'"), eq(id));
        verify(db, never()).update(contains("'ACTIVE'"), any(Object[].class));
    }

    @Test
    void revokedCheckerCannotDecideEvenIfLiveVerificationStillSaysYesForOthers() {
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), eq(id))).thenReturn(List.of(Map.of("applicant", "maker", "approvals_required", 1)));
        stubNoVotes();
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "chiaki")).thenReturn(false);
        var error = assertThrows(ResponseStatusException.class, () -> controller.approve(jwt("chiaki", "palais-api"), id));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    private void stubNoVotes() {
        when(db.query(anyString(), any(org.springframework.jdbc.core.ResultSetExtractor.class), any(Object[].class)))
                .thenReturn(new java.util.HashMap<String, Long>());
    }

    private Jwt jwt(String subject, String audience) {
        return Jwt.withTokenValue("fixture").header("alg", "RS256").subject(subject)
                .issuer("https://sso.example/realms/test").audience(List.of(audience))
                .expiresAt(Instant.now().plusSeconds(30))
                .claim("realm_access", Map.of("roles", List.of("palais-checker"))).build();
    }
}
