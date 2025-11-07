package tw.com.tymbackend.module.people.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.core.message.AsyncResultDTO;
import tw.com.tymbackend.core.service.AsyncResultService;

import java.util.HashMap;
import java.util.Map;

/**
 * People 異步結果查詢控制器
 * 
 * 提供 People 模組異步處理結果的查詢 API，包括檢查結果存在性、獲取處理結果和清理結果。
 * 這個 Controller 專門處理 People 相關的異步操作結果。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/people/result")
public class PeopleResultController {
    
    private static final Logger logger = LoggerFactory.getLogger(PeopleResultController.class);
    
    @Autowired
    private AsyncResultService asyncResultService;
    
    /**
     * 獲取 People 處理結果
     * 
     * GET /tymb/api/people/result/{requestId}
     * 
     * @param requestId 請求ID
     * @return 包含處理結果的 JSON 響應
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<AsyncResultDTO> getPeopleResult(@PathVariable String requestId) {
        try {
            logger.info("📥 查詢 People 異步處理結果: requestId={}", requestId);
            
            AsyncResultDTO result = asyncResultService.getResult(requestId);
            
            if (result != null) {
                logger.info("✅ 成功查詢到 People 處理結果: requestId={}, status={}", 
                    requestId, result.getStatus());
                return ResponseEntity.ok(result);
            } else {
                logger.warn("⚠️ 未找到 People 處理結果: requestId={}", requestId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("❌ 查詢 People 處理結果失敗: requestId={}", requestId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 檢查 People 結果是否存在
     * 
     * GET /tymb/api/people/result/{requestId}/exists
     * 
     * @param requestId 請求ID
     * @return 包含 exists、requestId 和 message 字段的 JSON 響應
     */
    @GetMapping("/{requestId}/exists")
    public ResponseEntity<Map<String, Object>> checkPeopleResultExists(@PathVariable String requestId) {
        try {
            logger.info("📥 檢查 People 結果存在性: requestId={}", requestId);
            
            boolean exists = asyncResultService.resultExists(requestId);
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("exists", exists);
            response.put("message", exists ? "結果存在" : "結果不存在");
            
            logger.debug("✅ People 結果存在性檢查完成: requestId={}, exists={}", requestId, exists);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 檢查 People 結果存在性失敗: requestId={}", requestId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("requestId", requestId);
            errorResponse.put("exists", false);
            errorResponse.put("message", "檢查失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 刪除 People 處理結果
     * 
     * DELETE /tymb/api/people/result/{requestId}
     * 
     * @param requestId 請求ID
     * @return 刪除操作結果
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Map<String, Object>> deletePeopleResult(@PathVariable String requestId) {
        try {
            logger.info("🗑️ 刪除 People 處理結果: requestId={}", requestId);
            
            // 檢查結果是否存在
            boolean exists = asyncResultService.resultExists(requestId);
            
            if (!exists) {
                logger.warn("⚠️ People 處理結果不存在，無法刪除: requestId={}", requestId);
                Map<String, Object> response = new HashMap<>();
                response.put("requestId", requestId);
                response.put("removed", false);
                response.put("message", "結果不存在");
                return ResponseEntity.notFound().build();
            }
            
            // 刪除結果
            asyncResultService.deleteResult(requestId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("removed", true);
            response.put("message", "People 處理結果已成功刪除");
            
            logger.info("✅ 成功刪除 People 處理結果: requestId={}", requestId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ 刪除 People 處理結果失敗: requestId={}", requestId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("requestId", requestId);
            errorResponse.put("removed", false);
            errorResponse.put("message", "刪除失敗: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

