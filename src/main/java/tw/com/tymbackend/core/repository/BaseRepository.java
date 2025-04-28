package tw.com.tymbackend.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface that defines common methods for all repository types.
 * This serves as the foundation for creating specific repository types.
 *
 * @param <T> The entity type
 * @param <ID> The ID type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    // Common methods for all repository types can be added here
} 