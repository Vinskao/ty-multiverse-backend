package tw.com.tymbackend.module.people.service.strategy;

import java.util.List;

import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

/**
 * 傷害計算策略介面。
 * <p>
 * 透過策略模式將傷害計算演算法解耦，
 * 方便未來擴充不同的傷害計算邏輯。
 * </p>
 */
public interface DamageStrategy {

    /**
     * 計算指定角色使用武器後的總傷害。
     *
     * @param people  角色實體
     * @param weapons 角色擁有的武器清單（可為空）
     * @return 計算後的整數傷害值
     */
    int calculateDamage(People people, List<Weapon> weapons);
}
