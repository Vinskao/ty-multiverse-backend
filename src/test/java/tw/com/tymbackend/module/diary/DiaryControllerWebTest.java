package tw.com.tymbackend.module.diary;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static tw.com.tymbackend.support.TestAuth.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.support.SecuredWebSlice;

@WebMvcTest(DiaryController.class)
@ContextConfiguration(classes = { DiaryController.class, SecurityConfig.class })
@SecuredWebSlice
class DiaryControllerWebTest {
    @Autowired MockMvc mvc;
    @MockBean DiaryService service;

    @Test void anonymousCannotReadImportOrTriggerWork() throws Exception {
        mvc.perform(get("/diary/entries")).andExpect(status().isUnauthorized());
        mvc.perform(get("/diary/entries/id")).andExpect(status().isUnauthorized());
        mvc.perform(get("/diary/context")).andExpect(status().isUnauthorized());
        mvc.perform(post("/diary/check-in")).andExpect(status().isUnauthorized());
        mvc.perform(post("/diary/import").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void ownerComesOnlyFromVerifiedJwtAndPrivateResponsesAreNotCached() throws Exception {
        when(service.checkIn("user-sub")).thenReturn(new DiaryService.Reminder(false, "next", null, null));
        mvc.perform(post("/diary/check-in").param("username", "someone-else").with(user()))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
        verify(service).checkIn("user-sub");
        when(service.entry("user-sub", "other-entry")).thenReturn(Optional.empty());
        mvc.perform(get("/diary/entries/other-entry").with(user())).andExpect(status().isNotFound());
    }

    @Test void internalMachineTokenCannotImpersonateDiaryOwner() throws Exception {
        mvc.perform(post("/diary/check-in").header(INTERNAL_HEADER, INTERNAL_TOKEN)).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test void importUsesJwtSubjectAndReportsInvalidContentWithoutEchoingIt() throws Exception {
        when(service.importMarkdown(eq("user-sub"), any(), anyString(), anyString(), eq("general")))
            .thenThrow(new IllegalArgumentException("Markdown is required"));
        mvc.perform(post("/diary/import").with(user()).contentType("application/json")
            .content("{\"entryDate\":\"2026-10-18\",\"title\":\"T\",\"markdown\":\" \"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Markdown is required"));
    }
}
