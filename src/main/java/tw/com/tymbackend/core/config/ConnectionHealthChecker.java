package tw.com.tymbackend.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 應用啟動時的連接健康檢查器
 *
 * 在應用完全啟動後，檢查資料庫和 RabbitMQ 連接狀態
 * 如果連接失敗，會等待並重試，直到成功或達到最大重試次數
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
public class ConnectionHealthChecker {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionHealthChecker.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RabbitMQRetryWrapper rabbitMQRetryWrapper;

    @Value("${app.connection.health-check.enabled:true}")
    private boolean healthCheckEnabled;

    @Value("${app.connection.health-check.max-retries:30}")
    private int maxRetries;

    @Value("${app.connection.health-check.retry-interval-seconds:10}")
    private int retryIntervalSeconds;

    @PostConstruct
    public void init() {
        logger.info("ConnectionHealthChecker initialized with:");
        logger.info("  - Health check enabled: {}", healthCheckEnabled);
        logger.info("  - Max retries: {}", maxRetries);
        logger.info("  - Retry interval: {} seconds", retryIntervalSeconds);
    }

    /**
     * 應用完全啟動後執行連接檢查
     */
    @EventListener(ApplicationReadyEvent.class)
    public void checkConnectionsOnStartup() {
        if (!healthCheckEnabled) {
            logger.info("Connection health check is disabled");
            return;
        }

        logger.info("🔍 開始執行應用啟動連接檢查...");

        try {
            // 檢查資料庫連接
            checkDatabaseConnection();

            // 檢查 RabbitMQ 連接
            checkRabbitMQConnection();

            logger.info("✅ 所有連接檢查通過，應用正常啟動");

        } catch (Exception e) {
            logger.error("❌ 連接檢查失敗: {}", e.getMessage());
            // 在生產環境中，這裡可能需要決定是否讓應用繼續啟動
            throw new RuntimeException("應用啟動連接檢查失敗", e);
        }
    }

    /**
     * 檢查資料庫連接
     */
    private void checkDatabaseConnection() {
        logger.info("🔍 檢查資料庫連接...");

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // 執行一個簡單的查詢來測試連接
                Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

                if (result != null && result == 1) {
                    logger.info("✅ 資料庫連接正常 (嘗試次數: {})", attempt);
                    return;
                }

            } catch (Exception e) {
                logger.warn("❌ 資料庫連接失敗 (嘗試 {}/{}): {}",
                    attempt, maxRetries, e.getMessage());

                if (attempt == maxRetries) {
                    throw new RuntimeException("資料庫連接檢查失敗，已達最大重試次數", e);
                }

                // 等待後重試
                waitBeforeRetry("資料庫連接", attempt);
            }
        }
    }

    /**
     * 檢查 RabbitMQ 連接
     */
    private void checkRabbitMQConnection() {
        logger.info("🔍 檢查 RabbitMQ 連接...");

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                boolean isHealthy = rabbitMQRetryWrapper.isConnectionHealthy();

                if (isHealthy) {
                    logger.info("✅ RabbitMQ 連接正常 (嘗試次數: {})", attempt);
                    return;
                } else {
                    throw new RuntimeException("RabbitMQ 連接狀態異常");
                }

            } catch (Exception e) {
                logger.warn("❌ RabbitMQ 連接失敗 (嘗試 {}/{}): {}",
                    attempt, maxRetries, e.getMessage());

                if (attempt == maxRetries) {
                    throw new RuntimeException("RabbitMQ 連接檢查失敗，已達最大重試次數", e);
                }

                // 等待後重試
                waitBeforeRetry("RabbitMQ 連接", attempt);
            }
        }
    }

    /**
     * 在重試前等待
     *
     * @param connectionType 連接類型
     * @param attempt 當前嘗試次數
     */
    private void waitBeforeRetry(String connectionType, int attempt) {
        try {
            logger.info("⏳ 等待 {} 秒後重試 {} (嘗試 {}/{})",
                retryIntervalSeconds, connectionType, attempt + 1, maxRetries);

            TimeUnit.SECONDS.sleep(retryIntervalSeconds);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.error("等待重試時被中斷: {}", connectionType);
        }
    }

    /**
     * 運行時檢查連接狀態
     *
     * @param connectionType 要檢查的連接類型 ("database" 或 "rabbitmq")
     * @return 連接是否正常
     */
    public boolean checkConnectionStatus(String connectionType) {
        try {
            switch (connectionType.toLowerCase()) {
                case "database":
                    jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                    return true;
                case "rabbitmq":
                    return rabbitMQRetryWrapper.isConnectionHealthy();
                default:
                    logger.warn("未知的連接類型: {}", connectionType);
                    return false;
            }
        } catch (Exception e) {
            logger.error("檢查 {} 連接狀態失敗: {}", connectionType, e.getMessage());
            return false;
        }
    }
}
