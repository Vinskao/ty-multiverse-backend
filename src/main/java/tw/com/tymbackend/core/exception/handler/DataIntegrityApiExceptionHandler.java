package tw.com.tymbackend.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tw.com.tymbackend.core.exception.ErrorCode;
import tw.com.tymbackend.core.exception.ErrorResponse;

/**
 * Handles data-integrity and concurrency related exceptions.
 */
@Component
@Order(1)
public class DataIntegrityApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof DataIntegrityViolationException || ex instanceof OptimisticLockingFailureException;
    }

    @Override
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        ErrorResponse response;
        HttpStatus status;
        if (ex instanceof OptimisticLockingFailureException) {
            response = ErrorResponse.fromErrorCode(ErrorCode.OPTIMISTIC_LOCKING_FAILURE, ex.getMessage(), request.getRequestURI());
            status = ErrorCode.OPTIMISTIC_LOCKING_FAILURE.getHttpStatus();
        } else {
            response = ErrorResponse.fromErrorCode(ErrorCode.CONFLICT, ex.getMessage(), request.getRequestURI());
            status = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(response, status);
    }
}