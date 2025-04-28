package tw.com.tymbackend.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Repository implementation for entities that use Integer as their primary key.
 * This interface extends the base repository and adds Integer-specific methods.
 *
 * @param <T> The entity type
 */
@NoRepositoryBean
public interface IntegerPkRepository<T> extends BaseRepository<T, Integer> {
    
    /**
     * Find entity by ID
     * 
     * @param id The ID to search for
     * @return Optional containing the entity if found
     */
    Optional<T> findById(Integer id);
    
    /**
     * Check if an entity exists by ID
     * 
     * @param id The ID to check
     * @return true if the entity exists, false otherwise
     */
    boolean existsById(Integer id);
    
    /**
     * Delete an entity by ID
     * 
     * @param id The ID of the entity to delete
     */
    void deleteById(Integer id);
    
    /**
     * Find all entities
     * @return List of all entities
     */
    List<T> findAll();
    
    /**
     * Find all entities with pagination
     * @param pageable The pagination information
     * @return Page of entities
     */
    Page<T> findAll(Pageable pageable);
    
    /**
     * Find all entities matching the specification
     * @param spec The specification to match
     * @return List of matching entities
     */
    List<T> findAll(Specification<T> spec);
    
    /**
     * Find all entities matching the specification with pagination
     * @param spec The specification to match
     * @param pageable The pagination information
     * @return Page of matching entities
     */
    Page<T> findAll(Specification<T> spec, Pageable pageable);
} 