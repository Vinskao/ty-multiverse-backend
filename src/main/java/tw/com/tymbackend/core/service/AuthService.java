package tw.com.tymbackend.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 認證服務
 *
 * 提供完整的認證業務邏輯，包括 Token 驗證、刷新、用戶信息管理等功能。
 * 從 Keycloak Controller 中提取業務邏輯，避免 Controller 間直接調用。
 */
@Service
public class AuthService extends BaseService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.auth-server-url}")
    private String ssoUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.clientId}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    /**
     * 驗證 Access Token 的有效性，並在必要時使用 Refresh Token 進行續期
     *
     * @param token Access Token
     * @param refreshToken Refresh Token (可選)
     * @return 包含驗證結果和可能的新 Token
     */
    public Map<String, Object> introspectToken(String token, String refreshToken) {
        String introspectUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
        String tokenUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        Map<String, Object> result = new HashMap<>();

        try {
            // Step 1: 驗證 Access Token 是否仍有效
            MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();
            bodyParams.add("client_id", clientId);
            bodyParams.add("client_secret", clientSecret);
            bodyParams.add("token", token);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(bodyParams, headers);

            ResponseEntity<Map<String, Object>> introspectResponse = restTemplate.exchange(
                    introspectUrl,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> introspectResult = introspectResponse.getBody();
            if (introspectResult == null) {
                result.put("error", "TOKEN_INTROSPECT_FAILED");
                result.put("status", HttpStatus.UNAUTHORIZED);
                return result;
            }

            // Step 2: 如果 Token 還有效，直接返回
            if (Boolean.TRUE.equals(introspectResult.get("active"))) {
                result.put("valid", true);
                result.put("data", introspectResult);
                result.put("status", HttpStatus.OK);
                return result;
            }

            // Step 3: Token 無效，嘗試用 Refresh Token 取得新 Token
            if (refreshToken != null && !refreshToken.isEmpty()) {
                Map<String, Object> refreshResult = refreshToken(refreshToken);
                result.putAll(refreshResult);
                return result;
            }

            // Step 4: 無法刷新，返回未授權
            result.put("valid", false);
            result.put("error", "TOKEN_INVALID_OR_REFRESH_FAILED");
            result.put("status", HttpStatus.UNAUTHORIZED);
            return result;

        } catch (Exception e) {
            log.error("Token 內省失敗", e);
            result.put("error", "TOKEN_CHECK_FAILED");
            result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
            return result;
        }
    }

    /**
     * 使用 Refresh Token 獲取新的 Access Token
     *
     * @param refreshToken Refresh Token
     * @return 包含新 Token 的結果
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        String tokenUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        Map<String, Object> result = new HashMap<>();

        try {
            MultiValueMap<String, String> refreshParams = new LinkedMultiValueMap<>();
            refreshParams.add("grant_type", "refresh_token");
            refreshParams.add("client_id", clientId);
            refreshParams.add("client_secret", clientSecret);
            refreshParams.add("refresh_token", refreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, String>> refreshEntity = new HttpEntity<>(refreshParams, headers);

            ResponseEntity<Map<String, Object>> refreshResponse = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    refreshEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> refreshResult = refreshResponse.getBody();
            if (refreshResult == null) {
                result.put("error", "TOKEN_REFRESH_FAILED");
                result.put("status", HttpStatus.UNAUTHORIZED);
                return result;
            }

            if (refreshResult.get("access_token") != null) {
                result.put("valid", true);
                result.put("refreshed", true);
                result.put("data", refreshResult);
                result.put("status", HttpStatus.OK);
                return result;
            }

        } catch (Exception e) {
            log.error("Token 刷新失敗", e);
        }

        result.put("valid", false);
        result.put("error", "TOKEN_REFRESH_FAILED");
        result.put("status", HttpStatus.UNAUTHORIZED);
        return result;
    }

    /**
     * 執行登出操作，撤銷 Refresh Token
     *
     * @param refreshToken Refresh Token
     * @return 登出結果
     */
    public Map<String, Object> logout(String refreshToken) {
        String logoutUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
        Map<String, Object> result = new HashMap<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(logoutUrl, HttpMethod.POST, entity, String.class);

            result.put("success", true);
            result.put("message", "LOGOUT_SUCCESS");
            result.put("status", HttpStatus.OK);
            return result;

        } catch (Exception e) {
            log.error("登出失敗", e);
            result.put("success", false);
            result.put("error", "LOGOUT_FAILED");
            result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
            return result;
        }
    }

    /**
     * 獲取用戶信息
     *
     * @param accessToken Access Token
     * @return 用戶信息
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
        Map<String, Object> result = new HashMap<>();

        try {
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);

            ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    userEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> userInfo = userResponse.getBody();
            if (userInfo == null) {
                result.put("error", "USER_INFO_FAILED");
                result.put("status", HttpStatus.UNAUTHORIZED);
                return result;
            }

            result.put("user_info", userInfo);
            result.put("status", HttpStatus.OK);
            return result;

        } catch (Exception e) {
            log.error("獲取用戶信息失敗", e);
            result.put("error", "USER_INFO_FAILED");
            result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
            return result;
        }
    }

    /**
     * 執行完整的認證測試
     *
     * @param token Access Token
     * @param refreshToken Refresh Token
     * @return 完整的認證測試結果
     */
    public Map<String, Object> performAuthTest(String token, String refreshToken) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 基本 Token 檢查
            result.put("token_provided", token != null);
            result.put("refresh_token_provided", refreshToken != null);

            if (token == null) {
                result.put("error", "NO_TOKEN");
                result.put("status", HttpStatus.BAD_REQUEST);
                return result;
            }

            // 2. Token 內省
            Map<String, Object> introspectResult = introspectToken(token, refreshToken);
            result.put("introspect_result", introspectResult);

            // 3. 如果 Token 有效，獲取用戶信息
            if (Boolean.TRUE.equals(introspectResult.get("valid"))) {
                Map<String, Object> userInfoResult = getUserInfo(token);
                result.put("user_info_result", userInfoResult);

                // 4. 提取 JWT 詳細信息 (如果需要的話)
                result.put("token_length", token.length());
                result.put("token_prefix", token.substring(0, Math.min(20, token.length())));

                if (Boolean.TRUE.equals(introspectResult.get("refreshed"))) {
                    result.put("token_refreshed", true);
                }
            }

            result.put("test_completed", true);
            result.put("status", introspectResult.get("status"));

        } catch (Exception e) {
            log.error("認證測試失敗", e);
            result.put("error", "AUTH_TEST_FAILED");
            result.put("status", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return result;
    }

    /**
     * 執行登出測試
     *
     * @param refreshToken Refresh Token
     * @return 登出測試結果
     */
    public Map<String, Object> performLogoutTest(String refreshToken) {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("logout_initiated", true);

            Map<String, Object> logoutResult = logout(refreshToken);
            result.put("keycloak_logout_result", logoutResult);

            if (Boolean.TRUE.equals(logoutResult.get("success"))) {
                result.put("logout_successful", true);
                result.put("security_context_should_clear", true);
            }

        } catch (Exception e) {
            log.error("登出測試失敗", e);
            result.put("error", "LOGOUT_TEST_FAILED");
            result.put("logout_successful", false);
        }

        return result;
    }

    /**
     * 執行認證系統健康檢查
     *
     * @return 健康檢查結果
     */
    public Map<String, Object> performHealthCheck() {
        Map<String, Object> result = new HashMap<>();

        result.put("timestamp", System.currentTimeMillis());
        result.put("service_available", true);

        // 檢查 Keycloak 配置
        try {
            result.put("keycloak_config", Map.of(
                "sso_url", ssoUrl != null,
                "realm", realm != null,
                "client_id", clientId != null,
                "client_secret", clientSecret != null
            ));
            result.put("keycloak_status", "CONFIGURED");
        } catch (Exception e) {
            result.put("keycloak_status", "ERROR");
            result.put("keycloak_error", e.getMessage());
        }

        // 檢查 RestTemplate
        try {
            result.put("rest_template_available", restTemplate != null);
        } catch (Exception e) {
            result.put("rest_template_available", false);
            result.put("rest_template_error", e.getMessage());
        }

        return result;
    }
}

