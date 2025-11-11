package tw.com.tymbackend.core.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.core.message.AsyncResultDTO;
import tw.com.tymbackend.core.service.AsyncResultService;
import tw.com.ty.common.response.BackendApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 異步結果查詢控制器
 *
 * 提供通用異步處理結果的查詢 API，包括檢查結果存在性、獲取處理結果和清理結果。
 * 這個 Controller 處理所有模組的異步操作結果，不局限於特定模組。
 *
 * 支持的數據類型：
 * - People (個人資料)
 * - Weapon (武器資料)
 * - DamageCalculation (傷害計算)
 * - 以及其他異步操作結果
 *
 * @author TY Backend Team
 * @version 2.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/async/result")
public class AsyncResultController {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncResultController.class);
    
    @Autowired
    private AsyncResultService asyncResultService;
    
    /**
     * 獲取異步處理結果
     *
     * GET /tymb/api/async/result/{requestId}
     *
     * 支持的結果類型：
     * - People (個人資料)
     * - Weapon (武器資料)
     * - DamageCalculation (傷害計算)
     * - 以及其他異步操作結果
     *
     * @param requestId 請求ID
     * @return 包含處理結果的 JSON 響應
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<BackendApiResponse<AsyncResultDTO>> getAsyncResult(@PathVariable String requestId) {
        try {
            logger.info("📥 查詢異步處理結果: requestId={}", requestId);

            AsyncResultDTO result = asyncResultService.getResult(requestId);

            if (result != null) {
                logger.info("✅ 成功查詢到異步處理結果: requestId={}, status={}",
                    requestId, result.getStatus());
                return ResponseEntity.ok(BackendApiResponse.success("异步结果查询成功", result));
            } else {
                logger.warn("⚠️ 未找到異步處理結果: requestId={}", requestId);
                return ResponseEntity.status(404)
                    .body(BackendApiResponse.notFound("未找到异步处理结果"));
            }

        } catch (Exception e) {
            logger.error("❌ 查詢異步處理結果失敗: requestId={}", requestId, e);
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("异步结果查询失败", e.getMessage()));
        }
    }
    
    /**
     * 檢查異步結果是否存在
     *
     * GET /tymb/api/async/result/{requestId}/exists
     *
     * @param requestId 請求ID
     * @return 包含 exists、requestId 和 message 字段的 JSON 響應
     */
    @GetMapping("/{requestId}/exists")
    public ResponseEntity<BackendApiResponse<Map<String, Object>>> checkAsyncResultExists(@PathVariable String requestId) {
        try {
            logger.info("📥 檢查異步結果存在性: requestId={}", requestId);

            boolean exists = asyncResultService.resultExists(requestId);
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("exists", exists);
            data.put("message", exists ? "結果存在" : "結果不存在");

            logger.debug("✅ 異步結果存在性檢查完成: requestId={}, exists={}", requestId, exists);
            return ResponseEntity.ok(BackendApiResponse.success("异步结果存在性检查完成", data));

        } catch (Exception e) {
            logger.error("❌ 檢查異步結果存在性失敗: requestId={}", requestId, e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("requestId", requestId);
            errorData.put("exists", false);
            errorData.put("message", "檢查失敗: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("异步结果存在性检查失败", e.getMessage()));
        }
    }
    
    /**
     * 刪除異步處理結果
     *
     * DELETE /tymb/api/async/result/{requestId}
     *
     * @param requestId 請求ID
     * @return 刪除操作結果
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<BackendApiResponse<Map<String, Object>>> deleteAsyncResult(@PathVariable String requestId) {
        try {
            logger.info("🗑️ 刪除異步處理結果: requestId={}", requestId);

            // 檢查結果是否存在
            boolean exists = asyncResultService.resultExists(requestId);

            if (!exists) {
                logger.warn("⚠️ 異步處理結果不存在，無法刪除: requestId={}", requestId);
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("requestId", requestId);
                errorData.put("removed", false);
                errorData.put("message", "結果不存在");
                return ResponseEntity.status(404)
                    .body(BackendApiResponse.notFound("异步处理结果不存在"));
            }

            // 刪除結果
            asyncResultService.deleteResult(requestId);

            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("removed", true);
            data.put("message", "異步處理結果已成功刪除");

            logger.info("✅ 成功刪除異步處理結果: requestId={}", requestId);
            return ResponseEntity.ok(BackendApiResponse.success("异步处理结果删除成功", data));

        } catch (Exception e) {
            logger.error("❌ 刪除異步處理結果失敗: requestId={}", requestId, e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("requestId", requestId);
            errorData.put("removed", false);
            errorData.put("message", "刪除失敗: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("异步处理结果删除失败", e.getMessage()));
        }
    }
}

