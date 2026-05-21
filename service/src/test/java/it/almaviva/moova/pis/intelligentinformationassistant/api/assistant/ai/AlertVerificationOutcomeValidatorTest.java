package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertVerificationOutcomeValidatorTest {

    private final AlertVerificationOutcomeValidator validator = new AlertVerificationOutcomeValidator();

    @Test
    void acceptsCatalogDrivenServiceDataFieldMatch() {
        AlertVerificationOutcome validated = validator.validate(outcomeWithCondition(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].passingType",
                        "operator", "EQUALS",
                        "value", "TRANSIT")))));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(validated.interpretedTargetTypes()).containsExactly("SERVICE_DATA_JOURNEY");
    }

    @Test
    void rejectsUnknownCatalogField() {
        AlertVerificationOutcome validated = validator.validate(outcomeWithCondition(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].unknownField",
                        "operator", "EQUALS",
                        "value", "x")))));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("field is not allowed");
    }

    @Test
    void rejectsUnsupportedCatalogOperator() {
        AlertVerificationOutcome validated = validator.validate(outcomeWithCondition(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].passingType",
                        "operator", "EXECUTE",
                        "value", "TRANSIT")))));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("operator is not allowed");
    }

    private AlertVerificationOutcome outcomeWithCondition(Map<String, Object> condition) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "The alert can be evaluated on realtime ServiceData events.",
                null,
                0.80,
                "test",
                "test-model",
                "alert-verify-mvp-v1",
                List.of("SERVICE_DATA"),
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "EVENT",
                "STATELESS_EVENT_MATCH",
                List.of("SERVICE_DATA_FIELD_MATCH"),
                List.of("SERVICE_DATA_JOURNEY"),
                Map.of(
                        "schemaVersion", "iia.alert.technical-specification/v2",
                        "source", "SERVICE_DATA",
                        "inputModel", "ServiceDataV2",
                        "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                        "triggerType", "EVENT",
                        "evaluationMode", "STATELESS_EVENT_MATCH",
                        "condition", condition,
                        "deduplicationKeyTemplate", "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}"),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "agentName", "ServiceDataFieldMatchAlertAgent",
                        "triggerType", "EVENT",
                        "requiredSources", List.of("SERVICE_DATA"),
                        "evaluationMode", "STATELESS_EVENT_MATCH",
                        "targetTypes", List.of("SERVICE_DATA_JOURNEY"),
                        "stateRequirements", Map.of("requiresState", false),
                        "output", Map.of("type", "CANDIDATE_SUGGESTION")),
                List.of(),
                List.of(
                        "No executable code generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
    }
}
