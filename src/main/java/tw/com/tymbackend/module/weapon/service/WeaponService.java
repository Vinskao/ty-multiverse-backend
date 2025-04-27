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
     * Get weapon by ID
     * 
     * @param id the weapon ID
     * @return the weapon
     * @throws NoSuchElementException if no weapon is found with the given ID
     */
    public Weapon getWeaponById(String id) {
        return weaponDataAccessor.findById(id)
            .orElseThrow(() -> new NoSuchElementException("No weapon found with ID: " + id));
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
     * Delete a weapon by name
     * 
     * @param name the name of the weapon to delete
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public void deleteWeapon(String name) {
        List<Weapon> weapons = weaponRepository.findByName(name);
        if (weapons.isEmpty()) {
            throw new NoSuchElementException("No weapon found with name: " + name);
        }
        for (Weapon weapon : weapons) {
            weaponDataAccessor.deleteById(weapon.getId());
        }
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
     * @param name the name of the weapon
     * @param attributes the new attributes
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponAttributes(String name, String attributes) {
        List<Weapon> weapons = weaponRepository.findByName(name);
        if (weapons.isEmpty()) {
            throw new NoSuchElementException("No weapon found with name: " + name);
        }
        Weapon weapon = weapons.get(0);
        weapon.setAttributes(attributes);
        return weaponDataAccessor.save(weapon);
    }
    
    /**
     * Update weapon base damage
     * 
     * @param name the name of the weapon
     * @param baseDamage the new base damage
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponBaseDamage(String name, Integer baseDamage) {
        List<Weapon> weapons = weaponRepository.findByName(name);
        if (weapons.isEmpty()) {
            throw new NoSuchElementException("No weapon found with name: " + name);
        }
        Weapon weapon = weapons.get(0);
        weapon.setBaseDamage(baseDamage);
        return weaponDataAccessor.save(weapon);
    }
    
    /**
     * Update weapon bonus damage
     * 
     * @param name the name of the weapon
     * @param bonusDamage the new bonus damage
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponBonusDamage(String name, Integer bonusDamage) {
        List<Weapon> weapons = weaponRepository.findByName(name);
        if (weapons.isEmpty()) {
            throw new NoSuchElementException("No weapon found with name: " + name);
        }
        Weapon weapon = weapons.get(0);
        weapon.setBonusDamage(bonusDamage);
        return weaponDataAccessor.save(weapon);
    }
    
    /**
     * Update weapon bonus attributes
     * 
     * @param name the name of the weapon
     * @param bonusAttributes the new bonus attributes
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponBonusAttributes(String name, List<String> bonusAttributes) {
        List<Weapon> weapons = weaponRepository.findByName(name);
        if (weapons.isEmpty()) {
            throw new NoSuchElementException("No weapon found with name: " + name);
        }
        Weapon weapon = weapons.get(0);
        weapon.setBonusAttributes(bonusAttributes);
        return weaponDataAccessor.save(weapon);
    }
    
    /**
     * Update weapon state attributes
     * 
     * @param name the name of the weapon
     * @param stateAttributes the new state attributes
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponStateAttributes(String name, List<String> stateAttributes) {
        List<Weapon> weapons = weaponRepository.findByName(name);
        if (weapons.isEmpty()) {
            throw new NoSuchElementException("No weapon found with name: " + name);
        }
        Weapon weapon = weapons.get(0);
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