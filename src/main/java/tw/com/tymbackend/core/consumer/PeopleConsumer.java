package tw.com.tymbackend.core.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tw.com.tymbackend.core.config.RabbitMQConfig;
import tw.com.tymbackend.core.message.AsyncMessageDTO;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.PeopleService;

import java.util.List;
import java.util.Map;

/**
 * People 請求處理 Consumer
 * 
 * 負責處理 People 相關的 RabbitMQ 請求
 * 只在 RabbitMQ 啟用時生效
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class PeopleConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(PeopleConsumer.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PeopleService peopleService;
    
    @Autowired
    private ConsumerResponseService consumerResponseService;
    
    /**
     * 監聽 People 獲取所有請求
     */
    @RabbitListener(queues = "people-get-all")
    public void handleGetAllPeople(String messageJson) {
        try {
            logger.info("收到獲取所有角色請求: {}", messageJson);
            
            AsyncMessageDTO message = objectMapper.readValue(messageJson, AsyncMessageDTO.class);
            String requestId = message.getRequestId();
            
            logger.info("開始獲取所有角色: requestId={}", requestId);
            
            // 處理請求
            List<People> peopleList = peopleService.getAllPeopleOptimized();
            
            // 記錄處理結果
            logger.info("成功獲取所有角色: count={}, requestId={}", peopleList.size(), requestId);
            logger.info("數據庫中的角色數據: ");
            for (People people : peopleList) {
                logger.info("  - 角色: name={}, age={}, gender={}, job={}, attributes={}", 
                           people.getName(), people.getAge(), people.getGender(), 
                           people.getJob(), people.getAttributes());
            }
            
            // 發送成功回應給 Producer
            consumerResponseService.sendPeopleGetAllSuccessResponse(
                requestId,
                "角色列表獲取成功",
                peopleList
            );
            
            logger.info("回傳消息解析成功: requestId={}, status=success, count={}", 
                       requestId, peopleList.size());
            
        } catch (Exception e) {
            logger.error("處理獲取所有角色請求失敗: {}", e.getMessage(), e);
            
            // 嘗試解析請求ID
            try {
                AsyncMessageDTO message = objectMapper.readValue(messageJson, AsyncMessageDTO.class);
                String requestId = message.getRequestId();
                
                // 發送錯誤回應給 Producer
                consumerResponseService.sendPeopleErrorResponse(
                    requestId,
                    "獲取角色列表失敗",
                    "PEOPLE_GET_ALL_ERROR",
                    e.getMessage()
                );
                
            } catch (Exception parseError) {
                logger.error("無法解析請求ID，無法發送錯誤回應: {}", parseError.getMessage());
            }
        }
    }
    
    /**
     * 監聽 People 根據名稱獲取請求
     */
    @RabbitListener(queues = "people-get-by-name")
    public void handleGetPeopleByName(String messageJson) {
        try {
            logger.info("收到根據名稱獲取角色請求: {}", messageJson);
            
            AsyncMessageDTO message = objectMapper.readValue(messageJson, AsyncMessageDTO.class);
            String requestId = message.getRequestId();
            String name = (String) message.getPayload();
            
            logger.info("開始根據名稱獲取角色: name={}, requestId={}", name, requestId);
            
            // 處理請求
            var peopleOptional = peopleService.getPeopleByName(name);
            
            if (peopleOptional.isPresent()) {
                People people = peopleOptional.get();
                logger.info("成功獲取角色: name={}, requestId={}", name, requestId);
                
                // 發送成功回應給 Producer
                consumerResponseService.sendPeopleSuccessResponse(
                    requestId,
                    "角色獲取成功",
                    people
                );
            } else {
                logger.warn("角色不存在: name={}, requestId={}", name, requestId);
                
                // 發送錯誤回應給 Producer
                consumerResponseService.sendPeopleErrorResponse(
                    requestId,
                    "角色不存在",
                    "PEOPLE_NOT_FOUND",
                    "角色名稱: " + name
                );
            }
            
        } catch (Exception e) {
            logger.error("處理根據名稱獲取角色請求失敗: {}", e.getMessage(), e);
            
            // 嘗試解析請求ID
            try {
                AsyncMessageDTO message = objectMapper.readValue(messageJson, AsyncMessageDTO.class);
                String requestId = message.getRequestId();
                
                // 發送錯誤回應給 Producer
                consumerResponseService.sendPeopleErrorResponse(
                    requestId,
                    "獲取角色失敗",
                    "PEOPLE_GET_BY_NAME_ERROR",
                    e.getMessage()
                );
                
            } catch (Exception parseError) {
                logger.error("無法解析請求ID，無法發送錯誤回應: {}", parseError.getMessage());
            }
        }
    }
    
    /**
     * 監聽 People 刪除所有請求
     */
    @RabbitListener(queues = "people-delete-all")
    public void handleDeleteAllPeople(String messageJson) {
        try {
            logger.info("收到刪除所有角色請求: {}", messageJson);
            
            AsyncMessageDTO message = objectMapper.readValue(messageJson, AsyncMessageDTO.class);
            String requestId = message.getRequestId();
            
            logger.info("開始刪除所有角色: requestId={}", requestId);
            
            // 處理請求
            peopleService.deleteAllPeople();
            
            logger.info("成功刪除所有角色: requestId={}", requestId);
            
            // 發送成功回應給 Producer
            consumerResponseService.sendPeopleSuccessResponse(
                requestId,
                "所有角色刪除成功",
                null
            );
            
        } catch (Exception e) {
            logger.error("處理刪除所有角色請求失敗: {}", e.getMessage(), e);
            
            // 嘗試解析請求ID
            try {
                AsyncMessageDTO message = objectMapper.readValue(messageJson, AsyncMessageDTO.class);
                String requestId = message.getRequestId();
                
                // 發送錯誤回應給 Producer
                consumerResponseService.sendPeopleErrorResponse(
                    requestId,
                    "刪除所有角色失敗",
                    "PEOPLE_DELETE_ALL_ERROR",
                    e.getMessage()
                );
                
            } catch (Exception parseError) {
                logger.error("無法解析請求ID，無法發送錯誤回應: {}", parseError.getMessage());
            }
        }
    }
}
