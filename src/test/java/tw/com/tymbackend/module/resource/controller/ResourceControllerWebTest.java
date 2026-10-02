package tw.com.tymbackend.module.resource.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.module.resource.service.CompanyProductMappingService;
import tw.com.tymbackend.support.SecuredWebSlice;

@WebMvcTest(ResourceController.class)
@ContextConfiguration(classes = { ResourceController.class, SecurityConfig.class })
@SecuredWebSlice
class ResourceControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CompanyProductMappingService service;

    @Test
    void mapping_Should_BePublicAndReturnParsedJson() throws Exception {
        when(service.getCompanyProductMappingJson()).thenReturn("{\"acme\":[\"widget\"]}");
        mvc.perform(get("/resources/company-product-mapping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.acme[0]").value("widget"));
    }

    @Test
    void mapping_Should_Return500_When_StoredJsonInvalid() throws Exception {
        when(service.getCompanyProductMappingJson()).thenReturn("not json");
        mvc.perform(get("/resources/company-product-mapping")).andExpect(status().isInternalServerError());
    }

    @Test
    void mapping_Should_Return500_When_StorageFails() throws Exception {
        when(service.getCompanyProductMappingJson()).thenThrow(new RuntimeException("oci down"));
        mvc.perform(get("/resources/company-product-mapping")).andExpect(status().isInternalServerError());
    }
}
