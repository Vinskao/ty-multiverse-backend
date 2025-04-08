package tw.com.tymbackend.module.weapon.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.List;

@Repository
public interface WeaponRepository extends JpaRepository<Weapon, String> {
    
    /**
     * Find weapon by name
     */
    Weapon findByName(String name);
    
    /**
     * Check if weapon exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Find weapons by attribute
     */
    List<Weapon> findByAttributes(String attributes);
    
    /**
     * Find weapons by base damage range
     */
    List<Weapon> findByBaseDamageBetween(Integer minDamage, Integer maxDamage);
    
    /**
     * Find weapons by total damage (base + bonus) range
     */
    @Query("SELECT w FROM Weapon w WHERE (w.baseDamage + w.bonusDamage) BETWEEN :minDamage AND :maxDamage")
    List<Weapon> findByTotalDamageBetween(@Param("minDamage") Integer minDamage, @Param("maxDamage") Integer maxDamage);
    
    /**
     * Find weapons by bonus attributes
     */
    @Query(value = "SELECT * FROM weapon WHERE :attribute = ANY(bonus_attributes)", nativeQuery = true)
    List<Weapon> findByBonusAttributesContaining(@Param("attribute") String attribute);
    
    /**
     * Find weapons by state attributes
     */
    @Query(value = "SELECT * FROM weapon WHERE :attribute = ANY(state_attributes)", nativeQuery = true)
    List<Weapon> findByStateAttributesContaining(@Param("attribute") String attribute);
    
    /**
     * Find weapons by weapon name
     */
    List<Weapon> findByWeaponName(String weaponName);
    
    /**
     * Find weapons by weapon name containing (case insensitive)
     */
    List<Weapon> findByWeaponNameContainingIgnoreCase(String weaponName);
} 