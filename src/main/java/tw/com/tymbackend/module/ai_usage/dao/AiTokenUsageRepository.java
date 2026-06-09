package tw.com.tymbackend.module.ai_usage.dao;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageSummaryDTO;
import tw.com.tymbackend.module.ai_usage.domain.vo.AiTokenUsage;

@Repository
public interface AiTokenUsageRepository extends JpaRepository<AiTokenUsage, Long> {

    @Query("""
        SELECT new tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageSummaryDTO(
            CAST(FUNCTION('DATE', u.calledAt) AS string),
            u.aiProvider,
            u.modelName,
            SUM(CAST(u.inputTokens AS long)),
            SUM(CAST(u.outputTokens AS long)),
            SUM(CAST(u.inputTokens AS long) + CAST(u.outputTokens AS long)
                + CAST(COALESCE(u.cacheCreationInputTokens, 0) AS long)
                + CAST(COALESCE(u.cacheReadInputTokens, 0) AS long)),
            SUM(u.estimatedCostUsd),
            COUNT(u)
        )
        FROM AiTokenUsage u
        WHERE u.calledAt >= :from AND u.calledAt < :to
        GROUP BY FUNCTION('DATE', u.calledAt), u.aiProvider, u.modelName
        ORDER BY FUNCTION('DATE', u.calledAt) DESC
        """)
    List<AiTokenUsageSummaryDTO> findDailySummary(
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to
    );

    @Query("""
        SELECT new tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageSummaryDTO(
            FUNCTION('TO_CHAR', u.calledAt, 'YYYY-MM'),
            u.aiProvider,
            u.modelName,
            SUM(CAST(u.inputTokens AS long)),
            SUM(CAST(u.outputTokens AS long)),
            SUM(CAST(u.inputTokens AS long) + CAST(u.outputTokens AS long)
                + CAST(COALESCE(u.cacheCreationInputTokens, 0) AS long)
                + CAST(COALESCE(u.cacheReadInputTokens, 0) AS long)),
            SUM(u.estimatedCostUsd),
            COUNT(u)
        )
        FROM AiTokenUsage u
        WHERE u.calledAt >= :from AND u.calledAt < :to
        GROUP BY FUNCTION('TO_CHAR', u.calledAt, 'YYYY-MM'), u.aiProvider, u.modelName
        ORDER BY FUNCTION('TO_CHAR', u.calledAt, 'YYYY-MM') DESC
        """)
    List<AiTokenUsageSummaryDTO> findMonthlySummary(
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to
    );
}
