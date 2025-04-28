package tw.com.tymbackend.module.livestock.dao;

import org.springframework.stereotype.Repository;
import tw.com.tymbackend.core.repository.IntegerPkRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.util.List;

@Repository
public interface LivestockRepository extends IntegerPkRepository<Livestock> {
    List<Livestock> findByOwner(String owner);
    List<Livestock> findByBuyer(String buyer);
    List<Livestock> findByLivestock(String livestock);
} 