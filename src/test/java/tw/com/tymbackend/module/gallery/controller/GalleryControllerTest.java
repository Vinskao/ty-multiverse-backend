package tw.com.tymbackend.module.gallery.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tw.com.tymbackend.module.gallery.domain.dto.DeleteByIdRequestDTO;
import tw.com.tymbackend.module.gallery.domain.dto.GalleryUpdateRequestDTO;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;
import tw.com.tymbackend.module.gallery.service.GalleryService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GalleryControllerTest {

    @Mock
    private GalleryService galleryService;

    @InjectMocks
    private GalleryController galleryController;

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
        when(galleryService.getAllImages()).thenReturn(testGalleryList);

        // Act
        ResponseEntity<List<Gallery>> response = galleryController.getAllImages();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGalleryList, response.getBody());
        verify(galleryService, times(1)).getAllImages();
    }

    @Test
    void getAllImages_Error() {
        // Arrange
        when(galleryService.getAllImages()).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<List<Gallery>> response = galleryController.getAllImages();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(galleryService, times(1)).getAllImages();
    }

    @Test
    void getImageById_Success() {
        // Arrange
        when(galleryService.getImageById(anyInt())).thenReturn(Optional.of(testGallery));

        // Act
        ResponseEntity<Gallery> response = galleryController.getImageById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGallery, response.getBody());
        verify(galleryService, times(1)).getImageById(1);
    }

    @Test
    void getImageById_NotFound() {
        // Arrange
        when(galleryService.getImageById(anyInt())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Gallery> response = galleryController.getImageById(1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(galleryService, times(1)).getImageById(1);
    }

    @Test
    void getImageById_Error() {
        // Arrange
        when(galleryService.getImageById(anyInt())).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<Gallery> response = galleryController.getImageById(1);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(galleryService, times(1)).getImageById(1);
    }

    @Test
    void saveImage_Success() {
        // Arrange
        when(galleryService.saveImage(any(Gallery.class))).thenReturn(testGallery);

        // Act
        ResponseEntity<Gallery> response = galleryController.saveImage(testGallery);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGallery, response.getBody());
        verify(galleryService, times(1)).saveImage(testGallery);
    }

    @Test
    void saveImage_Error() {
        // Arrange
        when(galleryService.saveImage(any(Gallery.class))).thenThrow(new RuntimeException());

        // Act
        ResponseEntity<Gallery> response = galleryController.saveImage(testGallery);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(galleryService, times(1)).saveImage(testGallery);
    }

    @Test
    void updateImage_Success() {
        // Arrange
        GalleryUpdateRequestDTO updateRequest = new GalleryUpdateRequestDTO();
        updateRequest.setId(1);
        updateRequest.setImageBase64("new-base64-image");
        when(galleryService.updateImage(anyInt(), anyString())).thenReturn(testGallery);

        // Act
        ResponseEntity<Gallery> response = galleryController.updateImage(updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGallery, response.getBody());
        verify(galleryService, times(1)).updateImage(1, "new-base64-image");
    }

    @Test
    void updateImage_NotFound() {
        // Arrange
        GalleryUpdateRequestDTO updateRequest = new GalleryUpdateRequestDTO();
        updateRequest.setId(1);
        updateRequest.setImageBase64("new-base64-image");
        when(galleryService.updateImage(anyInt(), anyString())).thenThrow(new RuntimeException("Image not found"));

        // Act
        ResponseEntity<Gallery> response = galleryController.updateImage(updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(galleryService, times(1)).updateImage(1, "new-base64-image");
    }

    @Test
    void updateImage_Error() {
        // Arrange
        GalleryUpdateRequestDTO updateRequest = new GalleryUpdateRequestDTO();
        updateRequest.setId(1);
        updateRequest.setImageBase64("new-base64-image");
        when(galleryService.updateImage(anyInt(), anyString())).thenThrow(new Exception());

        // Act
        ResponseEntity<Gallery> response = galleryController.updateImage(updateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(galleryService, times(1)).updateImage(1, "new-base64-image");
    }

    @Test
    void deleteImage_Success() {
        // Arrange
        DeleteByIdRequestDTO deleteRequest = new DeleteByIdRequestDTO();
        deleteRequest.setId(1);
        doNothing().when(galleryService).deleteImage(anyInt());

        // Act
        ResponseEntity<Void> response = galleryController.deleteImage(deleteRequest);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(galleryService, times(1)).deleteImage(1);
    }

    @Test
    void deleteImage_Error() {
        // Arrange
        DeleteByIdRequestDTO deleteRequest = new DeleteByIdRequestDTO();
        deleteRequest.setId(1);
        doThrow(new RuntimeException()).when(galleryService).deleteImage(anyInt());

        // Act
        ResponseEntity<Void> response = galleryController.deleteImage(deleteRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(galleryService, times(1)).deleteImage(1);
    }
} 