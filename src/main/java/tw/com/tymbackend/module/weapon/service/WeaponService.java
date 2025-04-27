package tw.com.tymbackend.module.weapon.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.ArrayList;

@Service
public class WeaponService {
    
    private final DataAccessor<Weapon, String> weaponDataAccessor;
    private final WeaponRepository weaponRepository;
    
    public WeaponService(DataAccessor<Weapon, String> weaponDataAccessor,
                         WeaponRepository weaponRepository) {
        this.weaponDataAccessor = weaponDataAccessor;
        this.weaponRepository = weaponRepository;
    }
    
    /**
     * Get all weapons
     * 
     * @return list of all weapons
     */
    public List<Weapon> getAllWeapons() {
        return weaponDataAccessor.findAll();
    }
    
    /**
     * Get weapons by owner name
     * 
     * @param name the owner name
     * @return list of weapons owned by the person
     */
    public List<Weapon> getWeaponsByOwnerName(String name) {
        return weaponRepository.findByName(name);
    }
    
    /**
     * Get weapon by weapon name (ID)
     * 
     * @param weaponName the weapon name (ID)
     * @return the weapon
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    public Weapon getWeaponByWeaponName(String weaponName) {
        return weaponDataAccessor.findById(weaponName)
            .orElseThrow(() -> new NoSuchElementException("No weapon found with name: " + weaponName));
    }
    
    /**
     * Save or update a weapon
     * 
     * @param weapon the weapon to save or update
     * @return the saved weapon
     */
    @Transactional
    public Weapon saveWeapon(Weapon weapon) {
        return weaponDataAccessor.save(weapon);
    }
    
    /**
     * Delete a weapon by weapon name (ID)
     * 
     * @param weaponName the weapon name (ID) of the weapon to delete
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    @Transactional
    public void deleteWeapon(String weaponName) {
        Weapon weapon = getWeaponByWeaponName(weaponName);
        weaponDataAccessor.deleteById(weaponName);
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
     * @param attributes the new attributes
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given weapon name
     */
    @Transactional
    public Weapon updateWeaponAttributes(String weaponName, String attributes) {
        Weapon weapon = getWeaponByWeaponName(weaponName);
        weapon.setAttributes(attributes);
        return weaponDataAccessor.save(weapon);
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
        Weapon weapon = getWeaponByWeaponName(weaponName);
        weapon.setBaseDamage(baseDamage);
        return weaponDataAccessor.save(weapon);
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
        Weapon weapon = getWeaponByWeaponName(weaponName);
        weapon.setBonusDamage(bonusDamage);
        return weaponDataAccessor.save(weapon);
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
        Weapon weapon = getWeaponByWeaponName(weaponName);
        weapon.setBonusAttributes(bonusAttributes);
        return weaponDataAccessor.save(weapon);
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
        Weapon weapon = getWeaponByWeaponName(weaponName);
        weapon.setStateAttributes(stateAttributes);
        return weaponDataAccessor.save(weapon);
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
            
        return weaponDataAccessor.findAll(combinedSpec);
    }
    
    /**
     * Find all weapons with pagination
     * 
     * @param pageable the pageable
     * @return the page of weapons
     */
    public Page<Weapon> findAll(Pageable pageable) {
        return weaponDataAccessor.findAll(pageable);
    }
} 