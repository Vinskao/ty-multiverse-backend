package tw.com.tymbackend.core.exception;

/**
 * Rate Limiter 相關異常
 * 
 * 用於處理 Rate Limiter 相關的異常情況。
 * 提供統一的異常處理機制，與現有的責任鏈設計整合。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public class ResilienceException extends BusinessException {
    
    private final String retryAfter;
    private final String errorType;
    
    /**
     * 建構子
     * 
     * @param errorCode 錯誤碼
     * @param retryAfter 建議重試時間
     * @param errorType 錯誤類型
     */
    public ResilienceException(ErrorCode errorCode, String retryAfter, String errorType) {
        super(errorCode, errorCode.getMessage());
        this.retryAfter = retryAfter;
        this.errorType = errorType;
    }
    
    /**
     * 建構子
     * 
     * @param errorCode 錯誤碼
     * @param message 自定義錯誤訊息
     * @param retryAfter 建議重試時間
     * @param errorType 錯誤類型
     */
    public ResilienceException(ErrorCode errorCode, String message, String retryAfter, String errorType) {
        super(errorCode, message);
        this.retryAfter = retryAfter;
        this.errorType = errorType;
    }
    
    /**
     * 取得建議重試時間
     * 
     * @return 重試時間字串
     */
    public String getRetryAfter() {
        return retryAfter;
    }
    
    /**
     * 取得錯誤類型
     * 
     * @return 錯誤類型字串
     */
    public String getErrorType() {
        return errorType;
    }
    
    /**
     * 創建 Rate Limiter 超量異常
     * 
     * @return ResilienceException
     */
    public static ResilienceException rateLimitExceeded() {
        return new ResilienceException(
            ErrorCode.RATE_LIMIT_EXCEEDED,
            "1s",
            "RATE_LIMIT_EXCEEDED"
        );
    }
}
