package tw.com.tymbackend.module.ckeditor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.user;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;
import tw.com.tymbackend.module.ckeditor.service.EditContentService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** Both ckeditor endpoints. */
@WebMvcTest(FileUploadController.class)
@ContextConfiguration(classes = { FileUploadController.class, SecurityConfig.class })
@SecuredWebSlice
class FileUploadControllerWebTest {

    private static final String BODY = "{\"editor\":\"main\",\"content\":\"hello\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EditContentService service;

    private static CompletableFuture<Optional<EditContentVO>> stored(String content) {
        return CompletableFuture.completedFuture(Optional.of(new EditContentVO("main", content)));
    }

    @Test
    void save_Should_Return401_When_NoLoggedInUser() throws Exception {
        // controller-level guard: independent of the (currently broken) SecurityConfig method matchers
        mvc.perform(post("/ckeditor/save-content").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        verify(service, never()).saveContent(any());
    }

    @Test
    void save_Should_Persist_When_ContentChanged() throws Exception {
        when(service.getContent("main")).thenReturn(stored("old"));
        mvc.perform(post("/ckeditor/save-content").with(user()).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().string("Content saved successfully!"));
        verify(service).saveContent(any());
    }

    @Test
    void save_Should_Skip_When_ContentUnchanged() throws Exception {
        when(service.getContent("main")).thenReturn(stored("hello"));
        mvc.perform(post("/ckeditor/save-content").with(user()).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().string("No changes detected. Draft saved to session."));
        verify(service, never()).saveContent(any());
    }

    @Test
    void save_Should_Create_When_NoContentFoundYet() throws Exception {
        when(service.getContent("main")).thenThrow(new RuntimeException("No content found for main"));
        mvc.perform(post("/ckeditor/save-content").with(user()).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().string("Content created successfully!"));
        verify(service).saveContent(any());
    }

    @Test
    void save_Should_Return500_When_OtherRuntimeError() throws Exception {
        when(service.getContent("main")).thenThrow(new RuntimeException("db down"));
        mvc.perform(post("/ckeditor/save-content").with(user()).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getContent_Should_ReturnStored() throws Exception {
        when(service.getContent("main")).thenReturn(stored("hello"));
        mvc.perform(post("/ckeditor/get-content").contentType(MediaType.APPLICATION_JSON).content("{\"editor\":\"main\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("hello"));
    }

    @Test
    void getContent_Should_ReturnEmptyContent_When_NothingStored() throws Exception {
        when(service.getContent("main")).thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        mvc.perform(post("/ckeditor/get-content").contentType(MediaType.APPLICATION_JSON).content("{\"editor\":\"main\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(""));
    }

    @Test
    void getContent_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getContent("main")).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/ckeditor/get-content").contentType(MediaType.APPLICATION_JSON).content("{\"editor\":\"main\"}"))
                .andExpect(status().isInternalServerError());
    }
}
