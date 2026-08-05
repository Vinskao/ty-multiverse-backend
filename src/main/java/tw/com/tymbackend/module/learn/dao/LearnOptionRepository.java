package tw.com.tymbackend.module.learn.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.tymbackend.module.learn.domain.vo.LearnOption;

public interface LearnOptionRepository extends JpaRepository<LearnOption, Long> {
    List<LearnOption> findByQuestionIdInOrderByQuestionIdAscKeyAsc(Collection<Long> questionIds);

    List<LearnOption> findByQuestionIdOrderByKeyAsc(Long questionId);
}
