package tw.com.tymbackend.module.learn.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
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
    /** Accounts allowed into mentor mode, lower-cased. Configured by {@code learn.mentors}. */
    private final Set<String> mentors;

    public LearnService(LearnQuizRepository quizzes, LearnQuestionRepository questions,
                        LearnOptionRepository options, LearnAttemptRepository attempts,
                        LearnAnswerRepository answers,
                        @Value("${learn.mentors:chiaki}") List<String> mentors) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.options = options;
        this.attempts = attempts;
        this.answers = answers;
        this.mentors = mentors.stream().map(String::trim).filter(name -> !name.isEmpty())
            .map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    public LearnDtos.Profile profile(String userId, String displayName) {
        return new LearnDtos.Profile(label(userId, displayName), isMentor(userId, displayName));
    }

    /** Mentors may be configured by username or by token subject; either match opens mentor mode. */
    public boolean isMentor(String userId, String displayName) {
        return matchesMentor(displayName) || matchesMentor(userId);
    }

    private boolean matchesMentor(String name) {
        return name != null && mentors.contains(name.toLowerCase(Locale.ROOT));
    }

    /** The subject is a UUID, so fall back to it only when no display name was ever recorded. */
    private String label(String userId, String displayName) {
        return displayName == null || displayName.isBlank() ? userId : displayName;
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
    public LearnDtos.Session startOrResume(String quizId, String userId, String displayName) {
        LearnQuiz quiz = publishedQuiz(quizId);
        Optional<LearnAttempt> open =
            attempts.findFirstByUserIdAndQuizIdAndStatus(userId, quizId, LearnAttempt.Status.IN_PROGRESS);

        LearnAttempt attempt;
        boolean resumed;
        if (open.isPresent()) {
            attempt = open.get();
            attempt.setDisplayName(label(userId, displayName));
            resumed = true;
        } else {
            List<LearnQuestion> shuffled = shuffle(questions.findByQuizIdOrderByPosition(quizId));
            if (shuffled.isEmpty()) throw new IllegalArgumentException("這個主題還沒有題目");
            attempt = new LearnAttempt();
            attempt.setQuiz(quiz);
            attempt.setUserId(userId);
            attempt.setDisplayName(label(userId, displayName));
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
        Map<Long, long[]> cohort = cohortPerformance(quiz.getId());

        List<LearnDtos.ReviewItem> items = new ArrayList<>();
        for (int index = 0; index < ordered.size(); index++) {
            LearnQuestion question = ordered.get(index);
            LearnAnswer answer = given.get(question.getId());
            String selected = answer == null ? null : answer.getSelectedOption();
            long[] counts = lifetime.getOrDefault(question.getId(), new long[2]);
            long[] crowd = cohort.getOrDefault(question.getId(), new long[2]);

            List<LearnDtos.ReviewOption> choices = optionMap.getOrDefault(question.getId(), List.of()).stream()
                .map(option -> new LearnDtos.ReviewOption(option.getKey(), option.getText(),
                    option.getRationale(), option.getKey().equals(question.getCorrectOption()),
                    option.getKey().equals(selected)))
                .toList();

            items.add(new LearnDtos.ReviewItem(question.getId(), index + 1, question.getPosition(),
                question.getSection(), question.getDifficulty(), question.getFocusPoint(),
                question.getPassageKey(), question.getPassageText(), question.getPrompt(), choices,
                selected, question.getCorrectOption(), answer != null && answer.isCorrect(),
                question.getExplanation(), counts[0], counts[1],
                crowd[0], crowd[1], accuracy(crowd[0], crowd[0] + crowd[1])));
        }

        return new LearnDtos.Review(attempt.getId(), quiz.getId(), quiz.getTitle(), attempt.getScore(),
            attempt.getTotalQuestions(), attempt.getDurationSeconds(), attempt.getSubmittedAt(), items,
            ranking(quiz.getId(), attempt.getUserId()));
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

    // -------------------------------------------------------------- ranking

    /**
     * Leaderboard for one topic. Everyone may read it: it exposes per-learner aggregates only, never
     * another learner's answers, and it is the same data the review page shows under each question.
     */
    @Transactional(readOnly = true)
    public LearnDtos.Ranking ranking(String quizId, String userId) {
        LearnQuiz quiz = publishedQuiz(quizId);
        long questionCount = questions.countByQuizId(quizId);

        // Accuracy first, then who has put in more rounds; name only as a stable tie-breaker.
        Comparator<LearnAttemptRepository.LearnerStats> order =
            Comparator.<LearnAttemptRepository.LearnerStats>comparingDouble(this::accuracyOf).reversed()
                .thenComparing(Comparator.<LearnAttemptRepository.LearnerStats>comparingLong(
                    LearnAttemptRepository.LearnerStats::getRounds).reversed())
                .thenComparing(LearnAttemptRepository.LearnerStats::getUserId);

        List<LearnAttemptRepository.LearnerStats> stats =
            attempts.statsByQuiz(quizId, LearnAttempt.Status.SUBMITTED).stream().sorted(order).toList();

        List<LearnDtos.RankingRow> rows = new ArrayList<>();
        Integer selfRank = null;
        for (int index = 0; index < stats.size(); index++) {
            LearnAttemptRepository.LearnerStats row = stats.get(index);
            boolean self = row.getUserId().equals(userId);
            if (self) selfRank = index + 1;
            rows.add(new LearnDtos.RankingRow(index + 1, label(row.getUserId(), row.getDisplayName()),
                self, row.getRounds(),
                row.getAverageScore() == null ? 0 : row.getAverageScore(), accuracyOf(row),
                row.getBestScore(), row.getAverageDurationSeconds(), row.getLastSubmittedAt()));
        }

        return new LearnDtos.Ranking(quiz.getId(), quiz.getTitle(), questionCount, rows.size(),
            average(rows.stream().mapToDouble(LearnDtos.RankingRow::averageScore)),
            average(rows.stream().mapToDouble(LearnDtos.RankingRow::averageAccuracy)),
            selfRank, rows);
    }

    // --------------------------------------------------------- mentor mode

    /**
     * Everything every learner has done, in one payload. Restricted to the configured mentor
     * accounts — {@link #isMentor(String)} is checked here rather than in the controller so no other
     * entry point can hand the data out by accident.
     */
    @Transactional(readOnly = true)
    public LearnDtos.MentorOverview mentorOverview(String userId, String displayName) {
        if (!isMentor(userId, displayName)) throw new AccessDeniedException("只有導師帳號可以看全班作答狀況");

        Map<String, LearnQuiz> quizById = quizzes.findByPublishedTrueOrderBySortOrderAscCreatedAtDesc()
            .stream().collect(Collectors.toMap(LearnQuiz::getId, Function.identity(), (a, b) -> a,
                LinkedHashMap::new));
        Map<String, Long> questionCounts = new HashMap<>();
        for (String quizId : quizById.keySet()) questionCounts.put(quizId, questions.countByQuizId(quizId));

        // learner -> quiz -> submitted aggregate
        Map<String, Map<String, LearnAttemptRepository.LearnerStats>> submitted = new TreeMap<>();
        Map<String, String> names = new HashMap<>();
        for (LearnAttemptRepository.LearnerStats row :
                attempts.statsByLearnerAndQuiz(LearnAttempt.Status.SUBMITTED)) {
            if (!quizById.containsKey(row.getQuizId())) continue;
            submitted.computeIfAbsent(row.getUserId(), ignored -> new LinkedHashMap<>())
                .put(row.getQuizId(), row);
            if (row.getDisplayName() != null) names.putIfAbsent(row.getUserId(), row.getDisplayName());
        }

        // learner -> quiz -> questions answered so far in the round still open
        Map<String, Map<String, Integer>> open = new HashMap<>();
        for (LearnAttempt attempt : attempts.findByStatus(LearnAttempt.Status.IN_PROGRESS)) {
            if (!quizById.containsKey(attempt.getQuiz().getId())) continue;
            open.computeIfAbsent(attempt.getUserId(), ignored -> new HashMap<>())
                .put(attempt.getQuiz().getId(), (int) answers.countByAttemptId(attempt.getId()));
            submitted.computeIfAbsent(attempt.getUserId(), ignored -> new LinkedHashMap<>());
            if (attempt.getDisplayName() != null) names.putIfAbsent(attempt.getUserId(), attempt.getDisplayName());
        }

        List<LearnDtos.MentorLearner> learners = new ArrayList<>();
        for (Map.Entry<String, Map<String, LearnAttemptRepository.LearnerStats>> entry : submitted.entrySet()) {
            String learner = entry.getKey();
            Map<String, Integer> openRounds = open.getOrDefault(learner, Map.of());

            List<LearnDtos.MentorTopicProgress> topics = new ArrayList<>();
            long totalRounds = 0;
            long totalCorrect = 0;
            long totalAnswered = 0;
            OffsetDateTime lastActive = null;
            int inProgress = 0;
            int completed = 0;

            for (LearnQuiz quiz : quizById.values()) {
                LearnAttemptRepository.LearnerStats row = entry.getValue().get(quiz.getId());
                Integer answeredNow = openRounds.get(quiz.getId());
                if (row == null && answeredNow == null) continue;

                long rounds = row == null ? 0 : row.getRounds();
                totalRounds += rounds;
                if (row != null) {
                    totalCorrect += row.getTotalScore() == null ? 0 : row.getTotalScore();
                    totalAnswered += row.getTotalAnswered() == null ? 0 : row.getTotalAnswered();
                    if (lastActive == null || (row.getLastSubmittedAt() != null
                            && row.getLastSubmittedAt().isAfter(lastActive))) {
                        lastActive = row.getLastSubmittedAt();
                    }
                }

                LearnDtos.SessionState state = answeredNow != null ? LearnDtos.SessionState.IN_PROGRESS
                    : rounds > 0 ? LearnDtos.SessionState.COMPLETED
                    : LearnDtos.SessionState.NOT_STARTED;
                if (state == LearnDtos.SessionState.IN_PROGRESS) inProgress++;
                if (state == LearnDtos.SessionState.COMPLETED) completed++;

                Optional<LearnAttempt> last = attempts.findFirstByUserIdAndQuizIdAndStatusOrderBySubmittedAtDesc(
                    learner, quiz.getId(), LearnAttempt.Status.SUBMITTED);

                topics.add(new LearnDtos.MentorTopicProgress(quiz.getId(), quiz.getTitle(), state,
                    questionCounts.getOrDefault(quiz.getId(), 0L), rounds,
                    answeredNow == null ? 0 : answeredNow,
                    row == null ? null : row.getAverageScore(),
                    row == null ? null : accuracyOf(row),
                    row == null ? null : row.getBestScore(),
                    last.map(LearnAttempt::getScore).orElse(null),
                    row == null ? null : row.getLastSubmittedAt()));
            }

            learners.add(new LearnDtos.MentorLearner(label(learner, names.get(learner)), totalRounds,
                totalAnswered == 0 ? null : accuracy(totalCorrect, totalAnswered),
                lastActive, inProgress, completed, topics));
        }

        learners.sort(Comparator.comparing(LearnDtos.MentorLearner::lastActiveAt,
            Comparator.nullsLast(Comparator.reverseOrder())));

        return new LearnDtos.MentorOverview(learners.size(),
            learners.stream().mapToLong(LearnDtos.MentorLearner::totalRounds).sum(),
            average(learners.stream().map(LearnDtos.MentorLearner::averageAccuracy)
                .filter(java.util.Objects::nonNull).mapToDouble(Double::doubleValue)),
            learners);
    }

    // ---------------------------------------------------------------- helpers

    private double accuracyOf(LearnAttemptRepository.LearnerStats row) {
        long answered = row.getTotalAnswered() == null ? 0 : row.getTotalAnswered();
        long correct = row.getTotalScore() == null ? 0 : row.getTotalScore();
        return answered == 0 ? 0 : (double) correct / answered;
    }

    private Double accuracy(long correct, long total) {
        return total == 0 ? null : (double) correct / total;
    }

    private Double average(java.util.stream.DoubleStream values) {
        return values.average().stream().boxed().findFirst().orElse(null);
    }


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

    /** Same shape as {@link #performance}, pooled over every learner who has submitted a round. */
    private Map<Long, long[]> cohortPerformance(String quizId) {
        Map<Long, long[]> result = new HashMap<>();
        for (LearnAnswerRepository.QuestionPerformance row :
                answers.cohortPerformance(quizId, LearnAttempt.Status.SUBMITTED)) {
            result.put(row.getQuestionId(), new long[] {row.getCorrectCount(), row.getIncorrectCount()});
        }
        return result;
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
