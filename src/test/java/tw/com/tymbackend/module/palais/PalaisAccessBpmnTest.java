package tw.com.tymbackend.module.palais;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/** The published diagram must keep describing the real state machine (docs/PALAIS_ACCESS.md). */
class PalaisAccessBpmnTest {
    private static String diagram() throws Exception {
        try (var in = new ClassPathResource("palais-access.bpmn").getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void everyFlowEndpointIsADeclaredElementAndEveryElementHasAShape() throws Exception {
        String xml = diagram();
        var ids = new HashSet<String>();
        var element = Pattern.compile("<bpmn:(?!sequenceFlow|process|definitions)\\w+ id=\"([^\"]+)\"").matcher(xml);
        while (element.find()) ids.add(element.group(1));
        var ref = Pattern.compile("(?:sourceRef|targetRef)=\"([^\"]+)\"").matcher(xml);
        while (ref.find()) assertThat(ids).contains(ref.group(1));
        for (String id : ids) assertThat(xml).contains("bpmnElement=\"" + id + "\"");
    }

    @Test
    void everyStateAndAdministratorActionOfTheStateMachineIsDrawn() throws Exception {
        String xml = diagram();
        // PENDING, WITHDRAWN, REJECTED, SYNCING, ACTIVE, FAILED, REVOKED and the transitions between them.
        assertThat(xml).contains("Checker reviews", "Withdrawn by Maker", "Rejected", "SYNCING", "Access active",
                "FAILED: administrator checks and retries", "Revoked by administrator");
        assertThat(xml).contains("Maker withdraws", "Checker rejects", "Checker approves", "Quorum reached",
                "More approvals needed", "Temporary failure", "5 attempts exhausted", "Administrator retries",
                "Administrator revokes");
    }
}
