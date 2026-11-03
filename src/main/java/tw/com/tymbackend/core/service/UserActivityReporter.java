package tw.com.tymbackend.core.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reports what a signed-in user did (create / read / update / delete) to maya-sawa's append-only activity log, so the owner can see
 * per user what they did on the site. Best effort: it runs on a small background thread, never blocks or fails the request, and
 * does nothing when {@code user-activity.url} or {@code user-activity.secret} is not configured. The user is the verified token's
 * {@code sub}; no email and no token is sent.
 */
@Component
public class UserActivityReporter {
    private static final Logger log = LoggerFactory.getLogger(UserActivityReporter.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final String url;
    private final String secret;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(200), runnable -> {
        Thread thread = new Thread(runnable, "user-activity-reporter");
        thread.setDaemon(true);
        return thread;
    });

    public UserActivityReporter(@Value("${user-activity.url:}") String url, @Value("${user-activity.secret:}") String secret) {
        this.url = url == null ? "" : url.trim();
        this.secret = secret == null ? "" : secret.trim();
    }

    public void report(Authentication authentication, String action, String resourceType, String resourceId, Map<String, Object> detail) {
        if (url.isEmpty() || secret.isEmpty() || !(authentication instanceof JwtAuthenticationToken token)) {
            return;
        }
        String sub = token.getToken().getSubject();
        if (sub == null || sub.isBlank()) {
            return;
        }
        String username = token.getToken().getClaimAsString("preferred_username");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sub", sub);
        body.put("username", username == null ? "" : username);
        body.put("action", action);
        body.put("resourceType", resourceType);
        body.put("resourceId", resourceId == null ? "" : resourceId);
        body.put("detail", detail == null ? Map.of() : detail);
        try {
            executor.execute(() -> post(body));
        } catch (RejectedExecutionException full) {
            log.warn("User activity queue is full; dropped {} {}", action, resourceType);
        }
    }

    private void post(Map<String, Object> body) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json").header("X-Internal-Secret", secret)
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body))).build();
            int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (status >= 300) {
                log.warn("User activity was refused with HTTP {}", status);
            }
        } catch (Exception exception) {
            log.warn("User activity could not be reported: {}", exception.getClass().getSimpleName());
        }
    }
}
