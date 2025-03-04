package tw.com.tymbackend.module.livestock.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivestockRepository extends JpaRepository<Livestock, Long> {
    List<Livestock> findByOwner(String owner);
    List<Livestock> findByBuyer(String buyer);
    Optional<Livestock> findByLivestock(String livestock);
} 