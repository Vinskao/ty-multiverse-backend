package tw.com.tymbackend.module.weapon.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

@Repository
public interface WeaponRepository extends JpaRepository<Weapon, String> {
    List<Weapon> findByName(String name);
    boolean existsByName(String name);
    List<Weapon> findByBaseDamageBetween(Integer minDamage, Integer maxDamage);
    List<Weapon> findByAttributes(String attribute);
} 