package tw.com.tymbackend.module.ai_usage.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageCreateDTO;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageSummaryDTO;
import tw.com.tymbackend.module.ai_usage.domain.vo.AiTokenUsage;
import tw.com.tymbackend.module.ai_usage.service.AiTokenUsageService;

@RestController
@RequestMapping("/ai-usage")
public class AiTokenUsageController {

    private final AiTokenUsageService service;

    public AiTokenUsageController(AiTokenUsageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BackendApiResponse<AiTokenUsage>> create(
            @RequestBody @Valid AiTokenUsageCreateDTO dto) {
        try {
            AiTokenUsage saved = service.createRecord(dto);
            return ResponseEntity.status(201)
                .body(BackendApiResponse.created(saved));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("儲存 token 用量失敗", e.getMessage()));
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<BackendApiResponse<List<AiTokenUsageSummaryDTO>>> daily(
            @RequestParam(defaultValue = "30") int days) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1);
        return ResponseEntity.ok(
            BackendApiResponse.success("每日統計", service.getDailySummary(from, to)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<BackendApiResponse<List<AiTokenUsageSummaryDTO>>> monthly(
            @RequestParam(defaultValue = "12") int months) {
        YearMonth to = YearMonth.now();
        YearMonth from = to.minusMonths(months - 1);
        return ResponseEntity.ok(
            BackendApiResponse.success("每月統計", service.getMonthlySummary(from, to)));
    }

    @GetMapping("/summary")
    public ResponseEntity<BackendApiResponse<Map<String, Object>>> summary() {
        List<AiTokenUsageSummaryDTO> today = service.getDailySummary(LocalDate.now(), LocalDate.now());
        List<AiTokenUsageSummaryDTO> thisMonth = service.getMonthlySummary(YearMonth.now(), YearMonth.now());
        Map<String, Object> result = Map.of("today", today, "thisMonth", thisMonth);
        return ResponseEntity.ok(BackendApiResponse.success("統計摘要", result));
    }
}
