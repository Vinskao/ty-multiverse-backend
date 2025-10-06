package tw.com.tymbackend.module.people.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人物圖片實體類別，代表系統中的人物圖片資訊
 * 
 * <p>此類別包含人物圖片的基本資訊、代號等詳細資料，
 * 支援樂觀鎖定機制防止並發更新衝突。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "people_image")
@Data
@NoArgsConstructor
public class PeopleImage {
    
    /**
     * 圖片ID（主鍵）
     * <p>作為實體的唯一識別符</p>
     */
    @Id
    private String id;

    /**
     * 樂觀鎖定版本字段
     * <p>用於防止並發更新衝突，每次更新時版本號會自動遞增</p>
     */
    @Version
    @Column(name = "version", nullable = true)
    private Long version = 0L;
    
    /**
     * 人物代號
     * <p>對應人物的代號或暱稱</p>
     */
    @Column(name = "codeName")
    private String codeName;
    
    /**
     * 圖片資料
     * <p>儲存圖片的Base64編碼或圖片URL，用於前端顯示</p>
     */
    @Column(name = "image", columnDefinition = "TEXT")
    private String image;
    
    /**
     * 人物圖片建構函數
     * 
     * @param id 圖片ID
     * @param codeName 人物代號
     * @param image 圖片資料
     */
    public PeopleImage(String id, String codeName, String image) {
        this.id = id;
        this.codeName = codeName;
        this.image = image;
    }
    
    /**
     * 取得圖片ID
     * 
     * @return 圖片ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 設定圖片ID
     * 
     * @param id 圖片ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * 取得人物代號
     * 
     * @return 人物代號
     */
    public String getCodeName() {
        return codeName;
    }
    
    /**
     * 設定人物代號
     * 
     * @param codeName 人物代號
     */
    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }
    
    /**
     * 取得圖片資料
     * 
     * @return 圖片資料
     */
    public String getImage() {
        return image;
    }
    
    /**
     * 設定圖片資料
     * 
     * @param image 圖片資料
     */
    public void setImage(String image) {
        this.image = image;
    }
}
