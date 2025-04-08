package tw.com.tymbackend.module.weapon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class WeaponService {
    
    @Autowired
    private WeaponRepository weaponRepository;
    
    /**
     * Get all weapons
     * 
     * @return list of all weapons
     */
    public List<Weapon> getAllWeapons() {
        return weaponRepository.findAll();
    }
    
    /**
     * Get weapon by name
     * 
     * @param name the name of the weapon
     * @return the weapon
     * @throws NoSuchElementException if no weapon is found with the given name
     */
    public Weapon getWeaponByName(String name) {
        Optional<Weapon> weapon = Optional.ofNullable(weaponRepository.findByName(name));
        return weapon.orElseThrow(() -> new NoSuchElementException("No weapon found with name: " + name));
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
        return weaponRepository.save(weapon);
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
        weaponRepository.deleteById(name);
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
        Weapon weapon = getWeaponByName(name);
        weapon.setAttributes(attributes);
        return weaponRepository.save(weapon);
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
        Weapon weapon = getWeaponByName(name);
        weapon.setBaseDamage(baseDamage);
        return weaponRepository.save(weapon);
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
        Weapon weapon = getWeaponByName(name);
        weapon.setBonusDamage(bonusDamage);
        return weaponRepository.save(weapon);
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
        Weapon weapon = getWeaponByName(name);
        weapon.setBonusAttributes(bonusAttributes);
        return weaponRepository.save(weapon);
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
        Weapon weapon = getWeaponByName(name);
        weapon.setStateAttributes(stateAttributes);
        return weaponRepository.save(weapon);
    }
} 