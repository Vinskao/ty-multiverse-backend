package tw.com.tymbackend.core.config.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Session 配置類 - 專門為有狀態服務提供 Session 支持
 * 
 * 此配置僅在有狀態服務需要時啟用，支持：
 * - CKEditor：編輯狀態、草稿、歷史記錄
 * - DeckOfCards：遊戲狀態、玩家手牌、遊戲進度
 * 
 * 使用條件化配置，可以通過配置開關控制是否啟用
 */
@Configuration
@ConditionalOnProperty(name = "app.session.enabled", havingValue = "true")
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 1800,  // 30分鐘過期，與 application.yml 保持一致
    redisNamespace = "tymb:sessions",     // 命名空間
    flushMode = org.springframework.session.FlushMode.ON_SAVE  // 只在保存時刷新
)
public class SessionConfig {

	/**
	 * 配置 Session Cookie 序列化器
	 * 
	 * 自定義 Session Cookie 的屬性，提高安全性和穩定性
	 * 
	 * @return 配置好的 CookieSerializer
	 */
	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName("TYMB-SESSION");  // 自定義 Cookie 名稱
		serializer.setCookiePath("/");             // Cookie 路徑
		serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");  // 域名模式
		serializer.setCookieMaxAge(3600);         // Cookie 最大年齡 (秒)
		serializer.setUseHttpOnlyCookie(true);    // 僅 HTTP 訪問
		serializer.setUseSecureCookie(false);     // 開發環境不使用 HTTPS
		serializer.setSameSite("Lax");            // 設置 SameSite 屬性
		return serializer;
	}


} 