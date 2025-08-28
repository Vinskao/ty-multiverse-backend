package tw.com.tymbackend.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.core.service.AsyncResultSimulatorService;

import java.util.HashMap;
import java.util.Map;

/**
 * 異步測試控制器
 * 
 * 用於手動觸發異步處理模擬，方便測試異步結果查詢功能。
 * 僅在開發環境中使用，生產環境應移除。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/test/async")
public class AsyncTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTestController.class);
    
    @Autowired
    private AsyncResultSimulatorService asyncResultSimulatorService;
    
    /**
     * 手動觸發傷害計算模擬
     * 
     * POST /tymb/api/test/async/damage-calculation
     * 
     * @param request 包含 requestId 和 characterName 的請求
     * @return 觸發結果
     */
    @PostMapping("/damage-calculation")
    public ResponseEntity<Map<String, String>> triggerDamageCalculation(@RequestBody Map<String, String> request) {
        try {
            String requestId = request.get("requestId");
            String characterName = request.get("characterName");
            
            if (requestId == null || characterName == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "缺少必要參數: requestId 和 characterName");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            logger.info("手動觸發傷害計算模擬: requestId={}, characterName={}", requestId, characterName);
            
            asyncResultSimulatorService.triggerDamageCalculation(requestId, characterName);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "傷害計算模擬已觸發");
            response.put("requestId", requestId);
            response.put("characterName", characterName);
            response.put("status", "processing");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("觸發傷害計算模擬失敗", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "觸發失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 手動觸發角色列表獲取模擬
     * 
     * POST /tymb/api/test/async/people-get-all
     * 
     * @param request 包含 requestId 的請求
     * @return 觸發結果
     */
    @PostMapping("/people-get-all")
    public ResponseEntity<Map<String, String>> triggerPeopleGetAll(@RequestBody Map<String, String> request) {
        try {
            String requestId = request.get("requestId");
            
            if (requestId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "缺少必要參數: requestId");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            logger.info("手動觸發角色列表獲取模擬: requestId={}", requestId);
            
            asyncResultSimulatorService.triggerPeopleGetAll(requestId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "角色列表獲取模擬已觸發");
            response.put("requestId", requestId);
            response.put("status", "processing");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("觸發角色列表獲取模擬失敗", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "觸發失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 生成測試用的 UUID
     * 
     * GET /tymb/api/test/async/generate-uuid
     * 
     * @return 生成的 UUID
     */
    @GetMapping("/generate-uuid")
    public ResponseEntity<Map<String, String>> generateUuid() {
        try {
            String uuid = java.util.UUID.randomUUID().toString();
            
            Map<String, String> response = new HashMap<>();
            response.put("uuid", uuid);
            response.put("message", "UUID 生成成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("生成 UUID 失敗", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "生成 UUID 失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
