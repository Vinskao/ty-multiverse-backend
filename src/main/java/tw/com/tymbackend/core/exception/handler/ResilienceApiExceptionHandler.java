package tw.com.tymbackend.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tw.com.tymbackend.core.exception.ErrorResponse;
import tw.com.tymbackend.core.exception.ResilienceException;

/**
 * Rate Limiter 相關異常處理器
 * 
 * 處理 Rate Limiter 相關的異常，
 * 提供統一的錯誤回應格式和自定義 Headers。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Component
public class ResilienceApiExceptionHandler implements ApiExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ResilienceApiExceptionHandler.class);
    
    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof ResilienceException;
    }
    
    @Override
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        ResilienceException resilienceException = (ResilienceException) ex;
        
        logger.warn("Rate Limiter 異常: {} - {}", 
                   resilienceException.getErrorType(), 
                   resilienceException.getMessage());
        
        // 創建錯誤回應
        ErrorResponse errorResponse = ErrorResponse.fromErrorCode(
            resilienceException.getErrorCode(),
            resilienceException.getMessage(),
            request.getRequestURI()
        );
        
        // 設置 Rate Limiter Header
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Rate-Limit-Exceeded", "true");
        
        // 設置 Retry-After Header
        headers.set("Retry-After", resilienceException.getRetryAfter());
        
        return ResponseEntity.status(resilienceException.getErrorCode().getHttpStatus())
                .headers(headers)
                .body(errorResponse);
    }
}
