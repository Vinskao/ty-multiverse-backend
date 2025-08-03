package tw.com.tymbackend.module.people.domain.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 人物回應資料傳輸物件
 * 
 * <p>此類別用於封裝人物資訊的 API 回應資料，
 * 包含人物的所有基本資訊和屬性資料。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Data
public class PeopleResponseDTO {
    
    /**
     * 人物名稱
     */
    private String name;
    
    /**
     * 基礎屬性
     */
    private String baseAttributes;
    
    /**
     * 加成屬性
     */
    private String bonusAttributes;
    
    /**
     * 狀態屬性
     */
    private String stateAttributes;
    
    /**
     * 人物原始名稱
     */
    private String nameOriginal;
    
    /**
     * 人物代號
     */
    private String codeName;
    
    /**
     * 物理力量
     */
    private int physicPower;
    
    /**
     * 魔法力量
     */
    private int magicPower;
    
    /**
     * 實用能力
     */
    private int utilityPower;
    
    /**
     * 出生日期
     */
    private LocalDate dob;
    
    /**
     * 種族
     */
    private String race;
    
    /**
     * 屬性
     */
    private String attributes;
    
    /**
     * 性別
     */
    private String gender;
    
    /**
     * 臀部尺寸
     */
    private String assSize;
    
    /**
     * 胸部尺寸
     */
    private String boobsSize;
    
    /**
     * 身高（公分）
     */
    private int heightCm;
    
    /**
     * 體重（公斤）
     */
    private int weightKg;
    
    /**
     * 職業
     */
    private String profession;
    
    /**
     * 戰鬥能力
     */
    private String combat;
    
    /**
     * 喜愛的食物
     */
    private String favoriteFoods;
    
    /**
     * 工作
     */
    private String job;
    
    /**
     * 物理特性
     */
    private String physics;
    
    /**
     * 別名
     */
    private String knownAs;
    
    /**
     * 個性
     */
    private String personality;
    
    /**
     * 興趣
     */
    private String interest;
    
    /**
     * 喜好
     */
    private String likes;
    
    /**
     * 厭惡
     */
    private String dislikes;
    
    /**
     * 後宮
     */
    private String concubine;
    
    /**
     * 派系
     */
    private String faction;
    
    /**
     * 軍隊編號
     */
    private int armyId;
    
    /**
     * 軍隊名稱
     */
    private String armyName;
    
    /**
     * 部門編號
     */
    private int deptId;
    
    /**
     * 部門名稱
     */
    private String deptName;
    
    /**
     * 原始軍隊編號
     */
    private int originArmyId;
    
    /**
     * 原始軍隊名稱
     */
    private String originArmyName;
    
    /**
     * 是否生育
     */
    private boolean gaveBirth;
    
    /**
     * 電子郵件
     */
    private String email;
    
    /**
     * 年齡
     */
    private int age;
    
    /**
     * 代理
     */
    private String proxy;
    
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