package tw.com.tymbackend.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Authentication helpers for web-slice tests. */
public final class TestAuth {

    public static final String INTERNAL_HEADER = "X-Internal-Token";
    public static final String INTERNAL_TOKEN = "internal-secret";

    private TestAuth() {
    }

    /** Keycloak JWT holding the manage-users realm role. */
    public static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("admin-sub").claim("preferred_username", "admin"))
                .authorities(new SimpleGrantedAuthority("ROLE_manage-users"));
    }

    /** The owner signed in as the same person as {@link #user()} (same subject and name), for tests that check identity. */
    public static RequestPostProcessor owner() {
        return jwt().jwt(j -> j.subject("user-sub").claim("preferred_username", "alice"))
                .authorities(new SimpleGrantedAuthority("ROLE_manage-users"));
    }

    /** Valid Keycloak JWT with no special role. */
    public static RequestPostProcessor user() {
        return jwt().jwt(j -> j.subject("user-sub").claim("preferred_username", "alice"))
                .authorities(new SimpleGrantedAuthority("ROLE_user"));
    }
}
