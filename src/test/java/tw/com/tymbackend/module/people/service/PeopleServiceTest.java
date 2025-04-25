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
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import tw.com.tymbackend.core.factory.QueryConditionFactory;
import tw.com.tymbackend.core.factory.RepositoryFactory;
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
    private RepositoryFactory repositoryFactory;

    @Mock
    private QueryConditionFactory queryConditionFactory;

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private PeopleService peopleService;

    private People testPerson;
    private List<People> testPeopleList;

    /**
     * 測試環境初始化設置
     * 職責：
     * 1. 數據準備：創建測試所需的基礎數據對象
     * 2. 狀態重置：確保每個測試方法都從相同的初始狀態開始
     * 3. 依賴配置：設置測試對象的基本屬性和狀態
     * 
     * 重要性：
     * 1. 測試隔離：確保測試方法之間互不影響
     * 2. 代碼重用：避免在每個測試方法中重複創建相同的測試數據
     * 3. 維護性：集中管理測試數據，便於統一修改
     * 
     * 執行時機：
     * 在每個測試方法執行前自動運行，由 @BeforeEach 註解保證
     */
    @BeforeEach
    void setUp() {
        // Arrange: 設置測試數據
        testPerson = new People();
        testPerson.setName("Test Person");
        testPerson.setAge(25);
        testPerson.setRace("Human");

        testPeopleList = Arrays.asList(testPerson);
    }

    /**
     * 測試目標：驗證 getAllPeople 方法能正確返回所有人員列表
     * 測試原則：
     * 1. 單一職責：只測試獲取所有人員的功能
     * 2. 依賴隔離：使用 mock 隔離 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和 RepositoryFactory 的調用
     */
    @Test
    void getAllPeople_Success() {
        // Arrange
        when(repositoryFactory.findAll(People.class)).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.getAllPeople();

        // Assert
        assertEquals(testPeopleList, result);
        verify(repositoryFactory, times(1)).findAll(People.class);
    }

    /**
     * 測試目標：驗證 getPersonBy 方法能根據名稱查找單個人員
     * 測試原則：
     * 1. 單一職責：只測試根據名稱查找的功能
     * 2. 依賴隔離：使用 mock 隔離 PeopleRepository 的依賴
     * 3. 行為驗證：驗證返回結果和 Repository 的調用
     */
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

    /**
     * 測試目標：驗證 getPeopleByName 方法能根據名稱查找人員
     * 測試原則：
     * 1. 單一職責：只測試根據名稱查找的功能
     * 2. 依賴隔離：使用 mock 隔離 PeopleRepository 的依賴
     * 3. 行為驗證：驗證返回結果和 Repository 的調用
     */
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

    /**
     * 測試目標：驗證 savePerson 方法能正確保存單個人員
     * 測試原則：
     * 1. 單一職責：只測試保存單個人員的功能
     * 2. 依賴隔離：使用 mock 隔離 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和 RepositoryFactory 的調用
     */
    @Test
    void savePerson_Success() {
        // Arrange
        when(repositoryFactory.save(any(People.class))).thenReturn(testPerson);

        // Act
        People result = peopleService.savePerson(testPerson);

        // Assert
        assertEquals(testPerson, result);
        verify(repositoryFactory, times(1)).save(testPerson);
    }

    /**
     * 測試目標：驗證 saveAllPeople 方法能正確批量保存人員
     * 測試原則：
     * 1. 單一職責：只測試批量保存的功能
     * 2. 依賴隔離：使用 mock 隔離 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和 RepositoryFactory 的調用
     */
    @Test
    void saveAllPeople_Success() {
        // Arrange
        when(repositoryFactory.<People>saveAll(any())).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.saveAllPeople(testPeopleList);

        // Assert
        assertEquals(testPeopleList, result);
        verify(repositoryFactory, times(1)).saveAll(testPeopleList);
    }

    /**
     * 測試目標：驗證 deletePerson 方法能正確刪除指定人員
     * 測試原則：
     * 1. 單一職責：只測試刪除指定人員的功能
     * 2. 依賴隔離：使用 mock 隔離 PeopleRepository 的依賴
     * 3. 行為驗證：驗證 Repository 的調用順序和參數
     */
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

    /**
     * 測試目標：驗證 deletePerson 方法在人員不存在時的處理
     * 測試原則：
     * 1. 單一職責：只測試人員不存在時的錯誤處理
     * 2. 依賴隔離：使用 mock 隔離 PeopleRepository 的依賴
     * 3. 行為驗證：驗證異常拋出和 Repository 的調用
     */
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

    /**
     * 測試目標：驗證 deleteAllPeople 方法能正確刪除所有人員
     * 測試原則：
     * 1. 單一職責：只測試刪除所有人員的功能
     * 2. 依賴隔離：使用 mock 隔離 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證 RepositoryFactory 的調用
     */
    @Test
    void deleteAllPeople_Success() {
        // Arrange
        doNothing().when(repositoryFactory).deleteAll(People.class);

        // Act
        peopleService.deleteAllPeople();

        // Assert
        verify(repositoryFactory, times(1)).deleteAll(People.class);
    }

    /**
     * 測試目標：驗證 insertPerson 方法能正確插入新人員
     * 測試原則：
     * 1. 單一職責：只測試插入新人員的功能
     * 2. 依賴隔離：使用 mock 隔離 Repository 和 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和 Repository 的調用順序
     */
    @Test
    void insertPerson_Success() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(repositoryFactory.save(any(People.class))).thenReturn(testPerson);

        // Act
        People result = peopleService.insertPerson(testPerson);

        // Assert
        assertEquals(testPerson, result);
        verify(peopleRepository, times(1)).findByName(testPerson.getName());
        verify(repositoryFactory, times(1)).save(testPerson);
    }

    /**
     * 測試目標：驗證 insertPerson 方法在人員已存在時的處理
     * 測試原則：
     * 1. 單一職責：只測試人員已存在時的錯誤處理
     * 2. 依賴隔離：使用 mock 隔離 PeopleRepository 的依賴
     * 3. 行為驗證：驗證異常拋出和 Repository 的調用
     */
    @Test
    void insertPerson_AlreadyExists() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.of(testPerson));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            peopleService.insertPerson(testPerson);
        });
        verify(peopleRepository, times(1)).findByName(testPerson.getName());
        verify(repositoryFactory, never()).save(any(People.class));
    }

    /**
     * 測試目標：驗證 updatePerson 方法能正確更新人員信息
     * 測試原則：
     * 1. 單一職責：只測試更新人員信息的功能
     * 2. 依賴隔離：使用 mock 隔離 Repository 和 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和 Repository 的調用順序
     */
    @Test
    void updatePerson_Success() {
        // Arrange
        People updatedPerson = new People();
        updatedPerson.setName("Test Person");
        updatedPerson.setAge(26);
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.of(testPerson));
        when(repositoryFactory.save(any(People.class))).thenReturn(updatedPerson);

        // Act
        People result = peopleService.updatePerson(updatedPerson);

        // Assert
        assertEquals(updatedPerson, result);
        verify(peopleRepository, times(1)).findByName(updatedPerson.getName());
        verify(repositoryFactory, times(1)).save(any(People.class));
    }

    /**
     * 測試目標：驗證 updatePerson 方法在人員不存在時的處理
     * 測試原則：
     * 1. 單一職責：只測試人員不存在時的錯誤處理
     * 2. 依賴隔離：使用 mock 隔離 PeopleRepository 的依賴
     * 3. 行為驗證：驗證異常拋出和 Repository 的調用
     */
    @Test
    void updatePerson_NotFound() {
        // Arrange
        when(peopleRepository.findByName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            peopleService.updatePerson(testPerson);
        });
        verify(peopleRepository, times(1)).findByName(testPerson.getName());
        verify(repositoryFactory, never()).save(any(People.class));
    }

    /**
     * 測試目標：驗證 findByRace 方法能根據種族查找人員
     * 測試原則：
     * 1. 單一職責：只測試根據種族查找的功能
     * 2. 依賴隔離：使用 mock 隔離 QueryConditionFactory 和 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和條件創建、查詢的調用
     */
    @Test
    void findByRace_Success() {
        // Arrange
        Specification<People> spec = mock(Specification.class);
        when(queryConditionFactory.<People>createEqualsCondition("race", "Human")).thenReturn(spec);
        JpaSpecificationExecutor<People> specRepo = mock(JpaSpecificationExecutor.class);
        when(repositoryFactory.getSpecificationRepository(People.class, Long.class)).thenReturn(specRepo);
        when(specRepo.findAll(spec)).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.findByRace("Human");

        // Assert
        assertEquals(testPeopleList, result);
        verify(queryConditionFactory, times(1)).createEqualsCondition("race", "Human");
    }

    /**
     * 測試目標：驗證 findByAgeRange 方法能根據年齡範圍查找人員
     * 測試原則：
     * 1. 單一職責：只測試根據年齡範圍查找的功能
     * 2. 依賴隔離：使用 mock 隔離 QueryConditionFactory 和 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和條件創建、查詢的調用
     */
    @Test
    void findByAgeRange_Success() {
        // Arrange
        Specification<People> spec = mock(Specification.class);
        when(queryConditionFactory.<People>createRangeCondition("age", 20, 30)).thenReturn(spec);
        JpaSpecificationExecutor<People> specRepo = mock(JpaSpecificationExecutor.class);
        when(repositoryFactory.getSpecificationRepository(People.class, Long.class)).thenReturn(specRepo);
        when(specRepo.findAll(spec)).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.findByAgeRange(20, 30);

        // Assert
        assertEquals(testPeopleList, result);
        verify(queryConditionFactory, times(1)).createRangeCondition("age", 20, 30);
    }

    /**
     * 測試目標：驗證 findByNameContaining 方法能根據名稱模糊查找人員
     * 測試原則：
     * 1. 單一職責：只測試根據名稱模糊查找的功能
     * 2. 依賴隔離：使用 mock 隔離 QueryConditionFactory 和 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和條件創建、查詢的調用
     */
    @Test
    void findByNameContaining_Success() {
        // Arrange
        Specification<People> spec = mock(Specification.class);
        when(queryConditionFactory.<People>createLikeCondition("name", "Test")).thenReturn(spec);
        JpaSpecificationExecutor<People> specRepo = mock(JpaSpecificationExecutor.class);
        when(repositoryFactory.getSpecificationRepository(People.class, Long.class)).thenReturn(specRepo);
        when(specRepo.findAll(spec)).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.findByNameContaining("Test");

        // Assert
        assertEquals(testPeopleList, result);
        verify(queryConditionFactory, times(1)).createLikeCondition("name", "Test");
    }

    /**
     * 測試目標：驗證 findByMultipleCriteria 方法能根據多個條件組合查找人員
     * 測試原則：
     * 1. 單一職責：只測試多條件組合查找的功能
     * 2. 依賴隔離：使用 mock 隔離 QueryConditionFactory 和 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和條件創建、組合、查詢的調用
     */
    @Test
    void findByMultipleCriteria_Success() {
        // Arrange
        Specification<People> raceSpec = mock(Specification.class);
        Specification<People> ageSpec = mock(Specification.class);
        Specification<People> combinedSpec = mock(Specification.class);
        when(queryConditionFactory.<People>createEqualsCondition("race", "Human")).thenReturn(raceSpec);
        when(queryConditionFactory.<People>createRangeCondition("age", 20, 30)).thenReturn(ageSpec);
        when(queryConditionFactory.<People>createCompositeCondition(raceSpec, ageSpec)).thenReturn(combinedSpec);
        JpaSpecificationExecutor<People> specRepo = mock(JpaSpecificationExecutor.class);
        when(repositoryFactory.getSpecificationRepository(People.class, Long.class)).thenReturn(specRepo);
        when(specRepo.findAll(combinedSpec)).thenReturn(testPeopleList);

        // Act
        List<People> result = peopleService.findByMultipleCriteria("Human", 20, 30);

        // Assert
        assertEquals(testPeopleList, result);
        verify(queryConditionFactory, times(1)).createEqualsCondition("race", "Human");
        verify(queryConditionFactory, times(1)).createRangeCondition("age", 20, 30);
        verify(queryConditionFactory, times(1)).createCompositeCondition(raceSpec, ageSpec);
    }

    /**
     * 測試目標：驗證 findAll 方法能正確處理分頁查詢
     * 測試原則：
     * 1. 單一職責：只測試分頁查詢的功能
     * 2. 依賴隔離：使用 mock 隔離 RepositoryFactory 的依賴
     * 3. 行為驗證：驗證返回結果和分頁參數的處理
     */
    @Test
    void findAll_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<People> expectedPage = new PageImpl<>(testPeopleList, pageable, testPeopleList.size());
        when(repositoryFactory.findAll(People.class, pageable)).thenReturn(expectedPage);

        // Act
        Page<People> result = peopleService.findAll(pageable);

        // Assert
        assertEquals(expectedPage, result);
        verify(repositoryFactory, times(1)).findAll(People.class, pageable);
    }
} 