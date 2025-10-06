package tw.com.tymbackend.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 守護者控制器
 * 
 * 提供不同權限等級的測試端點，用於驗證 Spring Security 和 JWT 認證功能。
 */
@RestController
@RequestMapping("/guardian")
public class Guardian {

    private static final Logger log = LoggerFactory.getLogger(Guardian.class);

    /**
     * 管理員端點 - 需要 manage-users 角色
     * 
     * @return 包含用戶資訊和權限的響應
     */
    @PreAuthorize("hasRole('manage-users')")
    @GetMapping("/admin")
    public Map<String, Object> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("管理員端點被用戶訪問: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "你好 " + username + "！你擁有 manage-users 角色。");
        response.put("user", username);
        response.put("authorities", authentication.getAuthorities());
        
        return response;
    }

    /**
     * 用戶端點 - 需要登入（包括 GUEST）
     * 
     * @return 包含用戶資訊的響應
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public Map<String, Object> userEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("用戶端點被用戶訪問: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "你好 " + username + "！你已通過認證。");
        response.put("user", username);
        response.put("authorities", authentication.getAuthorities());
        
        return response;
    }

    /**
     * 公開端點 - 不需要認證
     * 
     * @return 公開資訊
     */
    @GetMapping("/visitor")
    public Map<String, Object> visitorEndpoint() {
        log.info("訪客端點被訪問");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "這是公開資訊。");
        
        return response;
    }

    /**
     * 測試端點 - 沒有權限註解，預設需要認證
     * 
     * @return 包含認證狀態的響應
     */
    @GetMapping("/test-default")
    public Map<String, Object> testDefaultAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("測試預設端點被用戶訪問: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "此端點沒有 @PreAuthorize 註解");
        response.put("user", username);
        response.put("authenticated", authentication.isAuthenticated());
        response.put("authorities", authentication.getAuthorities());
        
        return response;
    }

    /**
     * JWT Token 信息端點 - 用於調試
     * 
     * @return 包含 JWT Token 詳細資訊的響應
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/token-info")
    public Map<String, Object> tokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", authentication.isAuthenticated());
        response.put("name", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            response.put("token_type", "JWT");
            response.put("issued_at", jwt.getIssuedAt());
            response.put("expires_at", jwt.getExpiresAt());
            response.put("issuer", jwt.getIssuer());
            response.put("audience", jwt.getAudience());
            response.put("subject", jwt.getSubject());
            
            // 添加JWT claims信息
            Map<String, Object> claims = new HashMap<>();
            claims.put("realm_access", jwt.getClaimAsMap("realm_access"));
            claims.put("resource_access", jwt.getClaimAsMap("resource_access"));
            claims.put("preferred_username", jwt.getClaimAsString("preferred_username"));
            claims.put("email", jwt.getClaimAsString("email"));
            response.put("claims", claims);
        }
        
        return response;
    }
}
