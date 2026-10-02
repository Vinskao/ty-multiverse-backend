package tw.com.tymbackend.module.people.controller;

import static org.mockito.ArgumentMatchers.any;
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

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;
import tw.com.tymbackend.module.people.service.PeopleImageService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** All 5 PeopleImageController endpoints, called as an admin. Role checks live in SecurityRulesWebTest. */
@WebMvcTest(PeopleImageController.class)
@ContextConfiguration(classes = { PeopleImageController.class, SecurityConfig.class })
@SecuredWebSlice
class PeopleImageControllerWebTest {

    private static final String IMAGE = "{\"codeName\":\"bob\",\"image\":\"AAAA\"}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PeopleImageService service;

    private static PeopleImage image(String codeName) {
        PeopleImage p = new PeopleImage();
        p.setCodeName(codeName);
        p.setImage("AAAA");
        return p;
    }

    @Test
    void getAll_Should_Return200() throws Exception {
        when(service.getAllPeopleImages()).thenReturn(List.of(image("bob")));
        mvc.perform(get("/people-images").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].codeName").value("bob"));
    }

    @Test
    void getByCodeName_Should_Return200() throws Exception {
        when(service.getPeopleImageByCodeName("bob")).thenReturn(image("bob"));
        mvc.perform(get("/people-images/bob").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeName").value("bob"));
    }

    @Test
    void getByCodeName_Should_Return404_When_Missing() throws Exception {
        when(service.getPeopleImageByCodeName("x")).thenThrow(new NoSuchElementException("none"));
        mvc.perform(get("/people-images/x").with(admin())).andExpect(status().isNotFound());
    }

    @Test
    void create_Should_Return201() throws Exception {
        when(service.peopleImageExists("bob")).thenReturn(false);
        when(service.savePeopleImage(any())).thenReturn(image("bob"));
        mvc.perform(post("/people-images").with(admin()).contentType(MediaType.APPLICATION_JSON).content(IMAGE))
                .andExpect(status().isCreated());
    }

    @Test
    void create_Should_Return409_When_AlreadyExists() throws Exception {
        when(service.peopleImageExists("bob")).thenReturn(true);
        mvc.perform(post("/people-images").with(admin()).contentType(MediaType.APPLICATION_JSON).content(IMAGE))
                .andExpect(status().isConflict());
        verify(service, never()).savePeopleImage(any());
    }

    @Test
    void create_Should_Return500_When_ServiceFails() throws Exception {
        when(service.peopleImageExists("bob")).thenReturn(false);
        when(service.savePeopleImage(any())).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/people-images").with(admin()).contentType(MediaType.APPLICATION_JSON).content(IMAGE))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void update_Should_Return200_And_ForceCodeNameFromPath() throws Exception {
        when(service.peopleImageExists("bob")).thenReturn(true);
        when(service.savePeopleImage(any())).thenAnswer(i -> i.getArgument(0));
        mvc.perform(put("/people-images/bob").with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeName\":\"hacker\",\"image\":\"BBBB\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeName").value("bob"));
    }

    @Test
    void update_Should_Return404_When_Missing() throws Exception {
        when(service.peopleImageExists("x")).thenReturn(false);
        mvc.perform(put("/people-images/x").with(admin()).contentType(MediaType.APPLICATION_JSON).content(IMAGE))
                .andExpect(status().isNotFound());
        verify(service, never()).savePeopleImage(any());
    }

    @Test
    void delete_Should_Return200() throws Exception {
        mvc.perform(delete("/people-images/bob").with(admin())).andExpect(status().isOk());
        verify(service).deletePeopleImage("bob");
    }

    @Test
    void delete_Should_Return404_When_Missing() throws Exception {
        doThrow(new NoSuchElementException("none")).when(service).deletePeopleImage("x");
        mvc.perform(delete("/people-images/x").with(admin())).andExpect(status().isNotFound());
    }

    @Test
    void delete_Should_Return500_When_ServiceFails() throws Exception {
        doThrow(new RuntimeException("boom")).when(service).deletePeopleImage("x");
        mvc.perform(delete("/people-images/x").with(admin())).andExpect(status().isInternalServerError());
    }
}
