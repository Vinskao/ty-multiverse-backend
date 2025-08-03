package tw.com.tymbackend.module.people.dao;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import tw.com.tymbackend.core.repository.StringPkRepositoryImpl;

/**
 * Configuration for People and Weapon repositories
 * Uses the people datasource
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "tw.com.tymbackend.module.people.dao",
    entityManagerFactoryRef = "peopleEntityManagerFactory",
    transactionManagerRef = "peopleTransactionManager",
    repositoryImplementationPostfix = "Impl",
    repositoryBaseClass = StringPkRepositoryImpl.class
)
public class PeopleRepositoryConfig {
    // Spring Data JPA will automatically create repository implementations
    // using our custom base classes for people datasource
} 