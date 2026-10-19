package tw.com.tymbackend.module.diary;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class DiaryNarrativesTest {
    private JdbcTemplate jdbc;
    private DiaryNarratives narratives;

    @BeforeEach void setup() throws Exception {
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
            "jdbc:h2:mem:" + java.util.UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
        for (String file : new String[] {"db/private_diary.sql", "db/private_diary_narratives.sql"}) {
            for (String statement : Files.readString(Path.of(file)).split(";")) {
                if (!statement.isBlank()) jdbc.execute(statement);
            }
        }
        narratives = new DiaryNarratives(jdbc);
        event("a", "alice", "2025-11-13", "h-a");
        event("b", "alice", "2025-11-20", "h-b");
        event("c", "alice", "2026-10-18", "h-c");
        event("bob", "bob", "2025-11-13", "h-bob");
        jdbc.update("INSERT INTO diary_entries (id, owner_subject, entry_date, title, markdown, content_hash, created_at, category, entry_kind, date_precision) VALUES ('doc', 'alice', DATE '2025-11-13', 'Source', 'body', 'h-doc', CURRENT_TIMESTAMP, 'general', 'document', 'document')");
        jdbc.update("INSERT INTO diary_entries (id, owner_subject, title, markdown, content_hash, created_at, category, entry_kind, date_precision) VALUES ('u', 'alice', 'Undated', 'body', 'h-u', CURRENT_TIMESTAMP, 'relationship', 'event', 'unknown')");
    }

    private void event(String id, String owner, String date, String hash) {
        jdbc.update("INSERT INTO diary_entries (id, owner_subject, entry_date, title, markdown, content_hash, created_at, category, entry_kind, date_precision, metadata_json) VALUES (?, ?, ?, ?, 'body', ?, CURRENT_TIMESTAMP, 'relationship', 'event', 'day', '{\"description\":\"d\"}')",
            id, owner, java.time.LocalDate.parse(date), "Title " + id, hash);
    }

    @Test void workListsOwnEventsAndMonthsThatStillNeedANarrative() {
        var work = narratives.work("alice");
        assertEquals(java.util.List.of("a", "b", "c", "u"), work.events().stream().map(DiaryNarratives.EventInput::id).toList());
        assertEquals("h-a", work.events().getFirst().sourceHash());
        assertEquals(java.util.List.of("2025-11", "2026-10"), work.months().stream().map(DiaryNarratives.MonthInput::month).toList());
        assertEquals(java.util.List.of("a", "b"), work.months().getFirst().entryIds());
        assertTrue(work.events().stream().allMatch(e -> e.narrative() == null && e.stale()));
    }

    @Test void savedNarrativesShowForTheOwnerAndBecomeStaleWhenTheSourceChanges() {
        var month = narratives.work("alice").months().getFirst();
        narratives.save("alice", "entry", "a", "  我當時的處境。  ", "h-a", "claude-opus-5-5");
        narratives.save("alice", "month", "2025-11", "這個月的狀態。", month.sourceHash(), "claude-opus-5-5");
        var view = narratives.view("alice");
        assertEquals("我當時的處境。", view.entries().get("a").narrative());
        assertFalse(view.entries().get("a").stale());
        assertEquals("這個月的狀態。", view.months().getFirst().narrative());
        assertTrue(narratives.view("bob").entries().isEmpty());

        // Rewrite overwrites in place; editing the source or adding an event to the month marks them stale.
        narratives.save("alice", "entry", "a", "改寫。", "h-a", "claude-opus-5-5");
        assertEquals("改寫。", narratives.view("alice").entries().get("a").narrative());
        jdbc.update("UPDATE diary_entries SET content_hash = 'h-a2' WHERE id = 'a'");
        assertTrue(narratives.view("alice").entries().get("a").stale());
        assertTrue(narratives.view("alice").months().getFirst().stale());
        assertFalse(narratives.work("alice").events().getFirst().narrative() == null);
    }

    @Test void rejectsStaleSourcesForeignEntriesDocumentsAndBadText() {
        assertThrows(IllegalArgumentException.class, () -> narratives.save("alice", "entry", "a", "x", "old-hash", "m"));
        assertThrows(IllegalArgumentException.class, () -> narratives.save("alice", "entry", "bob", "x", "h-bob", "m"));
        assertThrows(IllegalArgumentException.class, () -> narratives.save("alice", "entry", "doc", "x", "h-doc", "m"));
        assertThrows(IllegalArgumentException.class, () -> narratives.save("alice", "entry", "a", " ", "h-a", "m"));
        assertThrows(IllegalArgumentException.class, () -> narratives.save("alice", "entry", "a", "x".repeat(4001), "h-a", "m"));
        assertThrows(IllegalArgumentException.class, () -> narratives.save("alice", "month", "2024-01", "x", "any", "m"));
        assertThrows(IllegalArgumentException.class, () -> narratives.save("alice", "week", "a", "x", "h-a", "m"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM diary_narratives", Integer.class));
    }
}
