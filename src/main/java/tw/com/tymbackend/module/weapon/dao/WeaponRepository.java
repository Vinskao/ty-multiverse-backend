package tw.com.tymbackend.module.weapon.dao;

import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeaponRepository extends JpaRepository<Weapon, String>, JpaSpecificationExecutor<Weapon> {
    
    Optional<Weapon> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Weapon> findByWeaponType(String weaponType);
    
    List<Weapon> findByBaseDamageBetween(Integer minDamage, Integer maxDamage);
    
    List<Weapon> findByAttributes(String attributes);
    
    @Query(value = "SELECT * FROM weapon WHERE :attribute = ANY(bonus_attributes)", nativeQuery = true)
    List<Weapon> findByBonusAttributesContaining(@Param("attribute") String attribute);
    
    @Query(value = "SELECT * FROM weapon WHERE :attribute = ANY(state_attributes)", nativeQuery = true)
    List<Weapon> findByStateAttributesContaining(@Param("attribute") String attribute);
} 