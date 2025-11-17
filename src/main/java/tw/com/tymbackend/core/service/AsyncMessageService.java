package tw.com.tymbackend.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.core.config.RabbitMQConfig;
import tw.com.tymbackend.core.message.AsyncMessageDTO;
import jakarta.annotation.PostConstruct;

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
//@ConditionalOnProperty(name = "async-message-service.enabled", havingValue = "true") // 完全移除條件，讓它總是創建
public class AsyncMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncMessageService.class);

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        logger.info("=== AsyncMessageService 已初始化 ===");
        logger.info("AsyncMessageService Bean 已創建，RabbitMQ 異步處理已啟用");
    }
    
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
            "/tymb/people/damageWithWeapon",
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
            "/tymb/people/get-all",
            "POST",
            null
        );

        sendMessage(RabbitMQConfig.PEOPLE_GET_ALL_QUEUE, message);

        logger.info("發送角色列表獲取請求到 RabbitMQ: requestId={}", requestId);

        return requestId;
    }

    /**
     * 發送角色按名稱獲取請求到 RabbitMQ
     *
     * @param name 角色名稱
     * @return 請求ID
     */
    public String sendPeopleGetByNameRequest(String name) {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/people/get-by-name",
            "POST",
            name
        );

        sendMessage(RabbitMQConfig.PEOPLE_GET_BY_NAME_QUEUE, message);

        logger.info("發送角色按名稱獲取請求到 RabbitMQ: name={}, requestId={}", name, requestId);

        return requestId;
    }

    /**
     * 發送角色刪除全部請求到 RabbitMQ
     *
     * @return 請求ID
     */
    /**
     * 發送獲取角色名稱列表請求到 RabbitMQ
     *
     * @return 請求ID
     */
    public String sendPeopleGetNamesRequest() {
        if (rabbitTemplate == null) {
            throw new IllegalStateException("RabbitTemplate is not available. Please check RabbitMQ configuration.");
        }

        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/people/names",
            "GET",
            null
        );

        sendMessage(RabbitMQConfig.PEOPLE_GET_NAMES_QUEUE, message);

        logger.info("發送獲取角色名稱列表請求到 RabbitMQ: requestId={}", requestId);

        return requestId;
    }

    /**
     * 發送新增角色請求到 RabbitMQ
     *
     * @param people 要新增的角色數據
     * @return 請求ID
     */
    public String sendPeopleInsertRequest(Object people) {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/people/insert",
            "POST",
            people
        );

        sendMessage(RabbitMQConfig.PEOPLE_INSERT_QUEUE, message);

        logger.info("發送新增角色請求到 RabbitMQ: requestId={}, people={}", requestId, people);

        return requestId;
    }

    /**
     * 發送角色更新請求到 RabbitMQ
     *
     * @param people 要更新的角色數據
     * @return 請求ID
     */
    public String sendPeopleUpdateRequest(Object people) {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/people/update",
            "POST",
            people
        );

        sendMessage(RabbitMQConfig.PEOPLE_UPDATE_QUEUE, message);

        logger.info("發送角色更新請求到 RabbitMQ: requestId={}, people={}", requestId, people);

        return requestId;
    }

    public String sendPeopleDeleteAllRequest() {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/people/delete-all",
            "POST",
            null
        );

        sendMessage(RabbitMQConfig.PEOPLE_DELETE_ALL_QUEUE, message);

        logger.info("發送角色刪除全部請求到 RabbitMQ: requestId={}", requestId);

        return requestId;
    }

    /**
     * 發送武器列表獲取請求到 RabbitMQ
     *
     * @return 請求ID
     */
    public String sendWeaponGetAllRequest() {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/weapons",
            "GET",
            null
        );

        sendMessage(RabbitMQConfig.WEAPON_GET_ALL_QUEUE, message);

        logger.info("發送武器列表獲取請求到 RabbitMQ: requestId={}", requestId);

        return requestId;
    }

    /**
     * 發送武器按名稱獲取請求到 RabbitMQ
     *
     * @param name 武器名稱
     * @return 請求ID
     */
    public String sendWeaponGetByNameRequest(String name) {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/weapons/" + name,
            "GET",
            name
        );

        sendMessage(RabbitMQConfig.WEAPON_GET_BY_NAME_QUEUE, message);

        logger.info("發送武器按名稱獲取請求到 RabbitMQ: name={}, requestId={}", name, requestId);

        return requestId;
    }

    /**
     * 發送武器按擁有者獲取請求到 RabbitMQ
     *
     * @param owner 擁有者名稱
     * @return 請求ID
     */
    public String sendWeaponGetByOwnerRequest(String owner) {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/weapons/owner/" + owner,
            "GET",
            owner
        );

        sendMessage(RabbitMQConfig.WEAPON_GET_BY_OWNER_QUEUE, message);

        logger.info("發送武器按擁有者獲取請求到 RabbitMQ: owner={}, requestId={}", owner, requestId);

        return requestId;
    }

    /**
     * 發送武器保存請求到 RabbitMQ
     *
     * @param weapon 武器對象
     * @return 請求ID
     */
    public String sendWeaponSaveRequest(Object weapon) {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/weapons",
            "POST",
            weapon
        );

        sendMessage(RabbitMQConfig.WEAPON_SAVE_QUEUE, message);

        logger.info("發送武器保存請求到 RabbitMQ: requestId={}", requestId);

        return requestId;
    }

    /**
     * 發送武器刪除請求到 RabbitMQ
     *
     * @param name 武器名稱
     * @return 請求ID
     */
    public String sendWeaponDeleteRequest(String name) {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/weapons/" + name,
            "DELETE",
            name
        );

        sendMessage(RabbitMQConfig.WEAPON_DELETE_QUEUE, message);

        logger.info("發送武器刪除請求到 RabbitMQ: name={}, requestId={}", name, requestId);

        return requestId;
    }

    /**
     * 發送武器刪除全部請求到 RabbitMQ
     *
     * @return 請求ID
     */
    public String sendWeaponDeleteAllRequest() {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/weapons/delete-all",
            "DELETE",
            null
        );

        sendMessage(RabbitMQConfig.WEAPON_DELETE_ALL_QUEUE, message);

        logger.info("發送武器刪除全部請求到 RabbitMQ: requestId={}", requestId);

        return requestId;
    }

    /**
     * 發送武器存在檢查請求到 RabbitMQ
     *
     * @param name 武器名稱
     * @return 請求ID
     */
    public String sendWeaponExistsRequest(String name) {
        String requestId = UUID.randomUUID().toString();

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            "/tymb/weapons/exists/" + name,
            "GET",
            name
        );

        sendMessage(RabbitMQConfig.WEAPON_EXISTS_QUEUE, message);

        logger.info("發送武器存在檢查請求到 RabbitMQ: name={}, requestId={}", name, requestId);

        return requestId;
    }

    /**
     * 發送 Deckofcards 遊戲請求到 RabbitMQ
     *
     * @param action 遊戲動作 (start, hit, stand, status, double, split)
     * @param payload 額外數據
     * @return 請求ID
     */
    public String sendDeckofcardsRequest(String action, Object payload) {
        String requestId = UUID.randomUUID().toString();

        String endpoint = "/blackjack/" + action;
        String method = action.equals("status") ? "GET" : "POST";

        AsyncMessageDTO message = new AsyncMessageDTO(
            requestId,
            endpoint,
            method,
            payload
        );

        sendMessage(RabbitMQConfig.DECKOFCARDS_QUEUE, message);

        logger.info("發送 Deckofcards {} 請求到 RabbitMQ: requestId={}", action, requestId);

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
            // 直接發送對象，RabbitTemplate 的 Jackson2JsonMessageConverter 會自動序列化
            rabbitTemplate.convertAndSend(RabbitMQConfig.TYMB_EXCHANGE, getRoutingKey(queueName), message);
            
            logger.debug("消息已發送到隊列 {}: requestId={}, endpoint={}", 
                queueName, message.getRequestId(), message.getEndpoint());
            
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
        logger.debug("getRoutingKey called with queueName: '{}'", queueName);
        switch (queueName) {
            case RabbitMQConfig.DAMAGE_CALCULATION_QUEUE:
                return "people.damage.calculation";
            case RabbitMQConfig.PEOPLE_GET_ALL_QUEUE:
                return "people.get.all";
            case RabbitMQConfig.PEOPLE_GET_BY_NAME_QUEUE:
                return "people.get.by.name";
            case RabbitMQConfig.PEOPLE_GET_NAMES_QUEUE:
                return "people.get.names";
            case RabbitMQConfig.PEOPLE_INSERT_QUEUE:
                return "people.insert";
            case RabbitMQConfig.PEOPLE_UPDATE_QUEUE:
                return "people.update";
            case RabbitMQConfig.PEOPLE_DELETE_ALL_QUEUE:
                return "people.delete.all";
            case RabbitMQConfig.WEAPON_GET_ALL_QUEUE:
                return "weapon.get.all";
            case RabbitMQConfig.WEAPON_GET_BY_NAME_QUEUE:
                return "weapon.get.by.name";
            case RabbitMQConfig.WEAPON_GET_BY_OWNER_QUEUE:
                return "weapon.get.by.owner";
            case RabbitMQConfig.WEAPON_SAVE_QUEUE:
                return "weapon.save";
            case RabbitMQConfig.WEAPON_DELETE_QUEUE:
                return "weapon.delete";
            case RabbitMQConfig.WEAPON_DELETE_ALL_QUEUE:
                return "weapon.delete.all";
            case RabbitMQConfig.WEAPON_EXISTS_QUEUE:
                return "weapon.exists";
            case RabbitMQConfig.DECKOFCARDS_QUEUE:
                return "deckofcards";
            default:
                throw new IllegalArgumentException("未知的隊列名稱: " + queueName);
        }
    }
}
