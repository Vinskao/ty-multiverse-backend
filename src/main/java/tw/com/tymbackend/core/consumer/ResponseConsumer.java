package tw.com.tymbackend.core.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tw.com.tymbackend.core.config.RabbitMQConfig;
import tw.com.tymbackend.core.message.ProducerResponseDTO;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 回應消息 Consumer
 * 
 * 負責處理從 Consumer 回傳給 Producer 的消息
 * 只在 RabbitMQ 啟用時生效
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class ResponseConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseConsumer.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 用於存儲請求狀態的 Map（在實際生產環境中應該使用 Redis 或數據庫）
    private final Map<String, Object> requestStatusMap = new ConcurrentHashMap<>();
    
    /**
     * 監聽 People 回應消息
     */
    @RabbitListener(queues = "people.response.queue")
    public void handlePeopleResponse(String messageJson) {
        try {
            logger.info("收到 People 回應消息: {}", messageJson);
            
            ProducerResponseDTO response = objectMapper.readValue(messageJson, ProducerResponseDTO.class);
            
            // 處理回應消息
            handlePeopleResponse(response);
            
        } catch (Exception e) {
            logger.error("處理 People 回應消息失敗: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 監聽 Weapon 回應消息
     */
    @RabbitListener(queues = "weapon.response.queue")
    public void handleWeaponResponse(String messageJson) {
        try {
            logger.info("收到 Weapon 回應消息: {}", messageJson);
            
            ProducerResponseDTO response = objectMapper.readValue(messageJson, ProducerResponseDTO.class);
            
            // 處理回應消息
            handleWeaponResponse(response);
            
        } catch (Exception e) {
            logger.error("處理 Weapon 回應消息失敗: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 處理 People 回應
     */
    private void handlePeopleResponse(ProducerResponseDTO response) {
        String requestId = response.getRequestId();
        
        // 存儲回應狀態
        requestStatusMap.put(requestId, response);
        
        logger.info("處理 People 回應: requestId={}, status={}, message={}", 
                   requestId, response.getStatus(), response.getMessage());
        
        // 根據狀態進行不同處理
        switch (response.getStatus()) {
            case "SUCCESS":
                logger.info("People 請求成功完成: requestId={}, dataSize={}", 
                           requestId, response.getData() != null ? "有數據" : "無數據");
                break;
            case "ERROR":
                logger.error("People 請求處理失敗: requestId={}, errorCode={}, errorDetails={}", 
                            requestId, response.getErrorCode(), response.getErrorDetails());
                break;
            case "PROCESSING":
                logger.info("People 請求正在處理中: requestId={}", requestId);
                break;
            default:
                logger.warn("未知的 People 回應狀態: requestId={}, status={}", requestId, response.getStatus());
        }
    }
    
    /**
     * 處理 Weapon 回應
     */
    private void handleWeaponResponse(ProducerResponseDTO response) {
        String requestId = response.getRequestId();
        
        // 存儲回應狀態
        requestStatusMap.put(requestId, response);
        
        logger.info("處理 Weapon 回應: requestId={}, status={}, message={}", 
                   requestId, response.getStatus(), response.getMessage());
        
        // 根據狀態進行不同處理
        switch (response.getStatus()) {
            case "SUCCESS":
                logger.info("Weapon 請求成功完成: requestId={}, dataSize={}", 
                           requestId, response.getData() != null ? "有數據" : "無數據");
                break;
            case "ERROR":
                logger.error("Weapon 請求處理失敗: requestId={}, errorCode={}, errorDetails={}", 
                            requestId, response.getErrorCode(), response.getErrorDetails());
                break;
            case "PROCESSING":
                logger.info("Weapon 請求正在處理中: requestId={}", requestId);
                break;
            default:
                logger.warn("未知的 Weapon 回應狀態: requestId={}, status={}", requestId, response.getStatus());
        }
    }
    
    /**
     * 獲取請求狀態
     */
    public Object getRequestStatus(String requestId) {
        return requestStatusMap.get(requestId);
    }
    
    /**
     * 移除請求狀態
     */
    public void removeRequestStatus(String requestId) {
        requestStatusMap.remove(requestId);
    }
    
    /**
     * 檢查請求是否存在
     */
    public boolean hasRequest(String requestId) {
        return requestStatusMap.containsKey(requestId);
    }
}
