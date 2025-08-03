package tw.com.tymbackend.module.weapon.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 武器回應資料傳輸物件
 * 
 * <p>此類別用於封裝武器資訊的 API 回應資料，
 * 包含武器的所有基本資訊和屬性資料。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Data
public class WeaponResponseDTO {
    
    /**
     * 武器擁有者
     */
    private String owner;
    
    /**
     * 武器名稱
     */
    private String weapon;
    
    /**
     * 武器屬性
     */
    private String attributes;
    
    /**
     * 基礎傷害值
     */
    private Integer baseDamage;
    
    /**
     * 加成傷害值
     */
    private Integer bonusDamage;
    
    /**
     * 加成屬性列表
     */
    private List<String> bonusAttributes;
    
    /**
     * 狀態屬性列表
     */
    private List<String> stateAttributes;
    
    /**
     * 版本號
     */
    private Long version;
    
    /**
     * 嵌入向量
     */
    private String embedding;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
} 