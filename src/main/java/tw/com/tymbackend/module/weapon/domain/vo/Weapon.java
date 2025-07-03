package tw.com.tymbackend.module.weapon.domain.vo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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

    // Embedding field for semantic search
    @Column(name = "embedding", columnDefinition = "VECTOR(1536)")
    private String embedding;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime updatedAt;

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
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getEmbedding() {
        return this.embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
                && Objects.equals(stateAttributes, weapon.stateAttributes) && Objects.equals(version, weapon.version)
                && Objects.equals(embedding, weapon.embedding) && Objects.equals(createdAt, weapon.createdAt)
                && Objects.equals(updatedAt, weapon.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weapon, attributes, baseDamage, bonusDamage, bonusAttributes, stateAttributes, version,
                          embedding, createdAt, updatedAt);
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
                ", embedding='" + getEmbedding() + "'" +
                ", createdAt='" + getCreatedAt() + "'" +
                ", updatedAt='" + getUpdatedAt() + "'" +
                "}";
    }
} 