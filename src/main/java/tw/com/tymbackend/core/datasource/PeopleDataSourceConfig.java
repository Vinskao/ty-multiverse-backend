package tw.com.tymbackend.core.datasource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * People DataSource Configuration (已禁用)
 * 使用條件註解禁用此配置，保留配置備用
 */
@Configuration
@ConditionalOnProperty(name = "spring.people-datasource.enabled", havingValue = "true")
public class PeopleDataSourceConfig {

    private final PeopleDataSourceProperties properties;

    public PeopleDataSourceConfig(PeopleDataSourceProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "peopleDataSource", destroyMethod = "close")
    public DataSource peopleDataSource() {
        return DataSourceBuilder.create()
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }

    @Bean(name = "peopleEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean peopleEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(peopleDataSource());
        em.setPackagesToScan(
            "tw.com.tymbackend.module.people.domain.vo",
            "tw.com.tymbackend.module.weapon.domain.vo"
        );
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.connection.release_mode", "on_close");
        properties.setProperty("hibernate.current_session_context_class", "thread");
        properties.setProperty("hibernate.jdbc.batch_size", "50");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "false");
        properties.setProperty("hibernate.connection.handling_mode", "DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION");
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        properties.setProperty("hibernate.generate_statistics", "false");
        em.setJpaProperties(properties);
        
        return em;
    }

    @Bean(name = "peopleTransactionManager")
    public PlatformTransactionManager peopleTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(peopleEntityManagerFactory().getObject());
        transactionManager.setNestedTransactionAllowed(true);
        transactionManager.setDefaultTimeout(30);
        return transactionManager;
    }
}
