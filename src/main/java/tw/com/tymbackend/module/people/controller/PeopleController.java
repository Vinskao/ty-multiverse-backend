package tw.com.tymbackend.module.people.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import tw.com.tymbackend.module.people.domain.dto.PeopleNameRequestDTO;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.PeopleService;
import tw.com.tymbackend.module.people.service.PeopleProducerService;
import tw.com.tymbackend.core.service.AsyncMessageService;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private static final Logger logger = LoggerFactory.getLogger(PeopleController.class);

    @Autowired
    private PeopleService peopleService;
    
    @Autowired(required = false)
    private PeopleProducerService peopleProducerService;
    
    @Autowired(required = false)
    private AsyncMessageService asyncMessageService;

    // 插入 1 個 (接收 JSON)
    @PostMapping("/insert")
    public ResponseEntity<?> insertPeople(@RequestBody People people) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (peopleProducerService != null) {
            String requestId = peopleProducerService.sendInsertPeopleRequest(people);
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "processing");
            response.put("message", "角色插入請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted().body(response);
        }
        
        // RabbitMQ 未啟用時，返回錯誤
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "RabbitMQ 未啟用");
        errorResponse.put("message", "此 API 需要 RabbitMQ 異步處理，請確保 RabbitMQ 已正確配置");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // 更新 1 個 (接收 JSON)
    @PostMapping("/update")
    public ResponseEntity<?> updatePeople(@RequestBody People people) {
        // 驗證輸入
        if (people == null || people.getName() == null || people.getName().trim().isEmpty()) {
            return new ResponseEntity<>("Invalid input: name is required", HttpStatus.BAD_REQUEST);
        }
        
        // 如果 RabbitMQ 啟用，使用異步處理
        if (peopleProducerService != null) {
            String requestId = peopleProducerService.sendUpdatePeopleRequest(people);
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "processing");
            response.put("message", "角色更新請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted().body(response);
        }
        
        // RabbitMQ 未啟用時，返回錯誤
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "RabbitMQ 未啟用");
        errorResponse.put("message", "此 API 需要 RabbitMQ 異步處理，請確保 RabbitMQ 已正確配置");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // 插入多個 (接收 JSON)
    @PostMapping("/insert-multiple")
    public ResponseEntity<?> insertMultiplePeople(@RequestBody List<People> peopleList) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (peopleProducerService != null) {
            String requestId = peopleProducerService.sendInsertMultiplePeopleRequest(peopleList);
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "processing");
            response.put("message", "批量角色插入請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted().body(response);
        }
        
        // RabbitMQ 未啟用時，返回錯誤
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "RabbitMQ 未啟用");
        errorResponse.put("message", "此 API 需要 RabbitMQ 異步處理，請確保 RabbitMQ 已正確配置");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // 搜尋所有 (傳出 JSON)
    @PostMapping("/get-all")
    public ResponseEntity<?> getAllPeople() {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendPeopleGetAllRequest();
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "processing");
            response.put("message", "角色列表獲取請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted().body(response);
        }
        
        // RabbitMQ 未啟用時，返回錯誤
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "RabbitMQ 未啟用");
        errorResponse.put("message", "此 API 需要 RabbitMQ 異步處理，請確保 RabbitMQ 已正確配置");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // 搜尋 name (接收 name 傳出 JSON)
    @PostMapping("/get-by-name")
    public ResponseEntity<?> getPeopleByName(@RequestBody PeopleNameRequestDTO request) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (peopleProducerService != null) {
            String requestId = peopleProducerService.sendGetPeopleByNameRequest(request.getName());
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "processing");
            response.put("message", "角色查詢請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted().body(response);
        }
        
        // RabbitMQ 未啟用時，返回錯誤
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "RabbitMQ 未啟用");
        errorResponse.put("message", "此 API 需要 RabbitMQ 異步處理，請確保 RabbitMQ 已正確配置");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // 刪除所有
    @PostMapping("/delete-all")
    public ResponseEntity<?> deleteAllPeople() {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (peopleProducerService != null) {
            String requestId = peopleProducerService.sendDeleteAllPeopleRequest();
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "processing");
            response.put("message", "刪除所有角色請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted().body(response);
        }
        
        // RabbitMQ 未啟用時，返回錯誤
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "RabbitMQ 未啟用");
        errorResponse.put("message", "此 API 需要 RabbitMQ 異步處理，請確保 RabbitMQ 已正確配置");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // 取得所有人的名字
    @GetMapping("/names")
    public ResponseEntity<?> getAllPeopleNames() {
        try {
            // 添加認證診斷日誌
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            logger.info("=== 認證診斷 ===");
            logger.info("認證對象: {}", auth);
            logger.info("是否已認證: {}", auth.isAuthenticated());
            logger.info("用戶名: {}", auth.getName());
            logger.info("權限: {}", auth.getAuthorities());
            logger.info("主要對象類型: {}", auth.getPrincipal().getClass().getSimpleName());
            
            List<String> names = peopleService.getAllPeopleNames();
            return new ResponseEntity<>(names, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Runtime exception during getAllPeopleNames", e);
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error during getAllPeopleNames", e);
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
