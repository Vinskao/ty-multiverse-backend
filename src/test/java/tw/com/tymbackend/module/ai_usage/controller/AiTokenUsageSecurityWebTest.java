package tw.com.tymbackend.module.ai_usage.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.ai_usage.domain.vo.AiTokenUsage;
import tw.com.tymbackend.module.ai_usage.service.AiTokenUsageService;

/**
 * Web slice with the real SecurityConfig filter chain (no DB / Keycloak / Redis).
 * ContextConfiguration avoids TYMBackendApplication, whose websocket exporter needs a real container.
 */
@WebMvcTest(AiTokenUsageController.class)
@ContextConfiguration(classes = { AiTokenUsageController.class, SecurityConfig.class })
@TestPropertySource(properties = {
        "keycloak.auth-server-url=http://localhost:1/auth",
        "keycloak.realm=test",
        "ai-usage.ingest-token=ingest-secret",
        "project.env=platform"
})
class AiTokenUsageSecurityWebTest {

    private static final String VALID_BODY = """
            {"aiProvider":"anthropic","modelName":"opus","inputTokens":1,"outputTokens":2}
            """;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AiTokenUsageService service;

    @Test
    void post_Should_Return401_When_NoIngestToken() throws Exception {
        mvc.perform(post("/ai-usage").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());
        verify(service, never()).createRecord(any());
    }

    @Test
    void post_Should_Return401_When_IngestTokenWrong() throws Exception {
        mvc.perform(post("/ai-usage").header("X-AI-Usage-Token", "nope")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void post_Should_Return201_When_HeaderTokenValid() throws Exception {
        when(service.createRecord(any())).thenReturn(new AiTokenUsage());
        mvc.perform(post("/ai-usage").header("X-AI-Usage-Token", "ingest-secret")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());
    }

    /**
     * Characterization: the controller accepts "Authorization: Bearer <ingest-token>", but the
     * oauth2 resource server parses every Bearer header as a JWT and answers 401 first, so that
     * path is unreachable through the real filter chain. Clients must use X-AI-Usage-Token.
     * If this is ever fixed, flip this test to expect 201.
     */
    @Test
    void post_Should_Return401_When_IngestTokenSentAsBearer_BecauseResourceServerRejectsNonJwt() throws Exception {
        mvc.perform(post("/ai-usage").header("Authorization", "Bearer ingest-secret")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());
        verify(service, never()).createRecord(any());
    }

    @Test
    void post_Should_Return409_When_RecordAlreadyExists() throws Exception {
        when(service.createRecord(any())).thenThrow(new DataIntegrityViolationException("dup"));
        mvc.perform(post("/ai-usage").header("X-AI-Usage-Token", "ingest-secret")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void get_Should_BePublic_When_NoCredentials() throws Exception {
        when(service.getDailySummary(any(), any())).thenReturn(List.of());
        mvc.perform(get("/ai-usage/daily")).andExpect(status().isOk());
    }

    @Test
    void monthly_Should_BePublic() throws Exception {
        when(service.getMonthlySummary(any(), any())).thenReturn(List.of());
        mvc.perform(get("/ai-usage/monthly").param("months", "3")).andExpect(status().isOk());
    }

    @Test
    void summary_Should_ReturnTodayAndThisMonth() throws Exception {
        when(service.getDailySummary(any(), any())).thenReturn(List.of());
        when(service.getMonthlySummary(any(), any())).thenReturn(List.of());
        mvc.perform(get("/ai-usage/summary"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.today").isArray())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.thisMonth").isArray());
    }

    @Test
    void overview_Should_UseRequestedTimezone() throws Exception {
        mvc.perform(get("/ai-usage/overview").param("timezone", "UTC")).andExpect(status().isOk());
        verify(service).getOverview("UTC");
    }

    @Test
    void overview_Should_DefaultToTaipei() throws Exception {
        mvc.perform(get("/ai-usage/overview")).andExpect(status().isOk());
        verify(service).getOverview("Asia/Taipei");
    }
}
