package tw.com.tymbackend.module.people.service;

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
import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private PeopleService peopleService;

    private People testPeople;
    private List<People> testPeopleList;

    @BeforeEach
    void setUp() {
        testPeople = new People();
        testPeople.setName("Test Character");
        testPeople.setNameOriginal("Original Name");
        testPeople.setCodeName("TEST001");
        testPeople.setRace("Human");
        testPeople.setAttributes("Strength");
        testPeople.setBaseAttributes("Strength,Agility");
        testPeople.setBonusAttributes("Intelligence");
        testPeople.setStateAttributes("Normal");
        testPeople.setPhysicPower(100);
        testPeople.setMagicPower(50);
        testPeople.setUtilityPower(75);
        testPeople.setVersion(0L);

        testPeopleList = Arrays.asList(testPeople);
    }

    @Test
    void getAllPeople_Success() {
        // Arrange
        when(peopleRepository.findAll()).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.getAllPeople();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPeople, result.get(0));
        verify(peopleRepository, times(1)).findAll();
    }

    @Test
    void getPeopleByName_Success() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.of(testPeople));

        // Act
        Optional<People> result = peopleService.getPeopleByName("Test Character");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPeople, result.get());
        verify(peopleRepository, times(1)).findByName("Test Character");
    }

    @Test
    void getPeopleByName_NotFound() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<People> result = peopleService.getPeopleByName("NonExistent");

        // Assert
        assertFalse(result.isPresent());
        verify(peopleRepository, times(1)).findByName("NonExistent");
    }

    @Test
    void savePeople_Success() {
        // Arrange
        when(peopleRepository.save(any(People.class))).thenReturn(testPeople);

        // Act
        People result = peopleService.save(testPeople);

        // Assert
        assertNotNull(result);
        assertEquals(testPeople, result);
        verify(peopleRepository, times(1)).save(testPeople);
    }

    @Test
    void updatePerson_Success() {
        // Arrange
        when(peopleRepository.findById("Test Character")).thenReturn(Optional.of(testPeople));
        when(peopleRepository.save(any(People.class))).thenReturn(testPeople);

        // Act
        People result = peopleService.updatePerson(testPeople);

        // Assert
        assertNotNull(result);
        assertEquals(testPeople, result);
        verify(peopleRepository, times(1)).findById("Test Character");
        verify(peopleRepository, times(1)).save(testPeople);
    }

    @Test
    void deleteAllPeople_Success() {
        // Arrange
        doNothing().when(peopleRepository).deleteAll();

        // Act
        peopleService.deleteAllPeople();

        // Assert
        verify(peopleRepository, times(1)).deleteAll();
    }

    @Test
    void findBySpecification_Success() {
        // Arrange
        Specification<People> spec = mock(Specification.class);
        Pageable pageable = PageRequest.of(0, 10);
        Page<People> page = new PageImpl<>(testPeopleList, pageable, 1);
        when(peopleRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act
        Page<People> result = peopleService.findBySpecification(spec, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testPeople, result.getContent().get(0));
        verify(peopleRepository, times(1)).findAll(spec, pageable);
    }
} 