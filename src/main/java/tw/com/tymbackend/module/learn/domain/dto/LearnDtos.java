package tw.com.tymbackend.module.learn.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * The learn module runs in two strictly separated phases.
 *
 * <p>ANSWERING — everything under {@link Session} / {@link AnswerAck}. These payloads never carry
 * the correct option, the explanation, the per-option rationale, or the candidate's lifetime
 * statistics, so nothing on the page can hint at the answer while the test is being taken.
 *
 * <p>REVIEW — everything under {@link Review} / {@link Scorecard}, only reachable once the attempt
 * has been submitted with every question answered.
 */
public final class LearnDtos {
    private LearnDtos() {}

    /** Where the candidate stands on a topic. */
    public enum SessionState { NOT_STARTED, IN_PROGRESS, COMPLETED }

    // ---------------------------------------------------------------- sidebar

    public record TopicSummary(String id, String title, String description, String partCode,
                               Integer recommendedMinutes, long questionCount, List<Integer> difficulties,
                               SessionState state, int answeredCount, long completedRounds,
                               Integer lastScore, Integer lastTotal, OffsetDateTime lastSubmittedAt,
                               Long lastAttemptId) {}

    // ------------------------------------------------------- answering phase

    public record SessionOption(String key, String text) {}

    public record SessionQuestion(Long id, int order, String section, Integer difficulty, String passageKey,
                                  String passageText, String prompt, List<SessionOption> options,
                                  String selectedOption) {}

    public record Session(Long attemptId, String quizId, String quizTitle, String description,
                          Integer recommendedMinutes, OffsetDateTime startedAt, int totalQuestions,
                          int answeredCount, boolean resumed, List<SessionQuestion> questions) {}

    public record AnswerInput(Long questionId, String selectedOption) {}

    /** Deliberately says nothing about whether the choice was right. */
    public record AnswerAck(Long questionId, String selectedOption, int answeredCount, int totalQuestions,
                            boolean complete) {}

    public record Submission(Integer durationSeconds, Map<Long, String> answers) {}

    // ---------------------------------------------------------- review phase

    public record ReviewOption(String key, String text, String rationale, boolean correct, boolean selected) {}

    public record ReviewItem(Long questionId, int order, Integer position, String section, Integer difficulty,
                             String focusPoint, String passageKey, String passageText, String prompt,
                             List<ReviewOption> options, String selectedOption, String correctOption,
                             boolean correct, String explanation, long lifetimeCorrect, long lifetimeIncorrect,
                             long cohortCorrect, long cohortIncorrect, Double cohortAccuracy) {}

    public record Review(Long attemptId, String quizId, String quizTitle, int score, int totalQuestions,
                         Integer durationSeconds, OffsetDateTime submittedAt, List<ReviewItem> items,
                         Ranking ranking) {}

    public record ScorecardRow(Long questionId, Integer position, String section, Integer difficulty,
                               String focusPoint, String prompt, long timesAnswered, long correctCount,
                               long incorrectCount) {}

    public record Scorecard(String quizId, String quizTitle, long completedRounds, long questionCount,
                            long totalCorrect, long totalIncorrect, List<ScorecardRow> rows) {}

    public record AttemptSummary(Long attemptId, String quizId, String quizTitle, int score, int totalQuestions,
                                 Integer durationSeconds, OffsetDateTime submittedAt) {}

    // -------------------------------------------------------------- ranking

    /**
     * One learner's standing on a topic. Only aggregates travel here — never the individual answers
     * of somebody else, so a leaderboard can be shown without leaking another candidate's paper.
     */
    public record RankingRow(int rank, String userId, boolean self, long rounds, double averageScore,
                             double averageAccuracy, Integer bestScore, Double averageDurationSeconds,
                             OffsetDateTime lastSubmittedAt) {}

    /** Cohort leaderboard for one topic, plus where the requesting learner sits in it. */
    public record Ranking(String quizId, String quizTitle, long questionCount, int learnerCount,
                          Double cohortAverageScore, Double cohortAverageAccuracy, Integer selfRank,
                          List<RankingRow> rows) {}

    // --------------------------------------------------------- mentor mode

    public record MentorTopicProgress(String quizId, String quizTitle, SessionState state, long questionCount,
                                      long completedRounds, int answeredCount, Double averageScore,
                                      Double averageAccuracy, Integer bestScore, Integer lastScore,
                                      OffsetDateTime lastSubmittedAt) {}

    public record MentorLearner(String userId, long totalRounds, Double averageAccuracy,
                                OffsetDateTime lastActiveAt, int topicsInProgress, int topicsCompleted,
                                List<MentorTopicProgress> topics) {}

    public record MentorOverview(int learnerCount, long totalRounds, Double cohortAverageAccuracy,
                                 List<MentorLearner> learners) {}

    /** What the page needs to decide which mode to open in. */
    public record Profile(String userId, boolean mentor) {}
}
