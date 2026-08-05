package tw.com.tymbackend.module.learn.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import tw.com.tymbackend.module.learn.dao.LearnOptionRepository;
import tw.com.tymbackend.module.learn.dao.LearnQuestionRepository;
import tw.com.tymbackend.module.learn.dao.LearnQuizRepository;
import tw.com.tymbackend.module.learn.domain.vo.LearnOption;
import tw.com.tymbackend.module.learn.domain.vo.LearnQuestion;
import tw.com.tymbackend.module.learn.domain.vo.LearnQuiz;

/**
 * Seeds the bundled topics on every startup, upserting by {@code (quizId, position)} rather than
 * skipping whole quizzes. Content can therefore be corrected or extended in the JSON and shipped
 * with a redeploy, while question ids — and the answer history pointing at them — stay stable.
 */
@Component
public class LearnQuizSeeder {
    private static final Logger log = LoggerFactory.getLogger(LearnQuizSeeder.class);

    /** Topics shown in the sidebar, in their own sequential numbering. */
    private static final List<String> SEED_FILES = List.of(
        "learn/toeic-01-grammar.json",
        "learn/toeic-02-cloze.json",
        "learn/toeic-03-reading.json"
    );

    /**
     * Superseded topics: the original combined 50-question mock, plus the short-lived part-numbered
     * ids it was first split into. Their questions now live in the numbered topics above, so these
     * are unpublished rather than deleted — old attempts still reference them and stay readable in
     * history.
     */
    private static final List<String> RETIRED_QUIZ_IDS = List.of(
        "toeic-reading-mini-50-v1",
        "toeic-part5-v2",
        "toeic-part6-v2",
        "toeic-part7-v2"
    );

    private final ObjectMapper objectMapper;
    private final LearnQuizRepository quizzes;
    private final LearnQuestionRepository questions;
    private final LearnOptionRepository options;

    public LearnQuizSeeder(ObjectMapper objectMapper, LearnQuizRepository quizzes,
                           LearnQuestionRepository questions, LearnOptionRepository options) {
        this.objectMapper = objectMapper;
        this.quizzes = quizzes;
        this.questions = questions;
        this.options = options;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedBundledQuizzes() throws IOException {
        for (String path : SEED_FILES) {
            seed(path);
        }
        retireSupersededQuizzes();
    }

    private void seed(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.warn("Learn seed file {} is missing, skipping", path);
            return;
        }
        SeedQuiz seed = objectMapper.readValue(resource.getInputStream(), SeedQuiz.class);

        LearnQuiz quiz = quizzes.findById(seed.id()).orElseGet(LearnQuiz::new);
        quiz.setId(seed.id());
        quiz.setTitle(seed.title());
        quiz.setDescription(seed.description());
        quiz.setRecommendedMinutes(seed.recommendedMinutes());
        quiz.setPartCode(seed.partCode());
        quiz.setSortOrder(seed.sortOrder() == null ? 0 : seed.sortOrder());
        quiz.setPublished(seed.published() == null || seed.published());
        quizzes.save(quiz);

        // The JSON only spells the passage out once per set; every question in that set needs it so the
        // reader still gets the passage whichever question the shuffle puts first.
        Map<String, String> passages = new HashMap<>();
        for (SeedQuestion item : seed.questions()) {
            if (item.passageKey() != null && item.passageText() != null && !item.passageText().isBlank()) {
                passages.putIfAbsent(item.passageKey(), item.passageText());
            }
        }

        int inserted = 0;
        for (SeedQuestion item : seed.questions()) {
            LearnQuestion question = questions.findByQuizIdAndPosition(seed.id(), item.position())
                .orElseGet(LearnQuestion::new);
            boolean isNew = question.getId() == null;
            question.setQuiz(quiz);
            question.setPosition(item.position());
            question.setSection(item.section());
            question.setDifficulty(item.difficulty() == null ? 1 : item.difficulty());
            question.setDerivedFrom(item.derivedFrom());
            question.setFocusPoint(item.focusPoint());
            question.setPassageKey(item.passageKey());
            question.setPassageText(item.passageKey() == null
                ? item.passageText()
                : passages.getOrDefault(item.passageKey(), item.passageText()));
            question.setPrompt(item.prompt());
            question.setCorrectOption(item.correctOption());
            question.setExplanation(item.explanation());
            questions.save(question);
            if (isNew) inserted++;

            // Options are fully rewritten so edited texts and rationales always take effect.
            options.deleteAll(options.findByQuestionIdOrderByKeyAsc(question.getId()));
            options.flush();
            List<LearnOption> rewritten = new ArrayList<>();
            for (SeedOption seedOption : item.options()) {
                LearnOption option = new LearnOption();
                option.setQuestion(question);
                option.setKey(seedOption.key());
                option.setText(seedOption.text());
                option.setRationale(seedOption.rationale());
                rewritten.add(option);
            }
            options.saveAll(rewritten);
        }
        log.info("Learn topic {} seeded: {} questions ({} new)", seed.id(), seed.questions().size(), inserted);
    }

    private void retireSupersededQuizzes() {
        for (String quizId : RETIRED_QUIZ_IDS) {
            quizzes.findById(quizId).filter(LearnQuiz::isPublished).ifPresent(quiz -> {
                quiz.setPublished(false);
                quizzes.save(quiz);
                log.info("Learn topic {} unpublished, its items now live in the part topics", quizId);
            });
        }
    }

    private record SeedQuiz(String id, String title, String description, String partCode, Integer sortOrder,
                            Integer recommendedMinutes, Boolean published, List<SeedQuestion> questions) {}

    private record SeedQuestion(Integer position, String section, Integer difficulty, Integer derivedFrom,
                                String focusPoint, String passageKey, String passageText, String prompt,
                                List<SeedOption> options, String correctOption, String explanation) {}

    private record SeedOption(String key, String text, String rationale) {}
}
