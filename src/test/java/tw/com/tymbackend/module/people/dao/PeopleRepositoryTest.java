package tw.com.tymbackend.module.people.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import tw.com.tymbackend.config.TestConfig;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
class PeopleRepositoryTest {

    @Autowired
    private PeopleRepository peopleRepository;

    @Test
    void findByName_Success() {
        // Arrange
        People person = new People();
        person.setName("Test Person");
        person.setAge(25);
        peopleRepository.save(person);

        // Act
        Optional<People> found = peopleRepository.findByName("Test Person");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test Person", found.get().getName());
        assertEquals(25, found.get().getAge());
    }

    @Test
    void findByName_NotFound() {
        // Arrange
        // No data in database

        // Act
        Optional<People> found = peopleRepository.findByName("Non Existent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void deleteByName_Success() {
        // Arrange
        People person = new People();
        person.setName("Test Person");
        person.setAge(25);
        peopleRepository.save(person);

        // Act
        peopleRepository.deleteByName("Test Person");

        // Assert
        Optional<People> found = peopleRepository.findByName("Test Person");
        assertFalse(found.isPresent());
    }

    @Test
    void deleteAllPeople_Success() {
        // Arrange
        People person1 = new People();
        person1.setName("Test Person 1");
        person1.setAge(25);
        peopleRepository.save(person1);

        People person2 = new People();
        person2.setName("Test Person 2");
        person2.setAge(30);
        peopleRepository.save(person2);

        // Act
        peopleRepository.deleteAllPeople();

        // Assert
        assertEquals(0, peopleRepository.count());
    }
} 