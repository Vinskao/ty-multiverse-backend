package tw.com.tymbackend.core.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import tw.com.tymbackend.core.exception.ErrorCode;
import tw.com.tymbackend.core.exception.ErrorResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring Security 配置類 - 混合認證策略
 * 
 * 支持無狀態 JWT 認證和有狀態 Session 認證的混合架構：
 * - 無狀態服務：使用 JWT Token 認證
 * - 有狀態服務：使用 Session 認證 (CKEditor, DeckOfCards)
 */
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    /**
     * 配置安全過濾器鏈 - 混合認證策略
     * 
     * @param http HttpSecurity 實例
     * @return 配置好的 SecurityFilterChain
     * @throws Exception 配置異常
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 保護 (已禁用)
                .csrf(csrf -> csrf.disable())
                
                // 2. 授權配置 - 混合策略
                .authorizeHttpRequests(authorize -> authorize
                        // 公開訪問的靜態資源
                        .requestMatchers("/javadoc/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        
                        // 有狀態服務 - 使用 Session 認證
                        .requestMatchers("/ckeditor/**").authenticated()
                        .requestMatchers("/deckofcards/**").authenticated()
                        
                        // 無狀態服務 - 使用 JWT 認證
                        .requestMatchers("/guardian/admin").hasRole("manage-users")
                        .requestMatchers("/guardian/user").authenticated()
                        .requestMatchers("/guardian/token-info").authenticated()
                        .requestMatchers("/guardian/test-default").authenticated()
                        .requestMatchers("/people/**").authenticated()
                        .requestMatchers("/weapon/**").authenticated()
                        .requestMatchers("/gallery/**").authenticated()
                        .requestMatchers("/livestock/**").authenticated()
                        
                        // 其他所有端點預設公開
                        .anyRequest().permitAll())
                
                // 3. OAuth2 Resource Server 配置 (無狀態認證)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwkSetUri(keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs")
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                
                // 4. 會話管理 - 混合策略
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // 改為按需創建
                        .maximumSessions(1)  // 每個用戶最多一個會話
                        .maxSessionsPreventsLogin(false))  // 允許新登錄
                
                // 5. 錯誤處理 (連接到 Error Handler 模組)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                
                // 6. 登出配置
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

    /**
     * 認證失敗處理器
     * 
     * 當用戶未提供有效認證憑證時觸發
     * 
     * @return 配置好的 AuthenticationEntryPoint
     */
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.AUTHENTICATION_FAILED, 
                ErrorCode.AUTHENTICATION_FAILED.getMessage(), 
                authException.getMessage()
            );
            
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }

    /**
     * 授權失敗處理器
     * 
     * 當用戶已認證但沒有足夠權限時觸發
     * 
     * @return 配置好的 AccessDeniedHandler
     */
    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.AUTHORIZATION_FAILED, 
                ErrorCode.AUTHORIZATION_FAILED.getMessage(), 
                accessDeniedException.getMessage()
            );
            
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }

    /**
     * 登出成功處理器
     * 
     * 當用戶成功登出時觸發
     * 
     * @return 配置好的 LogoutSuccessHandler
     */
    @Bean
    LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
                ErrorCode.LOGOUT_SUCCESS, 
                ErrorCode.LOGOUT_SUCCESS.getMessage(), 
                "您已成功登出系統"
            );
            
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }

    /**
     * JWT 認證轉換器 - 處理 Keycloak 的角色
     * 
     * @return 配置好的 JwtAuthenticationConverter
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new CustomJwtGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }

    /**
     * 自定義 JWT 權限轉換器
     * 
     * 負責將 JWT Token 中的角色信息轉換為 Spring Security 的權限對象。
     */
    public static class CustomJwtGrantedAuthoritiesConverter implements org.springframework.core.convert.converter.Converter<Jwt, Collection<GrantedAuthority>> {
        
        /**
         * 轉換 JWT Token 為權限集合
         * 
         * @param jwt JWT Token
         * @return 權限集合
         */
        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(@SuppressWarnings("null") Jwt jwt) {
            List<GrantedAuthority> authorities = new java.util.ArrayList<>();
            
            // 處理 realm_access.roles (基本角色)
            if (jwt.hasClaim("realm_access")) {
                jwt.getClaimAsMap("realm_access")
                    .entrySet().stream()
                    .filter(entry -> "roles".equals(entry.getKey()))
                    .findFirst()
                    .map(entry -> (List<String>) entry.getValue())
                    .ifPresent(roles -> authorities.addAll(
                        roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList())
                    ));
            }
            
            // 處理 resource_access.realm-management.roles (manage-users 角色)
            if (jwt.hasClaim("resource_access")) {
                jwt.getClaimAsMap("resource_access")
                    .entrySet().stream()
                    .filter(entry -> "realm-management".equals(entry.getKey()))
                    .findFirst()
                    .map(entry -> (Map<String, Object>) entry.getValue())
                    .map(realmManagement -> realmManagement.get("roles"))
                    .map(roles -> (List<String>) roles)
                    .ifPresent(roles -> authorities.addAll(
                        roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList())
                    ));
            }
            
            // 如果沒有 manage-users 角色，添加 GUEST 角色
            boolean hasManageUsers = authorities.stream()
                    .anyMatch(authority -> "ROLE_manage-users".equals(authority.getAuthority()));
            
            if (!hasManageUsers) {
                authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
            }
            
            return authorities;
        }
    }

    /**
     * CORS 配置器
     * 
     * @return 配置好的 WebMvcConfigurer
     */
    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@SuppressWarnings("null") CorsRegistry registry) {
                // 添加CORS映射
                registry.addMapping("/**")
                        .allowedOrigins(
                            "http://localhost:4321", 
                            "http://localhost:8080", 
                            "http://localhost:8000", 
                            "https://peoplesystem.tatdvsonorth.com",
                            "https://peoplesystem.tatdvsonorth.com/tymultiverse",
                            "http://127.0.0.1:4321", 
                            "http://127.0.0.1:8080", 
                            "http://127.0.0.1:8000"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization", "Set-Cookie")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    /**
     * 密碼編碼器
     * 
     * @return BCrypt 密碼編碼器
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
