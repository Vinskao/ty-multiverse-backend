package tw.com.tymbackend.module.weapon.dao;

import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import java.util.Optional;

public interface WeaponRepositoryCustom {
    Optional<Weapon> findById(String id);
} 