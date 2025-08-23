package tw.com.tymbackend.module.weapon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.core.config.RabbitMQConfig;
import tw.com.tymbackend.core.config.QueueNames;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Weapon 模組 Producer 服務
 * 
 * 負責將 Weapon 相關操作發送為消息到 RabbitMQ
 * 只在 RabbitMQ 啟用時生效
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Service
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class WeaponProducerService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeaponProducerService.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 發送獲取所有武器請求
     */
    public String sendGetAllWeaponsRequest() {
        String requestId = UUID.randomUUID().toString();
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "GET_ALL",
            null,
            null
        );
        
        sendMessage(QueueNames.WEAPON_GET_ALL.getQueueName(), message);
        
        logger.info("發送獲取所有武器請求到 RabbitMQ: requestId={}", requestId);
        
        return requestId;
    }
    
    /**
     * 發送根據名稱獲取武器請求
     */
    public String sendGetWeaponByNameRequest(String name) {
        String requestId = UUID.randomUUID().toString();
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "GET_BY_NAME",
            null,
            name
        );
        
        sendMessage(QueueNames.WEAPON_GET_BY_NAME.getQueueName(), message);
        
        logger.info("發送根據名稱獲取武器請求到 RabbitMQ: name={}, requestId={}", 
                   name, requestId);
        
        return requestId;
    }
    
    /**
     * 發送根據擁有者獲取武器請求
     */
    public String sendGetWeaponsByOwnerRequest(String owner) {
        String requestId = UUID.randomUUID().toString();
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "GET_BY_OWNER",
            null,
            owner
        );
        
        sendMessage(QueueNames.WEAPON_GET_BY_OWNER.getQueueName(), message);
        
        logger.info("發送根據擁有者獲取武器請求到 RabbitMQ: owner={}, requestId={}", 
                   owner, requestId);
        
        return requestId;
    }
    
    /**
     * 發送保存武器請求
     */
    public String sendSaveWeaponRequest(Weapon weapon) {
        String requestId = UUID.randomUUID().toString();
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "SAVE",
            weapon,
            null
        );
        
        sendMessage(QueueNames.WEAPON_SAVE.getQueueName(), message);
        
        logger.info("發送保存武器請求到 RabbitMQ: name={}, requestId={}", 
                   weapon.getName(), requestId);
        
        return requestId;
    }
    
    /**
     * 發送刪除武器請求
     */
    public String sendDeleteWeaponRequest(String name) {
        String requestId = UUID.randomUUID().toString();
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "DELETE",
            null,
            name
        );
        
        sendMessage(QueueNames.WEAPON_DELETE.getQueueName(), message);
        
        logger.info("發送刪除武器請求到 RabbitMQ: name={}, requestId={}", 
                   name, requestId);
        
        return requestId;
    }
    
    /**
     * 發送刪除所有武器請求
     */
    public String sendDeleteAllWeaponsRequest() {
        String requestId = UUID.randomUUID().toString();
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "DELETE_ALL",
            null,
            null
        );
        
        sendMessage(QueueNames.WEAPON_DELETE_ALL.getQueueName(), message);
        
        logger.info("發送刪除所有武器請求到 RabbitMQ: requestId={}", requestId);
        
        return requestId;
    }
    
    /**
     * 發送檢查武器存在請求
     */
    public String sendWeaponExistsRequest(String name) {
        String requestId = UUID.randomUUID().toString();
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "EXISTS",
            null,
            name
        );
        
        sendMessage(QueueNames.WEAPON_EXISTS.getQueueName(), message);
        
        logger.info("發送檢查武器存在請求到 RabbitMQ: name={}, requestId={}", 
                   name, requestId);
        
        return requestId;
    }
    
    /**
     * 發送更新武器屬性請求
     */
    public String sendUpdateWeaponAttributesRequest(String name, String attributes) {
        String requestId = UUID.randomUUID().toString();
        
        Map<String, String> data = Map.of("name", name, "attributes", attributes);
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "UPDATE_ATTRIBUTES",
            null,
            data
        );
        
        sendMessage(QueueNames.WEAPON_UPDATE_ATTRIBUTES.getQueueName(), message);
        
        logger.info("發送更新武器屬性請求到 RabbitMQ: name={}, requestId={}", 
                   name, requestId);
        
        return requestId;
    }
    
    /**
     * 發送更新武器基礎傷害請求
     */
    public String sendUpdateWeaponBaseDamageRequest(String name, Integer baseDamage) {
        String requestId = UUID.randomUUID().toString();
        
        Map<String, Object> data = Map.of("name", name, "baseDamage", baseDamage);
        
        WeaponMessageDTO message = new WeaponMessageDTO(
            requestId,
            "UPDATE_BASE_DAMAGE",
            null,
            data
        );
        
        sendMessage(QueueNames.WEAPON_UPDATE_BASE_DAMAGE.getQueueName(), message);
        
        logger.info("發送更新武器基礎傷害請求到 RabbitMQ: name={}, baseDamage={}, requestId={}", 
                   name, baseDamage, requestId);
        
        return requestId;
    }
    
    /**
     * 發送消息到指定隊列
     */
    private void sendMessage(String queueName, WeaponMessageDTO message) {
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
     */
    private String getRoutingKey(String queueName) {
        switch (queueName) {
            case "weapon-get-all":
                return "weapon.get.all";
            case "weapon-get-by-name":
                return "weapon.get.by.name";
            case "weapon-get-by-owner":
                return "weapon.get.by.owner";
            case "weapon-save":
                return "weapon.save";
            case "weapon-delete":
                return "weapon.delete";
            case "weapon-delete-all":
                return "weapon.delete.all";
            case "weapon-exists":
                return "weapon.exists";
            case "weapon-update-attributes":
                return "weapon.update.attributes";
            case "weapon-update-base-damage":
                return "weapon.update.base.damage";
            default:
                throw new IllegalArgumentException("未知的隊列名稱: " + queueName);
        }
    }
    
    /**
     * Weapon 消息 DTO
     */
    public static class WeaponMessageDTO {
        private String requestId;
        private String operation;
        private Weapon weapon;
        private Object data;
        
        public WeaponMessageDTO() {}
        
        public WeaponMessageDTO(String requestId, String operation, Weapon weapon, Object data) {
            this.requestId = requestId;
            this.operation = operation;
            this.weapon = weapon;
            this.data = data;
        }
        
        // Getters and Setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public Weapon getWeapon() { return weapon; }
        public void setWeapon(Weapon weapon) { this.weapon = weapon; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}
