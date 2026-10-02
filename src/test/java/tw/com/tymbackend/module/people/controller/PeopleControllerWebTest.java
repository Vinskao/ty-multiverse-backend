package tw.com.tymbackend.module.people.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.PeopleService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** Sync mode, all 7 PeopleController endpoints. Auth matrix lives in SecurityRulesWebTest. */
@WebMvcTest(PeopleController.class)
@ContextConfiguration(classes = { PeopleController.class, SecurityConfig.class })
@SecuredWebSlice
class PeopleControllerWebTest {

    private static final String PERSON = "{\"name\":\"Bob\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PeopleService service;

    private static People person() {
        People p = new People();
        p.setName("Bob");
        return p;
    }

    // ---- POST /people/insert ------------------------------------------

    @Test
    void insert_Should_Return201_When_Valid() throws Exception {
        when(service.insertPerson(any())).thenReturn(person());
        mvc.perform(post("/people/insert").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void insert_Should_Return400_When_NameBlank() throws Exception {
        mvc.perform(post("/people/insert").with(user()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
        verify(service, never()).insertPerson(any());
    }

    @Test
    void insert_Should_Return400_When_NameMissing() throws Exception {
        mvc.perform(post("/people/insert").with(user()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void insert_Should_Return400_When_ServiceRejectsInput() throws Exception {
        when(service.insertPerson(any())).thenThrow(new IllegalArgumentException("bad"));
        mvc.perform(post("/people/insert").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void insert_Should_Return500_When_ServiceFails() throws Exception {
        when(service.insertPerson(any())).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/people/insert").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isInternalServerError());
    }

    // ---- POST /people/update ------------------------------------------

    @Test
    void update_Should_Return200_When_Valid() throws Exception {
        when(service.updatePerson(any())).thenReturn(person());
        mvc.perform(post("/people/update").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isOk());
    }

    @Test
    void update_Should_Return400_When_NameBlank() throws Exception {
        mvc.perform(post("/people/update").with(user()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_Should_Return404_When_PersonNotFound() throws Exception {
        when(service.updatePerson(any())).thenThrow(new IllegalArgumentException("missing"));
        mvc.perform(post("/people/update").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_Should_Return409_When_OptimisticLockFails() throws Exception {
        when(service.updatePerson(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(People.class, "Bob"));
        mvc.perform(post("/people/update").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isConflict());
    }

    @Test
    void update_Should_Return400_When_DataIntegrityViolation() throws Exception {
        when(service.updatePerson(any())).thenThrow(new DataIntegrityViolationException("dup"));
        mvc.perform(post("/people/update").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_Should_Return500_When_ServiceFails() throws Exception {
        when(service.updatePerson(any())).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/people/update").with(user()).contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isInternalServerError());
    }

    // ---- POST /people/insert-multiple ---------------------------------

    @Test
    void insertMultiple_Should_Return201_When_Valid() throws Exception {
        when(service.saveAllPeople(any())).thenReturn(List.of(person()));
        mvc.perform(post("/people/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("[" + PERSON + "]"))
                .andExpect(status().isCreated());
    }

    @Test
    void insertMultiple_Should_Return400_When_ListEmpty() throws Exception {
        mvc.perform(post("/people/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isBadRequest());
        verify(service, never()).saveAllPeople(any());
    }

    @Test
    void insertMultiple_Should_Return400_When_ServiceRejectsInput() throws Exception {
        when(service.saveAllPeople(any())).thenThrow(new IllegalArgumentException("bad"));
        mvc.perform(post("/people/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("[" + PERSON + "]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void insertMultiple_Should_Return500_When_ServiceFails() throws Exception {
        when(service.saveAllPeople(any())).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/people/insert-multiple").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("[" + PERSON + "]"))
                .andExpect(status().isInternalServerError());
    }

    // ---- POST /people/get-all -----------------------------------------

    @Test
    void getAll_Should_Return200() throws Exception {
        when(service.getAllPeopleOptimized()).thenReturn(List.of(person()));
        mvc.perform(post("/people/get-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getAll_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getAllPeopleOptimized()).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/people/get-all")).andExpect(status().isInternalServerError());
    }

    // ---- POST /people/get-by-name -------------------------------------

    @Test
    void getByName_Should_Return200_When_Found() throws Exception {
        when(service.getPeopleByName("Bob")).thenReturn(Optional.of(person()));
        mvc.perform(post("/people/get-by-name").contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isOk());
    }

    @Test
    void getByName_Should_Return404_When_Missing() throws Exception {
        when(service.getPeopleByName("Bob")).thenReturn(Optional.empty());
        mvc.perform(post("/people/get-by-name").contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByName_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getPeopleByName("Bob")).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/people/get-by-name").contentType(MediaType.APPLICATION_JSON).content(PERSON))
                .andExpect(status().isInternalServerError());
    }

    // ---- POST /people/delete-all --------------------------------------

    @Test
    void deleteAll_Should_Return204() throws Exception {
        mvc.perform(post("/people/delete-all").with(admin())).andExpect(status().isNoContent());
        verify(service).deleteAllPeople();
    }

    @Test
    void deleteAll_Should_Return500_When_ServiceFails() throws Exception {
        doThrow(new RuntimeException("boom")).when(service).deleteAllPeople();
        mvc.perform(post("/people/delete-all").with(admin())).andExpect(status().isInternalServerError());
    }

    // ---- GET /people/names --------------------------------------------

    @Test
    void names_Should_Return200() throws Exception {
        when(service.getAllPeopleNames()).thenReturn(List.of("Bob", "Amy"));
        mvc.perform(get("/people/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("Bob"));
    }

    @Test
    void names_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getAllPeopleNames()).thenThrow(new RuntimeException("boom"));
        mvc.perform(get("/people/names")).andExpect(status().isInternalServerError());
    }
}
