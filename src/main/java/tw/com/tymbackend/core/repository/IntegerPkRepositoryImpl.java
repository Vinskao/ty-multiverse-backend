package tw.com.tymbackend.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.NonNull;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of IntegerPkRepository for entities with Integer primary keys
 * @param <T> The entity type
 */
public class IntegerPkRepositoryImpl<T> extends SimpleJpaRepository<T, Integer> implements IntegerPkRepository<T> {
    
    public IntegerPkRepositoryImpl(JpaEntityInformation<T, Integer> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }
    
    public IntegerPkRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
    }
    
    @Override
    @NonNull
    public Optional<T> findById(@NonNull Integer id) {
        return super.findById(id);
    }
    
    @Override
    public boolean existsById(@NonNull Integer id) {
        return super.existsById(id);
    }
    
    @Override
    public void deleteById(@NonNull Integer id) {
        super.deleteById(id);
    }
    
    @Override
    @NonNull
    public List<T> findAll() {
        return super.findAll();
    }
    
    @Override
    @NonNull
    public Page<T> findAll(@NonNull Pageable pageable) {
        return super.findAll(pageable);
    }
    
    @Override
    @NonNull
    public List<T> findAll(@NonNull Specification<T> spec) {
        return super.findAll(spec);
    }
    
    @Override
    @NonNull
    public Page<T> findAll(@NonNull Specification<T> spec, @NonNull Pageable pageable) {
        return super.findAll(spec, pageable);
    }
} 