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
    private static final long MAX_TCX = 30L * 1024 * 1024;
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
        store.recordIngest("apple-export", rows.size(), "workouts");
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
        store.recordIngest("apple-export", rows.size(), "daily");
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
        // A past day that suddenly jumps by half or more looks like a double count; keep the stored value and say so.
        var guarded = DailyGuard.check(rows, store.dailyTotals(rows), LocalDate.now(ZONE));
        if (!guarded.accepted().isEmpty()) {
            store.saveDaily(guarded.accepted());
        }
        recordMetricHeartbeats(rows, guarded.accepted());
        return BackendApiResponse.success("saved", Map.of("saved", guarded.accepted().size(),
                "skipped", guarded.skipped().size(), "skippedDetails", guarded.skipped()));
    }

    /**
     * One heartbeat row per metric ("daily-lines:StepCount"), so a single shortcut that quietly stops shows as stale even while
     * the others keep arriving. A batch whose lines were all skipped still counts as a delivery for that metric.
     */
    private void recordMetricHeartbeats(List<HealthDataStore.Daily> received, List<HealthDataStore.Daily> accepted) {
        for (String metric : received.stream().map(HealthDataStore.Daily::metric).distinct().sorted().toList()) {
            long sent = received.stream().filter(r -> r.metric().equals(metric)).count();
            var saved = accepted.stream().filter(r -> r.metric().equals(metric)).toList();
            String latest = received.stream().filter(r -> r.metric().equals(metric)).map(HealthDataStore.Daily::day)
                    .max(LocalDate::compareTo).map(LocalDate::toString).orElse("-");
            long skipped = sent - saved.size();
            store.recordIngest("daily-lines:" + metric, saved.size(), "latest " + latest + (skipped > 0 ? " skipped " + skipped : ""));
        }
    }

    /**
     * A TCX workout file shared from WorkOutDoors through an iPhone Shortcut (the raw file is the request body). Only the lap
     * totals are kept, never the GPS track (see HealthTcx). A workout already imported from Apple Health is updated in place.
     */
    @PostMapping("/workout-tcx")
    public BackendApiResponse<Map<String, Object>> workoutTcx(@RequestHeader(value = "X-Health-Token", required = false) String given,
                                                             HttpServletRequest request) throws IOException {
        authorize(given);
        List<HealthTcx.Parsed> rows;
        try {
            rows = HealthTcx.parseDetailed(new CappedInput(request.getInputStream(), MAX_TCX));
        } catch (IllegalArgumentException invalid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalid.getMessage());
        }
        check(rows.stream().allMatch(r -> day(r.workout().day()) && optional(r.workout().distanceM())));
        // A speed that cannot be real (a GPS jump) is refused, not stored. A real workout that merely has a bad timer is kept.
        for (var row : rows) {
            var w = row.workout();
            var refused = HealthPlausibility.check(w.activity(), w.distanceM() == null ? 0 : w.distanceM(), row.detail().track().movingS(),
                    w.durationS(), store.movingSpeedsKmh(w.activity()));
            if (refused.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, refused.get());
            }
        }
        store.saveTcx(rows);
        var first = rows.get(0).workout();
        var track = rows.get(0).detail().track();
        store.recordIngest("workout-tcx", rows.size(), first.activity() + " " + first.day());
        // The Shortcut shows this as a notification, so the numbers worth eyeballing are in it.
        var result = new java.util.LinkedHashMap<String, Object>();
        result.put("saved", rows.size());
        result.put("activity", first.activity());
        result.put("kind", rows.get(0).detail().kind());
        result.put("day", first.day().toString());
        result.put("distanceM", first.distanceM());
        result.put("durationS", first.durationS());
        result.put("movingS", track.movingS() == null ? null : Math.round(track.movingS()));
        result.put("avgHr", track.avgHr() == null ? null : Math.round(track.avgHr()));
        result.put("maxHr", track.maxHr());
        result.put("ascentM", track.ascentM() == null ? null : Math.round(track.ascentM()));
        return BackendApiResponse.success("saved", result);
    }

    @GetMapping("/summary")
    public BackendApiResponse<Map<String, Object>> summary() {
        return BackendApiResponse.success("跑步摘要", Map.of("running", RunningStats.of(store.runs(), LocalDate.now(ZONE))));
    }

    @GetMapping("/overview")
    public BackendApiResponse<Map<String, Object>> overview(Authentication user) {
        guard.require(user);
        return BackendApiResponse.success("健康資料總覽", Map.of("activities", store.activities(),
                "metrics", store.overview(LocalDate.now(ZONE)), "ingest", store.ingestStatus()));
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

    /** Streams the upload but refuses anything over the limit, so a huge body cannot fill the pod's memory. */
    private static final class CappedInput extends java.io.FilterInputStream {
        private long left;

        CappedInput(java.io.InputStream in, long limit) {
            super(in);
            this.left = limit;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                spend(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buffer, int off, int len) throws IOException {
            int n = super.read(buffer, off, len);
            if (n > 0) {
                spend(n);
            }
            return n;
        }

        private void spend(long n) {
            left -= n;
            if (left < 0) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "TCX 檔案過大");
            }
        }
    }
}
