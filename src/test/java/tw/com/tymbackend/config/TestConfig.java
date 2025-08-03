package tw.com.tymbackend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * 測試配置類
 * 
 * 提供測試環境所需的配置，包括內存數據庫等。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@TestConfiguration
public class TestConfig {

    /**
     * 配置測試用數據源
     * 
     * 使用 H2 內存數據庫，避免測試時影響實際數據庫。
     * 
     * @return 測試用數據源
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .addScript("classpath:data.sql")
            .build();
    }
} 