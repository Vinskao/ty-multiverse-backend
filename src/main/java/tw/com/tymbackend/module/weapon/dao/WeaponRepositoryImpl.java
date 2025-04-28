package tw.com.tymbackend.module.weapon.dao;

import tw.com.tymbackend.core.repository.StringPkRepositoryImpl;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class WeaponRepositoryImpl extends StringPkRepositoryImpl<Weapon> implements WeaponRepositoryCustom {
    
    private final EntityManager entityManager;
    
    public WeaponRepositoryImpl(EntityManager entityManager) {
        super(Weapon.class, entityManager);
        this.entityManager = entityManager;
    }
    
    @Override
    public Optional<Weapon> findById(String id) {
        String jpql = "SELECT w FROM Weapon w WHERE w.weaponName = :id";
        TypedQuery<Weapon> query = entityManager.createQuery(jpql, Weapon.class);
        query.setParameter("id", id);
        return query.getResultList().stream().findFirst();
    }
} 