package tw.com.tymbackend.module.health;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Reads the summary of a TCX workout file (exported from WorkOutDoors). Streams the file. Distance and time are the sum of the
 * laps. The Track (nearly all of the size) is read only for time, distance, altitude and heart rate (see TcxTrack); the GPS
 * position is never parsed, so no route is read or kept.
 * TCX calories include basal energy, unlike the import's active energy, so energy is not taken from here.
 *
 * <p>The key matches import_apple_health.py (sha256 of "activity|start epoch seconds"), so uploading a workout that the
 * Apple Health export already holds overwrites that row instead of duplicating it.
 */
final class HealthTcx {
    static final ZoneId ZONE = ZoneId.of("Asia/Taipei");
    private static final Map<String, String> ACTIVITIES = Map.of("Biking", "Cycling");

    private HealthTcx() {}

    /** What the track adds to a workout: the strict kind (see HealthKind) and heart rate, climb and best efforts. */
    record Detail(String kind, TcxTrack.Summary track) {}

    record Parsed(HealthDataStore.Workout workout, Detail detail) {}

    static List<HealthDataStore.Workout> parse(InputStream in) {
        return parseDetailed(in).stream().map(Parsed::workout).toList();
    }

    static List<Parsed> parseDetailed(InputStream in) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        List<Parsed> rows = new ArrayList<>();
        try {
            XMLStreamReader xml = factory.createXMLStreamReader(in);
            TcxTrack track = new TcxTrack();
            String sport = null;
            Instant start = null;
            double meters = 0;
            double seconds = 0;
            int laps = 0;
            boolean inLap = false;
            while (xml.hasNext()) {
                int event = xml.next();
                if (event == XMLStreamConstants.DTD) {
                    throw new IllegalArgumentException("TCX 不可包含 DOCTYPE");
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    switch (xml.getLocalName()) {
                        case "Activity" -> {
                            sport = xml.getAttributeValue(null, "Sport");
                            track = new TcxTrack();
                            start = null;
                            meters = 0;
                            seconds = 0;
                            laps = 0;
                        }
                        case "Id" -> {
                            if (!inLap) {
                                start = instant(xml.getElementText());
                            }
                        }
                        case "Lap" -> {
                            inLap = true;
                            laps++;
                        }
                        case "Track" -> readTrack(xml, track);
                        case "TotalTimeSeconds" -> seconds += number(xml.getElementText(), "TotalTimeSeconds");
                        case "DistanceMeters" -> meters += number(xml.getElementText(), "DistanceMeters");
                        default -> { }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("Lap".equals(xml.getLocalName())) {
                        inLap = false;
                    } else if ("Activity".equals(xml.getLocalName())) {
                        var workout = workout(sport, start, meters, seconds, laps);
                        var summary = track.summarize();
                        rows.add(new Parsed(workout, new Detail(HealthKind.of(workout.activity(), workout.distanceM(), summary.ascentM()), summary)));
                    }
                }
            }
        } catch (XMLStreamException broken) {
            throw new IllegalArgumentException("不是有效的 TCX 檔案", broken);
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("TCX 裡沒有 Activity");
        }
        return rows;
    }

    private static HealthDataStore.Workout workout(String sport, Instant start, double meters, double seconds, int laps) {
        if (sport == null || sport.isBlank() || sport.length() > 40) {
            throw new IllegalArgumentException("Activity 缺少 Sport");
        }
        if (start == null) {
            throw new IllegalArgumentException("Activity 的 Id（開始時間）無法讀取");
        }
        if (laps == 0) {
            throw new IllegalArgumentException("Activity 裡沒有 Lap");
        }
        if (!(seconds > 0)) {
            throw new IllegalArgumentException("Activity 的總時間必須大於 0");
        }
        String activity = ACTIVITIES.getOrDefault(sport, sport);
        return new HealthDataStore.Workout(key(activity, start), activity, start.atZone(ZONE).toLocalDate(), meters, seconds, null);
    }

    /**
     * Reads the trackpoints of one Track into the accumulator. Only time, cumulative distance, altitude and heart rate are read;
     * the position (latitude and longitude) is skipped without ever being parsed, so no route can leak out of this class.
     */
    private static void readTrack(XMLStreamReader xml, TcxTrack track) throws XMLStreamException {
        int depth = 1;
        double time = Double.NaN;
        double distance = Double.NaN;
        double altitude = Double.NaN;
        int heartRate = 0;
        boolean inHeartRate = false;
        while (depth > 0 && xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
                switch (xml.getLocalName()) {
                    case "Trackpoint" -> {
                        time = Double.NaN;
                        distance = Double.NaN;
                        altitude = Double.NaN;
                        heartRate = 0;
                    }
                    case "HeartRateBpm" -> inHeartRate = true;
                    case "Time" -> {
                        Instant at = instant(xml.getElementText());
                        time = at == null ? Double.NaN : at.getEpochSecond() + at.getNano() / 1e9;
                        depth--;
                    }
                    case "DistanceMeters" -> {
                        distance = decimal(xml.getElementText());
                        depth--;
                    }
                    case "AltitudeMeters" -> {
                        altitude = decimal(xml.getElementText());
                        depth--;
                    }
                    case "Value" -> {
                        String text = xml.getElementText();
                        depth--;
                        if (inHeartRate) {
                            double bpm = decimal(text);
                            heartRate = Double.isNaN(bpm) ? 0 : (int) Math.round(bpm);
                        }
                    }
                    default -> { }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
                if ("HeartRateBpm".equals(xml.getLocalName())) {
                    inHeartRate = false;
                } else if ("Trackpoint".equals(xml.getLocalName()) && !Double.isNaN(time) && !Double.isNaN(distance)) {
                    track.add(time, distance, altitude, heartRate);
                }
            }
        }
    }

    /** A number from a trackpoint, or NaN when it is missing or unreadable: one bad point must not reject the whole file. */
    private static double decimal(String text) {
        try {
            double value = Double.parseDouble(text.trim());
            return Double.isFinite(value) ? value : Double.NaN;
        } catch (NumberFormatException invalid) {
            return Double.NaN;
        }
    }

    private static Instant instant(String text) {
        try {
            return Instant.parse(text.trim());
        } catch (DateTimeParseException invalid) {
            return null;
        }
    }

    private static double number(String text, String field) {
        try {
            double value = Double.parseDouble(text.trim());
            if (!Double.isFinite(value) || value < 0) {
                throw new NumberFormatException(text);
            }
            return value;
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException(field + " 不是有效的數字");
        }
    }

    static String key(String activity, Instant start) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest((activity + "|" + start.getEpochSecond()).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException(unavailable);
        }
    }
}
