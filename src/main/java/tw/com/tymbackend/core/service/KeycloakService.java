package tw.com.tymbackend.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.core.dao.KeycloakRepository;
import tw.com.tymbackend.core.domain.vo.Keycloak;

@Service
public class KeycloakService extends BaseService {
    @Autowired
    private KeycloakRepository keycloakRepository;

    public Keycloak saveKeycloakData(Keycloak keycloak) {
        return executeInTransaction(status -> keycloakRepository.save(keycloak));
    }

    public Keycloak findByUsername(String preferredUsername) {
        return keycloakRepository.findByPreferredUsername(preferredUsername);
    }

    public Keycloak findByEmail(String email) {
        return keycloakRepository.findByEmail(email);
    }

    @Transactional
    public void deleteByUsername(String preferredUsername) {
        executeInTransaction(status -> {
            keycloakRepository.deleteByPreferredUsername(preferredUsername);
            return null;
        });
    }

    public Keycloak findByAccessToken(String accessToken) {
        return keycloakRepository.findByAccessToken(accessToken);
    }
}
