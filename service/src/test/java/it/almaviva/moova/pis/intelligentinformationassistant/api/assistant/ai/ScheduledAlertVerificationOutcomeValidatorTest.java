package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertVerificationOutcomeValidatorTest {

    private final ScheduledAlertVerificationOutcomeValidator validator = new ScheduledAlertVerificationOutcomeValidator();

    @Test
    void acceptsValidVerifiedMinimalScheduledOutcome() {
        AlertVerificationOutcome validated = validator.validate(validOutcome());

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(validated.interpreterType()).isEqualTo("SCHEDULED_INTERPRETER");
    }

    @Test
    void rejectsVerifiedMissingTechnicalSpecification() {
        assertRejected(validOutcome(null, blueprint()), "technicalSpecification");
    }

    @Test
    void rejectsVerifiedMissingAgentBlueprintPreview() {
        assertRejected(validOutcome(technicalSpecification(), null), "agentBlueprintPreview");
    }

    @Test
    void rejectsVerifiedWithEventInterpreter() {
        AlertVerificationOutcome outcome = withTopLevel(validOutcome(), "interpreterType", "EVENT_INTERPRETER");

        assertRejected(outcome, "SCHEDULED_INTERPRETER");
    }

    @Test
    void rejectsVerifiedWithEventTriggerType() {
        AlertVerificationOutcome outcome = withTopLevel(validOutcome(), "triggerType", "EVENT");

        assertRejected(outcome, "SCHEDULE");
    }

    @Test
    void rejectsVerifiedWithStatelessEventMatch() {
        AlertVerificationOutcome outcome = withTopLevel(validOutcome(), "evaluationMode", "STATELESS_EVENT_MATCH");

        assertRejected(outcome, "SCHEDULED_SNAPSHOT_MATCH");
    }

    @Test
    void rejectsVerifiedWithServiceDataV2InputModel() {
        AlertVerificationOutcome outcome = withTopLevel(validOutcome(), "inputModel", "ServiceDataV2");

        assertRejected(outcome, "ServiceDataStopPointJourneysV2");
    }

    @Test
    void rejectsTechnicalSpecificationContainingOngroundServiceEvent() {
        Map<String, Object> technical = new LinkedHashMap<>(technicalSpecification());
        Map<String, Object> snapshotEvaluation = new LinkedHashMap<>(map(technical.get("snapshotEvaluation")));
        snapshotEvaluation.put("condition", Map.of(
                "type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                "field", "payload.ongroundServiceEvent.eventsType",
                "operator", "CONTAINS",
                "value", "ARRIVING"));
        technical.put("snapshotEvaluation", snapshotEvaluation);

        assertRejected(validOutcome(technical, blueprint()), "payload.ongroundServiceEvent");
    }

    @Test
    void rejectsVerifiedMissingServiceDataQuery() {
        Map<String, Object> technical = new LinkedHashMap<>(technicalSpecification());
        technical.remove("serviceDataQuery");

        assertRejected(validOutcome(technical, blueprint()), "serviceDataQuery");
    }

    @Test
    void rejectsVerifiedMissingSnapshotEvaluation() {
        Map<String, Object> technical = new LinkedHashMap<>(technicalSpecification());
        technical.remove("snapshotEvaluation");

        assertRejected(validOutcome(technical, blueprint()), "snapshotEvaluation");
    }

    @Test
    void rejectsVerifiedMissingOutputPolicy() {
        Map<String, Object> technical = new LinkedHashMap<>(technicalSpecification());
        technical.remove("outputPolicy");

        assertRejected(validOutcome(technical, blueprint()), "outputPolicy");
    }

    @Test
    void acceptsRejectedWithRejectedReasonAndNullSpecs() {
        AlertVerificationOutcome validated = validator.validate(new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Rejected.",
                "Scheduled constraint is unsupported.",
                0.0,
                "provider",
                "model",
                "alert-scheduled-verify-mvp-v1",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                coverage(),
                List.of(),
                List.of()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).isEqualTo("Scheduled constraint is unsupported.");
    }

    @Test
    void rejectsRejectedWithoutRejectedReason() {
        AlertVerificationOutcome validated = validator.validate(new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Rejected.",
                "",
                0.0,
                "provider",
                "model",
                "alert-scheduled-verify-mvp-v1",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                coverage(),
                List.of(),
                List.of()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("rejectedReason");
    }

    @Test
    void acceptsDelayThresholdInsideStopPointsJourneyDetailsAnyElement() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureDelay.delay", "GREATER_THAN", 600)));
    }

    @Test
    void acceptsGenericDelayOrInsideStopPointsJourneyDetailsAnyElement() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("any", List.of(
                        leaf("arrivalDelay.delay", "GREATER_THAN", 0),
                        leaf("departureDelay.delay", "GREATER_THAN", 0)))));
    }

    @Test
    void acceptsOriginFilter() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("callStart.stopPoint.id", "EQUALS", "TNPNTS00000000005439")));
    }

    @Test
    void acceptsNextCallNestedAnyElement() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("anyElement", Map.of(
                        "path", "nextCalls[]",
                        "conditions", leaf("stopPoint.id", "EQUALS", "TNPNTS00000000005439")))));
    }

    @Test
    void acceptsCancelledCallNestedAnyElement() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("anyElement", Map.of(
                        "path", "nextCancelledCalls[]",
                        "conditions", leafValues("stopPoint.id", "IN", List.of("TNPNTS00000000005439"))))));
    }

    @Test
    void acceptsPlatformEquality() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("timetabledDeparturePlatform.dsc", "EQUAL_PLATFORM", "3")));
    }

    @Test
    void acceptsPlatformFieldToFieldComparison() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of(
                        "field", "timetabledDeparturePlatform.dsc",
                        "operator", "PLATFORM_NOT_EQUALS_FIELD",
                        "otherField", "actualDeparturePlatform.platform.dsc")));
    }

    @Test
    void acceptsChangesEnum() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("changes", "CONTAINS", "PLATFORM_CHANGED")));
    }

    @Test
    void acceptsExclusionBoolean() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("exclusion.totalExclusion", "EQUALS", true)));
    }

    @Test
    void acceptsReplacementNestedAnyElement() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("anyElement", Map.of(
                        "path", "replacement.stopPointReplacements[]",
                        "conditions", leaf("stopPointId.id", "EQUALS", "TNPNTS00000000005439")))));
    }

    @Test
    void rejectsEventFieldInCondition() {
        assertRejected(validOutcome(technicalSpecification(condition(Map.of(
                "field", "payload.ongroundServiceEvent.eventsType",
                "operator", "CONTAINS",
                "value", "ARRIVING"))), blueprint()), "payload.ongroundServiceEvent");
    }

    @Test
    void rejectsUnknownField() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("wifi.available", "EQUALS", true)), "wifi.available");
    }

    @Test
    void rejectsOperatorNotAllowedForField() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureDelay.delay", "CONTAINS", "10")), "CONTAINS");
    }

    @Test
    void rejectsInWithEmptyValues() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leafValues("callStart.stopPoint.id", "IN", List.of())), "non-empty values");
    }

    @Test
    void rejectsInWithValueInsteadOfValues() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("callStart.stopPoint.id", "IN", "TNPNTS00000000005439")), "values");
    }

    @Test
    void rejectsContainsAnyWithEmptyValues() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leafValues("arrivalStatuses[].status", "CONTAINS_ANY", List.of())), "non-empty values");
    }

    @Test
    void rejectsInvalidEnumValue() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("changes", "CONTAINS", "WIFI_AVAILABLE")), "WIFI_AVAILABLE");
    }

    @Test
    void rejectsInvalidLocalTimeBetweenShape() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of(
                        "field", "callStart.departureTime",
                        "operator", "LOCAL_TIME_BETWEEN",
                        "value", Map.of("end", "12:00:00"))), "LOCAL_TIME_BETWEEN");
    }

    @Test
    void rejectsPlatformOperatorOnNonPlatformField() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("vehicleJourneyName", "EQUAL_PLATFORM", "3")), "EQUAL_PLATFORM");
    }

    @Test
    void rejectsPlatformFieldComparisonWithNonPlatformOtherField() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of(
                        "field", "timetabledDeparturePlatform.dsc",
                        "operator", "PLATFORM_NOT_EQUALS_FIELD",
                        "otherField", "vehicleJourneyName")), "otherField");
    }

    @Test
    void rejectsUnknownAnyElementPath() {
        assertRejectedForCondition(conditionAnyElement("unknownArray[]",
                leaf("stopPoint.id", "EQUALS", "TNPNTS00000000005439")), "unknownArray");
    }

    @Test
    void rejectsNestedTypeProperty() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of(
                        "type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                        "field", "departureDelay.delay",
                        "operator", "GREATER_THAN",
                        "value", 600)), "type");
    }

    @Test
    void rejectsBlueprintConditionDifferentFromTechnicalSpecificationCondition() {
        Map<String, Object> blueprint = blueprintWithCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("arrivalDelay.delay", "GREATER_THAN", 0)));

        assertRejected(validOutcome(technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureDelay.delay", "GREATER_THAN", 0))), blueprint), "must equal");
    }

    private void assertRejected(AlertVerificationOutcome outcome, String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(outcome);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    private void assertVerified(Map<String, Object> condition) {
        AlertVerificationOutcome validated = validator.validate(validOutcome(technicalSpecification(condition), blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertRejectedForCondition(Map<String, Object> condition, String reasonFragment) {
        assertRejected(validOutcome(technicalSpecification(condition), blueprint()), reasonFragment);
    }

    private AlertVerificationOutcome withTopLevel(AlertVerificationOutcome outcome, String field, String value) {
        return switch (field) {
            case "interpreterType" -> new AlertVerificationOutcome(
                    outcome.decision(), outcome.summary(), outcome.rejectedReason(), outcome.confidence(),
                    outcome.provider(), outcome.model(), outcome.promptVersion(), outcome.requiredSources(), value,
                    outcome.inputModel(), outcome.outputModel(), outcome.triggerType(), outcome.evaluationMode(),
                    outcome.interpretedEventNames(), outcome.interpretedTargetTypes(), outcome.technicalSpecification(),
                    outcome.agentBlueprintPreview(), outcome.requirementCoverage(), outcome.warnings(), outcome.safetyChecks());
            case "triggerType" -> new AlertVerificationOutcome(
                    outcome.decision(), outcome.summary(), outcome.rejectedReason(), outcome.confidence(),
                    outcome.provider(), outcome.model(), outcome.promptVersion(), outcome.requiredSources(), outcome.interpreterType(),
                    outcome.inputModel(), outcome.outputModel(), value, outcome.evaluationMode(),
                    outcome.interpretedEventNames(), outcome.interpretedTargetTypes(), outcome.technicalSpecification(),
                    outcome.agentBlueprintPreview(), outcome.requirementCoverage(), outcome.warnings(), outcome.safetyChecks());
            case "evaluationMode" -> new AlertVerificationOutcome(
                    outcome.decision(), outcome.summary(), outcome.rejectedReason(), outcome.confidence(),
                    outcome.provider(), outcome.model(), outcome.promptVersion(), outcome.requiredSources(), outcome.interpreterType(),
                    outcome.inputModel(), outcome.outputModel(), outcome.triggerType(), value,
                    outcome.interpretedEventNames(), outcome.interpretedTargetTypes(), outcome.technicalSpecification(),
                    outcome.agentBlueprintPreview(), outcome.requirementCoverage(), outcome.warnings(), outcome.safetyChecks());
            case "inputModel" -> new AlertVerificationOutcome(
                    outcome.decision(), outcome.summary(), outcome.rejectedReason(), outcome.confidence(),
                    outcome.provider(), outcome.model(), outcome.promptVersion(), outcome.requiredSources(), outcome.interpreterType(),
                    value, outcome.outputModel(), outcome.triggerType(), outcome.evaluationMode(),
                    outcome.interpretedEventNames(), outcome.interpretedTargetTypes(), outcome.technicalSpecification(),
                    outcome.agentBlueprintPreview(), outcome.requirementCoverage(), outcome.warnings(), outcome.safetyChecks());
            default -> throw new IllegalArgumentException(field);
        };
    }

    private AlertVerificationOutcome validOutcome() {
        return validOutcome(technicalSpecification(), blueprint());
    }

    private AlertVerificationOutcome validOutcome(Map<String, Object> technicalSpecification, Map<String, Object> blueprint) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "Verified.",
                null,
                0.91,
                "provider",
                "model",
                "alert-scheduled-verify-mvp-v1",
                List.of("SERVICE_DATA"),
                "SCHEDULED_INTERPRETER",
                "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "SCHEDULE",
                "SCHEDULED_SNAPSHOT_MATCH",
                List.of(),
                List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                technicalSpecification,
                blueprint,
                coverage(),
                List.of(),
                List.of("No executable code generated."));
    }

    private Map<String, Object> technicalSpecification() {
        return technicalSpecification(validCondition());
    }

    private Map<String, Object> technicalSpecification(Map<String, Object> condition) {
        return new LinkedHashMap<>(Map.ofEntries(
                Map.entry("schemaVersion", "iia.alert.technical-specification/v2"),
                Map.entry("source", "SERVICE_DATA"),
                Map.entry("interpreterType", "SCHEDULED_INTERPRETER"),
                Map.entry("accessMode", "SERVICE_DATA_API_SNAPSHOT"),
                Map.entry("inputModel", "ServiceDataStopPointJourneysV2"),
                Map.entry("outputModel", "AgentOutput.CANDIDATE_SUGGESTION"),
                Map.entry("triggerType", "SCHEDULE"),
                Map.entry("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH"),
                Map.entry("schedule", Map.of("frequencySeconds", 600, "defaulted", true)),
                Map.entry("serviceDataQuery", Map.of(
                        "operation", "POST /v2/stoppointjourneys",
                        "monitoringScope", "EXPLICIT_STOP_POINTS",
                        "stopPoints", List.of("TNPNTS00000000005439"),
                        "requiresAllKnownStopPoints", false)),
                Map.entry("snapshotEvaluation", Map.of(
                        "mode", "COUNT_MATCHING_JOURNEYS",
                        "journeyPath", "stopPointsJourneyDetails[]",
                        "condition", condition,
                        "threshold", Map.of("operator", "GREATER_OR_EQUAL", "value", 2))),
                Map.entry("outputPolicy", Map.of("emit", "ON_MATCH", "includeCount", true, "includeMatchingJourneys", true)),
                Map.entry("deduplicationKeyTemplate", "SERVICE_DATA_SCHEDULED:${alertId}:${queryWindowStart}:${conditionHash}")));
    }

    private Map<String, Object> blueprint() {
        return new LinkedHashMap<>(Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ScheduledServiceDataSnapshotAlertAgent",
                "triggerType", "SCHEDULE",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "targetTypes", List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                "parameters", Map.of(
                        "serviceDataQuery", Map.of("operation", "POST /v2/stoppointjourneys"),
                        "snapshotEvaluation", Map.of("mode", "COUNT_MATCHING_JOURNEYS"),
                        "outputPolicy", Map.of("emit", "ON_MATCH")),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION")));
    }

    private Map<String, Object> blueprintWithCondition(Map<String, Object> condition) {
        Map<String, Object> blueprint = new LinkedHashMap<>(blueprint());
        Map<String, Object> parameters = new LinkedHashMap<>(map(blueprint.get("parameters")));
        parameters.put("snapshotEvaluation", Map.of(
                "mode", "COUNT_MATCHING_JOURNEYS",
                "condition", condition));
        blueprint.put("parameters", parameters);
        return blueprint;
    }

    private Map<String, Object> validCondition() {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVING"));
    }

    private Map<String, Object> condition(Map<String, Object> body) {
        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH");
        condition.putAll(body);
        return condition;
    }

    private Map<String, Object> conditionAnyElement(String path, Map<String, Object> conditions) {
        return condition(Map.of("anyElement", Map.of(
                "path", path,
                "conditions", conditions)));
    }

    private Map<String, Object> leaf(String field, String operator, Object value) {
        return Map.of(
                "field", field,
                "operator", operator,
                "value", value);
    }

    private Map<String, Object> leafValues(String field, String operator, List<?> values) {
        return Map.of(
                "field", field,
                "operator", operator,
                "values", values);
    }

    private Map<String, Object> coverage() {
        return Map.of(
                "requirements", List.of(Map.of(
                        "text", "scheduled condition",
                        "required", true,
                        "mappable", true,
                        "mappedBy", List.of("stopPointsJourneyDetails[].arrivalStatuses[].status"))),
                "allRequiredRequirementsMapped", true);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
