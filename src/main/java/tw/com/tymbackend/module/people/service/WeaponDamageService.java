package tw.com.tymbackend.module.people.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.module.people.domain.dto.BatchDamageRequestDTO;
import tw.com.tymbackend.module.people.domain.dto.BatchDamageResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
     * 批量計算多個角色的武器傷害值
     * 優化：使用批量查詢避免N+1問題
     *
     * @param request 包含角色名稱列表的請求
     * @return 批量傷害計算結果
     */
    public BatchDamageResponseDTO calculateBatchDamageWithWeapon(BatchDamageRequestDTO request) {
        if (request == null || request.getNames() == null || request.getNames().isEmpty()) {
            return new BatchDamageResponseDTO(new HashMap<>(), new ArrayList<>());
        }

        List<String> names = request.getNames();
        
        // 批量查詢所有角色，避免N+1問題
        List<People> people = peopleService.findByNames(names);
        Map<String, People> peopleMap = people.stream()
                .collect(Collectors.toMap(People::getName, p -> p));
        
        // 批量查詢所有武器，避免N+1問題
        List<Weapon> allWeapons = weaponService.getWeaponsByOwners(names);
        Map<String, List<Weapon>> weaponsMap = allWeapons.stream()
                .collect(Collectors.groupingBy(Weapon::getOwner));
        
        Map<String, Integer> damageResults = new HashMap<>();
        List<String> notFoundNames = new ArrayList<>();
        
        for (String name : names) {
            People person = peopleMap.get(name);
            if (person == null) {
                notFoundNames.add(name);
                continue;
            }
            
            List<Weapon> weapons = weaponsMap.getOrDefault(name, new ArrayList<>());
            int damage = damageStrategy.calculateDamage(person, weapons);
            damageResults.put(name, damage);
        }
        
        return new BatchDamageResponseDTO(damageResults, notFoundNames);
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