package tw.com.tymbackend.module.integrity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MacroHistoryCheckTest {
    @Test
    void flagsShortAndEmptySeriesButAcceptsThreeCompleteYears() throws Exception {
        var snapshot = new ObjectMapper().readTree("""
            {"indicators":[
              {"code":"SHORT","frequency":"monthly","history":[{"period":"2026-07","value":"4"},{"period":"2026-11","value":"5"}]},
              {"code":"EMPTY","history":[{"period":"2023-01","value":"."}]},
              {"code":"ENOUGH","frequency":"monthly","history":[{"period":"2023-11","value":"1"},{"period":"2026-11","value":"2"}]}
            ]}
            """);
        assertThat(MacroHistoryCheck.scan(snapshot, LocalDate.of(2026,11,20)))
            .extracting(MacroHistoryCheck.Finding::code).containsExactly("SHORT", "EMPTY");
    }
}
