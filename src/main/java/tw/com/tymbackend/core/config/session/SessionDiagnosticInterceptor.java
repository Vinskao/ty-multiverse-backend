package tw.com.tymbackend.core.config.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Session 診斷攔截器
 * 
 * 用於追蹤 Session 的生命週期，診斷 Session 無效問題
 * 特別關注 K8s health check 和 proxy 相關的 Session 行為
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
@ConditionalOnProperty(name = "app.session.enabled", havingValue = "true")
public class SessionDiagnosticInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionDiagnosticInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");
        
        // 跳過靜態資源和健康檢查
        if (isHealthCheck(requestURI) || isStaticResource(requestURI)) {
            return true;
        }
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                logger.info("🔍 Session 診斷 - URI: {}, SessionID: {}, isNew: {}, UserAgent: {}", 
                    requestURI, 
                    session.getId(), 
                    session.isNew(),
                    userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "N/A");
                
                // 檢查 Session 是否有效
                try {
                    session.getAttribute("_diagnostic_check");
                } catch (IllegalStateException e) {
                    if (e.getMessage() != null && e.getMessage().contains("Session was invalidated")) {
                        logger.warn("⚠️ 檢測到無效 Session - URI: {}, SessionID: {}", requestURI, session.getId());
                    }
                }
            } else {
                logger.info("🔍 Session 診斷 - URI: {}, 無 Session, UserAgent: {}", 
                    requestURI, 
                    userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "N/A");
            }
            
        } catch (Exception e) {
            logger.error("❌ Session 診斷異常 - URI: {}, Error: {}", requestURI, e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        
        // 跳過靜態資源和健康檢查
        if (isHealthCheck(requestURI) || isStaticResource(requestURI)) {
            return;
        }
        
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                logger.info("✅ 請求完成 - URI: {}, SessionID: {}, Status: {}", 
                    requestURI, session.getId(), response.getStatus());
            }
        } catch (Exception e) {
            logger.error("❌ 請求完成時 Session 異常 - URI: {}, Error: {}", requestURI, e.getMessage());
        }
    }
    
    /**
     * 判斷是否為健康檢查請求
     */
    private boolean isHealthCheck(String uri) {
        return uri.contains("/actuator/health") || 
               uri.contains("/actuator/liveness") || 
               uri.contains("/actuator/readiness") ||
               uri.equals("/") ||
               uri.equals("/tymb") ||
               uri.equals("/tymb/");
    }
    
    /**
     * 判斷是否為靜態資源
     */
    private boolean isStaticResource(String uri) {
        return uri.contains("/static/") || 
               uri.contains("/favicon.ico") ||
               uri.contains("/swagger-ui") ||
               uri.contains("/v3/api-docs");
    }
}
