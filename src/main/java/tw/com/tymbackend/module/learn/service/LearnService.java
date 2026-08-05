package tw.com.tymbackend.module.learn.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.module.learn.dao.LearnAnswerRepository;
import tw.com.tymbackend.module.learn.dao.LearnAttemptRepository;
import tw.com.tymbackend.module.learn.dao.LearnOptionRepository;
import tw.com.tymbackend.module.learn.dao.LearnQuestionRepository;
import tw.com.tymbackend.module.learn.dao.LearnQuizRepository;
import tw.com.tymbackend.module.learn.domain.dto.LearnDtos;
import tw.com.tymbackend.module.learn.domain.vo.LearnAnswer;
import tw.com.tymbackend.module.learn.domain.vo.LearnAttempt;
import tw.com.tymbackend.module.learn.domain.vo.LearnOption;
import tw.com.tymbackend.module.learn.domain.vo.LearnQuestion;
import tw.com.tymbackend.module.learn.domain.vo.LearnQuiz;

/**
 * Two phases, enforced here rather than in the UI.
 *
 * <p>While an attempt is IN_PROGRESS the service only ever hands back prompts and option texts.
 * Correct answers, rationales and lifetime tallies are assembled exclusively by
 * {@link #review(Long, String)} and {@link #scorecard(String, String)}, both of which require a
 * SUBMITTED attempt.
 *
 * <p>A round must be finished before another can start: {@link #startOrResume} returns the open
 * attempt if there is one, and {@link #submit} refuses to close a round with unanswered questions.
 */
@Service
public class LearnService {
    private final LearnQuizRepository quizzes;
    private final LearnQuestionRepository questions;
    private final LearnOptionRepository options;
    private final LearnAttemptRepository attempts;
    private final LearnAnswerRepository answers;

    public LearnService(LearnQuizRepository quizzes, LearnQuestionRepository questions,
                        LearnOptionRepository options, LearnAttemptRepository attempts,
                        LearnAnswerRepository answers) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.options = options;
        this.attempts = attempts;
        this.answers = answers;
    }

    // ---------------------------------------------------------------- sidebar

    @Transactional(readOnly = true)
    public List<LearnDtos.TopicSummary> listTopics(String userId) {
        return quizzes.findByPublishedTrueOrderBySortOrderAscCreatedAtDesc().stream()
            .map(quiz -> topicSummary(quiz, userId))
            .toList();
    }

    private LearnDtos.TopicSummary topicSummary(LearnQuiz quiz, String userId) {
        List<LearnQuestion> quizQuestions = questions.findByQuizIdOrderByPosition(quiz.getId());
        List<Integer> difficulties = quizQuestions.stream()
            .map(LearnQuestion::getDifficulty).filter(java.util.Objects::nonNull)
            .distinct().sorted().toList();

        Optional<LearnAttempt> open =
            attempts.findFirstByUserIdAndQuizIdAndStatus(userId, quiz.getId(), LearnAttempt.Status.IN_PROGRESS);
        long completedRounds =
            attempts.countByUserIdAndQuizIdAndStatus(userId, quiz.getId(), LearnAttempt.Status.SUBMITTED);
        Optional<LearnAttempt> last = attempts.findFirstByUserIdAndQuizIdAndStatusOrderBySubmittedAtDesc(
            userId, quiz.getId(), LearnAttempt.Status.SUBMITTED);

        LearnDtos.SessionState state = open.isPresent() ? LearnDtos.SessionState.IN_PROGRESS
            : completedRounds > 0 ? LearnDtos.SessionState.COMPLETED
            : LearnDtos.SessionState.NOT_STARTED;

        return new LearnDtos.TopicSummary(quiz.getId(), quiz.getTitle(), quiz.getDescription(),
            quiz.getPartCode(), quiz.getRecommendedMinutes(), quizQuestions.size(), difficulties, state,
            open.map(attempt -> (int) answers.countByAttemptId(attempt.getId())).orElse(0),
            completedRounds,
            last.map(LearnAttempt::getScore).orElse(null),
            last.map(LearnAttempt::getTotalQuestions).orElse(null),
            last.map(LearnAttempt::getSubmittedAt).orElse(null),
            last.map(LearnAttempt::getId).orElse(null));
    }

    // ------------------------------------------------------- answering phase

    /**
     * Resumes the candidate's open round, or shuffles a fresh one. Never leaks correctness data:
     * the payload is prompts, option texts and whatever the candidate already picked.
     */
    @Transactional
    public LearnDtos.Session startOrResume(String quizId, String userId) {
        LearnQuiz quiz = publishedQuiz(quizId);
        Optional<LearnAttempt> open =
            attempts.findFirstByUserIdAndQuizIdAndStatus(userId, quizId, LearnAttempt.Status.IN_PROGRESS);

        LearnAttempt attempt;
        boolean resumed;
        if (open.isPresent()) {
            attempt = open.get();
            resumed = true;
        } else {
            List<LearnQuestion> shuffled = shuffle(questions.findByQuizIdOrderByPosition(quizId));
            if (shuffled.isEmpty()) throw new IllegalArgumentException("這個主題還沒有題目");
            attempt = new LearnAttempt();
            attempt.setQuiz(quiz);
            attempt.setUserId(userId);
            attempt.setTotalQuestions(shuffled.size());
            attempt.setStatus(LearnAttempt.Status.IN_PROGRESS);
            attempt.setQuestionOrder(shuffled.stream().map(q -> String.valueOf(q.getId()))
                .collect(Collectors.joining(",")));
            attempts.save(attempt);
            resumed = false;
        }

        List<LearnQuestion> ordered = orderedQuestions(attempt);
        Map<Long, List<LearnOption>> optionMap = optionsByQuestion(ordered);
        Map<Long, LearnAnswer> saved = answers.findByAttemptId(attempt.getId()).stream()
            .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity(), (a, b) -> a));

        List<LearnDtos.SessionQuestion> payload = new ArrayList<>();
        for (int index = 0; index < ordered.size(); index++) {
            LearnQuestion question = ordered.get(index);
            List<LearnDtos.SessionOption> choices = optionMap.getOrDefault(question.getId(), List.of()).stream()
                .map(option -> new LearnDtos.SessionOption(option.getKey(), option.getText()))
                .toList();
            payload.add(new LearnDtos.SessionQuestion(question.getId(), index + 1, question.getSection(),
                question.getDifficulty(), question.getPassageKey(), question.getPassageText(),
                question.getPrompt(), choices,
                Optional.ofNullable(saved.get(question.getId())).map(LearnAnswer::getSelectedOption).orElse(null)));
        }

        return new LearnDtos.Session(attempt.getId(), quiz.getId(), quiz.getTitle(), quiz.getDescription(),
            quiz.getRecommendedMinutes(), attempt.getStartedAt(), ordered.size(), saved.size(), resumed, payload);
    }

    /** Stores one choice so the round survives a reload. The response says nothing about correctness. */
    @Transactional
    public LearnDtos.AnswerAck saveAnswer(String quizId, String userId, LearnDtos.AnswerInput input) {
        LearnAttempt attempt = openAttempt(quizId, userId);
        LearnQuestion question = questions.findById(input.questionId())
            .filter(q -> q.getQuiz().getId().equals(quizId))
            .orElseThrow(() -> new IllegalArgumentException("這一題不屬於本主題"));

        String choice = normalizeChoice(input.selectedOption());
        LearnAnswer answer = answers.findByAttemptIdAndQuestionId(attempt.getId(), question.getId())
            .orElseGet(() -> {
                LearnAnswer created = new LearnAnswer();
                created.setAttempt(attempt);
                created.setQuestion(question);
                return created;
            });
        answer.setSelectedOption(choice);
        answer.setCorrect(question.getCorrectOption().equals(choice));
        answers.save(answer);

        long answered = answers.countByAttemptId(attempt.getId());
        return new LearnDtos.AnswerAck(question.getId(), choice, (int) answered, attempt.getTotalQuestions(),
            answered >= attempt.getTotalQuestions());
    }

    // ---------------------------------------------------------- review phase

    /** Closes the round. Refuses while anything is unanswered, then hands back the review payload. */
    @Transactional
    public LearnDtos.Review submit(String quizId, String userId, LearnDtos.Submission submission) {
        LearnAttempt attempt = openAttempt(quizId, userId);
        if (submission != null && submission.answers() != null) {
            for (Map.Entry<Long, String> entry : submission.answers().entrySet()) {
                saveAnswer(quizId, userId, new LearnDtos.AnswerInput(entry.getKey(), entry.getValue()));
            }
        }

        List<LearnAnswer> attemptAnswers = answers.findByAttemptId(attempt.getId());
        int missing = attempt.getTotalQuestions() - attemptAnswers.size();
        if (missing > 0) {
            throw new IllegalArgumentException("還有 " + missing + " 題未作答，全部作答完才能送出並進入檢討");
        }

        attempt.setScore((int) attemptAnswers.stream().filter(LearnAnswer::isCorrect).count());
        attempt.setStatus(LearnAttempt.Status.SUBMITTED);
        attempt.setSubmittedAt(OffsetDateTime.now());
        if (submission != null && submission.durationSeconds() != null) {
            attempt.setDurationSeconds(submission.durationSeconds());
        }
        attempts.save(attempt);

        return buildReview(attempt);
    }

    @Transactional(readOnly = true)
    public LearnDtos.Review review(Long attemptId, String userId) {
        LearnAttempt attempt = attempts.findByIdAndUserId(attemptId, userId)
            .orElseThrow(() -> new IllegalArgumentException("找不到這次作答紀錄"));
        if (attempt.getStatus() != LearnAttempt.Status.SUBMITTED) {
            throw new IllegalArgumentException("這一輪還沒送出，作答完成後才能看檢討");
        }
        return buildReview(attempt);
    }

    private LearnDtos.Review buildReview(LearnAttempt attempt) {
        LearnQuiz quiz = attempt.getQuiz();
        List<LearnQuestion> ordered = orderedQuestions(attempt);
        Map<Long, List<LearnOption>> optionMap = optionsByQuestion(ordered);
        Map<Long, LearnAnswer> given = answers.findByAttemptId(attempt.getId()).stream()
            .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity(), (a, b) -> a));
        Map<Long, long[]> lifetime = performance(attempt.getUserId(), quiz.getId());

        List<LearnDtos.ReviewItem> items = new ArrayList<>();
        for (int index = 0; index < ordered.size(); index++) {
            LearnQuestion question = ordered.get(index);
            LearnAnswer answer = given.get(question.getId());
            String selected = answer == null ? null : answer.getSelectedOption();
            long[] counts = lifetime.getOrDefault(question.getId(), new long[2]);

            List<LearnDtos.ReviewOption> choices = optionMap.getOrDefault(question.getId(), List.of()).stream()
                .map(option -> new LearnDtos.ReviewOption(option.getKey(), option.getText(),
                    option.getRationale(), option.getKey().equals(question.getCorrectOption()),
                    option.getKey().equals(selected)))
                .toList();

            items.add(new LearnDtos.ReviewItem(question.getId(), index + 1, question.getPosition(),
                question.getSection(), question.getDifficulty(), question.getFocusPoint(),
                question.getPassageKey(), question.getPassageText(), question.getPrompt(), choices,
                selected, question.getCorrectOption(), answer != null && answer.isCorrect(),
                question.getExplanation(), counts[0], counts[1]));
        }

        return new LearnDtos.Review(attempt.getId(), quiz.getId(), quiz.getTitle(), attempt.getScore(),
            attempt.getTotalQuestions(), attempt.getDurationSeconds(), attempt.getSubmittedAt(), items);
    }

    /** Cumulative per-question tally across every completed round, worst questions first. */
    @Transactional(readOnly = true)
    public LearnDtos.Scorecard scorecard(String quizId, String userId) {
        LearnQuiz quiz = publishedQuiz(quizId);
        Map<Long, long[]> lifetime = performance(userId, quizId);
        long completedRounds =
            attempts.countByUserIdAndQuizIdAndStatus(userId, quizId, LearnAttempt.Status.SUBMITTED);

        List<LearnDtos.ScorecardRow> rows = questions.findByQuizIdOrderByPosition(quizId).stream()
            .map(question -> {
                long[] counts = lifetime.getOrDefault(question.getId(), new long[2]);
                return new LearnDtos.ScorecardRow(question.getId(), question.getPosition(),
                    question.getSection(), question.getDifficulty(), question.getFocusPoint(),
                    question.getPrompt(), counts[0] + counts[1], counts[0], counts[1]);
            })
            .sorted(Comparator.comparingLong(LearnDtos.ScorecardRow::incorrectCount).reversed()
                .thenComparing(row -> row.position() == null ? 0 : row.position()))
            .toList();

        return new LearnDtos.Scorecard(quiz.getId(), quiz.getTitle(), completedRounds, rows.size(),
            rows.stream().mapToLong(LearnDtos.ScorecardRow::correctCount).sum(),
            rows.stream().mapToLong(LearnDtos.ScorecardRow::incorrectCount).sum(), rows);
    }

    @Transactional(readOnly = true)
    public List<LearnDtos.AttemptSummary> history(String userId) {
        return attempts.findTop20ByUserIdAndStatusOrderBySubmittedAtDesc(userId, LearnAttempt.Status.SUBMITTED)
            .stream()
            .map(a -> new LearnDtos.AttemptSummary(a.getId(), a.getQuiz().getId(), a.getQuiz().getTitle(),
                a.getScore(), a.getTotalQuestions(), a.getDurationSeconds(), a.getSubmittedAt()))
            .toList();
    }

    // ---------------------------------------------------------------- helpers

    /**
     * Shuffles at passage level: a Part 6/7 passage and its questions travel together and keep their
     * printed order, while standalone Part 5 items move freely.
     */
    private List<LearnQuestion> shuffle(List<LearnQuestion> quizQuestions) {
        Map<String, List<LearnQuestion>> groups = new LinkedHashMap<>();
        for (LearnQuestion question : quizQuestions) {
            String key = question.getPassageKey() == null || question.getPassageKey().isBlank()
                ? "solo-" + question.getId()
                : "passage-" + question.getPassageKey();
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(question);
        }
        List<List<LearnQuestion>> blocks = new ArrayList<>(groups.values());
        java.util.Collections.shuffle(blocks, new Random());
        return blocks.stream().flatMap(List::stream).toList();
    }

    /** Rebuilds the attempt's frozen order, tolerating questions added or removed since it started. */
    private List<LearnQuestion> orderedQuestions(LearnAttempt attempt) {
        List<LearnQuestion> available = questions.findByQuizIdOrderByPosition(attempt.getQuiz().getId());
        if (attempt.getQuestionOrder() == null || attempt.getQuestionOrder().isBlank()) return available;

        Map<Long, LearnQuestion> byId = available.stream()
            .collect(Collectors.toMap(LearnQuestion::getId, Function.identity()));
        List<LearnQuestion> ordered = new ArrayList<>();
        for (String token : attempt.getQuestionOrder().split(",")) {
            LearnQuestion question = byId.remove(parseId(token));
            if (question != null) ordered.add(question);
        }
        // Anything seeded after this round began gets appended rather than dropped.
        ordered.addAll(byId.values());
        return ordered;
    }

    private Long parseId(String token) {
        try {
            return Long.valueOf(token.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Map<Long, List<LearnOption>> optionsByQuestion(List<LearnQuestion> quizQuestions) {
        if (quizQuestions.isEmpty()) return Map.of();
        return options.findByQuestionIdInOrderByQuestionIdAscKeyAsc(
                quizQuestions.stream().map(LearnQuestion::getId).toList()).stream()
            .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
    }

    private LearnAttempt openAttempt(String quizId, String userId) {
        publishedQuiz(quizId);
        return attempts.findFirstByUserIdAndQuizIdAndStatus(userId, quizId, LearnAttempt.Status.IN_PROGRESS)
            .orElseThrow(() -> new IllegalArgumentException("目前沒有進行中的作答，請先開始這個主題"));
    }

    private LearnQuiz publishedQuiz(String quizId) {
        LearnQuiz quiz = quizzes.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("找不到這個主題"));
        if (!quiz.isPublished()) throw new IllegalArgumentException("找不到這個主題");
        return quiz;
    }

    private Map<Long, long[]> performance(String userId, String quizId) {
        Map<Long, long[]> result = new HashMap<>();
        for (LearnAnswerRepository.QuestionPerformance row :
                answers.performance(userId, quizId, LearnAttempt.Status.SUBMITTED)) {
            result.put(row.getQuestionId(), new long[] {row.getCorrectCount(), row.getIncorrectCount()});
        }
        return result;
    }

    private String normalizeChoice(String choice) {
        if (choice == null || choice.isBlank()) throw new IllegalArgumentException("請選擇一個選項");
        String normalized = choice.trim().toUpperCase();
        if (!normalized.matches("[A-D]")) throw new IllegalArgumentException("答案必須是 A、B、C 或 D");
        return normalized;
    }
}
