package tw.com.tymbackend.module.learn.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "learn_question", uniqueConstraints = @UniqueConstraint(columnNames = {"quiz_id", "position"}))
public class LearnQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private LearnQuiz quiz;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false, length = 40)
    private String section;

    @Column(name = "passage_key", length = 80)
    private String passageKey;

    @Column(name = "passage_text", columnDefinition = "text")
    private String passageText;

    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    @Column(nullable = false, length = 1)
    private String correctOption;

    @Column(nullable = false, columnDefinition = "text")
    private String explanation;
}
