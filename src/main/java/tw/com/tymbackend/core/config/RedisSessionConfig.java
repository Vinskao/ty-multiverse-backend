package tw.com.tymbackend.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Enables Spring Session backed by Redis. The Redis connection properties are
 * configured in application.yml and injected from environment-specific
 * properties files (local.properties / platform.properties).
 */
@Configuration
@EnableRedisHttpSession
public class RedisSessionConfig {
    // No additional code is required; the presence of this configuration class
    // together with the spring-session-data-redis dependency is enough to
    // switch Spring Session to Redis.
}
