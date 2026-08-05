package tw.com.tymbackend.module.learn.domain.vo;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "learn_attempt")
public class LearnAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private LearnQuiz quiz;

    @Column(name = "user_id", nullable = false, length = 160)
    private String userId;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @PrePersist
    void prePersist() {
        if (submittedAt == null) submittedAt = OffsetDateTime.now();
    }
}
