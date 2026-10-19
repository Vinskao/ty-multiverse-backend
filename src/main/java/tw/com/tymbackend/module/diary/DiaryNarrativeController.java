package tw.com.tymbackend.module.diary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Owner reads AI narratives with their JWT. The scheduled diary agent reads the events it writes about and
 * stores narratives with X-Diary-Agent-Key, whose SHA-256 is configured (never the key). That key acts only
 * for the configured owner and opens nothing outside /diary/agent; it cannot use the owner's diary API.
 */
@RestController
@RequestMapping("/diary")
public class DiaryNarrativeController {
    private final DiaryNarratives narratives;
    private final String agentKeySha256;
    private final String agentOwner;

    public DiaryNarrativeController(DiaryNarratives narratives,
            @Value("${diary.agent-key-sha256:}") String agentKeySha256, @Value("${diary.agent-owner:}") String agentOwner) {
        this.narratives = narratives;
        this.agentKeySha256 = agentKeySha256.strip().toLowerCase();
        this.agentOwner = agentOwner.strip();
    }

    public record NarrativeRequest(String narrative, String sourceHash, String model) {}

    @GetMapping("/narratives")
    public ResponseEntity<DiaryNarratives.View> view(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return privateResponse(narratives.view(jwt.getSubject()));
    }

    @GetMapping("/agent/work")
    public ResponseEntity<DiaryNarratives.Work> work(@RequestHeader(value = "X-Diary-Agent-Key", required = false) String key) {
        return privateResponse(narratives.work(agent(key)));
    }

    @PutMapping("/agent/narratives/{scopeType}/{scopeKey}")
    public ResponseEntity<Map<String, Boolean>> save(@RequestHeader(value = "X-Diary-Agent-Key", required = false) String key,
            @PathVariable String scopeType, @PathVariable String scopeKey, @RequestBody NarrativeRequest body) {
        narratives.save(agent(key), scopeType, scopeKey, body.narrative(), body.sourceHash(), body.model());
        return privateResponse(Map.of("saved", true));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalid(IllegalArgumentException e) {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(Map.of("error", e.getMessage()));
    }

    private String agent(String key) {
        if (key == null || agentKeySha256.isEmpty() || agentOwner.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        try {
            byte[] actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)))
                .getBytes(StandardCharsets.US_ASCII);
            if (!MessageDigest.isEqual(actual, agentKeySha256.getBytes(StandardCharsets.US_ASCII))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 unavailable"); }
        return agentOwner;
    }

    private <T> ResponseEntity<T> privateResponse(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
