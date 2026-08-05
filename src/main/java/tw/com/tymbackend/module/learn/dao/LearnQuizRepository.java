package tw.com.tymbackend.module.learn.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.tymbackend.module.learn.domain.vo.LearnQuiz;

public interface LearnQuizRepository extends JpaRepository<LearnQuiz, String> {
    /** Sidebar order: explicit sort_order first, newest as the tie-breaker. */
    List<LearnQuiz> findByPublishedTrueOrderBySortOrderAscCreatedAtDesc();
}
