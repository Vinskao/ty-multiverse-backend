package tw.com.tymbackend.module.diary;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import tw.com.tymbackend.core.config.JacksonConfig;

// The app's ObjectMapper writes LocalDate as [2026,10,18]; the diary panel sorts and labels ISO strings.
class DiaryJsonTest {
    private final ObjectMapper json = new JacksonConfig().objectMapper();
    private static final LocalDate DAY = LocalDate.parse("2026-10-18");

    @Test void diaryDatesAreIsoStringsInEveryResponse() throws Exception {
        assertTrue(json.writeValueAsString(new DiaryService.Summary("s", DAY, "T", "general", "day", "event", null)).contains("\"entryDate\":\"2026-10-18\""));
        assertTrue(json.writeValueAsString(new DiaryService.Entry("e", DAY, "T", "md", "now", "general", "day", "event", "{}")).contains("\"entryDate\":\"2026-10-18\""));
        assertTrue(json.writeValueAsString(new DiaryService.ContextEntry("c", DAY, "T", "md", false)).contains("\"entryDate\":\"2026-10-18\""));
        assertTrue(json.writeValueAsString(new DiaryNarratives.Work(
            List.of(new DiaryNarratives.EventInput("e", DAY, "general", "day", "T", "{}", "h", null, true)), List.of()))
            .contains("\"entryDate\":\"2026-10-18\""));
    }

    @Test void undatedEntriesStayNull() throws Exception {
        assertTrue(json.writeValueAsString(new DiaryService.Summary("s", null, "T", "general", "unknown", "event", null)).contains("\"entryDate\":null"));
    }
}
