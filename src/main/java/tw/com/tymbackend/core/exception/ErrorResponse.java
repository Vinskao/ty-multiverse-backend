package tw.com.tymbackend.core.exception;

import java.time.LocalDateTime;

/**
 * 錯誤響應類
 * 用於統一錯誤響應格式
 */
public class ErrorResponse {
    
    private final int code;
    private final String message;
    private final String detail;
    private final LocalDateTime timestamp;
    private final String path;
    
    /**
     * 創建錯誤響應
     * 
     * @param code 錯誤代碼
     * @param message 錯誤訊息
     * @param detail 詳細訊息
     * @param path 請求路徑
     */
    public ErrorResponse(int code, String message, String detail, String path) {
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
    
    /**
     * 從錯誤代碼創建錯誤響應
     * 
     * @param errorCode 錯誤代碼
     * @param detail 詳細訊息
     * @param path 請求路徑
     * @return 錯誤響應
     */
    public static ErrorResponse fromErrorCode(ErrorCode errorCode, String detail, String path) {
        return new ErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            detail,
            path
        );
    }
    
    /**
     * 從業務異常創建錯誤響應
     * 
     * @param exception 業務異常
     * @param path 請求路徑
     * @return 錯誤響應
     */
    public static ErrorResponse fromBusinessException(BusinessException exception, String path) {
        return new ErrorResponse(
            exception.getErrorCode().getCode(),
            exception.getErrorCode().getMessage(),
            exception.getMessage(),
            path
        );
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getPath() {
        return path;
    }
} 