package tw.com.tymbackend.module.diary;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class DiaryServiceTest {
    private DiaryService service;
    private JdbcTemplate jdbc;
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-18T13:00:00Z"), ZoneOffset.UTC);

    @BeforeEach void setup() throws Exception {
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
            "jdbc:h2:mem:" + java.util.UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", ""));
        for (String statement : Files.readString(Path.of("db/private_diary.sql")).split(";")) {
            if (!statement.isBlank()) jdbc.execute(statement);
        }
        service = new DiaryService(jdbc, clock);
    }

    @Test void importsPreserveMarkdownAndDeduplicateNormalizedLineEndingsPerOwnerAndDate() {
        var first = service.importMarkdown("alice", LocalDate.parse("2026-10-18"), "Weekly", "\uFEFF# Notes\r\n\r\n- test\r\n");
        var retry = service.importMarkdown("alice", LocalDate.parse("2026-10-18"), "Weekly", "# Notes\n\n- test\n");
        assertEquals(first.entry().id(), retry.entry().id());
        assertFalse(first.duplicate());
        assertTrue(retry.duplicate());
        assertEquals("# Notes\n\n- test", first.entry().markdown());
        assertNotEquals(first.entry().id(), service.importMarkdown("bob", LocalDate.parse("2026-10-18"), "Weekly", "# Notes\n\n- test").entry().id());
    }

    @Test void readersCannotAccessAnotherOwnersEntryAndListsDoNotIncludeBodies() {
        var entry = service.importMarkdown("alice", LocalDate.parse("2026-10-18"), "A", "private").entry();
        assertTrue(service.entry("bob", entry.id()).isEmpty());
        assertTrue(service.list("bob", LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31")).entries().isEmpty());
        assertEquals(1, service.list("alice", LocalDate.parse("2026-10-18"), LocalDate.parse("2026-10-18")).entries().size());
    }

    @Test void missedDeadlineQueuesUntilLoginThenRestartsSevenDaysFromDelivery() {
        var before = new DiaryService(jdbc, Clock.fixed(Instant.parse("2026-10-18T12:59:59Z"), ZoneOffset.UTC));
        assertFalse(before.checkIn("alice").pending());
        assertEquals("2026-10-18T21:00+08:00", before.checkIn("alice").nextDueAt());
        var monday = new DiaryService(jdbc, Clock.fixed(Instant.parse("2026-10-19T02:00:00Z"), ZoneOffset.UTC));
        var triggered = monday.checkIn("alice");
        assertTrue(triggered.pending());
        assertEquals("2026-10-26T10:00+08:00", triggered.nextDueAt());
        assertEquals(triggered, monday.checkIn("alice"));
        monday.acknowledge("alice", triggered.triggeredAt());
        assertFalse(monday.checkIn("alice").pending());
        assertTrue(monday.checkIn("bob").pending());
        var next = new DiaryService(jdbc, Clock.fixed(Instant.parse("2026-11-01T13:00:00Z"), ZoneOffset.UTC));
        assertTrue(next.checkIn("alice").pending());
        assertEquals("2026-11-08T21:00+08:00", next.checkIn("alice").nextDueAt());
        assertThrows(IllegalArgumentException.class, () -> next.acknowledge("alice", triggered.triggeredAt()));
    }

    @Test void scheduledTickQueuesOnceAtDeadlineAndLoginStartsTheRollingWeek() {
        var beforeDeadline = new DiaryService(jdbc, Clock.fixed(Instant.parse("2026-10-18T12:59:00Z"), ZoneOffset.UTC));
        assertFalse(beforeDeadline.checkIn("alice").pending());

        var atDeadline = new DiaryService(jdbc, Clock.fixed(Instant.parse("2026-10-18T13:00:00Z"), ZoneOffset.UTC));
        assertEquals(1, atDeadline.queueDueWorkflows());
        assertEquals(0, atDeadline.queueDueWorkflows());
        assertTrue(jdbc.queryForObject("SELECT pending FROM diary_workflows WHERE owner_subject = 'alice'", Boolean.class));
        assertEquals(OffsetDateTime.parse("2026-10-18T21:00+08:00"),
            jdbc.queryForObject("SELECT queued_since FROM diary_workflows WHERE owner_subject = 'alice'", OffsetDateTime.class));
        assertNull(jdbc.queryForObject("SELECT triggered_at FROM diary_workflows WHERE owner_subject = 'alice'", OffsetDateTime.class));

        var nextLogin = new DiaryService(jdbc, Clock.fixed(Instant.parse("2026-10-19T02:00:00Z"), ZoneOffset.UTC));
        var reminder = nextLogin.checkIn("alice");
        assertTrue(reminder.pending());
        assertEquals("2026-10-18T21:00+08:00", reminder.queuedSince());
        assertEquals("2026-10-26T10:00+08:00", reminder.nextDueAt());
    }

    @Test void oldImportsDoNotCompleteThisWeeksReminderButCurrentWeekImportsDo() {
        assertTrue(service.checkIn("alice").pending());
        service.importMarkdown("alice", LocalDate.parse("2026-02-14"), "Archive", "old notes");
        assertTrue(service.checkIn("alice").pending());
        service.importMarkdown("alice", LocalDate.parse("2026-10-18"), "Weekly", "this week");
        assertFalse(service.checkIn("alice").pending());
    }

    @Test void rejectsEmptyOversizedFutureAndInvalidRangesBeforeWriting() {
        assertThrows(IllegalArgumentException.class, () -> service.importMarkdown("alice", LocalDate.now(clock), "T", "  "));
        assertThrows(IllegalArgumentException.class, () -> service.importMarkdown("alice", LocalDate.now(clock), "T", "a".repeat(200001)));
        assertThrows(IllegalArgumentException.class, () -> service.importMarkdown("alice", LocalDate.parse("2026-10-19"), "T", "future"));
        assertThrows(IllegalArgumentException.class, () -> service.list("alice", LocalDate.parse("2026-10-19"), LocalDate.parse("2026-10-18")));
        assertThrows(IllegalArgumentException.class, () -> service.list("alice", LocalDate.parse("2020-01-01"), LocalDate.parse("2026-10-18")));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM diary_entries", Integer.class));
    }

    @Test void conversationContextIsOwnerScopedBoundedAndDoesNotInventLongTermMemory() {
        for (int i = 0; i < 16; i++) service.importMarkdown("alice", LocalDate.parse("2026-10-18"), "Note " + i, "x".repeat(7000) + i);
        service.importMarkdown("bob", LocalDate.parse("2026-10-18"), "Secret", "bob-only");
        var context = service.context("alice");
        assertEquals(14, context.recent().size());
        assertTrue(context.hasMore());
        assertTrue(context.recent().stream().allMatch(e -> e.markdown().length() <= 4000));
        assertTrue(context.recent().stream().allMatch(e -> e.truncated()));
        assertFalse(context.recent().stream().anyMatch(e -> e.markdown().contains("bob-only")));
        assertTrue(context.longTermSummary().isEmpty());
    }

    @Test void categoriesShareOneTableButHaveIndependentDeduplicationAndFilters() {
        var date = LocalDate.parse("2026-10-18");
        service.importMarkdown("alice", date, "Relationship", "same body", "relationship");
        service.importMarkdown("alice", date, "Investment", "same body", "investment");
        assertEquals(2, service.list("alice", date, date).entries().size());
        var investment = service.list("alice", date, date, "investment");
        assertEquals(1, investment.entries().size());
        assertEquals("investment", investment.entries().getFirst().category());
        assertThrows(IllegalArgumentException.class, () -> service.importMarkdown("alice", date, "Bad", "body", "public"));
        assertThrows(IllegalArgumentException.class, () -> service.list("alice", date, date, "invalid"));
    }

    @Test void undatedRecordsAreExplicitlyRequestedAndStillOwnerAndCategoryScoped() {
        jdbc.update("INSERT INTO diary_entries (id, owner_subject, title, markdown, content_hash, created_at, category, entry_kind, date_precision) VALUES ('undated', 'alice', 'Unknown', 'Source', 'hash', CURRENT_TIMESTAMP, 'relationship', 'event', 'unknown')");
        var from = LocalDate.parse("2026-10-01");
        var to = LocalDate.parse("2026-10-31");
        assertTrue(service.list("alice", from, to).entries().isEmpty());
        assertEquals(1, service.list("alice", from, to, "relationship", true).entries().size());
        assertTrue(service.list("alice", from, to, "investment", true).entries().isEmpty());
        assertTrue(service.list("bob", from, to, "all", true).entries().isEmpty());
        assertNull(service.entry("alice", "undated").orElseThrow().entryDate());
    }

    @Test void listsCarryEventDescriptionsForTheNarrativeButNotDocumentBodies() {
        jdbc.update("INSERT INTO diary_entries (id, owner_subject, entry_date, title, markdown, content_hash, created_at, category, entry_kind, date_precision, metadata_json) VALUES ('ev', 'alice', DATE '2026-10-18', 'Event', 'body', 'h1', CURRENT_TIMESTAMP, 'relationship', 'event', 'day', ?)",
            "{\"description\":\"" + "長".repeat(1200) + "\",\"notes\":\"n\"}");
        jdbc.update("INSERT INTO diary_entries (id, owner_subject, entry_date, title, markdown, content_hash, created_at, category, entry_kind, date_precision, metadata_json) VALUES ('doc', 'alice', DATE '2026-10-18', 'Source', 'body', 'h2', CURRENT_TIMESTAMP, 'relationship', 'document', 'document', '{\"source_file\":\"a.md\"}')");
        jdbc.update("INSERT INTO diary_entries (id, owner_subject, entry_date, title, markdown, content_hash, created_at, category, entry_kind, date_precision, metadata_json) VALUES ('bad', 'alice', DATE '2026-10-18', 'Broken', 'body', 'h3', CURRENT_TIMESTAMP, 'general', 'event', 'day', 'not json')");
        var byId = new java.util.HashMap<String, DiaryService.Summary>();
        service.recent("alice", "all").entries().forEach(s -> byId.put(s.id(), s));
        assertEquals(1000, byId.get("ev").description().length());
        assertNull(byId.get("doc").description());
        assertNull(byId.get("bad").description());
        var day = LocalDate.parse("2026-10-18");
        assertEquals(byId.get("ev").description(), service.list("alice", day, day).entries().stream().filter(s -> s.id().equals("ev")).findFirst().orElseThrow().description());
    }

    @Test void recentListSpansYearsNewestFirstWithUndatedLastAndStaysOwnerScoped() {
        service.importMarkdown("alice", LocalDate.parse("2023-05-01"), "Old", "old", "investment");
        service.importMarkdown("alice", LocalDate.parse("2026-10-18"), "New", "new", "relationship");
        service.importMarkdown("bob", LocalDate.parse("2026-10-18"), "Bob", "bob-only");
        jdbc.update("INSERT INTO diary_entries (id, owner_subject, title, markdown, content_hash, created_at, category, entry_kind, date_precision) VALUES ('undated', 'alice', 'Unknown', 'Source', 'hash', CURRENT_TIMESTAMP, 'relationship', 'event', 'unknown')");
        var page = service.recent("alice", "all");
        assertEquals(java.util.List.of("New", "Old", "Unknown"), page.entries().stream().map(DiaryService.Summary::title).toList());
        assertFalse(page.hasMore());
        assertEquals(java.util.List.of("Old"), service.recent("alice", "investment").entries().stream().map(DiaryService.Summary::title).toList());
        assertThrows(IllegalArgumentException.class, () -> service.recent("alice", "invalid"));
    }
}
