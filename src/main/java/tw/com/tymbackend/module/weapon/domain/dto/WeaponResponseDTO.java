package tw.com.tymbackend.module.weapon.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class WeaponResponseDTO {
    private String name;
    private String weapon;
    private String attributes;
    private Integer baseDamage;
    private Integer bonusDamage;
    private List<String> bonusAttributes;
    private List<String> stateAttributes;
    private Long version;
    private String embedding;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 