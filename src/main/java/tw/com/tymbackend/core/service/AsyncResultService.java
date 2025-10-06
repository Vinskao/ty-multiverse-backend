package tw.com.tymbackend.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.core.message.AsyncResultDTO;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 異步結果服務
 * 
 * 負責存儲和查詢異步處理的結果，使用 Redis 作為存儲介質。
 * 提供結果的存儲、查詢和存在性檢查功能。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Service
public class AsyncResultService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncResultService.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis key 前綴
    private static final String RESULT_KEY_PREFIX = "async:result:";
    
    // 結果過期時間（30分鐘）
    private static final Duration RESULT_EXPIRATION = Duration.ofMinutes(30);
    
    /**
     * 存儲異步處理結果
     * 
     * @param requestId 請求ID
     * @param result 處理結果
     */
    public void storeResult(String requestId, AsyncResultDTO result) {
        try {
            String key = RESULT_KEY_PREFIX + requestId;
            redisTemplate.opsForValue().set(key, result, RESULT_EXPIRATION);
            logger.info("已存儲異步處理結果: requestId={}, status={}", requestId, result.getStatus());
        } catch (Exception e) {
            logger.error("存儲異步處理結果失敗: requestId={}", requestId, e);
            throw new RuntimeException("存儲異步處理結果失敗", e);
        }
    }
    
    /**
     * 存儲處理中的狀態
     * 
     * @param requestId 請求ID
     */
    public void storeProcessingStatus(String requestId) {
        AsyncResultDTO processingResult = new AsyncResultDTO(
            requestId, 
            null, 
            "processing"
        );
        storeResult(requestId, processingResult);
    }
    
    /**
     * 存儲完成狀態和結果
     * 
     * @param requestId 請求ID
     * @param data 處理結果數據
     */
    public void storeCompletedResult(String requestId, Object data) {
        AsyncResultDTO completedResult = new AsyncResultDTO(
            requestId, 
            data, 
            "completed"
        );
        storeResult(requestId, completedResult);
    }
    
    /**
     * 存儲失敗狀態和錯誤訊息
     * 
     * @param requestId 請求ID
     * @param error 錯誤訊息
     */
    public void storeFailedResult(String requestId, String error) {
        AsyncResultDTO failedResult = new AsyncResultDTO(
            requestId, 
            null, 
            "failed", 
            error
        );
        storeResult(requestId, failedResult);
    }
    
    /**
     * 查詢異步處理結果
     * 
     * @param requestId 請求ID
     * @return 處理結果，如果不存在返回 null
     */
    public AsyncResultDTO getResult(String requestId) {
        try {
            String key = RESULT_KEY_PREFIX + requestId;
            Object result = redisTemplate.opsForValue().get(key);
            
            if (result instanceof AsyncResultDTO) {
                logger.debug("查詢到異步處理結果: requestId={}, status={}", 
                    requestId, ((AsyncResultDTO) result).getStatus());
                return (AsyncResultDTO) result;
            } else {
                logger.debug("未找到異步處理結果: requestId={}", requestId);
                return null;
            }
        } catch (Exception e) {
            logger.error("查詢異步處理結果失敗: requestId={}", requestId, e);
            throw new RuntimeException("查詢異步處理結果失敗", e);
        }
    }
    
    /**
     * 檢查結果是否存在
     * 
     * @param requestId 請求ID
     * @return 如果結果存在返回 true，否則返回 false
     */
    public boolean resultExists(String requestId) {
        try {
            String key = RESULT_KEY_PREFIX + requestId;
            Boolean exists = redisTemplate.hasKey(key);
            logger.debug("檢查異步處理結果存在性: requestId={}, exists={}", requestId, exists);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            logger.error("檢查異步處理結果存在性失敗: requestId={}", requestId, e);
            return false;
        }
    }
    
    /**
     * 刪除異步處理結果
     * 
     * @param requestId 請求ID
     */
    public void deleteResult(String requestId) {
        try {
            String key = RESULT_KEY_PREFIX + requestId;
            Boolean deleted = redisTemplate.delete(key);
            logger.info("刪除異步處理結果: requestId={}, deleted={}", requestId, deleted);
        } catch (Exception e) {
            logger.error("刪除異步處理結果失敗: requestId={}", requestId, e);
        }
    }
    
    /**
     * 延長結果的過期時間
     * 
     * @param requestId 請求ID
     * @param duration 延長的時間
     */
    public void extendExpiration(String requestId, Duration duration) {
        try {
            String key = RESULT_KEY_PREFIX + requestId;
            Boolean extended = redisTemplate.expire(key, duration);
            logger.debug("延長異步處理結果過期時間: requestId={}, extended={}", requestId, extended);
        } catch (Exception e) {
            logger.error("延長異步處理結果過期時間失敗: requestId={}", requestId, e);
        }
    }
}
