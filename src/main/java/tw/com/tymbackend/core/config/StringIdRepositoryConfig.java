package tw.com.tymbackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.QueryLookupStrategy;

/**
 * Configuration class for String ID repositories.
 * This class enables the use of repositories that use String as their ID type.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "tw.com.tymbackend.core.repository",
    repositoryBaseClass = tw.com.tymbackend.core.repository.StringIdRepositoryImpl.class,
    queryLookupStrategy = QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND
)
public class StringIdRepositoryConfig {
    // Configuration for String ID repositories
} 