package tw.com.tymbackend.core.message;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Producer 回應消息 DTO
 * 
 * 用於封裝從 Consumer 回傳給 Producer 的數據
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProducerResponseDTO {
    
    /**
     * 請求ID，用於匹配原始請求
     */
    private String requestId;
    
    /**
     * 回應狀態
     */
    private String status;
    
    /**
     * 回應消息
     */
    private String message;
    
    /**
     * 回應數據
     */
    private Object data;
    
    /**
     * 時間戳
     */
    private Long timestamp;
    
    /**
     * 錯誤代碼（如果有錯誤）
     */
    private String errorCode;
    
    /**
     * 錯誤詳情（如果有錯誤）
     */
    private String errorDetails;
    
    /**
     * 建構函數
     */
    public ProducerResponseDTO(String requestId, String status, String message, Object data) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功回應建構函數
     */
    public static ProducerResponseDTO success(String requestId, String message, Object data) {
        return new ProducerResponseDTO(requestId, "SUCCESS", message, data);
    }
    
    /**
     * 錯誤回應建構函數
     */
    public static ProducerResponseDTO error(String requestId, String message, String errorCode, String errorDetails) {
        ProducerResponseDTO response = new ProducerResponseDTO(requestId, "ERROR", message, null);
        response.setErrorCode(errorCode);
        response.setErrorDetails(errorDetails);
        return response;
    }
    
    /**
     * 處理中回應建構函數
     */
    public static ProducerResponseDTO processing(String requestId, String message) {
        return new ProducerResponseDTO(requestId, "PROCESSING", message, null);
    }
}
