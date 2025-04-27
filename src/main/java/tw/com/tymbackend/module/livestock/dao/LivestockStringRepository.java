package tw.com.tymbackend.module.livestock.dao;

import org.springframework.stereotype.Repository;
import tw.com.tymbackend.core.repository.StringIdRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

import java.util.List;

@Repository
public interface LivestockStringRepository extends StringIdRepository<Livestock, Long> {
    List<Livestock> findByName(String name);
    boolean existsByName(String name);
    List<Livestock> findByType(String type);
    List<Livestock> findByAge(Integer age);
    
    @Override
    default Long convertStringIdToEntityId(String stringId) {
        // Convert string ID to Long
        return Long.parseLong(stringId);
    }
    
    @Override
    default String convertEntityIdToStringId(Long entityId) {
        // Convert Long to string ID
        return String.valueOf(entityId);
    }
} 