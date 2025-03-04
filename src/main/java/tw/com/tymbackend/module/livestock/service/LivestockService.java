package tw.com.tymbackend.module.livestock.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import tw.com.tymbackend.core.service.BaseService;
import tw.com.tymbackend.module.livestock.dao.LivestockRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class LivestockService extends BaseService {

    @Autowired
    private LivestockRepository livestockRepository;

    public List<Livestock> getAllLivestock() {
        return livestockRepository.findAll();
    }

    public Optional<Livestock> getLivestockById(Long id) {
        return livestockRepository.findById(id);
    }

    public List<Livestock> getLivestockByOwner(String owner) {
        return livestockRepository.findByOwner(owner);
    }

    @Transactional
    public Livestock saveLivestock(Livestock livestock) {
        return livestockRepository.save(livestock);
    }

    @Transactional
    public void deleteLivestock(Long id) {
        livestockRepository.deleteById(id);
    }

    @Transactional
    public Livestock updateLivestock(Livestock livestock) {
        if (!livestockRepository.existsById(livestock.getId())) {
            throw new RuntimeException("Livestock not found with id: " + livestock.getId());
        }
        return livestockRepository.save(livestock);
    }

    @Transactional
    public Optional<Livestock> getLivestockByName(String livestock) { 
        return livestockRepository.findByLivestock(livestock);
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
            executeInTransaction(status -> 
                livestockRepository.save(livestock)
            )
        );
    }
} 