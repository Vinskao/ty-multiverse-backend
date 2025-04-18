package tw.com.tymbackend.core.factory;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;

/**
 * Implementation of QueryConditionFactory
 */
@Component
public class QueryConditionFactoryImpl implements QueryConditionFactory {

    @Override
    public <T> Specification<T> createEqualsCondition(String field, Object value) {
        return (root, query, cb) -> {
            if (value == null) {
                return null;
            }
            return cb.equal(root.get(field), value);
        };
    }

    @Override
    public <T> Specification<T> createLikeCondition(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.isEmpty()) {
                return null;
            }
            return cb.like(root.get(field), "%" + value + "%");
        };
    }

    @Override
    public <T> Specification<T> createRangeCondition(String field, Object min, Object max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            
            Predicate predicate = null;
            if (min != null) {
                predicate = cb.greaterThanOrEqualTo(root.get(field), (Comparable) min);
            }
            if (max != null) {
                Predicate maxPredicate = cb.lessThanOrEqualTo(root.get(field), (Comparable) max);
                predicate = predicate == null ? maxPredicate : cb.and(predicate, maxPredicate);
            }
            return predicate;
        };
    }

    // 複合條件
    @Override
    @SafeVarargs
    public final <T> Specification<T> createCompositeCondition(Specification<T>... conditions) {
        // 如果conditions為null或長度為0，則返回null
        return (root, query, cb) -> {
            if (conditions == null || conditions.length == 0) {
                return null;
            }
            
            // 創建predicates（是一種條件）陣列，邏輯為：
            // 1. 將conditions[i]轉換為Predicate
            // 2. 將predicates[i]加入predicates陣列
            // 3. 返回複合條件
            Predicate[] predicates = new Predicate[conditions.length];
            for (int i = 0; i < conditions.length; i++) {
                // 將conditions[i]轉換為Predicate
                predicates[i] = conditions[i].toPredicate(root, query, cb);
            }
            // 返回複合條件
            return cb.and(predicates);
        };
    }

    // 動態條件
    @Override
    public <T> Specification<T> createDynamicCondition(String field, Object value, String operator) {
        // 如果value為null，則返回null
        return (root, query, cb) -> {
            if (value == null) {
                return null;
            }
            
            // 根據operator選擇適當的條件
            switch (operator.toLowerCase()) {
                case "eq":
                    return cb.equal(root.get(field), value);
                case "gt":
                    return cb.greaterThan(root.get(field), (Comparable) value);
                case "lt":
                    return cb.lessThan(root.get(field), (Comparable) value);
                case "ge":
                    return cb.greaterThanOrEqualTo(root.get(field), (Comparable) value);
                case "le":
                    return cb.lessThanOrEqualTo(root.get(field), (Comparable) value);
                case "like":
                    return cb.like(root.get(field), "%" + value + "%");
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        };
    }

    @Override
    public <T> Specification<T> createIsNotNullCondition(String field) {
        return (root, query, cb) -> cb.isNotNull(root.get(field));
    }

    @Override
    public <T> Specification<T> createIsNullCondition(String field) {
        return (root, query, cb) -> cb.isNull(root.get(field));
    }
} 