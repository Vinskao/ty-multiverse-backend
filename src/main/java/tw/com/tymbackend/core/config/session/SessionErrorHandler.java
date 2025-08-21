package tw.com.tymbackend.core.config.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Session 錯誤處理配置
 * 
 * 處理 Session 相關的錯誤，特別是 Session 失效問題
 */
@Configuration
@ConditionalOnProperty(name = "app.session.enabled", havingValue = "true")
public class SessionErrorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionErrorHandler.class);
    
    /**
     * 配置 Session Repository 錯誤處理
     * 
     * @param redisTemplate Redis 模板
     * @return 配置好的 SessionRepository
     */
    @Bean
    public SessionRepository<?> sessionRepository(RedisTemplate<String, Object> redisTemplate) {
        return new RedisSessionRepository(redisTemplate);
    }
    
    /**
     * Session 錯誤處理器
     * 
     * 當 Session 操作失敗時，記錄錯誤並嘗試恢復
     */
    public static class SessionExceptionHandler {
        
        public static void handleSessionError(Exception e, String operation) {
            logger.warn("Session 操作失敗 - 操作: {}, 錯誤: {}", operation, e.getMessage());
            
            // 如果是 Session 失效錯誤，記錄詳細信息
            if (e instanceof IllegalStateException && 
                e.getMessage() != null && 
                e.getMessage().contains("Session was invalidated")) {
                logger.error("Session 已失效，可能需要重新建立 Session");
            }
        }
    }
}
