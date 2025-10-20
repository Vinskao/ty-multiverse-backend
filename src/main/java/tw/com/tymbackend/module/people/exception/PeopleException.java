package tw.com.tymbackend.module.people.exception;

import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.ErrorCode;

/**
 * People 模組的特定異常類
 */
public class PeopleException extends BusinessException {
    
    public PeopleException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public PeopleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public PeopleException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 