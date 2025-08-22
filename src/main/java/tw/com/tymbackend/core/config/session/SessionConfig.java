package tw.com.tymbackend.core.config.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Conditional;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Spring Session Redis 配置類
 * 
 * 支援 Redis Session 儲存，提供有狀態服務的 Session 管理
 * 使用 `tymb:sessions` 命名空間儲存 Session 數據
 * 
 * 配置特點：
 * - 啟用 Redis HTTP Session 支援
 * - 配置 Session 超時時間
 * - 自定義 Cookie 序列化器
 * - 支援 Session 固定攻擊防護
 */
@Configuration
@EnableRedisHttpSession(
    redisNamespace = "tymb:sessions",
    maxInactiveIntervalInSeconds = 1800  // 30分鐘
)
@Conditional(SessionEnabledCondition.class)
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {

    @Value("${app.session.timeout:1800}")
    private int sessionTimeout;

    @Value("${app.session.cookie.name:JSESSIONID}")
    private String cookieName;

    @Value("${app.session.cookie.path:/}")
    private String cookiePath;

    @Value("${app.session.cookie.http-only:true}")
    private boolean httpOnly;

    @Value("${app.session.cookie.secure:false}")
    private boolean secure;

    /**
     * 配置 Cookie 序列化器
     * 
     * @return 自定義的 Cookie 序列化器
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(cookieName);
        serializer.setCookiePath(cookiePath);
        serializer.setUseSecureCookie(secure);
        serializer.setUseHttpOnlyCookie(httpOnly);
        serializer.setCookieMaxAge(sessionTimeout);
        return serializer;
    }
}
