package tw.com.tymbackend.module.health;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
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
import tw.com.tymbackend.core.config.http.WebConfig;
import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.palais.PalaisReadGuard;

@WebMvcTest(HealthDataController.class)
@ContextConfiguration(classes = { HealthDataController.class, SecurityConfig.class, WebConfig.class })
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
    void shortcutLinesNeedTheTokenAndAreValidated() throws Exception {
        String lines = "StepCount,2026/11/15 00:04,8912 count\nFlightsClimbed,2026/11/15 00:04,12 count";
        mvc.perform(post("/health-data/daily-lines").contentType(MediaType.TEXT_PLAIN).content(lines)).andExpect(status().isUnauthorized());
        mvc.perform(post("/health-data/daily-lines").header("X-Health-Token", "nope").contentType(MediaType.TEXT_PLAIN).content(lines))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(store);
        for (MediaType type : List.of(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED)) {
            mvc.perform(post("/health-data/daily-lines").header("X-Health-Token", "ingest-secret").contentType(type).content(lines))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.saved").value(2));
        }
        // A container that parsed the form already leaves the text as a parameter name.
        mvc.perform(post("/health-data/daily-lines").header("X-Health-Token", "ingest-secret").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("StepCount,2026/11/15 00:04,8,912 count", "")).andExpect(status().isOk()).andExpect(jsonPath("$.data.saved").value(1));
        verify(store, times(3)).saveDaily(argThat(rows -> rows.size() == 2));
        verify(store).saveDaily(argThat(rows -> rows.size() == 1 && rows.get(0).total() == 8912));
        for (String bad : List.of("StepCount,昨天,1 count", "", "StepCount,2026-11-15,1 furlong")) {
            mvc.perform(post("/health-data/daily-lines").header("X-Health-Token", "ingest-secret")
                    .contentType(MediaType.TEXT_PLAIN).content(bad)).andExpect(status().isBadRequest());
        }
        verify(store, times(4)).saveDaily(anyList());
    }

    @Test
    void backendAnswersAllowedTcxPreflightWithoutRequiringAToken() throws Exception {
        mvc.perform(options("/health-data/workout-tcx")
                .header("Origin", "https://tymultiverse.cc")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "X-Health-Token"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Access-Control-Allow-Origin", "https://tymultiverse.cc"));
        verifyNoInteractions(store);
    }

    @Test
    void backendRejectsAnUnapprovedTcxPreflightOrigin() throws Exception {
        mvc.perform(options("/health-data/workout-tcx")
                .header("Origin", "https://unauthorized.example")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "X-Health-Token"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        verifyNoInteractions(store);
    }

    @Test
    void workoutTcxNeedsTheTokenAndKeepsOnlyTheSummary() throws Exception {
        String tcx = "<?xml version=\"1.0\"?><TrainingCenterDatabase xmlns=\"http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2\">"
                + "<Activities><Activity Sport=\"Running\"><Id>2026-11-21T02:00:48.304Z</Id><Lap StartTime=\"2026-11-21T02:00:48.304Z\">"
                + "<TotalTimeSeconds>1800</TotalTimeSeconds><DistanceMeters>5000</DistanceMeters><Calories>300</Calories>"
                + "<Track><Trackpoint><Time>2026-11-21T02:00:48.304Z</Time><DistanceMeters>7</DistanceMeters></Trackpoint></Track>"
                + "</Lap></Activity></Activities></TrainingCenterDatabase>";
        mvc.perform(post("/health-data/workout-tcx").contentType(MediaType.APPLICATION_XML).content(tcx)).andExpect(status().isUnauthorized());
        mvc.perform(post("/health-data/workout-tcx").header("X-Health-Token", "nope").content(tcx)).andExpect(status().isUnauthorized());
        verifyNoInteractions(store);
        mvc.perform(post("/health-data/workout-tcx").header("X-Health-Token", "ingest-secret").contentType(MediaType.APPLICATION_XML).content(tcx))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.saved").value(1))
                .andExpect(jsonPath("$.data.activity").value("Running")).andExpect(jsonPath("$.data.distanceM").value(5000.0));
        verify(store).saveWorkoutsKeepingEnergy(argThat(rows -> rows.size() == 1 && rows.get(0).energyKcal() == null
                && rows.get(0).distanceM() == 5000 && rows.get(0).durationS() == 1800));
        mvc.perform(post("/health-data/workout-tcx").header("X-Health-Token", "ingest-secret").content("not a tcx"))
                .andExpect(status().isBadRequest());
        verify(store, times(1)).saveWorkoutsKeepingEnergy(anyList());
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
