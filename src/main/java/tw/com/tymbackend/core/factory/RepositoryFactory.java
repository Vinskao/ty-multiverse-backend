package tw.com.tymbackend.core.factory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Factory interface for creating repositories
 */
public interface RepositoryFactory {
    
    /**
     * Get a custom repository instance
     * 
     * @param repositoryClass the repository class
     * @param entityType the entity type
     * @param idType the ID type
     * @param <R> the repository type
     * @param <T> the entity type
     * @param <ID> the ID type
     * @return the repository instance
     */
    <R, T, ID> R getCustomRepository(Class<R> repositoryClass, Class<T> entityType, Class<ID> idType);
    
    /**
     * Get a standard repository instance
     * 
     * @param entityType the entity type
     * @param idType the ID type
     * @param <T> the entity type
     * @param <ID> the ID type
     * @return the repository instance
     */
    <T, ID> JpaRepository<T, ID> getRepository(Class<T> entityType, Class<ID> idType);
    
    /**
     * Get a specification repository instance
     * 
     * @param entityType the entity type
     * @param idType the ID type
     * @param <T> the entity type
     * @param <ID> the ID type
     * @return the repository instance
     */
    <T, ID> JpaSpecificationExecutor<T> getSpecificationRepository(Class<T> entityType, Class<ID> idType);
    
    /**
     * Find all entities
     * 
     * @param entityType the entity type
     * @param <T> the entity type
     * @return list of all entities
     */
    <T> List<T> findAll(Class<T> entityType);
    
    /**
     * Find entity by ID
     * 
     * @param entityType the entity type
     * @param id the ID
     * @param <T> the entity type
     * @param <ID> the ID type
     * @return the entity
     */
    <T, ID> Optional<T> findById(Class<T> entityType, ID id);
    
    /**
     * Delete entity by ID
     * 
     * @param entityType the entity type
     * @param id the ID
     * @param <T> the entity type
     * @param <ID> the ID type
     */
    <T, ID> void deleteById(Class<T> entityType, ID id);
    
    /**
     * Delete all entities
     * 
     * @param entityType the entity type
     * @param <T> the entity type
     */
    <T> void deleteAll(Class<T> entityType);
    
    /**
     * Save entity
     * 
     * @param entity the entity to save
     * @param <T> the entity type
     * @return the saved entity
     */
    <T> T save(T entity);
    
    /**
     * Save all entities
     * 
     * @param entities the entities to save
     * @param <T> the entity type
     * @return the saved entities
     */
    <T> List<T> saveAll(List<T> entities);
    
    /**
     * Update entity by ID
     * 
     * @param entityType the entity type
     * @param id the ID
     * @param entity the entity to update
     * @param <T> the entity type
     * @param <ID> the ID type
     * @return the updated entity
     */
    <T, ID> T updateById(Class<T> entityType, ID id, T entity);
    
    /**
     * Find all entities with pagination
     * 
     * @param entityType the entity type
     * @param pageable the pageable
     * @param <T> the entity type
     * @return the page of entities
     */
    <T> Page<T> findAll(Class<T> entityType, Pageable pageable);
} 