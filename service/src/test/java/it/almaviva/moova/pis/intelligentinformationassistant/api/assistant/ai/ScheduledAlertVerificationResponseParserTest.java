package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertVerificationResponseParserTest {

    private final ScheduledAlertVerificationResponseParser parser = new ScheduledAlertVerificationResponseParser();

    @Test
    void parsesValidVerifiedMinimalScheduledResponse() {
        AlertVerificationOutcome outcome = parser.parse(verifiedResponse(), "test-provider", "test-model").orElseThrow();

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.requiredSources()).contains("SERVICE_DATA");
        assertThat(outcome.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
        assertThat(outcome.triggerType()).isEqualTo("SCHEDULE");
        assertThat(outcome.evaluationMode()).isEqualTo("SCHEDULED_SNAPSHOT_MATCH");
        assertThat(outcome.inputModel()).isEqualTo("ServiceDataStopPointJourneysV2");
        assertThat(outcome.technicalSpecification()).isNotNull();
        assertThat(outcome.agentBlueprintPreview()).isNotNull();
        assertThat(outcome.promptVersion()).isEqualTo("alert-scheduled-verify-mvp-v1");
    }

    @Test
    void parsesValidRejectedResponse() {
        AlertVerificationOutcome outcome = parser.parse("""
                {
                  "decision": "REJECTED",
                  "summary": "Rejected.",
                  "rejectedReason": "Passenger count is not available in Scheduled ServiceData snapshot catalog.",
                  "confidence": 0.0,
                  "requiredSources": ["SERVICE_DATA"],
                  "technicalSpecification": null,
                  "agentBlueprintPreview": null,
                  "requirementCoverage": {
                    "requirements": [{"text":"passenger count","required":true,"mappable":false,"mappedBy":[],"reason":"unsupported"}],
                    "allRequiredRequirementsMapped": false
                  },
                  "warnings": [],
                  "safetyChecks": []
                }
                """, "test-provider", "test-model").orElseThrow();

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("Passenger count");
        assertThat(outcome.technicalSpecification()).isNull();
        assertThat(outcome.agentBlueprintPreview()).isNull();
    }

    @Test
    void rejectsVerifiedMissingTechnicalSpecification() {
        assertParseFailure(verifiedResponse().replaceFirst("\"technicalSpecification\": \\{", "\"technicalSpecification\": null, \"removed\": {"),
                "technicalSpecification");
    }

    @Test
    void rejectsVerifiedWithEventInterpreter() {
        assertParseFailure(verifiedResponse().replace("\"interpreterType\": \"SCHEDULED_INTERPRETER\"", "\"interpreterType\": \"EVENT_INTERPRETER\""),
                "SCHEDULED_INTERPRETER");
    }

    @Test
    void rejectsVerifiedWithEventTriggerType() {
        assertParseFailure(verifiedResponse().replace("\"triggerType\": \"SCHEDULE\"", "\"triggerType\": \"EVENT\""),
                "SCHEDULE");
    }

    @Test
    void rejectsVerifiedWithServiceDataV2InputModel() {
        assertParseFailure(verifiedResponse().replace("\"inputModel\": \"ServiceDataStopPointJourneysV2\"", "\"inputModel\": \"ServiceDataV2\""),
                "ServiceDataStopPointJourneysV2");
    }

    @Test
    void rejectsNonJsonResponse() {
        ScheduledAlertVerificationResponseParser.ParseResult result =
                parser.parseDetailed("Here is the JSON: {}", "test-provider", "test-model");

        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PARSER] failure=" + result.failureReason());
        assertThat(result.outcome()).isEmpty();
        assertThat(result.failureReason()).contains("raw JSON");
    }

    @Test
    void rejectsMarkdownFencedJson() {
        ScheduledAlertVerificationResponseParser.ParseResult result =
                parser.parseDetailed("```json\n" + verifiedResponse() + "\n```", "test-provider", "test-model");

        assertThat(result.outcome()).isEmpty();
        assertThat(result.failureReason()).contains("raw JSON");
    }

    @Test
    void rejectsRejectedWithoutRejectedReason() {
        ScheduledAlertVerificationResponseParser.ParseResult result = parser.parseDetailed("""
                {
                  "decision": "REJECTED",
                  "summary": "Rejected.",
                  "rejectedReason": "",
                  "technicalSpecification": null,
                  "agentBlueprintPreview": null
                }
                """, "test-provider", "test-model");

        assertThat(result.outcome()).isEmpty();
        assertThat(result.failureReason()).contains("rejectedReason");
    }

    @Test
    void normalizesLocalTimeBetweenTopLevelInternalHintKeys() {
        AlertVerificationOutcome outcome = parser.parse(responseWithCondition("""
                {
                  "field": "callStart.departureTime",
                  "operator": "LOCAL_TIME_BETWEEN",
                  "startLocalTime": "10:00:00",
                  "endLocalTime": "12:00:00",
                  "timezone": "Europe/Rome"
                }
                """), "test-provider", "test-model").orElseThrow();

        assertNormalizedTimeValue(outcome, "10:00:00", "12:00:00");
    }

    @Test
    void normalizesLocalTimeBetweenValueInternalHintKeys() {
        AlertVerificationOutcome outcome = parser.parse(responseWithCondition("""
                {
                  "field": "callStart.departureTime",
                  "operator": "LOCAL_TIME_BETWEEN",
                  "value": {
                    "startLocalTime": "18:00:00",
                    "endLocalTime": "20:00:00",
                    "timezone": "Europe/Rome"
                  }
                }
                """), "test-provider", "test-model").orElseThrow();

        assertNormalizedTimeValue(outcome, "18:00:00", "20:00:00");
    }

    @Test
    void normalizesLocalTimeBetweenTopLevelStartEndKeys() {
        AlertVerificationOutcome outcome = parser.parse(responseWithCondition("""
                {
                  "field": "callStart.departureTime",
                  "operator": "LOCAL_TIME_BETWEEN",
                  "start": "18:30:00",
                  "end": "23:59:59",
                  "timezone": "Europe/Rome"
                }
                """), "test-provider", "test-model").orElseThrow();

        assertNormalizedTimeValue(outcome, "18:30:00", "23:59:59");
    }

    @Test
    void rejectsLocalTimeBetweenMissingTimezoneAfterNormalizationAttempt() {
        ScheduledAlertVerificationResponseParser.ParseResult result = parser.parseDetailed(responseWithCondition("""
                {
                  "field": "callStart.departureTime",
                  "operator": "LOCAL_TIME_BETWEEN",
                  "startLocalTime": "10:00:00",
                  "endLocalTime": "12:00:00"
                }
                """), "test-provider", "test-model");

        assertThat(result.outcome()).isEmpty();
        assertThat(result.failureReason()).contains("LOCAL_TIME_BETWEEN requires value.start");
    }

    @Test
    void rejectsAnyElementConditionsDirectArray() {
        ScheduledAlertVerificationResponseParser.ParseResult result = parser.parseDetailed(responseWithAnyElementConditions("""
                [
                  {
                    "field": "callStart.departureTime",
                    "operator": "LOCAL_TIME_BETWEEN",
                    "value": {
                      "start": "18:30:00",
                      "end": "23:59:59",
                      "timezone": "Europe/Rome"
                    }
                  }
                ]
                """), "test-provider", "test-model");

        assertThat(result.outcome()).isEmpty();
        assertThat(result.failureReason()).contains("anyElement.conditions must be an object");
    }

    private void assertParseFailure(String response, String expectedReasonFragment) {
        ScheduledAlertVerificationResponseParser.ParseResult result =
                parser.parseDetailed(response, "test-provider", "test-model");

        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PARSER] failure=" + result.failureReason());
        assertThat(result.outcome()).isEmpty();
        assertThat(result.failureReason()).contains(expectedReasonFragment);
    }

    @SuppressWarnings("unchecked")
    private void assertNormalizedTimeValue(AlertVerificationOutcome outcome, String start, String end) {
        Map<String, Object> snapshotEvaluation = (Map<String, Object>) outcome.technicalSpecification().get("snapshotEvaluation");
        Map<String, Object> condition = (Map<String, Object>) snapshotEvaluation.get("condition");
        Map<String, Object> anyElement = (Map<String, Object>) condition.get("anyElement");
        Map<String, Object> conditions = (Map<String, Object>) anyElement.get("conditions");
        Map<String, Object> value = (Map<String, Object>) conditions.get("value");
        assertThat(value)
                .containsEntry("start", start)
                .containsEntry("end", end)
                .containsEntry("timezone", "Europe/Rome");
        assertThat(conditions).doesNotContainKeys("startLocalTime", "endLocalTime", "start", "end", "timezone");
    }

    private String responseWithCondition(String conditionLeaf) {
        return responseWithAnyElementConditions(conditionLeaf);
    }

    private String responseWithAnyElementConditions(String conditionsJson) {
        return verifiedResponse().replace("""
                          "conditions": {
                            "field": "arrivalStatuses[].status",
                            "operator": "CONTAINS",
                            "value": "ARRIVING"
                          }
                """, """
                          "conditions": %s
                """.formatted(conditionsJson.stripIndent().trim()));
    }

    private String verifiedResponse() {
        return """
                {
                  "decision": "VERIFIED",
                  "summary": "The alert can be evaluated by scheduled snapshots.",
                  "rejectedReason": null,
                  "confidence": 0.91,
                  "requiredSources": ["SERVICE_DATA"],
                  "interpreterType": "SCHEDULED_INTERPRETER",
                  "accessMode": "SERVICE_DATA_API_SNAPSHOT",
                  "triggerType": "SCHEDULE",
                  "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH",
                  "inputModel": "ServiceDataStopPointJourneysV2",
                  "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                  "targetTypes": ["SERVICE_DATA_JOURNEY_AGGREGATE"],
                  "requirementCoverage": {
                    "requirements": [
                      {"text": "two arriving journeys", "required": true, "mappable": true, "mappedBy": ["stopPointsJourneyDetails[].arrivalStatuses[].status"], "reason": null}
                    ],
                    "allRequiredRequirementsMapped": true
                  },
                  "technicalSpecification": {
                    "schemaVersion": "iia.alert.technical-specification/v2",
                    "source": "SERVICE_DATA",
                    "interpreterType": "SCHEDULED_INTERPRETER",
                    "accessMode": "SERVICE_DATA_API_SNAPSHOT",
                    "inputModel": "ServiceDataStopPointJourneysV2",
                    "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                    "triggerType": "SCHEDULE",
                    "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH",
                    "schedule": {"frequencySeconds": 600, "defaulted": true, "rawText": null},
                    "serviceDataQuery": {
                      "operation": "POST /v2/stoppointjourneys",
                      "monitoringScope": "EXPLICIT_STOP_POINTS",
                      "stopPoints": ["TNPNTS00000000005439"],
                      "requiresAllKnownStopPoints": false,
                      "timeWindow": {
                        "startMode": "NOW_TRUNCATED_TO_MINUTE",
                        "endMode": "NOW_PLUS_DEFAULT_LOOKAHEAD",
                        "lookaheadMinutes": 480,
                        "defaulted": true,
                        "rawText": null
                      }
                    },
                    "snapshotEvaluation": {
                      "mode": "COUNT_MATCHING_JOURNEYS",
                      "journeyPath": "stopPointsJourneyDetails[]",
                      "condition": {
                        "type": "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                        "anyElement": {
                          "path": "stopPointsJourneyDetails[]",
                          "conditions": {
                            "field": "arrivalStatuses[].status",
                            "operator": "CONTAINS",
                            "value": "ARRIVING"
                          }
                        }
                      },
                      "threshold": {"operator": "GREATER_OR_EQUAL", "value": 2}
                    },
                    "outputPolicy": {"emit": "ON_MATCH", "includeCount": true, "includeMatchingJourneys": true},
                    "deduplicationKeyTemplate": "SERVICE_DATA_SCHEDULED:${alertId}:${queryWindowStart}:${conditionHash}"
                  },
                  "agentBlueprintPreview": {
                    "schemaVersion": "iia.agent.blueprint/v1",
                    "agentName": "ScheduledServiceDataSnapshotAlertAgent",
                    "description": "Periodically queries ServiceData stop point journeys and evaluates a snapshot condition.",
                    "triggerType": "SCHEDULE",
                    "requiredSources": ["SERVICE_DATA"],
                    "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH",
                    "targetTypes": ["SERVICE_DATA_JOURNEY_AGGREGATE"],
                    "parameters": {"serviceDataQuery": {}, "snapshotEvaluation": {}, "outputPolicy": {}},
                    "stateRequirements": {"requiresState": false},
                    "output": {"type": "CANDIDATE_SUGGESTION"}
                  },
                  "warnings": [],
                  "safetyChecks": [
                    "No executable code generated.",
                    "No Agent Definition created.",
                    "No Agent Run created.",
                    "No Suggestion created."
                  ]
                }
                """;
    }
}
