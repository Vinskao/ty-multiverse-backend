package tw.com.tymbackend.module.weapon.domain.vo;

import java.io.Serializable;
import java.util.Objects;

public class WeaponId implements Serializable {
    private String name;
    private String weapon;

    public WeaponId() {}
    public WeaponId(String name, String weapon) {
        this.name = name;
        this.weapon = weapon;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWeapon() { return weapon; }
    public void setWeapon(String weapon) { this.weapon = weapon; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeaponId)) return false;
        WeaponId that = (WeaponId) o;
        return Objects.equals(name, that.name) && Objects.equals(weapon, that.weapon);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, weapon);
    }
    
    @Override
    public String toString() {
        return "WeaponId{" +
                "name='" + name + '\'' +
                ", weapon='" + weapon + '\'' +
                '}';
    }
} 