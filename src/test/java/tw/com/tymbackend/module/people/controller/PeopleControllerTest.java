package tw.com.tymbackend.module.people.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tw.com.tymbackend.module.people.domain.dto.PeopleNameRequestDTO;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.PeopleService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleControllerTest {

    /**
     * 使用 @Mock 註解模擬 PeopleService
     * 原因：
     * 1. 隔離依賴：控制器層測試應該只關注 HTTP 請求和響應的處理，而不需要真實的服務層實現
     * 2. 控制行為：可以精確控制服務層方法的返回值，便於測試不同場景
     * 3. 提高測試速度：不需要初始化真實的服務層對象和其依賴
     * 4. 避免外部依賴：不需要連接數據庫或其他外部服務
     */
    @Mock
    private PeopleService peopleService;

    /**
     * 使用 @InjectMocks 註解注入模擬的依賴
     * 原因：
     * 1. 自動注入：自動將 @Mock 註解的對象注入到控制器中
     * 2. 簡化測試設置：不需要手動創建控制器實例並設置依賴
     * 3. 保持真實依賴關係：保持與實際代碼相同的依賴注入方式
     * 4. 便於維護：當控制器的依賴發生變化時，測試代碼不需要大量修改
     */
    @InjectMocks
    private PeopleController peopleController;

    private People testPerson;
    private List<People> testPeopleList;

    @BeforeEach
    void setUp() {
        // Arrange: 設置測試數據
        testPerson = new People();
        testPerson.setName("Test Person");
        testPerson.setAge(25);

        testPeopleList = Arrays.asList(testPerson);
    }

    @Test
    void insertPeople_Success() {
        // Arrange
        when(peopleService.insertPerson(any(People.class))).thenReturn(testPerson);

        // Act
        ResponseEntity<?> response = peopleController.insertPeople(testPerson);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testPerson, response.getBody());
        verify(peopleService, times(1)).insertPerson(any(People.class));
    }

    @Test
    void updatePeople_Success() {
        // Arrange
        when(peopleService.updatePerson(any(People.class))).thenReturn(testPerson);

        // Act
        ResponseEntity<?> response = peopleController.updatePeople(testPerson);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPerson, response.getBody());
        verify(peopleService, times(1)).updatePerson(any(People.class));
    }

    @Test
    void insertMultiplePeople_Success() {
        // Arrange
        when(peopleService.saveAllPeople(anyList())).thenReturn(testPeopleList);

        // Act
        ResponseEntity<?> response = peopleController.insertMultiplePeople(testPeopleList);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testPeopleList, response.getBody());
        verify(peopleService, times(1)).saveAllPeople(anyList());
    }

    @Test
    void getPeopleById_Success() {
        // Arrange
        PeopleNameRequestDTO request = new PeopleNameRequestDTO();
        request.setName("Test Person");
        when(peopleService.getPeopleByName(anyString())).thenReturn(Optional.of(testPerson));

        // Act
        ResponseEntity<?> response = peopleController.getPeopleById(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPerson, response.getBody());
        verify(peopleService, times(1)).getPeopleByName(anyString());
    }

    @Test
    void getPeopleById_NotFound() {
        // Arrange
        PeopleNameRequestDTO request = new PeopleNameRequestDTO();
        request.setName("Non Existent");
        when(peopleService.getPeopleByName(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = peopleController.getPeopleById(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Person not found", response.getBody());
        verify(peopleService, times(1)).getPeopleByName(anyString());
    }

    @Test
    void getAllPeople_Success() {
        // Arrange
        when(peopleService.getAllPeople()).thenReturn(testPeopleList);

        // Act
        ResponseEntity<?> response = peopleController.getAllPeople();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPeopleList, response.getBody());
        verify(peopleService, times(1)).getAllPeople();
    }

    @Test
    void getPeopleByName_Success() {
        // Arrange
        PeopleNameRequestDTO request = new PeopleNameRequestDTO();
        request.setName("Test Person");
        when(peopleService.getPeopleByName(anyString())).thenReturn(Optional.of(testPerson));

        // Act
        ResponseEntity<?> response = peopleController.getPeopleByName(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPerson, response.getBody());
        verify(peopleService, times(1)).getPeopleByName(anyString());
    }

    @Test
    void getPeopleByName_NotFound() {
        // Arrange
        PeopleNameRequestDTO request = new PeopleNameRequestDTO();
        request.setName("Non Existent");
        when(peopleService.getPeopleByName(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = peopleController.getPeopleByName(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Person not found", response.getBody());
        verify(peopleService, times(1)).getPeopleByName(anyString());
    }

    @Test
    void deleteAllPeople_Success() {
        // Arrange
        doNothing().when(peopleService).deleteAllPeople();

        // Act
        ResponseEntity<?> response = peopleController.deleteAllPeople();

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(peopleService, times(1)).deleteAllPeople();
    }
} 