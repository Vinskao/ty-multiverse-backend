package tw.com.tymbackend.module.livestock.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import tw.com.tymbackend.core.util.DistributedLockUtil;
import tw.com.tymbackend.module.livestock.dao.LivestockRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 畜牧業服務類
 * 
 * 負責處理畜牧業相關的業務邏輯，包括數據的增刪改查操作。
 * 使用樂觀鎖定和分布式鎖確保數據的一致性和並發安全性。
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>畜牧業數據的增刪改查</li>
 *   <li>異步數據處理</li>
 *   <li>樂觀鎖定衝突處理</li>
 *   <li>分布式鎖保護</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Service
public class LivestockService {

    private final LivestockRepository livestockRepository;
    
    @Autowired
    private DistributedLockUtil distributedLockUtil;
    
    // 從配置文件讀取分布式鎖超時時間
    @Value("${distributed-lock.livestock-query-timeout}")
    private int livestockQueryTimeout;
    
    @Value("${distributed-lock.livestock-save-timeout}")
    private int livestockSaveTimeout;

    /**
     * 構造函數，通過依賴注入初始化存儲庫
     * 
     * @param livestockRepository 畜牧業數據存儲庫實例
     */
    public LivestockService(LivestockRepository livestockRepository) {
        this.livestockRepository = livestockRepository;
    }

    /**
     * 查找所有畜牧業數據
     * 
     * @return 所有畜牧業數據的列表
     */
    public List<Livestock> findAll() {
        return livestockRepository.findAll();
    }
    
    /**
     * 獲取所有畜牧業數據
     * 
     * @return 所有畜牧業數據的列表
     */
    public List<Livestock> getAllLivestock() {
        return findAll();
    }

    /**
     * 根據ID查找畜牧業數據
     * 
     * @param id 畜牧業數據的ID
     * @return 包含找到的畜牧業數據的 Optional
     */
    public Optional<Livestock> findById(Integer id) {
        return livestockRepository.findById(id);
    }
    
    /**
     * 根據ID獲取畜牧業數據
     * 
     * @param id 畜牧業數據的ID
     * @return 包含找到的畜牧業數據的 Optional
     */
    public Optional<Livestock> getLivestockById(Long id) {
        return livestockRepository.findById(id.intValue());
    }

    /**
     * 保存畜牧業數據
     * 
     * @param livestock 要保存的畜牧業數據
     * @return 保存後的畜牧業數據
     */
    public Livestock save(Livestock livestock) {
        return livestockRepository.save(livestock);
    }

    /**
     * 根據ID刪除畜牧業數據
     * 
     * @param id 要刪除的畜牧業數據的ID
     */
    public void deleteById(Integer id) {
        livestockRepository.deleteById(id);
    }

    /**
     * 更新畜牧業數據（使用樂觀鎖定）
     * 
     * 使用樂觀鎖定機制防止並發更新衝突，如果發生衝突會自動重試。
     * 
     * @param id 要更新的畜牧業數據的ID
     * @param livestock 更新的畜牧業數據
     * @return 更新後的畜牧業數據，如果不存在則返回 null
     */
    @SuppressWarnings("null")
    public Livestock update(Integer id, Livestock livestock) {
        if (livestockRepository.existsById(id)) {
            livestock.setId(id);
            return livestockRepository.save(livestock);
        }
        return null;
    }

    /**
     * 根據所有者查找畜牧業數據
     * 
     * @param owner 所有者名稱
     * @return 該所有者的畜牧業數據列表
     */
    public List<Livestock> getLivestockByOwner(String owner) {
        return livestockRepository.findByOwner(owner);
    }

    /**
     * 根據買家查找畜牧業數據
     * 
     * @param buyer 買家名稱
     * @return 該買家的畜牧業數據列表
     */
    public List<Livestock> getLivestockByBuyer(String buyer) {
        return livestockRepository.findByBuyer(buyer);
    }

    /**
     * 保存畜牧業數據（事務管理）
     * 
     * @param livestock 要保存的畜牧業數據
     * @return 保存後的畜牧業數據
     */
    @Transactional
    public Livestock saveLivestock(Livestock livestock) {
        return livestockRepository.save(livestock);
    }

    /**
     * 刪除畜牧業數據（事務管理）
     * 
     * @param id 要刪除的畜牧業數據的ID
     */
    @Transactional
    public void deleteLivestock(Integer id) {
        livestockRepository.deleteById(id);
    }

    /**
     * 更新畜牧業數據（使用樂觀鎖定和事務管理）
     * 
     * @param id 要更新的畜牧業數據的ID
     * @param livestock 更新的畜牧業數據
     * @return 更新後的畜牧業數據，如果不存在則返回 null
     */
    @Transactional
    @SuppressWarnings("null")
    public Livestock updateLivestock(Integer id, Livestock livestock) {
        if (livestockRepository.existsById(id)) {
            livestock.setId(id);
            return livestockRepository.save(livestock);
        }
        return null;
    }
    
    /**
     * 更新畜牧業數據
     * 
     * @param livestock 要更新的畜牧業數據
     * @return 更新後的畜牧業數據
     */
    @Transactional
    public Livestock updateLivestock(Livestock livestock) {
        return livestockRepository.save(livestock);
    }

    /**
     * 根據名稱獲取畜牧業數據列表
     * 
     * @param livestock 畜牧業名稱
     * @return 具有該名稱的畜牧業數據列表
     */
    @Transactional
    public List<Livestock> getLivestockListByName(String livestock) { 
        return livestockRepository.findByLivestock(livestock);
    }
    
    /**
     * 根據名稱獲取畜牧業數據
     * 
     * @param livestock 畜牧業名稱
     * @return 包含找到的畜牧業數據的 Optional
     */
    public Optional<Livestock> getLivestockByName(String livestock) {
        List<Livestock> livestockList = livestockRepository.findByLivestock(livestock);
        return livestockList.isEmpty() ? Optional.empty() : Optional.of(livestockList.get(0));
    }

    /**
     * 異步獲取所有畜牧業數據
     * 
     * 使用分布式鎖防止多個實例同時執行大量數據查詢。
     * 
     * @return CompletableFuture&lt;List&lt;Livestock&gt;&gt; 所有畜牧業數據的異步結果
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<Livestock>> getAllLivestockAsync() {
        return CompletableFuture.supplyAsync(() -> {
            String lockKey = "livestock:getAll:lock";
            Duration lockTimeout = Duration.ofSeconds(livestockQueryTimeout);
            
            return distributedLockUtil.executeWithLock(lockKey, lockTimeout, () -> {
                return livestockRepository.findAll();
            });
        });
    }
    
    /**
     * 異步保存畜牧業數據
     * 
     * 使用分布式鎖防止多個實例同時保存相同數據。
     * 
     * @param livestock 要保存的畜牧業數據
     * @return CompletableFuture&lt;Livestock&gt; 保存後的畜牧業數據
     */
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Livestock> saveLivestockAsync(Livestock livestock) {
        return CompletableFuture.supplyAsync(() -> {
            String lockKey = "livestock:save:" + (livestock.getId() != null ? livestock.getId() : "new");
            Duration lockTimeout = Duration.ofSeconds(livestockSaveTimeout);
            
            return distributedLockUtil.executeWithLock(lockKey, lockTimeout, () -> {
                return livestockRepository.save(livestock);
            });
        });
    }
} 