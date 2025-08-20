package tw.com.tymbackend.core.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import tw.com.tymbackend.core.repository.StringPkRepositoryImpl;

/**
 * 配置類，用於啟用 String 類型主鍵的 JPA 倉儲
 * 是一種IOC容器，用於管理Spring容器中的Bean
 * 所有模組都使用主數據源
 */
@Configuration
@EnableJpaRepositories(
    basePackages = {
        "tw.com.tymbackend.module.people.dao",
        "tw.com.tymbackend.module.weapon.dao"
    },
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager",
    repositoryImplementationPostfix = "Impl",
    repositoryBaseClass = StringPkRepositoryImpl.class
)
public class StringRepositoryConfig {
    // Spring Data JPA will automatically create repository implementations
    // using StringPkRepositoryImpl for String primary key entities
}
