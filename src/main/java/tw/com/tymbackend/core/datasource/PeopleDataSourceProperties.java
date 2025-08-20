package tw.com.tymbackend.core.datasource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * People DataSource Properties (已禁用)
 * 使用條件註解禁用此配置，保留配置備用
 */
@Component
@ConfigurationProperties(prefix = "spring.people-datasource")
@ConditionalOnProperty(name = "spring.people-datasource.enabled", havingValue = "true")
public class PeopleDataSourceProperties {
    
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private boolean enabled = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
