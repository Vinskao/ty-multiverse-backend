package tw.com.tymbackend.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import tw.com.tymbackend.core.util.DistributedLockUtil;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskServiceTest {

    @Mock
    private DistributedLockUtil distributedLockUtil;

    @InjectMocks
    private ScheduledTaskService scheduledTaskService;

    @BeforeEach
    void setUp() {
        // 初始化測試環境
    }

    @Test
    void cleanupOldData_Success() {
        // Arrange
        when(distributedLockUtil.executeWithLock(anyString(), any(Duration.class), any(java.util.function.Supplier.class))).thenReturn(null);

        // Act
        scheduledTaskService.cleanupOldData();

        // Assert
        verify(distributedLockUtil, times(1)).executeWithLock(anyString(), any(Duration.class), any(java.util.function.Supplier.class));
    }

    @Test
    void generateWeeklyReport_Success() {
        // Arrange
        when(distributedLockUtil.executeWithLock(anyString(), any(Duration.class), any(java.util.function.Supplier.class))).thenReturn(null);

        // Act
        scheduledTaskService.generateWeeklyReport();

        // Assert
        verify(distributedLockUtil, times(1)).executeWithLock(anyString(), any(Duration.class), any(java.util.function.Supplier.class));
    }

    @Test
    void backupData_Success() {
        // Arrange
        when(distributedLockUtil.executeWithLock(anyString(), any(Duration.class), any(java.util.function.Supplier.class))).thenReturn(null);

        // Act
        scheduledTaskService.backupData();

        // Assert
        verify(distributedLockUtil, times(1)).executeWithLock(anyString(), any(Duration.class), any(java.util.function.Supplier.class));
    }

    @Test
    void healthCheck_Success() {
        // Arrange
        when(distributedLockUtil.executeWithLock(anyString(), any(Duration.class), any(java.util.function.Supplier.class))).thenReturn(null);

        // Act
        scheduledTaskService.healthCheck();

        // Assert
        verify(distributedLockUtil, times(1)).executeWithLock(anyString(), any(Duration.class), any(java.util.function.Supplier.class));
    }
} 