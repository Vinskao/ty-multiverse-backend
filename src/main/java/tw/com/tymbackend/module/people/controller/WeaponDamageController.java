package tw.com.tymbackend.module.people.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.tymbackend.module.people.service.WeaponDamageService;

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
     */
    @GetMapping("/damageWithWeapon")
    public ResponseEntity<Integer> damageWithWeapon(@RequestParam("name") String name) {
        int result = weaponDamageService.calculateDamageWithWeapon(name);
        if (result == -1) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // Removed safeInt method as the computation is now inside the service
} 