package tw.com.tymbackend.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基礎服務類別，提供所有服務層共用的基本功能。
 * 包含資料庫連接管理、事務處理和執行緒池管理等核心功能。
 *
 * @author TYM Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
public abstract class BaseService {
    
    /**
     * JPA 實體管理器，用於資料庫操作。
     */
    @PersistenceContext
    protected EntityManager entityManager;
    
    /**
     * 資料來源，用於獲取資料庫連接。
     */
    @Autowired
    protected DataSource dataSource;
    
    /**
     * 執行緒池，用於處理非同步任務。
     */
    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    protected Executor executor;
    
    /**
     * 事務管理器，用於處理資料庫事務。
     */
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    /**
     * 連接池使用計數器，用於監控每個執行緒的資料庫連接使用情況。
     * Key: 執行緒名稱
     * Value: 連接使用次數
     */
    protected static final ConcurrentHashMap<String, AtomicInteger> connectionUsageCount = 
        new ConcurrentHashMap<>();
    
    /**
     * 獲取資料庫連接。
     * 同時會記錄當前執行緒的連接使用次數。
     *
     * @return 資料庫連接
     * @throws SQLException 當獲取連接失敗時拋出
     */
    protected Connection getConnection() throws SQLException {
        String threadName = Thread.currentThread().getName();
        connectionUsageCount.computeIfAbsent(threadName, k -> new AtomicInteger(0))
                          .incrementAndGet();
        return dataSource.getConnection();
    }
    
    /**
     * 釋放資料庫連接。
     * 同時會減少當前執行緒的連接使用計數。
     *
     * @param connection 要釋放的資料庫連接
     */
    protected void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                String threadName = Thread.currentThread().getName();
                connectionUsageCount.computeIfPresent(threadName, (k, v) -> {
                    v.decrementAndGet();
                    return v;
                });
                connection.close();
            } catch (SQLException e) {
                // 記錄錯誤但不拋出異常
            }
        }
    }
    
    /**
     * 在事務中執行指定的操作。
     * 使用預設的事務隔離級別。
     *
     * @param <T> 操作返回值的類型
     * @param action 要在事務中執行的操作
     * @return 操作的執行結果
     */
    protected <T> T executeInTransaction(TransactionCallback<T> action) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        return transactionTemplate.execute(action);
    }
} 