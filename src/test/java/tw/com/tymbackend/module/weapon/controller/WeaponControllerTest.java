package tw.com.tymbackend.module.weapon.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeaponControllerTest {

    @Mock
    private WeaponService weaponService;

    @InjectMocks
    private WeaponController weaponController;

    private Weapon testWeapon;
    private List<Weapon> testWeaponList;

    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        testWeapon = new Weapon();
        testWeapon.setName("TestOwner");
        testWeapon.setWeaponName("TestWeapon");
        testWeapon.setAttributes("TestAttributes");
        testWeapon.setBaseDamage(10);
        testWeapon.setBonusDamage(5);
        testWeapon.setBonusAttributes(Arrays.asList("Bonus1", "Bonus2"));
        testWeapon.setStateAttributes(Arrays.asList("State1", "State2"));

        testWeaponList = Arrays.asList(testWeapon);
    }

    @Test
    void getAllWeapons_Success() {
        // Arrange
        when(weaponService.getAllWeapons()).thenReturn(testWeaponList);

        // Act
        ResponseEntity<List<Weapon>> response = weaponController.getAllWeapons();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeaponList, response.getBody());
        verify(weaponService, times(1)).getAllWeapons();
    }

    @Test
    void getWeaponByName_Success() {
        // Arrange
        when(weaponService.getWeaponsByOwnerName(anyString())).thenReturn(testWeaponList);

        // Act
        ResponseEntity<List<Weapon>> response = weaponController.getWeaponByName("TestOwner");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeaponList, response.getBody());
        verify(weaponService, times(1)).getWeaponsByOwnerName("TestOwner");
    }

    @Test
    void getWeaponsByOwnerName_Success() {
        // Arrange
        when(weaponService.getWeaponsByOwnerName(anyString())).thenReturn(testWeaponList);

        // Act
        ResponseEntity<List<Weapon>> response = weaponController.getWeaponsByOwnerName("TestOwner");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeaponList, response.getBody());
        verify(weaponService, times(1)).getWeaponsByOwnerName("TestOwner");
    }

    @Test
    void saveWeapon_Success() {
        // Arrange
        when(weaponService.saveWeapon(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        ResponseEntity<Weapon> response = weaponController.saveWeapon(testWeapon);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeapon, response.getBody());
        verify(weaponService, times(1)).saveWeapon(testWeapon);
    }

    @Test
    void deleteWeapon_Success() {
        // Arrange
        doNothing().when(weaponService).deleteWeapon(anyString());

        // Act
        ResponseEntity<Void> response = weaponController.deleteWeapon("TestWeapon");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(weaponService, times(1)).deleteWeapon("TestWeapon");
    }

    @Test
    void checkWeaponExists_Success() {
        // Arrange
        when(weaponService.weaponExists(anyString())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Boolean>> response = weaponController.checkWeaponExists("TestWeapon");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("exists"));
        verify(weaponService, times(1)).weaponExists("TestWeapon");
    }

    @Test
    void updateWeaponAttributes_Success() {
        // Arrange
        when(weaponService.updateWeaponAttributes(anyString(), anyString())).thenReturn(testWeapon);

        // Act
        ResponseEntity<Weapon> response = weaponController.updateWeaponAttributes("TestWeapon", "NewAttributes");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeapon, response.getBody());
        verify(weaponService, times(1)).updateWeaponAttributes("TestWeapon", "NewAttributes");
    }

    @Test
    void updateWeaponBaseDamage_Success() {
        // Arrange
        when(weaponService.updateWeaponBaseDamage(anyString(), anyInt())).thenReturn(testWeapon);

        // Act
        ResponseEntity<Weapon> response = weaponController.updateWeaponBaseDamage("TestWeapon", 15);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeapon, response.getBody());
        verify(weaponService, times(1)).updateWeaponBaseDamage("TestWeapon", 15);
    }

    @Test
    void updateWeaponBonusDamage_Success() {
        // Arrange
        when(weaponService.updateWeaponBonusDamage(anyString(), anyInt())).thenReturn(testWeapon);

        // Act
        ResponseEntity<Weapon> response = weaponController.updateWeaponBonusDamage("TestWeapon", 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeapon, response.getBody());
        verify(weaponService, times(1)).updateWeaponBonusDamage("TestWeapon", 10);
    }

    @Test
    void updateWeaponBonusAttributes_Success() {
        // Arrange
        List<String> newBonusAttributes = Arrays.asList("NewBonus1", "NewBonus2");
        when(weaponService.updateWeaponBonusAttributes(anyString(), any())).thenReturn(testWeapon);

        // Act
        ResponseEntity<Weapon> response = weaponController.updateWeaponBonusAttributes("TestWeapon", newBonusAttributes);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeapon, response.getBody());
        verify(weaponService, times(1)).updateWeaponBonusAttributes("TestWeapon", newBonusAttributes);
    }

    @Test
    void updateWeaponStateAttributes_Success() {
        // Arrange
        List<String> newStateAttributes = Arrays.asList("NewState1", "NewState2");
        when(weaponService.updateWeaponStateAttributes(anyString(), any())).thenReturn(testWeapon);

        // Act
        ResponseEntity<Weapon> response = weaponController.updateWeaponStateAttributes("TestWeapon", newStateAttributes);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testWeapon, response.getBody());
        verify(weaponService, times(1)).updateWeaponStateAttributes("TestWeapon", newStateAttributes);
    }
} 