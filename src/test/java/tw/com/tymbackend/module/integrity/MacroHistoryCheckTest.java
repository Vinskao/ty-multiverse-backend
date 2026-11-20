package tw.com.tymbackend.module.integrity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MacroHistoryCheckTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 20);

    @Test
    void flagsShortAndEmptySeriesButAcceptsThreeCompleteYears() throws Exception {
        var snapshot = new ObjectMapper().readTree("""
            {"indicators":[
              {"code":"SHORT","frequency":"monthly","history":[{"period":"2026-07","value":"4"},{"period":"2026-11","value":"5"}]},
              {"code":"EMPTY","history":[{"period":"2023-01","value":"."}]},
              {"code":"ENOUGH","frequency":"monthly","history":[{"period":"2023-11","value":"1"},{"period":"2026-11","value":"2"}]}
            ]}
            """);
        assertThat(MacroHistoryCheck.scan(snapshot, TODAY))
            .extracting(MacroHistoryCheck.Finding::code).containsExactly("SHORT", "EMPTY");
    }

    @Test
    void readsYearAndQuarterPeriodsInsteadOfCallingThemEmpty() throws Exception {
        // 2026-11-20 trial scan: population (periods like "2023") was reported as having no valid history.
        var snapshot = new ObjectMapper().readTree("""
            {"indicators":[
              {"code":"TW_POPULATION","history":[{"period":"2000","value":"22.3"},{"period":"2023","value":"23.3"}]},
              {"code":"QUARTERLY","history":[{"period":"2020-Q1","value":"1"},{"period":"2026-Q3","value":"2"}]}
            ]}
            """);
        assertThat(MacroHistoryCheck.scan(snapshot, TODAY)).isEmpty();
    }

    @Test
    void measuresFromTodayWithAFewDaysOfSlackNotBetweenFirstAndLastPoint() throws Exception {
        // ICE BofA spreads ran 2023-11-20 to 2026-11-18: exactly three years back from the scan, yet flagged.
        var snapshot = new ObjectMapper().readTree("""
            {"indicators":[
              {"code":"BAMLC0A0CM","history":[{"period":"2023-11-20","value":"1"},{"period":"2026-11-18","value":"1"}]},
              {"code":"LATE","history":[{"period":"2023-12-15","value":"1"},{"period":"2026-11-19","value":"1"}]}
            ]}
            """);
        assertThat(MacroHistoryCheck.scan(snapshot, TODAY)).extracting(MacroHistoryCheck.Finding::code).containsExactly("LATE");
    }

    @Test
    void separatesKnownSourceLimitsFromProblemsButNeverExcusesAnEmptySeries() throws Exception {
        var snapshot = new ObjectMapper().readTree("""
            {"indicators":[
              {"code":"TW_VIX","history":[{"period":"2026-08-03","value":"39"},{"period":"2026-11-19","value":"49"}]},
              {"code":"TW_PMI","history":[]},
              {"code":"NEW_SHORT","history":[{"period":"2026-01-01","value":"1"}]}
            ]}
            """);
        var findings = MacroHistoryCheck.scan(snapshot, TODAY);
        assertThat(findings).filteredOn(MacroHistoryCheck.Finding::expected)
            .extracting(MacroHistoryCheck.Finding::code).containsExactly("TW_VIX");
        assertThat(findings).filteredOn(finding -> !finding.expected())
            .extracting(MacroHistoryCheck.Finding::code).containsExactly("TW_PMI", "NEW_SHORT");
        assertThat(findings.get(0).alertLine()).contains("期交所只保留約 4 個月");
    }
}
