package tw.com.tymbackend.module.gallery.domain.dto;
import java.util.Objects;

/**
 * 畫廊刪除請求數據傳輸對象
 * 
 * <p>用於接收刪除畫廊項目的請求參數，包含要刪除項目的ID。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public class DeleteByIdRequestDTO {
    
    /**
     * 要刪除的項目ID
     */
    private Integer id;

    /**
     * 默認構造函數
     */
    public DeleteByIdRequestDTO() {
    }

    /**
     * 帶參數的構造函數
     * 
     * @param id 要刪除的項目ID
     */
    public DeleteByIdRequestDTO(Integer id) {
        this.id = id;
    }

    /**
     * 獲取項目ID
     * 
     * @return 項目ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 設置項目ID
     * 
     * @param id 項目ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 設置項目ID並返回當前對象（鏈式調用）
     * 
     * @param id 項目ID
     * @return 當前對象
     */
    public DeleteByIdRequestDTO id(Integer id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DeleteByIdRequestDTO)) {
            return false;
        }
        DeleteByIdRequestDTO deleteByIdRequestDTO = (DeleteByIdRequestDTO) o;
        return Objects.equals(id, deleteByIdRequestDTO.id);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            "}";
    }
    
}
