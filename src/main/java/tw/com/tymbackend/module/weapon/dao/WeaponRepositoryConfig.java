package tw.com.tymbackend.module.weapon.dao;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 專門配置 weapon 模組的 repository
 * 使用默認的 JpaRepository 實現，不使用自定義的 IntegerPkRepositoryImpl
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "tw.com.tymbackend.module.weapon.dao",
    entityManagerFactoryRef = "peopleEntityManagerFactory",
    transactionManagerRef = "peopleTransactionManager"
)
public class WeaponRepositoryConfig {
    // 使用默認的 JpaRepository 實現
} 