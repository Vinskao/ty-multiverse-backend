package tw.com.tymbackend.module.people.dao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import tw.com.tymbackend.core.repository.StringPkRepositoryImpl;

/**
 * Configuration for People repositories (已禁用)
 * 使用條件註解禁用此配置，保留配置備用
 */
@Configuration
@ConditionalOnProperty(name = "spring.people-datasource.enabled", havingValue = "true", matchIfMissing = false)
@EnableJpaRepositories(
    basePackages = "tw.com.tymbackend.module.people.dao",
    entityManagerFactoryRef = "peopleEntityManagerFactory",
    transactionManagerRef = "peopleTransactionManager",
    repositoryImplementationPostfix = "Impl",
    repositoryBaseClass = StringPkRepositoryImpl.class
)
public class PeopleRepositoryConfig {
    // Spring Data JPA will automatically create repository implementations
    // using our custom base classes for people datasource (已禁用)
}
