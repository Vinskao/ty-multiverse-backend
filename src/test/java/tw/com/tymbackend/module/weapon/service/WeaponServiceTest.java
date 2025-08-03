package tw.com.tymbackend.module.weapon.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeaponServiceTest {

    @Mock
    private WeaponRepository weaponRepository;

    @InjectMocks
    private WeaponService weaponService;

    private Weapon testWeapon;
    private List<Weapon> testWeaponList;

    @BeforeEach
    void setUp() {
        testWeapon = new Weapon();
        testWeapon.setName("Test Sword");
        testWeapon.setOwner("Test Character");
        testWeapon.setAttributes("Slashing");
        testWeapon.setBaseDamage(50);
        testWeapon.setBonusDamage(25);
        testWeapon.setBonusAttributes(Arrays.asList("Strength"));
        testWeapon.setStateAttributes(Arrays.asList("Normal"));

        testWeaponList = Arrays.asList(testWeapon);
    }

    @Test
    void getAllWeapons_Success() {
        // Arrange
        when(weaponRepository.findAll()).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.getAllWeapons();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWeapon, result.get(0));
        verify(weaponRepository, times(1)).findAll();
    }

    @Test
    void getWeaponById_Success() {
        // Arrange
        when(weaponRepository.findById(anyString())).thenReturn(Optional.of(testWeapon));

        // Act
        Optional<Weapon> result = weaponService.getWeaponById("Test Sword");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testWeapon, result.get());
        verify(weaponRepository, times(1)).findById("Test Sword");
    }

    @Test
    void getWeaponById_NotFound() {
        // Arrange
        when(weaponRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<Weapon> result = weaponService.getWeaponById("NonExistent");

        // Assert
        assertFalse(result.isPresent());
        verify(weaponRepository, times(1)).findById("NonExistent");
    }

    @Test
    void findByAttribute_Success() {
        // Arrange
        when(weaponRepository.findByAttributes(anyString())).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.findByAttribute("Slashing");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWeapon, result.get(0));
        verify(weaponRepository, times(1)).findByAttributes("Slashing");
    }

    @Test
    void findByBaseDamageRange_Success() {
        // Arrange
        when(weaponRepository.findByBaseDamageBetween(anyInt(), anyInt())).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.findByBaseDamageRange(40, 60);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWeapon, result.get(0));
        verify(weaponRepository, times(1)).findByBaseDamageBetween(40, 60);
    }

    @Test
    void getWeaponsByOwner_Success() {
        // Arrange
        when(weaponRepository.findByOwner(anyString())).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.getWeaponsByOwner("Test Character");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWeapon, result.get(0));
        verify(weaponRepository, times(1)).findByOwner("Test Character");
    }

    @Test
    void saveWeapon_Success() {
        // Arrange
        when(weaponRepository.save(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        Weapon result = weaponService.saveWeapon(testWeapon);

        // Assert
        assertNotNull(result);
        assertEquals(testWeapon, result);
        verify(weaponRepository, times(1)).save(testWeapon);
    }

    @Test
    void saveWeaponSmart_Success() {
        // Arrange
        when(weaponRepository.findById(anyString())).thenReturn(Optional.empty());
        when(weaponRepository.save(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        Weapon result = weaponService.saveWeaponSmart(testWeapon);

        // Assert
        assertNotNull(result);
        assertEquals(testWeapon, result);
        verify(weaponRepository, times(1)).save(testWeapon);
    }

    @Test
    void deleteWeapon_Success() {
        // Arrange
        doNothing().when(weaponRepository).deleteById(anyString());

        // Act
        weaponService.deleteWeapon("Test Sword");

        // Assert
        verify(weaponRepository, times(1)).deleteById("Test Sword");
    }
} 