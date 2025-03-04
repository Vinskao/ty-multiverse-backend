package tw.com.tymbackend.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis-test")
public class RedisTestController {
    private final RedisTemplate<String, String> redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(RedisTestController.class);

    public RedisTestController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> pingRedis() {
        try {
            String result = redisTemplate.execute((RedisCallback<String>) connection -> 
                new String(connection.ping()));
            return ResponseEntity.ok("Redis responded: " + result);
        } catch (Exception e) {
            logger.error("Redis connection failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Redis connection failed: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testRedis() {
        try {
            // 測試寫入
            redisTemplate.opsForValue().set("test-key", "test-value");
            // 測試讀取
            String value = redisTemplate.opsForValue().get("test-key");
            return ResponseEntity.ok("Redis test successful. Retrieved value: " + value);
        } catch (Exception e) {
            logger.error("Redis operation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Redis operation failed: " + e.getMessage());
        }
    }
} 