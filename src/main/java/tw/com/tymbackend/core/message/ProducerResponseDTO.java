package tw.com.tymbackend.core.message;

import java.time.LocalDateTime;

/**
 * Producer 響應 DTO
 * 
 * 用於統一處理 producer 模式的響應格式
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public class ProducerResponseDTO {
    
    private String message;
    private String requestId;
    private String status;
    private LocalDateTime timestamp;
    private Object data;
    
    public ProducerResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ProducerResponseDTO(String message, String requestId, String status) {
        this();
        this.message = message;
        this.requestId = requestId;
        this.status = status;
    }
    
    public ProducerResponseDTO(String message, String requestId, String status, Object data) {
        this(message, requestId, status);
        this.data = data;
    }
    
    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    /**
     * 創建成功響應
     */
    public static ProducerResponseDTO success(String message, String requestId) {
        return new ProducerResponseDTO(message, requestId, "PENDING");
    }
    
    /**
     * 創建成功響應（帶數據）
     */
    public static ProducerResponseDTO success(String message, String requestId, Object data) {
        return new ProducerResponseDTO(message, requestId, "PENDING", data);
    }
    
    /**
     * 創建錯誤響應
     */
    public static ProducerResponseDTO error(String message, String requestId) {
        return new ProducerResponseDTO(message, requestId, "ERROR");
    }
}
