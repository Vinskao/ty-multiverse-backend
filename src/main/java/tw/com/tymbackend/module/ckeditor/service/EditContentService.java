package tw.com.tymbackend.module.ckeditor.service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import tw.com.tymbackend.core.service.BaseService;
import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

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
public class EditContentService extends BaseService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
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
     * @return CompletableFuture<EditContentVO> 保存後的內容對象，包含數據庫生成的ID和其他信息
     * @throws RuntimeException 當數據庫操作失敗時拋出
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<EditContentVO> saveContent(EditContentVO editContentVO) {
        return CompletableFuture.supplyAsync(() -> {
            return transactionTemplate.execute(status -> {
                try {
                    return editContentRepository.save(editContentVO);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw new RuntimeException("Failed to save editor content", e);
                }
            });
        });
    }

    /**
     * 根據編輯器名稱查找內容
     * 
     * 此方法用於檢索特定編輯器的內容。如果找不到對應的內容，
     * 將返回空的 Optional。
     * 
     * @param editor 編輯器名稱，用作查詢的標識符
     * @return CompletableFuture<Optional<EditContentVO>> 包含找到的內容的 Optional，如果未找到則為空
     * @throws RuntimeException 當數據庫操作失敗時拋出
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Optional<EditContentVO>> findById(String editor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return transactionTemplate.execute(status -> {
                    return editContentRepository.findById(editor);
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to find editor content", e);
            }
        });
    }
    
    /**
     * 獲取編輯器內容的便捷方法
     * 
     * 此方法是 findById 方法的包裝，提供更直觀的方法名稱。
     * 
     * @param editor 編輯器名稱
     * @return CompletableFuture<Optional<EditContentVO>> 包含找到的內容的 Optional，如果未找到則為空
     * @see #findById(String)
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Optional<EditContentVO>> getContent(String editor) {
        return findById(editor);
    }
}
