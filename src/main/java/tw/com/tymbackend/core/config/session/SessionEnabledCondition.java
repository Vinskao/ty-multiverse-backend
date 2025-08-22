package tw.com.tymbackend.core.config.session;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Session 啟用條件類
 * 
 * 根據配置決定是否啟用 Spring Session Redis 功能
 * 預設為禁用狀態，可通過環境變數或配置檔案啟用
 */
public class SessionEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 檢查 app.session.enabled 配置
        String sessionEnabled = context.getEnvironment()
                .getProperty("app.session.enabled", "false");
        
        return Boolean.parseBoolean(sessionEnabled);
    }
}
