package tw.com.tymbackend.core.config;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableAsync
public class MultiThreadedDataSource {

    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);  
        executor.setQueueCapacity(100); 
        executor.setThreadNamePrefix("DBThread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        
        // 基本連線池設定
        config.setMinimumIdle(2);  
        config.setMaximumPoolSize(8);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(30000);
        
        // 優化多線程設定
        config.setAllowPoolSuspension(false);
        config.setInitializationFailTimeout(1);
        config.setValidationTimeout(5000);
        config.setLeakDetectionThreshold(60000);
        
        // 添加鎖相關配置
        config.setAutoCommit(false);  // 關閉自動提交
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");  // 設置隔離級別
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        if (this.jdbcTemplate == null) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return this.jdbcTemplate;
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<String>> executeQueryAsync(String sql) {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate has not been initialized");
        }
        List<String> results = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> rs.getString(1)
        );
        return CompletableFuture.completedFuture(results);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Integer> executeUpdateAsync(String sql, Object... params) {
        int result = jdbcTemplate.update(sql, params);
        return CompletableFuture.completedFuture(result);
    }

    @Async("threadPoolTaskExecutor")
    public <T> CompletableFuture<List<T>> executeComplexQueryAsync(String sql, 
                                                                  Class<T> targetClass) {
        List<T> results = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                try {
                    T instance = targetClass.getDeclaredConstructor().newInstance();
                    // 在這裡進行結果集到對象的映射
                    // 這裡需要根據具體的類型進行處理
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException("Error mapping result set", e);
                }
            }
        );
        return CompletableFuture.completedFuture(results);
    }
} 