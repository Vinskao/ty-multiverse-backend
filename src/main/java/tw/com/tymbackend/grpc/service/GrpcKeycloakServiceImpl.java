package tw.com.tymbackend.grpc.service;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tw.com.tymbackend.grpc.people.KeycloakServiceGrpc;
import tw.com.tymbackend.grpc.people.*;
import tw.com.tymbackend.core.exception.ErrorCode;

import java.util.Map;

/**
 * gRPC Keycloak Service 實現
 * 
 * <p>提供 Keycloak 認證相關的 gRPC 服務</p>
 * <p>包括 OAuth2 重定向處理、用戶登出和 Token 驗證等功能</p>
 * 
 * @author TY Team
 * @version 1.0
 */
@Service
public class GrpcKeycloakServiceImpl extends KeycloakServiceGrpc.KeycloakServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcKeycloakServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${url.frontend}")
    private String frontendUrl;

    @Value("${app.url.address}")
    private String backendUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.clientId}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${keycloak.auth-server-url}")
    private String ssoUrl;

    @Override
    public void processAuthRedirect(AuthRedirectRequest request, StreamObserver<AuthRedirectResponse> responseObserver) {
        logger.info("📥 gRPC: processAuthRedirect, code={}", request.getCode());
        
        try {
            String redirectUri = request.getRedirectUri();
            String code = request.getCode();
            
            // 組合 token 請求 URL
            String tokenUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            // 建立 token 請求參數
            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            tokenParams.add("client_id", clientId);
            tokenParams.add("client_secret", clientSecret);
            tokenParams.add("code", code);
            tokenParams.add("grant_type", "authorization_code");
            tokenParams.add("redirect_uri", redirectUri);
            
            // 建立 HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(tokenParams, headers);
            
            // 發送 token 請求
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
            
            String accessToken = (String) tokenBody.get("access_token");
            String refreshToken = (String) tokenBody.get("refresh_token");
            
            if (accessToken == null || refreshToken == null) {
                throw new RuntimeException("無法取得 access token");
            }
            
            // 取得使用者資訊
            String userInfoUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
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
                throw new RuntimeException("無法取得使用者資訊");
            }
            
            // 建立回應
            UserInfo.Builder userInfoBuilder = UserInfo.newBuilder()
                .setUsername((String) userInfo.getOrDefault("preferred_username", ""))
                .setEmail((String) userInfo.getOrDefault("email", ""))
                .setName((String) userInfo.getOrDefault("name", ""))
                .setFirstName((String) userInfo.getOrDefault("given_name", ""))
                .setLastName((String) userInfo.getOrDefault("family_name", ""));
            
            AuthRedirectResponse response = AuthRedirectResponse.newBuilder()
                .setSuccess(true)
                .setMessage("認證成功")
                .setUserInfo(userInfoBuilder.build())
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("✅ gRPC: processAuthRedirect 成功");
            
        } catch (Exception e) {
            logger.error("❌ gRPC: processAuthRedirect 錯誤", e);
            
            AuthRedirectResponse response = AuthRedirectResponse.newBuilder()
                .setSuccess(false)
                .setMessage("認證失敗: " + e.getMessage())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        logger.info("📥 gRPC: logout, refreshToken={}", request.getRefreshToken().substring(0, 20) + "...");
        
        try {
            String refreshToken = request.getRefreshToken();
            String logoutUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
            
            // 建立登出請求參數
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("refresh_token", refreshToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            
            // 發送登出請求
            restTemplate.exchange(logoutUrl, HttpMethod.POST, entity, String.class);
            
            LogoutResponse response = LogoutResponse.newBuilder()
                .setSuccess(true)
                .setMessage(ErrorCode.LOGOUT_SUCCESS.getMessage())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("✅ gRPC: logout 成功");
            
        } catch (Exception e) {
            logger.error("❌ gRPC: logout 錯誤", e);
            
            LogoutResponse response = LogoutResponse.newBuilder()
                .setSuccess(false)
                .setMessage(ErrorCode.LOGOUT_FAILED.getMessage())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void introspectToken(IntrospectTokenRequest request, StreamObserver<IntrospectTokenResponse> responseObserver) {
        logger.info("📥 gRPC: introspectToken, token={}", request.getAccessToken().substring(0, 20) + "...");
        
        try {
            String accessToken = request.getAccessToken();
            String refreshToken = request.getRefreshToken();
            
            String introspectUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
            String tokenUrl = ssoUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            // Step 1: 驗證 access token
            MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();
            bodyParams.add("client_id", clientId);
            bodyParams.add("client_secret", clientSecret);
            bodyParams.add("token", accessToken);
            
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
                throw new RuntimeException("無法取得 introspection 響應");
            }
            
            // Step 2: 如果 token 還有效，直接回傳
            if (Boolean.TRUE.equals(result.get("active"))) {
                IntrospectTokenResponse response = IntrospectTokenResponse.newBuilder()
                    .setActive(true)
                    .setMessage("Token 有效")
                    .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
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
                if (refreshResult != null && refreshResult.get("access_token") != null) {
                    IntrospectTokenResponse response = IntrospectTokenResponse.newBuilder()
                        .setActive(true)
                        .setMessage("Token 已刷新")
                        .setNewAccessToken((String) refreshResult.get("access_token"))
                        .setNewRefreshToken((String) refreshResult.get("refresh_token"))
                        .build();
                    
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return;
                }
            }
            
            // Step 4: 無法刷新，回傳無效
            IntrospectTokenResponse response = IntrospectTokenResponse.newBuilder()
                .setActive(false)
                .setMessage(ErrorCode.TOKEN_INVALID_OR_REFRESH_FAILED.getMessage())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("✅ gRPC: introspectToken 完成");
            
        } catch (Exception e) {
            logger.error("❌ gRPC: introspectToken 錯誤", e);
            
            IntrospectTokenResponse response = IntrospectTokenResponse.newBuilder()
                .setActive(false)
                .setMessage("Token 檢查失敗: " + e.getMessage())
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
