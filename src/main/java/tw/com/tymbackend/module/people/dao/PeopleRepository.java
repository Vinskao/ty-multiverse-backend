package tw.com.tymbackend.module.people.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tw.com.tymbackend.core.repository.IntegerPkRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

@Repository
public interface PeopleRepository extends IntegerPkRepository<People> {

    Optional<People> findByName(String name);

    @Modifying
    @Query("DELETE FROM People")
    void deleteAllPeople();

    void deleteByName(String name);
}
