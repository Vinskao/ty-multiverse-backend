package tw.com.tymbackend.core.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.user;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.ty.common.exception.web.CommonExceptionAutoConfiguration;
import tw.com.ty.common.response.ErrorCode;
import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.people.controller.PeopleImageController;
import tw.com.tymbackend.module.people.service.PeopleImageService;
import tw.com.tymbackend.module.weapon.controller.WeaponController;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.support.SecuredWebSlice;

/**
 * The shared error handling from ty-multiverse-common (CommonExceptionAutoConfiguration) applied to the
 * real controllers and SecurityConfig: every uncaught exception becomes a BackendApiResponse with the
 * right HTTP status, and method-security denials stay 403 (not 500).
 */
@WebMvcTest({ WeaponController.class, PeopleImageController.class })
@ContextConfiguration(classes = { WeaponController.class, PeopleImageController.class, SecurityConfig.class })
@ImportAutoConfiguration(CommonExceptionAutoConfiguration.class)
@SecuredWebSlice
class ErrorHandlingWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WeaponService weaponService;

    @MockBean
    private PeopleImageService peopleImageService;

    @Test
    void commonErrorHandling_Should_BeRegisteredAsSpringBootAutoConfiguration() {
        assertThat(ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader()).getCandidates())
                .contains(CommonExceptionAutoConfiguration.class.getName());
    }

    @Test
    void uncaughtBusinessException_Should_MapToItsErrorCodeStatus() throws Exception {
        when(weaponService.getWeaponById("Nope")).thenReturn(Optional.empty());

        mvc.perform(put("/weapons/Nope/attributes").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"attributes\":\"fire\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.WEAPON_NOT_FOUND.getCode()));
    }

    @Test
    void malformedJson_Should_Return400() throws Exception {
        mvc.perform(post("/weapons").with(user()).contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void missingRequiredParam_Should_Return400() throws Exception {
        mvc.perform(get("/weapons/damage-range").param("minDamage", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void methodSecurityDenial_Should_Return403_NotAdviceInducedFiveHundred() throws Exception {
        mvc.perform(get("/people-images").with(user()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void unexpectedException_Should_Return500WithBackendApiResponseBody() throws Exception {
        when(weaponService.updateWeaponBaseDamage("Sword", 5)).thenThrow(new IllegalStateException("kaboom"));

        mvc.perform(put("/weapons/Sword/base-damage").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"baseDamage\":5}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }
}
