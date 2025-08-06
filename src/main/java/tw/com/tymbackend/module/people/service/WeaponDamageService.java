package tw.com.tymbackend.module.people.service;

import org.springframework.cache.annotation.Cacheable;
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
    private final tw.com.tymbackend.module.people.service.strategy.DamageStrategy damageStrategy;

    /**
     * 建構函數
     * 
     * @param weaponService 武器服務
     * @param peopleService 角色服務
     */
    public WeaponDamageService(WeaponService weaponService, PeopleService peopleService,
                               tw.com.tymbackend.module.people.service.strategy.DamageStrategy damageStrategy) {
        this.weaponService = weaponService;
        this.peopleService = peopleService;
        this.damageStrategy = damageStrategy;
    }

    /**
     * 計算角色使用武器的傷害值
     * 當輸入無效時（找不到角色或武器）返回 -1
     * 使用快取機制避免重複查詢
     *
     * @param name 角色名稱（擁有者）
     * @return 計算出的傷害值，錯誤時返回 -1
     */
    @SuppressWarnings("null")
    @Cacheable(value = "damage-calculations", key = "#name")
    public int calculateDamageWithWeapon(String name) {
        People person = peopleService.getPeopleByName(name).orElse(null);
        if (person == null) {
            return -1;
        }
        List<Weapon> weapons = weaponService.getWeaponsByOwner(name);
        return damageStrategy.calculateDamage(person, weapons);
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