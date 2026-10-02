package tw.com.tymbackend.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.support.SecuredWebSlice;

/** All 3 KeycloakController endpoints; Keycloak itself is a mocked RestTemplate. */
@WebMvcTest(KeycloakController.class)
@ContextConfiguration(classes = { KeycloakController.class, SecurityConfig.class })
@SecuredWebSlice
@TestPropertySource(properties = {
        "url.frontend=http://front.test/app",
        "app.url.address=http://back.test",
        "keycloak.clientId=client",
        "keycloak.credentials.secret=shh"
})
class KeycloakControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RestTemplate rest;

    @SuppressWarnings("unchecked")
    private void stub(String urlPart, HttpMethod method, Map<String, Object> body) {
        when(rest.exchange(contains(urlPart), eq(method), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(body));
    }

    // ---- GET /keycloak/redirect ---------------------------------------

    @Test
    void redirect_Should_SendUserToFrontendWithTokens() throws Exception {
        stub("/token", HttpMethod.POST, Map.of("access_token", "at", "refresh_token", "rt"));
        stub("/userinfo", HttpMethod.GET, Map.of("preferred_username", "alice", "email", "a@x.io"));
        mvc.perform(get("/keycloak/redirect").param("code", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("http://front.test/app?username=alice&email=a%40x.io*token=at*refreshToken=rt"));
    }

    @Test
    void redirect_Should_Return500_When_TokenMissingFromKeycloakReply() throws Exception {
        stub("/token", HttpMethod.POST, Map.of("access_token", "at"));
        mvc.perform(get("/keycloak/redirect").param("code", "abc")).andExpect(status().isInternalServerError());
    }

    @Test
    void redirect_Should_Return500_When_UsernameMissing() throws Exception {
        stub("/token", HttpMethod.POST, Map.of("access_token", "at", "refresh_token", "rt"));
        stub("/userinfo", HttpMethod.GET, Map.of("email", "a@x.io"));
        mvc.perform(get("/keycloak/redirect").param("code", "abc")).andExpect(status().isInternalServerError());
    }

    @Test
    void redirect_Should_Return400_When_CodeMissing() throws Exception {
        mvc.perform(get("/keycloak/redirect")).andExpect(status().isBadRequest());
    }

    // ---- POST /keycloak/logout ----------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void logout_Should_Return200_When_KeycloakAccepts() throws Exception {
        when(rest.exchange(contains("/logout"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(""));
        mvc.perform(post("/keycloak/logout").param("refreshToken", "rt")).andExpect(status().isOk());
    }

    @Test
    @SuppressWarnings("unchecked")
    void logout_Should_Return500_When_KeycloakFails() throws Exception {
        when(rest.exchange(contains("/logout"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        mvc.perform(post("/keycloak/logout").param("refreshToken", "rt")).andExpect(status().isInternalServerError());
    }

    @Test
    void logout_Should_Return400_When_RefreshTokenMissing() throws Exception {
        mvc.perform(post("/keycloak/logout")).andExpect(status().isBadRequest());
    }

    // ---- POST /keycloak/introspect ------------------------------------

    @Test
    void introspect_Should_ReturnResult_When_TokenActive() throws Exception {
        stub("/token/introspect", HttpMethod.POST, Map.of("active", true, "sub", "u1"));
        mvc.perform(post("/keycloak/introspect").param("token", "t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.sub").value("u1"));
    }

    @Test
    void introspect_Should_RefreshAndMarkActive_When_TokenExpiredButRefreshValid() throws Exception {
        stub("/token/introspect", HttpMethod.POST, Map.of("active", false));
        stub("/openid-connect/token", HttpMethod.POST, Map.of("access_token", "new-at"));
        // the more specific introspect stub must win for the first call; re-stub order handled below
        when(rest.exchange(org.mockito.ArgumentMatchers.endsWith("/openid-connect/token"), eq(HttpMethod.POST),
                any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Map.of("access_token", "new-at")));
        mvc.perform(post("/keycloak/introspect").param("token", "t").param("refreshToken", "rt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.access_token").value("new-at"));
    }

    @Test
    void introspect_Should_Return401_When_InactiveAndNoRefreshToken() throws Exception {
        stub("/token/introspect", HttpMethod.POST, Map.of("active", false));
        mvc.perform(post("/keycloak/introspect").param("token", "t")).andExpect(status().isUnauthorized());
    }

    @Test
    void introspect_Should_Return500_When_KeycloakUnreachable() throws Exception {
        when(rest.exchange(contains("/introspect"), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class))).thenThrow(new RuntimeException("down"));
        mvc.perform(post("/keycloak/introspect").param("token", "t")).andExpect(status().isInternalServerError());
    }

    @Test
    void introspect_Should_Return400_When_TokenMissing() throws Exception {
        mvc.perform(post("/keycloak/introspect")).andExpect(status().isBadRequest());
    }
}
