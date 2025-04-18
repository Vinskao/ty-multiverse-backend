package tw.com.tymbackend.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 配置類，用於啟用 JPA 倉儲
 * 是一種IOC容器，用於管理Spring容器中的Bean
 */
@Configuration
@ComponentScan(basePackages = {
    "tw.com.tymbackend.module.people.dao",
    "tw.com.tymbackend.module.weapon.dao",
    "tw.com.tymbackend.module.livestock.dao",
    "tw.com.tymbackend.module.ckeditor.dao",
    "tw.com.tymbackend.module.gallery.dao",
})
public class RepositoryConfig {
    // No additional configuration needed
} 