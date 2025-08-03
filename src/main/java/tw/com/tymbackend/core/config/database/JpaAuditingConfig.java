package tw.com.tymbackend.core.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 審計配置類別
 * 
 * <p>此類別負責啟用 JPA 審計功能，使得實體類別可以自動追蹤：</p>
 * <ul>
 *   <li>實體的創建時間（@CreatedDate）</li>
 *   <li>實體的最後更新時間（@LastModifiedDate）</li>
 *   <li>實體的創建者（@CreatedBy）</li>
 *   <li>實體的最後更新者（@LastModifiedBy）</li>
 * </ul>
 * 
 * <p>這些審計功能對於追蹤資料變更和維護資料完整性非常重要。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
} 