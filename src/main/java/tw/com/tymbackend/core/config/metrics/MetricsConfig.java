package tw.com.tymbackend.core.config.metrics;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import javax.sql.DataSource;

/**
 * 度量配置類別
 * 
 * <p>此類別負責配置應用程式的度量監控功能，包括：</p>
 * <ul>
 *   <li>資料庫連接池度量監控</li>
 *   <li>應用程式性能指標收集</li>
 *   <li>系統資源使用情況追蹤</li>
 * </ul>
 * 
 * <p>主要用於整合 Micrometer 和 HikariCP 連接池的度量功能。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Configuration
public class MetricsConfig {

    /**
     * Micrometer 度量註冊表，用於收集和暴露度量數據
     */
    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 資料來源，用於獲取資料庫連接池資訊
     */
    @Autowired
    private DataSource dataSource;

    /**
     * 配置度量監控
     * 
     * <p>在應用程式啟動後自動配置 HikariCP 連接池的度量監控，
     * 將連接池的度量數據註冊到 Micrometer 中。</p>
     */
    @PostConstruct
    public void configureMetrics() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            hikariDataSource.setMetricRegistry(meterRegistry);
        }
    }
} 