package tw.com.tymbackend.module.people.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import tw.com.tymbackend.config.TestConfig;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
class PeopleImageRepositoryTest {

    @Autowired
    private PeopleImageRepository peopleImageRepository;

    @Test
    void findByCodeName_Success() {
        // Arrange
        PeopleImage peopleImage = new PeopleImage();
        peopleImage.setCodeName("test-code");
        peopleImage.setImage("test-image-data");
        peopleImageRepository.save(peopleImage);

        // Act
        PeopleImage found = peopleImageRepository.findByCodeName("test-code");

        // Assert
        assertNotNull(found);
        assertEquals("test-code", found.getCodeName());
        assertEquals("test-image-data", found.getImage());
    }

    @Test
    void findByCodeName_NotFound() {
        // Arrange
        // No data in database

        // Act
        PeopleImage found = peopleImageRepository.findByCodeName("non-existent");

        // Assert
        assertNull(found);
    }

    @Test
    void existsByCodeName_True() {
        // Arrange
        PeopleImage peopleImage = new PeopleImage();
        peopleImage.setCodeName("test-code");
        peopleImage.setImage("test-image-data");
        peopleImageRepository.save(peopleImage);

        // Act
        boolean exists = peopleImageRepository.existsByCodeName("test-code");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByCodeName_False() {
        // Arrange
        // No data in database

        // Act
        boolean exists = peopleImageRepository.existsByCodeName("non-existent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_Success() {
        // Arrange
        PeopleImage peopleImage = new PeopleImage();
        peopleImage.setCodeName("test-code");
        peopleImage.setImage("test-image-data");

        // Act
        PeopleImage saved = peopleImageRepository.save(peopleImage);

        // Assert
        assertNotNull(saved);
        assertEquals("test-code", saved.getCodeName());
        assertEquals("test-image-data", saved.getImage());
        assertTrue(peopleImageRepository.existsByCodeName("test-code"));
    }

    @Test
    void delete_Success() {
        // Arrange
        PeopleImage peopleImage = new PeopleImage();
        peopleImage.setCodeName("test-code");
        peopleImage.setImage("test-image-data");
        peopleImageRepository.save(peopleImage);

        // Act
        peopleImageRepository.delete(peopleImage);

        // Assert
        assertFalse(peopleImageRepository.existsByCodeName("test-code"));
    }

    @Test
    void deleteById_Success() {
        // Arrange
        PeopleImage peopleImage = new PeopleImage();
        peopleImage.setCodeName("test-code");
        peopleImage.setImage("test-image-data");
        peopleImageRepository.save(peopleImage);

        // Act
        peopleImageRepository.deleteById("test-code");

        // Assert
        assertFalse(peopleImageRepository.existsByCodeName("test-code"));
    }
} 