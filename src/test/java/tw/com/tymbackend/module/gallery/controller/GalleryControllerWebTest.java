package tw.com.tymbackend.module.gallery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;
import tw.com.tymbackend.module.gallery.service.GalleryService;
import tw.com.tymbackend.support.SecuredWebSlice;

/** All 5 GalleryController endpoints. Auth matrix lives in SecurityRulesWebTest. */
@WebMvcTest(GalleryController.class)
@ContextConfiguration(classes = { GalleryController.class, SecurityConfig.class })
@SecuredWebSlice
class GalleryControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GalleryService service;

    private static Gallery gallery() {
        Gallery g = new Gallery();
        g.setId(7);
        g.setImageBase64("AAAA");
        return g;
    }

    @Test
    void getAll_Should_Return200() throws Exception {
        when(service.getAllImages()).thenReturn(List.of(gallery()));
        mvc.perform(post("/gallery/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(7));
    }

    @Test
    void getAll_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getAllImages()).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/gallery/getAll")).andExpect(status().isInternalServerError());
    }

    @Test
    void getById_Should_Return200_When_Found() throws Exception {
        when(service.getImageById(7)).thenReturn(Optional.of(gallery()));
        mvc.perform(post("/gallery/getById").contentType(MediaType.APPLICATION_JSON).content("7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void getById_Should_Return404_When_Missing() throws Exception {
        when(service.getImageById(8)).thenReturn(Optional.empty());
        mvc.perform(post("/gallery/getById").contentType(MediaType.APPLICATION_JSON).content("8"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_Should_Return500_When_ServiceFails() throws Exception {
        when(service.getImageById(7)).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/gallery/getById").contentType(MediaType.APPLICATION_JSON).content("7"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void save_Should_Return200() throws Exception {
        when(service.saveImage(any())).thenReturn(gallery());
        mvc.perform(post("/gallery/save").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"imageBase64\":\"AAAA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void save_Should_Return500_When_ServiceFails() throws Exception {
        when(service.saveImage(any())).thenThrow(new RuntimeException("boom"));
        mvc.perform(post("/gallery/save").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"imageBase64\":\"AAAA\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void update_Should_Return200() throws Exception {
        when(service.updateImage(7, "BBBB")).thenReturn(gallery());
        mvc.perform(post("/gallery/update").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":7,\"imageBase64\":\"BBBB\"}"))
                .andExpect(status().isOk());
        verify(service).updateImage(7, "BBBB");
    }

    @Test
    void update_Should_Return404_When_ImageMissing() throws Exception {
        when(service.updateImage(any(), any())).thenThrow(new RuntimeException("not found"));
        mvc.perform(post("/gallery/update").with(user()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":9,\"imageBase64\":\"BBBB\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_Should_Return204() throws Exception {
        mvc.perform(post("/gallery/delete").with(user()).contentType(MediaType.APPLICATION_JSON).content("{\"id\":7}"))
                .andExpect(status().isNoContent());
        verify(service).deleteImage(7);
    }

    @Test
    void delete_Should_Return500_When_ServiceFails() throws Exception {
        doThrow(new RuntimeException("boom")).when(service).deleteImage(7);
        mvc.perform(post("/gallery/delete").with(user()).contentType(MediaType.APPLICATION_JSON).content("{\"id\":7}"))
                .andExpect(status().isInternalServerError());
    }
}
