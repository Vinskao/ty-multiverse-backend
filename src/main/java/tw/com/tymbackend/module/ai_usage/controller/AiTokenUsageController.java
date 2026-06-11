package tw.com.tymbackend.module.ai_usage.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageCreateDTO;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageOverviewDTO;
import tw.com.tymbackend.module.ai_usage.domain.dto.AiTokenUsageSummaryDTO;
import tw.com.tymbackend.module.ai_usage.domain.vo.AiTokenUsage;
import tw.com.tymbackend.module.ai_usage.service.AiTokenUsageService;

@RestController
@RequestMapping("/ai-usage")
public class AiTokenUsageController {

    private final AiTokenUsageService service;
    private final String ingestToken;

    public AiTokenUsageController(
            AiTokenUsageService service,
            @Value("${ai-usage.ingest-token:}") String ingestToken) {
        this.service = service;
        this.ingestToken = ingestToken;
    }

    @PostMapping
    public ResponseEntity<BackendApiResponse<AiTokenUsage>> create(
            @RequestHeader(value = "X-AI-Usage-Token", required = false) String usageToken,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody @Valid AiTokenUsageCreateDTO dto) {
        if (!isAuthorizedIngest(usageToken, authorization)) {
            return ResponseEntity.status(401)
                .body(BackendApiResponse.unauthorized("Unauthorized AI usage ingest"));
        }

        try {
            AiTokenUsage saved = service.createRecord(dto);
            return ResponseEntity.status(201)
                .body(BackendApiResponse.created(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409)
                .body(BackendApiResponse.error(409, "AI usage record already exists"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("儲存 token 用量失敗", e.getMessage()));
        }
    }

    private boolean isAuthorizedIngest(String usageToken, String authorization) {
        if (ingestToken == null || ingestToken.isBlank()) {
            return false;
        }

        if (ingestToken.equals(usageToken)) {
            return true;
        }

        String bearerPrefix = "Bearer ";
        return authorization != null
            && authorization.startsWith(bearerPrefix)
            && ingestToken.equals(authorization.substring(bearerPrefix.length()));
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

    @GetMapping("/overview")
    public ResponseEntity<BackendApiResponse<AiTokenUsageOverviewDTO>> overview(
            @RequestParam(defaultValue = "Asia/Taipei") String timezone) {
        return ResponseEntity.ok(
            BackendApiResponse.success("用量總覽", service.getOverview(timezone)));
    }
}
