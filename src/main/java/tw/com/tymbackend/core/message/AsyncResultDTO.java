package tw.com.tymbackend.core.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * 異步處理結果 DTO
 * 
 * 用於存儲和傳輸異步處理的結果，包含處理狀態、結果數據和時間戳。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public class AsyncResultDTO {
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("status")
    private String status; // "processing", "completed", "failed"
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("error")
    private String error; // 錯誤訊息，僅在 status 為 "failed" 時使用
    
    public AsyncResultDTO() {
        this.timestamp = Instant.now().toString();
    }
    
    public AsyncResultDTO(String requestId, Object data, String status) {
        this.requestId = requestId;
        this.data = data;
        this.status = status;
        this.timestamp = Instant.now().toString();
    }
    
    public AsyncResultDTO(String requestId, Object data, String status, String error) {
        this.requestId = requestId;
        this.data = data;
        this.status = status;
        this.error = error;
        this.timestamp = Instant.now().toString();
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "AsyncResultDTO{" +
                "requestId='" + requestId + '\'' +
                ", data=" + data +
                ", status='" + status + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
