package tw.com.tymbackend.module.gallery.exception;

import tw.com.ty.common.exception.BusinessException;
import tw.com.ty.common.exception.ErrorCode;

/**
 * Gallery 模組的特定異常類
 */
public class GalleryException extends BusinessException {
    
    public GalleryException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public GalleryException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public GalleryException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
} 