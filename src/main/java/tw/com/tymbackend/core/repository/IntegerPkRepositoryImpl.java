package tw.com.tymbackend.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.lang.NonNull;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * IntegerPkRepository 的實現類別，用於處理以 Integer 為主鍵的實體
 * 
 * <p>此類別繼承自 SimpleJpaRepository，提供完整的 CRUD 操作功能，
 * 並實現了 IntegerPkRepository 介面定義的所有方法。</p>
 * 
 * @param <T> 實體類型
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public class IntegerPkRepositoryImpl<T> extends SimpleJpaRepository<T, Integer> implements IntegerPkRepository<T> {
    
    /**
     * 建構函數，使用實體資訊和實體管理器初始化
     * 
     * @param entityInformation 實體資訊，包含實體類型和主鍵類型
     * @param entityManager JPA 實體管理器
     */
    public IntegerPkRepositoryImpl(JpaEntityInformation<T, Integer> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }
    
    /**
     * 建構函數，使用實體類型和實體管理器初始化
     * 
     * @param domainClass 實體類型
     * @param entityManager JPA 實體管理器
     */
    public IntegerPkRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
    }
    
    @Override
    @NonNull
    public Optional<T> findById(@SuppressWarnings("null") @NonNull Integer id) {
        return super.findById(id);
    }
    
    @Override
    public boolean existsById(@SuppressWarnings("null") @NonNull Integer id) {
        return super.existsById(id);
    }
    
    @Override
    public void deleteById(@SuppressWarnings("null") @NonNull Integer id) {
        super.deleteById(id);
    }
    
    @Override
    @NonNull
    public List<T> findAll() {
        return super.findAll();
    }
    
    @Override
    @NonNull
    public Page<T> findAll(@SuppressWarnings("null") @NonNull Pageable pageable) {
        return super.findAll(pageable);
    }
    
    @Override
    @NonNull
    public List<T> findAll(@SuppressWarnings("null") @NonNull Specification<T> spec) {
        return super.findAll(spec);
    }
    
    @Override
    @NonNull
    public Page<T> findAll(@SuppressWarnings("null") @NonNull Specification<T> spec, @SuppressWarnings("null") @NonNull Pageable pageable) {
        return super.findAll(spec, pageable);
    }
} 