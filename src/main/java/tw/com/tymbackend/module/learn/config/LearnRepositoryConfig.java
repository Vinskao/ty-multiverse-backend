package tw.com.tymbackend.module.learn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Keeps Learn's Long-ID repositories separate from the legacy Integer/String custom repository bases. */
@Configuration
@EnableJpaRepositories(
    basePackages = "tw.com.tymbackend.module.learn.dao",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class LearnRepositoryConfig {
}
