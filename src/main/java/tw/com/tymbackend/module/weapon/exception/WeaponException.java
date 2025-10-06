package tw.com.tymbackend.module.weapon.exception;

import tw.com.tymbackend.core.exception.BusinessException;
import tw.com.tymbackend.core.exception.ErrorCode;

/**
 * Weapon 模組的特定異常類
 */
public class WeaponException extends BusinessException {
    
    public WeaponException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public WeaponException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public WeaponException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 