package tw.com.tymbackend.core.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA 配置類別，用於多資料來源配置
 * 
 * <p>此類別負責配置 JPA 相關的設定，包括：</p>
 * <ul>
 *   <li>啟用事務管理</li>
 *   <li>配置 JPA 倉儲掃描路徑</li>
 *   <li>設定實體管理器工廠和事務管理器</li>
 * </ul>
 * 
 * <p>主要用於非 people/weapon 模組的資料來源配置。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
// @Configuration
// @EnableTransactionManagement
// @EnableJpaRepositories(
//     basePackages = {
//         "tw.com.tymbackend.module.livestock.dao",
//         "tw.com.tymbackend.module.ckeditor.dao",
//         "tw.com.tymbackend.module.gallery.dao"
//     },
//     entityManagerFactoryRef = "primaryEntityManagerFactory",
//     transactionManagerRef = "primaryTransactionManager"
// )
public class JpaConfig {
    // Primary datasource configuration for non-people/weapon modules
    // 此配置已被 RepositoryConfig 取代，避免重複定義
} 