package tw.com.tymbackend.core.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tw.com.ty.common.security.config.BaseSecurityConfig;

/**
 * TY Multiverse Backend Security 配置
 *
 * <p>
 * 基于 AGENTS.md 的端点定义进行集中权限配置：
 * </p>
 * <ul>
 * <li>SELECT 系列：GET 请求，已认证用户即可访问</li>
 * <li>INSERT/UPDATE/DELETE 系列：POST/PUT/DELETE 请求，需要认证</li>
 * <li>批量删除：DELETE *all 请求，仅管理员可访问</li>
 * <li>公共路径：健康检查、Swagger、认证端点部分公开</li>
 * </ul>
 *
 * <p>
 * 架构说明：
 * </p>
 * <ul>
 * <li>Gateway 已验证 Token 有效性（粗粒度路由级别）</li>
 * <li>Backend 通过 HttpSecurity 配置方法级别权限控制（细粒度）</li>
 * <li>实现深度防御：即使 Gateway 被绕过，Backend 仍有保护</li>
 * </ul>
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 * @see BaseSecurityConfig
 */
@Configuration
@EnableWebSecurity
@Import(BaseSecurityConfig.class)
public class SecurityConfig {

    @Value("${keycloak.auth-server-url}")
    private String keycloakAuthServerUrl;

    @Value("${keycloak.realm}")
    private String keycloakRealm;

    @Value("${security.disable-all:false}")
    private boolean disableAllSecurity;

    /**
     * 全局 Security Filter Chain 配置
     *
     * <p>
     * 按照 AGENTS.md 端点定义配置权限：
     * </p>
     * <ul>
     * <li>使用 Ant 路径匹配器进行精确控制</li>
     * <li>优先级：从具体到通用（permitAll -> authenticated -> hasRole）</li>
     * <li>最后兜底：所有未匹配请求都需要认证</li>
     * <li>開發模式：security.disable-all=true 時完全開放所有端點</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 保护：REST API 无需 CSRF
                .csrf(csrf -> csrf.disable())

                // Session 管理：无状态（JWT）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 開發測試模式：完全禁用安全性
        if (disableAllSecurity) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        // 正常安全性配置：基于 AGENTS.md 的端点定义
        // 注意：requestMatchers 路徑不應包含 context-path（/tymb）
        http.authorizeHttpRequests(auth -> auth

                // ========================================
                // 公共路径：完全开放，无需任何认证
                // ========================================
                .requestMatchers("/actuator/**").permitAll() // Spring Boot Actuator
                .requestMatchers("/health/**").permitAll() // 健康检查
                .requestMatchers("/swagger-ui/**").permitAll() // Swagger UI
                .requestMatchers("/v3/api-docs/**").permitAll() // API 文档
                .requestMatchers("/webjars/**").permitAll() // Swagger 静态资源
                .requestMatchers("/auth/visitor").permitAll() // 访客端点
                .requestMatchers("/auth/health").permitAll() // 认证健康检查
                .requestMatchers("/keycloak/**").permitAll() // Keycloak 所有端点（包含 introspect）

                // ========================================
                // SELECT 系列：GET 请求，完全开放，无需认证
                // ========================================
                .requestMatchers("GET", "/people/**").permitAll() // People 查询 - 完全开放
                .requestMatchers("GET", "/weapons/**").permitAll() // Weapon 查询 - 完全开放
                .requestMatchers("GET", "/gallery/**").permitAll() // Gallery 查询 - 完全开放
                .requestMatchers("GET", "/api/**").permitAll() // Async API 查询 - 完全开放
                .requestMatchers("GET", "/people-images/**").permitAll() // People Images 查询 - 完全开放
                .requestMatchers("GET", "/deckofcards/**").permitAll() // Deckofcards 查询 - 完全开放
                .requestMatchers("GET", "/ckeditor/**").permitAll() // CKEditor 查询 - 完全开放

                // ========================================
                // 特殊查询端点：POST 请求但用于查询，也完全开放
                // ========================================
                .requestMatchers("POST", "/people/get-all").permitAll() // People get-all 查询 - 完全开放
                .requestMatchers("POST", "/people/get-by-name").permitAll() // People get-by-name 查询 - 完全开放
                .requestMatchers("POST", "/people/names").permitAll() // People names 查询 - 完全开放
                .requestMatchers("POST", "/gallery/**").permitAll() // Gallery POST 查询 - 完全开放

                // ========================================
                // INSERT/UPDATE/DELETE 系列：需要认证用户
                // ========================================
                // People - 特定修改端点需要认证（查询端点已在上方放行）
                .requestMatchers("POST", "/people/insert").permitAll() // People 创建
                .requestMatchers("POST", "/people/insert-multiple").permitAll() // People 批量创建
                .requestMatchers("POST", "/people/update").permitAll() // People 更新
                .requestMatchers("POST", "/people/delete").permitAll() // People 删除
                .requestMatchers("PUT", "/people/**").permitAll() // People PUT 更新
                .requestMatchers("DELETE", "/people/**").permitAll() // People DELETE 删除

                // Weapons - 修改端点需要认证
                .requestMatchers("POST", "/weapons/**").permitAll() // Weapon 创建
                .requestMatchers("PUT", "/weapons/**").permitAll() // Weapon 更新
                .requestMatchers("DELETE", "/weapons/**").permitAll() // Weapon 删除

                // API - 修改端点需要认证
                .requestMatchers("POST", "/api/**").permitAll() // Async API 创建
                .requestMatchers("PUT", "/api/**").permitAll() // Async API 更新
                .requestMatchers("DELETE", "/api/**").permitAll() // Async API 删除

                // People Images - 修改端点需要认证
                .requestMatchers("POST", "/people-images/**").permitAll() // People Images 创建
                .requestMatchers("PUT", "/people-images/**").permitAll() // People Images 更新
                .requestMatchers("DELETE", "/people-images/**").permitAll() // People Images 删除

                // ========================================
                // 批量刪除：公開以利於同步腳本
                // ========================================
                .requestMatchers("POST", "/people/delete-all").permitAll() // 批量刪除 People (POST)
                .requestMatchers("DELETE", "/weapons/delete-all").permitAll() // 批量刪除 Weapons (DELETE)
                .requestMatchers("DELETE", "/gallery/delete-all").permitAll() // 批量刪除 Gallery (DELETE)

                // ========================================
                // 认证端点：需要认证用户
                // ========================================
                .requestMatchers("/auth/admin").authenticated() // Admin 测试端点
                .requestMatchers("/auth/user").authenticated() // User 测试端点
                .requestMatchers("/auth/test").authenticated() // Auth 测试端点
                .requestMatchers("/auth/logout-test").authenticated() // Logout 测试端点

                // ========================================
                // 默认规则：所有未匹配的请求都需要认证
                // ========================================
                .anyRequest().authenticated())

                // OAuth2 Resource Server：JWT Token 验证
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * JWT Decoder 配置
     *
     * <p>
     * 从 Keycloak 获取公钥验证 JWT Token
     * </p>
     * <ul>
     * <li>使用 JWK Set URI 动态获取公钥</li>
     * <li>支持 Token 自动刷新和吊销检查</li>
     * </ul>
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = keycloakAuthServerUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * JWT Authentication Converter 配置
     *
     * <p>
     * 从 Keycloak JWT Token 中提取角色信息
     * </p>
     * <ul>
     * <li>从 realm_access.roles 中提取角色</li>
     * <li>自动添加 ROLE_ 前缀以匹配 Spring Security 的角色格式</li>
     * <li>支持 @PreAuthorize("hasRole('manage-users')") 等注解</li>
     * </ul>
     *
     * <p>
     * Keycloak JWT Token 格式示例：
     * </p>
     * 
     * <pre>
     * {
     *   "realm_access": {
     *     "roles": ["manage-users", "user"]
     *   }
     * }
     * </pre>
     *
     * <p>
     * 转换后的 Spring Security Authorities：
     * </p>
     * 
     * <pre>
     * [ROLE_manage-users, ROLE_user]
     * </pre>
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // 创建自定义的 Converter，从 Keycloak JWT token 中提取角色
        Converter<Jwt, Collection<GrantedAuthority>> keycloakAuthoritiesConverter = jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // 从 Keycloak 的 realm_access.roles 中提取角色
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                if (roles != null) {
                    Collection<GrantedAuthority> keycloakAuthorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());
                    authorities.addAll(keycloakAuthorities);
                }
            }

            // 也可以从 resource_access 中提取客户端特定角色（可选）
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                // 遍历所有客户端
                for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                    Object clientAccess = entry.getValue();
                    if (clientAccess instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> clientMap = (Map<String, Object>) clientAccess;
                        @SuppressWarnings("unchecked")
                        List<String> clientRoles = (List<String>) clientMap.get("roles");
                        if (clientRoles != null) {
                            Collection<GrantedAuthority> clientAuthorities = clientRoles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList());
                            authorities.addAll(clientAuthorities);
                        }
                    }
                }
            }

            return authorities;
        };

        // 使用默认的 JwtGrantedAuthoritiesConverter 提取 scope 权限（如果有）
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

        // 创建组合 Converter，先提取 scope，再添加 Keycloak 角色
        Converter<Jwt, Collection<GrantedAuthority>> combinedConverter = jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // 先添加默认的 scope 权限
            authorities.addAll(defaultConverter.convert(jwt));

            // 再添加 Keycloak 角色
            authorities.addAll(keycloakAuthoritiesConverter.convert(jwt));

            return authorities;
        };

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(combinedConverter);

        return jwtAuthenticationConverter;
    }

}
