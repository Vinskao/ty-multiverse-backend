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
 * Reads the summary of a TCX workout file (exported from WorkOutDoors). Streams the file and skips every Track, which holds
 * the GPS points and is nearly all of the size, so no route is read or kept. Distance and time are the sum of the laps.
 * TCX calories include basal energy, unlike the import's active energy, so energy is not taken from here.
 *
 * <p>The key matches import_apple_health.py (sha256 of "activity|start epoch seconds"), so uploading a workout that the
 * Apple Health export already holds overwrites that row instead of duplicating it.
 */
final class HealthTcx {
    static final ZoneId ZONE = ZoneId.of("Asia/Taipei");
    private static final Map<String, String> ACTIVITIES = Map.of("Biking", "Cycling");

    private HealthTcx() {}

    static List<HealthDataStore.Workout> parse(InputStream in) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        List<HealthDataStore.Workout> rows = new ArrayList<>();
        try {
            XMLStreamReader xml = factory.createXMLStreamReader(in);
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
                        case "Track" -> skip(xml);
                        case "TotalTimeSeconds" -> seconds += number(xml.getElementText(), "TotalTimeSeconds");
                        case "DistanceMeters" -> meters += number(xml.getElementText(), "DistanceMeters");
                        default -> { }
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("Lap".equals(xml.getLocalName())) {
                        inLap = false;
                    } else if ("Activity".equals(xml.getLocalName())) {
                        rows.add(workout(sport, start, meters, seconds, laps));
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

    private static void skip(XMLStreamReader xml) throws XMLStreamException {
        int depth = 1;
        while (depth > 0 && xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
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
