package tw.com.tymbackend.module.ckeditor.service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import tw.com.tymbackend.core.service.BaseService;
import tw.com.tymbackend.core.util.DistributedLockUtil;
import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

/**
 * CKEditor 內容管理服務
 * 
 * 此服務類負責處理 CKEditor 編輯器內容的存儲和檢索操作。
 * 使用 Spring 的聲明式事務管理來確保數據庫操作的一致性。
 * 使用分布式鎖防止多個實例同時保存相同內容。
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>保存編輯器內容</li>
 *   <li>根據編輯器名稱查找內容</li>
 *   <li>異步處理內容操作</li>
 * </ul>
 * 
 * @author TY Multiverse Team
 * @version 1.0
 * @since 2024-05-10
 */
@Service
public class EditContentService extends BaseService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    @Autowired
    private DistributedLockUtil distributedLockUtil;
    
    // 從配置文件讀取分布式鎖超時時間
    @Value("${distributed-lock.content-save-timeout}")
    private int contentSaveTimeout;
    
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
     * 此方法將編輯器內容保存到數據庫中。使用分布式鎖防止多個實例同時保存相同內容。
     * 如果保存過程中發生錯誤，將拋出 RuntimeException。
     * 
     * <p>保存流程：</p>
     * <ol>
     *   <li>獲取基於編輯器名稱的分布式鎖</li>
     *   <li>在事務中執行保存操作</li>
     *   <li>自動釋放分布式鎖</li>
     * </ol>
     * 
     * @param editContentVO 要保存的內容對象，包含編輯器名稱和內容
     * @return CompletableFuture&lt;EditContentVO&gt; 保存後的內容對象，包含數據庫生成的ID和其他信息
     * @throws RuntimeException 當數據庫操作失敗或無法獲取分布式鎖時拋出
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<EditContentVO> saveContent(EditContentVO editContentVO) {
        return CompletableFuture.supplyAsync(() -> {
            String lockKey = "content:save:" + editContentVO.getEditor();
            Duration lockTimeout = Duration.ofSeconds(contentSaveTimeout);
            
            return distributedLockUtil.executeWithLock(lockKey, lockTimeout, () -> {
                return transactionTemplate.execute(status -> {
                    try {
                        return editContentRepository.save(editContentVO);
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        throw new RuntimeException("Failed to save editor content", e);
                    }
                });
            });
        });
    }

    /**
     * 根據編輯器名稱查找內容
     * 
     * 此方法用於檢索特定編輯器的內容。如果找不到對應的內容，
     * 將返回空的 Optional。
     * 
     * <p>查詢流程：</p>
     * <ol>
     *   <li>在事務中執行查詢操作</li>
     *   <li>返回查詢結果的 Optional</li>
     * </ol>
     * 
     * @param editor 編輯器名稱，用作查詢的標識符
     * @return CompletableFuture&lt;Optional&lt;EditContentVO&gt;&gt; 包含找到的內容的 Optional，如果未找到則為空
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
     * @return CompletableFuture&lt;Optional&lt;EditContentVO&gt;&gt; 包含找到的內容的 Optional，如果未找到則為空
     * @see #findById(String)
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Optional<EditContentVO>> getContent(String editor) {
        return findById(editor);
    }
}
