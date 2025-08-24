package tw.com.tymbackend.core.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.core.config.RabbitMQConfig;
import tw.com.tymbackend.core.message.ProducerResponseDTO;


/**
 * Consumer 回應發送服務
 * 
 * 負責將 Consumer 的處理結果發送回 Producer
 * 只在 RabbitMQ 啟用時生效
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Service
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class ConsumerResponseService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsumerResponseService.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 發送 People 處理成功回應
     */
    public void sendPeopleSuccessResponse(String requestId, String message, Object data) {
        ProducerResponseDTO response = ProducerResponseDTO.success(requestId, message, data);
        sendPeopleResponse(response);
    }
    
    /**
     * 發送 People 獲取所有成功回應
     */
    public void sendPeopleGetAllSuccessResponse(String requestId, String message, Object data) {
        ProducerResponseDTO response = ProducerResponseDTO.success(requestId, message, data);
        sendPeopleResponse(response);
    }
    
    /**
     * 發送 People 處理錯誤回應
     */
    public void sendPeopleErrorResponse(String requestId, String message, String errorCode, String errorDetails) {
        ProducerResponseDTO response = ProducerResponseDTO.error(requestId, message, errorCode, errorDetails);
        sendPeopleResponse(response);
    }
    
    /**
     * 發送 Weapon 處理成功回應
     */
    public void sendWeaponSuccessResponse(String requestId, String message, Object data) {
        ProducerResponseDTO response = ProducerResponseDTO.success(requestId, message, data);
        sendWeaponResponse(response);
    }
    
    /**
     * 發送 Weapon 處理錯誤回應
     */
    public void sendWeaponErrorResponse(String requestId, String message, String errorCode, String errorDetails) {
        ProducerResponseDTO response = ProducerResponseDTO.error(requestId, message, errorCode, errorDetails);
        sendWeaponResponse(response);
    }
    
    /**
     * 發送 People 回應消息
     */
    private void sendPeopleResponse(ProducerResponseDTO response) {
        try {
            String messageJson = objectMapper.writeValueAsString(response);
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.PEOPLE_RESPONSE_EXCHANGE,
                RabbitMQConfig.PEOPLE_RESPONSE_ROUTING_KEY,
                messageJson
            );
            
            logger.info("發送 People 回應消息: requestId={}, status={}, message={}", 
                       response.getRequestId(), response.getStatus(), response.getMessage());
            
        } catch (JsonProcessingException e) {
            logger.error("序列化 People 回應消息失敗: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("發送 People 回應消息失敗: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 發送 Weapon 回應消息
     */
    private void sendWeaponResponse(ProducerResponseDTO response) {
        try {
            String messageJson = objectMapper.writeValueAsString(response);
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.WEAPON_RESPONSE_EXCHANGE,
                RabbitMQConfig.WEAPON_RESPONSE_ROUTING_KEY,
                messageJson
            );
            
            logger.info("發送 Weapon 回應消息: requestId={}, status={}, message={}", 
                       response.getRequestId(), response.getStatus(), response.getMessage());
            
        } catch (JsonProcessingException e) {
            logger.error("序列化 Weapon 回應消息失敗: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("發送 Weapon 回應消息失敗: {}", e.getMessage(), e);
        }
    }
    

}
