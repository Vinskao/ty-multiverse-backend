package tw.com.tymbackend.module.weapon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.admin;
import static tw.com.tymbackend.support.TestAuth.user;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;
import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.support.SecuredWebSlice;

/**
 * Sync mode (no AsyncMessageService bean), all 15 endpoints. Requests carry a JWT so these tests are
 * about controller behavior; the auth matrix lives in SecurityRulesWebTest.
 */
@WebMvcTest(WeaponController.class)
@ContextConfiguration(classes = { WeaponController.class, SecurityConfig.class })
@SecuredWebSlice
class WeaponControllerWebTest {

    private static final String WEAPON_JSON = "{\"name\":\"Sword\",\"owner\":\"Bob\",\"baseDamage\":10}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WeaponService service;

    private static Weapon weapon(String name) {
        Weapon w = new Weapon();
        w.setName(name);
        w.setOwner("Bob");
        w.setBaseDamage(10);
        return w;
    }

    // ---- GET /weapons -------------------------------------------------

    @Test
    void getAll_Should_BePublicAndReturnList() throws Exception {
        when(service.getAllWeapons()).thenReturn(List.of(weapon("Sword")));
        mvc.perform(get("/weapons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].weapon").value("Sword"));
    }

    @Test
    void getAll_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getAllWeapons()).thenThrow(new RuntimeException("db down"));
        mvc.perform(get("/weapons")).andExpect(status().isInternalServerError());
    }

    // ---- GET /weapons/{name} ------------------------------------------

    @Test
    void getByName_Should_Return200_When_Found() throws Exception {
        when(service.getWeaponById("Sword")).thenReturn(Optional.of(weapon("Sword")));
        mvc.perform(get("/weapons/Sword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weapon").value("Sword"));
    }

    @Test
    void getByName_Should_Return404_When_Missing() throws Exception {
        when(service.getWeaponById("Nope")).thenReturn(Optional.empty());
        mvc.perform(get("/weapons/Nope")).andExpect(status().isNotFound());
    }

    @Test
    void getByName_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getWeaponById("x")).thenThrow(new RuntimeException("boom"));
        mvc.perform(get("/weapons/x")).andExpect(status().isInternalServerError());
    }

    // ---- GET /weapons/owner/{owner} -----------------------------------

    @Test
    void getByOwner_Should_Return200() throws Exception {
        when(service.getWeaponsByOwner("Bob")).thenReturn(List.of(weapon("Sword")));
        mvc.perform(get("/weapons/owner/Bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].owner").value("Bob"));
    }

    @Test
    void getByOwner_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getWeaponsByOwner("Bob")).thenThrow(new RuntimeException("boom"));
        mvc.perform(get("/weapons/owner/Bob")).andExpect(status().isInternalServerError());
    }

    // ---- POST /weapons ------------------------------------------------

    @Test
    void save_Should_Return200_When_JwtUser() throws Exception {
        when(service.saveWeaponSmart(any())).thenReturn(weapon("Sword"));
        mvc.perform(post("/weapons").with(user()).contentType(MediaType.APPLICATION_JSON).content(WEAPON_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weapon").value("Sword"));
    }

    @Test
    void save_Should_Return500_When_ServiceFails() throws Exception {
        when(service.saveWeaponSmart(any())).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/weapons").with(user()).contentType(MediaType.APPLICATION_JSON).content(WEAPON_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void save_Should_Return400_When_BodyMalformed() throws Exception {
        mvc.perform(post("/weapons").with(user()).contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest());
    }

    // ---- POST /weapons/insert-multiple --------------------------------

    @Test
    void insertMultiple_Should_Return201_When_Valid() throws Exception {
        when(service.saveAllWeapons(any())).thenReturn(List.of(weapon("Sword")));
        mvc.perform(post("/weapons/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("[" + WEAPON_JSON + "]"))
                .andExpect(status().isCreated());
    }

    @Test
    void insertMultiple_Should_Return400_When_ListEmpty() throws Exception {
        mvc.perform(post("/weapons/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isBadRequest());
        verify(service, never()).saveAllWeapons(any());
    }

    @Test
    void insertMultiple_Should_Return400_When_ServiceRejectsInput() throws Exception {
        when(service.saveAllWeapons(any())).thenThrow(new IllegalArgumentException("dup"));
        mvc.perform(post("/weapons/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("[" + WEAPON_JSON + "]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void insertMultiple_Should_Return500_When_ServiceFails() throws Exception {
        when(service.saveAllWeapons(any())).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/weapons/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("[" + WEAPON_JSON + "]"))
                .andExpect(status().isInternalServerError());
    }

    // ---- DELETE /weapons/{name} ---------------------------------------

    @Test
    void delete_Should_Return204_When_JwtUser() throws Exception {
        mvc.perform(delete("/weapons/Sword").with(user())).andExpect(status().isNoContent());
        verify(service).deleteWeapon("Sword");
    }

    @Test
    void delete_Should_Return500_When_ServiceFails() throws Exception {
        doThrow(new RuntimeException("boom")).when(service).deleteWeapon("Sword");
        mvc.perform(delete("/weapons/Sword").with(user())).andExpect(status().isInternalServerError());
    }

    // ---- DELETE /weapons/delete-all -----------------------------------

    @Test
    void deleteAll_Should_Return204_When_Admin() throws Exception {
        mvc.perform(delete("/weapons/delete-all").with(admin())).andExpect(status().isNoContent());
        verify(service).deleteAllWeapons();
    }

    // ---- GET /weapons/exists/{name} -----------------------------------

    @Test
    void exists_Should_ReturnFlag() throws Exception {
        when(service.weaponExists("Sword")).thenReturn(true);
        mvc.perform(get("/weapons/exists/Sword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exists").value(true));
    }

    @Test
    void exists_Should_Return500_When_ServiceFails() throws Exception {
        when(service.weaponExists("x")).thenThrow(new RuntimeException("boom"));
        mvc.perform(get("/weapons/exists/x")).andExpect(status().isInternalServerError());
    }

    // ---- PUT /weapons/{name}/... --------------------------------------

    @Test
    void updateAttributes_Should_Return200_When_Found() throws Exception {
        when(service.getWeaponById("Sword")).thenReturn(Optional.of(weapon("Sword")));
        when(service.updateWeaponAttributes(eq("Sword"), any())).thenReturn(weapon("Sword"));
        mvc.perform(put("/weapons/Sword/attributes").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"attributes\":\"fire\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weapon").value("Sword"));
    }

    /** KNOWN BUG (red): unknown weapon throws a bare RuntimeException (no advice) instead of 404. */
    @Test
    @org.junit.jupiter.api.Tag("known-bug")
    void updateAttributes_Should_Return404_When_WeaponMissing() throws Exception {
        when(service.getWeaponById("Nope")).thenReturn(Optional.empty());
        mvc.perform(put("/weapons/Nope/attributes").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"attributes\":\"fire\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBaseDamage_Should_Return200() throws Exception {
        when(service.updateWeaponBaseDamage("Sword", 5)).thenReturn(weapon("Sword"));
        mvc.perform(put("/weapons/Sword/base-damage").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"baseDamage\":5}"))
                .andExpect(status().isOk());
        verify(service).updateWeaponBaseDamage("Sword", 5);
    }

    @Test
    void updateBonusDamage_Should_Return200() throws Exception {
        when(service.updateWeaponBonusDamage("Sword", 7)).thenReturn(weapon("Sword"));
        mvc.perform(put("/weapons/Sword/bonus-damage").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"bonusDamage\":7}"))
                .andExpect(status().isOk());
        verify(service).updateWeaponBonusDamage("Sword", 7);
    }

    @Test
    void updateBonusAttributes_Should_Return200() throws Exception {
        when(service.updateWeaponBonusAttributes("Sword", List.of("a"))).thenReturn(weapon("Sword"));
        mvc.perform(put("/weapons/Sword/bonus-attributes").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"bonusAttributes\":[\"a\"]}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStateAttributes_Should_Return200() throws Exception {
        when(service.updateWeaponStateAttributes("Sword", List.of("s"))).thenReturn(weapon("Sword"));
        mvc.perform(put("/weapons/Sword/state-attributes").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"stateAttributes\":[\"s\"]}"))
                .andExpect(status().isOk());
    }

    // ---- GET damage-range / attribute ---------------------------------

    @Test
    void damageRange_Should_ReturnMatches() throws Exception {
        when(service.findByBaseDamageRange(5, 20)).thenReturn(List.of(weapon("Sword")));
        mvc.perform(get("/weapons/damage-range").param("minDamage", "5").param("maxDamage", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weapon").value("Sword"));
    }

    @Test
    void damageRange_Should_Return400_When_ParamMissing() throws Exception {
        mvc.perform(get("/weapons/damage-range").param("minDamage", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByAttribute_Should_ReturnMatches() throws Exception {
        when(service.findByAttribute("fire")).thenReturn(List.of(weapon("Sword")));
        mvc.perform(get("/weapons/attribute/fire"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].weapon").value("Sword"));
    }
}
