package tw.com.tymbackend.core.controller;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/keycloak")
public class KeycloakController {

    @Value("${url.frontend}")
    private String frontendUrl;

    private static final Logger log = LoggerFactory.getLogger(KeycloakController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.address}")
    private String backendUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.clientId}")
    private String clientId;
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    @Value("${keycloak.auth-server-url}")
    private String ssoUrl;

    @GetMapping("/redirect")
    public void keycloakRedirect(@RequestParam("code") String code, 
                                @RequestParam(value = "session_state", required = false) String sessionState,
                                @RequestParam(value = "iss", required = false) String issuer,
                                HttpServletResponse response)
            throws IOException {
        String redirectUri = backendUrl + "/keycloak/redirect";
        String tokenUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        try {
            log.info("Received authorization code: {}", code);
            log.info("Session state: {}", sessionState);
            log.info("Issuer: {}", issuer);

            // Token Request
            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            tokenParams.add("client_id", clientId);
            tokenParams.add("client_secret", clientSecret);
            tokenParams.add("code", code);
            tokenParams.add("grant_type", "authorization_code");
            tokenParams.add("redirect_uri", redirectUri);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(tokenParams, headers);

            log.info("Sending token request to: {}", tokenUrl);
            log.info("Token request params: {}", tokenParams);

            @SuppressWarnings({ "rawtypes" })
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);
            
            if (tokenResponse.getStatusCode() != HttpStatus.OK) {
                log.error("Token request failed with status: {}", tokenResponse.getStatusCode());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to obtain access token");
                return;
            }
            
            @SuppressWarnings("null")
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            @SuppressWarnings("null")
            String refreshToken = (String) tokenResponse.getBody().get("refresh_token");

            log.info("Access Token: {}", accessToken);
            log.info("Refresh Token: {}", refreshToken);

            if (accessToken == null || refreshToken == null) {
                log.error("Access token or refresh token is null");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to obtain access token");
                return;
            }

            // Request User Info
            String userInfoUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);

            log.info("Sending user info request to: {}", userInfoUrl);

            @SuppressWarnings({ "rawtypes" })
            ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userEntity,
                    Map.class);
            
            if (userResponse.getStatusCode() != HttpStatus.OK) {
                log.error("User info request failed with status: {}", userResponse.getStatusCode());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to obtain user info");
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = userResponse.getBody();

            log.info("User Info: {}", userInfo);

            @SuppressWarnings("null")
            String preferredUsername = (String) userInfo.get("preferred_username");
            if (preferredUsername == null) {
                log.error("Preferred username is null");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Failed to retrieve user info");
                return;
            }

            String email = (String) userInfo.get("email");

            response.setHeader("Set-Cookie", "refreshToken=; Path=/; Max-Age=0; SameSite=Lax");
            // 设置Cookie并重定向到前端
            response.addHeader("Set-Cookie", "authorizationCode=" + code + "; Path=/; SameSite=Lax");
            response.addHeader("Set-Cookie", "refreshToken=" + refreshToken + "; Path=/; SameSite=Lax");

            String redirectUrl = frontendUrl + "?username=" + preferredUsername 
                            + "&email=" + (email != null ? email : "")
                            + "&token=" + accessToken
                            + "&refreshToken=" + refreshToken;
            
            log.info("Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("Error processing OAuth redirect", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing OAuth redirect: " + e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam("refreshToken") String refreshToken) {
        String logoutUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        try {
            // 建立 HTTP headers，設定 Content-Type 為 x-www-form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            // 封裝登出所需參數（client_id, client_secret, refresh_token）到 MultiValueMap 中
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            // 封裝參數與 headers 到 HttpEntity 中
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            // 發送 POST 請求至 Keycloak 登出端點執行 token 撤銷
            restTemplate.exchange(logoutUrl, HttpMethod.POST, entity, String.class);
            
            // 記錄登出成功的日誌，包含 refresh token 資訊
            log.info("Logout successful for refresh token: {}", refreshToken);
            
            // 若成功則回傳 200 OK 與訊息
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            // 若發生錯誤，記錄錯誤訊息並回傳 500 錯誤碼與錯誤訊息
            log.error("Logout failed for refresh token: {}", refreshToken, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed");
        }
    }
    
    @CrossOrigin
    @GetMapping("/introspect")
    public ResponseEntity<?> introspectToken(
            @RequestParam("token") String token,
            @RequestParam(value = "refreshToken", required = false) String refreshToken) {
        
        String introspectUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
        String tokenUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    
        try {
            // 先嘗試檢查原始 token 是否有效
            MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();
            bodyParams.add("client_id", clientId);
            bodyParams.add("client_secret", clientSecret);
            bodyParams.add("token", token);
    
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(bodyParams, headers);
    
            ResponseEntity<Map> introspectResponse = restTemplate.exchange(introspectUrl, HttpMethod.POST, entity,
                    Map.class);
            Map<String, Object> introspectionResult = introspectResponse.getBody();
    
            // 如果 token 有效，直接返回結果
            if (introspectionResult != null && Boolean.TRUE.equals(introspectionResult.get("active"))) {
                return ResponseEntity.ok(introspectionResult);
            } 
            // 如果 token 無效且有 refreshToken，嘗試刷新 token
            else if (refreshToken != null && !refreshToken.isEmpty()) {
                log.info("Access token is invalid, attempting to refresh...");
                
                // 調用刷新 token 的邏輯
                MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
                tokenParams.add("client_id", clientId);
                tokenParams.add("client_secret", clientSecret);
                tokenParams.add("refresh_token", refreshToken);
                tokenParams.add("grant_type", "refresh_token");
                
                HttpHeaders refreshHeaders = new HttpHeaders();
                refreshHeaders.set("Content-Type", "application/x-www-form-urlencoded");
                HttpEntity<MultiValueMap<String, String>> refreshEntity = new HttpEntity<>(tokenParams, refreshHeaders);
    
                try {
                    ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, refreshEntity, Map.class);
                    
                    if (tokenResponse.getStatusCode().is2xxSuccessful()) {
                        // 成功刷新 token，返回新 token 信息
                        Map<String, Object> tokenInfo = tokenResponse.getBody();
                        tokenInfo.put("refreshed", true); // 添加標記表示這是刷新的結果
                        return ResponseEntity.ok(tokenInfo);
                    }
                } catch (Exception e) {
                    log.error("Error refreshing token", e);
                    // 刷新失敗，繼續走下面的失敗流程
                }
            }
            
            // 如果 token 無效且 refreshToken 也無效或未提供
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("active", false, "error", "Token is not active or invalid, and refresh failed.")
            );
        } catch (Exception e) {
            log.error("Error during token introspection/refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Error processing token.")
            );
        }
    }
}
