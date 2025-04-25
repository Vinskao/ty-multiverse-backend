package tw.com.tymbackend.module.weapon.service;

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
import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeaponServiceTest {

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private QueryConditionFactory queryConditionFactory;

    @Mock
    private WeaponRepository weaponRepository;

    @Mock
    private JpaSpecificationExecutor<Weapon> specificationExecutor;

    @InjectMocks
    private WeaponService weaponService;

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
        when(repositoryFactory.findAll(Weapon.class)).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.getAllWeapons();

        // Assert
        assertEquals(testWeaponList, result);
        verify(repositoryFactory, times(1)).findAll(Weapon.class);
    }

    @Test
    void getWeaponsByOwnerName_Success() {
        // Arrange
        when(weaponRepository.findByName(anyString())).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.getWeaponsByOwnerName("TestOwner");

        // Assert
        assertEquals(testWeaponList, result);
        verify(weaponRepository, times(1)).findByName("TestOwner");
    }

    @Test
    void saveWeapon_Success() {
        // Arrange
        when(repositoryFactory.save(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        Weapon result = weaponService.saveWeapon(testWeapon);

        // Assert
        assertEquals(testWeapon, result);
        verify(repositoryFactory, times(1)).save(testWeapon);
    }

    @Test
    void deleteWeapon_Success() {
        // Arrange
        when(weaponRepository.existsByName(anyString())).thenReturn(true);
        doNothing().when(repositoryFactory).deleteById(eq(Weapon.class), anyString());

        // Act
        weaponService.deleteWeapon("TestWeapon");

        // Assert
        verify(weaponRepository, times(1)).existsByName("TestWeapon");
        verify(repositoryFactory, times(1)).deleteById(Weapon.class, "TestWeapon");
    }

    @Test
    void deleteWeapon_NotFound() {
        // Arrange
        when(weaponRepository.existsByName(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            weaponService.deleteWeapon("NonExistentWeapon"));
        verify(weaponRepository, times(1)).existsByName("NonExistentWeapon");
        verify(repositoryFactory, never()).deleteById(any(), anyString());
    }

    @Test
    void weaponExists_True() {
        // Arrange
        when(weaponRepository.existsByName(anyString())).thenReturn(true);

        // Act
        boolean result = weaponService.weaponExists("TestWeapon");

        // Assert
        assertTrue(result);
        verify(weaponRepository, times(1)).existsByName("TestWeapon");
    }

    @Test
    void weaponExists_False() {
        // Arrange
        when(weaponRepository.existsByName(anyString())).thenReturn(false);

        // Act
        boolean result = weaponService.weaponExists("NonExistentWeapon");

        // Assert
        assertFalse(result);
        verify(weaponRepository, times(1)).existsByName("NonExistentWeapon");
    }

    @Test
    void updateWeaponAttributes_Success() {
        // Arrange
        when(weaponRepository.existsByName(anyString())).thenReturn(true);
        when(repositoryFactory.findById(eq(Weapon.class), anyString())).thenReturn(Optional.of(testWeapon));
        when(repositoryFactory.save(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        Weapon result = weaponService.updateWeaponAttributes("TestWeapon", "NewAttributes");

        // Assert
        assertEquals("NewAttributes", result.getAttributes());
        verify(weaponRepository, times(1)).existsByName("TestWeapon");
        verify(repositoryFactory, times(1)).findById(Weapon.class, "TestWeapon");
        verify(repositoryFactory, times(1)).save(any(Weapon.class));
    }

    @Test
    void updateWeaponAttributes_NotFound() {
        // Arrange
        when(weaponRepository.existsByName(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> 
            weaponService.updateWeaponAttributes("NonExistentWeapon", "NewAttributes"));
        verify(weaponRepository, times(1)).existsByName("NonExistentWeapon");
        verify(repositoryFactory, never()).findById(any(), anyString());
        verify(repositoryFactory, never()).save(any());
    }

    @Test
    void updateWeaponBaseDamage_Success() {
        // Arrange
        when(weaponRepository.existsByName(anyString())).thenReturn(true);
        when(repositoryFactory.findById(eq(Weapon.class), anyString())).thenReturn(Optional.of(testWeapon));
        when(repositoryFactory.save(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        Weapon result = weaponService.updateWeaponBaseDamage("TestWeapon", 15);

        // Assert
        assertEquals(15, result.getBaseDamage());
        verify(weaponRepository, times(1)).existsByName("TestWeapon");
        verify(repositoryFactory, times(1)).findById(Weapon.class, "TestWeapon");
        verify(repositoryFactory, times(1)).save(any(Weapon.class));
    }

    @Test
    void updateWeaponBonusDamage_Success() {
        // Arrange
        when(weaponRepository.existsByName(anyString())).thenReturn(true);
        when(repositoryFactory.findById(eq(Weapon.class), anyString())).thenReturn(Optional.of(testWeapon));
        when(repositoryFactory.save(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        Weapon result = weaponService.updateWeaponBonusDamage("TestWeapon", 10);

        // Assert
        assertEquals(10, result.getBonusDamage());
        verify(weaponRepository, times(1)).existsByName("TestWeapon");
        verify(repositoryFactory, times(1)).findById(Weapon.class, "TestWeapon");
        verify(repositoryFactory, times(1)).save(any(Weapon.class));
    }

    @Test
    void updateWeaponBonusAttributes_Success() {
        // Arrange
        List<String> newBonusAttributes = Arrays.asList("NewBonus1", "NewBonus2");
        when(weaponRepository.existsByName(anyString())).thenReturn(true);
        when(repositoryFactory.findById(eq(Weapon.class), anyString())).thenReturn(Optional.of(testWeapon));
        when(repositoryFactory.save(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        Weapon result = weaponService.updateWeaponBonusAttributes("TestWeapon", newBonusAttributes);

        // Assert
        assertEquals(newBonusAttributes, result.getBonusAttributes());
        verify(weaponRepository, times(1)).existsByName("TestWeapon");
        verify(repositoryFactory, times(1)).findById(Weapon.class, "TestWeapon");
        verify(repositoryFactory, times(1)).save(any(Weapon.class));
    }

    @Test
    void updateWeaponStateAttributes_Success() {
        // Arrange
        List<String> newStateAttributes = Arrays.asList("NewState1", "NewState2");
        when(weaponRepository.existsByName(anyString())).thenReturn(true);
        when(repositoryFactory.findById(eq(Weapon.class), anyString())).thenReturn(Optional.of(testWeapon));
        when(repositoryFactory.save(any(Weapon.class))).thenReturn(testWeapon);

        // Act
        Weapon result = weaponService.updateWeaponStateAttributes("TestWeapon", newStateAttributes);

        // Assert
        assertEquals(newStateAttributes, result.getStateAttributes());
        verify(weaponRepository, times(1)).existsByName("TestWeapon");
        verify(repositoryFactory, times(1)).findById(Weapon.class, "TestWeapon");
        verify(repositoryFactory, times(1)).save(any(Weapon.class));
    }

    @Test
    void findByBaseDamageRange_Success() {
        // Arrange
        when(weaponRepository.findByBaseDamageBetween(anyInt(), anyInt())).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.findByBaseDamageRange(5, 15);

        // Assert
        assertEquals(testWeaponList, result);
        verify(weaponRepository, times(1)).findByBaseDamageBetween(5, 15);
    }

    @Test
    void findByAttribute_Success() {
        // Arrange
        when(weaponRepository.findByAttributes(anyString())).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.findByAttribute("TestAttribute");

        // Assert
        assertEquals(testWeaponList, result);
        verify(weaponRepository, times(1)).findByAttributes("TestAttribute");
    }

    @Test
    void findByMultipleCriteria_Success() {
        // Arrange
        @SuppressWarnings("unchecked")
        Specification<Weapon> mockSpec = mock(Specification.class);
        when(queryConditionFactory.<Weapon>createRangeCondition(anyString(), any(), any())).thenReturn(mockSpec);
        when(queryConditionFactory.<Weapon>createLikeCondition(anyString(), anyString())).thenReturn(mockSpec);
        when(repositoryFactory.getSpecificationRepository(Weapon.class, String.class))
            .thenReturn(specificationExecutor);
        when(specificationExecutor.findAll(any(Specification.class))).thenReturn(testWeaponList);

        // Act
        List<Weapon> result = weaponService.findByMultipleCriteria(5, 15, "TestAttribute");

        // Assert
        assertEquals(testWeaponList, result);
        verify(queryConditionFactory, times(1)).createRangeCondition("baseDamage", 5, 15);
        verify(queryConditionFactory, times(1)).createLikeCondition("attributes", "TestAttribute");
        verify(repositoryFactory, times(1)).getSpecificationRepository(Weapon.class, String.class);
        verify(specificationExecutor, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findAll_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Weapon> expectedPage = new PageImpl<>(testWeaponList, pageable, testWeaponList.size());
        when(repositoryFactory.findAll(eq(Weapon.class), any(Pageable.class))).thenReturn(expectedPage);

        // Act
        Page<Weapon> result = weaponService.findAll(pageable);

        // Assert
        assertEquals(expectedPage, result);
        verify(repositoryFactory, times(1)).findAll(Weapon.class, pageable);
    }
} 