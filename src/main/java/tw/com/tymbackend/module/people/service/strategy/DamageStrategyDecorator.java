package tw.com.tymbackend.module.people.service.strategy;

import java.util.List;

import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

/**
 * 傷害計算裝飾器基類。
 * <p>
 * 透過裝飾器模式可以在不改變原有策略的情況下，
 * 疊加額外的傷害計算邏輯。
 * </p>
 */
public abstract class DamageStrategyDecorator implements DamageStrategy {

    protected final DamageStrategy delegate;

    protected DamageStrategyDecorator(DamageStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public int calculateDamage(People people, List<Weapon> weapons) {
        return delegate.calculateDamage(people, weapons);
    }
}
