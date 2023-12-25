package tw.com.ty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "psmember")
public class PsMemberBean {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name_original")
    private String nameOriginal;

    @Column(name = "code_name")
    private String codeName;

    @Column(name = "name")
    private String name;

    @Column(name = "physic_power")
    private Integer physicPower;

    @Column(name = "magic_power")
    private Integer magicPower;

    @Column(name = "utility_power")
    private Integer utilityPower;

    @Column(name = "dob" , columnDefinition = "date")
    private java.util.Date dob;

    @Column(name = "race")
    private String race;

    @Column(name = "attributes")
    private String attributes;

    @Column(name = "gender")
    private Character gender;

    @Column(name = "ass_size")
    private String assSize;

    @Column(name = "boobs_size")
    private String boobsSize;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "army_name")
    private String armyName;

    @Column(name = "dept_name")
    private String deptName;

    @Column(name = "profession")
    private String profession;

    @Column(name = "combat")
    private String combat;

    @Column(name = "favorite_foods")
    private String favoriteFoods;

    @Column(name = "job")
    private String job;

    @Column(name = "physics")
    private String physics;

    @Column(name = "known_as")
    private String knownAs;

    @Column(name = "email")
    private String email;

    @Column(name = "personally")
    private String personally;

    @Column(name = "main_weapon")
    private String mainWeapon;

    @Column(name = "sub_weapon")
    private String subWeapon;

    @Column(name = "interest")
    private String interest;

    @Column(name = "likes")
    private String likes;

    @Column(name = "dislikes")
    private String dislikes;

    @Column(name = "faction")
    private String faction;

    @Column(name = "concubine")
    private Integer concubine;

    @Column(name = "gave_birth")
    private Integer gaveBirth;

    @Column(name = "army_id")
    private Integer armyId;

    @Column(name = "dept_id")
    private Integer deptId;
}
