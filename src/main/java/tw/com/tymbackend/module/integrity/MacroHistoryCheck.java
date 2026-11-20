package tw.com.tymbackend.module.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

final class MacroHistoryCheck {
    record Finding(String code, String name, LocalDate first, LocalDate last, int points) {
        String alertLine() {
            return "%s %s | %s | %d 點".formatted(code, name,
                first == null ? "無有效歷史" : first + " 至 " + last, points);
        }
    }

    static List<Finding> scan(JsonNode snapshot, LocalDate today) {
        JsonNode indicators = snapshot.path("indicators");
        if (!indicators.isArray() || indicators.isEmpty()) {
            throw new IllegalArgumentException("Macro API returned no indicator inventory");
        }
        List<Finding> findings = new ArrayList<>();
        for (JsonNode indicator : indicators) {
            var dates = new TreeSet<LocalDate>();
            for (JsonNode row : indicator.path("history")) {
                LocalDate date = validPeriod(row);
                if (date != null && !date.isAfter(today)) dates.add(date);
            }
            if (dates.isEmpty() || dates.first().plusYears(3).isAfter(dates.last())) {
                findings.add(new Finding(indicator.path("code").asText(), indicator.path("name").asText(),
                    dates.isEmpty() ? null : dates.first(), dates.isEmpty() ? null : dates.last(), dates.size()));
            }
        }
        return findings;
    }

    private static LocalDate validPeriod(JsonNode row) {
        try {
            new BigDecimal(row.path("value").asText());
            String period = row.path("period").asText();
            return period.length() == 7 ? YearMonth.parse(period).atDay(1) : LocalDate.parse(period);
        } catch (NumberFormatException | DateTimeParseException invalid) {
            return null;
        }
    }
}
