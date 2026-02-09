package tw.com.tymbackend.module.people.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.tymbackend.module.people.service.WeaponDamageService;
import tw.com.tymbackend.module.people.domain.dto.BatchDamageRequestDTO;
import tw.com.tymbackend.module.people.domain.dto.BatchDamageResponseDTO;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.ty.common.response.MessageKey;
import tw.com.tymbackend.core.service.AsyncMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/people")
public class WeaponDamageController {

    private static final Logger logger = LoggerFactory.getLogger(WeaponDamageController.class);
    private final WeaponDamageService weaponDamageService;

    @Autowired(required = false)
    private AsyncMessageService asyncMessageService;

    public WeaponDamageController(WeaponDamageService weaponDamageService) {
        this.weaponDamageService = weaponDamageService;
    }

    /**
     * Calculate damage with owner's weapon.
     * Example: /people/damageWithWeapon?name=Draeny
     *
     * @param name person name (owner)
     * @return damageWithWeapon value in JSON {"damageWithWeapon": value}
     *
     *         同步處理：直接調用業務邏輯並返回結果
     */
    @GetMapping("/damageWithWeapon")
    public ResponseEntity<BackendApiResponse<Integer>> damageWithWeapon(@RequestParam("name") String name) {
        try {
            int result = weaponDamageService.calculateDamageWithWeapon(name);
            if (result == -1) {
                return ResponseEntity.status(400)
                        .body(BackendApiResponse.badRequest("Character not found or invalid"));
            }
            return ResponseEntity.ok(BackendApiResponse.success("Damage calculation successful", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(BackendApiResponse.internalError("Internal server error", e.getMessage()));
        }
    }

    /**
     * Batch calculate damage with weapons for multiple characters.
     * Optimized to reduce database connections and improve performance.
     *
     * @param request batch request containing character names
     * @return batch damage calculation results
     */
    @PostMapping("/batchDamageWithWeapon")
    public ResponseEntity<?> batchDamageWithWeapon(@RequestBody BatchDamageRequestDTO request) {
        // 如果 AsyncMessageService 啟用，則使用異步處理（這是 Gateway 期望的流程）
        if (asyncMessageService != null) {
            try {
                String requestId = asyncMessageService.sendPeopleBatchDamageRequest(request.getNames());
                logger.info("批量傷害計算請求已提交到 RabbitMQ: requestId={}, count={}", requestId, request.getNames().size());
                return ResponseEntity.accepted()
                        .body(BackendApiResponse.accepted(requestId, MessageKey.ASYNC_PEOPLE_QUERY_SUBMITTED));
            } catch (Exception e) {
                logger.error("Failed to send async batch damage request", e);
                // 如果異步發送失敗，回退到同步處理
            }
        }

        // 同步處理
        BatchDamageResponseDTO result = weaponDamageService.calculateBatchDamageWithWeapon(request);
        return ResponseEntity.ok(result);
    }

    // Removed safeInt method as the computation is now inside the service
}