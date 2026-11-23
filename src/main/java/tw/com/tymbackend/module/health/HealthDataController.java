package tw.com.tymbackend.module.health;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.tymbackend.module.palais.PalaisReadGuard;

/**
 * Apple Health import. Only running statistics are public. Every other read needs the owner role or a live Palais
 * Checker role; writes need the ingest token.
 */
@RestController
@RequestMapping("/health-data")
public class HealthDataController {
    private static final int MAX_BATCH = 1000;
    private static final int MAX_BODY = 20_000;
    private static final LocalDate EARLIEST = LocalDate.of(2000, 1, 1);
    private static final ZoneId ZONE = ZoneId.of("Asia/Taipei");

    private final HealthDataStore store;
    private final byte[] token;
    private final PalaisReadGuard guard;

    HealthDataController(HealthDataStore store, PalaisReadGuard guard,
                         @Value("${health-data.ingest-token:}") String token) {
        this.store = store;
        this.token = token.getBytes(StandardCharsets.UTF_8);
        this.guard = guard;
    }

    @PostMapping("/workouts")
    public BackendApiResponse<Map<String, Object>> workouts(@RequestHeader(value = "X-Health-Token", required = false) String given,
                                                           @RequestBody List<HealthDataStore.Workout> rows) {
        authorize(given);
        check(rows.size() <= MAX_BATCH);
        for (var r : rows) {
            check(r.sourceId() != null && r.sourceId().matches("[0-9a-f]{64}") && name(r.activity()) && day(r.day())
                    && Double.isFinite(r.durationS()) && r.durationS() > 0 && optional(r.distanceM()) && optional(r.energyKcal()));
        }
        store.saveWorkouts(rows);
        return BackendApiResponse.success("saved", Map.of("saved", rows.size()));
    }

    @PostMapping("/daily")
    public BackendApiResponse<Map<String, Object>> daily(@RequestHeader(value = "X-Health-Token", required = false) String given,
                                                        @RequestBody List<HealthDataStore.Daily> rows) {
        authorize(given);
        check(rows.size() <= MAX_BATCH);
        for (var r : rows) {
            check(name(r.metric()) && day(r.day()) && r.unit() != null && r.unit().length() <= 40 && r.samples() > 0
                    && finite(r.total()) && finite(r.average()) && finite(r.minimum()) && finite(r.maximum()));
        }
        store.saveDaily(rows);
        return BackendApiResponse.success("saved", Map.of("saved", rows.size()));
    }

    /**
     * Plain text from the iPhone Shortcut, one "metric,date,value" line per day (see HealthLines). The raw body is read so any
     * content type works; Spring's own String binding re-encodes a form-typed body (commas become %2C), which is why it is not used.
     */
    @PostMapping("/daily-lines")
    public BackendApiResponse<Map<String, Object>> dailyLines(@RequestHeader(value = "X-Health-Token", required = false) String given,
                                                             HttpServletRequest request) throws IOException {
        authorize(given);
        String body = new String(request.getInputStream().readNBytes(MAX_BODY + 1), StandardCharsets.UTF_8);
        if (body.isBlank()) {
            body = formLines(request);
        }
        check(body.length() <= MAX_BODY);
        List<HealthDataStore.Daily> rows;
        try {
            rows = HealthLines.parse(body, LocalDate.now(ZONE).plusDays(1));
        } catch (IllegalArgumentException invalid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalid.getMessage());
        }
        check(!rows.isEmpty());
        store.saveDaily(rows);
        return BackendApiResponse.success("saved", Map.of("saved", rows.size()));
    }

    @GetMapping("/summary")
    public BackendApiResponse<Map<String, Object>> summary() {
        return BackendApiResponse.success("跑步摘要", Map.of("running", RunningStats.of(store.runs(), LocalDate.now(ZONE))));
    }

    @GetMapping("/overview")
    public BackendApiResponse<Map<String, Object>> overview(Authentication user) {
        guard.require(user);
        return BackendApiResponse.success("健康資料總覽", Map.of("activities", store.activities(),
                "metrics", store.overview(LocalDate.now(ZONE))));
    }

    @GetMapping("/metrics")
    public BackendApiResponse<List<Map<String, Object>>> metrics(Authentication user) {
        guard.require(user);
        return BackendApiResponse.success("指標清單", store.metrics());
    }

    @GetMapping("/daily")
    public BackendApiResponse<List<Map<String, Object>>> daily(Authentication user, @RequestParam String metric) {
        guard.require(user);
        return BackendApiResponse.success("每日資料", store.daily(metric));
    }

    /** A container that already parsed a form body leaves the text as parameter names. */
    private static String formLines(HttpServletRequest request) {
        StringBuilder lines = new StringBuilder();
        request.getParameterMap().forEach((name, values) -> {
            String value = values.length == 0 ? "" : values[0];
            lines.append(name).append(value.isEmpty() ? "" : "=" + value).append('\n');
        });
        return lines.toString();
    }

    private void authorize(String given) {
        if (token.length == 0 || given == null || !MessageDigest.isEqual(token, given.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private static void check(boolean valid) {
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid health data batch");
        }
    }

    private static boolean name(String value) {
        return value != null && value.matches("[A-Za-z0-9.]{1,120}");
    }

    // One day ahead is allowed: the phone's time zone may be east of Taipei.
    private static boolean day(LocalDate value) {
        return value != null && !value.isBefore(EARLIEST) && !value.isAfter(LocalDate.now(ZONE).plusDays(1));
    }

    private static boolean finite(Double value) {
        return value == null || Double.isFinite(value);
    }

    private static boolean optional(Double value) {
        return finite(value) && (value == null || value >= 0);
    }
}
