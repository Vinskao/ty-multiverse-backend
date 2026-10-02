package tw.com.tymbackend.module.people.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.admin;
import static tw.com.tymbackend.support.TestAuth.user;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.core.service.AsyncMessageService;
import tw.com.tymbackend.module.people.service.PeopleService;
import tw.com.tymbackend.module.people.service.WeaponDamageService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** RabbitMQ-enabled mode: People + WeaponDamage endpoints answer 202 and delegate to AsyncMessageService. */
@WebMvcTest({ PeopleController.class, WeaponDamageController.class })
@ContextConfiguration(classes = { PeopleController.class, WeaponDamageController.class, SecurityConfig.class })
@SecuredWebSlice
class PeopleControllerAsyncWebTest {

    private static final String PERSON = "{\"name\":\"Bob\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PeopleService service;

    @MockBean
    private WeaponDamageService damageService;

    @MockBean
    private AsyncMessageService async;

    @Test
    void insert_Should_Return202_And_NotTouchService() throws Exception {
        when(async.sendPeopleInsertRequest(any())).thenReturn("req-1");
        mvc.perform(post("/people/insert").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isAccepted());
        verifyNoInteractions(service);
    }

    @Test
    void insert_Should_FallBackToSync_When_QueueFails() throws Exception {
        when(async.sendPeopleInsertRequest(any())).thenThrow(new RuntimeException("mq down"));
        when(service.insertPerson(any())).thenReturn(new tw.com.tymbackend.module.people.domain.vo.People());
        mvc.perform(post("/people/insert").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isCreated());
    }

    @Test
    void insert_Should_Return400_BeforeQueueing_When_NameBlank() throws Exception {
        mvc.perform(post("/people/insert").with(user()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(async, never()).sendPeopleInsertRequest(any());
    }

    @Test
    void update_Should_Return202() throws Exception {
        when(async.sendPeopleUpdateRequest(any())).thenReturn("req-2");
        mvc.perform(post("/people/update").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isAccepted());
    }

    @Test
    void insertMultiple_Should_Return202() throws Exception {
        when(async.sendPeopleInsertMultipleRequest(any())).thenReturn("req-3");
        mvc.perform(post("/people/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("[" + PERSON + "]"))
                .andExpect(status().isAccepted());
    }

    @Test
    void getAll_Should_Return202() throws Exception {
        when(async.sendPeopleGetAllRequest()).thenReturn("req-4");
        mvc.perform(post("/people/get-all")).andExpect(status().isAccepted());
        verifyNoInteractions(service);
    }

    @Test
    void getByName_Should_Return202() throws Exception {
        when(async.sendPeopleGetByNameRequest("Bob")).thenReturn("req-5");
        mvc.perform(post("/people/get-by-name").contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isAccepted());
    }

    @Test
    void deleteAll_Should_Return202() throws Exception {
        when(async.sendPeopleDeleteAllRequest()).thenReturn("req-6");
        mvc.perform(post("/people/delete-all").with(admin())).andExpect(status().isAccepted());
        verifyNoInteractions(service);
    }

    @Test
    void names_Should_Return202() throws Exception {
        when(async.sendPeopleGetNamesRequest()).thenReturn("req-7");
        mvc.perform(get("/people/names")).andExpect(status().isAccepted());
    }

    @Test
    void names_Should_Return500_When_QueueFails() throws Exception {
        when(async.sendPeopleGetNamesRequest()).thenThrow(new RuntimeException("mq down"));
        mvc.perform(get("/people/names")).andExpect(status().isInternalServerError());
    }

    @Test
    void batchDamage_Should_Return202() throws Exception {
        when(async.sendPeopleBatchDamageRequest(List.of("Bob"))).thenReturn("req-8");
        mvc.perform(post("/people/batchDamageWithWeapon").contentType(MediaType.APPLICATION_JSON)
                .content("{\"names\":[\"Bob\"]}"))
                .andExpect(status().isAccepted());
        verifyNoInteractions(damageService);
    }

    @Test
    void batchDamage_Should_FallBackToSync_When_QueueFails() throws Exception {
        when(async.sendPeopleBatchDamageRequest(any())).thenThrow(new RuntimeException("mq down"));
        mvc.perform(post("/people/batchDamageWithWeapon").contentType(MediaType.APPLICATION_JSON)
                .content("{\"names\":[\"Bob\"]}"))
                .andExpect(status().isOk());
        verify(damageService).calculateBatchDamageWithWeapon(any());
    }
}
