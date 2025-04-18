package tw.com.tymbackend.module.weapon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration class for the Weapon module
 */
@Configuration
@EnableJpaRepositories(basePackages = "tw.com.tymbackend.module.weapon.dao")
public class WeaponConfig {
    // No additional configuration needed
} 