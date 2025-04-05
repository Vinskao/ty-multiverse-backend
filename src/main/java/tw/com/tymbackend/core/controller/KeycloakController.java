package tw.com.tymbackend.core.controller;

import java.io.IOException;
import java.time.Instant;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpServletResponse;
import tw.com.tymbackend.core.domain.vo.Keycloak;
import tw.com.tymbackend.core.service.KeycloakService;

@RestController
@RequestMapping("/keycloak")
public class KeycloakController {

    @Value("${url.frontend}")
    private String frontendUrl;

    private static final Logger log = LoggerFactory.getLogger(KeycloakController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KeycloakService keycloakService;

    @Value("${url.address}")
    private String backendUrl;

    private String clientId = "peoplesystem";
    private String clientSecret = "vjTssuy94TUlk8mipbQjMlSSlHyS3CxG";

    @GetMapping("/redirect")
    public void keycloakRedirect(@RequestParam("code") String code, HttpServletResponse response)
            throws IOException {
        String redirectUri = backendUrl + "/keycloak/redirect";
        String tokenUrl = "https://peoplesystem.tatdvsonorth.com/sso/realms/PeopleSystem/protocol/openid-connect/token";

        try {
            log.info("Received authorization code: {}", code);

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

            @SuppressWarnings({ "rawtypes" })
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);
            @SuppressWarnings("null")
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            @SuppressWarnings("null")
            String refreshToken = (String) tokenResponse.getBody().get("refresh_token");

            log.info("Access Token: {}", accessToken);
            log.info("Refresh Token: {}", refreshToken);

            if (accessToken == null || refreshToken == null) {
                throw new RuntimeException("Failed to obtain access token");
            }

            // Request User Info
            String userInfoUrl = "https://peoplesystem.tatdvsonorth.com/sso/realms/PeopleSystem/protocol/openid-connect/userinfo";
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);

            @SuppressWarnings({ "rawtypes" })
            ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userEntity,
                    Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = userResponse.getBody();

            log.info("User Info: {}", userInfo);

            @SuppressWarnings("null")
            String preferredUsername = (String) userInfo.get("preferred_username");
            if (preferredUsername == null) {
                throw new RuntimeException("Failed to retrieve user info");
            }

            keycloakService.deleteByUsername(preferredUsername);

            // Save or Update User
            Keycloak user = new Keycloak();
            user.setPreferredUsername(preferredUsername);
            user.setEmail((String) userInfo.get("email"));
            user.setGivenName((String) userInfo.get("given_name"));
            user.setFamilyName((String) userInfo.get("family_name"));
            user.setEmailVerified((Boolean) userInfo.get("email_verified"));
            user.setSub((String) userInfo.get("sub"));
            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setIssuedAt(Instant.now());
            user.setExpiresIn(Instant.now().plusSeconds(3600));
            keycloakService.saveKeycloakData(user);

            response.setHeader("Set-Cookie", "refreshToken=; Path=/; Max-Age=0; SameSite=Lax");
            // 设置Cookie并重定向到前端
            response.addHeader("Set-Cookie", "authorizationCode=" + code + "; Path=/; SameSite=Lax");
            response.addHeader("Set-Cookie", "refreshToken=" + refreshToken + "; Path=/; SameSite=Lax");

            response.sendRedirect(
                frontendUrl + "?username=" + preferredUsername 
                            + "&email=" + user.getEmail()
                            + "&token=" + accessToken
                            + "&refreshToken=" + refreshToken);
        } catch (Exception e) {
            log.error("Error processing OAuth redirect", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing OAuth redirect");
        }
    }

    @CrossOrigin
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String refreshToken) {
        String logoutUrl = "https://peoplesystem.tatdvsonorth.com/sso/realms/PeopleSystem/protocol/openid-connect/logout";

        try {
            // 檢查 refreshToken 是否為空
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                log.warn("Received empty refresh token");
                return ResponseEntity.badRequest().body("Refresh token cannot be empty");
            }

            log.info("Attempting to logout with refresh token: {}", refreshToken.substring(0, 10) + "...");

            // Revoke Token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            try {
                // 同步執行登出請求
                ResponseEntity<String> response = restTemplate.exchange(logoutUrl, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    log.info("Session invalidated successfully");
                    return ResponseEntity.ok("Logout successful");
                } else {
                    log.error("Logout failed with status: {}", response.getStatusCode());
                    return ResponseEntity.status(response.getStatusCode()).body("Logout failed");
                }
            } catch (HttpClientErrorException.BadRequest e) {
                log.error("Invalid refresh token: {}", e.getResponseBodyAsString());
                return ResponseEntity.ok("Logout completed");
            }
        } catch (Exception e) {
            log.error("Logout failed with error", e);
            return ResponseEntity.ok("Logout completed");
        }
    }

    @CrossOrigin
    @GetMapping("/introspect")
    public ResponseEntity<?> introspectToken(
            @RequestParam("token") String token,
            @RequestParam(value = "refreshToken", required = false) String refreshToken) {
        
        String introspectUrl = "https://peoplesystem.tatdvsonorth.com/sso/realms/PeopleSystem/protocol/openid-connect/token/introspect";
        String tokenUrl = "https://peoplesystem.tatdvsonorth.com/sso/realms/PeopleSystem/protocol/openid-connect/token";
    
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
