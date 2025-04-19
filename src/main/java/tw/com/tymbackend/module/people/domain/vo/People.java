package tw.com.tymbackend.module.people.domain.vo;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "people")
public class People {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_attributes")
    private String baseAttributes;

    @Column(name = "bonus_attributes")
    private String bonusAttributes;

    @Column(name = "state_attributes")
    private String stateAttributes;

    @Column(name = "name_original", columnDefinition = "VARCHAR(255)")
    private String nameOriginal;

    @Column(name = "code_name", columnDefinition = "VARCHAR(255)")
    private String codeName;

    @Column(name = "physic_power", columnDefinition = "INT")
    private int physicPower;

    @Column(name = "magic_power", columnDefinition = "INT")
    private int magicPower;

    @Column(name = "utility_power", columnDefinition = "INT")
    private int utilityPower;

    @Column(name = "dob", columnDefinition = "DATE")
    private LocalDate dob;

    @Column(name = "race", columnDefinition = "VARCHAR(255)")
    private String race;

    @Column(name = "attributes", columnDefinition = "VARCHAR(255)")
    private String attributes;

    @Column(name = "gender", columnDefinition = "VARCHAR(255)")
    private String gender;

    @Column(name = "ass_size", columnDefinition = "VARCHAR(255)")
    private String assSize;

    @Column(name = "boobs_size", columnDefinition = "VARCHAR(255)")
    private String boobsSize;

    @Column(name = "height_cm", columnDefinition = "INT")
    private int heightCm;

    @Column(name = "weight_kg", columnDefinition = "INT")
    private int weightKg;

    @Column(name = "profession", columnDefinition = "VARCHAR(255)")
    private String profession;

    @Column(name = "combat", columnDefinition = "VARCHAR(255)")
    private String combat;

    @Column(name = "favorite_foods", columnDefinition = "VARCHAR(255)")
    private String favoriteFoods;

    @Column(name = "job", columnDefinition = "VARCHAR(255)")
    private String job;

    @Column(name = "physics", columnDefinition = "VARCHAR(255)")
    private String physics;

    @Column(name = "known_as", columnDefinition = "VARCHAR(255)")
    private String knownAs;

    @Column(name = "personally", columnDefinition = "VARCHAR(255)")
    private String personally;

    @Column(name = "interest", columnDefinition = "VARCHAR(255)")
    private String interest;

    @Column(name = "likes", columnDefinition = "VARCHAR(255)")
    private String likes;

    @Column(name = "dislikes", columnDefinition = "VARCHAR(255)")
    private String dislikes;

    @Column(name = "concubine", columnDefinition = "VARCHAR(255)")
    private String concubine;

    @Column(name = "faction", columnDefinition = "VARCHAR(255)")
    private String faction;

    @Column(name = "army_id", columnDefinition = "INT")
    private int armyId;

    @Column(name = "army_name", columnDefinition = "VARCHAR(255)")
    private String armyName;

    @Column(name = "dept_id", columnDefinition = "INT")
    private int deptId;

    @Column(name = "dept_name", columnDefinition = "VARCHAR(255)")
    private String deptName;

    @Column(name = "origin_army_id", columnDefinition = "INT")
    private int originArmyId;

    @Column(name = "origin_army_name", columnDefinition = "VARCHAR(255)")
    private String originArmyName;

    @Column(name = "gave_birth", columnDefinition = "BOOLEAN")
    private boolean gaveBirth;

    @Column(name = "email", columnDefinition = "VARCHAR(255)")
    private String email;

    @Column(name = "age", columnDefinition = "INT")
    private int age;

    @Column(name = "proxy", columnDefinition = "VARCHAR(255)")
    private String proxy;

    @Column(name = "hei", columnDefinition = "VARCHAR(255)")
    private String hei;

    @Column(name = "HRRatio", columnDefinition = "VARCHAR(255)")
    private String HRRatio;

    @Column(name = "physicsFallout4", columnDefinition = "VARCHAR(255)")
    private String physicsFallout4;

    @Column(name = "version", nullable = true)
    private Long version = 0L;

    public void initializeVersion() {
        if (this.version == null) {
            this.version = 0L;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        People people = (People) o;
        return Objects.equals(id, people.id) &&
                Objects.equals(name, people.name) &&
                Objects.equals(baseAttributes, people.baseAttributes) &&
                Objects.equals(bonusAttributes, people.bonusAttributes) &&
                Objects.equals(stateAttributes, people.stateAttributes) &&
                Objects.equals(nameOriginal, people.nameOriginal) &&
                Objects.equals(version, people.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, baseAttributes, bonusAttributes, stateAttributes, nameOriginal, version);
    }

    @Override
    public String toString() {
        return "People{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", baseAttributes='" + baseAttributes + '\'' +
                ", bonusAttributes='" + bonusAttributes + '\'' +
                ", stateAttributes='" + stateAttributes + '\'' +
                ", nameOriginal='" + nameOriginal + '\'' +
                ", version=" + version +
                '}';
    }
}
