package tw.com.tymbackend.core.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 內部寫入 Token 過濾器（混合驗證的一半）
 *
 * <p>
 * 背景：people/weapons/gallery/people-images/ckeditor 的「寫入／刪除」端點原本在 SecurityConfig
 * 中為 {@code permitAll()}（為了讓同步腳本免 Keycloak 登入即可呼叫），導致未經任何驗證的外部使用者
 * 可以隨意 insert / update / delete / delete-all。
 * </p>
 *
 * <p>
 * 修正策略採「JWT 或 內部 Token」混合驗證：
 * </p>
 * <ul>
 * <li>瀏覽器 admin 管理頁已帶 Keycloak JWT → 由 oauth2ResourceServer 驗證（本過濾器不介入）。</li>
 * <li>同步腳本帶共用密鑰標頭 {@code X-Internal-Token} → 本過濾器驗證通過後，於 SecurityContext
 * 注入一個帶 {@code ROLE_manage-users} 的 Authentication，讓後續 {@code authenticated()} /
 * {@code hasRole('manage-users')} 規則放行（含 delete-all）。</li>
 * <li>匿名（兩者皆無）→ 本過濾器不設定身分，最終由 Spring Security 以 401 擋下。</li>
 * </ul>
 *
 * <p>
 * 注意：本過濾器只在「變更類」端點上嘗試授予身分，查詢端點（含以 POST 實作的 get-all /
 * get-by-name / batchDamageWithWeapon 等）完全不受影響。Token 比較採
 * {@link MessageDigest#isEqual} 常數時間比較，避免 timing side-channel。
 * </p>
 */
public class InternalWriteTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalWriteTokenFilter.class);
    private static final String HEADER = "X-Internal-Token";
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /**
     * 受保護的變更類端點：method → ant patterns。
     * 路徑不含 context-path（/tymb），與 Spring Security requestMatchers 一致。
     */
    private static final Map<String, List<String>> PROTECTED = Map.of(
            "POST", List.of(
                    "/people/insert",
                    "/people/insert-multiple",
                    "/people/update",
                    "/people/delete",
                    "/people/delete-all",
                    "/weapons",
                    "/gallery/save",
                    "/gallery/update",
                    "/gallery/delete",
                    "/gallery/delete-all",
                    "/people-images",
                    "/ckeditor/save-content"),
            "PUT", List.of(
                    "/people/**",
                    "/weapons/**",
                    "/people-images/**"),
            "DELETE", List.of(
                    "/people/**",
                    "/weapons/**",
                    "/people-images/**"));

    private final String writeToken;

    public InternalWriteTokenFilter(String writeToken) {
        this.writeToken = writeToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (isProtected(request) && hasValidInternalToken(request)) {
            // 內部 Token 有效：注入機器身分，讓 authenticated() / hasRole('manage-users') 放行
            var auth = new UsernamePasswordAuthenticationToken(
                    "internal-sync",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_manage-users"),
                            new SimpleGrantedAuthority("ROLE_internal-sync")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("內部 Token 驗證通過，授予 internal-sync 身分: {} {}",
                    request.getMethod(), request.getRequestURI());
        }
        // 未帶或無效 Token 時不設定身分；若該請求帶有 JWT，oauth2ResourceServer 會接手驗證；
        // 兩者皆無則最終由 Spring Security 以 401 擋下。

        chain.doFilter(request, response);
    }

    private boolean isProtected(HttpServletRequest request) {
        List<String> patterns = PROTECTED.get(request.getMethod());
        if (patterns == null) {
            return false;
        }
        String path = pathWithinApplication(request);
        for (String pattern : patterns) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValidInternalToken(HttpServletRequest request) {
        if (writeToken == null || writeToken.isBlank()) {
            return false;
        }
        String provided = request.getHeader(HEADER);
        if (provided == null || provided.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                writeToken.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    /** 取得去除 context-path（/tymb）後的路徑。 */
    private String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        return uri.isEmpty() ? "/" : uri;
    }
}
