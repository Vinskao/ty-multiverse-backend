package tw.com.tymbackend.module.learn.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.com.tymbackend.module.learn.domain.vo.LearnQuiz;

public interface LearnQuizRepository extends JpaRepository<LearnQuiz, String> {
    List<LearnQuiz> findByPublishedTrueOrderByCreatedAtDesc();
}
