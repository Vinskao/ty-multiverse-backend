package tw.com.tymbackend.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * RabbitMQ 連接重試包裝器
 *
 * 為 RabbitMQ 操作提供自動重試功能，處理臨時連接問題。
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
public class RabbitMQRetryWrapper {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQRetryWrapper.class);

    @Autowired
    @Qualifier("rabbitRetryTemplate")
    private RetryTemplate rabbitRetryTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 執行 RabbitMQ 操作，自動重試連接失敗
     *
     * @param operation 要執行的 MQ 操作
     * @param operationName 操作名稱（用於日誌）
     * @param <T> 返回類型
     * @return 操作結果
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        return rabbitRetryTemplate.execute(context -> {
            try {
                logger.debug("嘗試執行 MQ 操作: {}, 重試次數: {}", operationName, context.getRetryCount());
                return operation.get();
            } catch (Exception e) {
                logger.warn("MQ 操作失敗: {}, 重試次數: {}, 錯誤: {}",
                    operationName, context.getRetryCount(), e.getMessage());

                // 如果是連接相關的異常，拋出讓 RetryTemplate 重試
                if (isConnectionException(e)) {
                    throw e;
                }

                // 其他異常不重試，直接拋出
                throw new RuntimeException("MQ 操作失敗: " + operationName, e);
            }
        });
    }

    /**
     * 發送消息到交換機，自動重試連接失敗
     *
     * @param exchange 交換機名稱
     * @param routingKey 路由鍵
     * @param message 消息內容
     * @param messageId 消息ID（用於日誌）
     */
    public void sendWithRetry(String exchange, String routingKey, Object message, String messageId) {
        executeWithRetry(() -> {
            try {
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
                logger.debug("成功發送消息: {} 到 {}/{}", messageId, exchange, routingKey);
                return true;
            } catch (AmqpException e) {
                logger.error("發送消息失敗: {}, 交換機: {}, 路由鍵: {}", messageId, exchange, routingKey);
                throw e;
            }
        }, "SendMessage-" + messageId);
    }

    /**
     * 發送消息到隊列，自動重試連接失敗
     *
     * @param queueName 隊列名稱
     * @param message 消息內容
     * @param messageId 消息ID（用於日誌）
     */
    public void sendToQueueWithRetry(String queueName, Object message, String messageId) {
        executeWithRetry(() -> {
            try {
                rabbitTemplate.convertAndSend(queueName, message);
                logger.debug("成功發送消息: {} 到隊列: {}", messageId, queueName);
                return true;
            } catch (AmqpException e) {
                logger.error("發送消息到隊列失敗: {}, 隊列: {}", messageId, queueName);
                throw e;
            }
        }, "SendToQueue-" + messageId);
    }

    /**
     * 檢查是否為連接相關的異常
     *
     * @param e 異常
     * @return 是否為連接異常
     */
    private boolean isConnectionException(Exception e) {
        if (e instanceof AmqpException) {
            String message = e.getMessage();
            if (message != null) {
                message = message.toLowerCase();
                return message.contains("connection") ||
                       message.contains("channel") ||
                       message.contains("broker") ||
                       message.contains("unreachable") ||
                       message.contains("timeout") ||
                       message.contains("refused");
            }
        }

        // 其他連接相關異常
        String message = e.getMessage();
        if (message != null) {
            message = message.toLowerCase();
            return message.contains("connection") &&
                   (message.contains("refused") ||
                    message.contains("timeout") ||
                    message.contains("unreachable") ||
                    message.contains("reset"));
        }

        return false;
    }

    /**
     * 檢查 RabbitMQ 連接狀態
     *
     * @return 是否連接正常
     */
    public boolean isConnectionHealthy() {
        try {
            // 嘗試一個簡單的連接測試
            rabbitTemplate.execute(channel -> {
                channel.getConnection().isOpen();
                return null;
            });
            return true;
        } catch (Exception e) {
            logger.warn("RabbitMQ 連接檢查失敗: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 獲取當前重試統計信息
     *
     * @return 重試統計
     */
    public String getRetryStatistics() {
        return "RabbitMQ retry template is active";
    }
}
