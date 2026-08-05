package tw.com.tymbackend.module.learn.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tw.com.tymbackend.module.learn.domain.vo.LearnAnswer;
import tw.com.tymbackend.module.learn.domain.vo.LearnAttempt;

public interface LearnAnswerRepository extends JpaRepository<LearnAnswer, Long> {
    interface QuestionPerformance {
        Long getQuestionId();
        Long getCorrectCount();
        Long getIncorrectCount();
    }

    List<LearnAnswer> findByAttemptId(Long attemptId);

    Optional<LearnAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    long countByAttemptId(Long attemptId);

    /**
     * Lifetime tallies. Only submitted attempts count, so an abandoned half-finished round never
     * distorts the scorecard.
     */
    @Query("""
        select a.question.id as questionId,
               sum(case when a.correct = true then 1 else 0 end) as correctCount,
               sum(case when a.correct = false then 1 else 0 end) as incorrectCount
        from LearnAnswer a
        where a.attempt.userId = :userId
          and a.attempt.quiz.id = :quizId
          and a.attempt.status = :status
        group by a.question.id
        """)
    List<QuestionPerformance> performance(@Param("userId") String userId, @Param("quizId") String quizId,
                                          @Param("status") LearnAttempt.Status status);
}
