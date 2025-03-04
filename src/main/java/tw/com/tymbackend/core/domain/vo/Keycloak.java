package tw.com.tymbackend.core.domain.vo;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "keycloak", indexes = {
        @Index(name = "inx_username", columnList = "preferred_username"),
        @Index(name = "inx_email", columnList = "email")
})
public class Keycloak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "preferred_username", nullable = false, length = 255)
    private String preferredUsername;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "given_name", length = 255)
    private String givenName;

    @Column(name = "family_name", length = 255)
    private String familyName;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "sub", length = 255)
    private String sub;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "expires_in")
    private Instant expiresIn;

    @Column(name = "issued_at")
    private Instant issuedAt;

    public Keycloak() {
    }

    public Keycloak(Long id, String preferredUsername, String email, String givenName, String familyName,
            Boolean emailVerified, String sub, String accessToken, String refreshToken, Instant expiresIn,
            Instant issuedAt) {
        this.id = id;
        this.preferredUsername = preferredUsername;
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
        this.emailVerified = emailVerified;
        this.sub = sub;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.issuedAt = issuedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPreferredUsername() {
        return this.preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenName() {
        return this.givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public Boolean isEmailVerified() {
        return this.emailVerified;
    }

    public Boolean getEmailVerified() {
        return this.emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getSub() {
        return this.sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getExpiresIn() {
        return this.expiresIn;
    }

    public void setExpiresIn(Instant expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Instant getIssuedAt() {
        return this.issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Keycloak id(Long id) {
        setId(id);
        return this;
    }

    public Keycloak preferredUsername(String preferredUsername) {
        setPreferredUsername(preferredUsername);
        return this;
    }

    public Keycloak email(String email) {
        setEmail(email);
        return this;
    }

    public Keycloak givenName(String givenName) {
        setGivenName(givenName);
        return this;
    }

    public Keycloak familyName(String familyName) {
        setFamilyName(familyName);
        return this;
    }

    public Keycloak emailVerified(Boolean emailVerified) {
        setEmailVerified(emailVerified);
        return this;
    }

    public Keycloak sub(String sub) {
        setSub(sub);
        return this;
    }

    public Keycloak accessToken(String accessToken) {
        setAccessToken(accessToken);
        return this;
    }

    public Keycloak refreshToken(String refreshToken) {
        setRefreshToken(refreshToken);
        return this;
    }

    public Keycloak expiresIn(Instant expiresIn) {
        setExpiresIn(expiresIn);
        return this;
    }

    public Keycloak issuedAt(Instant issuedAt) {
        setIssuedAt(issuedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Keycloak)) {
            return false;
        }
        Keycloak keycloak = (Keycloak) o;
        return Objects.equals(id, keycloak.id) && Objects.equals(preferredUsername, keycloak.preferredUsername)
                && Objects.equals(email, keycloak.email) && Objects.equals(givenName, keycloak.givenName)
                && Objects.equals(familyName, keycloak.familyName)
                && Objects.equals(emailVerified, keycloak.emailVerified) && Objects.equals(sub, keycloak.sub)
                && Objects.equals(accessToken, keycloak.accessToken)
                && Objects.equals(refreshToken, keycloak.refreshToken) && Objects.equals(expiresIn, keycloak.expiresIn)
                && Objects.equals(issuedAt, keycloak.issuedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, preferredUsername, email, givenName, familyName, emailVerified, sub, accessToken,
                refreshToken, expiresIn, issuedAt);
    }

    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", preferredUsername='" + getPreferredUsername() + "'" +
                ", email='" + getEmail() + "'" +
                ", givenName='" + getGivenName() + "'" +
                ", familyName='" + getFamilyName() + "'" +
                ", emailVerified='" + isEmailVerified() + "'" +
                ", sub='" + getSub() + "'" +
                ", accessToken='" + getAccessToken() + "'" +
                ", refreshToken='" + getRefreshToken() + "'" +
                ", expiresIn='" + getExpiresIn() + "'" +
                ", issuedAt='" + getIssuedAt() + "'" +
                "}";
    }

}