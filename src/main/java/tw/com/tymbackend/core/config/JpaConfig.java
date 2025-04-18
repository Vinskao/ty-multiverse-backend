package tw.com.tymbackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration class
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "tw.com.tymbackend.module.people.dao",
    "tw.com.tymbackend.module.weapon.dao",
    "tw.com.tymbackend.module.livestock.dao",
    "tw.com.tymbackend.module.ckeditor.dao",
    "tw.com.tymbackend.module.gallery.dao"
})
public class JpaConfig {
    // No additional configuration needed
} 