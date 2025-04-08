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
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"}, allowCredentials = "true")
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
     * Get weapon by name
     */
    @GetMapping("/{name}")
    public ResponseEntity<Weapon> getWeaponByName(@PathVariable String name) {
        return ResponseEntity.ok(weaponService.getWeaponByName(name));
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
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteWeapon(@PathVariable String name) {
        weaponService.deleteWeapon(name);
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
    @PutMapping("/{name}/attributes")
    public ResponseEntity<Weapon> updateWeaponAttributes(
            @PathVariable String name,
            @RequestBody String attributes) {
        return ResponseEntity.ok(weaponService.updateWeaponAttributes(name, attributes));
    }

    /**
     * Update weapon base damage
     */
    @PutMapping("/{name}/base-damage")
    public ResponseEntity<Weapon> updateWeaponBaseDamage(
            @PathVariable String name,
            @RequestBody Integer baseDamage) {
        return ResponseEntity.ok(weaponService.updateWeaponBaseDamage(name, baseDamage));
    }

    /**
     * Update weapon bonus damage
     */
    @PutMapping("/{name}/bonus-damage")
    public ResponseEntity<Weapon> updateWeaponBonusDamage(
            @PathVariable String name,
            @RequestBody Integer bonusDamage) {
        return ResponseEntity.ok(weaponService.updateWeaponBonusDamage(name, bonusDamage));
    }

    /**
     * Update weapon bonus attributes
     */
    @PutMapping("/{name}/bonus-attributes")
    public ResponseEntity<Weapon> updateWeaponBonusAttributes(
            @PathVariable String name,
            @RequestBody List<String> bonusAttributes) {
        return ResponseEntity.ok(weaponService.updateWeaponBonusAttributes(name, bonusAttributes));
    }

    /**
     * Update weapon state attributes
     */
    @PutMapping("/{name}/state-attributes")
    public ResponseEntity<Weapon> updateWeaponStateAttributes(
            @PathVariable String name,
            @RequestBody List<String> stateAttributes) {
        return ResponseEntity.ok(weaponService.updateWeaponStateAttributes(name, stateAttributes));
    }
} 