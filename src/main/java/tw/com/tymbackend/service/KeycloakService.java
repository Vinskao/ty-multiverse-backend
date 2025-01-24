package tw.com.tymbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import  tw.com.tymbackend.dao.KeycloakRepository;
import  tw.com.tymbackend.domain.vo.Keycloak;

@Service
public class KeycloakService {
    @Autowired
    private KeycloakRepository keycloakRepository;

    public Keycloak saveKeycloakData(Keycloak keycloak) {
        return keycloakRepository.save(keycloak);
    }

    public Keycloak findByUsername(String preferredUsername) {
        return keycloakRepository.findByPreferredUsername(preferredUsername);
    }

    public Keycloak findByEmail(String email) {
        return keycloakRepository.findByEmail(email);
    }

    @Transactional
    public void deleteByUsername(String preferredUsername) {
        keycloakRepository.deleteByPreferredUsername(preferredUsername);
    }

    public Keycloak findByAccessToken(String accessToken) {
        return keycloakRepository.findByAccessToken(accessToken);
    }
}
