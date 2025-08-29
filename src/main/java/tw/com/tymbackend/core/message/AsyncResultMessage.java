package tw.com.tymbackend.core.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * 異步結果消息 DTO
 *
 * 用於 Consumer 向 Producer 發送處理結果
 * 與消費者應用保持一致的消息格式
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public class AsyncResultMessage {

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("status")
    private String status; // "completed", "failed"

    @JsonProperty("data")
    private Object data;

    @JsonProperty("error")
    private String error;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timestamp;

    @JsonProperty("source")
    private String source; // "consumer"

    // 默認構造函數
    public AsyncResultMessage() {
        this.timestamp = Instant.now().toString();
        this.source = "consumer";
    }

    // 全參數構造函數
    public AsyncResultMessage(String requestId, String status, Object data, String error) {
        this();
        this.requestId = requestId;
        this.status = status;
        this.data = data;
        this.error = error;
    }

    // 成功結果靜態工廠方法
    public static AsyncResultMessage completed(String requestId, Object data) {
        return new AsyncResultMessage(requestId, "completed", data, null);
    }

    // 失敗結果靜態工廠方法
    public static AsyncResultMessage failed(String requestId, String error) {
        return new AsyncResultMessage(requestId, "failed", null, error);
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