package tw.com.tymbackend.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    // 安全過濾器鏈
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 公開端點 - 不需要認證
                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/tymb/public/**").permitAll()
                        .requestMatchers("/keycloak/**").permitAll()  // 允許Keycloak相關端點
                        // 其他所有端點需要認證
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwkSetUri(keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs")
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // JWT 認證轉換器 - 處理 Keycloak 的角色
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new CustomJwtGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }

    // 自定義 JWT 權限轉換器
    public static class CustomJwtGrantedAuthoritiesConverter implements org.springframework.core.convert.converter.Converter<Jwt, Collection<GrantedAuthority>> {
        
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<GrantedAuthority> authorities = new java.util.ArrayList<>();
            
            // 處理 realm_access.roles (基本角色)
            if (jwt.hasClaim("realm_access")) {
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess != null && realmAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) realmAccess.get("roles");
                    authorities.addAll(roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList()));
                }
            }
            
            // 處理 resource_access.realm-management.roles (manage-users 角色)
            if (jwt.hasClaim("resource_access")) {
                Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
                if (resourceAccess != null && resourceAccess.containsKey("realm-management")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> realmManagement = (Map<String, Object>) resourceAccess.get("realm-management");
                    if (realmManagement != null && realmManagement.containsKey("roles")) {
                        @SuppressWarnings("unchecked")
                        List<String> roles = (List<String>) realmManagement.get("roles");
                        authorities.addAll(roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList()));
                    }
                }
            }
            
            // 如果沒有 manage-users 角色，添加 GUEST 角色
            boolean hasManageUsers = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_manage-users"));
            
            if (!hasManageUsers) {
                authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
            }
            
            return authorities;
        }
    }

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

    // 密碼編碼器
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
