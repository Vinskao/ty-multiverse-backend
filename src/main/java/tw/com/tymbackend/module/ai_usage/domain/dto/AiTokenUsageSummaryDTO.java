package tw.com.tymbackend.module.ai_usage.domain.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTokenUsageSummaryDTO {

    private String period;
    private String aiProvider;
    private String modelName;
    private Long totalInputTokens;
    private Long totalOutputTokens;
    private Long totalTokens;
    private BigDecimal totalEstimatedCostUsd;
    private Long callCount;
}
