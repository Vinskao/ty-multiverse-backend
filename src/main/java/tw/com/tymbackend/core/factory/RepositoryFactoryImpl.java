package tw.com.tymbackend.core.factory;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of RepositoryFactory
 */
@Component
public class RepositoryFactoryImpl implements RepositoryFactory {

    // 注入ApplicationContext
    private final ApplicationContext applicationContext;
    private final Map<Class<?>, Object> repositoryCache = new ConcurrentHashMap<>();

    public RepositoryFactoryImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // 獲取自定義的Repository
    @Override
    public <R, T, ID> R getCustomRepository(Class<R> repositoryClass, Class<T> entityType, Class<ID> idType) {
        // 如果Repository不存在於緩存中，則從Spring容器中獲取
        Object repository = repositoryCache.computeIfAbsent(repositoryClass, 
            k -> applicationContext.getBean(repositoryClass));
            
        // 如果Repository不實現自定義的Repository，則拋出異常
        if (!repositoryClass.isInstance(repository)) {
            throw new RuntimeException("Repository is not of type: " + repositoryClass.getName());
        }
        // 將Repository轉換為自定義的Repository
        @SuppressWarnings("unchecked")
        R typedRepository = (R) repository;
        return typedRepository;
    }

    // 獲取標準的Repository
    @Override
    public <T, ID> JpaRepository<T, ID> getRepository(Class<T> entityType, Class<ID> idType) {
        // 獲取Repository的類名
        String repositoryClassName = entityType.getSimpleName() + "Repository";
        Class<?> repositoryClass;
        try {
            // 獲取Repository的類
            repositoryClass = Class.forName(entityType.getPackage().getName().replace("domain.vo", "dao") + "." + repositoryClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Repository class not found: " + repositoryClassName, e);
        }
        // 如果Repository不存在於緩存中，則從Spring容器中獲取
        Object repository = repositoryCache.computeIfAbsent(
            entityType, 
            k -> applicationContext.getBean(repositoryClass)
        );
        
        // 如果Repository不實現JpaRepository，則拋出異常
        if (!(repository instanceof JpaRepository)) {
            throw new RuntimeException("Repository does not implement JpaRepository: " + repositoryClass.getName());
        }
        
        // 將Repository轉換為JpaRepository<T, ID>
        @SuppressWarnings("unchecked")
        JpaRepository<T, ID> typedRepository = (JpaRepository<T, ID>) repository;
        return typedRepository;
    }

    // 獲取規範的Repository
    @Override
    public <T, ID> JpaSpecificationExecutor<T> getSpecificationRepository(Class<T> entityType, Class<ID> idType) {
        // 獲取Repository的類名
        String repositoryClassName = entityType.getSimpleName() + "Repository";
        Class<?> repositoryClass;
        try {
            // 獲取Repository的類
            repositoryClass = Class.forName(entityType.getPackage().getName().replace("domain.vo", "dao") + "." + repositoryClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Repository class not found: " + repositoryClassName, e);
        }
        // 如果Repository不存在於緩存中，則從Spring容器中獲取
        Object repository = repositoryCache.computeIfAbsent(
            entityType, 
            k -> applicationContext.getBean(repositoryClass)
        );
        
        // 如果Repository不實現JpaSpecificationExecutor，則拋出異常
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new RuntimeException("Repository does not implement JpaSpecificationExecutor: " + repositoryClass.getName());
        }
        // 將Repository轉換為JpaSpecificationExecutor<T>
        @SuppressWarnings("unchecked")
        JpaSpecificationExecutor<T> typedRepository = (JpaSpecificationExecutor<T>) repository;
        return typedRepository;
    }

    // 獲取所有實體
    @Override
    public <T> List<T> findAll(Class<T> entityType) {
        // 獲取Repository
        JpaRepository<T, ?> repository = getRepository(entityType, String.class);
        // 返回所有實體
        return repository.findAll();
    }

    // 獲取指定ID的實體
    @Override
    public <T, ID> Optional<T> findById(Class<T> entityType, ID id) {
        // 獲取Repository
        @SuppressWarnings("unchecked")
        Class<ID> idType = (Class<ID>) id.getClass();
        JpaRepository<T, ID> repository = getRepository(entityType, idType);
        // 返回指定ID的實體
        return repository.findById(id);
    }

    // 刪除指定ID的實體
    @Override
    public <T, ID> void deleteById(Class<T> entityType, ID id) {
        // 獲取Repository
        @SuppressWarnings("unchecked")
        JpaRepository<T, ID> repository = getRepository(entityType, (Class<ID>) id.getClass());
        // 刪除指定ID的實體
        repository.deleteById(id);
    }

    // 刪除所有實體
    @Override
    public <T> void deleteAll(Class<T> entityType) {
        // 獲取Repository
        JpaRepository<T, ?> repository = getRepository(entityType, String.class);
        // 刪除所有實體
        repository.deleteAll();
    }

    // 保存實體
    @Override
    public <T> T save(T entity) {
        if (entity == null) {
            return null;
        }
        
        try {
            // 使用 JpaRepository 的 save 方法保存實體
            // 不使用 @Version 註解，避免樂觀鎖定衝突
            @SuppressWarnings("unchecked")
            JpaRepository<T, ?> repository = (JpaRepository<T, ?>) repositoryCache.get(entity.getClass());
            return repository.save(entity);
        } catch (Exception e) {
            // 記錄異常但不重新拋出，防止事務回滾
            System.err.println("Error saving entity: " + e.getMessage());
            e.printStackTrace();
            // 返回原始實體
            return entity;
        }
    }

    // 保存所有實體
    @Override
    public <T> List<T> saveAll(List<T> entities) {
        if (entities.isEmpty()) {
            return entities;
        }
        @SuppressWarnings("unchecked")
        JpaRepository<T, ?> repository = getRepository((Class<T>) entities.get(0).getClass(), String.class);
        return repository.saveAll(entities);
    }

    // 更新指定ID的實體
    @Override
    public <T, ID> T updateById(Class<T> entityType, ID id, T entity) {
        // 獲取Repository
        @SuppressWarnings("unchecked")
        JpaRepository<T, ID> repository = getRepository(entityType, (Class<ID>) id.getClass());
        if (repository.existsById(id)) {
            return repository.save(entity);
        }
        throw new IllegalArgumentException("Entity with ID " + id + " does not exist");
    }

    // 獲取所有實體並分頁   
    @Override
    public <T> Page<T> findAll(Class<T> entityType, Pageable pageable) {
        // 獲取Repository
        JpaRepository<T, ?> repository = getRepository(entityType, String.class);
        // 返回所有實體並分頁
        return repository.findAll(pageable);
    }
} 