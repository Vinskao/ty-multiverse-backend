package tw.com.tymbackend.module.people.service.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

/**
 * 預設傷害計算策略。
 * <p>
 * 將原本 {@code WeaponDamageService} 內部的傷害計算公式抽出，
 * 以方便後續替換或裝飾。
 * </p>
 */
@Component
public class DefaultDamageStrategy implements DamageStrategy {

    @Override
    public int calculateDamage(People people, List<Weapon> weapons) {
        if (people == null) {
            return -1;
        }

        int physicPower = safeInt(people.getPhysicPower());
        int magicPower = safeInt(people.getMagicPower());
        int utilityPower = safeInt(people.getUtilityPower());

        // 基礎是角色自身屬性傷害
        double damage = physicPower + magicPower + utilityPower;

        if (weapons == null || weapons.isEmpty()) {
            return (int) Math.round(damage);
        }

        double weaponDamage = weapons.stream()
                .mapToDouble(w -> safeInt(w.getBaseDamage()))
                .sum();

        return (int) Math.round(damage + weaponDamage);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
