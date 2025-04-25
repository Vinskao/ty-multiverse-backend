package tw.com.tymbackend.module.livestock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import tw.com.tymbackend.core.factory.RepositoryFactory;
import tw.com.tymbackend.core.service.BaseService;
import tw.com.tymbackend.module.livestock.dao.LivestockRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class LivestockService extends BaseService {

    private final RepositoryFactory repositoryFactory;
    private final LivestockRepository livestockRepository;

    public LivestockService(RepositoryFactory repositoryFactory, LivestockRepository livestockRepository) {
        this.repositoryFactory = repositoryFactory;
        this.livestockRepository = livestockRepository;
    }

    public List<Livestock> getAllLivestock() {
        return repositoryFactory.findAll(Livestock.class);
    }

    public Optional<Livestock> getLivestockById(Long id) {
        return repositoryFactory.findById(Livestock.class, id);
    }

    public List<Livestock> getLivestockByOwner(String owner) {
        return livestockRepository.findByOwner(owner);
    }

    public List<Livestock> getLivestockByBuyer(String buyer) {
        return livestockRepository.findByBuyer(buyer);
    }

    @Transactional
    public Livestock saveLivestock(Livestock livestock) {
        return repositoryFactory.save(livestock);
    }

    @Transactional
    public void deleteLivestock(Long id) {
        repositoryFactory.deleteById(Livestock.class, id);
    }

    @Transactional
    public Livestock updateLivestock(Livestock livestock) {
        return repositoryFactory.updateById(Livestock.class, livestock.getId(), livestock);
    }

    @Transactional
    public Optional<Livestock> getLivestockByName(String livestock) { 
        return livestockRepository.findByLivestock(livestock);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<List<Livestock>> getAllLivestockAsync() {
        return CompletableFuture.completedFuture(
            repositoryFactory.findAll(Livestock.class)
        );
    }
    
    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Livestock> saveLivestockAsync(Livestock livestock) {
        return CompletableFuture.completedFuture(
            executeInTransaction(status -> 
                repositoryFactory.save(livestock)
            )
        );
    }
} 