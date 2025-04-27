package tw.com.tymbackend.module.people.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleImageServiceTest {

    @Mock
    private DataAccessor<PeopleImage, String> peopleImageDataAccessor;

    @InjectMocks
    private PeopleImageService peopleImageService;

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
        when(peopleImageDataAccessor.findAll()).thenReturn(testPeopleImageList);

        // Act
        List<PeopleImage> result = peopleImageService.getAllPeopleImages();

        // Assert
        assertEquals(testPeopleImageList, result);
        verify(peopleImageDataAccessor, times(1)).findAll();
    }

    @Test
    void getPeopleImageByCodeName_Success() {
        // Arrange
        when(peopleImageDataAccessor.findById(anyString()))
            .thenReturn(Optional.of(testPeopleImage));

        // Act
        PeopleImage result = peopleImageService.getPeopleImageByCodeName("test-code");

        // Assert
        assertEquals(testPeopleImage, result);
        verify(peopleImageDataAccessor, times(1)).findById("test-code");
    }

    @Test
    void getPeopleImageByCodeName_NotFound() {
        // Arrange
        when(peopleImageDataAccessor.findById(anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            peopleImageService.getPeopleImageByCodeName("non-existent"));
        verify(peopleImageDataAccessor, times(1)).findById("non-existent");
    }

    @Test
    void savePeopleImage_Success() {
        // Arrange
        when(peopleImageDataAccessor.save(any(PeopleImage.class))).thenReturn(testPeopleImage);

        // Act
        PeopleImage result = peopleImageService.savePeopleImage(testPeopleImage);

        // Assert
        assertEquals(testPeopleImage, result);
        verify(peopleImageDataAccessor, times(1)).save(testPeopleImage);
    }

    @Test
    void deletePeopleImage_Success() {
        // Arrange
        when(peopleImageDataAccessor.findById(anyString()))
            .thenReturn(Optional.of(testPeopleImage));
        doNothing().when(peopleImageDataAccessor).deleteById(anyString());

        // Act
        peopleImageService.deletePeopleImage("test-code");

        // Assert
        verify(peopleImageDataAccessor, times(1)).findById("test-code");
        verify(peopleImageDataAccessor, times(1)).deleteById("test-code");
    }

    @Test
    void deletePeopleImage_NotFound() {
        // Arrange
        when(peopleImageDataAccessor.findById(anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            peopleImageService.deletePeopleImage("non-existent"));
        verify(peopleImageDataAccessor, times(1)).findById("non-existent");
        verify(peopleImageDataAccessor, never()).deleteById(anyString());
    }

    @Test
    void peopleImageExists_True() {
        // Arrange
        when(peopleImageDataAccessor.findById(anyString()))
            .thenReturn(Optional.of(testPeopleImage));

        // Act
        boolean result = peopleImageService.peopleImageExists("test-code");

        // Assert
        assertTrue(result);
        verify(peopleImageDataAccessor, times(1)).findById("test-code");
    }

    @Test
    void peopleImageExists_False() {
        // Arrange
        when(peopleImageDataAccessor.findById(anyString()))
            .thenReturn(Optional.empty());

        // Act
        boolean result = peopleImageService.peopleImageExists("non-existent");

        // Assert
        assertFalse(result);
        verify(peopleImageDataAccessor, times(1)).findById("non-existent");
    }

    @Test
    void testFindByCodeNameContaining() {
        // Given
        String searchTerm = "test";
        PeopleImage image1 = new PeopleImage();
        image1.setCodeName("test1");
        PeopleImage image2 = new PeopleImage();
        image2.setCodeName("TEST2");
        List<PeopleImage> expectedImages = Arrays.asList(image1, image2);
        
        Specification<PeopleImage> spec = (root, query, cb) -> 
            cb.like(cb.lower(root.get("codeName")), "%" + searchTerm.toLowerCase() + "%");
        
        when(peopleImageDataAccessor.findAll(any(Specification.class)))
            .thenReturn(expectedImages);
        
        // When
        List<PeopleImage> result = peopleImageService.findByCodeNameContaining(searchTerm);
        
        // Then
        assertEquals(expectedImages, result);
        verify(peopleImageDataAccessor).findAll(any(Specification.class));
    }

    @Test
    void findAll_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<PeopleImage> expectedPage = new PageImpl<>(testPeopleImageList, pageable, testPeopleImageList.size());
        when(peopleImageDataAccessor.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // Act
        Page<PeopleImage> result = peopleImageService.findAll(pageable);

        // Assert
        assertEquals(expectedPage, result);
        verify(peopleImageDataAccessor, times(1)).findAll(pageable);
    }
} 