package tw.com.tymbackend.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import tw.com.tymbackend.domain.vo.People;

public interface PeopleRepository extends JpaRepository<People, String> {

    Optional<People> findByName(String name);

    @Modifying
    @Query("DELETE FROM People")
    void deleteAllPeople();

    void deleteByName(String name);
}
