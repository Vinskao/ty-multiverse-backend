package tw.com.tymbackend.module.learn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.tymbackend.module.learn.domain.dto.LearnDtos;
import tw.com.tymbackend.module.learn.service.LearnService;

@RestController
@RequestMapping("/learn")
public class LearnController {
    private final LearnService service;

    public LearnController(LearnService service) {
        this.service = service;
    }

    @GetMapping("/quizzes")
    public ResponseEntity<BackendApiResponse<List<LearnDtos.QuizSummary>>> quizzes() {
        return ResponseEntity.ok(BackendApiResponse.success("題庫列表", service.listQuizzes()));
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<BackendApiResponse<LearnDtos.Quiz>> quiz(
            @PathVariable String quizId, Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("題目", service.getQuiz(quizId, authentication.getName())));
    }

    @PostMapping("/quizzes/{quizId}/attempts")
    public ResponseEntity<BackendApiResponse<LearnDtos.AttemptResult>> submit(
            @PathVariable String quizId, Authentication authentication,
            @RequestBody @Valid LearnDtos.Submission submission) {
        return ResponseEntity.ok(BackendApiResponse.success("作答完成", service.submit(quizId, authentication.getName(), submission)));
    }

    @GetMapping("/attempts")
    public ResponseEntity<BackendApiResponse<List<LearnDtos.AttemptSummary>>> history(Authentication authentication) {
        return ResponseEntity.ok(BackendApiResponse.success("作答紀錄", service.history(authentication.getName())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<BackendApiResponse<Void>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(BackendApiResponse.error(400, exception.getMessage()));
    }
}
