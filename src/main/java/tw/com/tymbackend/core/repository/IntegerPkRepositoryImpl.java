package tw.com.tymbackend.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of IntegerPkRepository for entities with Integer primary keys
 * @param <T> The entity type
 */
public class IntegerPkRepositoryImpl<T> extends SimpleJpaRepository<T, Integer> implements IntegerPkRepository<T> {
    
    private final EntityManager entityManager;
    
    public IntegerPkRepositoryImpl(JpaEntityInformation<T, Integer> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }
    
    public IntegerPkRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }
    
    @Override
    public Optional<T> findById(Integer id) {
        return super.findById(id);
    }
    
    @Override
    public boolean existsById(Integer id) {
        return super.existsById(id);
    }
    
    @Override
    public void deleteById(Integer id) {
        super.deleteById(id);
    }
    
    @Override
    public List<T> findAll() {
        return super.findAll();
    }
    
    @Override
    public Page<T> findAll(Pageable pageable) {
        return super.findAll(pageable);
    }
    
    @Override
    public List<T> findAll(Specification<T> spec) {
        return super.findAll(spec);
    }
    
    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return super.findAll(spec, pageable);
    }
} 