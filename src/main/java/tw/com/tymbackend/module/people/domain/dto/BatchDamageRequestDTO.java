package tw.com.tymbackend.module.people.domain.dto;

import java.util.List;

/**
 * 批量傷害計算請求DTO
 */
public class BatchDamageRequestDTO {
    
    private List<String> names;
    
    public BatchDamageRequestDTO() {}
    
    public BatchDamageRequestDTO(List<String> names) {
        this.names = names;
    }
    
    public List<String> getNames() {
        return names;
    }
    
    public void setNames(List<String> names) {
        this.names = names;
    }
}
