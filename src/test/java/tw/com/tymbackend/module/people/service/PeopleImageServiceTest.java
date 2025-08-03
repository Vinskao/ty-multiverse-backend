package tw.com.tymbackend.module.people.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.com.tymbackend.module.people.dao.PeopleImageRepository;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleImageServiceTest {

    @Mock
    private PeopleImageRepository peopleImageRepository;

    @InjectMocks
    private PeopleImageService peopleImageService;

    private PeopleImage testPeopleImage;
    private List<PeopleImage> testPeopleImageList;

    @BeforeEach
    void setUp() {
        testPeopleImage = new PeopleImage();
        testPeopleImage.setId("IMG001");
        testPeopleImage.setCodeName("TEST001");
        testPeopleImage.setImage("base64_encoded_image_data");

        testPeopleImageList = Arrays.asList(testPeopleImage);
    }

    @Test
    void getAllPeopleImages_Success() {
        // Arrange
        when(peopleImageRepository.findAll()).thenReturn(testPeopleImageList);

        // Act
        List<PeopleImage> result = peopleImageService.getAllPeopleImages();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPeopleImage, result.get(0));
        verify(peopleImageRepository, times(1)).findAll();
    }

    @Test
    void getPeopleImageByCodeName_Success() {
        // Arrange
        when(peopleImageRepository.findByCodeName(anyString())).thenReturn(testPeopleImage);

        // Act
        PeopleImage result = peopleImageService.getPeopleImageByCodeName("TEST001");

        // Assert
        assertNotNull(result);
        assertEquals(testPeopleImage, result);
        verify(peopleImageRepository, times(1)).findByCodeName("TEST001");
    }

    @Test
    void getPeopleImageByCodeName_NotFound() {
        // Arrange
        when(peopleImageRepository.findByCodeName(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            peopleImageService.getPeopleImageByCodeName("NONEXISTENT");
        });
        verify(peopleImageRepository, times(1)).findByCodeName("NONEXISTENT");
    }

    @Test
    void savePeopleImage_Success() {
        // Arrange
        when(peopleImageRepository.save(any(PeopleImage.class))).thenReturn(testPeopleImage);

        // Act
        PeopleImage result = peopleImageService.savePeopleImage(testPeopleImage);

        // Assert
        assertNotNull(result);
        assertEquals(testPeopleImage, result);
        verify(peopleImageRepository, times(1)).save(testPeopleImage);
    }

    @Test
    void deletePeopleImage_Success() {
        // Arrange
        when(peopleImageRepository.findByCodeName(anyString())).thenReturn(testPeopleImage);
        doNothing().when(peopleImageRepository).delete(any(PeopleImage.class));

        // Act
        peopleImageService.deletePeopleImage("TEST001");

        // Assert
        verify(peopleImageRepository, times(1)).findByCodeName("TEST001");
        verify(peopleImageRepository, times(1)).delete(testPeopleImage);
    }

    @Test
    void peopleImageExists_Success() {
        // Arrange
        when(peopleImageRepository.existsByCodeName(anyString())).thenReturn(true);

        // Act
        boolean result = peopleImageService.peopleImageExists("TEST001");

        // Assert
        assertTrue(result);
        verify(peopleImageRepository, times(1)).existsByCodeName("TEST001");
    }

    @Test
    void isCodeNameUnique_Success() {
        // Arrange
        when(peopleImageRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(testPeopleImageList);

        // Act
        boolean result = peopleImageService.isCodeNameUnique("TEST001");

        // Assert
        assertTrue(result);
        verify(peopleImageRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class));
    }
} 