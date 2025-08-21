package tw.com.tymbackend.module.people.domain.dto;

import java.util.List;
import java.util.Map;

/**
 * 批量傷害計算回應DTO
 */
public class BatchDamageResponseDTO {
    
    private Map<String, Integer> damageResults;
    private List<String> notFoundNames;
    
    public BatchDamageResponseDTO() {}
    
    public BatchDamageResponseDTO(Map<String, Integer> damageResults, List<String> notFoundNames) {
        this.damageResults = damageResults;
        this.notFoundNames = notFoundNames;
    }
    
    public Map<String, Integer> getDamageResults() {
        return damageResults;
    }
    
    public void setDamageResults(Map<String, Integer> damageResults) {
        this.damageResults = damageResults;
    }
    
    public List<String> getNotFoundNames() {
        return notFoundNames;
    }
    
    public void setNotFoundNames(List<String> notFoundNames) {
        this.notFoundNames = notFoundNames;
    }
}
