package tw.com.tymbackend.module.learn.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import tw.com.tymbackend.module.learn.dao.*;
import tw.com.tymbackend.module.learn.domain.vo.*;

class LearnRecentAttemptsTest {
    @Test void returnsSubmittedAttemptsAcrossLearnersWithNamesAndScores() {
        var repo = mock(LearnAttemptRepository.class);
        var quiz = new LearnQuiz(); quiz.setTitle("TOEIC");
        var a = new LearnAttempt(); a.setQuiz(quiz); a.setDisplayName("alice"); a.setScore(8); a.setTotalQuestions(10);
        var b = new LearnAttempt(); b.setQuiz(quiz); b.setDisplayName("bob"); b.setScore(6); b.setTotalQuestions(10);
        when(repo.findTop20ByStatusOrderBySubmittedAtDesc(LearnAttempt.Status.SUBMITTED)).thenReturn(List.of(a, b));
        var service = new LearnService(mock(LearnQuizRepository.class), mock(LearnQuestionRepository.class),
            mock(LearnOptionRepository.class), repo, mock(LearnAnswerRepository.class), List.of());
        var result = service.recentAttempts();
        assertThat(result).extracting("displayName").containsExactly("alice", "bob");
        assertThat(result).extracting("score").containsExactly(8, 6);
        verify(repo).findTop20ByStatusOrderBySubmittedAtDesc(LearnAttempt.Status.SUBMITTED);
    }
}
