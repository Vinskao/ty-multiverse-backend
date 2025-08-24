package tw.com.tymbackend.core.message;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * People 獲取所有回應 DTO
 * 
 * 專門用於 People 獲取所有操作的回應
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeopleGetAllResponseDTO {
    
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
     * 數據數量
     */
    private int count;
    
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
     * 成功回應建構函數
     */
    public static PeopleGetAllResponseDTO success(String requestId, String message, Object data, int count) {
        PeopleGetAllResponseDTO response = new PeopleGetAllResponseDTO();
        response.setRequestId(requestId);
        response.setStatus("SUCCESS");
        response.setMessage(message);
        response.setData(data);
        response.setCount(count);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    /**
     * 錯誤回應建構函數
     */
    public static PeopleGetAllResponseDTO error(String requestId, String message, String errorCode, String errorDetails) {
        PeopleGetAllResponseDTO response = new PeopleGetAllResponseDTO();
        response.setRequestId(requestId);
        response.setStatus("ERROR");
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setErrorDetails(errorDetails);
        response.setCount(0);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    /**
     * 處理中回應建構函數
     */
    public static PeopleGetAllResponseDTO processing(String requestId, String message) {
        PeopleGetAllResponseDTO response = new PeopleGetAllResponseDTO();
        response.setRequestId(requestId);
        response.setStatus("PROCESSING");
        response.setMessage(message);
        response.setCount(0);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}
