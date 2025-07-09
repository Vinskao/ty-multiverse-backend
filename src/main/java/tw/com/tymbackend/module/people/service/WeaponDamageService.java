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

        // Fetch weapons owned by the person (may be empty)
        List<Weapon> weapons = weaponService.getWeaponsByOwner(name);
        Weapon weapon = (weapons != null && !weapons.isEmpty()) ? weapons.get(0) : null;

        int physicPower = safeInt(person.getPhysicPower());
        int magicPower = safeInt(person.getMagicPower());
        int utilityPower = safeInt(person.getUtilityPower());

        // If no weapon, damage equals character's own stats
        if (weapon == null) {
            double damage = physicPower + magicPower + utilityPower;
            return (int) Math.round(damage);
        }

        int baseDamage = safeInt(weapon.getBaseDamage());
        int bonusDamage = safeInt(weapon.getBonusDamage());

        String personAttr = person.getAttributes();
        List<String> bonusAttrs = weapon.getBonusAttributes();
        boolean attrMatch = bonusAttrs != null && personAttr != null && bonusAttrs.contains(personAttr);

        double damage;
        if (attrMatch) {
            damage = physicPower
            + magicPower 
            + utilityPower
            + baseDamage 
            + (bonusDamage + physicPower + utilityPower) / 3;
        } else {
            damage = physicPower 
            + magicPower 
            + utilityPower 
            + baseDamage;
        }

        return (int) Math.round(damage);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
} 