package tw.com.tymbackend.core.exception;

/**
 * 業務異常基類
 * 所有模組拋出的異常都應該繼承此類
 */
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    /**
     * 使用錯誤代碼創建業務異常
     * 
     * @param errorCode 錯誤代碼
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    /**
     * 使用錯誤代碼和詳細訊息創建業務異常
     * 
     * @param errorCode 錯誤代碼
     * @param message 詳細訊息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 使用錯誤代碼、詳細訊息和原因創建業務異常
     * 
     * @param errorCode 錯誤代碼
     * @param message 詳細訊息
     * @param cause 原因
     */
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 獲取錯誤代碼
     * 
     * @return 錯誤代碼
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
} 