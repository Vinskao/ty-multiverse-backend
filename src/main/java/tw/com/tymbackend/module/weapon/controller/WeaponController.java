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
     * Get weapon by composite key (name, weapon)
     */
    @GetMapping("/{name}/{weapon}")
    public ResponseEntity<Weapon> getWeaponById(@PathVariable String name, @PathVariable String weapon) {
        return weaponService.getWeaponById(name, weapon)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get weapons by owner name
     */
    @GetMapping("/owner/{name}")
    public ResponseEntity<List<Weapon>> getWeaponsByOwnerName(@PathVariable String name) {
        return ResponseEntity.ok(weaponService.getWeaponsByOwnerName(name));
    }

    /**
     * Create or update a weapon
     */
    @PostMapping
    public ResponseEntity<Weapon> saveWeapon(@RequestBody Weapon weapon) {
        return ResponseEntity.ok(weaponService.saveWeapon(weapon));
    }

    /**
     * Delete a weapon by composite key (name, weapon)
     */
    @DeleteMapping("/{name}/{weapon}")
    public ResponseEntity<Void> deleteWeapon(@PathVariable String name, @PathVariable String weapon) {
        weaponService.deleteWeapon(name, weapon);
        return ResponseEntity.ok().build();
    }

    /**
     * Check if weapon exists by composite key (name, weapon)
     */
    @GetMapping("/exists/{name}/{weapon}")
    public ResponseEntity<Map<String, Boolean>> checkWeaponExists(@PathVariable String name, @PathVariable String weapon) {
        return ResponseEntity.ok(Map.of("exists", weaponService.weaponExists(name, weapon)));
    }

    /**
     * Update weapon attributes
     */
    @PutMapping("/{name}/{weapon}/attributes")
    public ResponseEntity<Weapon> updateWeaponAttributes(
            @PathVariable String name,
            @PathVariable String weapon,
            @RequestBody Map<String, String> request) {
        Weapon w = weaponService.getWeaponById(name, weapon)
                .orElseThrow(() -> new RuntimeException("Weapon not found: " + weapon));
        w.setAttributes(request.get("attributes"));
        return ResponseEntity.ok(weaponService.updateWeaponAttributes(name, weapon, w));
    }

    /**
     * Update weapon base damage
     */
    @PutMapping("/{name}/{weapon}/base-damage")
    public ResponseEntity<Weapon> updateWeaponBaseDamage(
            @PathVariable String name,
            @PathVariable String weapon,
            @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBaseDamage(name, weapon, request.get("baseDamage")));
    }

    /**
     * Update weapon bonus damage
     */
    @PutMapping("/{name}/{weapon}/bonus-damage")
    public ResponseEntity<Weapon> updateWeaponBonusDamage(
            @PathVariable String name,
            @PathVariable String weapon,
            @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBonusDamage(name, weapon, request.get("bonusDamage")));
    }

    /**
     * Update weapon bonus attributes
     */
    @PutMapping("/{name}/{weapon}/bonus-attributes")
    public ResponseEntity<Weapon> updateWeaponBonusAttributes(
            @PathVariable String name,
            @PathVariable String weapon,
            @RequestBody Map<String, List<String>> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBonusAttributes(name, weapon, request.get("bonusAttributes")));
    }

    /**
     * Update weapon state attributes
     */
    @PutMapping("/{name}/{weapon}/state-attributes")
    public ResponseEntity<Weapon> updateWeaponStateAttributes(
            @PathVariable String name,
            @PathVariable String weapon,
            @RequestBody Map<String, List<String>> request) {
        return ResponseEntity.ok(weaponService.updateWeaponStateAttributes(name, weapon, request.get("stateAttributes")));
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