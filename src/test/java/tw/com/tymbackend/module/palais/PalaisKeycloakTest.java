package tw.com.tymbackend.module.palais;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PalaisKeycloakTest {
    @Test
    void grantsAndRevokesDirectRoleIdempotently() throws IOException {
        var mapped = new AtomicBoolean();
        var enabled = new AtomicBoolean(true);
        var writes = new AtomicInteger();
        var tokens = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String role = "{\"id\":\"role-id\",\"name\":\"palais-checker\"}";
        server.createContext("/realms/test/protocol/openid-connect/token", exchange -> {
            tokens.incrementAndGet();
            respond(exchange, "{\"access_token\":\"fixture\",\"expires_in\":120}");
        });
        server.createContext("/admin/realms/test/users/member", exchange ->
                respond(exchange, "{\"enabled\":" + enabled.get() + "}"));
        server.createContext("/admin/realms/test/roles/palais-checker", exchange -> respond(exchange, role));
        server.createContext("/admin/realms/test/users/member/role-mappings/realm", exchange -> {
            if (exchange.getRequestMethod().equals("GET")) {
                respond(exchange, mapped.get() ? "[" + role + "]" : "[]");
                return;
            }
            writes.incrementAndGet();
            mapped.set(exchange.getRequestMethod().equals("POST"));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        try {
            var keycloak = new PalaisKeycloak("http://127.0.0.1:" + server.getAddress().getPort(),
                    "test", "backend-fixture", "fixture-secret");
            String token = keycloak.token();
            assertEquals(token, keycloak.token());
            assertEquals(1, tokens.get());
            keycloak.setRole(token, "member", true);
            keycloak.setRole(token, "member", true);
            assertTrue(keycloak.hasRole(token, "member"));
            enabled.set(false);
            assertFalse(keycloak.hasRole(token, "member"));
            keycloak.setRole(token, "member", false);
            keycloak.setRole(token, "member", false);
            assertFalse(mapped.get());
            assertEquals(2, writes.get());
            assertThrows(IllegalStateException.class, () -> keycloak.setRole(token, "member", true));
        } finally {
            server.stop(0);
        }
    }

    private static void respond(HttpExchange exchange, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (var stream = exchange.getResponseBody()) {
            stream.write(body);
        }
    }
}
