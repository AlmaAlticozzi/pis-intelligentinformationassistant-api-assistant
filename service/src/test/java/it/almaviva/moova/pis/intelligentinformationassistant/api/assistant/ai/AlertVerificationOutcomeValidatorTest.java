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

    @Test
    void rejectsVerifiedOutcomeWithoutRequirementCoverage() {
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName",
                        "operator", "CONTAINS_NORMALIZED",
                        "value", "1253"))), null));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("requirementCoverage");
    }

    @Test
    void rejectsUnmappedRequiredRequirement() {
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(
                Map.of(
                        "type", "SERVICE_DATA_FIELD_MATCH",
                        "all", List.of(Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName",
                                "operator", "CONTAINS_NORMALIZED",
                                "value", "1253"))),
                Map.of(
                        "requirements", List.of(
                                Map.of(
                                        "text", "treno 1253",
                                        "required", true,
                                        "mappable", true,
                                        "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName"),
                                        "reason", ""),
                                Map.of(
                                        "text", "almeno 10 passeggeri",
                                        "required", true,
                                        "mappable", false,
                                        "mappedBy", List.of(),
                                        "reason", "Passenger count is not available in the ServiceData catalog.")),
                        "allRequiredRequirementsMapped", false)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("not all required requirements");
    }

    @Test
    void rejectsMappedByOutsideCatalog() {
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(
                Map.of(
                        "type", "SERVICE_DATA_FIELD_MATCH",
                        "all", List.of(Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName",
                                "operator", "CONTAINS_NORMALIZED",
                                "value", "1253"))),
                Map.of(
                        "requirements", List.of(Map.of(
                                "text", "almeno 10 passeggeri",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.train.passengerCount"),
                                "reason", "")),
                        "allRequiredRequirementsMapped", true)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("mappedBy");
    }

    @Test
    void rejectsRequirementNotCoveredByTechnicalSpecification() {
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(
                Map.of(
                        "type", "SERVICE_DATA_FIELD_MATCH",
                        "all", List.of(Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName",
                                "operator", "CONTAINS_NORMALIZED",
                                "value", "1253"))),
                Map.of(
                        "requirements", List.of(
                                Map.of(
                                        "text", "treno 1253",
                                        "required", true,
                                        "mappable", true,
                                        "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName"),
                                        "reason", ""),
                                Map.of(
                                        "text", "parte da Genova",
                                        "required", true,
                                        "mappable", true,
                                        "mappedBy", List.of("payload.stopPointJourney.stopPoint.nameLong"),
                                        "reason", "")),
                        "allRequiredRequirementsMapped", true)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("does not cover");
    }

    @Test
    void acceptsStatelessEventGenerationTimeWindowAndAppliesDefaultTimezone() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "02:00:00",
                "end", "10:00:00"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(temporalValue(validated.technicalSpecification())).containsEntry("timezone", "Europe/Rome");
        assertThat(temporalValueFromBlueprint(validated.agentBlueprintPreview())).containsEntry("timezone", "Europe/Rome");
    }

    @Test
    void rejectsTemporalOperatorOnNonTemporalField() {
        AlertVerificationOutcome validated = validator.validate(temporalOutcome(
                Map.of(
                        "type", "SERVICE_DATA_FIELD_MATCH",
                        "all", List.of(Map.of(
                                "field", "payload.ongroundServiceEvent.stopPoint.nameLong",
                                "operator", "LOCAL_TIME_BETWEEN",
                                "value", Map.of("start", "02:00", "end", "10:00", "timezone", "Europe/Rome")))),
                temporalDepartureCondition(Map.of("start", "02:00", "end", "10:00", "timezone", "Europe/Rome"))));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("temporal condition is not supported");
    }

    @Test
    void rejectsInvalidTemporalClockValue() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "25:00",
                "end", "10:00:00",
                "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("HH:mm");
    }

    @Test
    void rejectsInventedTemporalOperator() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.ongroundServiceEvent.eventGenerationTime",
                        "operator", "TIME_WINDOW",
                        "value", Map.of("start", "02:00", "end", "10:00", "timezone", "Europe/Rome"))));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("temporal condition is not supported");
    }

    @Test
    void rejectsTemporalTechnicalConditionMissingFromBlueprint() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "02:00",
                "end", "10:00",
                "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, null));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("agentBlueprintPreview.parameters.condition");
    }

    @Test
    void rejectsTemporalConditionPresentOnlyInBlueprint() {
        Map<String, Object> ordinaryCondition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.ongroundServiceEvent.stopPoint.nameLong",
                        "operator", "EQUALS_NORMALIZED",
                        "value", "Genova")));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(
                ordinaryCondition,
                temporalDepartureCondition(Map.of("start", "02:00", "end", "10:00", "timezone", "Europe/Rome"))));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("not represented in technicalSpecification");
    }

    @Test
    void rejectsActivationPolicyFromTemporalBlueprint() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "02:00",
                "end", "10:00",
                "timezone", "Europe/Rome"));
        AlertVerificationOutcome base = temporalOutcome(condition, condition);
        Map<String, Object> blueprint = new java.util.LinkedHashMap<>(base.agentBlueprintPreview());
        blueprint.put("activationPolicy", Map.of("type", "DAILY_WINDOW"));

        AlertVerificationOutcome validated = validator.validate(new AlertVerificationOutcome(
                base.decision(), base.summary(), base.rejectedReason(), base.confidence(), base.provider(), base.model(),
                base.promptVersion(), base.requiredSources(), base.interpreterType(), base.inputModel(), base.outputModel(),
                base.triggerType(), base.evaluationMode(), base.interpretedEventNames(), base.interpretedTargetTypes(),
                base.technicalSpecification(), blueprint, base.requirementCoverage(), base.warnings(), base.safetyChecks()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("activation policy or scheduler");
    }

    private AlertVerificationOutcome outcomeWithCondition(Map<String, Object> condition) {
        return outcomeWithConditionAndCoverage(
                condition,
                Map.of(
                        "requirements", List.of(Map.of(
                                "text", "passing type transit",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].passingType"),
                                "reason", "")),
                        "allRequiredRequirementsMapped", true));
    }

    private AlertVerificationOutcome outcomeWithConditionAndCoverage(
            Map<String, Object> condition,
            Map<String, Object> requirementCoverage) {
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
                requirementCoverage,
                List.of(),
                List.of(
                        "No executable code generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
    }

    private Map<String, Object> temporalDepartureCondition(Map<String, Object> temporalValue) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "DEPARTED"),
                        Map.of(
                                "field", "payload.ongroundServiceEvent.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova"),
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventGenerationTime",
                                "operator", "LOCAL_TIME_BETWEEN",
                                "value", temporalValue)));
    }

    private AlertVerificationOutcome temporalOutcome(Map<String, Object> condition, Map<String, Object> blueprintCondition) {
        Map<String, Object> coverage = Map.of(
                "requirements", List.of(
                        Map.of(
                                "text", "partenza evento",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.ongroundServiceEvent.eventsType")),
                        Map.of(
                                "text", "Genova",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.ongroundServiceEvent.stopPoint.nameLong")),
                        Map.of(
                                "text", "tra le 02:00 e le 10:00",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.ongroundServiceEvent.eventGenerationTime"))),
                "allRequiredRequirementsMapped", true);
        AlertVerificationOutcome base = outcomeWithConditionAndCoverage(condition, coverage);
        Map<String, Object> blueprint = new java.util.LinkedHashMap<>(base.agentBlueprintPreview());
        if (blueprintCondition != null) {
            blueprint.put("parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", blueprintCondition));
        }
        return new AlertVerificationOutcome(
                base.decision(), base.summary(), base.rejectedReason(), base.confidence(), base.provider(), base.model(),
                base.promptVersion(), base.requiredSources(), base.interpreterType(), base.inputModel(), base.outputModel(),
                base.triggerType(), base.evaluationMode(), base.interpretedEventNames(), base.interpretedTargetTypes(),
                base.technicalSpecification(), blueprint, base.requirementCoverage(), base.warnings(), base.safetyChecks());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> temporalValue(Map<String, Object> technicalSpecification) {
        Map<String, Object> condition = (Map<String, Object>) technicalSpecification.get("condition");
        List<Map<String, Object>> all = (List<Map<String, Object>>) condition.get("all");
        return (Map<String, Object>) all.get(2).get("value");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> temporalValueFromBlueprint(Map<String, Object> blueprint) {
        Map<String, Object> parameters = (Map<String, Object>) blueprint.get("parameters");
        return temporalValue(Map.of("condition", parameters.get("condition")));
    }
}
