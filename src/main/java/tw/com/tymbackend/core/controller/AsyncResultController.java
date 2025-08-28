package tw.com.tymbackend.core.controller;

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
 * 異步結果查詢控制器
 * 
 * 提供異步處理結果的查詢 API，包括檢查結果存在性和獲取處理結果。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/request-status")
public class AsyncResultController {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncResultController.class);
    
    @Autowired
    private AsyncResultService asyncResultService;
    
    /**
     * 檢查結果是否存在
     * 
     * GET /tymb/api/request-status/{requestId}/exists
     * 
     * @param requestId 請求ID
     * @return 包含 exists 字段的 JSON 響應
     */
    @GetMapping("/{requestId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkResultExists(@PathVariable String requestId) {
        try {
            logger.info("檢查異步處理結果存在性: requestId={}", requestId);
            
            boolean exists = asyncResultService.resultExists(requestId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            
            logger.debug("異步處理結果存在性檢查完成: requestId={}, exists={}", requestId, exists);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("檢查異步處理結果存在性失敗: requestId={}", requestId, e);
            Map<String, Boolean> errorResponse = new HashMap<>();
            errorResponse.put("exists", false);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 獲取處理結果
     * 
     * GET /tymb/api/request-status/{requestId}
     * 
     * @param requestId 請求ID
     * @return 包含處理結果的 JSON 響應
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<AsyncResultDTO> getResult(@PathVariable String requestId) {
        try {
            logger.info("查詢異步處理結果: requestId={}", requestId);
            
            AsyncResultDTO result = asyncResultService.getResult(requestId);
            
            if (result != null) {
                logger.debug("成功查詢到異步處理結果: requestId={}, status={}", 
                    requestId, result.getStatus());
                return ResponseEntity.ok(result);
            } else {
                logger.debug("未找到異步處理結果: requestId={}", requestId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("查詢異步處理結果失敗: requestId={}", requestId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 刪除處理結果
     * 
     * DELETE /tymb/api/request-status/{requestId}
     * 
     * @param requestId 請求ID
     * @return 刪除操作結果
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<Map<String, String>> deleteResult(@PathVariable String requestId) {
        try {
            logger.info("刪除異步處理結果: requestId={}", requestId);
            
            asyncResultService.deleteResult(requestId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "異步處理結果已刪除");
            response.put("requestId", requestId);
            
            logger.debug("成功刪除異步處理結果: requestId={}", requestId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("刪除異步處理結果失敗: requestId={}", requestId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "刪除異步處理結果失敗");
            errorResponse.put("requestId", requestId);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
}
