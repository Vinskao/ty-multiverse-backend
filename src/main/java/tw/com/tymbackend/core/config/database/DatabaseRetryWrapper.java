package tw.com.tymbackend.core.config.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * 資料庫連接重試包裝器
 *
 * 為資料庫操作提供自動重試功能，處理臨時連接問題。
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
public class DatabaseRetryWrapper {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseRetryWrapper.class);

    @Autowired
    @Qualifier("databaseRetryTemplate")
    private RetryTemplate databaseRetryTemplate;

    /**
     * 執行資料庫操作，自動重試連接失敗
     *
     * @param operation 要執行的資料庫操作
     * @param operationName 操作名稱（用於日誌）
     * @param <T> 返回類型
     * @return 操作結果
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        return databaseRetryTemplate.execute(context -> {
            try {
                logger.debug("嘗試執行資料庫操作: {}, 重試次數: {}", operationName, context.getRetryCount());
                return operation.get();
            } catch (Exception e) {
                logger.warn("資料庫操作失敗: {}, 重試次數: {}, 錯誤: {}",
                    operationName, context.getRetryCount(), e.getMessage());

                // 如果是連接相關的異常，拋出讓 RetryTemplate 重試
                if (isConnectionException(e)) {
                    throw e;
                }

                // 其他異常不重試，直接拋出
                throw new RuntimeException("資料庫操作失敗: " + operationName, e);
            }
        });
    }

    /**
     * 執行資料庫查詢操作
     *
     * @param queryOperation 查詢操作
     * @param operationName 操作名稱
     * @return ResultSet
     */
    public ResultSet executeQueryWithRetry(Supplier<ResultSet> queryOperation, String operationName) {
        return executeWithRetry(queryOperation, operationName + " (Query)");
    }

    /**
     * 執行資料庫更新操作
     *
     * @param updateOperation 更新操作
     * @param operationName 操作名稱
     * @return 受影響的行數
     */
    public Integer executeUpdateWithRetry(Supplier<Integer> updateOperation, String operationName) {
        return executeWithRetry(updateOperation, operationName + " (Update)");
    }

    /**
     * 檢查是否為連接相關的異常
     *
     * @param e 異常
     * @return 是否為連接異常
     */
    private boolean isConnectionException(Exception e) {
        if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            String sqlState = sqlEx.getSQLState();

            // PostgreSQL 連接相關的 SQL 狀態碼
            return sqlState != null && (
                sqlState.startsWith("08") ||  // 連接異常
                sqlState.startsWith("57") ||  // 操作員干預
                sqlState.equals("08000") ||   // 連接異常
                sqlState.equals("08003") ||   // 連接不存在
                sqlState.equals("08006")      // 連接失敗
            );
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
     * 獲取當前重試統計信息
     *
     * @return 重試統計
     */
    public String getRetryStatistics() {
        // 這裡可以添加更詳細的統計信息
        return "Database retry template is active";
    }
}
