package tw.com.tymbackend.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.core.consumer.ResponseConsumer;


import java.util.HashMap;
import java.util.Map;

/**
 * 結果查詢 Controller
 * 
 * 提供查詢處理結果的 API
 * 只在 RabbitMQ 啟用時生效
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@RestController
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class ResultController {
    
    @Autowired(required = false)
    private ResponseConsumer responseConsumer;
    
    /**
     * 查詢 People 獲取所有結果
     * 
     * @param requestId 請求ID
     * @return 處理結果
     */
    @GetMapping("/people/result/{requestId}")
    public ResponseEntity<Object> getPeopleResult(@PathVariable String requestId) {
        if (responseConsumer == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "RabbitMQ 未啟用");
            errorResponse.put("message", "無法查詢結果");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        Object result = responseConsumer.getRequestStatus(requestId);
        
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            Map<String, Object> processingResponse = new HashMap<>();
            processingResponse.put("status", "PROCESSING");
            processingResponse.put("message", "請求處理中，請稍後再試");
            processingResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(processingResponse);
        }
    }
    
    /**
     * 查詢 People 獲取所有結果（使用專門的 DTO）
     * 
     * @param requestId 請求ID
     * @return People 獲取所有結果
     */
    @GetMapping("/people/get-all/result/{requestId}")
    public ResponseEntity<Object> getPeopleGetAllResult(@PathVariable String requestId) {
        if (responseConsumer == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "RabbitMQ 未啟用");
            errorResponse.put("message", "無法查詢結果");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        Object result = responseConsumer.getRequestStatus(requestId);
        
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            Map<String, Object> processingResponse = new HashMap<>();
            processingResponse.put("status", "PROCESSING");
            processingResponse.put("message", "請求處理中，請稍後再試");
            processingResponse.put("requestId", requestId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(processingResponse);
        }
    }
    
    /**
     * 檢查結果是否存在
     * 
     * @param requestId 請求ID
     * @return 是否存在
     */
    @GetMapping("/people/result/{requestId}/exists")
    public ResponseEntity<Object> checkResultExists(@PathVariable String requestId) {
        if (responseConsumer == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "RabbitMQ 未啟用");
            errorResponse.put("message", "無法檢查結果");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean exists = responseConsumer.hasRequest(requestId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("requestId", requestId);
        response.put("exists", exists);
        response.put("message", exists ? "結果已準備就緒" : "結果尚未準備就緒");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 清理結果
     * 
     * @param requestId 請求ID
     * @return 清理結果
     */
    @DeleteMapping("/people/result/{requestId}")
    public ResponseEntity<Object> clearResult(@PathVariable String requestId) {
        if (responseConsumer == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "RabbitMQ 未啟用");
            errorResponse.put("message", "無法清理結果");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean removed = responseConsumer.hasRequest(requestId);
        if (removed) {
            responseConsumer.removeRequestStatus(requestId);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("requestId", requestId);
        response.put("removed", removed);
        response.put("message", removed ? "結果已清理" : "結果不存在");
        
        return ResponseEntity.ok(response);
    }
}
