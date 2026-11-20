package tw.com.tymbackend.module.integrity;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
final class BusinessProcessController {
    @GetMapping(value = "/api/business-processes.bpmn", produces = "application/xml")
    Resource businessProcesses() {
        return new ClassPathResource("business-processes.bpmn");
    }
}
