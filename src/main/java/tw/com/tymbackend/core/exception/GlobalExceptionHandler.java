package tw.com.tymbackend.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 全局異常處理器
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 處理業務邏輯異常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logger.error("Business exception occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }

    /**
     * 處理實體未找到異常
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(jakarta.persistence.EntityNotFoundException ex, HttpServletRequest request) {
        logger.error("Entity not found: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * 處理數據完整性違規異常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.CONFLICT, "數據完整性違規", request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * 處理樂觀鎖定失敗異常
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(OptimisticLockingFailureException ex, HttpServletRequest request) {
        logger.error("Optimistic locking failure: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.CONFLICT, "數據已被其他用戶修改", request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * 處理方法參數驗證失敗異常
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        logger.error("Method argument not valid: {}", ex.getMessage(), ex);
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce("", (a, b) -> a + (a.isEmpty() ? "" : "; ") + b);
        
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, "參數驗證失敗", detail);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 處理約束違規異常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        logger.error("Constraint violation: {}", ex.getMessage(), ex);
        String detail = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .reduce("", (a, b) -> a + (a.isEmpty() ? "" : "; ") + b);
        
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, "約束違規", detail);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 處理文件上傳大小超限異常
     */
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            @NonNull MaxUploadSizeExceededException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        logger.error("File upload size exceeded: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, "文件大小超過限制", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 處理全局異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.INTERNAL_SERVER_ERROR, "服務器內部錯誤", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 