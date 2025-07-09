package tw.com.tymbackend.module.people.service;

import org.springframework.stereotype.Service;

import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;

import java.util.List;

@Service
public class WeaponDamageService {

    private final WeaponService weaponService;
    private final PeopleService peopleService;

    public WeaponDamageService(WeaponService weaponService, PeopleService peopleService) {
        this.weaponService = weaponService;
        this.peopleService = peopleService;
    }

    /**
     * Calculate damage with owner's weapon.
     * Returns -1 when invalid input (no person or weapon found).
     *
     * @param name person name (owner)
     * @return calculated damage or -1 when error
     */
    public int calculateDamageWithWeapon(String name) {
        // Fetch person
        People person = peopleService.getPeopleByName(name).orElse(null);
        if (person == null) {
            // both collections missing (people not found)
            return -1;
        }

        int physicPower = safeInt(person.getPhysicPower());
        int magicPower = safeInt(person.getMagicPower());
        int utilityPower = safeInt(person.getUtilityPower());

        // Fetch weapons owned by the person (may be empty)
        List<Weapon> weapons = weaponService.getWeaponsByOwner(name);

        // If no weapon, damage equals character's own stats
        if (weapons == null || weapons.isEmpty()) {
            double damage = physicPower + magicPower + utilityPower;
            return (int) Math.round(damage);
        }

        // Prepare person attribute for matching
        String personAttr = person.getAttributes();

        // Start with character's own stats
        double damage = physicPower + magicPower + utilityPower;

        for (Weapon w : weapons) {
            int baseDamage = safeInt(w.getBaseDamage());
            int bonusDamage = safeInt(w.getBonusDamage());

            List<String> bonusAttrs = w.getBonusAttributes();
            boolean match = bonusAttrs != null && personAttr != null && bonusAttrs.contains(personAttr);

            if (match) {
                damage += baseDamage + (bonusDamage + physicPower + utilityPower) / 3.0;
            } else {
                damage += baseDamage;
            }
        }

        return (int) Math.round(damage);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
} 