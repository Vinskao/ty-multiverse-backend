package tw.com.tymbackend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.tymbackend.domain.vo.Keycloak;

@Repository
public interface KeycloakRepository extends JpaRepository<Keycloak, Long> {

    Keycloak findByPreferredUsername(String preferredUsername);

    Keycloak findByEmail(String email);

    void deleteByPreferredUsername(String preferredUsername);

    Keycloak findByAccessToken(String accessToken);

}
