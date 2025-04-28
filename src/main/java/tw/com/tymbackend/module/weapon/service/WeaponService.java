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
     * Get weapon by weapon name (ID)
     * 
     * @param weaponName the weapon name (ID)
     * @return the weapon
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    public Optional<Weapon> getWeaponByWeaponName(String weaponName) {
        return weaponRepository.findById(weaponName);
    }
    
    /**
     * Save or update a weapon
     * 
     * @param weapon the weapon to save or update
     * @return the saved weapon
     */
    @Transactional
    public Weapon saveWeapon(Weapon weapon) {
        return weaponRepository.save(weapon);
    }
    
    /**
     * Delete a weapon by weapon name (ID)
     * 
     * @param weaponName the weapon name (ID) of the weapon to delete
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    @Transactional
    public void deleteWeapon(String weaponName) {
        weaponRepository.deleteById(weaponName);
    }
    
    /**
     * Check if a weapon exists
     * 
     * @param name the name of the weapon
     * @return true if weapon exists, false otherwise
     */
    public boolean weaponExists(String name) {
        return weaponRepository.existsByName(name);
    }
    
    /**
     * Update weapon attributes
     * 
     * @param weaponName the weapon name (ID) of the weapon
     * @param weapon the new weapon
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    @Transactional
    public Weapon updateWeaponAttributes(String weaponName, Weapon weapon) {
        return weaponRepository.findById(weaponName)
            .map(existing -> {
                existing.setBaseDamage(weapon.getBaseDamage());
                existing.setAttributes(weapon.getAttributes());
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Update weapon base damage
     * 
     * @param weaponName the weapon name (ID) of the weapon
     * @param baseDamage the new base damage
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    @Transactional
    public Weapon updateWeaponBaseDamage(String weaponName, Integer baseDamage) {
        return weaponRepository.findById(weaponName)
            .map(existing -> {
                existing.setBaseDamage(baseDamage);
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Update weapon bonus damage
     * 
     * @param weaponName the weapon name (ID) of the weapon
     * @param bonusDamage the new bonus damage
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    @Transactional
    public Weapon updateWeaponBonusDamage(String weaponName, Integer bonusDamage) {
        return weaponRepository.findById(weaponName)
            .map(existing -> {
                existing.setBonusDamage(bonusDamage);
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Update weapon bonus attributes
     * 
     * @param weaponName the weapon name (ID) of the weapon
     * @param bonusAttributes the new bonus attributes
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    @Transactional
    public Weapon updateWeaponBonusAttributes(String weaponName, List<String> bonusAttributes) {
        return weaponRepository.findById(weaponName)
            .map(existing -> {
                existing.setBonusAttributes(bonusAttributes);
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Update weapon state attributes
     * 
     * @param weaponName the weapon name (ID) of the weapon
     * @param stateAttributes the new state attributes
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    @Transactional
    public Weapon updateWeaponStateAttributes(String weaponName, List<String> stateAttributes) {
        return weaponRepository.findById(weaponName)
            .map(existing -> {
                existing.setStateAttributes(stateAttributes);
                return weaponRepository.save(existing);
            })
            .orElse(null);
    }
    
    /**
     * Find weapons by base damage range
     * 
     * @param minDamage minimum damage
     * @param maxDamage maximum damage
     * @return list of weapons within the damage range
     */
    public List<Weapon> findByBaseDamageRange(Integer minDamage, Integer maxDamage) {
        return weaponRepository.findByBaseDamageBetween(minDamage, maxDamage);
    }
    
    /**
     * Find weapons by attribute
     * 
     * @param attribute the attribute to search for
     * @return list of weapons with the specified attribute
     */
    public List<Weapon> findByAttribute(String attribute) {
        return weaponRepository.findByAttributes(attribute);
    }
    
    /**
     * Find weapons by multiple criteria
     * 
     * @param minDamage minimum damage
     * @param maxDamage maximum damage
     * @param attribute the attribute to search for
     * @return list of weapons matching all criteria
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
     * 
     * @param pageable the pageable
     * @return the page of weapons
     */
    public Page<Weapon> findAll(Pageable pageable) {
        return weaponRepository.findAll(pageable);
    }
} 