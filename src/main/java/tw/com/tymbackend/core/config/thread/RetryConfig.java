package tw.com.tymbackend.core.config.thread;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * 重試機制配置類
 * 
 * 啟用 Spring Retry 功能，用於處理樂觀鎖定失敗等場景。
 * 提供自動重試機制，提高系統的並發處理能力。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableRetry
public class RetryConfig {
    // Spring Retry 將自動配置重試機制
    // 可以在方法上使用 @Retryable 註解來啟用重試
    // 使用 @Recover 註解來處理重試失敗的情況
} 