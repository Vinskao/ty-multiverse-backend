package tw.com.tymbackend.module.ai_usage.domain.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "ai_token_usage")
public class AiTokenUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "user_id", length = 128)
    private String userId;

    @Column(name = "source_device", length = 128)
    private String sourceDevice;

    @Column(name = "ai_provider", nullable = false, length = 32)
    private String aiProvider;

    @Column(name = "model_name", nullable = false, length = 128)
    private String modelName;

    @Column(name = "input_tokens", nullable = false)
    private Integer inputTokens = 0;

    @Column(name = "output_tokens", nullable = false)
    private Integer outputTokens = 0;

    @Column(name = "cache_creation_input_tokens")
    private Integer cacheCreationInputTokens = 0;

    @Column(name = "cache_read_input_tokens")
    private Integer cacheReadInputTokens = 0;

    @Column(name = "total_tokens", insertable = false, updatable = false)
    private Integer totalTokens;

    @Column(name = "estimated_cost_usd", precision = 12, scale = 8)
    private BigDecimal estimatedCostUsd;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "called_at", nullable = false)
    private OffsetDateTime calledAt;

    @Column(name = "endpoint", length = 256)
    private String endpoint;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "success";

    @Column(name = "granularity", nullable = false, length = 16)
    private String granularity = "per_call";

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    public void prePersist() {
        if (calledAt == null) {
            calledAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
}
