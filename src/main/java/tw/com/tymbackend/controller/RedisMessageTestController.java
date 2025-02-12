package tw.com.tymbackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.service.RedisMessageQueueService;

@RestController
@RequestMapping("/redis-message")
public class RedisMessageTestController {
    private final RedisMessageQueueService messageQueueService;
    private final Logger logger = LoggerFactory.getLogger(RedisMessageTestController.class);

    public RedisMessageTestController(RedisMessageQueueService messageQueueService) {
        this.messageQueueService = messageQueueService;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publishMessage(@RequestBody String message) {
        try {
            messageQueueService.publishMetrics(message);
            return ResponseEntity.ok("Message published successfully");
        } catch (Exception e) {
            logger.error("Failed to publish message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Failed to publish message: " + e.getMessage());
        }
    }
}
