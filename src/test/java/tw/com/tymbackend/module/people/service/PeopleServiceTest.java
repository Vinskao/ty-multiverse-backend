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

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private DataAccessor<People, Long> peopleDataAccessor;

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private PeopleService peopleService;

    private People testPerson;
    private List<People> testPeopleList;

    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        testPerson = new People();
        testPerson.setName("Test Person");
        testPerson.setAge(25);
        testPerson.setRace("Human");

        testPeopleList = Arrays.asList(testPerson);
    }

    @Test
    void getAllPeople_Success() {
        // Arrange
        when(peopleDataAccessor.findAll()).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.getAllPeople();

        // Assert
        assertEquals(testPeopleList, result);
        verify(peopleDataAccessor, times(1)).findAll();
    }

    @Test
    void getPersonBy_Success() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.of(testPerson));

        // Act
        Optional<People> result = peopleService.getPersonBy("Test Person");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPerson, result.get());
        verify(peopleRepository, times(1)).findByName("Test Person");
    }

    @Test
    void getPeopleByName_Success() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.of(testPerson));

        // Act
        Optional<People> result = peopleService.getPeopleByName("Test Person");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPerson, result.get());
        verify(peopleRepository, times(1)).findByName("Test Person");
    }

    @Test
    void savePerson_Success() {
        // Arrange
        when(peopleDataAccessor.save(any(People.class))).thenReturn(testPerson);

        // Act
        People result = peopleService.savePerson(testPerson);

        // Assert
        assertEquals(testPerson, result);
        verify(peopleDataAccessor, times(1)).save(testPerson);
    }

    @Test
    void saveAllPeople_Success() {
        // Arrange
        when(peopleDataAccessor.saveAll(any())).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.saveAllPeople(testPeopleList);

        // Assert
        assertEquals(testPeopleList, result);
        verify(peopleDataAccessor, times(1)).saveAll(testPeopleList);
    }

    @Test
    void deletePerson_Success() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.of(testPerson));
        doNothing().when(peopleRepository).deleteByName(anyString());

        // Act
        peopleService.deletePerson("Test Person");

        // Assert
        verify(peopleRepository, times(1)).findByName("Test Person");
        verify(peopleRepository, times(1)).deleteByName("Test Person");
    }

    @Test
    void deletePerson_NotFound() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act
        peopleService.deletePerson("Non Existent");

        // Assert
        verify(peopleRepository, times(1)).findByName("Non Existent");
        verify(peopleRepository, never()).deleteByName(anyString());
    }

    @Test
    void deleteAllPeople_Success() {
        // Arrange
        doNothing().when(peopleDataAccessor).deleteAll();

        // Act
        peopleService.deleteAllPeople();

        // Assert
        verify(peopleDataAccessor, times(1)).deleteAll();
    }

    @Test
    void insertPerson_Success() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(peopleDataAccessor.save(any(People.class))).thenReturn(testPerson);

        // Act
        People result = peopleService.insertPerson(testPerson);

        // Assert
        assertEquals(testPerson, result);
        verify(peopleRepository, times(1)).findByName(testPerson.getName());
        verify(peopleDataAccessor, times(1)).save(testPerson);
    }

    @Test
    void insertPerson_AlreadyExists() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.of(testPerson));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            peopleService.insertPerson(testPerson);
        });
        verify(peopleRepository, times(1)).findByName(testPerson.getName());
        verify(peopleDataAccessor, never()).save(any(People.class));
    }

    @Test
    void updatePerson_Success() {
        // Arrange
        People updatedPerson = new People();
        updatedPerson.setName("Test Person");
        updatedPerson.setAge(26);
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.of(testPerson));
        when(peopleDataAccessor.save(any(People.class))).thenReturn(updatedPerson);

        // Act
        People result = peopleService.updatePerson(updatedPerson);

        // Assert
        assertEquals(updatedPerson, result);
        verify(peopleRepository, times(1)).findByName(updatedPerson.getName());
        verify(peopleDataAccessor, times(1)).save(any(People.class));
    }

    @Test
    void updatePerson_NotFound() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            peopleService.updatePerson(testPerson);
        });
        verify(peopleRepository, times(1)).findByName(testPerson.getName());
        verify(peopleDataAccessor, never()).save(any(People.class));
    }

    @Test
    void findByRace_Success() {
        // Arrange
        Specification<People> spec = (root, query, cb) -> 
            cb.equal(root.get("race"), "Human");
        when(peopleDataAccessor.findAll(any(Specification.class))).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.findByRace("Human");

        // Assert
        assertEquals(testPeopleList, result);
        verify(peopleDataAccessor, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findByAgeRange_Success() {
        // Arrange
        Specification<People> spec = (root, query, cb) -> 
            cb.between(root.get("age"), 20, 30);
        when(peopleDataAccessor.findAll(any(Specification.class))).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.findByAgeRange(20, 30);

        // Assert
        assertEquals(testPeopleList, result);
        verify(peopleDataAccessor, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findByNameContaining_Success() {
        // Arrange
        Specification<People> spec = (root, query, cb) -> 
            cb.like(cb.lower(root.get("name")), "%test%");
        when(peopleDataAccessor.findAll(any(Specification.class))).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.findByNameContaining("test");

        // Assert
        assertEquals(testPeopleList, result);
        verify(peopleDataAccessor, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findByMultipleCriteria_Success() {
        // Arrange
        Specification<People> spec = (root, query, cb) -> 
            cb.and(
                cb.equal(root.get("race"), "Human"),
                cb.between(root.get("age"), 20, 30)
            );
        when(peopleDataAccessor.findAll(any(Specification.class))).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.findByMultipleCriteria("Human", 20, 30);

        // Assert
        assertEquals(testPeopleList, result);
        verify(peopleDataAccessor, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findAll_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<People> expectedPage = new PageImpl<>(testPeopleList, pageable, testPeopleList.size());
        when(peopleDataAccessor.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // Act
        Page<People> result = peopleService.findAll(pageable);

        // Assert
        assertEquals(expectedPage, result);
        verify(peopleDataAccessor, times(1)).findAll(pageable);
    }
} 