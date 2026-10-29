package tw.com.tymbackend.module.learn.controller;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.tymbackend.module.learn.domain.dto.LearnDtos;
import tw.com.tymbackend.module.learn.service.LearnService;

/**
 * Answering-phase endpoints ({@code /session}, {@code /session/answers}) never return correctness
 * data; review-phase endpoints ({@code /review}, {@code /scorecard}) require a submitted attempt.
 */
@RestController
@RequestMapping("/learn")
public class LearnController {
    private static final String GUEST_HEADER = "X-Learn-Guest-Id";
    private final LearnService service;

    public LearnController(LearnService service) {
        this.service = service;
    }

    /** Public learning is available without an account. Guest progress is namespaced and keyed by a browser UUID. */
    @GetMapping("/public/topics")
    public ResponseEntity<BackendApiResponse<List<LearnDtos.TopicSummary>>> publicTopics(
            @RequestHeader(value = GUEST_HEADER, required = false) String guestId) {
        String userId = guestUserId(guestId);
        return ResponseEntity.ok(BackendApiResponse.success("topics", service.listTopics(userId)));
    }

    @PostMapping("/public/topics/{quizId}/session")
    public ResponseEntity<BackendApiResponse<LearnDtos.Session>> publicSession(
            @PathVariable String quizId, @RequestHeader(value = GUEST_HEADER, required = false) String guestId) {
        String userId = guestUserId(guestId);
        return ResponseEntity.ok(BackendApiResponse.success("start learning",
                service.startOrResume(quizId, userId, "訪客")));
    }

    @PostMapping("/public/topics/{quizId}/session/answers")
    public ResponseEntity<BackendApiResponse<LearnDtos.AnswerAck>> publicAnswer(
            @PathVariable String quizId, @RequestHeader(value = GUEST_HEADER, required = false) String guestId,
            @RequestBody @Valid LearnDtos.AnswerInput input) {
        return ResponseEntity.ok(BackendApiResponse.success("saved",
                service.saveAnswer(quizId, guestUserId(guestId), input)));
    }

    @PostMapping("/public/topics/{quizId}/attempts")
    public ResponseEntity<BackendApiResponse<LearnDtos.Review>> publicSubmit(
            @PathVariable String quizId, @RequestHeader(value = GUEST_HEADER, required = false) String guestId,
            @RequestBody(required = false) LearnDtos.Submission submission) {
        return ResponseEntity.ok(BackendApiResponse.success("submitted",
                withoutRanking(service.submit(quizId, guestUserId(guestId), submission))));
    }

    @GetMapping("/public/attempts/{attemptId}/review")
    public ResponseEntity<BackendApiResponse<LearnDtos.Review>> publicReview(
            @PathVariable Long attemptId, @RequestHeader(value = GUEST_HEADER, required = false) String guestId) {
        return ResponseEntity.ok(BackendApiResponse.success("review",
                withoutRanking(service.review(attemptId, guestUserId(guestId)))));
    }

    @GetMapping("/public/topics/{quizId}/scorecard")
    public ResponseEntity<BackendApiResponse<LearnDtos.Scorecard>> publicScorecard(
            @PathVariable String quizId, @RequestHeader(value = GUEST_HEADER, required = false) String guestId) {
        return ResponseEntity.ok(BackendApiResponse.success("scorecard",
                service.scorecard(quizId, guestUserId(guestId))));
    }

    @GetMapping("/public/attempts")
    public ResponseEntity<BackendApiResponse<List<LearnDtos.AttemptSummary>>> publicHistory(
            @RequestHeader(value = GUEST_HEADER, required = false) String guestId) {
        return ResponseEntity.ok(BackendApiResponse.success("history", service.history(guestUserId(guestId))));
    }

    private LearnDtos.Review withoutRanking(LearnDtos.Review review) {
        return new LearnDtos.Review(review.attemptId(), review.quizId(), review.quizTitle(), review.score(),
                review.totalQuestions(), review.durationSeconds(), review.submittedAt(), review.items(), null);
    }

    private String guestUserId(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("A valid guest ID is required");
        try {
            return "guest:" + UUID.fromString(raw.trim()).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("A valid guest ID is required");
        }
    }

    /** Sidebar: every topic plus this candidate's progress on it. */
    @GetMapping("/topics")
    public ResponseEntity<BackendApiResponse<List<LearnDtos.TopicSummary>>> topics(Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("主題列表", service.listTopics(authentication.getName())));
    }

    /** Kept so an older cached page keeps working; same payload as {@code /topics}. */
    @GetMapping("/quizzes")
    public ResponseEntity<BackendApiResponse<List<LearnDtos.TopicSummary>>> quizzes(Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("主題列表", service.listTopics(authentication.getName())));
    }

    /** Starts a freshly shuffled round, or resumes the unfinished one. */
    @PostMapping("/topics/{quizId}/session")
    public ResponseEntity<BackendApiResponse<LearnDtos.Session>> session(
            @PathVariable String quizId, Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("開始作答",
            service.startOrResume(quizId, authentication.getName(), displayName(authentication))));
    }

    /** Autosaves one choice mid-round. */
    @PostMapping("/topics/{quizId}/session/answers")
    public ResponseEntity<BackendApiResponse<LearnDtos.AnswerAck>> answer(
            @PathVariable String quizId, Authentication authentication,
            @RequestBody @Valid LearnDtos.AnswerInput input) {
        return ResponseEntity.ok(BackendApiResponse.success("已儲存",
            service.saveAnswer(quizId, authentication.getName(), input)));
    }

    /** Closes the round and returns the review; rejected while any question is unanswered. */
    @PostMapping("/topics/{quizId}/attempts")
    public ResponseEntity<BackendApiResponse<LearnDtos.Review>> submit(
            @PathVariable String quizId, Authentication authentication,
            @RequestBody(required = false) LearnDtos.Submission submission) {
        return ResponseEntity.ok(BackendApiResponse.success("作答完成",
            service.submit(quizId, authentication.getName(), submission)));
    }

    @GetMapping("/attempts/{attemptId}/review")
    public ResponseEntity<BackendApiResponse<LearnDtos.Review>> review(
            @PathVariable Long attemptId, Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("檢討",
            service.review(attemptId, authentication.getName())));
    }

    /** Cumulative per-question tally across every completed round of a topic. */
    @GetMapping("/topics/{quizId}/scorecard")
    public ResponseEntity<BackendApiResponse<LearnDtos.Scorecard>> scorecard(
            @PathVariable String quizId, Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("累計成績單",
            service.scorecard(quizId, authentication.getName())));
    }

    @GetMapping("/attempts")
    public ResponseEntity<BackendApiResponse<List<LearnDtos.AttemptSummary>>> history(
            Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("作答紀錄", service.history(authentication.getName())));
    }

    /** Whether this account opens the page as a learner or straight into mentor mode. */
    @GetMapping("/profile")
    public ResponseEntity<BackendApiResponse<LearnDtos.Profile>> profile(Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("身分", service.profile(authentication.getName(), displayName(authentication))));
    }

    /** Cohort leaderboard for a topic — aggregates only, readable by every signed-in learner. */
    @GetMapping("/topics/{quizId}/ranking")
    public ResponseEntity<BackendApiResponse<LearnDtos.Ranking>> ranking(
            @PathVariable String quizId, Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("排行榜",
            service.ranking(quizId, authentication.getName())));
    }

    /** Mentor mode: every account's progress across every topic. Mentor accounts only. */
    @GetMapping("/mentor/overview")
    public ResponseEntity<BackendApiResponse<LearnDtos.MentorOverview>> mentorOverview(
            Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("全班作答狀況",
            service.mentorOverview(authentication.getName(), displayName(authentication))));
    }

    /**
     * Keycloak's principal name is the subject (a UUID). Progress keys off it, but the leaderboard
     * and the mentor allow-list need the human username, so it is read from the token claim.
     */
    private String displayName(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken token) {
            String username = token.getToken().getClaimAsString("preferred_username");
            if (username != null && !username.isBlank()) return username;
        }
        return authentication.getName();
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<BackendApiResponse<Void>> forbidden(AccessDeniedException exception) {
        return ResponseEntity.status(403).body(BackendApiResponse.error(403, exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<BackendApiResponse<Void>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(BackendApiResponse.error(400, exception.getMessage()));
    }
}
