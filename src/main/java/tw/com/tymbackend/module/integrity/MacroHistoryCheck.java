package tw.com.tymbackend.module.integrity;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

final class MacroHistoryCheck {
    /** Days of slack on the three-year bar, so a series starting exactly three years back is not flagged. */
    private static final int SLACK_DAYS = 7;

    /**
     * Series that are short by the source's nature, not by a fault. Kept in step with maya-sawa
     * docs/MACRO_DATA_INTEGRITY.md. They are reported as a count only; an empty one is always a problem.
     */
    static final Map<String, String> KNOWN_SHORT = Map.ofEntries(
        Map.entry("TW_VIX", "期交所只保留約 4 個月月檔，往後累積"),
        Map.entry("TW_PMI", "中經院趨勢頁只列約 12 個月，往後累積"),
        Map.entry("UMICH_CPI_EXPECTED_1Y", "密西根表格只給近 13 個月，往後累積"),
        Map.entry("TW_MARGIN_CALL_ACCOUNTS", "證交所儀表板只回傳近 60 個交易日，往後累積"),
        Map.entry("TPEx_GOLD_TWD", "櫃買只公布最新一筆，往後累積"),
        Map.entry("TX_CALENDAR_SPREAD", "合理價差用今日股本，刻意只留 13 個月"),
        Map.entry("TX_ROLL_EXCESS_SPREAD", "合理價差用今日股本，刻意只留 13 個月"),
        Map.entry("TW_MARKET_PE", "逐日以全部個股計算，往後累積"),
        Map.entry("VIX_FUT_FRONT", "每天一個請求，只抓 45 天，往後累積"),
        Map.entry("TIER_TW_GDP_FORECAST", "預測只有今年與明年"),
        Map.entry("TIER_TW_CPI_FORECAST", "預測只有今年與明年"),
        Map.entry("CRWV_DEBT", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("CRWV_CASH", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("CRWV_DEBT_DUE_12M", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("CRWV_RPO", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("CRWV_OPERATING_CASH_FLOW", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("CRWV_CAPEX", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("CRWV_INTEREST_EXPENSE", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("CRWV_CASH_TO_DEBT_DUE_12M", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("CRWV_OCF_TO_CAPEX", "CoreWeave 2025 年才上市，SEC 申報歷史不足三年，往後累積"),
        Map.entry("NVDA_GUARANTEE_EXPOSURE", "輝達近期才揭露擔保曝險，SEC 只有兩筆，往後累積"));

    record Finding(String code, String name, LocalDate first, LocalDate last, int points, String knownReason) {
        boolean expected() {
            return knownReason != null && points > 0;
        }

        String alertLine() {
            return "%s %s | %s | %d 點%s".formatted(code, name,
                first == null ? "無有效歷史" : first + " 至 " + last, points,
                expected() ? "（" + knownReason + "）" : "");
        }
    }

    static List<Finding> scan(JsonNode snapshot, LocalDate today) {
        JsonNode indicators = snapshot.path("indicators");
        if (!indicators.isArray() || indicators.isEmpty()) {
            throw new IllegalArgumentException("Macro API returned no indicator inventory");
        }
        LocalDate latestAcceptableStart = today.minusYears(3).plusDays(SLACK_DAYS);
        List<Finding> findings = new ArrayList<>();
        for (JsonNode indicator : indicators) {
            var dates = new TreeSet<LocalDate>();
            for (JsonNode row : indicator.path("history")) {
                LocalDate date = validPeriod(row);
                if (date != null && !date.isAfter(today)) dates.add(date);
            }
            if (dates.isEmpty() || dates.first().isAfter(latestAcceptableStart)) {
                String code = indicator.path("code").asText();
                findings.add(new Finding(code, indicator.path("name").asText(),
                    dates.isEmpty() ? null : dates.first(), dates.isEmpty() ? null : dates.last(), dates.size(),
                    KNOWN_SHORT.get(code)));
            }
        }
        return findings;
    }

    /** Periods come as YYYY (annual), YYYY-MM, YYYY-Qn or YYYY-MM-DD; each counts from its first day. */
    private static LocalDate validPeriod(JsonNode row) {
        try {
            new BigDecimal(row.path("value").asText());
            String period = row.path("period").asText();
            if (period.matches("\\d{4}")) return Year.parse(period).atDay(1);
            if (period.matches("\\d{4}-Q[1-4]")) {
                return YearMonth.of(Integer.parseInt(period.substring(0, 4)), (period.charAt(6) - '1') * 3 + 1).atDay(1);
            }
            return period.length() == 7 ? YearMonth.parse(period).atDay(1) : LocalDate.parse(period);
        } catch (NumberFormatException | DateTimeParseException invalid) {
            return null;
        }
    }
}
