package tw.com.tymbackend.module.people.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw.com.tymbackend.core.repository.StringPkRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeopleRepository extends StringPkRepository<People> {
    
    Optional<People> findByName(String name);
    
    boolean existsByName(String name);
    
    // 新增：批量查詢，避免N+1問題
    @Query("SELECT p FROM People p WHERE p.name IN :names")
    List<People> findByNamesIn(@Param("names") List<String> names);
    
    // 新增：只查詢名稱，避免載入所有欄位
    @Query("SELECT p.name FROM People p")
    List<String> findAllNames();
    
    // 新增：根據單一屬性查詢
    @Query("SELECT p FROM People p WHERE p.attributes IS NOT NULL AND p.attributes LIKE %:attribute%")
    List<People> findByAttributeContaining(@Param("attribute") String attribute);
}
