package tw.com.tymbackend.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("127.0.0.1");
        config.setPort(6379); // Redis的預設埠號
        config.setPassword(""); // 放Redis的密碼，這裡暫時沒有設
        config.setDatabase(0);
        
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxWaitMillis(3000); // 當連線取完時，欲取得連線的最大的等待時間
        poolConfig.setMaxIdle(8); // 最大空閒連線數
        poolConfig.setMinIdle(4); // 最小空閒連線數
        poolConfig.setMaxTotal(3000); // 最大連線數
        
        LettucePoolingClientConfiguration poolingClientConfig = 
            LettucePoolingClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(3000))
            .poolConfig(poolConfig)
            .build();

        return new LettuceConnectionFactory(config, poolingClientConfig);
    }
}
