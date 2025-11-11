package tw.com.tymbackend.module.weapon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.ty.common.response.BackendApiResponse;
import tw.com.tymbackend.core.service.AsyncMessageService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/weapons")
public class WeaponController {

    private static final Logger logger = LoggerFactory.getLogger(WeaponController.class);

    @Autowired
    private WeaponService weaponService;

    @Autowired(required = false)
    private AsyncMessageService asyncMessageService;

    /**
     * Get all weapons
     */
    @GetMapping
    public ResponseEntity<?> getAllWeapons() {
        // 調試日誌
        logger.info("=== WeaponController.getAllWeapons ===");
        logger.info("asyncMessageService: {}", asyncMessageService);
        logger.info("weaponService: {}", weaponService);

        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            logger.info("使用異步處理");
            String requestId = asyncMessageService.sendWeaponGetAllRequest();
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", "武器列表獲取請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, "武器列表獲取請求已提交"));
        }

        // 本地環境，同步處理
        logger.info("使用同步處理");
        try {
            List<Weapon> weapons = weaponService.getAllWeapons();
            return ResponseEntity.ok(BackendApiResponse.success("獲取所有武器成功", weapons));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("獲取武器列表失敗", e.getMessage()));
        }
    }

    /**
     * Get weapon by name (ID)
     */
    @GetMapping("/{name}")
    public ResponseEntity<?> getWeaponById(@PathVariable String name) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendWeaponGetByNameRequest(name);
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", "武器查詢請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, "武器查詢請求已提交"));
        }

        // 本地環境，同步處理
        try {
            return weaponService.getWeaponById(name)
                    .map(weapon -> ResponseEntity.ok(BackendApiResponse.success("獲取武器成功", weapon)))
                    .orElse(ResponseEntity.status(404)
                        .body(BackendApiResponse.error("武器不存在", "NOT_FOUND")));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("獲取武器失敗", e.getMessage()));
        }
    }

    /**
     * Get weapons by owner
     */
    @GetMapping("/owner/{owner}")
    public ResponseEntity<?> getWeaponsByOwner(@PathVariable String owner) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendWeaponGetByOwnerRequest(owner);
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", "武器按擁有者查詢請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, "武器按擁有者查詢請求已提交"));
        }

        // 本地環境，同步處理
        try {
            List<Weapon> weapons = weaponService.getWeaponsByOwner(owner);
            return ResponseEntity.ok(BackendApiResponse.success("獲取武器成功", weapons));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("獲取武器失敗", e.getMessage()));
        }
    }

    /**
     * Create or update a weapon
     */
    @PostMapping
    public ResponseEntity<?> saveWeapon(@RequestBody Weapon weapon) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendWeaponSaveRequest(weapon);
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", "武器保存請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, "武器保存請求已提交"));
        }

        // 本地環境，同步處理
        try {
            Weapon savedWeapon = weaponService.saveWeaponSmart(weapon);
            return ResponseEntity.ok(BackendApiResponse.success("保存武器成功", savedWeapon));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("保存武器失敗", e.getMessage()));
        }
    }

    /**
     * Delete a weapon by name (ID)
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteWeapon(@PathVariable String name) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendWeaponDeleteRequest(name);
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", "武器刪除請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, "武器刪除請求已提交"));
        }

        // 本地環境，同步處理
        try {
            weaponService.deleteWeapon(name);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("刪除武器失敗", e.getMessage()));
        }
    }

    /**
     * Delete all weapons
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllWeapons() {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendWeaponDeleteAllRequest();
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", "武器刪除全部請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, "武器刪除全部請求已提交"));
        }

        // 本地環境，同步處理
        try {
            weaponService.deleteAllWeapons();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("刪除全部武器失敗", e.getMessage()));
        }
    }

    /**
     * Check if weapon exists by name (ID)
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<?> checkWeaponExists(@PathVariable String name) {
        // 如果 RabbitMQ 啟用，使用異步處理
        if (asyncMessageService != null) {
            String requestId = asyncMessageService.sendWeaponExistsRequest(name);
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "processing");
            data.put("message", "武器存在檢查請求已提交，請稍後查詢結果");
            return ResponseEntity.accepted()
                .body(BackendApiResponse.accepted(requestId, "武器存在檢查請求已提交"));
        }

        // 本地環境，同步處理
        try {
            boolean exists = weaponService.weaponExists(name);
            return ResponseEntity.ok(BackendApiResponse.success("檢查武器存在成功", Map.of("exists", exists)));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("檢查武器存在失敗", e.getMessage()));
        }
    }

    /**
     * Update weapon attributes
     */
    @PutMapping("/{name}/attributes")
    public ResponseEntity<Weapon> updateWeaponAttributes(
            @PathVariable String name,
            @RequestBody Map<String, String> request) {
        Weapon w = weaponService.getWeaponById(name)
                .orElseThrow(() -> new RuntimeException("Weapon not found: " + name));
        w.setAttributes(request.get("attributes"));
        return ResponseEntity.ok(weaponService.updateWeaponAttributes(name, w));
    }

    /**
     * Update weapon base damage
     */
    @PutMapping("/{name}/base-damage")
    public ResponseEntity<Weapon> updateWeaponBaseDamage(
            @PathVariable String name,
            @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBaseDamage(name, request.get("baseDamage")));
    }

    /**
     * Update weapon bonus damage
     */
    @PutMapping("/{name}/bonus-damage")
    public ResponseEntity<Weapon> updateWeaponBonusDamage(
            @PathVariable String name,
            @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBonusDamage(name, request.get("bonusDamage")));
    }

    /**
     * Update weapon bonus attributes
     */
    @PutMapping("/{name}/bonus-attributes")
    public ResponseEntity<Weapon> updateWeaponBonusAttributes(
            @PathVariable String name,
            @RequestBody Map<String, List<String>> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBonusAttributes(name, request.get("bonusAttributes")));
    }

    /**
     * Update weapon state attributes
     */
    @PutMapping("/{name}/state-attributes")
    public ResponseEntity<Weapon> updateWeaponStateAttributes(
            @PathVariable String name,
            @RequestBody Map<String, List<String>> request) {
        return ResponseEntity.ok(weaponService.updateWeaponStateAttributes(name, request.get("stateAttributes")));
    }

    /**
     * Find weapons by base damage range
     */
    @GetMapping("/damage-range")
    public ResponseEntity<List<Weapon>> findByBaseDamageRange(
            @RequestParam Integer minDamage,
            @RequestParam Integer maxDamage) {
        return ResponseEntity.ok(weaponService.findByBaseDamageRange(minDamage, maxDamage));
    }

    /**
     * Find weapons by attribute
     */
    @GetMapping("/attribute/{attribute}")
    public ResponseEntity<List<Weapon>> findByAttribute(@PathVariable String attribute) {
        return ResponseEntity.ok(weaponService.findByAttribute(attribute));
    }
} 