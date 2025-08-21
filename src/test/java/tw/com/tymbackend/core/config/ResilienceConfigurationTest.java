package tw.com.tymbackend.core.config;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rate Limiter 配置測試
 */
class ResilienceConfigurationTest {

    @Test
    void testApiRateLimiter() {
        ResilienceConfiguration config = new ResilienceConfiguration();
        
        Bucket apiRateLimiter = config.apiRateLimiter();
        assertNotNull(apiRateLimiter);
        
        // 測試前100個請求應該通過（放寬限制後）
        for (int i = 0; i < 100; i++) {
            assertTrue(apiRateLimiter.tryConsume(1), "第 " + (i + 1) + " 個請求應該通過");
        }
        
        // 第101個請求應該被限制
        assertFalse(apiRateLimiter.tryConsume(1), "第101個請求應該被限制");
    }

    @Test
    void testBatchApiRateLimiter() {
        ResilienceConfiguration config = new ResilienceConfiguration();
        
        Bucket batchApiRateLimiter = config.batchApiRateLimiter();
        assertNotNull(batchApiRateLimiter);
        
        // 測試前50個請求應該通過（放寬限制後）
        for (int i = 0; i < 50; i++) {
            assertTrue(batchApiRateLimiter.tryConsume(1), "第 " + (i + 1) + " 個批量請求應該通過");
        }
        
        // 第51個請求應該被限制
        assertFalse(batchApiRateLimiter.tryConsume(1), "第51個批量請求應該被限制");
    }
}
