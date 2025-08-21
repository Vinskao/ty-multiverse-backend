package tw.com.tymbackend.module.people.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import tw.com.tymbackend.core.service.AsyncMessageService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/people")
public class WeaponDamageController {

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
     * 異步處理邏輯：
     * - 本地環境：同步處理
     * - 生產環境：發送消息到 RabbitMQ，立即回應請求ID
     */
    @GetMapping("/damageWithWeapon")
    public ResponseEntity<?> damageWithWeapon(@RequestParam("name") String name) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendDamageCalculationRequest(name);
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("status", "processing");
            response.put("message", "傷害計算請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted().body(response);
        }
        
        // 本地環境，同步處理
        int result = weaponDamageService.calculateDamageWithWeapon(name);
        if (result == -1) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Batch calculate damage with weapons for multiple characters.
     * Optimized to reduce database connections and improve performance.
     *
     * @param request batch request containing character names
     * @return batch damage calculation results
     */
    @PostMapping("/batchDamageWithWeapon")
    public ResponseEntity<BatchDamageResponseDTO> batchDamageWithWeapon(@RequestBody BatchDamageRequestDTO request) {
        BatchDamageResponseDTO result = weaponDamageService.calculateBatchDamageWithWeapon(request);
        return ResponseEntity.ok(result);
    }

    // Removed safeInt method as the computation is now inside the service
} 