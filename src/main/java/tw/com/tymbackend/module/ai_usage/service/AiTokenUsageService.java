package tw.com.tymbackend.module.ai_usage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.module.ai_usage.dao.AiTokenUsageRepository;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageCreateDTO;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageSummaryDTO;
import tw.com.tymbackend.module.ai_usage.domain.vo.AiTokenUsage;

@Service
public class AiTokenUsageService {

    private final AiTokenUsageRepository repository;

    // USD per 1K tokens: [inputRate, outputRate]
    private static final Map<String, BigDecimal[]> COST_TABLE = Map.of(
        "gpt-4o-mini",       new BigDecimal[]{ new BigDecimal("0.00015"), new BigDecimal("0.00060") },
        "gpt-4o",            new BigDecimal[]{ new BigDecimal("0.0025"),  new BigDecimal("0.01000") },
        "gpt-4.1-nano",      new BigDecimal[]{ new BigDecimal("0.00010"), new BigDecimal("0.00040") },
        "claude-sonnet-4-6", new BigDecimal[]{ new BigDecimal("0.003"),   new BigDecimal("0.015")   },
        "claude-opus-4-8",   new BigDecimal[]{ new BigDecimal("0.015"),   new BigDecimal("0.075")   },
        "claude-haiku-4-5",  new BigDecimal[]{ new BigDecimal("0.00080"), new BigDecimal("0.00400") }
    );

    public AiTokenUsageService(AiTokenUsageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AiTokenUsage createRecord(AiTokenUsageCreateDTO dto) {
        AiTokenUsage entity = new AiTokenUsage();
        entity.setSessionId(dto.getSessionId());
        entity.setUserId(dto.getUserId());
        entity.setAiProvider(dto.getAiProvider());
        entity.setModelName(dto.getModelName());
        entity.setInputTokens(dto.getInputTokens() != null ? dto.getInputTokens() : 0);
        entity.setOutputTokens(dto.getOutputTokens() != null ? dto.getOutputTokens() : 0);
        entity.setCacheCreationInputTokens(dto.getCacheCreationInputTokens() != null ? dto.getCacheCreationInputTokens() : 0);
        entity.setCacheReadInputTokens(dto.getCacheReadInputTokens() != null ? dto.getCacheReadInputTokens() : 0);
        entity.setEstimatedCostUsd(dto.getEstimatedCostUsd() != null ? dto.getEstimatedCostUsd() : calculateCost(dto));
        entity.setRequestId(dto.getRequestId());
        entity.setEndpoint(dto.getEndpoint());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "success");
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<AiTokenUsageSummaryDTO> getDailySummary(LocalDate from, LocalDate to) {
        OffsetDateTime fromDt = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return repository.findDailySummary(fromDt, toDt);
    }

    @Transactional(readOnly = true)
    public List<AiTokenUsageSummaryDTO> getMonthlySummary(YearMonth from, YearMonth to) {
        OffsetDateTime fromDt = from.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDt = to.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return repository.findMonthlySummary(fromDt, toDt);
    }

    private BigDecimal calculateCost(AiTokenUsageCreateDTO dto) {
        BigDecimal[] rates = COST_TABLE.get(dto.getModelName());
        if (rates == null) {
            return BigDecimal.ZERO;
        }
        int inputTokens = dto.getInputTokens() != null ? dto.getInputTokens() : 0;
        int outputTokens = dto.getOutputTokens() != null ? dto.getOutputTokens() : 0;
        BigDecimal inputCost = rates[0].multiply(BigDecimal.valueOf(inputTokens))
            .divide(BigDecimal.valueOf(1000), 8, RoundingMode.HALF_UP);
        BigDecimal outputCost = rates[1].multiply(BigDecimal.valueOf(outputTokens))
            .divide(BigDecimal.valueOf(1000), 8, RoundingMode.HALF_UP);
        return inputCost.add(outputCost);
    }
}
