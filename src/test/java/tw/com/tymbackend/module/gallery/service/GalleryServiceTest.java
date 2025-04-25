package tw.com.tymbackend.module.gallery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import tw.com.tymbackend.core.factory.RepositoryFactory;
import tw.com.tymbackend.module.gallery.dao.GalleryRepository;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private GalleryRepository galleryRepository;

    @InjectMocks
    private GalleryService galleryService;

    private Gallery testGallery;
    private List<Gallery> testGalleryList;

    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        testGallery = new Gallery();
        testGallery.setId(1);
        testGallery.setImageBase64("test-base64-image");
        testGallery.setUploadTime(LocalDateTime.now());
        testGallery.setVersion(0L);

        testGalleryList = Arrays.asList(testGallery);
    }

    @Test
    void getAllImages_Success() {
        // Arrange
        when(repositoryFactory.findAll(Gallery.class)).thenReturn(testGalleryList);

        // Act
        List<Gallery> result = galleryService.getAllImages();

        // Assert
        assertEquals(testGalleryList, result);
        verify(repositoryFactory, times(1)).findAll(Gallery.class);
    }

    @Test
    void getImageById_Success() {
        // Arrange
        when(repositoryFactory.findById(eq(Gallery.class), anyInt())).thenReturn(Optional.of(testGallery));

        // Act
        Optional<Gallery> result = galleryService.getImageById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGallery, result.get());
        verify(repositoryFactory, times(1)).findById(Gallery.class, 1);
    }

    @Test
    void getImageById_NotFound() {
        // Arrange
        when(repositoryFactory.findById(eq(Gallery.class), anyInt())).thenReturn(Optional.empty());

        // Act
        Optional<Gallery> result = galleryService.getImageById(1);

        // Assert
        assertFalse(result.isPresent());
        verify(repositoryFactory, times(1)).findById(Gallery.class, 1);
    }

    @Test
    void saveImage_Success() {
        // Arrange
        when(repositoryFactory.save(any(Gallery.class))).thenReturn(testGallery);

        // Act
        Gallery result = galleryService.saveImage(testGallery);

        // Assert
        assertEquals(testGallery, result);
        verify(repositoryFactory, times(1)).save(testGallery);
    }

    @Test
    void deleteImage_Success() {
        // Arrange
        doNothing().when(repositoryFactory).deleteById(eq(Gallery.class), anyInt());

        // Act
        galleryService.deleteImage(1);

        // Assert
        verify(repositoryFactory, times(1)).deleteById(Gallery.class, 1);
    }

    @Test
    void updateImage_Success() {
        // Arrange
        when(repositoryFactory.findById(eq(Gallery.class), anyInt())).thenReturn(Optional.of(testGallery));
        when(repositoryFactory.save(any(Gallery.class))).thenReturn(testGallery);

        // Act
        Gallery result = galleryService.updateImage(1, "new-base64-image");

        // Assert
        assertEquals("new-base64-image", result.getImageBase64());
        verify(repositoryFactory, times(1)).findById(Gallery.class, 1);
        verify(repositoryFactory, times(1)).save(testGallery);
    }

    @Test
    void updateImage_NotFound() {
        // Arrange
        when(repositoryFactory.findById(eq(Gallery.class), anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            galleryService.updateImage(1, "new-base64-image"));
        assertEquals("Image not found with ID: 1", exception.getMessage());
        verify(repositoryFactory, times(1)).findById(Gallery.class, 1);
        verify(repositoryFactory, never()).save(any());
    }

    @Test
    void updateImage_OptimisticLockingFailure() {
        // Arrange
        when(repositoryFactory.findById(eq(Gallery.class), anyInt())).thenReturn(Optional.of(testGallery));
        when(repositoryFactory.save(any(Gallery.class))).thenThrow(new ObjectOptimisticLockingFailureException(Gallery.class, 1));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            galleryService.updateImage(1, "new-base64-image"));
        assertEquals("圖片已被其他使用者修改，請重新整理後再試。", exception.getMessage());
        verify(repositoryFactory, times(1)).findById(Gallery.class, 1);
        verify(repositoryFactory, times(1)).save(testGallery);
    }
} 