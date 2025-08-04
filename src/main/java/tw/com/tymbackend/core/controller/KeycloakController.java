package tw.com.tymbackend.core.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;
import tw.com.tymbackend.core.exception.ErrorCode;

/**
 * Keycloak 控制器
 * 
 * 負責處理與 Keycloak 的 OAuth2 認證流程，包括重定向處理、登出和 Token 驗證等功能。
 */
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

    /**
     * 處理從 Keycloak 認證後重導向回來的請求
     * 
     * 本方法使用授權碼向 Keycloak 取得存取憑證 (access token) 與更新憑證 (refresh token)，
     * 並呼叫 userinfo 端點以獲取使用者資訊。成功取得資料後，會將使用者名稱、電子郵件、
     * access token 以及 refresh token 附加至前端 URL 並進行重導向。
     *
     * @param code Keycloak 返回的授權碼
     * @param response HttpServletResponse 用於進行重導向
     * @throws IOException 當重導向失敗時會拋出此例外
     */
    @GetMapping("/redirect")
    public void keycloakRedirect(@RequestParam("code") String code, HttpServletResponse response)
            throws IOException {
        // 組合重導向用的 URI，此 URI 與 token 請求同時使用
        String redirectUri = backendUrl + "/keycloak/redirect";
        // 組合 token 請求 URL：Keycloak Token Endpoint
        String tokenUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        try {
            log.info("收到授權碼: {}", code);

            // 建立存放 token 請求參數的 MultiValueMap
            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            // 加入 clientId
            tokenParams.add("client_id", clientId);
            // 加入 clientSecret
            tokenParams.add("client_secret", clientSecret);
            // 加入從 Keycloak 傳回的授權碼
            tokenParams.add("code", code);
            // 指定授權類型為 authorization_code
            tokenParams.add("grant_type", "authorization_code");
            // 加入 redirect URI，必須與授權請求時一致
            tokenParams.add("redirect_uri", redirectUri);

            // 建立 HTTP headers，並設定 Content-Type 為 x-www-form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            // 封裝 token 請求的 body 與 headers 到 HttpEntity 中
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(tokenParams, headers);

            // 發送 POST 請求給 Keycloak 的 token endpoint
            ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                tokenUrl, 
                HttpMethod.POST, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> tokenBody = tokenResponse.getBody();
            if (tokenBody == null) {
                throw new RuntimeException("無法取得 token 響應");
            }
            // 從回傳內容中取得 access token
            String accessToken = (String) tokenBody.get("access_token");
            // 取得 refresh token
            String refreshToken = (String) tokenBody.get("refresh_token");

            log.info("Access Token: {}", accessToken);
            log.info("Refresh Token: {}", refreshToken);

            // 若其中任一 token 為 null，表示取得失敗，則拋出異常
            if (accessToken == null || refreshToken == null) {
                throw new RuntimeException("無法取得 access token");
            }

            // 呼叫 Keycloak userinfo endpoint 取得使用者資訊
            String userInfoUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
            // 設定 HTTP headers，加上 bearer token 授權
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            // 封裝 headers 至 HttpEntity（此處無需 body）
            HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);

            // 發送 GET 請求取得使用者資訊
            ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                userInfoUrl, 
                HttpMethod.GET, 
                userEntity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> userInfo = userResponse.getBody();

            log.info("使用者資訊: {}", userInfo);

            if (userInfo == null) {
                throw new RuntimeException("無法取得使用者資訊");
            }

            // 從使用者資訊中取得使用者名稱
            String preferredUsername = (String) userInfo.get("preferred_username");
            if (preferredUsername == null) {
                // 若使用者名稱不存在，則拋出異常
                throw new RuntimeException("無法取得使用者資訊");
            }

            // 從使用者資訊中取得電子郵件
            String email = userInfo.get("email") != null ? (String) userInfo.get("email") : "未知";
            String name = userInfo.get("name") != null ? (String) userInfo.get("name") : "未知";
            String firstName = userInfo.get("given_name") != null ? (String) userInfo.get("given_name") : "未知";
            String lastName = userInfo.get("family_name") != null ? (String) userInfo.get("family_name") : "未知";
            
            // 組合重導向 URL，將使用者資訊與 tokens 附加至 query string 中
            String redirectTarget = frontendUrl
                + "?username=" + URLEncoder.encode(preferredUsername, "UTF-8")
                + "&email=" + URLEncoder.encode(email, "UTF-8")
                + "&name=" + URLEncoder.encode(name, "UTF-8")
                + "&firstName=" + URLEncoder.encode(firstName, "UTF-8")
                + "&lastName=" + URLEncoder.encode(lastName, "UTF-8")
                + "&token=" + URLEncoder.encode(accessToken, "UTF-8")
                + "&refreshToken=" + URLEncoder.encode(refreshToken, "UTF-8");
        
            // 添加詳細日誌
            log.info("=== 重定向診斷 ===");
            log.info("前端URL: {}", frontendUrl);
            log.info("用戶名: {}", preferredUsername);
            log.info("Token長度: {}", accessToken.length());
            log.info("Token前20字符: {}", accessToken.substring(0, Math.min(20, accessToken.length())));
            log.info("完整重定向URL: {}", redirectTarget);
        
            // 執行 HTTP 重導向
            response.sendRedirect(redirectTarget);
        } catch (Exception e) {
            // 若有任何錯誤，記錄錯誤並回傳 500 錯誤碼
            log.error("處理 OAuth 重定向時發生錯誤", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "處理 OAuth 重定向時發生錯誤");
        }
    }

    /**
     * 使用提供的 refresh token 呼叫 Keycloak 的登出 API，撤銷更新憑證
     * 
     * 此方法將 refresh token 與 client 資訊作為參數傳遞至 Keycloak 登出端點，
     * 若成功則回傳登出成功訊息；若失敗則回傳錯誤訊息。
     *
     * @param refreshToken 用於登出的更新憑證
     * @return ResponseEntity 包含登出操作結果的訊息與狀態碼
     */
    @CrossOrigin
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam("refreshToken") String refreshToken) {
        // 組合 Keycloak 的登出 endpoint URL
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

            // 若成功則回傳 200 OK 與訊息
            return ResponseEntity.ok(ErrorCode.LOGOUT_SUCCESS.getMessage());
        } catch (Exception e) {
            // 若發生錯誤，記錄錯誤訊息並回傳 500 錯誤碼與錯誤訊息
            log.error("登出失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorCode.LOGOUT_FAILED.getMessage());
        }
    }

    /**
     * 檢查指定的 access token 是否有效，並在必要時使用 refresh token 進行續期
     * 
     * 此方法會先呼叫 Keycloak 的 introspection 端點檢查存取憑證 (access token) 的有效性，
     * 若 token 有效則直接回傳檢查結果；若 token 無效且同時提供了 refresh token，則會嘗試透過 refresh token 來刷新存取憑證，
     * 若刷新成功，則回傳新取得的 token 資訊並增加 "refreshed" 標記；若刷新失敗，則回傳未授權狀態。
     *
     * @param token 要檢查的存取憑證 (access token)
     * @param refreshToken (可選) 用於刷新存取憑證的更新憑證 (refresh token)
     * @return ResponseEntity 包含 token 檢查結果、刷新後的 token 資訊或錯誤訊息的回應
     */
    @CrossOrigin
    @PostMapping("/introspect")
    public ResponseEntity<?> introspectToken(
            @RequestParam("token") String token,
            @RequestParam(value = "refreshToken", required = false) String refreshToken) {
    
        String introspectUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
        String tokenUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    
        try {
            // Step 1: 驗證 access token 是否仍有效
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
            Map<String, Object> result = introspectResponse.getBody();
            if (result == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorCode.TOKEN_INTROSPECT_FAILED.getMessage());
            }
    
            // Step 2: 如果 token 還有效，直接回傳
            if (result != null && Boolean.TRUE.equals(result.get("active"))) {
                return ResponseEntity.ok(result);
            }
    
            // Step 3: token 無效，嘗試用 refresh token 取得新 token
            if (refreshToken != null && !refreshToken.isEmpty()) {
                MultiValueMap<String, String> refreshParams = new LinkedMultiValueMap<>();
                refreshParams.add("grant_type", "refresh_token");
                refreshParams.add("client_id", clientId);
                refreshParams.add("client_secret", clientSecret);
                refreshParams.add("refresh_token", refreshToken);
    
                HttpEntity<MultiValueMap<String, String>> refreshEntity = new HttpEntity<>(refreshParams, headers);
    
                ResponseEntity<Map<String, Object>> refreshResponse = restTemplate.exchange(
                        tokenUrl, 
                        HttpMethod.POST, 
                        refreshEntity, 
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );
                Map<String, Object> refreshResult = refreshResponse.getBody();
                if (refreshResult == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorCode.TOKEN_REFRESH_FAILED.getMessage());
                }
    
                if (refreshResult.get("access_token") != null) {
                    // 回傳新的 access token 及相關資訊
                    return ResponseEntity.ok(refreshResult);
                }
            }
    
            // Step 4: 無法刷新，回傳 UNAUTHORIZED
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorCode.TOKEN_INVALID_OR_REFRESH_FAILED.getMessage());
        } catch (Exception e) {
            log.error("內省失敗", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorCode.TOKEN_CHECK_FAILED.getMessage());
        }
    }
}
