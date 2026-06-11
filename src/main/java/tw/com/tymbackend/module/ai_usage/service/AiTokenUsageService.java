package tw.com.tymbackend.module.ai_usage.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.module.ai_usage.dao.AiTokenUsageRepository;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageCreateDTO;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageOverviewDTO;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageSummaryDTO;
import tw.com.tymbackend.module.ai_usage.domain.vo.AiTokenUsage;

@Service
public class AiTokenUsageService {

    private final AiTokenUsageRepository repository;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Taipei");

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
        String granularity = dto.getGranularity() != null ? dto.getGranularity() : "per_call";
        OffsetDateTime calledAt = dto.getCalledAt() != null
            ? OffsetDateTime.parse(dto.getCalledAt())
            : null;
        AiTokenUsage entity = findExistingDailyAggregate(dto, granularity, calledAt);
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
        entity.setGranularity(granularity);
        if (calledAt != null) {
            entity.setCalledAt(calledAt);
        }
        return repository.save(entity);
    }

    private AiTokenUsage findExistingDailyAggregate(
            AiTokenUsageCreateDTO dto,
            String granularity,
            OffsetDateTime calledAt) {
        if (!"daily_aggregate".equals(granularity) || calledAt == null) {
            return new AiTokenUsage();
        }

        OffsetDateTime dayStart = calledAt.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
        return repository
            .findFirstByAiProviderAndModelNameAndGranularityAndCalledAtGreaterThanEqualAndCalledAtLessThan(
                dto.getAiProvider(),
                dto.getModelName(),
                granularity,
                dayStart,
                dayStart.plusDays(1)
            )
            .orElseGet(AiTokenUsage::new);
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

    @Transactional(readOnly = true)
    public AiTokenUsageOverviewDTO getOverview(String timezone) {
        ZoneId zone = timezone == null || timezone.isBlank() ? DEFAULT_ZONE : ZoneId.of(timezone);
        LocalDate today = LocalDate.now(zone);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate thisMonthStart = today.withDayOfMonth(1);
        LocalDate lastMonthStart = thisMonthStart.minusMonths(1);
        LocalDate thisYearStart = today.withDayOfYear(1);
        LocalDate lastYearStart = thisYearStart.minusYears(1);
        LocalDate lastYearMatchedEnd = lastYearStart.plusDays(
            ChronoUnit.DAYS.between(thisYearStart, tomorrow)
        );

        long thisMonth = sumTokens(thisMonthStart, tomorrow, zone);
        long lastMonth = sumTokens(lastMonthStart, thisMonthStart, zone);
        long thisYear = sumTokens(thisYearStart, tomorrow, zone);
        long previousMonthMatched = sumTokens(
            lastMonthStart,
            lastMonthStart.plusDays(Math.min(today.getDayOfMonth(), lastMonthStart.lengthOfMonth())),
            zone
        );
        long previousYearMatched = sumTokens(lastYearStart, lastYearMatchedEnd, zone);

        OffsetDateTime firstCalledAt = repository.findFirstCalledAt();
        if (firstCalledAt == null) {
            return new AiTokenUsageOverviewDTO(
                0L, 0L, 0L, 0L, 0L, 0L, 0L, null, null, null, zone.getId()
            );
        }

        LocalDate dataSince = firstCalledAt.atZoneSameInstant(zone).toLocalDate();
        long observableTotal = sumTokens(dataSince, tomorrow, zone);
        long observedDays = Math.max(1, ChronoUnit.DAYS.between(dataSince, tomorrow));
        long observedMonths = Math.max(
            1,
            ChronoUnit.MONTHS.between(YearMonth.from(dataSince), YearMonth.from(today)) + 1
        );
        long observedYears = Math.max(1, ChronoUnit.YEARS.between(dataSince, today) + 1);

        return new AiTokenUsageOverviewDTO(
            thisMonth,
            lastMonth,
            thisYear,
            observableTotal,
            Math.round((double) observableTotal / observedDays),
            Math.round((double) observableTotal / observedMonths),
            Math.round((double) observableTotal / observedYears),
            growthPercent(thisMonth, previousMonthMatched),
            growthPercent(thisYear, previousYearMatched),
            dataSince,
            zone.getId()
        );
    }

    private long sumTokens(LocalDate from, LocalDate toExclusive, ZoneId zone) {
        OffsetDateTime fromDt = from.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime toDt = toExclusive.atStartOfDay(zone).toOffsetDateTime();
        Long total = repository.sumTotalTokens(fromDt, toDt);
        return total != null ? total : 0L;
    }

    private Double growthPercent(long current, long previous) {
        if (previous == 0) {
            return null;
        }
        return BigDecimal.valueOf(current - previous)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(previous), 2, RoundingMode.HALF_UP)
            .doubleValue();
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
