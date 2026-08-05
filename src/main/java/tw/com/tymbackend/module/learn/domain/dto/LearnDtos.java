package tw.com.tymbackend.module.learn.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class LearnDtos {
    private LearnDtos() {}

    public record QuizSummary(String id, String title, String description, Integer recommendedMinutes,
                              long questionCount) {}
    public record Option(String key, String text) {}
    public record Question(Long id, Integer position, String section, String passageKey, String passageText,
                           String prompt, List<Option> options, long correctCount, long incorrectCount) {}
    public record Quiz(String id, String title, String description, Integer recommendedMinutes,
                       List<Question> questions) {}
    public record Submission(Map<Long, String> answers, Integer durationSeconds) {}
    public record AnswerResult(Long questionId, String selectedOption, String correctOption, boolean correct,
                               String explanation, long correctCount, long incorrectCount) {}
    public record AttemptResult(Long attemptId, int score, int totalQuestions, OffsetDateTime submittedAt,
                                List<AnswerResult> answers) {}
    public record AttemptSummary(Long attemptId, String quizId, String quizTitle, int score, int totalQuestions,
                                 Integer durationSeconds, OffsetDateTime submittedAt) {}
}
