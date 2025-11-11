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
import tw.com.ty.common.response.BackendApiResponse;

@RestController
@RequestMapping("/people")
public class WeaponDamageController {

    private final WeaponDamageService weaponDamageService;

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
     * 同步處理：直接調用業務邏輯並返回結果
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
    public ResponseEntity<BatchDamageResponseDTO> batchDamageWithWeapon(@RequestBody BatchDamageRequestDTO request) {
        BatchDamageResponseDTO result = weaponDamageService.calculateBatchDamageWithWeapon(request);
        return ResponseEntity.ok(result);
    }

    // Removed safeInt method as the computation is now inside the service
} 