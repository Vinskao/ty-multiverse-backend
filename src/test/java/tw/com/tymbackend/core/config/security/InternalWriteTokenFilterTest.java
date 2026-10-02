package tw.com.tymbackend.core.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class InternalWriteTokenFilterTest {

    private static final String TOKEN = "s3cret";

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Authentication run(String serverToken, String method, String uri, String contextPath, String header)
            throws Exception {
        SecurityContextHolder.clearContext();
        MockHttpServletRequest req = new MockHttpServletRequest(method, uri);
        req.setContextPath(contextPath);
        if (header != null) {
            req.addHeader("X-Internal-Token", header);
        }
        new InternalWriteTokenFilter(serverToken)
                .doFilter(req, new MockHttpServletResponse(), new MockFilterChain());
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void doFilter_Should_GrantManageUsers_When_TokenValidOnProtectedEndpoint() throws Exception {
        Authentication auth = run(TOKEN, "POST", "/tymb/people/delete-all", "/tymb", TOKEN);

        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_manage-users")));
    }

    @Test
    void doFilter_Should_StayAnonymous_When_TokenWrong() throws Exception {
        assertNull(run(TOKEN, "POST", "/people/delete-all", "", "wrong"));
    }

    @Test
    void doFilter_Should_StayAnonymous_When_TokenIsPrefixOfSecret() throws Exception {
        assertNull(run(TOKEN, "POST", "/people/delete-all", "", "s3cre"));
    }

    @Test
    void doFilter_Should_StayAnonymous_When_TokenMissing() throws Exception {
        assertNull(run(TOKEN, "DELETE", "/people/1", "", null));
    }

    @Test
    void doFilter_Should_StayAnonymous_When_ServerTokenNotConfigured() throws Exception {
        assertNull(run("", "POST", "/people/insert", "", ""));
        assertNull(run(null, "POST", "/people/insert", "", "anything"));
        assertNull(run("  ", "POST", "/people/insert", "", "  "));
    }

    @ParameterizedTest
    @CsvSource({
            "GET,/people/1",
            "POST,/people/get-all",
            "POST,/people/get-by-name",
            "POST,/people/batchDamageWithWeapon",
            "POST,/gallery/getAll",
            "GET,/weapons/abc"
    })
    void doFilter_Should_NotGrantIdentity_When_EndpointIsReadOnly(String method, String uri) throws Exception {
        assertNull(run(TOKEN, method, uri, "", TOKEN));
    }

    @ParameterizedTest
    @CsvSource({
            "POST,/people/insert",
            "POST,/people/insert-multiple",
            "POST,/people/update",
            "POST,/people/delete",
            "POST,/weapons",
            "POST,/weapons/insert-multiple",
            "POST,/people-images/bob",
            "POST,/gallery/save",
            "POST,/gallery/delete-all",
            "POST,/people-images",
            "POST,/ckeditor/save-content",
            "PUT,/weapons/abc",
            "DELETE,/people-images/9"
    })
    void doFilter_Should_GrantIdentity_When_TokenValidOnWriteEndpoint(String method, String uri) throws Exception {
        Authentication auth = run(TOKEN, method, uri, "", TOKEN);
        assertNotNull(auth, method + " " + uri);
        assertEquals("internal-sync", auth.getPrincipal());
    }

    @ParameterizedTest
    @CsvSource({
            "POST,/people/delete-all/",
            "POST,/people/%64elete-all",
            "POST,/people/delete-all;x=1"
    })
    void doFilter_Should_FailClosed_When_PathIsObfuscated(String method, String uri) throws Exception {
        assertNull(run(TOKEN, method, uri, "", TOKEN));
    }
}
