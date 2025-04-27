package tw.com.tymbackend.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of DataAccessor that uses JpaRepository and JpaSpecificationExecutor
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public class RepositoryDataAccessor<T, ID> implements DataAccessor<T, ID> {

    private final JpaRepository<T, ID> repository;
    private final JpaSpecificationExecutor<T> specificationExecutor;

    // Private constructor to prevent direct instantiation
    private RepositoryDataAccessor(JpaRepository<T, ID> repository) {
        this.repository = repository;
        if (repository instanceof JpaSpecificationExecutor) {
            this.specificationExecutor = (JpaSpecificationExecutor<T>) repository;
        } else {
            throw new IllegalArgumentException("Repository must implement JpaSpecificationExecutor");
        }
    }

    // Factory method to create a RepositoryDataAccessor
    public static <T, ID> RepositoryDataAccessor<T, ID> create(JpaRepository<T, ID> repository) {
        return new RepositoryDataAccessor<>(repository);
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        return repository.saveAll(entities);
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public List<T> findAll(Specification<T> spec) {
        return specificationExecutor.findAll(spec);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return specificationExecutor.findAll(spec, pageable);
    }
} 