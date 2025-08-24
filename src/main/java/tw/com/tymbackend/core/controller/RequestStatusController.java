package tw.com.tymbackend.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.core.consumer.ResponseConsumer;
import tw.com.tymbackend.core.message.ProducerResponseDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * 請求狀態查詢 Controller
 * 
 * 提供查詢 Producer 請求處理狀態的 API
 * 只在 RabbitMQ 啟用時生效
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/request-status")
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class RequestStatusController {
    
    @Autowired(required = false)
    private ResponseConsumer responseConsumer;
    
    /**
     * 查詢請求狀態
     * 
     * @param requestId 請求ID
     * @return 請求狀態
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequestStatus(@PathVariable String requestId) {
        if (responseConsumer == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "RabbitMQ 未啟用");
            errorResponse.put("message", "無法查詢請求狀態");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        Object result = responseConsumer.getRequestStatus(requestId);
        
        if (result == null) {
            Map<String, Object> notFoundResponse = new HashMap<>();
            notFoundResponse.put("error", "請求未找到");
            notFoundResponse.put("requestId", requestId);
            notFoundResponse.put("message", "該請求ID不存在或已過期");
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 檢查請求是否存在
     * 
     * @param requestId 請求ID
     * @return 是否存在
     */
    @GetMapping("/{requestId}/exists")
    public ResponseEntity<?> checkRequestExists(@PathVariable String requestId) {
        if (responseConsumer == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "RabbitMQ 未啟用");
            errorResponse.put("message", "無法檢查請求狀態");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean exists = responseConsumer.hasRequest(requestId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("requestId", requestId);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 移除請求狀態
     * 
     * @param requestId 請求ID
     * @return 移除結果
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<?> removeRequestStatus(@PathVariable String requestId) {
        if (responseConsumer == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "RabbitMQ 未啟用");
            errorResponse.put("message", "無法移除請求狀態");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean removed = responseConsumer.hasRequest(requestId);
        if (removed) {
            responseConsumer.removeRequestStatus(requestId);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("requestId", requestId);
        response.put("removed", removed);
        response.put("message", removed ? "請求狀態已移除" : "請求狀態不存在");
        
        return ResponseEntity.ok(response);
    }
}
