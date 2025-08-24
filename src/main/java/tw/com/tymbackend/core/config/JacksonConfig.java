package tw.com.tymbackend.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson 配置類
 * 
 * 配置 ObjectMapper 以支援 Java 8 時間類型和其他序列化需求
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置 ObjectMapper Bean
     * 
     * 啟用 JSR310 模組以支援 Java 8 時間類型（如 LocalDateTime）
     * 
     * @return 配置好的 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 註冊 JavaTimeModule 以支援 Java 8 時間類型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 配置字符編碼
        objectMapper.configure(com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        
        return objectMapper;
    }
}
