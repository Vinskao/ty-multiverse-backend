package tw.com.tymbackend.module.gallery.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import tw.com.tymbackend.config.TestConfig;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
class GalleryRepositoryTest {

    @Autowired
    private GalleryRepository galleryRepository;

    @Test
    void save_Success() {
        // Arrange
        Gallery gallery = new Gallery();
        gallery.setImageBase64("test-base64-image");
        gallery.setUploadTime(LocalDateTime.now());
        gallery.setVersion(0L);

        // Act
        Gallery saved = galleryRepository.save(gallery);

        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("test-base64-image", saved.getImageBase64());
    }

    @Test
    void findById_Success() {
        // Arrange
        Gallery gallery = new Gallery();
        gallery.setImageBase64("test-base64-image");
        gallery.setUploadTime(LocalDateTime.now());
        gallery.setVersion(0L);
        Gallery saved = galleryRepository.save(gallery);

        // Act
        Optional<Gallery> found = galleryRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("test-base64-image", found.get().getImageBase64());
    }

    @Test
    void findById_NotFound() {
        // Act
        Optional<Gallery> found = galleryRepository.findById(999);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void delete_Success() {
        // Arrange
        Gallery gallery = new Gallery();
        gallery.setImageBase64("test-base64-image");
        gallery.setUploadTime(LocalDateTime.now());
        gallery.setVersion(0L);
        Gallery saved = galleryRepository.save(gallery);

        // Act
        galleryRepository.delete(saved);

        // Assert
        Optional<Gallery> found = galleryRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void optimisticLocking_Success() {
        // Arrange
        Gallery gallery = new Gallery();
        gallery.setImageBase64("test-base64-image");
        gallery.setUploadTime(LocalDateTime.now());
        gallery.setVersion(0L);
        Gallery saved = galleryRepository.save(gallery);

        // Act
        saved.setImageBase64("updated-base64-image");
        Gallery updated = galleryRepository.save(saved);

        // Assert
        assertEquals(1L, updated.getVersion());
        assertEquals("updated-base64-image", updated.getImageBase64());
    }
} 