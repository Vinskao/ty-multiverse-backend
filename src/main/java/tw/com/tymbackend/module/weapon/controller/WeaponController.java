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
     * Get weapon by weapon name (ID)
     */
    @GetMapping("/{weaponName}")
    public ResponseEntity<Weapon> getWeaponByWeaponName(@PathVariable String weaponName) {
        return ResponseEntity.ok(weaponService.getWeaponByWeaponName(weaponName));
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
     * Delete a weapon
     */
    @DeleteMapping("/{weaponName}")
    public ResponseEntity<Void> deleteWeapon(@PathVariable String weaponName) {
        weaponService.deleteWeapon(weaponName);
        return ResponseEntity.ok().build();
    }

    /**
     * Check if weapon exists
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<Map<String, Boolean>> checkWeaponExists(@PathVariable String name) {
        return ResponseEntity.ok(Map.of("exists", weaponService.weaponExists(name)));
    }

    /**
     * Update weapon attributes
     */
    @PutMapping("/{weaponName}/attributes")
    public ResponseEntity<Weapon> updateWeaponAttributes(
            @PathVariable String weaponName,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(weaponService.updateWeaponAttributes(weaponName, request.get("attributes")));
    }

    /**
     * Update weapon base damage
     */
    @PutMapping("/{weaponName}/base-damage")
    public ResponseEntity<Weapon> updateWeaponBaseDamage(
            @PathVariable String weaponName,
            @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBaseDamage(weaponName, request.get("baseDamage")));
    }

    /**
     * Update weapon bonus damage
     */
    @PutMapping("/{weaponName}/bonus-damage")
    public ResponseEntity<Weapon> updateWeaponBonusDamage(
            @PathVariable String weaponName,
            @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBonusDamage(weaponName, request.get("bonusDamage")));
    }

    /**
     * Update weapon bonus attributes
     */
    @PutMapping("/{weaponName}/bonus-attributes")
    public ResponseEntity<Weapon> updateWeaponBonusAttributes(
            @PathVariable String weaponName,
            @RequestBody Map<String, List<String>> request) {
        return ResponseEntity.ok(weaponService.updateWeaponBonusAttributes(weaponName, request.get("bonusAttributes")));
    }

    /**
     * Update weapon state attributes
     */
    @PutMapping("/{weaponName}/state-attributes")
    public ResponseEntity<Weapon> updateWeaponStateAttributes(
            @PathVariable String weaponName,
            @RequestBody Map<String, List<String>> request) {
        return ResponseEntity.ok(weaponService.updateWeaponStateAttributes(weaponName, request.get("stateAttributes")));
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