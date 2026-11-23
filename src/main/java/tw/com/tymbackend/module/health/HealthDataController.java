package tw.com.tymbackend.module.health;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tw.com.ty.common.response.BackendApiResponse;

/**
 * Apple Health import. Only /summary is public and it holds running statistics, workout totals and the allowlisted metrics;
 * everything else needs the ingest token (writes) or the owner role (reads, SecurityConfig default rule).
 */
@RestController
@RequestMapping("/health-data")
public class HealthDataController {
    private static final int MAX_BATCH = 1000;
    private static final LocalDate EARLIEST = LocalDate.of(2000, 1, 1);
    private static final ZoneId ZONE = ZoneId.of("Asia/Taipei");

    private final HealthDataStore store;
    private final byte[] token;
    private final Set<String> publicMetrics;

    HealthDataController(HealthDataStore store, @Value("${health-data.ingest-token:}") String token,
                         @Value("${health-data.public-metrics:}") Set<String> publicMetrics) {
        this.store = store;
        this.token = token.getBytes(StandardCharsets.UTF_8);
        this.publicMetrics = new TreeSet<>(publicMetrics);
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

    @GetMapping("/summary")
    public BackendApiResponse<Map<String, Object>> summary() {
        LocalDate today = LocalDate.now(ZONE);
        return BackendApiResponse.success("健康資料摘要", Map.of("running", RunningStats.of(store.runs(), today),
                "activities", store.activities(), "metrics", store.dailyTotals(publicMetrics, today)));
    }

    @GetMapping("/metrics")
    public BackendApiResponse<List<Map<String, Object>>> metrics() {
        return BackendApiResponse.success("指標清單", store.metrics());
    }

    @GetMapping("/daily")
    public BackendApiResponse<List<Map<String, Object>>> daily(@RequestParam String metric) {
        return BackendApiResponse.success("每日資料", store.daily(metric));
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
