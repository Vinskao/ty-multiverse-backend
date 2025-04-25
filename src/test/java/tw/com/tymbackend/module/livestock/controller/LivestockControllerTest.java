package tw.com.tymbackend.module.livestock.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tw.com.tymbackend.module.livestock.domain.vo.Livestock;
import tw.com.tymbackend.module.livestock.service.LivestockService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivestockControllerTest {

    @Mock
    private LivestockService livestockService;

    @Mock
    private LivestockWSController livestockWebSocketController;

    @InjectMocks
    private LivestockController livestockController;

    private Livestock testLivestock;
    private List<Livestock> testLivestockList;

    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        testLivestock = new Livestock();
        testLivestock.setId(1L);
        testLivestock.setLivestock("Cow");
        testLivestock.setHeight(1.5);
        testLivestock.setWeight(500.0);
        testLivestock.setMelee(5);
        testLivestock.setMagicka(0);
        testLivestock.setRanged(0);
        testLivestock.setSellingPrice(new BigDecimal("1000.00"));
        testLivestock.setBuyingPrice(new BigDecimal("800.00"));
        testLivestock.setDealPrice(new BigDecimal("900.00"));
        testLivestock.setBuyer("John");
        testLivestock.setOwner("Farm A");
        testLivestock.setVersion(0L);

        testLivestockList = Arrays.asList(testLivestock);
    }

    @Test
    void getAllLivestock_Success() {
        // Arrange
        when(livestockService.getAllLivestock()).thenReturn(testLivestockList);

        // Act
        ResponseEntity<List<Livestock>> response = livestockController.getAllLivestock();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testLivestockList, response.getBody());
        verify(livestockService, times(1)).getAllLivestock();
    }

    @Test
    void getLivestockById_Success() {
        // Arrange
        when(livestockService.getLivestockById(anyLong())).thenReturn(Optional.of(testLivestock));

        // Act
        ResponseEntity<Livestock> response = livestockController.getLivestockById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testLivestock, response.getBody());
        verify(livestockService, times(1)).getLivestockById(1L);
    }

    @Test
    void getLivestockById_NotFound() {
        // Arrange
        when(livestockService.getLivestockById(anyLong())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Livestock> response = livestockController.getLivestockById(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(livestockService, times(1)).getLivestockById(1L);
    }

    @Test
    void getLivestockByName_Success() {
        // Arrange
        when(livestockService.getLivestockByName(anyString())).thenReturn(Optional.of(testLivestock));

        // Act
        ResponseEntity<Livestock> response = livestockController.getLivestockByName("Cow");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testLivestock, response.getBody());
        verify(livestockService, times(1)).getLivestockByName("Cow");
    }

    @Test
    void getLivestockByName_NotFound() {
        // Arrange
        when(livestockService.getLivestockByName(anyString())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Livestock> response = livestockController.getLivestockByName("NonExistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(livestockService, times(1)).getLivestockByName("NonExistent");
    }

    @Test
    void createLivestock_Success() {
        // Arrange
        when(livestockService.saveLivestock(any(Livestock.class))).thenReturn(testLivestock);
        doNothing().when(livestockWebSocketController).broadcastLivestockUpdate(any(Livestock.class));

        // Act
        ResponseEntity<Livestock> response = livestockController.createLivestock(testLivestock);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testLivestock, response.getBody());
        verify(livestockService, times(1)).saveLivestock(testLivestock);
        verify(livestockWebSocketController, times(1)).broadcastLivestockUpdate(testLivestock);
    }

    @Test
    void updateLivestock_Success() {
        // Arrange
        when(livestockService.updateLivestock(any(Livestock.class))).thenReturn(testLivestock);
        doNothing().when(livestockWebSocketController).broadcastLivestockUpdate(any(Livestock.class));

        // Act
        ResponseEntity<Livestock> response = livestockController.updateLivestock(1L, testLivestock);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testLivestock, response.getBody());
        assertEquals(1L, testLivestock.getId());
        verify(livestockService, times(1)).updateLivestock(testLivestock);
        verify(livestockWebSocketController, times(1)).broadcastLivestockUpdate(testLivestock);
    }

    @Test
    void deleteLivestock_Success() {
        // Arrange
        doNothing().when(livestockService).deleteLivestock(anyLong());

        // Act
        ResponseEntity<Void> response = livestockController.deleteLivestock(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(livestockService, times(1)).deleteLivestock(1L);
    }
} 