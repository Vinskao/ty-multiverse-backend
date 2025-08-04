package tw.com.tymbackend.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import tw.com.tymbackend.core.exception.ErrorResponse;

/**
 * Chain-of-Responsibility handler interface for API exceptions.
 *
 * Implementations should return <code>true</code> from {@link #canHandle(Exception)}
 * if they are able to create a {@link ResponseEntity} for the supplied exception.
 */
public interface ApiExceptionHandler {

    /**
     * Whether this handler can handle the supplied exception.
     */
    boolean canHandle(Exception ex);

    /**
     * Handle the supplied exception and return a {@link ResponseEntity} containing
     * the {@link ErrorResponse} body.
     */
    ResponseEntity<ErrorResponse> handle(Exception ex, HttpServletRequest request);
}