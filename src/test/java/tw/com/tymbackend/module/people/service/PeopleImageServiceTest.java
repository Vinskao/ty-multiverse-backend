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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import tw.com.tymbackend.core.factory.QueryConditionFactory;
import tw.com.tymbackend.core.factory.RepositoryFactory;
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
    private RepositoryFactory repositoryFactory;

    @Mock
    private QueryConditionFactory queryConditionFactory;

    @Mock
    private JpaSpecificationExecutor<PeopleImage> specificationExecutor;

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
        when(repositoryFactory.findAll(PeopleImage.class)).thenReturn(testPeopleImageList);

        // Act
        List<PeopleImage> result = peopleImageService.getAllPeopleImages();

        // Assert
        assertEquals(testPeopleImageList, result);
        verify(repositoryFactory, times(1)).findAll(PeopleImage.class);
    }

    @Test
    void getPeopleImageByCodeName_Success() {
        // Arrange
        when(repositoryFactory.findById(eq(PeopleImage.class), anyString()))
            .thenReturn(Optional.of(testPeopleImage));

        // Act
        PeopleImage result = peopleImageService.getPeopleImageByCodeName("test-code");

        // Assert
        assertEquals(testPeopleImage, result);
        verify(repositoryFactory, times(1)).findById(PeopleImage.class, "test-code");
    }

    @Test
    void getPeopleImageByCodeName_NotFound() {
        // Arrange
        when(repositoryFactory.findById(eq(PeopleImage.class), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            peopleImageService.getPeopleImageByCodeName("non-existent"));
        verify(repositoryFactory, times(1)).findById(PeopleImage.class, "non-existent");
    }

    @Test
    void savePeopleImage_Success() {
        // Arrange
        when(repositoryFactory.save(any(PeopleImage.class))).thenReturn(testPeopleImage);

        // Act
        PeopleImage result = peopleImageService.savePeopleImage(testPeopleImage);

        // Assert
        assertEquals(testPeopleImage, result);
        verify(repositoryFactory, times(1)).save(testPeopleImage);
    }

    @Test
    void deletePeopleImage_Success() {
        // Arrange
        when(repositoryFactory.findById(eq(PeopleImage.class), anyString()))
            .thenReturn(Optional.of(testPeopleImage));
        doNothing().when(repositoryFactory).deleteById(eq(PeopleImage.class), anyString());

        // Act
        peopleImageService.deletePeopleImage("test-code");

        // Assert
        verify(repositoryFactory, times(1)).findById(PeopleImage.class, "test-code");
        verify(repositoryFactory, times(1)).deleteById(PeopleImage.class, "test-code");
    }

    @Test
    void deletePeopleImage_NotFound() {
        // Arrange
        when(repositoryFactory.findById(eq(PeopleImage.class), anyString()))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            peopleImageService.deletePeopleImage("non-existent"));
        verify(repositoryFactory, times(1)).findById(PeopleImage.class, "non-existent");
        verify(repositoryFactory, never()).deleteById(any(), anyString());
    }

    @Test
    void peopleImageExists_True() {
        // Arrange
        when(repositoryFactory.findById(eq(PeopleImage.class), anyString()))
            .thenReturn(Optional.of(testPeopleImage));

        // Act
        boolean result = peopleImageService.peopleImageExists("test-code");

        // Assert
        assertTrue(result);
        verify(repositoryFactory, times(1)).findById(PeopleImage.class, "test-code");
    }

    @Test
    void peopleImageExists_False() {
        // Arrange
        when(repositoryFactory.findById(eq(PeopleImage.class), anyString()))
            .thenReturn(Optional.empty());

        // Act
        boolean result = peopleImageService.peopleImageExists("non-existent");

        // Assert
        assertFalse(result);
        verify(repositoryFactory, times(1)).findById(PeopleImage.class, "non-existent");
    }

    @Test
    void findByCodeNameContaining_Success() {
        // Arrange
        @SuppressWarnings("unchecked")
        Specification<PeopleImage> mockSpec = mock(Specification.class);
        when(queryConditionFactory.<PeopleImage>createLikeCondition(anyString(), anyString())).thenReturn(mockSpec);
        when(repositoryFactory.getSpecificationRepository(PeopleImage.class, String.class))
            .thenReturn(specificationExecutor);
        when(specificationExecutor.findAll(any(Specification.class))).thenReturn(testPeopleImageList);

        // Act
        List<PeopleImage> result = peopleImageService.findByCodeNameContaining("test");

        // Assert
        assertEquals(testPeopleImageList, result);
        verify(queryConditionFactory, times(1)).createLikeCondition("codeName", "test");
        verify(repositoryFactory, times(1)).getSpecificationRepository(PeopleImage.class, String.class);
        verify(specificationExecutor, times(1)).findAll(mockSpec);
    }

    @Test
    void findAll_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<PeopleImage> expectedPage = new PageImpl<>(testPeopleImageList, pageable, testPeopleImageList.size());
        when(repositoryFactory.findAll(eq(PeopleImage.class), any(Pageable.class))).thenReturn(expectedPage);

        // Act
        Page<PeopleImage> result = peopleImageService.findAll(pageable);

        // Assert
        assertEquals(expectedPage, result);
        verify(repositoryFactory, times(1)).findAll(PeopleImage.class, pageable);
    }

    @Test
    void findByMultipleCriteria_Success() {
        // Arrange
        @SuppressWarnings("unchecked")
        Specification<PeopleImage> mockCodeNameSpec = mock(Specification.class);
        @SuppressWarnings("unchecked")
        Specification<PeopleImage> mockImageSpec = mock(Specification.class);
        @SuppressWarnings("unchecked")
        Specification<PeopleImage> mockCombinedSpec = mock(Specification.class);

        when(queryConditionFactory.<PeopleImage>createLikeCondition(anyString(), anyString())).thenReturn(mockCodeNameSpec);
        when(queryConditionFactory.<PeopleImage>createIsNotNullCondition(anyString())).thenReturn(mockImageSpec);
        when(queryConditionFactory.<PeopleImage>createCompositeCondition(any(), any())).thenReturn(mockCombinedSpec);
        when(repositoryFactory.getSpecificationRepository(PeopleImage.class, String.class))
            .thenReturn(specificationExecutor);
        when(specificationExecutor.findAll(any(Specification.class))).thenReturn(testPeopleImageList);

        // Act
        List<PeopleImage> result = peopleImageService.findByMultipleCriteria("test", true);

        // Assert
        assertEquals(testPeopleImageList, result);
        verify(queryConditionFactory, times(1)).createLikeCondition("codeName", "test");
        verify(queryConditionFactory, times(1)).createIsNotNullCondition("image");
        verify(queryConditionFactory, times(1)).createCompositeCondition(mockCodeNameSpec, mockImageSpec);
        verify(repositoryFactory, times(1)).getSpecificationRepository(PeopleImage.class, String.class);
        verify(specificationExecutor, times(1)).findAll(mockCombinedSpec);
    }
} 