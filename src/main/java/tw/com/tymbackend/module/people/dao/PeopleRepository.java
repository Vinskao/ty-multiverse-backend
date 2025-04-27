package tw.com.tymbackend.module.people.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.people.domain.vo.People;

@Repository
public interface PeopleRepository extends JpaRepository<People, Long>, JpaSpecificationExecutor<People>, DataAccessor<People, Long> {

    Optional<People> findByName(String name);

    @Modifying
    @Query("DELETE FROM People")
    void deleteAllPeople();

    void deleteByName(String name);
}
