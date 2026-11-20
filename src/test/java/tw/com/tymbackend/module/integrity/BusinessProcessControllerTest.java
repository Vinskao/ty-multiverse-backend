package tw.com.tymbackend.module.integrity;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

class BusinessProcessControllerTest {
    @Test
    void servesPackagedBpmnXml() throws Exception {
        MockMvcBuilders.standaloneSetup(new BusinessProcessController()).build()
            .perform(get("/api/business-processes.bpmn"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/xml"))
            .andExpect(content().string(containsString("<bpmndi:BPMNDiagram")));
    }
}
