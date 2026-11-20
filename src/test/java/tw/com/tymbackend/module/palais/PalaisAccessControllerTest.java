package tw.com.tymbackend.module.palais;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PalaisAccessControllerTest {
    private final JdbcTemplate db = mock(JdbcTemplate.class);
    private final PalaisKeycloak keycloak = mock(PalaisKeycloak.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final PalaisAccessController controller = new PalaisAccessController(
            db, keycloak, events, transactions, "chiaki", 1, "https://sso.example/realms/test", "palais-api");

    @BeforeEach
    void permitTransactions() {
        when(transactions.getTransaction(any())).thenAnswer(call -> new SimpleTransactionStatus());
    }

    @Test
    void rejectsWrongAudienceBeforeLookingUpRequests() {
        var error = assertThrows(ResponseStatusException.class, () -> controller.me(jwt("maker", "other")));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(db, keycloak);
    }

    @Test
    void rejectsRevokedCheckerEvenWithOldRoleClaim() {
        when(keycloak.token()).thenReturn("service");
        when(keycloak.hasRole("service", "chiaki")).thenReturn(false);
        var error = assertThrows(ResponseStatusException.class, () -> controller.queue(jwt("chiaki", "palais-api"), 0));
        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verifyNoInteractions(db);
    }

    @Test
    void returnsUnavailableWhenLiveRoleVerificationFails() {
        when(keycloak.token()).thenThrow(new IllegalStateException());
        var error = assertThrows(ResponseStatusException.class, () -> controller.queue(jwt("chiaki", "palais-api"), 0));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
        verifyNoInteractions(db);
    }

    @Test
    void successfulSynchronizationWritesActivationAfterKeycloakConfirmation() {
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), any(UUID.class))).thenReturn(List.of(Map.of("id", id, "applicant", "maker", "attempts", 0)));
        when(keycloak.token()).thenReturn("service");
        when(db.update(anyString(), eq(1), eq("ACTIVE"), anyInt(), eq(id), any(UUID.class))).thenReturn(1);
        controller.syncOne();
        var order = inOrder(keycloak, db);
        order.verify(keycloak).setRole("service", "maker", true);
        order.verify(db).update(anyString(), eq(1), eq("ACTIVE"), anyInt(), eq(id), any(UUID.class));
        order.verify(db).update(anyString(), eq(id), eq("system"), eq("ACTIVATE"), isNull());
    }

    @Test
    void exhaustedSynchronizationWritesFailureWithoutActivation() {
        UUID id = UUID.randomUUID();
        when(db.queryForList(anyString(), any(UUID.class))).thenReturn(List.of(Map.of("id", id, "applicant", "maker", "attempts", 4)));
        when(keycloak.token()).thenReturn("service");
        when(db.update(anyString(), eq(5), eq("FAILED"), anyInt(), eq(id), any(UUID.class))).thenReturn(1);
        doThrow(new IllegalStateException()).when(keycloak).setRole("service", "maker", true);
        controller.syncOne();
        verify(db).update(anyString(), eq(5), eq("FAILED"), anyInt(), eq(id), any(UUID.class));
        verify(db).update(anyString(), eq(id), eq("system"), eq("SYNC_FAILED"), eq("IllegalStateException"));
        verify(events).publishEvent(new PalaisFailureNotification.Exhausted(id));
        verify(db, never()).update(anyString(), anyInt(), eq("ACTIVE"), anyInt(), eq(id), any(UUID.class));
    }

    private Jwt jwt(String subject, String audience) {
        return Jwt.withTokenValue("fixture").header("alg", "RS256").subject(subject)
                .issuer("https://sso.example/realms/test").audience(List.of(audience))
                .expiresAt(Instant.now().plusSeconds(30))
                .claim("realm_access", Map.of("roles", List.of("palais-checker"))).build();
    }
}
