package tw.com.tymbackend.core.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Primary DataSource
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class PrimaryDataSourceProperties {
    
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    
    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
} 