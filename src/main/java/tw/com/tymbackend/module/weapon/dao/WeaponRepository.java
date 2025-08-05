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
    
    List<Weapon> findByOwner(String owner);
    
    // 新增：批量查詢多個擁有者的武器，避免N+1問題
    @Query("SELECT w FROM Weapon w WHERE w.owner IN :owners")
    List<Weapon> findByOwnersIn(@Param("owners") List<String> owners);
    
    List<Weapon> findByBaseDamageBetween(Integer minDamage, Integer maxDamage);
    
    List<Weapon> findByAttributes(String attributes);
    
    @Query(value = "SELECT * FROM weapon WHERE :attribute = ANY(bonus_attributes)", nativeQuery = true)
    List<Weapon> findByBonusAttributesContaining(@Param("attribute") String attribute);
    
    @Query(value = "SELECT * FROM weapon WHERE :attribute = ANY(state_attributes)", nativeQuery = true)
    List<Weapon> findByStateAttributesContaining(@Param("attribute") String attribute);
} 