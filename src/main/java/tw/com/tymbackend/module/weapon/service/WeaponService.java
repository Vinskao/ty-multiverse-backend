package tw.com.tymbackend.module.weapon.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

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
     * Get weapons by weapon type
     * 
     * @param weaponType the weapon type
     * @return list of weapons of the specified type
     */
    public List<Weapon> getWeaponsByWeaponType(String weaponType) {
        return weaponRepository.findByWeaponType(weaponType);
    }
    
    /**
     * Get weapon by name (ID)
     */
    public Optional<Weapon> getWeaponById(String name) {
        return weaponRepository.findById(name);
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
     * Embedding field is not handled - managed by external AI services
     */
    @Transactional
    public Weapon saveWeaponSmart(Weapon weapon) {
        // 檢查是否為更新操作（武器已存在）
        if (weapon.getName() != null) {
            Optional<Weapon> existingWeapon = weaponRepository.findById(weapon.getName());
            if (existingWeapon.isPresent()) {
                return updateWeaponSmart(existingWeapon.get(), weapon);
            }
        }
        
        // 如果是新武器，確保不處理 embedding 欄位
        weapon.setEmbedding(null);
        return weaponRepository.save(weapon);
    }
    
    /**
     * Smart update weapon - only update non-null and non-empty fields
     */
    @Transactional
    public Weapon updateWeaponSmart(Weapon existing, Weapon updateData) {
        // 只更新非空且非空字串的欄位
        if (isValidString(updateData.getWeaponType())) {
            existing.setWeaponType(updateData.getWeaponType());
        }
        
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
        
        // 不處理 embedding 欄位 - 由外部 AI 服務管理
        
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
     * Delete a weapon by name (ID)
     */
    @Transactional
    public void deleteWeapon(String name) {
        weaponRepository.deleteById(name);
    }
    
    /**
     * Delete all weapons
     */
    @Transactional
    public void deleteAllWeapons() {
        weaponRepository.deleteAll();
    }
    
    /**
     * Check if a weapon exists by name (ID)
     */
    public boolean weaponExists(String name) {
        return weaponRepository.existsById(name);
    }
    
    /**
     * Update weapon attributes
     */
    @Transactional
    public Weapon updateWeaponAttributes(String name, Weapon newWeapon) {
        return weaponRepository.findById(name)
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
    public Weapon updateWeaponBaseDamage(String name, Integer baseDamage) {
        return weaponRepository.findById(name)
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
    public Weapon updateWeaponBonusDamage(String name, Integer bonusDamage) {
        return weaponRepository.findById(name)
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
    public Weapon updateWeaponBonusAttributes(String name, List<String> bonusAttributes) {
        return weaponRepository.findById(name)
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
    public Weapon updateWeaponStateAttributes(String name, List<String> stateAttributes) {
        return weaponRepository.findById(name)
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