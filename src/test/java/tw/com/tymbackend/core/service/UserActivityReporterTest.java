package tw.com.tymbackend.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.sun.net.httpserver.HttpServer;

/**
 * The learn backend reports what each signed-in user did to maya-sawa's activity log
 * ({@code POST /market/internal/activity}, internal secret). Reporting is best effort: it never blocks or fails a quiz.
 */
class UserActivityReporterTest {
    private HttpServer server;
    private final BlockingQueue<String[]> received = new LinkedBlockingQueue<>();
    private volatile int status = 200;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/maya-sawa/market/internal/activity", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            received.add(new String[] { exchange.getRequestHeaders().getFirst("X-Internal-Secret"),
                    exchange.getRequestHeaders().getFirst("Content-Type"), body });
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/maya-sawa/market/internal/activity";
    }

    private static Authentication alice() {
        Jwt token = Jwt.withTokenValue("t").header("alg", "none").subject("alice-sub").claim("preferred_username", "alice").build();
        return new JwtAuthenticationToken(token);
    }

    @Test
    void report_Should_PostTheUserAndTheActionWithTheInternalSecret() throws Exception {
        var reporter = new UserActivityReporter(url(), "s3cret");
        reporter.report(alice(), "C", "learn-attempt", "42", Map.of("quizId", "toeic-1", "score", 7));
        String[] call = received.poll(5, TimeUnit.SECONDS);
        assertThat(call).isNotNull();
        assertThat(call[0]).isEqualTo("s3cret");
        assertThat(call[1]).startsWith("application/json");
        assertThat(call[2]).contains("\"sub\":\"alice-sub\"").contains("\"username\":\"alice\"").contains("\"action\":\"C\"")
                .contains("\"resourceType\":\"learn-attempt\"").contains("\"resourceId\":\"42\"")
                .contains("\"quizId\":\"toeic-1\"").contains("\"score\":7");
    }

    @Test
    void report_Should_NotFail_When_MayaSawaAnswersWithAnError() throws Exception {
        status = 503;
        var reporter = new UserActivityReporter(url(), "s3cret");
        assertThatCode(() -> reporter.report(alice(), "C", "learn-attempt", "1", Map.of())).doesNotThrowAnyException();
        assertThat(received.poll(5, TimeUnit.SECONDS)).isNotNull();
    }

    @Test
    void report_Should_NotFail_When_MayaSawaIsUnreachable() {
        server.stop(0);
        var reporter = new UserActivityReporter(url(), "s3cret");
        assertThatCode(() -> reporter.report(alice(), "C", "learn-attempt", "1", Map.of())).doesNotThrowAnyException();
    }

    @Test
    void report_Should_DoNothing_When_NotConfigured() throws Exception {
        new UserActivityReporter("", "").report(alice(), "C", "learn-attempt", "1", Map.of());
        new UserActivityReporter(url(), "").report(alice(), "C", "learn-attempt", "1", Map.of());
        assertThat(received.poll(500, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void report_Should_IgnoreAnUnauthenticatedCaller() throws Exception {
        new UserActivityReporter(url(), "s3cret").report(null, "C", "learn-attempt", "1", Map.of());
        assertThat(received.poll(500, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void report_Should_FallBackToTheSubject_When_ThereIsNoUserName() throws Exception {
        Jwt token = Jwt.withTokenValue("t").header("alg", "none").subject("bob-sub").build();
        new UserActivityReporter(url(), "s3cret").report(new JwtAuthenticationToken(token), "R", "learn-review", "9", Map.of());
        String[] call = received.poll(5, TimeUnit.SECONDS);
        assertThat(call[2]).contains("\"sub\":\"bob-sub\"").contains("\"username\":\"\"");
    }
}
