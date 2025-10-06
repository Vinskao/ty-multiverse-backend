package tw.com.tymbackend.module.people.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tw.com.tymbackend.module.people.domain.dto.BatchDamageRequestDTO;
import tw.com.tymbackend.module.people.domain.dto.BatchDamageResponseDTO;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.strategy.DamageStrategy;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * 批量傷害計算服務測試
 */
@ExtendWith(MockitoExtension.class)
class WeaponDamageServiceBatchTest {

    @Mock
    private WeaponService weaponService;

    @Mock
    private PeopleService peopleService;

    @Mock
    private DamageStrategy damageStrategy;

    private WeaponDamageService weaponDamageService;

    @BeforeEach
    void setUp() {
        weaponDamageService = new WeaponDamageService(weaponService, peopleService, damageStrategy);
    }

    @Test
    void testCalculateBatchDamageWithWeapon_Success() {
        // Arrange
        List<String> names = Arrays.asList("角色1", "角色2", "角色3");
        BatchDamageRequestDTO request = new BatchDamageRequestDTO(names);

        People person1 = new People();
        person1.setName("角色1");
        People person2 = new People();
        person2.setName("角色2");
        People person3 = new People();
        person3.setName("角色3");

        List<People> people = Arrays.asList(person1, person2, person3);

        Weapon weapon1 = new Weapon();
        weapon1.setOwner("角色1");
        Weapon weapon2 = new Weapon();
        weapon2.setOwner("角色2");
        List<Weapon> weapons = Arrays.asList(weapon1, weapon2);

        when(peopleService.findByNames(names)).thenReturn(people);
        when(weaponService.getWeaponsByOwners(names)).thenReturn(weapons);
        when(damageStrategy.calculateDamage(person1, Arrays.asList(weapon1))).thenReturn(150);
        when(damageStrategy.calculateDamage(person2, Arrays.asList(weapon2))).thenReturn(200);
        when(damageStrategy.calculateDamage(person3, Arrays.asList())).thenReturn(100);

        // Act
        BatchDamageResponseDTO result = weaponDamageService.calculateBatchDamageWithWeapon(request);

        // Assert
        assertNotNull(result);
        Map<String, Integer> damageResults = result.getDamageResults();
        assertEquals(3, damageResults.size());
        assertEquals(150, damageResults.get("角色1"));
        assertEquals(200, damageResults.get("角色2"));
        assertEquals(100, damageResults.get("角色3"));
        assertTrue(result.getNotFoundNames().isEmpty());
    }

    @Test
    void testCalculateBatchDamageWithWeapon_SomeNotFound() {
        // Arrange
        List<String> names = Arrays.asList("角色1", "不存在角色", "角色3");
        BatchDamageRequestDTO request = new BatchDamageRequestDTO(names);

        People person1 = new People();
        person1.setName("角色1");
        People person3 = new People();
        person3.setName("角色3");

        List<People> people = Arrays.asList(person1, person3);

        Weapon weapon1 = new Weapon();
        weapon1.setOwner("角色1");
        List<Weapon> weapons = Arrays.asList(weapon1);

        when(peopleService.findByNames(names)).thenReturn(people);
        when(weaponService.getWeaponsByOwners(names)).thenReturn(weapons);
        when(damageStrategy.calculateDamage(person1, Arrays.asList(weapon1))).thenReturn(150);
        when(damageStrategy.calculateDamage(person3, Arrays.asList())).thenReturn(100);

        // Act
        BatchDamageResponseDTO result = weaponDamageService.calculateBatchDamageWithWeapon(request);

        // Assert
        assertNotNull(result);
        Map<String, Integer> damageResults = result.getDamageResults();
        assertEquals(2, damageResults.size());
        assertEquals(150, damageResults.get("角色1"));
        assertEquals(100, damageResults.get("角色3"));
        
        List<String> notFoundNames = result.getNotFoundNames();
        assertEquals(1, notFoundNames.size());
        assertTrue(notFoundNames.contains("不存在角色"));
    }

    @Test
    void testCalculateBatchDamageWithWeapon_EmptyRequest() {
        // Arrange
        BatchDamageRequestDTO request = new BatchDamageRequestDTO(Arrays.asList());

        // Act
        BatchDamageResponseDTO result = weaponDamageService.calculateBatchDamageWithWeapon(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getDamageResults().isEmpty());
        assertTrue(result.getNotFoundNames().isEmpty());
    }

    @Test
    void testCalculateBatchDamageWithWeapon_NullRequest() {
        // Act
        BatchDamageResponseDTO result = weaponDamageService.calculateBatchDamageWithWeapon(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getDamageResults().isEmpty());
        assertTrue(result.getNotFoundNames().isEmpty());
    }
}
