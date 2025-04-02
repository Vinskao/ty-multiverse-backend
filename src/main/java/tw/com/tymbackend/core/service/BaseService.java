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

@Service
public abstract class BaseService {
    
    @PersistenceContext
    protected EntityManager entityManager;
    
    @Autowired
    protected DataSource dataSource;
    
    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    protected Executor executor;
    
    @Autowired
    private PlatformTransactionManager transactionManager;
    
    // 連接池監控
    protected static final ConcurrentHashMap<String, AtomicInteger> connectionUsageCount = 
        new ConcurrentHashMap<>();
    
    // 連接池管理
    protected Connection getConnection() throws SQLException {
        String threadName = Thread.currentThread().getName();
        connectionUsageCount.computeIfAbsent(threadName, k -> new AtomicInteger(0))
                          .incrementAndGet();
        return dataSource.getConnection();
    }
    
    // 釋放連接
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
    
    // 事務管理
    protected <T> T executeInTransaction(TransactionCallback<T> action) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        return transactionTemplate.execute(action);
    }
} 