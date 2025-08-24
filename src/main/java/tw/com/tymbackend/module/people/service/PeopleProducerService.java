package tw.com.tymbackend.module.people.service;

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
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.List;
import java.util.UUID;

/**
 * People 模組 Producer 服務
 * 
 * 負責將 People 相關操作發送為消息到 RabbitMQ
 * 只在 RabbitMQ 啟用時生效
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Service
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class PeopleProducerService {
    
    private static final Logger logger = LoggerFactory.getLogger(PeopleProducerService.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 發送插入單個角色請求
     */
    public String sendInsertPeopleRequest(People people) {
        String requestId = UUID.randomUUID().toString();
        
        PeopleMessageDTO message = new PeopleMessageDTO(
            requestId,
            "INSERT_SINGLE",
            people,
            null
        );
        
        sendMessage(QueueNames.PEOPLE_INSERT.getQueueName(), message);
        
        logger.info("發送插入角色請求到 RabbitMQ: name={}, requestId={}", 
                   people.getName(), requestId);
        
        return requestId;
    }
    
    /**
     * 發送更新角色請求
     */
    public String sendUpdatePeopleRequest(People people) {
        String requestId = UUID.randomUUID().toString();
        
        PeopleMessageDTO message = new PeopleMessageDTO(
            requestId,
            "UPDATE",
            people,
            null
        );
        
        sendMessage(QueueNames.PEOPLE_UPDATE.getQueueName(), message);
        
        logger.info("發送更新角色請求到 RabbitMQ: name={}, requestId={}", 
                   people.getName(), requestId);
        
        return requestId;
    }
    
    /**
     * 發送插入多個角色請求
     */
    public String sendInsertMultiplePeopleRequest(List<People> peopleList) {
        String requestId = UUID.randomUUID().toString();
        
        PeopleMessageDTO message = new PeopleMessageDTO(
            requestId,
            "INSERT_MULTIPLE",
            null,
            peopleList
        );
        
        sendMessage(QueueNames.PEOPLE_INSERT_MULTIPLE.getQueueName(), message);
        
        logger.info("發送插入多個角色請求到 RabbitMQ: count={}, requestId={}", 
                   peopleList.size(), requestId);
        
        return requestId;
    }
    
    /**
     * 發送獲取所有角色請求
     */
    public String sendGetAllPeopleRequest() {
        String requestId = UUID.randomUUID().toString();
        
        PeopleMessageDTO message = new PeopleMessageDTO(
            requestId,
            "GET_ALL",
            null,
            null
        );
        
        sendMessage(QueueNames.PEOPLE_GET_ALL.getQueueName(), message);
        
        logger.info("發送獲取所有角色請求到 RabbitMQ: requestId={}", requestId);
        
        return requestId;
    }
    
    /**
     * 發送根據名稱獲取角色請求
     */
    public String sendGetPeopleByNameRequest(String name) {
        String requestId = UUID.randomUUID().toString();
        
        PeopleMessageDTO message = new PeopleMessageDTO(
            requestId,
            "GET_BY_NAME",
            null,
            name
        );
        
        sendMessage(QueueNames.PEOPLE_GET_BY_NAME.getQueueName(), message);
        
        logger.info("發送根據名稱獲取角色請求到 RabbitMQ: name={}, requestId={}", 
                   name, requestId);
        
        return requestId;
    }
    
    /**
     * 發送刪除角色請求
     */
    public String sendDeletePeopleRequest(String name) {
        String requestId = UUID.randomUUID().toString();
        
        PeopleMessageDTO message = new PeopleMessageDTO(
            requestId,
            "DELETE",
            null,
            name
        );
        
        sendMessage(QueueNames.PEOPLE_DELETE.getQueueName(), message);
        
        logger.info("發送刪除角色請求到 RabbitMQ: name={}, requestId={}", 
                   name, requestId);
        
        return requestId;
    }
    
    /**
     * 發送刪除所有角色請求
     */
    public String sendDeleteAllPeopleRequest() {
        String requestId = UUID.randomUUID().toString();
        
        PeopleMessageDTO message = new PeopleMessageDTO(
            requestId,
            "DELETE_ALL",
            null,
            null
        );
        
        sendMessage(QueueNames.PEOPLE_DELETE_ALL.getQueueName(), message);
        
        logger.info("發送刪除所有角色請求到 RabbitMQ: requestId={}", requestId);
        
        return requestId;
    }
    
    /**
     * 發送傷害計算請求
     */
    public String sendDamageCalculationRequest(String characterName) {
        String requestId = UUID.randomUUID().toString();
        
        PeopleMessageDTO message = new PeopleMessageDTO(
            requestId,
            "DAMAGE_CALCULATION",
            null,
            characterName
        );
        
        sendMessage(QueueNames.PEOPLE_DAMAGE_CALCULATION.getQueueName(), message);
        
        logger.info("發送傷害計算請求到 RabbitMQ: characterName={}, requestId={}", 
                   characterName, requestId);
        
        return requestId;
    }
    
    /**
     * 發送消息到指定隊列
     */
    private void sendMessage(String queueName, PeopleMessageDTO message) {
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
            case "people-insert":
                return "people.insert";
            case "people-update":
                return "people.update";
            case "people-insert-multiple":
                return "people.insert.multiple";
            case "people-get-all":
                return "people.get.all";
            case "people-get-by-name":
                return "people.get.by.name";
            case "people-delete":
                return "people.delete";
            case "people-delete-all":
                return "people.delete.all";
            case "people-damage-calculation":
                return "people.damage.calculation";
            default:
                throw new IllegalArgumentException("未知的隊列名稱: " + queueName);
        }
    }
    
    /**
     * People 消息 DTO
     */
    public static class PeopleMessageDTO {
        private String requestId;
        private String operation;
        private People people;
        private Object data;
        
        public PeopleMessageDTO() {}
        
        public PeopleMessageDTO(String requestId, String operation, People people, Object data) {
            this.requestId = requestId;
            this.operation = operation;
            this.people = people;
            this.data = data;
        }
        
        // Getters and Setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public People getPeople() { return people; }
        public void setPeople(People people) { this.people = people; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}
