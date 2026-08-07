package tw.com.tymbackend.module.learn.dao;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tw.com.tymbackend.module.learn.domain.vo.LearnAttempt;

public interface LearnAttemptRepository extends JpaRepository<LearnAttempt, Long> {
    /** One learner's aggregate over the submitted rounds of a single topic. */
    interface LearnerStats {
        String getUserId();
        String getDisplayName();
        String getQuizId();
        Long getRounds();
        Double getAverageScore();
        Integer getBestScore();
        Long getTotalScore();
        Long getTotalAnswered();
        Double getAverageDurationSeconds();
        OffsetDateTime getLastSubmittedAt();
    }

    Optional<LearnAttempt> findFirstByUserIdAndQuizIdAndStatus(String userId, String quizId,
                                                              LearnAttempt.Status status);

    Optional<LearnAttempt> findFirstByUserIdAndQuizIdAndStatusOrderBySubmittedAtDesc(
        String userId, String quizId, LearnAttempt.Status status);

    Optional<LearnAttempt> findByIdAndUserId(Long id, String userId);

    List<LearnAttempt> findByUserIdAndStatus(String userId, LearnAttempt.Status status);

    List<LearnAttempt> findTop20ByUserIdAndStatusOrderBySubmittedAtDesc(String userId,
                                                                       LearnAttempt.Status status);

    long countByUserIdAndQuizIdAndStatus(String userId, String quizId, LearnAttempt.Status status);

    /** Cohort ranking source for one topic: one row per learner who has finished at least one round. */
    @Query("""
        select a.userId as userId, max(a.displayName) as displayName, a.quiz.id as quizId, count(a) as rounds,
               avg(a.score) as averageScore, max(a.score) as bestScore, sum(a.score) as totalScore,
               sum(a.totalQuestions) as totalAnswered, avg(a.durationSeconds) as averageDurationSeconds,
               max(a.submittedAt) as lastSubmittedAt
        from LearnAttempt a
        where a.quiz.id = :quizId and a.status = :status
        group by a.userId, a.quiz.id
        """)
    List<LearnerStats> statsByQuiz(@Param("quizId") String quizId, @Param("status") LearnAttempt.Status status);

    /** Mentor overview source: every learner × every topic they have finished a round of. */
    @Query("""
        select a.userId as userId, max(a.displayName) as displayName, a.quiz.id as quizId, count(a) as rounds,
               avg(a.score) as averageScore, max(a.score) as bestScore, sum(a.score) as totalScore,
               sum(a.totalQuestions) as totalAnswered, avg(a.durationSeconds) as averageDurationSeconds,
               max(a.submittedAt) as lastSubmittedAt
        from LearnAttempt a
        where a.status = :status
        group by a.userId, a.quiz.id
        """)
    List<LearnerStats> statsByLearnerAndQuiz(@Param("status") LearnAttempt.Status status);

    List<LearnAttempt> findByStatus(LearnAttempt.Status status);
}
