package tw.com.tymbackend.module.gallery.domain.vo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 畫廊實體類別，代表系統中的圖片展示資訊
 * 
 * <p>此類別包含圖片的基本資訊、上傳時間等詳細資料，
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
@Table(name = "gallery")
public class Gallery {
    
    /**
     * 畫廊項目ID（主鍵）
     * <p>作為實體的唯一識別符，自動遞增</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // 對應資料表中的 id 欄位
    private Integer id;

    /**
     * 圖片Base64編碼
     * <p>儲存圖片的Base64編碼字串，用於前端顯示</p>
     */
    @Column(name = "image_base64", columnDefinition = "TEXT")
    private String imageBase64;

    /**
     * 上傳時間
     * <p>記錄圖片上傳的時間戳記，建立後不可修改</p>
     */
    @Column(name = "upload_time", nullable = false, updatable = false) // 對應資料表中的 upload_time 欄位
    private LocalDateTime uploadTime;

    /**
     * 樂觀鎖定版本字段
     * <p>用於防止並發更新衝突，每次更新時版本號會自動遞增</p>
     */
    @Column(name = "version")
    @Version
    private Long version;
}
