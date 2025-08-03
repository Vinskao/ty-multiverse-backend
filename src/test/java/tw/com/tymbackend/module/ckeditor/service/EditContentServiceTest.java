package tw.com.tymbackend.module.ckeditor.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EditContentServiceTest {

    @Mock
    private EditContentRepository editContentRepository;

    @InjectMocks
    private EditContentService editContentService;

    private EditContentVO testEditContent;

    @BeforeEach
    void setUp() {
        testEditContent = new EditContentVO();
        testEditContent.setEditor("test_editor");
        testEditContent.setContent("test content");
    }

    @Test
    void saveContent_Success() {
        // Arrange
        when(editContentRepository.save(any(EditContentVO.class))).thenReturn(testEditContent);

        // Act
        EditContentVO result = editContentRepository.save(testEditContent);

        // Assert
        assertNotNull(result);
        assertEquals(testEditContent, result);
        verify(editContentRepository, times(1)).save(testEditContent);
    }

    @Test
    void findById_Success() {
        // Arrange
        when(editContentRepository.findById(anyString())).thenReturn(Optional.of(testEditContent));

        // Act
        Optional<EditContentVO> result = editContentRepository.findById("test_editor");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testEditContent, result.get());
        verify(editContentRepository, times(1)).findById("test_editor");
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(editContentRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<EditContentVO> result = editContentRepository.findById("non_existent");

        // Assert
        assertFalse(result.isPresent());
        verify(editContentRepository, times(1)).findById("non_existent");
    }
} 