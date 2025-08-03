package tw.com.tymbackend.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 分布式鎖工具類
 * 
 * 提供分布式環境下的鎖機制，防止多個實例同時執行相同操作。
 * 使用 Redis 實現，支持自動釋放和手動釋放。
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>使用分布式鎖執行操作</li>
 *   <li>嘗試獲取分布式鎖</li>
 *   <li>釋放分布式鎖</li>
 *   <li>檢查鎖狀態</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * distributedLockUtil.executeWithLock("lock:key", Duration.ofSeconds(30), () -> {
 *     // 需要保護的操作
 *     return result;
 * });
 * </pre>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
public class DistributedLockUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedLockUtil.class);
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 使用分布式鎖執行操作
     * 
     * 此方法會嘗試獲取指定的分布式鎖，如果成功獲取則執行操作，
     * 操作完成後自動釋放鎖。如果無法獲取鎖，則拋出異常。
     * 
     * @param lockKey 鎖的鍵名，用於標識不同的鎖
     * @param lockTimeout 鎖的超時時間，防止死鎖
     * @param operation 要執行的操作，返回操作結果
     * @param <T> 返回值類型
     * @return 操作的結果
     * @throws RuntimeException 當無法獲取鎖或操作執行失敗時拋出
     * @see #tryAcquireLock(String, Duration)
     * @see #releaseLock(String, String)
     */
    public <T> T executeWithLock(String lockKey, Duration lockTimeout, Supplier<T> operation) {
        String lockValue = UUID.randomUUID().toString();
        
        try {
            Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, lockTimeout);
            
            if (Boolean.TRUE.equals(acquired)) {
                try {
                    logger.debug("成功獲取分布式鎖: {}", lockKey);
                    return operation.get();
                } finally {
                    releaseLock(lockKey, lockValue);
                }
            } else {
                logger.debug("無法獲取分布式鎖: {}，操作被跳過", lockKey);
                throw new RuntimeException("操作正在被其他實例執行，請稍後再試");
            }
        } catch (Exception e) {
            logger.error("分布式鎖操作失敗: {}", lockKey, e);
            throw new RuntimeException("分布式鎖操作失敗", e);
        }
    }
    
    /**
     * 使用分布式鎖執行無返回值的操作
     * 
     * 此方法是 {@link #executeWithLock(String, Duration, Supplier)} 的簡化版本，
     * 用於執行不需要返回值的操作。
     * 
     * @param lockKey 鎖的鍵名，用於標識不同的鎖
     * @param lockTimeout 鎖的超時時間，防止死鎖
     * @param operation 要執行的操作
     * @throws RuntimeException 當無法獲取鎖或操作執行失敗時拋出
     * @see #executeWithLock(String, Duration, Supplier)
     */
    public void executeWithLock(String lockKey, Duration lockTimeout, Runnable operation) {
        executeWithLock(lockKey, lockTimeout, () -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * 嘗試獲取分布式鎖
     * 
     * 此方法會嘗試獲取指定的分布式鎖，但不執行任何操作。
     * 獲取成功返回鎖值，失敗返回 null。
     * 
     * @param lockKey 鎖的鍵名，用於標識不同的鎖
     * @param lockTimeout 鎖的超時時間，防止死鎖
     * @return 鎖的值，如果獲取失敗返回 null
     * @see #releaseLock(String, String)
     */
    public String tryAcquireLock(String lockKey, Duration lockTimeout) {
        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, lockTimeout);
        
        if (Boolean.TRUE.equals(acquired)) {
            logger.debug("成功獲取分布式鎖: {}", lockKey);
            return lockValue;
        } else {
            logger.debug("無法獲取分布式鎖: {}", lockKey);
            return null;
        }
    }
    
    /**
     * 釋放分布式鎖
     * 
     * 使用 Lua 腳本確保釋放鎖的原子性，只有持有正確鎖值的實例才能釋放鎖。
     * 
     * @param lockKey 鎖的鍵名
     * @param lockValue 鎖的值，必須與獲取鎖時的值一致
     * @see #tryAcquireLock(String, Duration)
     */
    public void releaseLock(String lockKey, String lockValue) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                           "return redis.call('del', KEYS[1]) " +
                           "else return 0 end";
            Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class), 
                Arrays.asList(lockKey), 
                lockValue
            );
            
            if (result != null && result > 0) {
                logger.debug("成功釋放分布式鎖: {}", lockKey);
            } else {
                logger.warn("釋放分布式鎖失敗或鎖已過期: {}", lockKey);
            }
        } catch (Exception e) {
            logger.error("釋放分布式鎖時發生錯誤: {}", lockKey, e);
        }
    }
    
    /**
     * 檢查鎖是否存在
     * 
     * 檢查指定的鎖是否仍然存在於 Redis 中。
     * 
     * @param lockKey 鎖的鍵名
     * @return 如果鎖存在返回 true，否則返回 false
     */
    public boolean isLocked(String lockKey) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        } catch (Exception e) {
            logger.error("檢查鎖狀態時發生錯誤: {}", lockKey, e);
            return false;
        }
    }
} 