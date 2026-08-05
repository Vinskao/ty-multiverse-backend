package tw.com.tymbackend.module.learn.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tw.com.tymbackend.module.learn.domain.vo.LearnQuestion;

public interface LearnQuestionRepository extends JpaRepository<LearnQuestion, Long> {
    List<LearnQuestion> findByQuizIdOrderByPosition(String quizId);
    long countByQuizId(String quizId);
}
