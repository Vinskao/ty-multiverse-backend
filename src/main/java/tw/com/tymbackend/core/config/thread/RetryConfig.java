package tw.com.tymbackend.core.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 重試機制配置類 - 支援資料庫和 MQ 連接重試
 *
 * 啟用 Spring Retry 功能，用於處理連接失敗等場景。
 * 提供多層次的自動重試機制，提高系統的穩定性。
 *
 * @author TY Backend Team
 * @version 2.0
 * @since 2024
 */
@Configuration
@EnableRetry
public class RetryConfig {

    /**
     * 資料庫連接重試模板
     * - 適用於資料庫連接失敗時的重試
     * - 較長的重試間隔，適合網路問題
     */
    @Bean(name = "databaseRetryTemplate")
    public RetryTemplate databaseRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 重試策略 - 最大重試次數
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(10);  // 資料庫連接重試更多次

        // 退避策略 - 較慢的指數退避
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000);  // 初始間隔 2 秒
        backOffPolicy.setMaxInterval(60000);     // 最大間隔 1 分鐘
        backOffPolicy.setMultiplier(1.5);        // 較慢的倍數

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    /**
     * MQ 連接重試模板
     * - 適用於 RabbitMQ 連接失敗時的重試
     * - 較快重試間隔，適合消息處理
     */
    @Bean(name = "rabbitRetryTemplate")
    public RetryTemplate rabbitRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 重試策略 - 最大重試次數
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);   // MQ 重試次數適中

        // 退避策略 - 較快的指數退避
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);  // 初始間隔 1 秒
        backOffPolicy.setMaxInterval(30000);     // 最大間隔 30 秒
        backOffPolicy.setMultiplier(2.0);        // 標準倍數

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    /**
     * 通用重試模板 - 適用於一般業務邏輯重試
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 重試策略 - 最大重試次數
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);   // 一般業務重試 3 次

        // 退避策略 - 標準指數退避
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);   // 初始間隔 0.5 秒
        backOffPolicy.setMaxInterval(5000);      // 最大間隔 5 秒
        backOffPolicy.setMultiplier(1.5);        // 適中的倍數

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    // Spring Retry 將自動配置重試機制
    // 可以在方法上使用 @Retryable 註解來啟用重試
    // 使用 @Recover 註解來處理重試失敗的情況
} 