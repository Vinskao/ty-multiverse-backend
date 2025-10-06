package tw.com.tymbackend.module.weapon.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 武器實體類別，代表系統中的武器資訊
 * 
 * <p>此類別包含武器的基本資訊、屬性、傷害值等詳細資料，
 * 支援樂觀鎖定機制防止並發更新衝突。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "weapon")
@Data
@NoArgsConstructor
public class Weapon {

    /**
     * 武器名稱（主鍵）
     * <p>作為實體的唯一識別符</p>
     */
    @Id
    @Column(name = "weapon", length = 255)
    @JsonProperty("weapon")
    private String name;

    /**
     * 武器擁有者
     * <p>擁有此武器的人物名稱</p>
     */
    @Column(name = "owner", length = 255)
    private String owner;

    /**
     * 武器屬性
     * <p>武器的基本屬性描述</p>
     */
    @Column(name = "attributes", length = 255)
    private String attributes;

    /**
     * 基礎傷害值
     * <p>武器的基礎攻擊傷害</p>
     */
    @Column(name = "base_damage")
    private Integer baseDamage;

    /**
     * 加成傷害值
     * <p>武器的額外攻擊傷害</p>
     */
    @Column(name = "bonus_damage")
    private Integer bonusDamage;

    /**
     * 加成屬性列表
     * <p>武器的額外屬性，以文字陣列形式儲存</p>
     */
    @Column(name = "bonus_attributes", columnDefinition = "text[]")
    private List<String> bonusAttributes;

    /**
     * 狀態屬性列表
     * <p>武器的狀態相關屬性，以文字陣列形式儲存</p>
     */
    @Column(name = "state_attributes", columnDefinition = "text[]")
    private List<String> stateAttributes;

    /**
     * 嵌入向量
     * <p>用於語義搜尋的嵌入向量，通常由外部AI嵌入服務產生
     * 此欄位不映射到資料庫，僅用於記憶體中的語義搜尋功能</p>
     */
    @Transient
    private String embedding;

    /**
     * 建立時間
     * <p>記錄建立此武器資料的時間戳記</p>
     */
    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     * <p>記錄最後更新此武器資料的時間戳記</p>
     */
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT NOW()")
    private LocalDateTime updatedAt;

    /**
     * 樂觀鎖定版本字段
     * <p>用於防止並發更新衝突，每次更新時版本號會自動遞增</p>
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 武器建構函數
     * 
     * @param name 武器名稱
     * @param owner 武器擁有者
     * @param attributes 武器屬性
     * @param baseDamage 基礎傷害值
     * @param bonusDamage 加成傷害值
     * @param bonusAttributes 加成屬性列表
     * @param stateAttributes 狀態屬性列表
     */
    public Weapon(String name, String owner, String attributes, Integer baseDamage, 
                 Integer bonusDamage, List<String> bonusAttributes, List<String> stateAttributes) {
        this.name = name;
        this.owner = owner;
        this.attributes = attributes;
        this.baseDamage = baseDamage;
        this.bonusDamage = bonusDamage;
        this.bonusAttributes = bonusAttributes;
        this.stateAttributes = stateAttributes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取得武器ID（等同於武器名稱）
     * 
     * @return 武器ID
     */
    @JsonIgnore
    public String getId() {
        return this.name;
    }

    /**
     * 設定武器ID（等同於武器名稱）
     * 
     * @param id 武器ID
     */
    @JsonIgnore
    public void setId(String id) {
        this.name = id;
    }

    /**
     * 取得武器名稱
     * 
     * @return 武器名稱
     */
    public String getName() {
        return this.name;
    }

    /**
     * 設定武器名稱
     * 
     * @param name 武器名稱
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 取得武器擁有者
     * 
     * @return 武器擁有者
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * 設定武器擁有者
     * 
     * @param owner 武器擁有者
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * 取得武器屬性
     * 
     * @return 武器屬性
     */
    public String getAttributes() {
        return this.attributes;
    }

    /**
     * 設定武器屬性
     * 
     * @param attributes 武器屬性
     */
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    /**
     * 取得基礎傷害值
     * 
     * @return 基礎傷害值
     */
    public Integer getBaseDamage() {
        return this.baseDamage;
    }

    /**
     * 設定基礎傷害值
     * 
     * @param baseDamage 基礎傷害值
     */
    public void setBaseDamage(Integer baseDamage) {
        this.baseDamage = baseDamage;
    }

    /**
     * 取得加成傷害值
     * 
     * @return 加成傷害值
     */
    public Integer getBonusDamage() {
        return this.bonusDamage;
    }

    /**
     * 設定加成傷害值
     * 
     * @param bonusDamage 加成傷害值
     */
    public void setBonusDamage(Integer bonusDamage) {
        this.bonusDamage = bonusDamage;
    }

    /**
     * 取得加成屬性列表
     * 
     * @return 加成屬性列表
     */
    public List<String> getBonusAttributes() {
        return this.bonusAttributes;
    }

    /**
     * 設定加成屬性列表
     * 
     * @param bonusAttributes 加成屬性列表
     */
    public void setBonusAttributes(List<String> bonusAttributes) {
        this.bonusAttributes = bonusAttributes;
    }

    /**
     * 取得狀態屬性列表
     * 
     * @return 狀態屬性列表
     */
    public List<String> getStateAttributes() {
        return this.stateAttributes;
    }

    /**
     * 設定狀態屬性列表
     * 
     * @param stateAttributes 狀態屬性列表
     */
    public void setStateAttributes(List<String> stateAttributes) {
        this.stateAttributes = stateAttributes;
    }

    /**
     * 取得嵌入向量
     * 
     * @return 嵌入向量
     */
    public String getEmbedding() {
        return this.embedding;
    }

    /**
     * 設定嵌入向量
     * 
     * @param embedding 嵌入向量
     */
    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    /**
     * 取得建立時間
     * 
     * @return 建立時間
     */
    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    /**
     * 設定建立時間
     * 
     * @param createdAt 建立時間
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 取得更新時間
     * 
     * @return 更新時間
     */
    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    /**
     * 設定更新時間
     * 
     * @param updatedAt 更新時間
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 取得版本號
     * 
     * @return 版本號
     */
    public Long getVersion() {
        return this.version;
    }

    /**
     * 設定版本號
     * 
     * @param version 版本號
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 比較此武器與指定物件是否相等
     * 
     * @param o 要比較的物件
     * @return 如果相等則返回true，否則返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Weapon weapon = (Weapon) o;
        return Objects.equals(name, weapon.name) &&
                Objects.equals(owner, weapon.owner) &&
                Objects.equals(attributes, weapon.attributes) &&
                Objects.equals(baseDamage, weapon.baseDamage) &&
                Objects.equals(bonusDamage, weapon.bonusDamage) &&
                Objects.equals(bonusAttributes, weapon.bonusAttributes) &&
                Objects.equals(stateAttributes, weapon.stateAttributes) &&
                Objects.equals(embedding, weapon.embedding) &&
                Objects.equals(createdAt, weapon.createdAt) &&
                Objects.equals(updatedAt, weapon.updatedAt);
    }

    /**
     * 計算此武器物件的雜湊碼
     * 
     * @return 雜湊碼值
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, owner, attributes, baseDamage, bonusDamage, 
                          bonusAttributes, stateAttributes, embedding, createdAt, updatedAt);
    }

    /**
     * 返回此武器物件的字串表示
     * 
     * @return 包含主要屬性的字串表示
     */
    @Override
    public String toString() {
        return "Weapon{" +
                "name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", attributes='" + attributes + '\'' +
                ", baseDamage=" + baseDamage +
                ", bonusDamage=" + bonusDamage +
                ", bonusAttributes=" + bonusAttributes +
                ", stateAttributes=" + stateAttributes +
                ", embedding='" + embedding + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 