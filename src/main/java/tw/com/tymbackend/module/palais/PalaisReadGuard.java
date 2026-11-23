package tw.com.tymbackend.module.palais;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Owner, or a Checker whose role is confirmed live in Keycloak, so a revoked Checker is refused even with an older token. */
@Component
public class PalaisReadGuard {
    private final ObjectProvider<PalaisKeycloak> keycloak;

    PalaisReadGuard(ObjectProvider<PalaisKeycloak> keycloak) {
        this.keycloak = keycloak;
    }

    public void require(Authentication user) {
        if (user.getAuthorities().stream().anyMatch(a -> "ROLE_manage-users".equals(a.getAuthority()))) {
            return;
        }
        PalaisKeycloak live = keycloak.getIfAvailable();
        boolean allowed;
        try {
            allowed = live != null && live.hasRole(live.token(), user.getName());
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Checker verification unavailable");
        }
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
