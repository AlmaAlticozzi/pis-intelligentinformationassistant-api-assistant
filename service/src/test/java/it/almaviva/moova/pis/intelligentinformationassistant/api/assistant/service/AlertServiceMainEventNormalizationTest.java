package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertServiceMainEventNormalizationTest {

    private static final String RHO_FIERAMILANO_ID = "TNPNTS00000000005467";
    private static final String EVENT_FIELD = "payload.ongroundServiceEvent.eventsType";
    private static final String STOP_FIELD = "payload.stopPointJourney.stopPoint.id";

    private final AlertService service = new AlertService();
    private final AlertVerificationOutcomeValidator validator = new AlertVerificationOutcomeValidator();

    @Test
    void normalizesDepartedToDepartingWhenExpectedProgressiveDeparture() {
        AlertVerificationLocationContext context = locationContext("DEPARTURE", "PROGRESSIVE", "DEPARTING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEvent("DEPARTED")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("DEPARTING");
        assertThat(blueprintEventValue(normalized.agentBlueprintPreview())).isEqualTo("DEPARTING");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void normalizesArrivedToArrivingWhenExpectedProgressiveArrival() {
        AlertVerificationLocationContext context = locationContext("ARRIVAL", "PROGRESSIVE", "ARRIVING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEvent("ARRIVED")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("ARRIVING");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void doesNotNormalizeAcrossArrivalDepartureDomains() {
        AlertVerificationLocationContext context = locationContext("DEPARTURE", "PROGRESSIVE", "DEPARTING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithEvent("ARRIVED")),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(eventValue(normalized.technicalSpecification())).isEqualTo("ARRIVED");
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("DEPARTING");
    }

    @Test
    void doesNotInventMissingEventsTypeCondition() {
        AlertVerificationLocationContext context = locationContext("DEPARTURE", "PROGRESSIVE", "DEPARTING");

        AlertVerificationOutcome normalized = service.normalizeExpectedMainEventType(
                outcomeWithCondition(conditionWithoutEvent()),
                promptData(context));
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(hasEventCondition(normalized.technicalSpecification())).isFalse();
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("DEPARTING");
    }

    private AlertVerificationPromptData promptData(AlertVerificationLocationContext context) {
        return new AlertVerificationPromptData("ALRT1", "Alert", null, "Prompt", context);
    }

    private AlertVerificationLocationContext locationContext(String intent, String phase, String expectedEventType) {
        return new AlertVerificationLocationContext(
                true,
                List.of(new AlertVerificationLocationContext.LocationResolution(
                        "X",
                        "X",
                        "MAIN_EVENT_LOCATION",
                        "EVENT_LOCATION",
                        true,
                        "INCLUDE",
                        "G1",
                        0.90,
                        "RESOLVED",
                        List.of(new AlertVerificationLocationContext.LocationCandidate(
                                RHO_FIERAMILANO_ID,
                                "RHO FIERAMILANO",
                                "RHO FIERAMILANO",
                                "RAIL",
                                1.0,
                                "EXACT_NORMALIZED",
                                true)),
                        List.of(RHO_FIERAMILANO_ID),
                        false,
                        false,
                        0.0,
                        "",
                        List.of(STOP_FIELD))),
                List.of(
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_INTENT", intent),
                        new AlertVerificationLocationContext.NonLocationConstraint("MAIN_EVENT_PHASE", phase),
                        new AlertVerificationLocationContext.NonLocationConstraint("EXPECTED_MAIN_EVENT_TYPE", expectedEventType)),
                List.of());
    }

    private Map<String, Object> conditionWithEvent(String eventType) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("field", EVENT_FIELD, "operator", "CONTAINS", "value", eventType),
                        Map.of("field", STOP_FIELD, "operator", "EQUALS", "value", RHO_FIERAMILANO_ID)));
    }

    private Map<String, Object> conditionWithoutEvent() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of("field", STOP_FIELD, "operator", "EQUALS", "value", RHO_FIERAMILANO_ID)));
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
                        "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                        "stateRequirements", Map.of("requiresState", false),
                        "output", Map.of("type", "CANDIDATE_SUGGESTION")),
                coverageFor(condition),
                List.of(),
                List.of(
                        "No executable code generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
    }

    private Map<String, Object> coverageFor(Map<String, Object> condition) {
        List<Map<String, Object>> requirements = hasEventLeaf(condition)
                ? List.of(coverageRequirement(EVENT_FIELD), coverageRequirement(STOP_FIELD))
                : List.of(coverageRequirement(STOP_FIELD));
        return Map.of("requirements", requirements, "allRequiredRequirementsMapped", true);
    }

    private Map<String, Object> coverageRequirement(String field) {
        return Map.of(
                "text", field,
                "required", true,
                "mappable", true,
                "mappedBy", List.of(field),
                "reason", "");
    }

    @SuppressWarnings("unchecked")
    private String eventValue(Map<String, Object> technicalSpecification) {
        Map<String, Object> condition = (Map<String, Object>) technicalSpecification.get("condition");
        List<Map<String, Object>> all = (List<Map<String, Object>>) condition.get("all");
        return all.stream()
                .filter(leaf -> EVENT_FIELD.equals(leaf.get("field")))
                .map(leaf -> String.valueOf(leaf.get("value")))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private String blueprintEventValue(Map<String, Object> agentBlueprintPreview) {
        Map<String, Object> parameters = (Map<String, Object>) agentBlueprintPreview.get("parameters");
        return eventValue(Map.of("condition", parameters.get("condition")));
    }

    private boolean hasEventCondition(Map<String, Object> technicalSpecification) {
        return eventValue(technicalSpecification) != null;
    }

    private boolean hasEventLeaf(Map<String, Object> condition) {
        Object all = condition.get("all");
        if (all instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (item instanceof Map<?, ?> leaf && EVENT_FIELD.equals(leaf.get("field"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
