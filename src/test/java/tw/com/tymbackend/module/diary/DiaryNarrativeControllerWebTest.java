package tw.com.tymbackend.module.diary;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.com.tymbackend.support.TestAuth.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.support.SecuredWebSlice;

// sha256("agent-key") = the configured hash; the plain key never appears in configuration.
@WebMvcTest({DiaryNarrativeController.class, DiaryController.class})
@ContextConfiguration(classes = { DiaryNarrativeController.class, DiaryController.class, SecurityConfig.class })
@SecuredWebSlice
@TestPropertySource(properties = {
    "diary.agent-key-sha256=112a8d31e2b0fb3f207031fef32f7a7245f787b4d4e85b45f65aa5b83435368c",
    "diary.agent-owner=owner-sub"})
class DiaryNarrativeControllerWebTest {
    static final String KEY = "agent-key";
    @Autowired MockMvc mvc;
    @MockBean DiaryNarratives narratives;
    @MockBean DiaryService service;

    @Test void agentReadsWorkAndWritesNarrativesForTheConfiguredOwnerOnly() throws Exception {
        when(narratives.work("owner-sub")).thenReturn(new DiaryNarratives.Work(List.of(), List.of()));
        mvc.perform(get("/diary/agent/work").header("X-Diary-Agent-Key", KEY))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
        mvc.perform(put("/diary/agent/narratives/entry/e1").header("X-Diary-Agent-Key", KEY).contentType("application/json")
            .content("{\"narrative\":\"text\",\"sourceHash\":\"h\",\"model\":\"claude-opus-5-5\"}")).andExpect(status().isOk());
        verify(narratives).save("owner-sub", "entry", "e1", "text", "h", "claude-opus-5-5");
    }

    @Test void wrongMissingOrJwtCredentialsCannotUseTheAgentEndpoints() throws Exception {
        mvc.perform(get("/diary/agent/work")).andExpect(status().isUnauthorized());
        mvc.perform(get("/diary/agent/work").header("X-Diary-Agent-Key", "wrong")).andExpect(status().isUnauthorized());
        mvc.perform(get("/diary/agent/work").with(user())).andExpect(status().isUnauthorized());
        mvc.perform(put("/diary/agent/narratives/entry/e1").header(INTERNAL_HEADER, INTERNAL_TOKEN).contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(narratives);
    }

    @Test void theAgentKeyCannotReadTheOwnersDiaryApi() throws Exception {
        mvc.perform(get("/diary/entries").header("X-Diary-Agent-Key", KEY)).andExpect(status().isUnauthorized());
        mvc.perform(get("/diary/narratives").header("X-Diary-Agent-Key", KEY)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void ownerReadsNarrativesWithTheirJwt() throws Exception {
        when(narratives.view("user-sub")).thenReturn(new DiaryNarratives.View(Map.of(), List.of()));
        mvc.perform(get("/diary/narratives").with(user())).andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store"));
        verify(narratives).view("user-sub");
    }

    @Test void invalidNarrativesAreRejectedWithoutEchoingThem() throws Exception {
        doThrow(new IllegalArgumentException("Source changed; regenerate")).when(narratives).save(any(), any(), any(), any(), any(), any());
        mvc.perform(put("/diary/agent/narratives/entry/e1").header("X-Diary-Agent-Key", KEY).contentType("application/json")
            .content("{\"narrative\":\"private text\",\"sourceHash\":\"old\",\"model\":\"m\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Source changed; regenerate"));
    }
}
