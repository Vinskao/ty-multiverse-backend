package tw.com.tymbackend.module.people.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PeopleResponseDTO {
    private String name;
    private String baseAttributes;
    private String bonusAttributes;
    private String stateAttributes;
    private String nameOriginal;
    private String codeName;
    private int physicPower;
    private int magicPower;
    private int utilityPower;
    private LocalDate dob;
    private String race;
    private String attributes;
    private String gender;
    private String assSize;
    private String boobsSize;
    private int heightCm;
    private int weightKg;
    private String profession;
    private String combat;
    private String favoriteFoods;
    private String job;
    private String physics;
    private String knownAs;
    private String personality;
    private String interest;
    private String likes;
    private String dislikes;
    private String concubine;
    private String faction;
    private int armyId;
    private String armyName;
    private int deptId;
    private String deptName;
    private int originArmyId;
    private String originArmyName;
    private boolean gaveBirth;
    private String email;
    private int age;
    private String proxy;
    private String embedding;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 