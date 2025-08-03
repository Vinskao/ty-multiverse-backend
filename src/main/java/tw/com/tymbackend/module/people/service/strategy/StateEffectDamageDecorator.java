package tw.com.tymbackend.module.people.service.strategy;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

/**
 * 依照武器 stateAttributes 與角色 stateAttributes 產生額外效果的裝飾器。
 * <p>
 * 若武器的 {@code stateAttributes} 含有角色對應的狀態，
 * 會按照狀態數量及基礎傷害加成額外傷害。
 * </p>
 */
@Component
@Primary
public class StateEffectDamageDecorator extends DamageStrategyDecorator {

    public StateEffectDamageDecorator(BonusAttributeDamageDecorator delegate) {
        super(delegate);
    }

    @Override
    public int calculateDamage(People people, List<Weapon> weapons) {
        int base = super.calculateDamage(people, weapons);

        if (people == null || weapons == null || weapons.isEmpty()) {
            return base;
        }

        String personState = people.getStateAttributes();
        if (personState == null) {
            return base;
        }

        double extra = weapons.stream()
                .filter(w -> w.getStateAttributes() != null && w.getStateAttributes().contains(personState))
                .mapToDouble(w -> {
                    int baseDamage = safeInt(w.getBaseDamage());
                    int states = w.getStateAttributes().size();
                    // 每個匹配狀態增加 5% 基礎傷害
                    return baseDamage * 0.05 * states;
                })
                .sum();

        return (int) Math.round(base + extra);
    }

    private int safeInt(Integer v) {
        return v == null ? 0 : v;
    }
}
