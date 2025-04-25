package tw.com.tymbackend.module.people.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;
import tw.com.tymbackend.module.people.service.PeopleImageService;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleImageControllerTest {

    @Mock
    private PeopleImageService peopleImageService;

    @InjectMocks
    private PeopleImageController peopleImageController;

    private PeopleImage testPeopleImage;
    private List<PeopleImage> testPeopleImageList;

    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        testPeopleImage = new PeopleImage();
        testPeopleImage.setCodeName("test-code");
        testPeopleImage.setImage("http://example.com/test.jpg");

        testPeopleImageList = Arrays.asList(testPeopleImage);
    }

    @Test
    void getAllPeopleImages_Success() {
        // Arrange
        when(peopleImageService.getAllPeopleImages()).thenReturn(testPeopleImageList);

        // Act
        ResponseEntity<List<PeopleImage>> response = peopleImageController.getAllPeopleImages();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPeopleImageList, response.getBody());
        verify(peopleImageService, times(1)).getAllPeopleImages();
    }

    @Test
    void getPeopleImageByCodeName_Success() {
        // Arrange
        when(peopleImageService.getPeopleImageByCodeName(anyString())).thenReturn(testPeopleImage);

        // Act
        ResponseEntity<?> response = peopleImageController.getPeopleImageByCodeName("test-code");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPeopleImage, response.getBody());
        verify(peopleImageService, times(1)).getPeopleImageByCodeName("test-code");
    }

    @Test
    void getPeopleImageByCodeName_NotFound() {
        // Arrange
        when(peopleImageService.getPeopleImageByCodeName(anyString()))
            .thenThrow(new NoSuchElementException("Image not found"));

        // Act
        ResponseEntity<?> response = peopleImageController.getPeopleImageByCodeName("non-existent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Image not found", response.getBody());
        verify(peopleImageService, times(1)).getPeopleImageByCodeName("non-existent");
    }

    @Test
    void createPeopleImage_Success() {
        // Arrange
        when(peopleImageService.peopleImageExists(anyString())).thenReturn(false);
        when(peopleImageService.savePeopleImage(any(PeopleImage.class))).thenReturn(testPeopleImage);

        // Act
        ResponseEntity<?> response = peopleImageController.createPeopleImage(testPeopleImage);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testPeopleImage, response.getBody());
        verify(peopleImageService, times(1)).peopleImageExists(testPeopleImage.getCodeName());
        verify(peopleImageService, times(1)).savePeopleImage(testPeopleImage);
    }

    @Test
    void createPeopleImage_AlreadyExists() {
        // Arrange
        when(peopleImageService.peopleImageExists(anyString())).thenReturn(true);

        // Act
        ResponseEntity<?> response = peopleImageController.createPeopleImage(testPeopleImage);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(((String) response.getBody()).contains("Image already exists"));
        verify(peopleImageService, times(1)).peopleImageExists(testPeopleImage.getCodeName());
        verify(peopleImageService, never()).savePeopleImage(any());
    }

    @Test
    void updatePeopleImage_Success() {
        // Arrange
        when(peopleImageService.peopleImageExists(anyString())).thenReturn(true);
        when(peopleImageService.savePeopleImage(any(PeopleImage.class))).thenReturn(testPeopleImage);

        // Act
        ResponseEntity<?> response = peopleImageController.updatePeopleImage("test-code", testPeopleImage);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPeopleImage, response.getBody());
        verify(peopleImageService, times(1)).peopleImageExists("test-code");
        verify(peopleImageService, times(1)).savePeopleImage(testPeopleImage);
    }

    @Test
    void updatePeopleImage_NotFound() {
        // Arrange
        when(peopleImageService.peopleImageExists(anyString())).thenReturn(false);

        // Act
        ResponseEntity<?> response = peopleImageController.updatePeopleImage("non-existent", testPeopleImage);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(((String) response.getBody()).contains("No image found"));
        verify(peopleImageService, times(1)).peopleImageExists("non-existent");
        verify(peopleImageService, never()).savePeopleImage(any());
    }

    @Test
    void deletePeopleImage_Success() {
        // Arrange
        doNothing().when(peopleImageService).deletePeopleImage(anyString());

        // Act
        ResponseEntity<?> response = peopleImageController.deletePeopleImage("test-code");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((String) response.getBody()).contains("Image deleted successfully"));
        verify(peopleImageService, times(1)).deletePeopleImage("test-code");
    }

    @Test
    void deletePeopleImage_NotFound() {
        // Arrange
        doThrow(new NoSuchElementException("Image not found"))
            .when(peopleImageService).deletePeopleImage(anyString());

        // Act
        ResponseEntity<?> response = peopleImageController.deletePeopleImage("non-existent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Image not found", response.getBody());
        verify(peopleImageService, times(1)).deletePeopleImage("non-existent");
    }
} 