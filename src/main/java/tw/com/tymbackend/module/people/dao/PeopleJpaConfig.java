package tw.com.tymbackend.module.people.dao;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration class for People module
 * Uses people datasource specifically
 * 
 * This configuration ensures that all repositories in the people module
 * use the dedicated people datasource instead of the primary datasource.
 */
@Configuration
@EnableTransactionManagement
public class PeopleJpaConfig {
    // Repository configuration is handled by PeopleRepositoryConfig
    // Transaction management is enabled for people datasource
} 