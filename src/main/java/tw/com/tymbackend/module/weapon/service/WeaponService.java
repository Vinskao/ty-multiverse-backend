package tw.com.tymbackend.module.weapon.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.domain.vo.WeaponId;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class WeaponService {
    
    private final WeaponRepository weaponRepository;
    
    public WeaponService(WeaponRepository weaponRepository) {
        this.weaponRepository = weaponRepository;
    }
    
    /**
     * Get all weapons
     * 
     * @return list of all weapons
     */
    public List<Weapon> getAllWeapons() {
        return weaponRepository.findAll();
    }
    
    /**
     * Get weapons by owner name
     * 
     * @param name the owner name
     * @return list of weapons owned by the person
     */
    public List<Weapon> getWeaponsByOwnerName(String name) {
        return weaponRepository.findByName(name)
                .map(List::of)
                .orElse(List.of());
    }
    
    /**
     * Get weapon by weapon (ID)
     */
    public Optional<Weapon> getWeaponById(String name, String weapon) {
        return weaponRepository.findById(new WeaponId(name, weapon));
    }
    
    /**
     * Save or update a weapon
     */
    @Transactional
    public Weapon saveWeapon(Weapon weapon) {
        return weaponRepository.save(weapon);
    }
    
    /**
     * Save or update a weapon with smart field update
     * Only updates non-null and non-empty string fields
     */
    @Transactional
    public Weapon saveWeaponSmart(Weapon weapon) {
        // 檢查是否為更新操作（武器已存在）
        if (weapon.getName() != null && weapon.getWeapon() != null) {
            Optional<Weapon> existingWeapon = weaponRepository.findById(new WeaponId(weapon.getName(), weapon.getWeapon()));
            if (existingWeapon.isPresent()) {
                return updateWeaponSmart(existingWeapon.get(), weapon);
            }
        }
        
        // 如果是新武器，直接保存
        return weaponRepository.save(weapon);
    }
    
    /**
     * Smart update weapon - only update non-null and non-empty fields
     */
    @Transactional
    public Weapon updateWeaponSmart(Weapon existing, Weapon updateData) {
        // 只更新非空且非空字串的欄位
        if (isValidString(updateData.getAttributes())) {
            existing.setAttributes(updateData.getAttributes());
        }
        
        if (updateData.getBaseDamage() != null) {
            existing.setBaseDamage(updateData.getBaseDamage());
        }
        
        if (updateData.getBonusDamage() != null) {
            existing.setBonusDamage(updateData.getBonusDamage());
        }
        
        if (updateData.getBonusAttributes() != null && !updateData.getBonusAttributes().isEmpty()) {
            existing.setBonusAttributes(updateData.getBonusAttributes());
        }
        
        if (updateData.getStateAttributes() != null && !updateData.getStateAttributes().isEmpty()) {
            existing.setStateAttributes(updateData.getStateAttributes());
        }
        
        if (isValidString(updateData.getEmbedding())) {
            existing.setEmbedding(updateData.getEmbedding());
        }
        
        // 更新時間戳
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        
        return weaponRepository.save(existing);
    }
    
    /**
     * Check if a string is valid (not null and not empty)
     */
    private boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Delete a weapon by weapon (ID)
     */
    @Transactional
    public void deleteWeapon(String name, String weapon) {
        weaponRepository.deleteById(new WeaponId(name, weapon));
    }
    
    /**
     * Delete all weapons
     */
    @Transactional
    public void deleteAllWeapons() {
        weaponRepository.deleteAll();
    }
    
    /**
     * Check if a weapon exists by weapon (ID)
     */
    public boolean weaponExists(String name, String weapon) {
        return weaponRepository.existsById(new WeaponId(name, weapon));
    }
    
    /**
     * Update weapon attributes
     */
    @Transactional
    public Weapon updateWeaponAttributes(String name, String weapon, Weapon newWeapon) {
        return weaponRepository.findById(new WeaponId(name, weapon))
            .map(existing -> {
                existing.setBaseDamage(newWeapon.getBaseDamage());
                existing.setAttributes(newWeapon.getAttributes());
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Update weapon base damage
     */
    @Transactional
    public Weapon updateWeaponBaseDamage(String name, String weapon, Integer baseDamage) {
        return weaponRepository.findById(new WeaponId(name, weapon))
            .map(existing -> {
                existing.setBaseDamage(baseDamage);
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Update weapon bonus damage
     */
    @Transactional
    public Weapon updateWeaponBonusDamage(String name, String weapon, Integer bonusDamage) {
        return weaponRepository.findById(new WeaponId(name, weapon))
            .map(existing -> {
                existing.setBonusDamage(bonusDamage);
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Update weapon bonus attributes
     */
    @Transactional
    public Weapon updateWeaponBonusAttributes(String name, String weapon, List<String> bonusAttributes) {
        return weaponRepository.findById(new WeaponId(name, weapon))
            .map(existing -> {
                existing.setBonusAttributes(bonusAttributes);
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Update weapon state attributes
     */
    @Transactional
    public Weapon updateWeaponStateAttributes(String name, String weapon, List<String> stateAttributes) {
        return weaponRepository.findById(new WeaponId(name, weapon))
            .map(existing -> {
                existing.setStateAttributes(stateAttributes);
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Find weapons by base damage range
     */
    public List<Weapon> findByBaseDamageRange(Integer minDamage, Integer maxDamage) {
        return weaponRepository.findByBaseDamageBetween(minDamage, maxDamage);
    }
    
    /**
     * Find weapons by attribute
     */
    public List<Weapon> findByAttribute(String attribute) {
        return weaponRepository.findByAttributes(attribute);
    }
    
    /**
     * Find weapons by multiple criteria
     */
    public List<Weapon> findByMultipleCriteria(Integer minDamage, Integer maxDamage, String attribute) {
        List<Specification<Weapon>> specs = new ArrayList<>();
        
        if (minDamage != null && maxDamage != null) {
            specs.add((root, query, cb) -> cb.between(root.get("baseDamage"), minDamage, maxDamage));
        }
        
        if (attribute != null) {
            specs.add((root, query, cb) -> cb.like(root.get("attributes"), "%" + attribute + "%"));
        }
        
        Specification<Weapon> combinedSpec = specs.stream()
            .reduce(Specification.where(null), Specification::and);
            
        return weaponRepository.findAll(combinedSpec);
    }
    
    /**
     * Find all weapons with pagination
     */
    public Page<Weapon> findAll(Pageable pageable) {
        return weaponRepository.findAll(pageable);
    }
} 