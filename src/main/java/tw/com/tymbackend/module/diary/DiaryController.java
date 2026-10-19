package tw.com.tymbackend.module.diary;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/diary")
public class DiaryController {
    private final DiaryService service;
    public DiaryController(DiaryService service) { this.service = service; }
    public record ImportRequest(LocalDate entryDate, String title, String markdown, String category) {}
    public record AcknowledgeRequest(String triggeredAt) {}

    @GetMapping("/context")
    public ResponseEntity<DiaryService.ConversationContext> context(@AuthenticationPrincipal Jwt jwt) {
        return privateResponse(service.context(owner(jwt)));
    }

    @PostMapping("/check-in")
    public ResponseEntity<DiaryService.Reminder> checkIn(@AuthenticationPrincipal Jwt jwt) {
        return privateResponse(service.checkIn(owner(jwt)));
    }
    @PostMapping("/acknowledge")
    public ResponseEntity<Map<String, Boolean>> acknowledge(@AuthenticationPrincipal Jwt jwt, @RequestBody AcknowledgeRequest body) {
        service.acknowledge(owner(jwt), body.triggeredAt());
        return privateResponse(Map.of("acknowledged", true));
    }
    @PostMapping("/import")
    public ResponseEntity<DiaryService.ImportResult> importMarkdown(@AuthenticationPrincipal Jwt jwt, @RequestBody ImportRequest body) {
        return privateResponse(service.importMarkdown(owner(jwt), body.entryDate(), body.title(), body.markdown(), body.category() == null ? "general" : body.category()));
    }
    @GetMapping("/entries")
    public ResponseEntity<DiaryService.Page> entries(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to, @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "false") boolean includeUndated) {
        if (from == null && to == null) return privateResponse(service.recent(owner(jwt), category));
        if (from == null || to == null) throw new IllegalArgumentException("Choose both a start and an end date");
        return privateResponse(service.list(owner(jwt), from, to, category, includeUndated));
    }
    @GetMapping("/entries/{id}")
    public ResponseEntity<DiaryService.Entry> entry(@AuthenticationPrincipal Jwt jwt, @PathVariable String id) {
        return service.entry(owner(jwt), id).map(this::privateResponse)
            .orElseGet(() -> ResponseEntity.status(404).cacheControl(CacheControl.noStore()).build());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(Map.of("error", e.getMessage()));
    }
    private String owner(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return jwt.getSubject();
    }
    private <T> ResponseEntity<T> privateResponse(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
