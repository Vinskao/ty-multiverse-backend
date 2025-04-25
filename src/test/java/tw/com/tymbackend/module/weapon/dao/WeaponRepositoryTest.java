package tw.com.tymbackend.module.weapon.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import tw.com.tymbackend.config.TestConfig;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
class WeaponRepositoryTest {

    @Autowired
    private WeaponRepository weaponRepository;

    @Test
    void findByName_Success() {
        // Arrange
        Weapon weapon = new Weapon();
        weapon.setName("TestOwner");
        weapon.setWeaponName("TestWeapon");
        weapon.setAttributes("TestAttributes");
        weapon.setBaseDamage(10);
        weapon.setBonusDamage(5);
        weapon.setBonusAttributes(Arrays.asList("Bonus1", "Bonus2"));
        weapon.setStateAttributes(Arrays.asList("State1", "State2"));
        weaponRepository.save(weapon);

        // Act
        List<Weapon> found = weaponRepository.findByName("TestOwner");

        // Assert
        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals("TestOwner", found.get(0).getName());
        assertEquals("TestWeapon", found.get(0).getWeaponName());
    }

    @Test
    void findByName_NotFound() {
        // Arrange
        // No data in database

        // Act
        List<Weapon> found = weaponRepository.findByName("NonExistentOwner");

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByName_True() {
        // Arrange
        Weapon weapon = new Weapon();
        weapon.setName("TestOwner");
        weapon.setWeaponName("TestWeapon");
        weapon.setAttributes("TestAttributes");
        weapon.setBaseDamage(10);
        weapon.setBonusDamage(5);
        weapon.setBonusAttributes(Arrays.asList("Bonus1", "Bonus2"));
        weapon.setStateAttributes(Arrays.asList("State1", "State2"));
        weaponRepository.save(weapon);

        // Act
        boolean exists = weaponRepository.existsByName("TestOwner");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByName_False() {
        // Arrange
        // No data in database

        // Act
        boolean exists = weaponRepository.existsByName("NonExistentOwner");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByAttributes_Success() {
        // Arrange
        Weapon weapon = new Weapon();
        weapon.setName("TestOwner");
        weapon.setWeaponName("TestWeapon");
        weapon.setAttributes("TestAttributes");
        weapon.setBaseDamage(10);
        weapon.setBonusDamage(5);
        weapon.setBonusAttributes(Arrays.asList("Bonus1", "Bonus2"));
        weapon.setStateAttributes(Arrays.asList("State1", "State2"));
        weaponRepository.save(weapon);

        // Act
        List<Weapon> found = weaponRepository.findByAttributes("TestAttributes");

        // Assert
        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals("TestAttributes", found.get(0).getAttributes());
    }

    @Test
    void findByBaseDamageBetween_Success() {
        // Arrange
        Weapon weapon = new Weapon();
        weapon.setName("TestOwner");
        weapon.setWeaponName("TestWeapon");
        weapon.setAttributes("TestAttributes");
        weapon.setBaseDamage(10);
        weapon.setBonusDamage(5);
        weapon.setBonusAttributes(Arrays.asList("Bonus1", "Bonus2"));
        weapon.setStateAttributes(Arrays.asList("State1", "State2"));
        weaponRepository.save(weapon);

        // Act
        List<Weapon> found = weaponRepository.findByBaseDamageBetween(5, 15);

        // Assert
        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals(10, found.get(0).getBaseDamage());
    }

    @Test
    void findByTotalDamageBetween_Success() {
        // Arrange
        Weapon weapon = new Weapon();
        weapon.setName("TestOwner");
        weapon.setWeaponName("TestWeapon");
        weapon.setAttributes("TestAttributes");
        weapon.setBaseDamage(10);
        weapon.setBonusDamage(5);
        weapon.setBonusAttributes(Arrays.asList("Bonus1", "Bonus2"));
        weapon.setStateAttributes(Arrays.asList("State1", "State2"));
        weaponRepository.save(weapon);

        // Act
        List<Weapon> found = weaponRepository.findByTotalDamageBetween(10, 20);

        // Assert
        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals(10, found.get(0).getBaseDamage());
        assertEquals(5, found.get(0).getBonusDamage());
    }

    @Test
    void findByWeaponName_Success() {
        // Arrange
        Weapon weapon = new Weapon();
        weapon.setName("TestOwner");
        weapon.setWeaponName("TestWeapon");
        weapon.setAttributes("TestAttributes");
        weapon.setBaseDamage(10);
        weapon.setBonusDamage(5);
        weapon.setBonusAttributes(Arrays.asList("Bonus1", "Bonus2"));
        weapon.setStateAttributes(Arrays.asList("State1", "State2"));
        weaponRepository.save(weapon);

        // Act
        List<Weapon> found = weaponRepository.findByWeaponName("TestWeapon");

        // Assert
        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals("TestWeapon", found.get(0).getWeaponName());
    }

    @Test
    void findByWeaponNameContainingIgnoreCase_Success() {
        // Arrange
        Weapon weapon = new Weapon();
        weapon.setName("TestOwner");
        weapon.setWeaponName("TestWeapon");
        weapon.setAttributes("TestAttributes");
        weapon.setBaseDamage(10);
        weapon.setBonusDamage(5);
        weapon.setBonusAttributes(Arrays.asList("Bonus1", "Bonus2"));
        weapon.setStateAttributes(Arrays.asList("State1", "State2"));
        weaponRepository.save(weapon);

        // Act
        List<Weapon> found = weaponRepository.findByWeaponNameContainingIgnoreCase("weapon");

        // Assert
        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertEquals("TestWeapon", found.get(0).getWeaponName());
    }
} 