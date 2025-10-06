package tw.com.tymbackend.module.ckeditor.dao;

import org.springframework.stereotype.Repository;
import tw.com.tymbackend.core.repository.StringPkRepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

/**
 * CKEditor 內容存儲庫
 * 
 * 此存儲庫負責處理 CKEditor 內容的數據庫操作。它繼承自 StringPkRepository，
 * 使用 String 類型作為主鍵（editor 名稱）。
 * 
 * 連接管理說明：
 * 1. 所有數據庫操作都在 Spring 事務管理下執行
 * 2. 讀操作（findById, findByEditor）使用只讀事務，優化性能
 * 3. 寫操作（save）使用讀寫事務，確保數據一致性
 * 4. 連接由 Spring Data JPA 自動管理，無需手動關閉
 * 
 * 事務傳播行為：
 * - 如果沒有現有事務，創建新事務
 * - 如果已有事務，加入現有事務
 * 
 * 事務隔離級別：
 * - 默認使用數據庫的默認隔離級別
 * - 讀操作使用只讀事務，提高並發性能
 * 
 * @author TY Multiverse Team
 * @version 1.0
 * @since 2024-05-10
 */
@Repository
public interface EditContentRepository extends StringPkRepository<EditContentVO> {
    
    /**
     * 根據編輯器名稱查詢內容
     * 
     * 此方法在只讀事務中執行，用於檢索特定編輯器的內容。
     * 如果找不到對應的內容，返回 null。
     * 
     * 連接行為：
     * 1. 在方法開始時獲取數據庫連接
     * 2. 執行查詢操作
     * 3. 方法結束時自動釋放連接
     * 
     * @param editor 編輯器名稱，用作查詢條件
     * @return 對應的 EditorContent 實體，如果未找到則返回 null
     */
    @SuppressWarnings("null")
    default EditContentVO findByEditor(String editor) {
        return findById(editor).orElse(null);
    }
}