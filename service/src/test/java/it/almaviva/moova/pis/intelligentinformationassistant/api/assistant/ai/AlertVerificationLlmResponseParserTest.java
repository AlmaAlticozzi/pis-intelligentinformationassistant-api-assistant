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

    @Test
    @SuppressWarnings("unchecked")
    void preservesCorrelatedNextCallsAnyElementCondition() {
        String response = """
                {
                  "decision":"VERIFIED",
                  "technicalSpecification":{
                    "condition":{
                      "type":"SERVICE_DATA_FIELD_MATCH",
                      "all":[{
                        "anyElement":{
                          "path":"payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                          "conditions":{"all":[
                            {"field":"stopPoint.nameLong","operator":"EQUALS_NORMALIZED","value":"Gorla"},
                            {"field":"departureTime","operator":"LOCAL_TIME_BETWEEN","value":{"start":"11:30:00","end":"12:35:00","timezone":"Europe/Rome"}}
                          ]}
                        }
                      }]
                    }
                  },
                  "agentBlueprintPreview":{"parameters":{"condition":{"type":"SERVICE_DATA_FIELD_MATCH","all":[]}}}
                }
                """;

        AlertVerificationOutcome outcome = parser.parse(response, "test", "model").orElseThrow();
        Map<String, Object> condition = (Map<String, Object>) outcome.technicalSpecification().get("condition");
        Map<String, Object> anyElement = (Map<String, Object>) ((List<Map<String, Object>>) condition.get("all"))
                .getFirst().get("anyElement");
        Map<String, Object> conditions = (Map<String, Object>) anyElement.get("conditions");
        List<Map<String, Object>> leaves = (List<Map<String, Object>>) conditions.get("all");

        assertThat(anyElement).containsEntry("path", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]");
        assertThat(leaves.getFirst()).containsEntry("field", "stopPoint.nameLong");
        assertThat(leaves.get(1)).containsEntry("field", "departureTime")
                .containsEntry("operator", "LOCAL_TIME_BETWEEN");
    }
}
