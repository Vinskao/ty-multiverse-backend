package tw.com.tymbackend.module.people.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import tw.com.tymbackend.config.TestConfig;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用 @DataJpaTest 註解進行數據訪問層測試
 * 原因：
 * 1. 自動配置：自動配置測試環境，包括內存數據庫、JPA 相關組件等
 * 2. 隔離測試：只加載與 JPA 相關的配置，不加載其他不必要的組件
 * 3. 事務管理：自動管理測試事務，每個測試方法執行完後自動回滾
 * 4. 提高測試速度：使用內存數據庫，避免網絡延遲和外部依賴
 */
@DataJpaTest
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("test")
class PeopleRepositoryTest {

    /**
     * 使用 @Autowired 註解注入真實的 Repository
     * 原因：
     * 1. 真實測試：數據訪問層測試需要使用真實的 Repository 實現
     * 2. 完整功能：測試 Repository 的所有功能，包括基本的 CRUD 操作
     * 3. 驗證 SQL：可以驗證生成的 SQL 語句是否正確
     * 4. 數據持久化：測試數據的持久化和查詢功能
     */
    @Autowired
    private PeopleRepository peopleRepository;

    @Test
    void findByName_Success() {
        // Arrange
        People person = new People();
        person.setName("Test Person");
        person.setAge(25);
        peopleRepository.save(person);

        // Act
        Optional<People> found = peopleRepository.findByName("Test Person");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test Person", found.get().getName());
        assertEquals(25, found.get().getAge());
    }

    @Test
    void findByName_NotFound() {
        // Arrange
        // No data in database

        // Act
        Optional<People> found = peopleRepository.findByName("Non Existent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void deleteByName_Success() {
        // Arrange
        People person = new People();
        person.setName("Test Person");
        person.setAge(25);
        peopleRepository.save(person);

        // Act
        peopleRepository.deleteByName("Test Person");

        // Assert
        Optional<People> found = peopleRepository.findByName("Test Person");
        assertFalse(found.isPresent());
    }

    @Test
    void deleteAllPeople_Success() {
        // Arrange
        People person1 = new People();
        person1.setName("Test Person 1");
        person1.setAge(25);
        peopleRepository.save(person1);

        People person2 = new People();
        person2.setName("Test Person 2");
        person2.setAge(30);
        peopleRepository.save(person2);

        // Act
        peopleRepository.deleteAllPeople();

        // Assert
        assertEquals(0, peopleRepository.count());
    }
} 