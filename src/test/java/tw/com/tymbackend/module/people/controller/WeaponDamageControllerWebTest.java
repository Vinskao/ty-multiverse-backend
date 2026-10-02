package tw.com.tymbackend.module.people.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.people.domain.dto.BatchDamageResponseDTO;
import tw.com.tymbackend.module.people.service.WeaponDamageService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** Sync mode, both WeaponDamageController endpoints. */
@WebMvcTest(WeaponDamageController.class)
@ContextConfiguration(classes = { WeaponDamageController.class, SecurityConfig.class })
@SecuredWebSlice
class WeaponDamageControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WeaponDamageService service;

    @Test
    void damage_Should_Return200_WithValue() throws Exception {
        when(service.calculateDamageWithWeapon("Bob")).thenReturn(42);
        mvc.perform(get("/people/damageWithWeapon").param("name", "Bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    void damage_Should_Return400_When_CharacterUnknown() throws Exception {
        when(service.calculateDamageWithWeapon("Ghost")).thenReturn(-1);
        mvc.perform(get("/people/damageWithWeapon").param("name", "Ghost"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void damage_Should_Return400_When_NameParamMissing() throws Exception {
        mvc.perform(get("/people/damageWithWeapon")).andExpect(status().isBadRequest());
    }

    @Test
    void damage_Should_Return500_When_ServiceFails() throws Exception {
        when(service.calculateDamageWithWeapon("Bob")).thenThrow(new RuntimeException("boom"));
        mvc.perform(get("/people/damageWithWeapon").param("name", "Bob"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void batchDamage_Should_Return200() throws Exception {
        when(service.calculateBatchDamageWithWeapon(any())).thenReturn(new BatchDamageResponseDTO());
        mvc.perform(post("/people/batchDamageWithWeapon").contentType(MediaType.APPLICATION_JSON)
                .content("{\"names\":[\"Bob\"]}"))
                .andExpect(status().isOk());
    }

    @Test
    void batchDamage_Should_Return400_When_BodyMalformed() throws Exception {
        mvc.perform(post("/people/batchDamageWithWeapon").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }
}
