package tw.com.tymbackend.core.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import tw.com.tymbackend.core.repository.IntegerPkRepositoryImpl;

/**
 * 配置類，用於啟用 JPA 倉儲
 * 是一種IOC容器，用於管理Spring容器中的Bean
 * 所有模組都使用主數據源
 */
@Configuration
@EnableJpaRepositories(
    basePackages = {
        "tw.com.tymbackend.module.ckeditor.dao",
        "tw.com.tymbackend.module.gallery.dao"
    },
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager",
    repositoryImplementationPostfix = "Impl",
    repositoryBaseClass = IntegerPkRepositoryImpl.class
)
public class RepositoryConfig {
    // Spring Data JPA will automatically create repository implementations
    // using our custom base classes for primary datasource
} 