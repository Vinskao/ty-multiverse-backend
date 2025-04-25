package tw.com.tymbackend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;

/**
 * Configuration class for test environment.
 * 
 * This class provides test-specific configuration beans that override the default
 * application configuration during testing. It's specifically designed to be used
 * with Spring Boot test contexts.
 * 
 * The primary purpose of this configuration is to provide an in-memory H2 database
 * for testing, which ensures that tests run in isolation without affecting any
 * external database.
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * Creates and configures an in-memory H2 database for testing purposes.
     * 
     * This bean is marked as @Primary to ensure it takes precedence over any other
     * DataSource beans that might be defined in the application context during testing.
     * 
     * @return A configured DataSource instance pointing to an in-memory H2 database
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