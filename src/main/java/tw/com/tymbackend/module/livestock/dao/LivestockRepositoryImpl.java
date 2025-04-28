package tw.com.tymbackend.module.livestock.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import tw.com.tymbackend.core.repository.IntegerPkRepositoryImpl;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.util.List;

@Repository
public class LivestockRepositoryImpl extends IntegerPkRepositoryImpl<Livestock> implements LivestockRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public LivestockRepositoryImpl(EntityManager entityManager) {
        super(Livestock.class, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public List<Livestock> findByOwner(String owner) {
        TypedQuery<Livestock> query = entityManager.createQuery(
            "SELECT l FROM Livestock l WHERE l.owner = :owner",
            Livestock.class
        );
        query.setParameter("owner", owner);
        return query.getResultList();
    }

    @Override
    public List<Livestock> findByBuyer(String buyer) {
        TypedQuery<Livestock> query = entityManager.createQuery(
            "SELECT l FROM Livestock l WHERE l.buyer = :buyer",
            Livestock.class
        );
        query.setParameter("buyer", buyer);
        return query.getResultList();
    }

    @Override
    public List<Livestock> findByLivestock(String livestock) {
        TypedQuery<Livestock> query = entityManager.createQuery(
            "SELECT l FROM Livestock l WHERE l.livestock = :livestock",
            Livestock.class
        );
        query.setParameter("livestock", livestock);
        return query.getResultList();
    }
} 