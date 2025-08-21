package tw.com.tymbackend.core.message;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 異步處理消息 DTO
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public class AsyncMessageDTO {
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("endpoint")
    private String endpoint;
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("payload")
    private Object payload;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    public AsyncMessageDTO() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public AsyncMessageDTO(String requestId, String endpoint, String method, Object payload) {
        this.requestId = requestId;
        this.endpoint = endpoint;
        this.method = method;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "AsyncMessageDTO{" +
                "requestId='" + requestId + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                ", payload=" + payload +
                ", timestamp=" + timestamp +
                '}';
    }
}
