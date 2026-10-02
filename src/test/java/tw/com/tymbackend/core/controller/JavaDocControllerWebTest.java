package tw.com.tymbackend.core.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.com.tymbackend.support.TestAuth.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import tw.com.tymbackend.core.config.security.SecurityConfig;
import tw.com.tymbackend.support.SecuredWebSlice;

@WebMvcTest(JavaDocController.class)
@ContextConfiguration(classes = { JavaDocController.class, SecurityConfig.class })
@SecuredWebSlice
class JavaDocControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void docs_Should_RedirectToJavadocIndex() throws Exception {
        mvc.perform(get("/docs").with(user()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/javadoc/index.html"));
    }

    @Test
    void docsApi_Should_RedirectToJavadocIndex() throws Exception {
        mvc.perform(get("/docs/api").with(user()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/javadoc/index.html"));
    }
}
