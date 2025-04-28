package tw.com.tymbackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import tw.com.tymbackend.core.repository.IntegerPkRepositoryImpl;
import tw.com.tymbackend.core.repository.StringPkRepositoryImpl;

/**
 * 配置類，用於啟用 JPA 倉儲
 * 是一種IOC容器，用於管理Spring容器中的Bean
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "tw.com.tymbackend.module",
    repositoryImplementationPostfix = "Impl",
    repositoryBaseClass = IntegerPkRepositoryImpl.class
)
public class RepositoryConfig {
    // Spring Data JPA will automatically create repository implementations
    // using our custom base classes
} 