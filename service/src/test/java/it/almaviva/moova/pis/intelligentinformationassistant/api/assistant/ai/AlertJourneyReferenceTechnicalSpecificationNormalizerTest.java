package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertJourneyReferenceTechnicalSpecificationNormalizerTest {

    private final AlertJourneyReferenceTechnicalSpecificationNormalizer normalizer =
            new AlertJourneyReferenceTechnicalSpecificationNormalizer();
    private final AlertVerificationOutcomeValidator validator = new AlertVerificationOutcomeValidator();

    @Test
    void normalizesUnqualifiedDescriptorVehicleJourneyNameToCanonicalOrAndValidates() {
        AlertVerificationLocationContext context = context(AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, "M2");

        AlertVerificationOutcome normalized = normalizer.normalize(
                outcome(anyElement(contains("vehicleJourneyName", "M2"))),
                context,
                "ALRT_M2");
        AlertVerificationOutcome validated = validator.validate(
                normalized,
                null,
                context);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertCanonicalOr(normalized.technicalSpecification());
        assertThat(normalized.technicalSpecification().toString()).doesNotContain("vehicleJourneyName=M2");
    }

    @Test
    void normalizesUnqualifiedDescriptorLineOnlyToCanonicalOr() {
        AlertVerificationLocationContext context = context(AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, "M2");

        AlertVerificationOutcome normalized = normalizer.normalize(
                outcome(anyElement(eq("line.dsc", "M2"))),
                context,
                "ALRT_M2");

        assertCanonicalOr(normalized.technicalSpecification());
    }

    @Test
    void leavesAlreadyCanonicalUnqualifiedDescriptorUnchanged() {
        AlertVerificationLocationContext context = context(AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, "M2");
        AlertVerificationOutcome original = outcome(anyElement(canonicalOr("M2")));

        AlertVerificationOutcome normalized = normalizer.normalize(original, context, "ALRT_M2");

        assertThat(normalized).isSameAs(original);
    }

    @Test
    void doesNotBroadenExplicitLineOrOperator() {
        AlertVerificationOutcome line = normalizer.normalize(
                outcome(anyElement(eq("line.dsc", "M2"))),
                context(AlertJourneyReferenceKind.LINE, "M2"),
                "ALRT_LINE");
        AlertVerificationOutcome operator = normalizer.normalize(
                outcome(anyElement(eq("transportOperator.dsc", "ATM"))),
                context(AlertJourneyReferenceKind.TRANSPORT_OPERATOR, "ATM"),
                "ALRT_OPERATOR");

        assertThat(line).isNotNull();
        assertThat(condition(line.technicalSpecification()).toString()).doesNotContain("serviceCategory.dsc");
        assertThat(condition(operator.technicalSpecification()).toString()).doesNotContain("line.dsc");
    }

    @Test
    void preservesCorrelationWhenCombinedWithAnotherJourneyDetailPredicateAndAlignsPreview() {
        AlertVerificationLocationContext context = context(AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, "M2");
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("all", List.of(
                                Map.of(
                                        "field", "departureDelay.delay",
                                        "operator", "GREATER_OR_EQUAL",
                                        "value", 300),
                                contains("vehicleJourneyName", "M2")))));

        AlertVerificationOutcome normalized = normalizer.normalize(outcome(condition), context, "ALRT_M2_DELAY");

        assertThat(condition(normalized.technicalSpecification()).toString())
                .contains("departureDelay.delay")
                .contains("line.dsc")
                .contains("serviceCategory.dsc")
                .contains("transportOperator.dsc");
        assertThat(condition(normalized.agentBlueprintPreview()).toString())
                .contains("line.dsc")
                .doesNotContain("vehicleJourneyName");
    }

    @Test
    void leavesAmbiguousContradictoryPredicatesUnchangedForValidatorRejection() {
        AlertVerificationLocationContext context = context(AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR, "M2");
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("all", List.of(
                                contains("vehicleJourneyName", "M2"),
                                eq("line.dsc", "M3")))));
        AlertVerificationOutcome original = outcome(condition);

        AlertVerificationOutcome normalized = normalizer.normalize(original, context, "ALRT_M2");
        AlertVerificationOutcome validated = validator.validate(normalized, "Prompt", context);

        assertThat(normalized).isSameAs(original);
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
    }

    @SuppressWarnings("unchecked")
    private void assertCanonicalOr(Map<String, Object> technicalSpecification) {
        Map<String, Object> condition = condition(technicalSpecification);
        Map<String, Object> anyElement = (Map<String, Object>) condition.get("anyElement");
        Map<String, Object> conditions = (Map<String, Object>) anyElement.get("conditions");
        List<Map<String, Object>> any = (List<Map<String, Object>>) conditions.get("any");

        assertThat(any).hasSize(3);
        assertThat(any).extracting(item -> item.get("field"))
                .containsExactlyInAnyOrder("line.dsc", "serviceCategory.dsc", "transportOperator.dsc");
        assertThat(any).extracting(item -> item.get("operator"))
                .containsOnly("EQUALS_NORMALIZED");
        assertThat(any).extracting(item -> item.get("value"))
                .containsOnly("M2");
    }

    private AlertVerificationLocationContext context(AlertJourneyReferenceKind kind, String value) {
        return new AlertVerificationLocationContext(
                false,
                List.of(),
                List.of(new AlertVerificationLocationContext.NonLocationConstraint(
                        "JOURNEY_REFERENCE",
                        value,
                        kind.name(),
                        value,
                        true,
                        0.92)),
                List.of());
    }

    private AlertVerificationOutcome outcome(Map<String, Object> condition) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "ok",
                null,
                0.90,
                "test",
                "test-model",
                "test",
                List.of("SERVICE_DATA"),
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "EVENT",
                "STATELESS_EVENT_MATCH",
                List.of("ARRIVING"),
                List.of("SERVICE_DATA_JOURNEY"),
                Map.of(
                        "source", "SERVICE_DATA",
                        "interpreterType", "EVENT_INTERPRETER",
                        "triggerType", "EVENT",
                        "inputModel", "ServiceDataV2",
                        "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                        "evaluationMode", "STATELESS_EVENT_MATCH",
                        "condition", condition),
                Map.of(
                        "requiredSources", List.of("SERVICE_DATA"),
                        "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition)),
                Map.of(
                        "allRequiredRequirementsMapped", true,
                        "requirements", List.of(Map.of(
                                "text", "M2",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of(
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].line.dsc",
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].serviceCategory.dsc",
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].transportOperator.dsc"),
                                "reason", ""))),
                List.of(),
                List.of());
    }

    private Map<String, Object> anyElement(Map<String, Object> condition) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", condition));
    }

    private Map<String, Object> eq(String field, String value) {
        return Map.of("field", field, "operator", "EQUALS_NORMALIZED", "value", value);
    }

    private Map<String, Object> contains(String field, String value) {
        return Map.of("field", field, "operator", "CONTAINS_NORMALIZED", "value", value);
    }

    private Map<String, Object> canonicalOr(String value) {
        return Map.of("any", List.of(
                eq("line.dsc", value),
                eq("serviceCategory.dsc", value),
                eq("transportOperator.dsc", value)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> condition(Map<String, Object> wrapper) {
        if (wrapper.containsKey("parameters")) {
            return (Map<String, Object>) ((Map<String, Object>) wrapper.get("parameters")).get("condition");
        }
        return (Map<String, Object>) wrapper.get("condition");
    }
}
