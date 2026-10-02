package tw.com.tymbackend.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.TestPropertySource;

/**
 * Properties needed to load the real {@code SecurityConfig} in a {@code @WebMvcTest} slice.
 * Pair with {@code @WebMvcTest(X.class)} and
 * {@code @ContextConfiguration(classes = {X.class, SecurityConfig.class})}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource(properties = {
        "keycloak.auth-server-url=http://localhost:1/auth",
        "keycloak.realm=test",
        "ai-usage.ingest-token=ingest-secret",
        "internal.write-token=" + TestAuth.INTERNAL_TOKEN,
        "project.env=platform",
        "security.disable-all=false"
})
public @interface SecuredWebSlice {
}
