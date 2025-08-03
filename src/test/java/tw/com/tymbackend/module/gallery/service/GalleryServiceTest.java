package tw.com.tymbackend.module.gallery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.com.tymbackend.module.gallery.dao.GalleryRepository;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {

    @Mock
    private GalleryRepository galleryRepository;

    @InjectMocks
    private GalleryService galleryService;

    private Gallery testGallery;
    private List<Gallery> testGalleryList;

    @BeforeEach
    void setUp() {
        testGallery = new Gallery();
        testGallery.setId(1);
        testGallery.setImageBase64("base64_encoded_image_data");
        testGallery.setUploadTime(LocalDateTime.now());

        testGalleryList = Arrays.asList(testGallery);
    }

    @Test
    void getAllImages_Success() {
        // Arrange
        when(galleryRepository.findAll()).thenReturn(testGalleryList);

        // Act
        List<Gallery> result = galleryService.getAllImages();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGallery, result.get(0));
        verify(galleryRepository, times(1)).findAll();
    }

    @Test
    void getImageById_Success() {
        // Arrange
        when(galleryRepository.findById(anyInt())).thenReturn(Optional.of(testGallery));

        // Act
        Optional<Gallery> result = galleryService.getImageById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGallery, result.get());
        verify(galleryRepository, times(1)).findById(1);
    }

    @Test
    void getImageById_NotFound() {
        // Arrange
        when(galleryRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        Optional<Gallery> result = galleryService.getImageById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(galleryRepository, times(1)).findById(999);
    }

    @Test
    void saveImage_Success() {
        // Arrange
        when(galleryRepository.save(any(Gallery.class))).thenReturn(testGallery);

        // Act
        Gallery result = galleryService.saveImage(testGallery);

        // Assert
        assertNotNull(result);
        assertEquals(testGallery, result);
        verify(galleryRepository, times(1)).save(testGallery);
    }

    @Test
    void updateImage_Success() {
        // Arrange
        when(galleryRepository.findById(anyInt())).thenReturn(Optional.of(testGallery));
        when(galleryRepository.save(any(Gallery.class))).thenReturn(testGallery);

        // Act
        Gallery result = galleryService.updateImage(1, "new_base64_data");

        // Assert
        assertNotNull(result);
        assertEquals(testGallery, result);
        verify(galleryRepository, times(1)).findById(1);
        verify(galleryRepository, times(1)).save(testGallery);
    }

    @Test
    void deleteImage_Success() {
        // Arrange
        doNothing().when(galleryRepository).deleteById(anyInt());

        // Act
        galleryService.deleteImage(1);

        // Assert
        verify(galleryRepository, times(1)).deleteById(1);
    }
} 