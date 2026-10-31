package tw.com.tymbackend.core.config.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/** 2026-10-31: the site moved to https://tymultiverse.cc; the old host only redirects pages now. */
class WebConfigCorsTest {

    private static CorsConfiguration corsForAllPaths() {
        var registry = new CorsRegistry() {
            Map<String, CorsConfiguration> configs() {
                return getCorsConfigurations();
            }
        };
        new WebConfig().addCorsMappings(registry);
        return registry.configs().get("/**");
    }

    @Test
    void allowsTheSiteOrigin() {
        assertEquals("https://tymultiverse.cc", corsForAllPaths().checkOrigin("https://tymultiverse.cc"));
    }

    @Test
    void rejectsTheRetiredHostAndUnrelatedSites() {
        CorsConfiguration cors = corsForAllPaths();
        assertNull(cors.checkOrigin("https://peoplesystem.tatdvsonorth.com"));
        assertNull(cors.checkOrigin("https://evil.example"));
    }
}
