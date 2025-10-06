package tw.com.tymbackend.core.config.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * 基於信號量的全域並發請求限制器，避免在資源受限環境下 CPU 飆升。
 * 允許量與等待時間可透過環境變數調整。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestConcurrencyLimiter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestConcurrencyLimiter.class);

    private final Semaphore semaphore;
    private final long tryAcquireTimeoutMs;

    public RequestConcurrencyLimiter(
            @Value("${app.concurrency.max-concurrent-requests:2}") int maxConcurrentRequests,
            @Value("${app.concurrency.try-acquire-timeout-ms:200}") long tryAcquireTimeoutMs) {
        int permits = Math.max(1, maxConcurrentRequests);
        this.semaphore = new Semaphore(permits, true);
        this.tryAcquireTimeoutMs = tryAcquireTimeoutMs;
        logger.info("RequestConcurrencyLimiter initialized: permits={}, timeoutMs={}", permits, tryAcquireTimeoutMs);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean acquired = false;
        try {
            // 盡量在設定的超時內取得許可，避免長時間阻塞導致堆積
            if (tryAcquireTimeoutMs > 0) {
                acquired = semaphore.tryAcquire(tryAcquireTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            } else {
                acquired = semaphore.tryAcquire();
            }
            if (!acquired) {
                // 簡單丟擲 429，交由上層全域錯誤處理（Spring Boot 預設會處理）
                logger.warn("Too many concurrent requests - path={}",
                        (request instanceof HttpServletRequest r) ? r.getRequestURI() : "N/A");
                throw new ServletException("Too many concurrent requests");
            }
            chain.doFilter(request, response);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ServletException("Interrupted while waiting for concurrency permit", ie);
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
    }
}


