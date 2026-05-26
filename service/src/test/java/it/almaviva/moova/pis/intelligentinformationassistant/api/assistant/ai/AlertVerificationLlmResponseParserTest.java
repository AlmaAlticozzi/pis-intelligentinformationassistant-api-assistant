package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertVerificationLlmResponseParserTest {

    private final AlertVerificationLlmResponseParser parser = new AlertVerificationLlmResponseParser();

    @Test
    @SuppressWarnings("unchecked")
    void preservesLocalTimeBetweenValueObject() {
        String response = """
                {
                  "decision":"VERIFIED",
                  "technicalSpecification":{
                    "condition":{
                      "type":"SERVICE_DATA_FIELD_MATCH",
                      "all":[{
                        "field":"payload.ongroundServiceEvent.eventGenerationTime",
                        "operator":"LOCAL_TIME_BETWEEN",
                        "value":{"start":"02:00:00","end":"10:00:00","timezone":"Europe/Rome"}
                      }]
                    }
                  },
                  "agentBlueprintPreview":{"parameters":{"condition":{"type":"SERVICE_DATA_FIELD_MATCH","all":[]}}}
                }
                """;

        AlertVerificationOutcome outcome = parser.parse(response, "test", "model").orElseThrow();
        Map<String, Object> condition = (Map<String, Object>) outcome.technicalSpecification().get("condition");
        Map<String, Object> leaf = ((List<Map<String, Object>>) condition.get("all")).getFirst();
        Map<String, Object> value = (Map<String, Object>) leaf.get("value");

        assertThat(leaf.get("operator")).isEqualTo("LOCAL_TIME_BETWEEN");
        assertThat(value).containsEntry("start", "02:00:00")
                .containsEntry("end", "10:00:00")
                .containsEntry("timezone", "Europe/Rome");
    }
}
