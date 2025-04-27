package tw.com.tymbackend.core.repository;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import jakarta.persistence.EntityManager;
import java.io.Serializable;

/**
 * Generic implementation of the StringIdRepository interface.
 * This class provides the actual implementation of the repository methods.
 *
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public class StringIdRepositoryImpl<T, ID extends Serializable> 
        extends SimpleJpaRepository<T, ID> implements StringIdRepository<T, ID> {

    public StringIdRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
    }
    
    @Override
    public ID convertStringIdToEntityId(String stringId) {
        // Default implementation - should be overridden by concrete repositories
        throw new UnsupportedOperationException("convertStringIdToEntityId not implemented");
    }
    
    @Override
    public String convertEntityIdToStringId(ID entityId) {
        // Default implementation - should be overridden by concrete repositories
        throw new UnsupportedOperationException("convertEntityIdToStringId not implemented");
    }
} 