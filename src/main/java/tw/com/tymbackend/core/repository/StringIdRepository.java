package tw.com.tymbackend.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Generic repository interface for entities that use String as their ID type.
 * This interface can be used by any entity that has a String ID field.
 *
 * @param <T> The entity type
 * @param <ID> The entity-specific ID type
 */
@NoRepositoryBean
public interface StringIdRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    // Add any common methods for String ID repositories here
    
    /**
     * Convert a String ID to the entity-specific ID type
     * 
     * @param stringId The String ID
     * @return The entity-specific ID
     */
    ID convertStringIdToEntityId(String stringId);
    
    /**
     * Convert an entity-specific ID to a String ID
     * 
     * @param entityId The entity-specific ID
     * @return The String ID
     */
    String convertEntityIdToStringId(ID entityId);
} 