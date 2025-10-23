package tw.com.tymbackend.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import tw.com.tymbackend.core.service.AuthService;

import java.util.HashMap;
import java.util.Map;

/**
 * 認證控制器
 *
 * 提供不同權限等級的測試端點，用於驗證 Spring Security 和 JWT 認證功能。
 * 整合 AuthService 提供完整的認證測試和驗證功能。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * 管理員端點 - 需要 manage-users 角色
     *
     * @return 包含用戶資訊和權限的響應
     */
    @PreAuthorize("hasRole('manage-users')")
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("管理員端點被用戶訪問: {}", username);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "你好 " + username + "！你擁有 manage-users 角色。");
        response.put("user", username);
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * 用戶端點 - 需要登入（包括 GUEST）
     *
     * @return 包含用戶資訊的響應
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("用戶端點被用戶訪問: {}", username);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "你好 " + username + "！你已通過認證。");
        response.put("user", username);
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * 公開端點 - 不需要認證
     *
     * @return 公開資訊
     */
    @GetMapping("/visitor")
    public ResponseEntity<Map<String, Object>> visitorEndpoint() {
        log.info("訪客端點被訪問");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "這是公開資訊。");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * 測試端點 - 沒有權限註解，預設需要認證
     *
     * @return 包含認證狀態的響應
     */
    @GetMapping("/test-default")
    public ResponseEntity<Map<String, Object>> testDefaultAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("測試預設端點被用戶訪問: {}", username);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "此端點沒有 @PreAuthorize 註解");
        response.put("user", username);
        response.put("authenticated", authentication.isAuthenticated());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * JWT Token 信息端點 - 用於調試
     *
     * @return 包含 JWT Token 詳細資訊的響應
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> tokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", authentication.isAuthenticated());
        response.put("name", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", System.currentTimeMillis());

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
            claims.put("name", jwt.getClaimAsString("name"));
            claims.put("given_name", jwt.getClaimAsString("given_name"));
            claims.put("family_name", jwt.getClaimAsString("family_name"));
            response.put("claims", claims);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 完整的認證測試 - 整合 AuthService 功能
     *
     * @param refreshToken 可選的刷新令牌
     * @return 包含完整認證測試結果的響應
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> authTest(
            @RequestParam(value = "refreshToken", required = false) String refreshToken) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentToken = null;

        // 從 JWT 中提取當前 token
        if (authentication.getCredentials() instanceof String) {
            currentToken = (String) authentication.getCredentials();
        }

        log.info("執行完整認證測試，用戶: {}", authentication.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("current_user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("current_token_available", currentToken != null);
        response.put("test_initiated", System.currentTimeMillis());

        // 調用 AuthService 進行完整測試
        if (currentToken != null) {
            try {
                Map<String, Object> authResult = authService.performAuthTest(currentToken, refreshToken);
                response.putAll(authResult);

            } catch (Exception e) {
                log.error("認證測試失敗", e);
                response.put("error", "AUTH_TEST_FAILED");
                response.put("error_message", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        response.put("test_completed", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 登出測試 - 測試 AuthService 登出功能
     *
     * @param refreshToken 刷新令牌
     * @return 登出測試結果
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout-test")
    public ResponseEntity<Map<String, Object>> logoutTest(@RequestParam("refreshToken") String refreshToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("執行登出測試，用戶: {}", authentication.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("user", authentication.getName());
        response.put("logout_initiated", System.currentTimeMillis());

        try {
            Map<String, Object> logoutResult = authService.performLogoutTest(refreshToken);
            response.putAll(logoutResult);

            // 登出成功後清除 Spring Security 上下文
            if (Boolean.TRUE.equals(logoutResult.get("logout_successful"))) {
                SecurityContextHolder.clearContext();
                response.put("security_context_cleared", true);
            }

        } catch (Exception e) {
            log.error("登出測試失敗", e);
            response.put("error", "LOGOUT_TEST_FAILED");
            response.put("error_message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        response.put("test_completed", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 認證健康檢查 - 測試認證系統的整體健康狀態
     *
     * @return 認證系統健康檢查結果
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> authHealthCheck() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("執行認證健康檢查");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("authentication_exists", authentication != null);
        response.put("authenticated", authentication != null && authentication.isAuthenticated());

        if (authentication != null) {
            response.put("principal_type", authentication.getPrincipal().getClass().getSimpleName());
            response.put("authorities_count", authentication.getAuthorities().size());
            response.put("name", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
        }

        // 調用 AuthService 進行健康檢查
        try {
            Map<String, Object> healthResult = authService.performHealthCheck();
            response.putAll(healthResult);
        } catch (Exception e) {
            response.put("service_error", "HEALTH_CHECK_FAILED");
            response.put("service_error_message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
