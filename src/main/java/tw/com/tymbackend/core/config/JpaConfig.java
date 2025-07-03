package tw.com.tymbackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration class for multiple datasources
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {
        "tw.com.tymbackend.module.livestock.dao",
        "tw.com.tymbackend.module.ckeditor.dao",
        "tw.com.tymbackend.module.gallery.dao"
    },
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class JpaConfig {
    // Primary datasource configuration for non-people/weapon modules
} 