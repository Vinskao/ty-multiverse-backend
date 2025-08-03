package tw.com.tymbackend.module.people.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.strategy.DamageStrategy;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeaponDamageServiceTest {

    @Mock
    private WeaponService weaponService;

    @Mock
    private PeopleService peopleService;

    @Mock
    private DamageStrategy damageStrategy;

    @InjectMocks
    private WeaponDamageService weaponDamageService;

    private People testPeople;
    private List<Weapon> testWeapons;

    @BeforeEach
    void setUp() {
        testPeople = new People();
        testPeople.setName("Test Character");
        testPeople.setAttributes("Strength");
        testPeople.setStateAttributes("Normal");
        testPeople.setPhysicPower(100);
        testPeople.setMagicPower(50);
        testPeople.setUtilityPower(75);

        Weapon testWeapon = new Weapon();
        testWeapon.setName("Test Sword");
        testWeapon.setOwner("Test Character");
        testWeapon.setBaseDamage(50);
        testWeapon.setBonusDamage(25);
        testWeapon.setBonusAttributes(Arrays.asList("Strength"));
        testWeapon.setStateAttributes(Arrays.asList("Normal"));

        testWeapons = Arrays.asList(testWeapon);
    }

    @Test
    void calculateDamageWithWeapon_Success() {
        // Arrange
        when(peopleService.getPeopleByName(anyString())).thenReturn(java.util.Optional.of(testPeople));
        when(weaponService.getWeaponsByOwner(anyString())).thenReturn(testWeapons);
        when(damageStrategy.calculateDamage(any(People.class), anyList())).thenReturn(300);

        // Act
        int result = weaponDamageService.calculateDamageWithWeapon("Test Character");

        // Assert
        assertEquals(300, result);
        verify(peopleService, times(1)).getPeopleByName("Test Character");
        verify(weaponService, times(1)).getWeaponsByOwner("Test Character");
        verify(damageStrategy, times(1)).calculateDamage(testPeople, testWeapons);
    }

    @Test
    void calculateDamageWithWeapon_PersonNotFound() {
        // Arrange
        when(peopleService.getPeopleByName(anyString())).thenReturn(java.util.Optional.empty());

        // Act
        int result = weaponDamageService.calculateDamageWithWeapon("NonExistent");

        // Assert
        assertEquals(-1, result);
        verify(peopleService, times(1)).getPeopleByName("NonExistent");
        verify(weaponService, never()).getWeaponsByOwner(anyString());
        verify(damageStrategy, never()).calculateDamage(any(People.class), anyList());
    }

    @Test
    void calculateDamageWithWeapon_NoWeapons() {
        // Arrange
        when(peopleService.getPeopleByName(anyString())).thenReturn(java.util.Optional.of(testPeople));
        when(weaponService.getWeaponsByOwner(anyString())).thenReturn(null);
        when(damageStrategy.calculateDamage(any(People.class), eq(null))).thenReturn(225);

        // Act
        int result = weaponDamageService.calculateDamageWithWeapon("Test Character");

        // Assert
        assertEquals(225, result);
        verify(peopleService, times(1)).getPeopleByName("Test Character");
        verify(weaponService, times(1)).getWeaponsByOwner("Test Character");
        verify(damageStrategy, times(1)).calculateDamage(testPeople, null);
    }

    @Test
    void calculateDamageWithWeapon_EmptyWeaponsList() {
        // Arrange
        when(peopleService.getPeopleByName(anyString())).thenReturn(java.util.Optional.of(testPeople));
        when(weaponService.getWeaponsByOwner(anyString())).thenReturn(Arrays.asList());
        when(damageStrategy.calculateDamage(any(People.class), anyList())).thenReturn(225);

        // Act
        int result = weaponDamageService.calculateDamageWithWeapon("Test Character");

        // Assert
        assertEquals(225, result);
        verify(peopleService, times(1)).getPeopleByName("Test Character");
        verify(weaponService, times(1)).getWeaponsByOwner("Test Character");
        verify(damageStrategy, times(1)).calculateDamage(testPeople, Arrays.asList());
    }
} 