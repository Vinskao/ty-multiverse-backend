package tw.com.tymbackend.module.weapon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.core.factory.QueryConditionFactory;
import tw.com.tymbackend.core.factory.RepositoryFactory;
import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class WeaponService {
    
    private final RepositoryFactory repositoryFactory;
    private final QueryConditionFactory queryConditionFactory;
    private final WeaponRepository weaponRepository;
    
    public WeaponService(RepositoryFactory repositoryFactory,
                         QueryConditionFactory queryConditionFactory,
                         WeaponRepository weaponRepository) {
        this.repositoryFactory = repositoryFactory;
        this.queryConditionFactory = queryConditionFactory;
        this.weaponRepository = weaponRepository;
    }
    
    /**
     * Get all weapons
     * 
     * @return list of all weapons
     */
    public List<Weapon> getAllWeapons() {
        return repositoryFactory.findAll(Weapon.class);
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
     * Save or update a weapon
     * 
     * @param weapon the weapon to save or update
     * @return the saved weapon
     * @throws IllegalArgumentException if weapon name is null or empty
     */
    @Transactional
    public Weapon saveWeapon(Weapon weapon) {
        if (weapon.getName() == null || weapon.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Weapon name cannot be null or empty");
        }
        if (weapon.getWeaponName() == null || weapon.getWeaponName().trim().isEmpty()) {
            throw new IllegalArgumentException("Weapon weapon name cannot be null or empty");
        }
        return repositoryFactory.save(weapon);
    }
    
    /**
     * Delete a weapon by name
     * 
     * @param name the name of the weapon to delete
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public void deleteWeapon(String name) {
        if (!weaponRepository.existsByName(name)) {
            throw new NoSuchElementException("No weapon found with name: " + name);
        }
        repositoryFactory.deleteById(Weapon.class, name);
    }
    
    /**
     * Check if a weapon exists
     * 
     * @param name the name to check
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
        Weapon weapon = getWeaponsByOwnerName(name).get(0);
        weapon.setAttributes(attributes);
        return repositoryFactory.save(weapon);
    }

    /**
     * Update weapon base damage
     * 
     * @param name the name of the weapon
     * @param baseDamage the new base damage value
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponBaseDamage(String name, Integer baseDamage) {
        Weapon weapon = getWeaponsByOwnerName(name).get(0);
        weapon.setBaseDamage(baseDamage);
        return repositoryFactory.save(weapon);
    }

    /**
     * Update weapon bonus damage
     * 
     * @param name the name of the weapon
     * @param bonusDamage the new bonus damage value
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponBonusDamage(String name, Integer bonusDamage) {
        Weapon weapon = getWeaponsByOwnerName(name).get(0);
        weapon.setBonusDamage(bonusDamage);
        return repositoryFactory.save(weapon);
    }

    /**
     * Update weapon bonus attributes
     * 
     * @param name the name of the weapon
     * @param bonusAttributes the new bonus attributes list
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponBonusAttributes(String name, List<String> bonusAttributes) {
        Weapon weapon = getWeaponsByOwnerName(name).get(0);
        weapon.setBonusAttributes(bonusAttributes);
        return repositoryFactory.save(weapon);
    }

    /**
     * Update weapon state attributes
     * 
     * @param name the name of the weapon
     * @param stateAttributes the new state attributes list
     * @return the updated weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    @Transactional
    public Weapon updateWeaponStateAttributes(String name, List<String> stateAttributes) {
        Weapon weapon = getWeaponsByOwnerName(name).get(0);
        weapon.setStateAttributes(stateAttributes);
        return repositoryFactory.save(weapon);
    }

    /**
     * Find weapons by base damage range
     * 
     * @param minDamage minimum base damage
     * @param maxDamage maximum base damage
     * @return list of weapons within the damage range
     */
    public List<Weapon> findByBaseDamageRange(Integer minDamage, Integer maxDamage) {
        Specification<Weapon> spec = queryConditionFactory.createRangeCondition("baseDamage", minDamage, maxDamage);
        return weaponRepository.findAll(spec);
    }
    
    /**
     * Find weapons by attribute
     * 
     * @param attribute the attribute to search for
     * @return list of weapons with the specified attribute
     */
    public List<Weapon> findByAttribute(String attribute) {
        Specification<Weapon> spec = queryConditionFactory.createLikeCondition("attributes", attribute);
        return weaponRepository.findAll(spec);
    }
    
    /**
     * Find weapons by multiple criteria
     * 
     * @param minDamage minimum base damage
     * @param maxDamage maximum base damage
     * @param attribute the attribute to search for
     * @return list of weapons matching all criteria
     */
    public List<Weapon> findByMultipleCriteria(Integer minDamage, Integer maxDamage, String attribute) {
        Specification<Weapon> damageSpec = queryConditionFactory.createRangeCondition("baseDamage", minDamage, maxDamage);
        Specification<Weapon> attributeSpec = queryConditionFactory.createLikeCondition("attributes", attribute);
        Specification<Weapon> combinedSpec = queryConditionFactory.createCompositeCondition(damageSpec, attributeSpec);
        return weaponRepository.findAll(combinedSpec);
    }
    
    /**
     * Find all weapons with pagination
     * 
     * @param pageable the pageable
     * @return the page of weapons
     */
    public Page<Weapon> findAll(Pageable pageable) {
        return repositoryFactory.findAll(Weapon.class, pageable);
    }
} 