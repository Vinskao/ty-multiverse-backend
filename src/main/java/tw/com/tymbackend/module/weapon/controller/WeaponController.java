package tw.com.tymbackend.module.weapon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/weapons")
public class WeaponController {

    @Autowired
    private WeaponService weaponService;

    /**
     * Get all weapons
     */
    @GetMapping
    public ResponseEntity<List<Weapon>> getAllWeapons() {
        return ResponseEntity.ok(weaponService.getAllWeapons());
    }

    /**
     * Get weapon by name (ID)
     */
    @GetMapping("/{name}")
    public ResponseEntity<Weapon> getWeaponById(@PathVariable String name) {
        return weaponService.getWeaponById(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get weapons by owner
     */
    @GetMapping("/owner/{owner}")
    public ResponseEntity<List<Weapon>> getWeaponsByOwner(@PathVariable String owner) {
        return ResponseEntity.ok(weaponService.getWeaponsByOwner(owner));
    }

    /**
     * Create or update a weapon
     */
    @PostMapping
    public ResponseEntity<Weapon> saveWeapon(@RequestBody Weapon weapon) {
        return ResponseEntity.ok(weaponService.saveWeaponSmart(weapon));
    }

    /**
     * Delete a weapon by name (ID)
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteWeapon(@PathVariable String name) {
        weaponService.deleteWeapon(name);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete all weapons
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllWeapons() {
        weaponService.deleteAllWeapons();
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if weapon exists by name (ID)
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<Map<String, Boolean>> checkWeaponExists(@PathVariable String name) {
        return ResponseEntity.ok(Map.of("exists", weaponService.weaponExists(name)));
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