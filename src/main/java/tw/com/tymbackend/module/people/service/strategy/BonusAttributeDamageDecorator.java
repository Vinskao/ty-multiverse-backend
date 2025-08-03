package tw.com.tymbackend.module.people.service.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

/**
 * 依照武器加成屬性與角色屬性匹配增加額外傷害的裝飾器。
 */
@Component
public class BonusAttributeDamageDecorator extends DamageStrategyDecorator {

    public BonusAttributeDamageDecorator(DefaultDamageStrategy delegate) {
        super(delegate);
    }

    @Override
    public int calculateDamage(People people, List<Weapon> weapons) {
        int baseDamage = super.calculateDamage(people, weapons);

        if (people == null || weapons == null || weapons.isEmpty()) {
            return baseDamage;
        }

        String personAttr = people.getAttributes();

        double extra = weapons.stream()
                .filter(w -> w.getBonusAttributes() != null && personAttr != null && w.getBonusAttributes().contains(personAttr))
                .mapToDouble(w -> safeInt(w.getBonusDamage()))
                .sum();

        return (int) Math.round(baseDamage + extra);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
