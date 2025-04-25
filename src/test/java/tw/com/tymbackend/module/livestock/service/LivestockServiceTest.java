package tw.com.tymbackend.module.livestock.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.com.tymbackend.module.livestock.dao.LivestockRepository;
import tw.com.tymbackend.module.livestock.domain.vo.Livestock;

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
class LivestockServiceTest {

    @Mock
    private LivestockRepository livestockRepository;

    @InjectMocks
    private LivestockService livestockService;

    private Livestock testLivestock;
    private List<Livestock> testLivestockList;

    @BeforeEach
    void setUp() {
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
        when(livestockRepository.findAll()).thenReturn(testLivestockList);

        // Act
        List<Livestock> result = livestockService.getAllLivestock();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLivestock, result.get(0));
        verify(livestockRepository, times(1)).findAll();
    }

    @Test
    void getLivestockById_Success() {
        // Arrange
        when(livestockRepository.findById(anyLong())).thenReturn(Optional.of(testLivestock));

        // Act
        Optional<Livestock> result = livestockService.getLivestockById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLivestock, result.get());
        verify(livestockRepository, times(1)).findById(1L);
    }

    @Test
    void getLivestockById_NotFound() {
        // Arrange
        when(livestockRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<Livestock> result = livestockService.getLivestockById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(livestockRepository, times(1)).findById(1L);
    }

    @Test
    void getLivestockByName_Success() {
        // Arrange
        when(livestockRepository.findByLivestock(anyString())).thenReturn(Optional.of(testLivestock));

        // Act
        Optional<Livestock> result = livestockService.getLivestockByName("Cow");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLivestock, result.get());
        verify(livestockRepository, times(1)).findByLivestock("Cow");
    }

    @Test
    void getLivestockByName_NotFound() {
        // Arrange
        when(livestockRepository.findByLivestock(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<Livestock> result = livestockService.getLivestockByName("NonExistent");

        // Assert
        assertFalse(result.isPresent());
        verify(livestockRepository, times(1)).findByLivestock("NonExistent");
    }

    @Test
    void saveLivestock_Success() {
        // Arrange
        when(livestockRepository.save(any(Livestock.class))).thenReturn(testLivestock);

        // Act
        Livestock result = livestockService.saveLivestock(testLivestock);

        // Assert
        assertNotNull(result);
        assertEquals(testLivestock, result);
        verify(livestockRepository, times(1)).save(testLivestock);
    }

    @Test
    void updateLivestock_Success() {
        // Arrange
        when(livestockRepository.save(any(Livestock.class))).thenReturn(testLivestock);

        // Act
        Livestock result = livestockService.updateLivestock(testLivestock);

        // Assert
        assertNotNull(result);
        assertEquals(testLivestock, result);
        verify(livestockRepository, times(1)).save(testLivestock);
    }

    @Test
    void deleteLivestock_Success() {
        // Arrange
        doNothing().when(livestockRepository).deleteById(anyLong());

        // Act
        livestockService.deleteLivestock(1L);

        // Assert
        verify(livestockRepository, times(1)).deleteById(1L);
    }

    @Test
    void getLivestockByOwner_Success() {
        // Arrange
        when(livestockRepository.findByOwner(anyString())).thenReturn(testLivestockList);

        // Act
        List<Livestock> result = livestockService.getLivestockByOwner("Farm A");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLivestock, result.get(0));
        verify(livestockRepository, times(1)).findByOwner("Farm A");
    }

    @Test
    void getLivestockByBuyer_Success() {
        // Arrange
        when(livestockRepository.findByBuyer(anyString())).thenReturn(testLivestockList);

        // Act
        List<Livestock> result = livestockService.getLivestockByBuyer("John");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLivestock, result.get(0));
        verify(livestockRepository, times(1)).findByBuyer("John");
    }
} 