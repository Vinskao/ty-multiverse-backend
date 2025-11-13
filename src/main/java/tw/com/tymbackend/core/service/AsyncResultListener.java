package tw.com.tymbackend.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.core.config.RabbitMQConfig;
import tw.com.tymbackend.core.message.AsyncResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

/**
 * 異步結果監聽器
 * 
 * Producer 端監聽 async-result 隊列，接收 Consumer 發送的處理結果。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Service
public class AsyncResultListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncResultListener.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AsyncResultService asyncResultService;
    
    @PostConstruct
    public void init() {
        logger.info("=== AsyncResultListener 已註冊為 Spring Bean ===");
        logger.info("AsyncResultListener 正在監聽隊列: {}", RabbitMQConfig.ASYNC_RESULT_QUEUE);
    }
    
    /**
     * 監聽異步結果隊列
     *
     * @param messageJson 結果消息 JSON
     */
    @RabbitListener(queues = RabbitMQConfig.ASYNC_RESULT_QUEUE)
    public void handleAsyncResult(String messageJson) {
        logger.info("=== AsyncResultListener 被觸發 ===");
        logger.info("收到的原始消息: {}", messageJson);

        try {
            // 解析來自消費者的結果消息
            logger.info("開始解析 JSON 消息...");
            AsyncResultMessage resultMessage = objectMapper.readValue(messageJson, AsyncResultMessage.class);
            logger.info("解析成功! requestId={}, status={}, source={}",
                resultMessage.getRequestId(), resultMessage.getStatus(), resultMessage.getSource());

            // 根據狀態處理結果
            String status = resultMessage.getStatus();
            logger.info("檢查消息狀態: status={}", status);

            switch (status) {
                case "completed":
                    logger.info("處理完成狀態消息...");
                    handleCompletedResult(resultMessage);
                    logger.info("✅ 成功處理完成狀態消息: requestId={}", resultMessage.getRequestId());
                    break;
                case "failed":
                    logger.info("處理失敗狀態消息...");
                    handleFailedResult(resultMessage);
                    logger.info("✅ 成功處理失敗狀態消息: requestId={}", resultMessage.getRequestId());
                    break;
                default:
                    logger.warn("未知的結果狀態: {}", status);
            }

            logger.info("=== AsyncResultListener 處理完成 ===");

        } catch (Exception e) {
            logger.error("=== AsyncResultListener 處理失敗 ===");
            logger.error("消息內容: {}", messageJson);
            logger.error("錯誤類型: {}", e.getClass().getName());
            logger.error("錯誤訊息: {}", e.getMessage());
            logger.error("完整錯誤詳情: ", e);
            // 重新拋出異常以確保它被正確處理
            throw new RuntimeException("AsyncResultListener 處理失敗", e);
        }
    }
    
    /**
     * 處理完成狀態的結果
     *
     * @param resultMessage 來自消費者的結果消息
     */
    private void handleCompletedResult(tw.com.tymbackend.core.message.AsyncResultMessage resultMessage) {
        try {
            logger.info("處理完成結果: requestId={}, source={}",
                resultMessage.getRequestId(), resultMessage.getSource());

            // 存儲完成結果到 Redis
            asyncResultService.storeCompletedResult(
                resultMessage.getRequestId(),
                resultMessage.getData()
            );

            logger.info("成功存儲完成結果到Redis: requestId={}", resultMessage.getRequestId());

        } catch (Exception e) {
            logger.error("處理完成結果失敗: requestId={}", resultMessage.getRequestId(), e);
        }
    }

    /**
     * 處理失敗狀態的結果
     *
     * @param resultMessage 來自消費者的結果消息
     */
    private void handleFailedResult(tw.com.tymbackend.core.message.AsyncResultMessage resultMessage) {
        try {
            logger.info("處理失敗結果: requestId={}, source={}, error={}",
                resultMessage.getRequestId(), resultMessage.getSource(), resultMessage.getError());

            // 存儲失敗結果到 Redis
            asyncResultService.storeFailedResult(
                resultMessage.getRequestId(),
                resultMessage.getError()
            );

            logger.info("成功存儲失敗結果到Redis: requestId={}", resultMessage.getRequestId());

        } catch (Exception e) {
            logger.error("處理失敗結果失敗: requestId={}", resultMessage.getRequestId(), e);
        }
    }
}
