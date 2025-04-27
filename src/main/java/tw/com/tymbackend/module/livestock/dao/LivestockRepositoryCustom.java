package tw.com.tymbackend.module.livestock.dao;

import java.util.List;
import java.util.Optional;

import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

public interface LivestockRepositoryCustom {
    List<Livestock> findByOwner(String owner);
    List<Livestock> findByBuyer(String buyer);
    Optional<Livestock> findByLivestock(String livestock);
} 