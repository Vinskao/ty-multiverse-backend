package tw.com.tymbackend.core.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static tw.com.tymbackend.support.TestAuth.INTERNAL_HEADER;
import static tw.com.tymbackend.support.TestAuth.INTERNAL_TOKEN;
import static tw.com.tymbackend.support.TestAuth.admin;
import static tw.com.tymbackend.support.TestAuth.user;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import tw.com.tymbackend.core.controller.AuthController;
import tw.com.tymbackend.core.controller.JavaDocController;
import tw.com.tymbackend.core.controller.KeycloakController;
import tw.com.tymbackend.core.service.AuthService;
import tw.com.tymbackend.module.ai_usage.controller.AiTokenUsageController;
import tw.com.tymbackend.module.ai_usage.service.AiTokenUsageService;
import tw.com.tymbackend.module.ckeditor.controller.FileUploadController;
import tw.com.tymbackend.module.ckeditor.service.EditContentService;
import tw.com.tymbackend.module.gallery.controller.GalleryController;
import tw.com.tymbackend.module.gallery.service.GalleryService;
import tw.com.tymbackend.module.learn.controller.LearnController;
import tw.com.tymbackend.module.learn.service.LearnService;
import tw.com.tymbackend.module.people.controller.PeopleController;
import tw.com.tymbackend.module.people.controller.PeopleImageController;
import tw.com.tymbackend.module.people.controller.WeaponDamageController;
import tw.com.tymbackend.module.people.service.PeopleImageService;
import tw.com.tymbackend.module.people.service.PeopleService;
import tw.com.tymbackend.module.people.service.WeaponDamageService;
import tw.com.tymbackend.module.resource.controller.ResourceController;
import tw.com.tymbackend.module.resource.service.CompanyProductMappingService;
import tw.com.tymbackend.module.weapon.controller.WeaponController;
import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.support.SecuredWebSlice;

/**
 * Authorization matrix for every HTTP endpoint, run against the real SecurityConfig and all 12
 * controllers (services mocked, so only the security filter chain and @PreAuthorize decide the status).
 *
 * <p>History: these tests were written red first and exposed that {@code requestMatchers("GET", ...)}
 * treated "GET" as a path (so every method on those paths was permitAll), that {@code @PreAuthorize}
 * was never enforced, and that delete-all rules were shadowed. Fixed in SecurityConfig.
 */
@WebMvcTest({ AuthController.class, JavaDocController.class, KeycloakController.class, AiTokenUsageController.class,
        FileUploadController.class, GalleryController.class, LearnController.class, PeopleController.class,
        PeopleImageController.class, WeaponDamageController.class, ResourceController.class,
        WeaponController.class })
@ContextConfiguration(classes = { AuthController.class, JavaDocController.class, KeycloakController.class,
        AiTokenUsageController.class, FileUploadController.class, GalleryController.class, LearnController.class,
        PeopleController.class, PeopleImageController.class, WeaponDamageController.class, ResourceController.class,
        WeaponController.class, SecurityConfig.class })
@SecuredWebSlice
@TestPropertySource(properties = {
        "url.frontend=http://front.test",
        "app.url.address=http://back.test",
        "keycloak.clientId=client",
        "keycloak.credentials.secret=shh"
})
class SecurityRulesWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean AuthService authService;
    @MockBean RestTemplate restTemplate;
    @MockBean AiTokenUsageService aiTokenUsageService;
    @MockBean EditContentService editContentService;
    @MockBean GalleryService galleryService;
    @MockBean LearnService learnService;
    @MockBean PeopleService peopleService;
    @MockBean PeopleImageService peopleImageService;
    @MockBean WeaponDamageService weaponDamageService;
    @MockBean CompanyProductMappingService companyProductMappingService;
    @MockBean WeaponService weaponService;

    @org.junit.jupiter.api.BeforeEach
    void stubLookups() {
        // PUT /weapons/{name}/attributes looks the weapon up first; without this the controller throws
        // (that separate bug is covered in WeaponControllerWebTest) and masks the security status.
        org.mockito.Mockito.when(weaponService.getWeaponById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.Optional.of(new tw.com.tymbackend.module.weapon.domain.vo.Weapon()));
    }

    private static final String JSON = "{\"name\":\"x\",\"codeName\":\"x\",\"id\":1,\"owner\":\"x\"}";
    private static final String JSON_LIST = "[" + JSON + "]";

    private static Arguments ep(String method, String path) {
        return Arguments.of(method, path, method.equals("GET") || method.equals("DELETE") ? null : JSON);
    }

    private int call(String method, String path, String body, org.springframework.test.web.servlet.request.RequestPostProcessor auth,
            String internalToken) throws Exception {
        var req = request(HttpMethod.valueOf(method), path);
        if (body != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(path.contains("insert-multiple") ? JSON_LIST : body);
        }
        if (auth != null) {
            req.with(auth);
        }
        if (internalToken != null) {
            req.header(INTERNAL_HEADER, internalToken);
        }
        return mvc.perform(req).andReturn().getResponse().getStatus();
    }

    /** Mutating endpoints: anonymous callers must be rejected with 401. */
    static Stream<Arguments> writeEndpoints() {
        return Stream.of(
                ep("POST", "/people/insert"), ep("POST", "/people/update"), ep("POST", "/people/insert-multiple"),
                ep("POST", "/people/delete-all"),
                ep("POST", "/weapons"), ep("POST", "/weapons/insert-multiple"),
                ep("DELETE", "/weapons/Sword"), ep("DELETE", "/weapons/delete-all"),
                ep("PUT", "/weapons/Sword/attributes"), ep("PUT", "/weapons/Sword/base-damage"),
                ep("PUT", "/weapons/Sword/bonus-damage"), ep("PUT", "/weapons/Sword/bonus-attributes"),
                ep("PUT", "/weapons/Sword/state-attributes"),
                ep("POST", "/gallery/save"), ep("POST", "/gallery/update"), ep("POST", "/gallery/delete"),
                ep("POST", "/people-images"), ep("PUT", "/people-images/bob"), ep("DELETE", "/people-images/bob"));
    }

    /**
     * Regression guard: in Spring Security 6 requestMatchers("GET", "/people/**") treats "GET" as a
     * path, making the rule match every method. SecurityConfig must use requestMatchers(HttpMethod.GET, ...).
     */
    @ParameterizedTest(name = "anonymous {0} {1} -> 401")
    @MethodSource("writeEndpoints")
    void writeEndpoint_Should_Return401_When_Anonymous(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, null, null)).isEqualTo(401);
    }

    @ParameterizedTest(name = "wrong internal token {0} {1} -> 401")
    @MethodSource("writeEndpoints")
    void writeEndpoint_Should_Return401_When_InternalTokenWrong(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, null, "wrong")).isEqualTo(401);
    }

    @ParameterizedTest(name = "jwt user {0} {1} -> not 401/403")
    @MethodSource("writeEndpoints")
    void writeEndpoint_Should_BeAllowed_When_AuthenticatedAdmin(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, admin(), null)).isNotIn(401, 403);
    }

    @ParameterizedTest(name = "internal token {0} {1} -> not 401/403")
    @MethodSource("writeEndpoints")
    void writeEndpoint_Should_BeAllowed_When_InternalTokenValid(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, null, INTERNAL_TOKEN)).isNotIn(401, 403);
    }

    /** Destructive / admin-only endpoints: a logged-in non-admin must get 403. */
    static Stream<Arguments> adminOnlyEndpoints() {
        return Stream.of(
                ep("POST", "/people/delete-all"), ep("DELETE", "/weapons/delete-all"),
                ep("GET", "/people-images"), ep("POST", "/people-images"),
                ep("PUT", "/people-images/bob"), ep("DELETE", "/people-images/bob"),
                ep("GET", "/auth/admin"));
    }

    /**
     * Regression guard: (a) hasRole delete-all rules must precede the generic DELETE /x/** rules (first
     * match wins); (b) @PreAuthorize on AuthController/PeopleImageController needs @EnableMethodSecurity.
     */
    @ParameterizedTest(name = "plain user {0} {1} -> 403")
    @MethodSource("adminOnlyEndpoints")
    void adminEndpoint_Should_Return403_When_PlainUser(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, user(), null)).isEqualTo(403);
    }

    @ParameterizedTest(name = "admin {0} {1} -> not 401/403")
    @MethodSource("adminOnlyEndpoints")
    void adminEndpoint_Should_BeAllowed_When_Admin(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, admin(), null)).isNotIn(401, 403);
    }

    /** Endpoints that must work without credentials. */
    static Stream<Arguments> publicEndpoints() {
        return Stream.of(
                ep("GET", "/auth/visitor"), ep("GET", "/auth/health"),
                ep("GET", "/people/names"), ep("POST", "/people/get-all"), ep("POST", "/people/get-by-name"),
                ep("GET", "/people/damageWithWeapon?name=x"), ep("POST", "/people/batchDamageWithWeapon"),
                ep("GET", "/weapons"), ep("GET", "/weapons/Sword"), ep("GET", "/weapons/owner/Bob"),
                ep("GET", "/weapons/exists/Sword"), ep("GET", "/weapons/damage-range?minDamage=1&maxDamage=2"),
                ep("GET", "/weapons/attribute/fire"),
                ep("POST", "/gallery/getAll"), ep("POST", "/gallery/getById"),
                ep("POST", "/ckeditor/get-content"),
                ep("GET", "/resources/company-product-mapping"),
                ep("GET", "/ai-usage/daily"), ep("GET", "/ai-usage/monthly"), ep("GET", "/ai-usage/summary"),
                ep("GET", "/ai-usage/overview"),
                ep("GET", "/keycloak/redirect?code=x"));
    }

    @ParameterizedTest(name = "anonymous {0} {1} -> not 401/403")
    @MethodSource("publicEndpoints")
    void publicEndpoint_Should_NotRequireCredentials(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, null, null)).isNotIn(401, 403);
    }

    /** Endpoints that need a valid login but no particular role (rules that DO work today). */
    static Stream<Arguments> authenticatedOnlyEndpoints() {
        return Stream.of(
                ep("GET", "/auth/user"), ep("GET", "/auth/token-info"), ep("GET", "/auth/test-default"),
                ep("POST", "/auth/test"), ep("POST", "/auth/logout-test?refreshToken=x"),
                ep("GET", "/docs"), ep("GET", "/docs/api"),
                ep("GET", "/learn/topics"), ep("GET", "/learn/quizzes"),
                ep("POST", "/learn/topics/q/session"), ep("POST", "/learn/topics/q/session/answers"),
                ep("POST", "/learn/topics/q/attempts"), ep("GET", "/learn/attempts/1/review"),
                ep("GET", "/learn/topics/q/scorecard"), ep("GET", "/learn/attempts"), ep("GET", "/learn/profile"),
                ep("GET", "/learn/topics/q/ranking"), ep("GET", "/learn/mentor/overview"),
                ep("POST", "/ckeditor/save-content"));
    }

    @ParameterizedTest(name = "anonymous {0} {1} -> 401")
    @MethodSource("authenticatedOnlyEndpoints")
    void loginRequiredEndpoint_Should_Return401_When_Anonymous(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, null, null)).isEqualTo(401);
    }

    @ParameterizedTest(name = "plain user {0} {1} -> not 401/403")
    @MethodSource("authenticatedOnlyEndpoints")
    void loginRequiredEndpoint_Should_BeAllowed_When_AnyAuthenticatedUser(String method, String path, String body) throws Exception {
        assertThat(call(method, path, body, user(), null)).isNotIn(401, 403);
    }
}
