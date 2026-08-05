package tw.com.tymbackend.module.learn.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.tymbackend.module.learn.domain.vo.LearnAttempt;

public interface LearnAttemptRepository extends JpaRepository<LearnAttempt, Long> {
    Optional<LearnAttempt> findFirstByUserIdAndQuizIdAndStatus(String userId, String quizId,
                                                              LearnAttempt.Status status);

    Optional<LearnAttempt> findFirstByUserIdAndQuizIdAndStatusOrderBySubmittedAtDesc(
        String userId, String quizId, LearnAttempt.Status status);

    Optional<LearnAttempt> findByIdAndUserId(Long id, String userId);

    List<LearnAttempt> findByUserIdAndStatus(String userId, LearnAttempt.Status status);

    List<LearnAttempt> findTop20ByUserIdAndStatusOrderBySubmittedAtDesc(String userId,
                                                                       LearnAttempt.Status status);

    long countByUserIdAndQuizIdAndStatus(String userId, String quizId, LearnAttempt.Status status);
}
