package tw.com.tymbackend.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Generic data access interface that abstracts common database operations
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public interface DataAccessor<T, ID> {
    
    /**
     * Find all entities
     * @return List of all entities
     */
    List<T> findAll();
    
    /**
     * Find entity by ID
     * @param id The ID to search for
     * @return Optional containing the entity if found
     */
    Optional<T> findById(ID id);
    
    /**
     * Save an entity
     * @param entity The entity to save
     * @return The saved entity
     */
    T save(T entity);
    
    /**
     * Save all entities
     * @param entities List of entities to save
     * @return List of saved entities
     */
    List<T> saveAll(List<T> entities);
    
    /**
     * Delete an entity by ID
     * @param id The ID of the entity to delete
     */
    void deleteById(ID id);
    
    /**
     * Delete all entities
     */
    void deleteAll();
    
    /**
     * Find all entities matching the specification
     * @param spec The specification to match
     * @return List of matching entities
     */
    List<T> findAll(Specification<T> spec);
    
    /**
     * Find all entities with pagination
     * @param pageable The pagination information
     * @return Page of entities
     */
    Page<T> findAll(Pageable pageable);
    
    /**
     * Find all entities matching the specification with pagination
     * @param spec The specification to match
     * @param pageable The pagination information
     * @return Page of matching entities
     */
    Page<T> findAll(Specification<T> spec, Pageable pageable);
} 