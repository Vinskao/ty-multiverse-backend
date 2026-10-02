package tw.com.tymbackend.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.admin;
import static tw.com.tymbackend.support.TestAuth.user;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.core.service.AuthService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** All 8 AuthController endpoints. Role enforcement (@PreAuthorize) lives in SecurityRulesWebTest. */
@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = { AuthController.class, SecurityConfig.class })
@SecuredWebSlice
class AuthControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthService authService;

    @Test
    void visitor_Should_BePublic() throws Exception {
        mvc.perform(get("/auth/visitor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void admin_Should_GreetAdmin() throws Exception {
        mvc.perform(get("/auth/admin").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user").value("admin-sub"));
    }

    @Test
    void user_Should_GreetAuthenticatedUser() throws Exception {
        mvc.perform(get("/auth/user").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user").value("user-sub"));
    }

    @Test
    void testDefault_Should_ReportAuthenticated() throws Exception {
        mvc.perform(get("/auth/test-default").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true));
    }

    @Test
    void tokenInfo_Should_ExposeJwtClaims() throws Exception {
        mvc.perform(get("/auth/token-info").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token_type").value("JWT"))
                .andExpect(jsonPath("$.data.claims.preferred_username").value("alice"));
    }

    @Test
    void authTest_Should_SkipServiceCall_When_CredentialsAreNotAString() throws Exception {
        // a JwtAuthenticationToken carries the Jwt object, not the raw token string
        mvc.perform(post("/auth/test").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current_token_available").value(false));
        verify(authService, never()).performAuthTest(any(), any());
    }

    @Test
    void authTest_Should_DelegateToService_When_RawTokenAvailable() throws Exception {
        when(authService.performAuthTest("raw-token", "rt")).thenReturn(Map.of("token_valid", true));
        var auth = new UsernamePasswordAuthenticationToken("alice", "raw-token",
                List.of(new SimpleGrantedAuthority("ROLE_user")));
        mvc.perform(post("/auth/test").param("refreshToken", "rt").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token_valid").value(true));
    }

    @Test
    void authTest_Should_Return500_When_ServiceFails() throws Exception {
        when(authService.performAuthTest(eq("raw-token"), any())).thenThrow(new RuntimeException("boom"));
        var auth = new UsernamePasswordAuthenticationToken("alice", "raw-token",
                List.of(new SimpleGrantedAuthority("ROLE_user")));
        mvc.perform(post("/auth/test").with(authentication(auth)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void logoutTest_Should_Return200_When_ServiceSucceeds() throws Exception {
        when(authService.performLogoutTest("rt")).thenReturn(Map.of("logout_successful", true));
        mvc.perform(post("/auth/logout-test").param("refreshToken", "rt").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.security_context_cleared").value(true));
    }

    @Test
    void logoutTest_Should_NotClearContext_When_LogoutUnsuccessful() throws Exception {
        when(authService.performLogoutTest("rt")).thenReturn(Map.of("logout_successful", false));
        mvc.perform(post("/auth/logout-test").param("refreshToken", "rt").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.security_context_cleared").doesNotExist());
    }

    @Test
    void logoutTest_Should_Return400_When_RefreshTokenMissing() throws Exception {
        mvc.perform(post("/auth/logout-test").with(user())).andExpect(status().isBadRequest());
    }

    @Test
    void logoutTest_Should_Return500_When_ServiceFails() throws Exception {
        when(authService.performLogoutTest("rt")).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/auth/logout-test").param("refreshToken", "rt").with(user()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void health_Should_BePublicAndMergeServiceResult() throws Exception {
        when(authService.performHealthCheck()).thenReturn(Map.of("keycloak_reachable", true));
        mvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keycloak_reachable").value(true));
    }

    @Test
    void health_Should_StillReturn200_When_ServiceFails() throws Exception {
        when(authService.performHealthCheck()).thenThrow(new RuntimeException("kc down"));
        mvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.service_error").value("HEALTH_CHECK_FAILED"));
    }
}
