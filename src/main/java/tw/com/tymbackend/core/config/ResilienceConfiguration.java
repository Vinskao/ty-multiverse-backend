package tw.com.tymbackend.core.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate Limiter 配置類別
 * 
 * 提供輕量級的 Rate Limiter 保護，防止 DDOS 攻擊
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Configuration
public class ResilienceConfiguration {

    // ==================== Rate Limiter 配置 ====================
    
    /**
     * 創建通用API的Rate Limiter
     * 
     * 配置（放寬限制，只防止 DDOS）：
     * - 每秒100個請求
     * - 突發200個請求
     * 
     * @return Bucket 實例
     */
    @Bean("apiRateLimiter")
    public Bucket apiRateLimiter() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofSeconds(1)));
        Bandwidth burstLimit = Bandwidth.classic(200, Refill.intervally(200, Duration.ofSeconds(1)));
        return Bucket.builder()
                .addLimit(limit)
                .addLimit(burstLimit)
                .build();
    }

    /**
     * 創建批量API的Rate Limiter
     * 
     * 配置（放寬限制，只防止 DDOS）：
     * - 每秒50個請求
     * - 突發100個請求
     * 
     * @return Bucket 實例
     */
    @Bean("batchApiRateLimiter")
    public Bucket batchApiRateLimiter() {
        Bandwidth limit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofSeconds(1)));
        Bandwidth burstLimit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofSeconds(1)));
        return Bucket.builder()
                .addLimit(limit)
                .addLimit(burstLimit)
                .build();
    }
}
