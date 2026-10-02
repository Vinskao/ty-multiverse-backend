package tw.com.tymbackend.module.learn.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.user;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.learn.domain.dto.LearnDtos;
import tw.com.tymbackend.module.learn.service.LearnService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** All 11 LearnController endpoints. The user id handed to the service must be the JWT subject. */
@WebMvcTest(LearnController.class)
@ContextConfiguration(classes = { LearnController.class, SecurityConfig.class })
@SecuredWebSlice
class LearnControllerWebTest {

    private static final String UID = "user-sub";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LearnService service;

    @Test
    void topics_Should_ListForCurrentUser() throws Exception {
        when(service.listTopics(UID)).thenReturn(List.of());
        mvc.perform(get("/learn/topics").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(service).listTopics(UID);
    }

    @Test
    void quizzes_Should_BeAliasOfTopics() throws Exception {
        mvc.perform(get("/learn/quizzes").with(user())).andExpect(status().isOk());
        verify(service).listTopics(UID);
    }

    @Test
    void session_Should_StartOrResume_WithDisplayNameFromClaim() throws Exception {
        mvc.perform(post("/learn/topics/q1/session").with(user())).andExpect(status().isOk());
        verify(service).startOrResume("q1", UID, "alice");
    }

    @Test
    void answer_Should_SaveForCurrentUser() throws Exception {
        mvc.perform(post("/learn/topics/q1/session/answers").with(user())
                .contentType(MediaType.APPLICATION_JSON).content("{\"questionId\":5,\"selectedOption\":\"B\"}"))
                .andExpect(status().isOk());
        verify(service).saveAnswer(eq("q1"), eq(UID), eq(new LearnDtos.AnswerInput(5L, "B")));
    }

    @Test
    void answer_Should_Return400_When_BodyMalformed() throws Exception {
        mvc.perform(post("/learn/topics/q1/session/answers").with(user())
                .contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_Should_AcceptEmptyBody() throws Exception {
        mvc.perform(post("/learn/topics/q1/attempts").with(user())).andExpect(status().isOk());
        verify(service).submit("q1", UID, null);
    }

    @Test
    void submit_Should_PassSubmission() throws Exception {
        mvc.perform(post("/learn/topics/q1/attempts").with(user())
                .contentType(MediaType.APPLICATION_JSON).content("{\"durationSeconds\":30,\"answers\":{\"5\":\"B\"}}"))
                .andExpect(status().isOk());
        verify(service).submit(eq("q1"), eq(UID), any(LearnDtos.Submission.class));
    }

    @Test
    void submit_Should_Return400_When_ServiceRejectsInput() throws Exception {
        when(service.submit(any(), any(), any())).thenThrow(new IllegalArgumentException("unknown quiz"));
        mvc.perform(post("/learn/topics/nope/attempts").with(user()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("unknown quiz"));
    }

    @Test
    void review_Should_ScopeToCurrentUser() throws Exception {
        mvc.perform(get("/learn/attempts/9/review").with(user())).andExpect(status().isOk());
        verify(service).review(9L, UID);
    }

    @Test
    void review_Should_Return403_When_AttemptBelongsToSomeoneElse() throws Exception {
        when(service.review(9L, UID)).thenThrow(new AccessDeniedException("not yours"));
        mvc.perform(get("/learn/attempts/9/review").with(user()))
                .andExpect(status().isForbidden());
    }

    @Test
    void review_Should_Return400_When_AttemptIdNotNumeric() throws Exception {
        mvc.perform(get("/learn/attempts/abc/review").with(user())).andExpect(status().isBadRequest());
    }

    @Test
    void scorecard_Should_ScopeToCurrentUser() throws Exception {
        mvc.perform(get("/learn/topics/q1/scorecard").with(user())).andExpect(status().isOk());
        verify(service).scorecard("q1", UID);
    }

    @Test
    void history_Should_ListCurrentUsersAttempts() throws Exception {
        mvc.perform(get("/learn/attempts").with(user())).andExpect(status().isOk());
        verify(service).history(UID);
    }

    @Test
    void profile_Should_ReturnIdentity() throws Exception {
        when(service.profile(UID, "alice")).thenReturn(new LearnDtos.Profile(UID, false));
        mvc.perform(get("/learn/profile").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(UID))
                .andExpect(jsonPath("$.data.mentor").value(false));
    }

    @Test
    void ranking_Should_ScopeToCurrentUser() throws Exception {
        mvc.perform(get("/learn/topics/q1/ranking").with(user())).andExpect(status().isOk());
        verify(service).ranking("q1", UID);
    }

    @Test
    void mentorOverview_Should_Return200_ForMentor() throws Exception {
        mvc.perform(get("/learn/mentor/overview").with(user())).andExpect(status().isOk());
        verify(service).mentorOverview(UID, "alice");
    }

    @Test
    void mentorOverview_Should_Return403_When_NotMentor() throws Exception {
        when(service.mentorOverview(UID, "alice")).thenThrow(new AccessDeniedException("mentors only"));
        mvc.perform(get("/learn/mentor/overview").with(user())).andExpect(status().isForbidden());
    }
}
