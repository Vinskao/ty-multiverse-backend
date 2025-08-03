package tw.com.tymbackend.module.livestock.domain.vo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 牲畜實體類別，代表系統中的牲畜資訊
 * 
 * <p>此類別包含牲畜的基本資訊、屬性、價格等詳細資料，
 * 支援樂觀鎖定機制防止並發更新衝突。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "livestock")
public class Livestock {
    
    /**
     * 牲畜ID（主鍵）
     * <p>作為實體的唯一識別符，自動遞增</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /**
     * 牲畜名稱
     * <p>牲畜的種類或名稱</p>
     */
    @Column(name = "livestock", nullable = false)
    private String livestock;

    /**
     * 身高
     * <p>牲畜的身高，單位為公尺</p>
     */
    @Column(name = "height", nullable = false)
    private Double height;

    /**
     * 體重
     * <p>牲畜的體重，單位為公斤</p>
     */
    @Column(name = "weight", nullable = false)
    private Double weight;

    /**
     * 近戰能力
     * <p>牲畜的近戰攻擊和防禦能力值</p>
     */
    @Column(name = "melee", nullable = false)
    private Integer melee;

    /**
     * 魔法能力
     * <p>牲畜的魔法攻擊和防禦能力值</p>
     */
    @Column(name = "magicka", nullable = false)
    private Integer magicka;

    /**
     * 遠程能力
     * <p>牲畜的遠程攻擊和防禦能力值</p>
     */
    @Column(name = "ranged", nullable = false)
    private Integer ranged;

    /**
     * 售價
     * <p>牲畜的銷售價格，精確到小數點後4位</p>
     */
    @Column(name = "selling_price", precision = 19, scale = 4)
    private BigDecimal sellingPrice;

    /**
     * 買價
     * <p>牲畜的購買價格，精確到小數點後4位</p>
     */
    @Column(name = "buying_price", precision = 19, scale = 4)
    private BigDecimal buyingPrice;

    /**
     * 成交價
     * <p>牲畜的實際成交價格，精確到小數點後4位</p>
     */
    @Column(name = "deal_price", precision = 19, scale = 4)
    private BigDecimal dealPrice;

    /**
     * 買家
     * <p>購買此牲畜的人物名稱</p>
     */
    @Column(name = "buyer")
    private String buyer;

    /**
     * 擁有者
     * <p>擁有此牲畜的人物名稱</p>
     */
    @Column(name = "owner", nullable = false)
    private String owner;

    /**
     * 樂觀鎖定版本字段
     * <p>用於防止並發更新衝突，每次更新時版本號會自動遞增</p>
     */
    @Version
    private Long version;
} 