package tw.com.tymbackend.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import tw.com.tymbackend.core.exception.ErrorCode;
import tw.com.tymbackend.core.exception.ErrorResponse;

/**
 * Handles validation related exceptions.
 */
@Component
@Order(2)
public class ValidationApiExceptionHandler implements ApiExceptionHandler {

    @Override
    public boolean canHandle(Exception ex) {
        return ex instanceof MethodArgumentNotValidException || ex instanceof ConstraintViolationException;
    }

    @Override
    public ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request) {
        String detail;
        if (ex instanceof MethodArgumentNotValidException manve) {
            detail = manve.getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(java.util.stream.Collectors.joining("; "));
        } else if (ex instanceof ConstraintViolationException cve) {
            detail = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(java.util.stream.Collectors.joining("; "));
        } else {
            detail = ex.getMessage();
        }
        ErrorResponse response = ErrorResponse.fromErrorCode(ErrorCode.BAD_REQUEST, detail, request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}