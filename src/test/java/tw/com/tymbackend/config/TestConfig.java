package tw.com.tymbackend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;

/**
 * 測試環境的配置類。
 * 
 * 此類提供測試特定的配置 bean，這些 bean 在測試期間覆蓋默認的應用程序配置。
 * 它專門設計用於與 Spring Boot 測試上下文一起使用。
 * 
 * 此配置的主要目的是提供一個內存 H2 數據庫用於測試，確保測試在隔離環境中運行，
 * 不會影響任何外部數據庫。
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * 創建並配置用於測試目的的內存 H2 數據庫。
     * 
     * 此 bean 標記為 @Primary，確保它在測試期間優先於應用程序上下文中可能定義的
     * 任何其他 DataSource bean。
     * 
     * @return 一個配置好的 DataSource 實例，指向內存 H2 數據庫
     */
    @Bean
    @Primary
    DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
} 