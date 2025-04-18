package tw.com.tymbackend.module.livestock.exception;

import tw.com.tymbackend.core.exception.BusinessException;
import tw.com.tymbackend.core.exception.ErrorCode;

/**
 * Livestock 模組的特定異常類
 */
public class LivestockException extends BusinessException {
    
    public LivestockException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public LivestockException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public LivestockException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 