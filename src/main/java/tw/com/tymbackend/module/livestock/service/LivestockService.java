package tw.com.tymbackend.module.livestock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.livestock.dao.LivestockRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class LivestockService {

    private final DataAccessor<Livestock, Long> livestockDataAccessor;
    private final LivestockRepository livestockRepository;

    public LivestockService(DataAccessor<Livestock, Long> livestockDataAccessor, LivestockRepository livestockRepository) {
        this.livestockDataAccessor = livestockDataAccessor;
        this.livestockRepository = livestockRepository;
    }

    public List<Livestock> getAllLivestock() {
        return livestockDataAccessor.findAll();
    }

    public Optional<Livestock> getLivestockById(Long id) {
        return livestockDataAccessor.findById(id);
    }

    public List<Livestock> getLivestockByOwner(String owner) {
        return livestockRepository.findByOwner(owner);
    }

    public List<Livestock> getLivestockByBuyer(String buyer) {
        return livestockRepository.findByBuyer(buyer);
    }

    @Transactional
    public Livestock saveLivestock(Livestock livestock) {
        return livestockDataAccessor.save(livestock);
    }

    @Transactional
    public void deleteLivestock(Long id) {
        livestockDataAccessor.deleteById(id);
    }

    @Transactional
    public Livestock updateLivestock(Livestock livestock) {
        return livestockDataAccessor.save(livestock);
    }

    @Transactional
    public Optional<Livestock> getLivestockByName(String livestock) { 
        return livestockRepository.findByLivestock(livestock);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<Livestock>> getAllLivestockAsync() {
        return CompletableFuture.completedFuture(
            livestockDataAccessor.findAll()
        );
    }
    
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Livestock> saveLivestockAsync(Livestock livestock) {
        return CompletableFuture.completedFuture(
            livestockDataAccessor.save(livestock)
        );
    }
} 