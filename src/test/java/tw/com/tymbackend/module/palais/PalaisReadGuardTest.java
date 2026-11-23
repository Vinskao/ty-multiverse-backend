package tw.com.tymbackend.module.palais;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

class PalaisReadGuardTest {
    @SuppressWarnings("unchecked")
    private final ObjectProvider<PalaisKeycloak> provider = mock(ObjectProvider.class);
    private final PalaisKeycloak keycloak = mock(PalaisKeycloak.class);
    private final PalaisReadGuard guard = new PalaisReadGuard(provider);
    private final TestingAuthenticationToken checker = new TestingAuthenticationToken("id", null, "ROLE_palais-checker");

    private int refused() {
        return assertThrows(ResponseStatusException.class, () -> guard.require(checker)).getStatusCode().value();
    }

    @Test
    void ownerNeedsNoLiveCheck() {
        guard.require(new TestingAuthenticationToken("id", null, "ROLE_manage-users"));
        verifyNoInteractions(provider);
    }

    @Test
    void checkerIsConfirmedLive() {
        assertEquals(403, refused());
        when(provider.getIfAvailable()).thenReturn(keycloak);
        when(keycloak.token()).thenReturn("t");
        assertEquals(403, refused());
        when(keycloak.hasRole("t", "id")).thenReturn(true);
        guard.require(checker);
        when(keycloak.token()).thenThrow(new IllegalStateException("down"));
        assertEquals(503, refused());
    }
}
