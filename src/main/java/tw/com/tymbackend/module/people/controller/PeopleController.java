package tw.com.tymbackend.module.people.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import tw.com.tymbackend.module.people.domain.dto.PeopleNameRequestDTO;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.PeopleService;
import tw.com.tymbackend.core.service.AsyncMessageService;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.ty.common.response.ErrorCode;
import tw.com.ty.common.response.MessageKey;

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
    private AsyncMessageService asyncMessageService;

    // 插入 1 個 (接收 JSON)
    @PostMapping("/insert")
    public ResponseEntity<?> insertPeople(@RequestBody People people) {
        // 驗證輸入
        if (people == null || people.getName() == null || people.getName().trim().isEmpty()) {
            return new ResponseEntity<>("Invalid input: name is required", HttpStatus.BAD_REQUEST);
        }

        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            try {
                String requestId = asyncMessageService.sendPeopleInsertRequest(people);
                Map<String, Object> data = new HashMap<>();
                data.put("requestId", requestId);
                data.put("status", "processing");
                data.put("message", MessageKey.ASYNC_PEOPLE_INSERT_SUBMITTED.getMessage());
                return ResponseEntity.accepted()
                    .body(BackendApiResponse.accepted(requestId, MessageKey.ASYNC_PEOPLE_INSERT_SUBMITTED));
            } catch (Exception e) {
                logger.error("Failed to send async insert request", e);
                // 如果異步發送失敗，回退到同步處理
            }
        }

        // 本地環境或異步失敗時，使用同步處理
        try {
            People savedPeople = peopleService.insertPerson(people);
            return new ResponseEntity<>(BackendApiResponse.success(MessageKey.PEOPLE_INSERT_SUCCESS, savedPeople), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input while inserting person", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.PEOPLE_INVALID_INPUT), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Runtime exception during insert person", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.PEOPLE_INSERT_FAILED, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error during insert person", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新 1 個 (接收 JSON)
    @PostMapping("/update")
    public ResponseEntity<?> updatePeople(@RequestBody People people) {
        // 驗證輸入
        if (people == null || people.getName() == null || people.getName().trim().isEmpty()) {
            return new ResponseEntity<>("Invalid input: name is required", HttpStatus.BAD_REQUEST);
        }

        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            try {
                String requestId = asyncMessageService.sendPeopleUpdateRequest(people);
                Map<String, Object> data = new HashMap<>();
                data.put("requestId", requestId);
                data.put("status", "processing");
                data.put("message", MessageKey.ASYNC_PEOPLE_UPDATE_SUBMITTED.getMessage());
                return ResponseEntity.accepted()
                    .body(BackendApiResponse.accepted(requestId, MessageKey.ASYNC_PEOPLE_UPDATE_SUBMITTED));
            } catch (Exception e) {
                logger.error("Failed to send async update request", e);
                // 如果異步發送失敗，回退到同步處理
            }
        }

        // 本地環境或異步失敗時，使用同步處理
        try {
            People updatedPeople = peopleService.updatePerson(people);
            return new ResponseEntity<>(BackendApiResponse.success(MessageKey.PEOPLE_UPDATE_SUCCESS, updatedPeople), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input while updating person", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.PEOPLE_NOT_FOUND), HttpStatus.NOT_FOUND);
        } catch (org.hibernate.StaleObjectStateException e) {
            logger.error("Concurrent update detected", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.CONCURRENT_UPDATE_DETECTED), HttpStatus.CONFLICT);
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Optimistic locking failure detected", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.OPTIMISTIC_LOCKING_FAILURE), HttpStatus.CONFLICT);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Data integrity violation", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.DATA_INTEGRITY_VIOLATION), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Runtime exception during update", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error during update", e);
            return new ResponseEntity<>(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 插入多個 (接收 JSON)
    @PostMapping("/insert-multiple")
    public ResponseEntity<?> insertMultiplePeople(@RequestBody List<People> peopleList) {
        try {
            List<People> savedPeople = peopleService.saveAllPeople(peopleList);
            return new ResponseEntity<>(savedPeople, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 搜尋所有 (傳出 JSON)
    @PostMapping("/get-all")
    public ResponseEntity<?> getAllPeople() {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendPeopleGetAllRequest();
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", MessageKey.ASYNC_PEOPLE_LIST_SUBMITTED.getMessage());
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, MessageKey.ASYNC_PEOPLE_LIST_SUBMITTED));
        }

        // 本地環境，同步處理
        try {
            // 使用優化的批量查詢方法，但保持相同的API介面
            List<People> people = peopleService.getAllPeopleOptimized();
            return ResponseEntity.ok(BackendApiResponse.success(MessageKey.PEOPLE_GET_ALL_SUCCESS, people));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.error(ErrorCode.PEOPLE_LIST_FAILED, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    // 搜尋 name (接收 name 傳出 JSON)
    @PostMapping("/get-by-name")
    public ResponseEntity<?> getPeopleByName(@RequestBody PeopleNameRequestDTO request) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendPeopleGetByNameRequest(request.getName());
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", MessageKey.ASYNC_PEOPLE_QUERY_SUBMITTED.getMessage());
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, MessageKey.ASYNC_PEOPLE_QUERY_SUBMITTED));
        }

        // 本地環境，同步處理
        try {
            Optional<People> people = peopleService.getPeopleByName(request.getName());
            if (people.isPresent()) {
                return ResponseEntity.ok(BackendApiResponse.success(MessageKey.PEOPLE_GET_SUCCESS, people.get()));
            } else {
                return ResponseEntity.status(404)
                    .body(BackendApiResponse.error(ErrorCode.PEOPLE_NOT_FOUND));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.error(ErrorCode.PEOPLE_GET_FAILED, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    // 刪除所有
    @PostMapping("/delete-all")
    public ResponseEntity<?> deleteAllPeople() {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendPeopleDeleteAllRequest();
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", MessageKey.ASYNC_PEOPLE_DELETE_SUBMITTED.getMessage());
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, MessageKey.ASYNC_PEOPLE_DELETE_SUBMITTED));
        }

        // 本地環境，同步處理
        try {
            peopleService.deleteAllPeople();
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.error(ErrorCode.PEOPLE_DELETE_FAILED, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        }
    }

    // 取得所有人的名字
    @GetMapping("/names")
    public ResponseEntity<?> getAllPeopleNames() {
        logger.info("=== 強制使用異步處理模式 ===");

        // 強制使用異步處理，通過 Consumer 處理
        if (asyncMessageService != null) {
            logger.info("使用異步處理模式");
            try {
                String requestId = asyncMessageService.sendPeopleGetNamesRequest();
                logger.info("異步請求已發送，requestId: {}", requestId);
                return ResponseEntity.accepted()
                    .body(BackendApiResponse.accepted(requestId, MessageKey.ASYNC_PEOPLE_LIST_SUBMITTED));
            } catch (Exception e) {
                logger.error("Failed to send async get names request", e);
                return ResponseEntity.status(500)
                    .body(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "異步處理失敗: " + e.getMessage()));
            }
        } else {
            logger.error("AsyncMessageService 為 null，無法進行異步處理");
            return ResponseEntity.status(500)
                .body(BackendApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, "異步服務不可用，請檢查 RabbitMQ 配置"));
        }
    }
}
