package tw.com.tymbackend.module.ckeditor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.tymbackend.core.service.BaseService;
import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

import java.util.Optional;

/**
 * CKEditor 內容管理服務
 * 
 * 此服務類負責處理 CKEditor 編輯器內容的存儲和檢索操作。
 * 使用 Spring 的聲明式事務管理來確保數據庫操作的一致性。
 * 
 * @author TY Multiverse Team
 * @version 1.0
 * @since 2024-05-10
 */
@Service
@Transactional
public class EditContentService extends BaseService {
    
    /**
     * CKEditor 內容存儲庫
     * 用於執行數據庫操作
     */
    private final EditContentRepository editContentRepository;

    /**
     * 構造函數，通過依賴注入初始化存儲庫
     * 
     * @param editContentRepository CKEditor 內容存儲庫實例
     */
    public EditContentService(EditContentRepository editContentRepository) {
        this.editContentRepository = editContentRepository;
    }

    /**
     * 保存編輯器內容
     * 
     * 此方法將編輯器內容保存到數據庫中。如果保存過程中發生錯誤，
     * 將拋出 RuntimeException。
     * 
     * @param editContentVO 要保存的內容對象，包含編輯器名稱和內容
     * @return 保存後的內容對象，包含數據庫生成的ID和其他信息
     * @throws RuntimeException 當數據庫操作失敗時拋出
     */
    @Transactional
    public EditContentVO save(EditContentVO editContentVO) {
        try {
            return editContentRepository.save(editContentVO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save editor content", e);
        }
    }

    /**
     * 根據編輯器名稱查找內容
     * 
     * 此方法用於檢索特定編輯器的內容。如果找不到對應的內容，
     * 將返回空的 Optional。
     * 
     * @param editor 編輯器名稱，用作查詢的標識符
     * @return 包含找到的內容的 Optional，如果未找到則為空
     * @throws RuntimeException 當數據庫操作失敗時拋出
     */
    @Transactional(readOnly = true)
    public Optional<EditContentVO> findById(String editor) {
        try {
            return editContentRepository.findById(editor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find editor content", e);
        }
    }
    
    /**
     * 保存編輯器內容的便捷方法
     * 
     * 此方法是 save 方法的包裝，提供更直觀的方法名稱。
     * 
     * @param editContentVO 要保存的內容對象
     * @return 保存後的內容對象
     * @see #save(EditContentVO)
     */
    @Transactional
    public EditContentVO saveContent(EditContentVO editContentVO) {
        return save(editContentVO);
    }
    
    /**
     * 獲取編輯器內容的便捷方法
     * 
     * 此方法是 findById 方法的包裝，提供更直觀的方法名稱。
     * 
     * @param editor 編輯器名稱
     * @return 包含找到的內容的 Optional，如果未找到則為空
     * @see #findById(String)
     */
    @Transactional(readOnly = true)
    public Optional<EditContentVO> getContent(String editor) {
        return findById(editor);
    }
}
