package tw.com.tymbackend.module.people.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.people.service.PeopleService;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/people")
public class WeaponDamageController {

    private final WeaponService weaponService;
    private final PeopleService peopleService;

    public WeaponDamageController(WeaponService weaponService, PeopleService peopleService) {
        this.weaponService = weaponService;
        this.peopleService = peopleService;
    }

    /**
     * Calculate damage with owner's weapon.
     * Example: /people/damageWithWeapon?name=Draeny
     *
     * @param name person name (owner)
     * @return damageWithWeapon value in JSON {"damageWithWeapon": value}
     */
    @GetMapping("/damageWithWeapon")
    public ResponseEntity<Integer> damageWithWeapon(@RequestParam("name") String name) {
        // Fetch person
        People person = peopleService.getPeopleByName(name)
                .orElse(null);
        if (person == null) {
            return ResponseEntity.badRequest().body(-1);
        }

        // Fetch weapons owned by the person
        List<Weapon> weapons = weaponService.getWeaponsByOwner(name);
        if (weapons == null || weapons.isEmpty()) {
            return ResponseEntity.badRequest().body(-1);
        }

        Weapon weapon = weapons.get(0); // Use first weapon

        int physicPower = safeInt(person.getPhysicPower());
        int magicPower = safeInt(person.getMagicPower());
        int utilityPower = safeInt(person.getUtilityPower());
        int baseDamage = safeInt(weapon.getBaseDamage());
        int bonusDamage = safeInt(weapon.getBonusDamage());

        double ratio = baseDamage > 0 ? (double) utilityPower / baseDamage : 0;

        String personAttr = person.getAttributes();
        List<String> bonusAttrs = weapon.getBonusAttributes();
        boolean attrMatch = bonusAttrs != null && personAttr != null && bonusAttrs.contains(personAttr);

        double damage;
        if (attrMatch) {
            damage = physicPower + magicPower + utilityPower + ratio * bonusDamage;
        } else {
            damage = physicPower + magicPower + utilityPower + utilityPower * ratio;
        }

        int result = (int) Math.round(damage);
        return ResponseEntity.ok(result);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
} 