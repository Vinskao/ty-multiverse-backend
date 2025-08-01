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

@RestController
@RequestMapping("/guardian")
public class Guardian {

    private static final Logger log = LoggerFactory.getLogger(Guardian.class);

    /**
     * 管理員端點 - 需要 manage-users 角色
     */
    @PreAuthorize("hasRole('manage-users')")
    @GetMapping("/admin")
    public Map<String, Object> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Admin endpoint accessed by user: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello " + username + "! You have manage-users role.");
        response.put("user", username);
        response.put("authorities", authentication.getAuthorities());
        
        return response;
    }

    /**
     * 用戶端點 - 需要登入（包括 GUEST）
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public Map<String, Object> userEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("User endpoint accessed by user: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello " + username + "! You are authenticated.");
        response.put("user", username);
        response.put("authorities", authentication.getAuthorities());
        
        return response;
    }

    /**
     * 公開端點 - 不需要認證
     */
    @GetMapping("/visitor")
    public Map<String, Object> visitorEndpoint() {
        log.info("Visitor endpoint accessed");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is public information.");
        
        return response;
    }

    /**
     * 測試端點 - 沒有權限註解，預設需要認證
     */
    @GetMapping("/test-default")
    public Map<String, Object> testDefaultAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Test default endpoint accessed by user: {}", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This endpoint has no @PreAuthorize annotation");
        response.put("user", username);
        response.put("authenticated", authentication.isAuthenticated());
        response.put("authorities", authentication.getAuthorities());
        
        return response;
    }

    /**
     * JWT Token 信息端點 - 用於調試
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
