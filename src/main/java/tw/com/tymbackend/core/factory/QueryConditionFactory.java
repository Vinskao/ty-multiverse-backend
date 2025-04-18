package tw.com.tymbackend.core.factory;

import org.springframework.data.jpa.domain.Specification;

/**
 * Factory interface for creating query conditions
 */
public interface QueryConditionFactory {
    
    /**
     * Create an equals condition
     * 
     * @param field the field to compare
     * @param value the value to compare against
     * @param <T> the entity type
     * @return a Specification for equals condition
     */
    <T> Specification<T> createEqualsCondition(String field, Object value);
    
    /**
     * Create a like condition
     * 
     * @param field the field to compare
     * @param value the value to compare against
     * @param <T> the entity type
     * @return a Specification for like condition
     */
    <T> Specification<T> createLikeCondition(String field, String value);
    
    /**
     * Create a range condition
     * 
     * @param field the field to compare
     * @param min the minimum value
     * @param max the maximum value
     * @param <T> the entity type
     * @return a Specification for range condition
     */
    <T> Specification<T> createRangeCondition(String field, Object min, Object max);
    
    /**
     * Create a composite condition from multiple conditions
     * 
     * @param conditions the conditions to combine
     * @param <T> the entity type
     * @return a Specification for composite condition
     */
    <T> Specification<T> createCompositeCondition(Specification<T>... conditions);
    
    /**
     * Create a dynamic condition based on operator
     * 
     * @param field the field to compare
     * @param value the value to compare against
     * @param operator the operator to use (eq, gt, lt, ge, le, like)
     * @param <T> the entity type
     * @return a Specification for dynamic condition
     */
    <T> Specification<T> createDynamicCondition(String field, Object value, String operator);
} 