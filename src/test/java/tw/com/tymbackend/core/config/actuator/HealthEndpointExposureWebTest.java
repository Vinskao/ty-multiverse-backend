package tw.com.tymbackend.core.config.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.admin;
import static tw.com.tymbackend.support.TestAuth.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.support.SecuredWebSlice;

/**
 * /actuator/health is public (the k8s probes and the ingress use it) but must not reveal infrastructure
 * details (DB/Redis/RabbitMQ versions, disk paths) to anonymous callers. Uses the real application.yml.
 */
@WebMvcTest
@ContextConfiguration(classes = { SecurityConfig.class, HealthEndpointExposureWebTest.FakeInfra.class })
@ImportAutoConfiguration({ EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
        HealthContributorAutoConfiguration.class, HealthEndpointAutoConfiguration.class,
        ManagementContextAutoConfiguration.class, ServletManagementContextAutoConfiguration.class,
        org.springframework.boot.autoconfigure.availability.ApplicationAvailabilityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.availability.AvailabilityHealthContributorAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.availability.AvailabilityProbesAutoConfiguration.class })
@SecuredWebSlice
// production profile: application-local.yml (dev: show-details always) must not mask the real setting
@ActiveProfiles("platform")
class HealthEndpointExposureWebTest {

    @Configuration
    static class FakeInfra {
        @Bean
        HealthIndicator infraProbe() {
            return () -> Health.up().withDetail("version", "7.2.10").build();
        }
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void anonymous_Should_GetStatusOnly() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void plainUser_Should_GetStatusOnly() throws Exception {
        mvc.perform(get("/actuator/health").with(user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void admin_Should_SeeComponentDetails() throws Exception {
        mvc.perform(get("/actuator/health").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.infraProbe.details.version").value("7.2.10"));
    }

    @Test
    void probeGroups_Should_StayPublic() throws Exception {
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
    }
}
