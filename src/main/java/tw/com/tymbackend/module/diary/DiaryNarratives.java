package tw.com.tymbackend.module.diary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * AI narratives of the owner's situation and thoughts, per event and per month. They are interpretations,
 * stored apart from the original entries; each records the source hash it was written from, so an edited
 * entry, or a month that gained an event, shows as stale until rewritten. Writers: the scoped diary agent.
 */
@Service
public class DiaryNarratives {
    private static final int MAX_LENGTH = 4000;
    private final JdbcTemplate jdbc;

    public DiaryNarratives(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record EventInput(String id, @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING) LocalDate entryDate, String category, String datePrecision, String title,
        String metadataJson, String sourceHash, String narrative, boolean stale) {}
    public record MonthInput(String month, String sourceHash, List<String> entryIds, String narrative, boolean stale) {}
    public record Work(List<EventInput> events, List<MonthInput> months) {}
    public record Text(String narrative, boolean stale, String model, String generatedAt) {}
    public record MonthText(String month, String narrative, boolean stale, String model, String generatedAt) {}
    public record View(Map<String, Text> entries, List<MonthText> months) {}
    private record Event(String id, LocalDate date, String category, String precision, String title, String metadata, String hash) {}
    private record Stored(String narrative, String hash, String model, String generatedAt) {}

    /** Every event with its narrative state, oldest first (undated last), and every month that has events. */
    public Work work(String owner) {
        var stored = stored(owner);
        var events = events(owner).stream().map(e -> {
            var s = stored.get("entry:" + e.id());
            return new EventInput(e.id(), e.date(), e.category(), e.precision(), e.title(), e.metadata(), e.hash(),
                s == null ? null : s.narrative(), s == null || !s.hash().equals(e.hash()));
        }).toList();
        var months = months(owner).entrySet().stream().map(m -> {
            var s = stored.get("month:" + m.getKey());
            String hash = monthHash(m.getValue());
            return new MonthInput(m.getKey(), hash, m.getValue().stream().map(Event::id).toList(),
                s == null ? null : s.narrative(), s == null || !s.hash().equals(hash));
        }).toList();
        return new Work(events, months);
    }

    /** What the owner's panel shows: narratives keyed by entry ID, and month summaries in order. */
    public View view(String owner) {
        var stored = stored(owner);
        var hashes = new HashMap<String, String>();
        events(owner).forEach(e -> hashes.put(e.id(), e.hash()));
        var entries = new HashMap<String, Text>();
        stored.forEach((key, s) -> {
            if (key.startsWith("entry:") && hashes.containsKey(key.substring(6)))
                entries.put(key.substring(6), new Text(s.narrative(), !s.hash().equals(hashes.get(key.substring(6))), s.model(), s.generatedAt()));
        });
        var months = new ArrayList<MonthText>();
        months(owner).forEach((month, events) -> {
            var s = stored.get("month:" + month);
            if (s != null) months.add(new MonthText(month, s.narrative(), !s.hash().equals(monthHash(events)), s.model(), s.generatedAt()));
        });
        return new View(entries, months);
    }

    /** Store or replace a narrative written from the current source; a stale source hash is refused. */
    public void save(String owner, String scopeType, String scopeKey, String narrative, String sourceHash, String model) {
        String text = narrative == null ? "" : narrative.strip();
        if (text.isEmpty() || text.length() > MAX_LENGTH) throw new IllegalArgumentException("Narrative must be 1-4000 characters");
        if (model == null || model.isBlank() || model.length() > 64) throw new IllegalArgumentException("Model is required");
        String current = switch (scopeType == null ? "" : scopeType) {
            case "entry" -> events(owner).stream().filter(e -> e.id().equals(scopeKey)).map(Event::hash).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown diary event"));
            case "month" -> Optional.ofNullable(months(owner).get(scopeKey)).map(this::monthHash)
                .orElseThrow(() -> new IllegalArgumentException("Unknown diary month"));
            default -> throw new IllegalArgumentException("Scope must be entry or month");
        };
        if (!current.equals(sourceHash)) throw new IllegalArgumentException("Source changed; regenerate");
        var now = OffsetDateTime.now();
        int updated = jdbc.update("UPDATE diary_narratives SET narrative = ?, source_hash = ?, model = ?, generated_at = ? WHERE owner_subject = ? AND scope_type = ? AND scope_key = ?",
            text, sourceHash, model, now, owner, scopeType, scopeKey);
        if (updated == 0) jdbc.update("INSERT INTO diary_narratives (owner_subject, scope_type, scope_key, narrative, source_hash, model, generated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
            owner, scopeType, scopeKey, text, sourceHash, model, now);
    }

    private List<Event> events(String owner) {
        return jdbc.query("SELECT id, entry_date, category, date_precision, title, metadata_json, content_hash FROM diary_entries WHERE owner_subject = ? AND entry_kind = 'event' ORDER BY entry_date ASC NULLS LAST, id LIMIT 1000",
            (rs, row) -> new Event(rs.getString("id"), rs.getObject("entry_date", LocalDate.class), rs.getString("category"),
                rs.getString("date_precision"), rs.getString("title"), rs.getString("metadata_json"), rs.getString("content_hash")), owner);
    }

    private SortedMap<String, List<Event>> months(String owner) {
        var months = new TreeMap<String, List<Event>>();
        for (var e : events(owner)) if (e.date() != null) months.computeIfAbsent(e.date().toString().substring(0, 7), k -> new ArrayList<>()).add(e);
        return months;
    }

    private Map<String, Stored> stored(String owner) {
        var stored = new HashMap<String, Stored>();
        jdbc.query("SELECT scope_type, scope_key, narrative, source_hash, model, generated_at FROM diary_narratives WHERE owner_subject = ?",
            rs -> { stored.put(rs.getString("scope_type") + ":" + rs.getString("scope_key"), new Stored(rs.getString("narrative"),
                rs.getString("source_hash"), rs.getString("model"), rs.getObject("generated_at", OffsetDateTime.class).toString())); }, owner);
        return stored;
    }

    private String monthHash(List<Event> events) {
        var joined = new StringBuilder();
        events.stream().sorted(Comparator.comparing(Event::id)).forEach(e -> joined.append(e.id()).append(':').append(e.hash()).append('\n'));
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(joined.toString().getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 unavailable"); }
    }
}
