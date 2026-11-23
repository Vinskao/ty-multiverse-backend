package tw.com.tymbackend.module.health;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.palais.PalaisReadGuard;

@WebMvcTest(HealthDataController.class)
@ContextConfiguration(classes = { HealthDataController.class, SecurityConfig.class })
@TestPropertySource(properties = {
        "keycloak.auth-server-url=http://localhost:1/auth", "keycloak.realm=test", "project.env=platform",
        "health-data.ingest-token=ingest-secret"
})
class HealthDataSecurityWebTest {
    private static final String WORKOUT = "[{\"sourceId\":\"" + "a".repeat(64)
            + "\",\"activity\":\"Running\",\"day\":\"2026-11-01\",\"distanceM\":5000,\"durationS\":1500}]";
    private static final String DAILY = "[{\"metric\":\"HeartRate\",\"day\":\"2026-11-01\",\"unit\":\"count/min\",\"average\":60,\"samples\":3}]";

    @Autowired private MockMvc mvc;
    @MockBean private HealthDataStore store;
    @MockBean private PalaisReadGuard guard;

    @Test
    void writesNeedTheIngestToken() throws Exception {
        for (String[] call : new String[][]{{"/health-data/workouts", WORKOUT}, {"/health-data/daily", DAILY}}) {
            mvc.perform(post(call[0]).contentType(MediaType.APPLICATION_JSON).content(call[1])).andExpect(status().isUnauthorized());
            mvc.perform(post(call[0]).header("X-Health-Token", "nope").contentType(MediaType.APPLICATION_JSON).content(call[1]))
                    .andExpect(status().isUnauthorized());
            verifyNoInteractions(store);
        }
        mvc.perform(post("/health-data/workouts").header("X-Health-Token", "ingest-secret")
                .contentType(MediaType.APPLICATION_JSON).content(WORKOUT)).andExpect(status().isOk());
        mvc.perform(post("/health-data/daily").header("X-Health-Token", "ingest-secret")
                .contentType(MediaType.APPLICATION_JSON).content(DAILY)).andExpect(status().isOk());
        verify(store).saveWorkouts(anyList());
        verify(store).saveDaily(anyList());
    }

    @Test
    void rejectsInvalidRows() throws Exception {
        for (String body : List.of(WORKOUT.replace("1500", "0"), WORKOUT.replace("5000", "-1"),
                WORKOUT.replace("2026-11-01", "2999-01-01"), WORKOUT.replace("Running", "x;y"))) {
            mvc.perform(post("/health-data/workouts").header("X-Health-Token", "ingest-secret")
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(store);
    }

    @Test
    void onlyRunningIsPublic() throws Exception {
        mvc.perform(get("/health-data/summary")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.running.weeks.length()").value(12))
                .andExpect(jsonPath("$.data.length()").value(1));
        for (String path : List.of("/health-data/overview", "/health-data/metrics", "/health-data/daily?metric=HeartRate")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).with(user("member"))).andExpect(status().isForbidden());
            mvc.perform(get(path).with(user("checker").roles("palais-checker"))).andExpect(status().isOk());
            mvc.perform(get(path).with(user("owner").roles("manage-users"))).andExpect(status().isOk());
        }
    }

    @Test
    void revokedCheckerIsRefusedEvenWithAnOldToken() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(guard).require(any());
        mvc.perform(get("/health-data/overview").with(user("checker").roles("palais-checker"))).andExpect(status().isForbidden());
        verify(store, never()).overview(any());
    }
}
