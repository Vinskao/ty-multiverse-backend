package tw.com.tymbackend.module.weapon.domain.vo;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weapon")
@Data
@NoArgsConstructor
@IdClass(WeaponId.class)
public class Weapon {

    @Id
    @Column(name = "name", length = 255)
    private String name;

    @Id
    @Column(name = "weapon", length = 255)
    private String weapon;

    @Column(name = "attributes", length = 255)
    private String attributes;

    @Column(name = "base_damage")
    private Integer baseDamage;

    @Column(name = "bonus_damage")
    private Integer bonusDamage;

    @Column(name = "bonus_attributes", columnDefinition = "text[]")
    private List<String> bonusAttributes;

    @Column(name = "state_attributes", columnDefinition = "text[]")
    private List<String> stateAttributes;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    public Weapon(String name, String weapon, String attributes, Integer baseDamage, 
                 Integer bonusDamage, List<String> bonusAttributes, List<String> stateAttributes) {
        this.name = name;
        this.weapon = weapon;
        this.attributes = attributes;
        this.baseDamage = baseDamage;
        this.bonusDamage = bonusDamage;
        this.bonusAttributes = bonusAttributes;
        this.stateAttributes = stateAttributes;
        this.version = 0L;
    }

    public void initializeVersion() {
        if (this.version == null) {
            this.version = 0L;
        }
    }

    public String getId() {
        return this.weapon;
    }

    public void setId(String id) {
        this.weapon = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeapon() {
        return this.weapon;
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    public String getAttributes() {
        return this.attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public Integer getBaseDamage() {
        return this.baseDamage;
    }

    public void setBaseDamage(Integer baseDamage) {
        this.baseDamage = baseDamage;
    }

    public Integer getBonusDamage() {
        return this.bonusDamage;
    }

    public void setBonusDamage(Integer bonusDamage) {
        this.bonusDamage = bonusDamage;
    }

    public List<String> getBonusAttributes() {
        return this.bonusAttributes;
    }

    public void setBonusAttributes(List<String> bonusAttributes2) {
        this.bonusAttributes = bonusAttributes2;
    }

    public List<String> getStateAttributes() {
        return this.stateAttributes;
    }

    public void setStateAttributes(List<String> stateAttributes2) {
        this.stateAttributes = stateAttributes2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Weapon)) {
            return false;
        }
        Weapon weapon = (Weapon) o;
        return Objects.equals(name, weapon.name) && Objects.equals(this.weapon, weapon.weapon)
                && Objects.equals(attributes, weapon.attributes) && Objects.equals(baseDamage, weapon.baseDamage)
                && Objects.equals(bonusDamage, weapon.bonusDamage) && Objects.equals(bonusAttributes, weapon.bonusAttributes)
                && Objects.equals(stateAttributes, weapon.stateAttributes) && Objects.equals(version, weapon.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weapon, attributes, baseDamage, bonusDamage, bonusAttributes, stateAttributes, version);
    }

    @Override
    public String toString() {
        return "{" +
                " name='" + getName() + "'" +
                ", weapon='" + getWeapon() + "'" +
                ", attributes='" + getAttributes() + "'" +
                ", baseDamage='" + getBaseDamage() + "'" +
                ", bonusDamage='" + getBonusDamage() + "'" +
                ", bonusAttributes='" + getBonusAttributes() + "'" +
                ", stateAttributes='" + getStateAttributes() + "'" +
                ", version='" + getVersion() + "'" +
                "}";
    }
} 