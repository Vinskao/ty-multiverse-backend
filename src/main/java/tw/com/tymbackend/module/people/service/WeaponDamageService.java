package tw.com.tymbackend.module.people.service;

import org.springframework.stereotype.Service;

import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;

import java.util.List;

/**
 * 武器傷害計算服務
 * 
 * 負責計算角色使用武器時的傷害值，包含基礎傷害和屬性加成計算。
 */
@Service
public class WeaponDamageService {

    private final WeaponService weaponService;
    private final PeopleService peopleService;

    /**
     * 建構函數
     * 
     * @param weaponService 武器服務
     * @param peopleService 角色服務
     */
    public WeaponDamageService(WeaponService weaponService, PeopleService peopleService) {
        this.weaponService = weaponService;
        this.peopleService = peopleService;
    }

    /**
     * 計算角色使用武器的傷害值
     * 當輸入無效時（找不到角色或武器）返回 -1
     *
     * @param name 角色名稱（擁有者）
     * @return 計算出的傷害值，錯誤時返回 -1
     */
    public int calculateDamageWithWeapon(String name) {
        // 獲取角色
        People person = peopleService.getPeopleByName(name).orElse(null);
        if (person == null) {
            // 角色不存在
            return -1;
        }

        int physicPower = safeInt(person.getPhysicPower());
        int magicPower = safeInt(person.getMagicPower());
        int utilityPower = safeInt(person.getUtilityPower());

        // 獲取角色擁有的武器（可能為空）
        List<Weapon> weapons = weaponService.getWeaponsByOwner(name);

        // 如果沒有武器，傷害等於角色自身屬性
        if (weapons == null || weapons.isEmpty()) {
            double damage = physicPower + magicPower + utilityPower;
            return (int) Math.round(damage);
        }

        // 準備角色屬性用於匹配
        String personAttr = person.getAttributes();

        // 從角色自身屬性開始
        double damage = physicPower + magicPower + utilityPower;

        // 使用 Stream API 計算武器傷害
        double weaponDamage = weapons.stream()
            .mapToDouble(weapon -> {
                int baseDamage = safeInt(weapon.getBaseDamage());
                int bonusDamage = safeInt(weapon.getBonusDamage());

                List<String> bonusAttrs = weapon.getBonusAttributes();
                boolean match = bonusAttrs != null && personAttr != null && bonusAttrs.contains(personAttr);

                return match 
                    ? baseDamage + (bonusDamage + physicPower + utilityPower) / 3.0
                    : baseDamage;
            })
            .sum();

        return (int) Math.round(damage + weaponDamage);
    }

    /**
     * 安全地將 Integer 轉換為 int，null 值轉換為 0
     * 
     * @param value 要轉換的 Integer 值
     * @return 轉換後的 int 值，null 時返回 0
     */
    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
} 