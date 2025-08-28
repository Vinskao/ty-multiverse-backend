package tw.com.tymbackend.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tw.com.tymbackend.module.people.service.WeaponDamageService;
import tw.com.tymbackend.module.people.service.PeopleService;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 異步結果模擬服務
 * 
 * 用於測試和演示異步處理完成後的結果存儲功能。
 * 在實際生產環境中，這個服務會被獨立的 consumer 服務替代。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Service
public class AsyncResultSimulatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncResultSimulatorService.class);
    
    @Autowired
    private AsyncResultService asyncResultService;
    
    @Autowired
    private WeaponDamageService weaponDamageService;
    
    @Autowired
    private PeopleService peopleService;
    
    /**
     * 模擬傷害計算的異步處理
     * 
     * @param requestId 請求ID
     * @param characterName 角色名稱
     * @return CompletableFuture 處理結果
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> simulateDamageCalculation(String requestId, String characterName) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("開始模擬傷害計算處理: requestId={}, characterName={}", requestId, characterName);
                
                // 模擬處理時間
                Thread.sleep(3000);
                
                // 執行實際的傷害計算
                int damage = weaponDamageService.calculateDamageWithWeapon(characterName);
                
                if (damage == -1) {
                    // 處理失敗
                    asyncResultService.storeFailedResult(requestId, "角色不存在或計算失敗");
                    logger.warn("傷害計算失敗: requestId={}, characterName={}", requestId, characterName);
                } else {
                    // 處理成功
                    asyncResultService.storeCompletedResult(requestId, damage);
                    logger.info("傷害計算完成: requestId={}, characterName={}, damage={}", 
                        requestId, characterName, damage);
                }
                
            } catch (Exception e) {
                logger.error("模擬傷害計算處理失敗: requestId={}, characterName={}", requestId, characterName, e);
                asyncResultService.storeFailedResult(requestId, "處理過程中發生錯誤: " + e.getMessage());
            }
        });
    }
    
    /**
     * 模擬角色列表獲取的異步處理
     * 
     * @param requestId 請求ID
     * @return CompletableFuture 處理結果
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> simulatePeopleGetAll(String requestId) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("開始模擬角色列表獲取處理: requestId={}", requestId);
                
                // 模擬處理時間
                Thread.sleep(2000);
                
                // 執行實際的角色列表獲取
                List<People> peopleList = peopleService.getAllPeopleOptimized();
                
                // 處理成功
                asyncResultService.storeCompletedResult(requestId, peopleList);
                logger.info("角色列表獲取完成: requestId={}, count={}", requestId, peopleList.size());
                
            } catch (Exception e) {
                logger.error("模擬角色列表獲取處理失敗: requestId={}", requestId, e);
                asyncResultService.storeFailedResult(requestId, "處理過程中發生錯誤: " + e.getMessage());
            }
        });
    }
    
    /**
     * 手動觸發傷害計算模擬
     * 
     * @param requestId 請求ID
     * @param characterName 角色名稱
     */
    public void triggerDamageCalculation(String requestId, String characterName) {
        logger.info("手動觸發傷害計算模擬: requestId={}, characterName={}", requestId, characterName);
        simulateDamageCalculation(requestId, characterName);
    }
    
    /**
     * 手動觸發角色列表獲取模擬
     * 
     * @param requestId 請求ID
     */
    public void triggerPeopleGetAll(String requestId) {
        logger.info("手動觸發角色列表獲取模擬: requestId={}", requestId);
        simulatePeopleGetAll(requestId);
    }
}
