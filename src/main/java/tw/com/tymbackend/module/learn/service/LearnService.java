package tw.com.tymbackend.module.learn.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Transactional(readOnly = true)
    public List<LearnDtos.QuizSummary> listQuizzes() {
        return quizzes.findByPublishedTrueOrderByCreatedAtDesc().stream()
            .map(q -> new LearnDtos.QuizSummary(q.getId(), q.getTitle(), q.getDescription(),
                q.getRecommendedMinutes(), questions.countByQuizId(q.getId())))
            .toList();
    }

    @Transactional(readOnly = true)
    public LearnDtos.Quiz getQuiz(String quizId, String userId) {
        LearnQuiz quiz = publishedQuiz(quizId);
        List<LearnQuestion> quizQuestions = questions.findByQuizIdOrderByPosition(quizId);
        Map<Long, List<LearnDtos.Option>> optionMap = optionMap(quizQuestions);
        Map<Long, long[]> performance = performance(userId, quizId);
        List<LearnDtos.Question> result = quizQuestions.stream().map(question -> {
            long[] counts = performance.getOrDefault(question.getId(), new long[2]);
            return new LearnDtos.Question(question.getId(), question.getPosition(), question.getSection(),
                question.getPassageKey(), question.getPassageText(), question.getPrompt(),
                optionMap.getOrDefault(question.getId(), List.of()), counts[0], counts[1]);
        }).toList();
        return new LearnDtos.Quiz(quiz.getId(), quiz.getTitle(), quiz.getDescription(),
            quiz.getRecommendedMinutes(), result);
    }

    @Transactional
    public LearnDtos.AttemptResult submit(String quizId, String userId, LearnDtos.Submission submission) {
        LearnQuiz quiz = publishedQuiz(quizId);
        List<LearnQuestion> quizQuestions = questions.findByQuizIdOrderByPosition(quizId);
        Map<Long, String> selected = submission.answers() == null ? Map.of() : submission.answers();
        Map<Long, LearnQuestion> byId = quizQuestions.stream()
            .collect(Collectors.toMap(LearnQuestion::getId, Function.identity()));
        if (!byId.keySet().containsAll(selected.keySet())) {
            throw new IllegalArgumentException("Submission contains a question outside this quiz");
        }

        LearnAttempt attempt = new LearnAttempt();
        attempt.setQuiz(quiz);
        attempt.setUserId(userId);
        attempt.setTotalQuestions(quizQuestions.size());
        attempt.setDurationSeconds(submission.durationSeconds());

        int score = 0;
        List<LearnAnswer> savedAnswers = new ArrayList<>();
        for (LearnQuestion question : quizQuestions) {
            String choice = normalizeChoice(selected.get(question.getId()));
            boolean correct = question.getCorrectOption().equals(choice);
            if (correct) score++;
            LearnAnswer answer = new LearnAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(choice);
            answer.setCorrect(correct);
            savedAnswers.add(answer);
        }
        attempt.setScore(score);
        attempts.save(attempt);
        answers.saveAll(savedAnswers);

        Map<Long, long[]> performance = performance(userId, quizId);
        List<LearnDtos.AnswerResult> results = savedAnswers.stream().map(answer -> {
            LearnQuestion question = answer.getQuestion();
            long[] counts = performance.getOrDefault(question.getId(), new long[2]);
            return new LearnDtos.AnswerResult(question.getId(), answer.getSelectedOption(),
                question.getCorrectOption(), answer.isCorrect(), question.getExplanation(), counts[0], counts[1]);
        }).toList();
        return new LearnDtos.AttemptResult(attempt.getId(), score, quizQuestions.size(),
            attempt.getSubmittedAt(), results);
    }

    @Transactional(readOnly = true)
    public List<LearnDtos.AttemptSummary> history(String userId) {
        return attempts.findTop20ByUserIdOrderBySubmittedAtDesc(userId).stream()
            .map(a -> new LearnDtos.AttemptSummary(a.getId(), a.getQuiz().getId(), a.getQuiz().getTitle(),
                a.getScore(), a.getTotalQuestions(), a.getDurationSeconds(), a.getSubmittedAt()))
            .toList();
    }

    private LearnQuiz publishedQuiz(String quizId) {
        LearnQuiz quiz = quizzes.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        if (!quiz.isPublished()) throw new IllegalArgumentException("Quiz not found");
        return quiz;
    }

    private Map<Long, List<LearnDtos.Option>> optionMap(List<LearnQuestion> quizQuestions) {
        if (quizQuestions.isEmpty()) return Map.of();
        return options.findByQuestionIdInOrderByQuestionIdAscKeyAsc(
                quizQuestions.stream().map(LearnQuestion::getId).toList()).stream()
            .collect(Collectors.groupingBy(o -> o.getQuestion().getId(),
                Collectors.mapping(o -> new LearnDtos.Option(o.getKey(), o.getText()), Collectors.toList())));
    }

    private Map<Long, long[]> performance(String userId, String quizId) {
        Map<Long, long[]> result = new HashMap<>();
        for (LearnAnswerRepository.QuestionPerformance row : answers.performance(userId, quizId)) {
            result.put(row.getQuestionId(), new long[] {row.getCorrectCount(), row.getIncorrectCount()});
        }
        return result;
    }

    private String normalizeChoice(String choice) {
        if (choice == null || choice.isBlank()) return null;
        String normalized = choice.trim().toUpperCase();
        if (!normalized.matches("[A-D]")) throw new IllegalArgumentException("Answer must be A, B, C or D");
        return normalized;
    }
}
