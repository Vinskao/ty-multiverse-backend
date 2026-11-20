package tw.com.tymbackend.module.palais;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "palais.access.enabled", havingValue = "true")
class PalaisKeycloak {
    private final RestClient client;
    private final String clientId;
    private final String secret;
    private final String realm;
    private String cachedToken;
    private Instant tokenExpiresAt = Instant.EPOCH;
    private static final String ROLE = "palais-checker";

    PalaisKeycloak(@Value("${keycloak.auth-server-url}") String url,
                   @Value("${keycloak.realm}") String realm,
                   @Value("${palais.access.client-id}") String clientId,
                   @Value("${palais.access.client-secret}") String secret) {
        if (url.isBlank() || realm.isBlank() || clientId.isBlank() || secret.isBlank()) {
            throw new IllegalArgumentException("Incomplete Palais Keycloak configuration");
        }
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
        this.realm = realm;
        this.clientId = clientId;
        this.secret = secret;
    }

    synchronized String token() {
        Instant now = Instant.now();
        if (cachedToken != null && now.isBefore(tokenExpiresAt)) {
            return cachedToken;
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", secret);
        JsonNode response = client.post().uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(JsonNode.class);
        String token = response == null ? "" : response.path("access_token").asText();
        if (token.isBlank()) {
            throw new IllegalStateException("Keycloak did not return an access token");
        }
        cachedToken = token;
        tokenExpiresAt = now.plusSeconds(Math.max(0, response.path("expires_in").asLong(0) - 30));
        return token;
    }

    boolean hasRole(String token, String user) {
        JsonNode account = account(token, user);
        return account != null && account.path("enabled").asBoolean() && mapped(token, user);
    }

    private JsonNode account(String token, String user) {
        try {
            return client.get().uri("/admin/realms/{realm}/users/{user}", realm, user)
                    .headers(headers -> headers.setBearerAuth(token)).retrieve().body(JsonNode.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound missingUser) {
            return null;
        }
    }

    private boolean mapped(String token, String user) {
        JsonNode roles = client.get()
                .uri("/admin/realms/{realm}/users/{user}/role-mappings/realm", realm, user)
                .headers(headers -> headers.setBearerAuth(token)).retrieve().body(JsonNode.class);
        if (roles == null || !roles.isArray()) {
            throw new IllegalStateException("Invalid Keycloak role response");
        }
        for (JsonNode role : roles) {
            if (ROLE.equals(role.path("name").asText())) {
                return true;
            }
        }
        return false;
    }

    void setRole(String token, String user, boolean grant) {
        JsonNode account = account(token, user);
        if (grant && (account == null || !account.path("enabled").asBoolean())) {
            throw new IllegalStateException("Target Keycloak account is not enabled");
        }
        if (account == null || mapped(token, user) == grant) {
            return;
        }
        JsonNode role = client.get().uri("/admin/realms/{realm}/roles/{role}", realm, ROLE)
                .headers(headers -> headers.setBearerAuth(token)).retrieve().body(JsonNode.class);
        if (role == null || role.path("id").asText().isBlank() || !ROLE.equals(role.path("name").asText())) {
            throw new IllegalStateException("Keycloak role was not found");
        }
        client.method(grant ? org.springframework.http.HttpMethod.POST : org.springframework.http.HttpMethod.DELETE)
                .uri("/admin/realms/{realm}/users/{user}/role-mappings/realm", realm, user)
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON).body(List.of(role)).retrieve().toBodilessEntity();
        if (mapped(token, user) != grant) {
            throw new IllegalStateException("Keycloak role change was not confirmed");
        }
    }
}
