package tw.com.tymbackend.core.config.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
                // 必須回 429 而不是 500：被限流是「稍後重試」而不是「伺服器壞了」，
                // 丟 ServletException 會變成 500，呼叫端（例如 Apps Script 同步）無從分辨，
                // 曾因此把「刪除失敗」當成成功、接著 insert 造成 duplicate key。
                String path = (request instanceof HttpServletRequest r) ? r.getRequestURI() : "N/A";
                logger.warn("Too many concurrent requests - path={}", path);
                if (response instanceof HttpServletResponse res) {
                    res.setStatus(429); // Servlet API 沒有 429 的常數
                    res.setHeader("Retry-After", "5");
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(
                            "{\"success\":false,\"code\":429,\"message\":\"請求過於頻繁，請稍後重試\"}");
                    return;
                }
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


