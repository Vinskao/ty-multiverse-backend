package tw.com.tymbackend.module.ai_usage.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTokenUsageCreateDTO {

    private String sessionId;
    private String userId;
    private String sourceDevice;

    @NotBlank
    private String aiProvider;

    @NotBlank
    private String modelName;

    @NotNull
    private Integer inputTokens;

    @NotNull
    private Integer outputTokens;

    private Integer cacheCreationInputTokens;
    private Integer cacheReadInputTokens;
    private BigDecimal estimatedCostUsd;
    private String requestId;
    private String endpoint;
    private String status;
    private String granularity;
    private String calledAt;
}
