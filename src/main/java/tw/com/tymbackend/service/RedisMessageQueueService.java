package tw.com.tymbackend.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RedisMessageQueueService {
    private static final Logger logger = LoggerFactory.getLogger(RedisMessageQueueService.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic metricsTopic;

    public RedisMessageQueueService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.metricsTopic = new ChannelTopic("metrics-channel");
    }

    public void publishMetrics(String message) {
        try {
            redisTemplate.convertAndSend(metricsTopic.getTopic(), message);
            logger.debug("Published message to Redis: {}", message);
        } catch (Exception e) {
            logger.error("Error publishing message to Redis", e);
        }
    }
} 