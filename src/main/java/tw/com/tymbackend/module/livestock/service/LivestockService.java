package tw.com.tymbackend.module.livestock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import tw.com.tymbackend.core.repository.IntegerPkRepository;
import tw.com.tymbackend.module.livestock.dao.LivestockRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class LivestockService {

    private final LivestockRepository livestockRepository;

    public LivestockService(LivestockRepository livestockRepository) {
        this.livestockRepository = livestockRepository;
    }

    public List<Livestock> findAll() {
        return livestockRepository.findAll();
    }
    
    /**
     * Get all livestock
     * 
     * @return list of all livestock
     */
    public List<Livestock> getAllLivestock() {
        return findAll();
    }

    public Optional<Livestock> findById(Integer id) {
        return livestockRepository.findById(id);
    }
    
    /**
     * Get livestock by ID
     * 
     * @param id the livestock ID
     * @return the livestock
     */
    public Optional<Livestock> getLivestockById(Long id) {
        return livestockRepository.findById(id.intValue());
    }

    public Livestock save(Livestock livestock) {
        return livestockRepository.save(livestock);
    }

    public void deleteById(Integer id) {
        livestockRepository.deleteById(id);
    }

    public Livestock update(Integer id, Livestock livestock) {
        if (livestockRepository.existsById(id)) {
            livestock.setId(id);
            return livestockRepository.save(livestock);
        }
        return null;
    }

    public List<Livestock> getLivestockByOwner(String owner) {
        return livestockRepository.findByOwner(owner);
    }

    public List<Livestock> getLivestockByBuyer(String buyer) {
        return livestockRepository.findByBuyer(buyer);
    }

    @Transactional
    public Livestock saveLivestock(Livestock livestock) {
        return livestockRepository.save(livestock);
    }

    @Transactional
    public void deleteLivestock(Integer id) {
        livestockRepository.deleteById(id);
    }

    @Transactional
    public Livestock updateLivestock(Integer id, Livestock livestock) {
        if (livestockRepository.existsById(id)) {
            livestock.setId(id);
            return livestockRepository.save(livestock);
        }
        return null;
    }
    
    /**
     * Update livestock
     * 
     * @param livestock the livestock to update
     * @return the updated livestock
     */
    @Transactional
    public Livestock updateLivestock(Livestock livestock) {
        return livestockRepository.save(livestock);
    }

    /**
     * Get livestock list by name
     * 
     * @param livestock the livestock name
     * @return list of livestock with the given name
     */
    @Transactional
    public List<Livestock> getLivestockListByName(String livestock) { 
        return livestockRepository.findByLivestock(livestock);
    }
    
    /**
     * Get livestock by name
     * 
     * @param livestock the livestock name
     * @return the livestock
     */
    public Optional<Livestock> getLivestockByName(String livestock) {
        List<Livestock> livestockList = livestockRepository.findByLivestock(livestock);
        return livestockList.isEmpty() ? Optional.empty() : Optional.of(livestockList.get(0));
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<Livestock>> getAllLivestockAsync() {
        return CompletableFuture.completedFuture(
            livestockRepository.findAll()
        );
    }
    
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Livestock> saveLivestockAsync(Livestock livestock) {
        return CompletableFuture.completedFuture(
            livestockRepository.save(livestock)
        );
    }
} 