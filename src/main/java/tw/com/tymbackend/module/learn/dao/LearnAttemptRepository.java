package tw.com.tymbackend.module.learn.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.com.tymbackend.module.learn.domain.vo.LearnAttempt;

public interface LearnAttemptRepository extends JpaRepository<LearnAttempt, Long> {
    List<LearnAttempt> findTop20ByUserIdOrderBySubmittedAtDesc(String userId);
}
