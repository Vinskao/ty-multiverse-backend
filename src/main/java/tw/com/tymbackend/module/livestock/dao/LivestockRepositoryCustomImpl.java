package tw.com.tymbackend.module.livestock.dao;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

@Repository
public class LivestockRepositoryCustomImpl {
    
    @PersistenceContext
    private EntityManager entityManager;

    public List<Livestock> findByOwner(String owner) {
        TypedQuery<Livestock> query = entityManager.createQuery(
            "SELECT l FROM Livestock l WHERE l.owner = :owner", Livestock.class);
        query.setParameter("owner", owner);
        return query.getResultList();
    }

    public List<Livestock> findByBuyer(String buyer) {
        TypedQuery<Livestock> query = entityManager.createQuery(
            "SELECT l FROM Livestock l WHERE l.buyer = :buyer", Livestock.class);
        query.setParameter("buyer", buyer);
        return query.getResultList();
    }

    public Optional<Livestock> findByLivestock(String livestock) {
        TypedQuery<Livestock> query = entityManager.createQuery(
            "SELECT l FROM Livestock l WHERE l.livestock = :livestock", Livestock.class);
        query.setParameter("livestock", livestock);
        List<Livestock> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
} 