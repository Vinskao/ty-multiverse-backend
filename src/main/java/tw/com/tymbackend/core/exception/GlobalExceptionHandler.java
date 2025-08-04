package tw.com.tymbackend.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import tw.com.tymbackend.core.exception.ErrorCode;
import tw.com.tymbackend.core.exception.handler.ApiExceptionHandler;

/**
 * 全局異常處理器
 * 
 * 統一處理應用程序中的各種異常，並返回標準化的錯誤響應。
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final List<ApiExceptionHandler> handlerChain;

    @Autowired
    public GlobalExceptionHandler(List<ApiExceptionHandler> handlerChain) {
        this.handlerChain = handlerChain;
    }

    /**
     * 處理方法參數驗證失敗異常
     * 
     * @param ex 方法參數驗證失敗異常
     * @param headers HTTP 標頭
     * @param status HTTP 狀態碼
     * @param request Web 請求
     * @return 錯誤響應
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @SuppressWarnings("null") @NonNull MethodArgumentNotValidException ex,
            @SuppressWarnings("null") @NonNull HttpHeaders headers,
            @SuppressWarnings("null") @NonNull HttpStatusCode status,
            @SuppressWarnings("null") @NonNull WebRequest request) {
        logger.error("方法參數驗證失敗: {}", ex.getMessage(), ex);
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining("; "));
        
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, "參數驗證失敗", detail);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            @SuppressWarnings("null") @NonNull MaxUploadSizeExceededException ex,
            @SuppressWarnings("null") @NonNull HttpHeaders headers,
            @SuppressWarnings("null") @NonNull HttpStatusCode status,
            @SuppressWarnings("null") @NonNull WebRequest request) {
        logger.error("文件上傳大小超限: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, "文件大小超過限制", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 使用責任鏈模式處理所有異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        for (ApiExceptionHandler handler : handlerChain) {
            if (handler.canHandle(ex)) {
                return handler.handle(ex, request);
            }
        }
        // 理論上不會走到這裡，因為 DefaultApiExceptionHandler 會處理所有剩餘異常
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
} 