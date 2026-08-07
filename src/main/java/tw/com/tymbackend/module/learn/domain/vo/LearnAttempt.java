package tw.com.tymbackend.module.learn.domain.vo;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    /** Token subject. Stable across username changes, so all progress keys off it. */
    @Column(name = "user_id", nullable = false, length = 160)
    private String userId;

    /** Human-readable name for the leaderboard and mentor mode; the subject alone is a UUID. */
    @Column(name = "display_name", length = 160)
    private String displayName;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /** IN_PROGRESS until every question is answered and the attempt is submitted. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.IN_PROGRESS;

    /** Comma-separated question ids, the shuffled order this attempt is being taken in. */
    @Column(name = "question_order", columnDefinition = "text")
    private String questionOrder;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    public enum Status { IN_PROGRESS, SUBMITTED }

    @PrePersist
    void prePersist() {
        if (startedAt == null) startedAt = OffsetDateTime.now();
    }
}
