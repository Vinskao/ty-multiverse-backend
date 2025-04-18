package tw.com.tymbackend.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * 全局異常處理器
 * 用於統一處理各種異常，並返回標準化的錯誤響應
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 處理業務異常
     * 
     * @param ex 業務異常
     * @param request 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logger.error("業務異常: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromBusinessException(ex, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ex.getErrorCode().getHttpStatus());
    }
    
    /**
     * 處理實體不存在異常
     * 
     * @param ex 異常
     * @param request 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(jakarta.persistence.EntityNotFoundException ex, HttpServletRequest request) {
        logger.error("實體不存在: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.ENTITY_NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.ENTITY_NOT_FOUND.getHttpStatus());
    }
    
    /**
     * 處理資料完整性違規異常
     * 
     * @param ex 異常
     * @param request 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.error("資料完整性違規: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.DUPLICATE_ENTRY, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.DUPLICATE_ENTRY.getHttpStatus());
    }
    
    /**
     * 處理樂觀鎖定失敗異常
     * 
     * @param ex 異常
     * @param request 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(OptimisticLockingFailureException ex, HttpServletRequest request) {
        logger.error("樂觀鎖定失敗: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.OPTIMISTIC_LOCKING_FAILURE, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.OPTIMISTIC_LOCKING_FAILURE.getHttpStatus());
    }
    
    /**
     * 處理參數驗證異常
     * 
     * @param ex 異常
     * @param request 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        logger.error("參數驗證失敗: {}", errors, ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, errors, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.BAD_REQUEST.getHttpStatus());
    }
    
    /**
     * 處理約束違規異常
     * 
     * @param ex 異常
     * @param request 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String errors = ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));
        logger.error("約束違規: {}", errors, ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, errors, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.BAD_REQUEST.getHttpStatus());
    }
    
    /**
     * 處理綁定異常
     * 
     * @param ex 異常
     * @param request 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        logger.error("綁定異常: {}", errors, ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, errors, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.BAD_REQUEST.getHttpStatus());
    }
    
    /**
     * 處理其他未處理的異常
     * 
     * @param ex 異常
     * @param request 請求
     * @return 錯誤響應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        logger.error("未處理的異常: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
} 