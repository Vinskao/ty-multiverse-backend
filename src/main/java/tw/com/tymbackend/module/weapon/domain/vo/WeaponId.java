package tw.com.tymbackend.module.weapon.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class WeaponId implements Serializable {
    
    @Column(name = "name", length = 255)
    private String name;
    
    @Column(name = "weapon", length = 255)
    private String weaponName;
    
    public WeaponId() {
    }
    
    public WeaponId(String name, String weaponName) {
        this.name = name;
        this.weaponName = weaponName;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getWeaponName() {
        return weaponName;
    }
    
    public void setWeaponName(String weaponName) {
        this.weaponName = weaponName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeaponId weaponId = (WeaponId) o;
        return Objects.equals(name, weaponId.name) &&
               Objects.equals(weaponName, weaponId.weaponName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, weaponName);
    }
} 