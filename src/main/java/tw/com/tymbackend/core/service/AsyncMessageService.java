package tw.com.tymbackend.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.core.config.RabbitMQConfig;
import tw.com.tymbackend.core.message.AsyncMessageDTO;

import java.util.UUID;

/**
 * 異步消息服務
 * 
 * 負責發送消息到 RabbitMQ 進行異步處理
 * 只在生產環境啟用 (spring.rabbitmq.enabled=true)
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Service
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class AsyncMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncMessageService.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 發送傷害計算請求到 RabbitMQ
     * 
     * @param characterName 角色名稱
     * @return 請求ID
     */
    public String sendDamageCalculationRequest(String characterName) {
        String requestId = UUID.randomUUID().toString();
        
        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/people/damageWithWeapon",
            "GET",
            characterName
        );
        
        sendMessage(RabbitMQConfig.DAMAGE_CALCULATION_QUEUE, message);
        
        logger.info("發送傷害計算請求到 RabbitMQ: characterName={}, requestId={}", characterName, requestId);
        
        return requestId;
    }
    
    /**
     * 發送角色列表獲取請求到 RabbitMQ
     * 
     * @return 請求ID
     */
    public String sendPeopleGetAllRequest() {
        String requestId = UUID.randomUUID().toString();
        
        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/people/get-all",
            "POST",
            null
        );
        
        sendMessage(RabbitMQConfig.PEOPLE_GET_ALL_QUEUE, message);
        
        logger.info("發送角色列表獲取請求到 RabbitMQ: requestId={}", requestId);
        
        return requestId;
    }
    
    /**
     * 發送消息到指定隊列
     * 
     * @param queueName 隊列名稱
     * @param message 消息內容
     */
    private void sendMessage(String queueName, AsyncMessageDTO message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(RabbitMQConfig.TYMB_EXCHANGE, getRoutingKey(queueName), messageJson);
            
            logger.debug("消息已發送到隊列 {}: {}", queueName, messageJson);
            
        } catch (JsonProcessingException e) {
            logger.error("序列化消息失敗: {}", e.getMessage(), e);
            throw new RuntimeException("消息序列化失敗", e);
        } catch (Exception e) {
            logger.error("發送消息到 RabbitMQ 失敗: {}", e.getMessage(), e);
            throw new RuntimeException("消息發送失敗", e);
        }
    }
    
    /**
     * 根據隊列名稱獲取路由鍵
     * 
     * @param queueName 隊列名稱
     * @return 路由鍵
     */
    private String getRoutingKey(String queueName) {
        switch (queueName) {
            case RabbitMQConfig.DAMAGE_CALCULATION_QUEUE:
                return "damage.calculation";
            case RabbitMQConfig.PEOPLE_GET_ALL_QUEUE:
                return "people.get.all";
            default:
                throw new IllegalArgumentException("未知的隊列名稱: " + queueName);
        }
    }
}
