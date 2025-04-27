package tw.com.tymbackend.module.people.dao;

import org.springframework.stereotype.Repository;
import tw.com.tymbackend.core.repository.StringIdRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.List;

@Repository
public interface PeopleStringRepository extends StringIdRepository<People, Long> {
    List<People> findByName(String name);
    boolean existsByName(String name);
    List<People> findByAge(Integer age);
    List<People> findByGender(String gender);
    
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