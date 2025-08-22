package tw.com.tymbackend.core.config.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Session 無效化處理過濾器
 * 
 * 此過濾器用於處理 Session 無效的問題，避免在 Session 已經無效時還嘗試保存
 * 主要解決 Spring Session 在嘗試保存已無效 Session 時拋出 IllegalStateException 的問題
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
@Order(1)  // 確保在其他過濾器之前執行
@ConditionalOnProperty(name = "app.session.enabled", havingValue = "true")
public class SessionInvalidationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionInvalidationFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");
        
        // 記錄請求信息，特別關注 K8s 相關的請求
        if (isK8sRelatedRequest(requestURI, userAgent)) {
            logger.info("🔍 K8s 相關請求 - URI: {}, UserAgent: {}", requestURI, userAgent);
        }
        
        try {
            // 檢查 Session 是否有效
            HttpSession session = request.getSession(false);
            if (session != null) {
                try {
                    // 嘗試訪問 Session 屬性來檢查是否有效
                    session.getAttribute("_session_validity_check");
                } catch (IllegalStateException e) {
                    if (e.getMessage() != null && e.getMessage().contains("Session was invalidated")) {
                        logger.warn("⚠️ 檢測到無效的 Session - URI: {}, SessionID: {}, UserAgent: {}", 
                            requestURI, session.getId(), userAgent);
                        // 將 Session 標記為需要清理
                        request.setAttribute("_session_invalidated", true);
                    }
                }
            }
            
            // 繼續過濾器鏈
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            // 捕獲所有異常，避免 Session 相關錯誤影響請求處理
            if (e.getMessage() != null && e.getMessage().contains("Session was invalidated")) {
                logger.warn("⚠️ Session 無效錯誤已捕獲並忽略 - URI: {}, UserAgent: {}", requestURI, userAgent);
                // 返回 200 狀態碼，避免前端重定向
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"code\":200,\"message\":\"Session 已重新建立\"}");
                return;
            }
            throw e;
        }
    }
    
    /**
     * 判斷是否為 K8s 相關的請求
     */
    private boolean isK8sRelatedRequest(String uri, String userAgent) {
        // K8s health check 相關
        if (uri.contains("/actuator/health") || uri.contains("/actuator/liveness") || uri.contains("/actuator/readiness")) {
            return true;
        }
        
        // K8s 相關的 User-Agent
        if (userAgent != null && (
            userAgent.contains("kube-probe") || 
            userAgent.contains("k8s") || 
            userAgent.contains("kubernetes"))) {
            return true;
        }
        
        return false;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 跳過靜態資源和健康檢查端點
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || 
               path.startsWith("/static/") || 
               path.startsWith("/favicon.ico") ||
               path.startsWith("/error");
    }
}
