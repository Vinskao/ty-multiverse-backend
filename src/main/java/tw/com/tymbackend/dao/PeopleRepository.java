package tw.com.tymbackend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.tymbackend.domain.People;

public interface PeopleRepository extends JpaRepository<People, Long> {

}
