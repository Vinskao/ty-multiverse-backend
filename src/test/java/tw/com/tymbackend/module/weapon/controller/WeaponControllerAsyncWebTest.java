package tw.com.tymbackend.module.weapon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.admin;
import static tw.com.tymbackend.support.TestAuth.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.core.service.AsyncMessageService;
import tw.com.tymbackend.module.weapon.service.WeaponService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** RabbitMQ-enabled mode: weapon reads/writes answer 202 and never touch WeaponService. */
@WebMvcTest(WeaponController.class)
@ContextConfiguration(classes = { WeaponController.class, SecurityConfig.class })
@SecuredWebSlice
class WeaponControllerAsyncWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WeaponService service;

    @MockBean
    private AsyncMessageService async;

    @Test
    void getAll_Should_Return202() throws Exception {
        when(async.sendWeaponGetAllRequest()).thenReturn("r1");
        mvc.perform(get("/weapons")).andExpect(status().isAccepted());
        verifyNoInteractions(service);
    }

    @Test
    void getByName_Should_Return202() throws Exception {
        when(async.sendWeaponGetByNameRequest("Sword")).thenReturn("r2");
        mvc.perform(get("/weapons/Sword")).andExpect(status().isAccepted());
        verifyNoInteractions(service);
    }

    @Test
    void getByOwner_Should_Return202() throws Exception {
        when(async.sendWeaponGetByOwnerRequest("Bob")).thenReturn("r3");
        mvc.perform(get("/weapons/owner/Bob")).andExpect(status().isAccepted());
    }

    @Test
    void save_Should_Return202() throws Exception {
        when(async.sendWeaponSaveRequest(any())).thenReturn("r4");
        mvc.perform(post("/weapons").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"owner\":\"Bob\"}"))
                .andExpect(status().isAccepted());
        verifyNoInteractions(service);
    }

    @Test
    void delete_Should_Return202() throws Exception {
        when(async.sendWeaponDeleteRequest("Sword")).thenReturn("r5");
        mvc.perform(delete("/weapons/Sword").with(user())).andExpect(status().isAccepted());
        verifyNoInteractions(service);
    }

    @Test
    void deleteAll_Should_Return202() throws Exception {
        when(async.sendWeaponDeleteAllRequest()).thenReturn("r6");
        mvc.perform(delete("/weapons/delete-all").with(admin())).andExpect(status().isAccepted());
        verifyNoInteractions(service);
    }

    @Test
    void exists_Should_Return202() throws Exception {
        when(async.sendWeaponExistsRequest("Sword")).thenReturn("r7");
        mvc.perform(get("/weapons/exists/Sword")).andExpect(status().isAccepted());
    }
}
