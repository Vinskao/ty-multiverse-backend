package tw.com.tymbackend.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis配置類
 * 此類負責配置Redis連接工廠及連接池相關設定
 */
@Configuration  // 標記此類為Spring配置類
public class RedisConfig {

    /**
     * 創建Lettuce Redis連接工廠的Bean
     * Lettuce是一個基於Netty的Redis客戶端，支持異步操作
     *
     * @return LettuceConnectionFactory Redis連接工廠實例
     */
    @Bean  // 標記此方法將產生一個Spring管理的Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        // 配置Redis獨立模式的連接信息
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("127.0.0.1");  // 設置Redis服務器地址
        config.setPort(6379);             // 設置Redis服務器端口
        config.setPassword("");           // 設置Redis密碼（此處為空）
        config.setDatabase(0);            // 設置使用的數據庫編號

        // 配置連接池參數
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxWaitMillis(3000);  // 設置獲取連接時的最大等待時間（毫秒）
        poolConfig.setMaxIdle(8);           // 設置最大空閒連接數
        poolConfig.setMinIdle(4);           // 設置最小空閒連接數
        poolConfig.setMaxTotal(3000);       // 設置最大連接數
        
        // 創建Lettuce客戶端池配置
        LettucePoolingClientConfiguration poolingClientConfig = 
            LettucePoolingClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(3000))  // 設置Redis命令執行超時時間
            .poolConfig(poolConfig)                   // 設置連接池配置
            .build();

        // 返回配置完成的連接工廠實例
        return new LettuceConnectionFactory(config, poolingClientConfig);
    }

    @Bean
    RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
            LettuceConnectionFactory lettuceConnectionFactory,
            RedisMessageSubscriber redisMessageSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactory);
        container.addMessageListener(redisMessageSubscriber, new ChannelTopic("metrics-channel"));
        return container;
    }
}
