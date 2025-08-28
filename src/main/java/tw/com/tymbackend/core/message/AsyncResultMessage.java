package tw.com.tymbackend.core.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * 異步結果消息
 * 
 * 用於在 RabbitMQ 中傳遞異步處理的結果，Producer 和 Consumer 都認得這個格式。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public class AsyncResultMessage {
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("status")
    private String status; // "processing", "completed", "failed"
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("source")
    private String source; // "producer" 或 "consumer"
    
    public AsyncResultMessage() {
        this.timestamp = Instant.now().toString();
    }
    
    public AsyncResultMessage(String requestId, String status, Object data, String source) {
        this.requestId = requestId;
        this.status = status;
        this.data = data;
        this.source = source;
        this.timestamp = Instant.now().toString();
    }
    
    public AsyncResultMessage(String requestId, String status, Object data, String error, String source) {
        this.requestId = requestId;
        this.status = status;
        this.data = data;
        this.error = error;
        this.source = source;
        this.timestamp = Instant.now().toString();
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    @Override
    public String toString() {
        return "AsyncResultMessage{" +
                "requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", data=" + data +
                ", error='" + error + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
