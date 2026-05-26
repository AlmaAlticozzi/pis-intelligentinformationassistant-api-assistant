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
    void acceptsStatelessArrivalEventGenerationTimeWindow() {
        Map<String, Object> condition = temporalArrivalCondition(Map.of(
                "start", "02:00:00",
                "end", "10:00:00",
                "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsCorrelatedNextCallDepartureWindowAndAppliesDefaultTimezone() {
        Map<String, Object> condition = correlatedNextCallsCondition(
                "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                "departureTime",
                Map.of("start", "11:30:00", "end", "12:35:00"));

        AlertVerificationOutcome validated = validator.validate(correlatedNextCallsOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(nextCallTemporalValue(validated.technicalSpecification())).containsEntry("timezone", "Europe/Rome");
        assertThat(nextCallTemporalValueFromBlueprint(validated.agentBlueprintPreview())).containsEntry("timezone", "Europe/Rome");
    }

    @Test
    void acceptsCorrelatedNextCallArrivalWindow() {
        Map<String, Object> condition = correlatedNextCallsCondition(
                "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                "arrivalTime",
                Map.of("start", "11:30:00", "end", "12:35:00", "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(correlatedNextCallsOutcome(
                condition,
                condition,
                "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].arrivalTime"));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(nextCallTemporalValue(validated.technicalSpecification())).containsEntry("timezone", "Europe/Rome");
    }

    @Test
    void rejectsAnyElementOnUnsupportedArrayPath() {
        Map<String, Object> condition = correlatedNextCallsCondition(
                "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[]",
                "departureTime",
                Map.of("start", "11:30", "end", "12:35", "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(correlatedNextCallsOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("anyElement").contains("path is not allowed");
    }

    @Test
    void rejectsAbsoluteFieldInsideAnyElement() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                                "conditions", Map.of("all", List.of(Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].departureTime",
                                        "operator", "LOCAL_TIME_BETWEEN",
                                        "value", Map.of("start", "11:30", "end", "12:35", "timezone", "Europe/Rome"))))))));

        AlertVerificationOutcome validated = validator.validate(correlatedNextCallsOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("relative field is not allowed");
    }

    @Test
    void rejectsFlatNextCallTemporalConditionWithoutCorrelationNode() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.nameLong",
                                "operator", "CONTAINS_NORMALIZED",
                                "value", "Gorla"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].departureTime",
                                "operator", "LOCAL_TIME_BETWEEN",
                                "value", Map.of("start", "11:30", "end", "12:35", "timezone", "Europe/Rome"))));

        AlertVerificationOutcome validated = validator.validate(correlatedNextCallsOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("correlated anyElement");
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
    void rejectsTemporalOperatorWithoutFieldOrSupportedScope() {
        Map<String, Object> invalid = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "operator", "LOCAL_TIME_BETWEEN",
                        "value", Map.of("start", "02:00", "end", "10:00", "timezone", "Europe/Rome"))));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(invalid, invalid));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("field is missing");
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

    @Test
    void rejectsScheduledInterpreterForTemporalCondition() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "02:00", "end", "10:00", "timezone", "Europe/Rome"));
        AlertVerificationOutcome base = temporalOutcome(condition, condition);
        AlertVerificationOutcome scheduled = new AlertVerificationOutcome(
                base.decision(), base.summary(), base.rejectedReason(), base.confidence(), base.provider(), base.model(),
                base.promptVersion(), base.requiredSources(), "SCHEDULED_INTERPRETER", base.inputModel(), base.outputModel(),
                base.triggerType(), base.evaluationMode(), base.interpretedEventNames(), base.interpretedTargetTypes(),
                base.technicalSpecification(), base.agentBlueprintPreview(), base.requirementCoverage(), base.warnings(), base.safetyChecks());

        AlertVerificationOutcome validated = validator.validate(scheduled);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("scheduled evaluation");
    }

    @Test
    void rejectsExternalToolLookupForTemporalCondition() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "02:00", "end", "10:00", "timezone", "Europe/Rome"));
        AlertVerificationOutcome base = temporalOutcome(condition, condition);
        Map<String, Object> technical = new java.util.LinkedHashMap<>(base.technicalSpecification());
        technical.put("toolCalls", List.of("CENTRAL_API"));
        AlertVerificationOutcome externalLookup = new AlertVerificationOutcome(
                base.decision(), base.summary(), base.rejectedReason(), base.confidence(), base.provider(), base.model(),
                base.promptVersion(), base.requiredSources(), base.interpreterType(), base.inputModel(), base.outputModel(),
                base.triggerType(), base.evaluationMode(), base.interpretedEventNames(), base.interpretedTargetTypes(),
                technical, base.agentBlueprintPreview(), base.requirementCoverage(), base.warnings(), base.safetyChecks());

        AlertVerificationOutcome validated = validator.validate(externalLookup);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("tools, APIs or queries");
    }

    @Test
    void rejectsDateRelativePromptEvenWhenLlmReturnsSupportedTemporalTree() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "02:00:00", "end", "10:00:00", "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(
                temporalOutcome(condition, condition),
                "Segnalami se domani ci partono dall'origine autobus da Pisa Centrale");

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("scheduled future or date-relative lookup");
    }

    @Test
    void rejectsUnanchoredTimeWindowPromptEvenWhenLlmInventsAnEventMapping() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "02:00:00", "end", "10:00:00", "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(
                temporalOutcome(condition, condition),
                "Avvisami quando una corsa passa in una fascia oraria");

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("without a supported event field");
    }

    @Test
    void rejectsAbsenceHistoricalAndPredictionPromptsEvenWithValidTemporalTree() {
        Map<String, Object> condition = temporalDepartureCondition(Map.of(
                "start", "02:00:00", "end", "10:00:00", "timezone", "Europe/Rome"));

        AlertVerificationOutcome absence = validator.validate(
                temporalOutcome(condition, condition),
                "Avvisami se domani non parte nessuna corsa da Genova");
        AlertVerificationOutcome history = validator.validate(
                temporalOutcome(condition, condition),
                "Avvisami se negli ultimi 30 minuti non sono arrivati treni");
        AlertVerificationOutcome prediction = validator.validate(
                temporalOutcome(condition, condition),
                "Avvisami quando una corsa sara probabilmente in ritardo domani");

        assertThat(absence.rejectedReason()).contains("absence-of-events");
        assertThat(history.rejectedReason()).contains("historical event evaluation");
        assertThat(prediction.rejectedReason()).contains("prediction");
    }

    @Test
    void rejectsTemporalNextCallsConditionWithoutSameElementStopPointCorrelation() {
        Map<String, Object> condition = correlatedNextCallsCondition(
                "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                "departureTime",
                Map.of("start", "11:30:00", "end", "12:35:00", "timezone", "Europe/Rome"));
        Map<String, Object> uncorrelated = new java.util.LinkedHashMap<>(condition);
        uncorrelated.put("all", List.of(
                Map.of(
                        "field", "payload.ongroundServiceEvent.eventsType",
                        "operator", "CONTAINS",
                        "value", "ARRIVED"),
                Map.of(
                        "field", "payload.ongroundServiceEvent.stopPoint.nameLong",
                        "operator", "EQUALS_NORMALIZED",
                        "value", "Genova"),
                Map.of(
                        "anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                                "conditions", Map.of("all", List.of(Map.of(
                                        "field", "departureTime",
                                        "operator", "LOCAL_TIME_BETWEEN",
                                        "value", Map.of(
                                                "start", "11:30:00",
                                                "end", "12:35:00",
                                                "timezone", "Europe/Rome"))))))));

        AlertVerificationOutcome validated = validator.validate(correlatedNextCallsOutcome(uncorrelated, uncorrelated));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("correlate stopPoint.nameLong");
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

    private Map<String, Object> temporalArrivalCondition(Map<String, Object> temporalValue) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "ARRIVED"),
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

    private Map<String, Object> correlatedNextCallsCondition(
            String path,
            String temporalField,
            Map<String, Object> temporalValue) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "ARRIVED"),
                        Map.of(
                                "field", "payload.ongroundServiceEvent.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova"),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", path,
                                        "conditions", Map.of(
                                                "all", List.of(
                                                        Map.of(
                                                                "field", "stopPoint.nameLong",
                                                                "operator", "EQUALS_NORMALIZED",
                                                                "value", "Gorla"),
                                                        Map.of(
                                                                "field", temporalField,
                                                                "operator", "LOCAL_TIME_BETWEEN",
                                                                "value", temporalValue)))))));
    }

    private AlertVerificationOutcome correlatedNextCallsOutcome(
            Map<String, Object> condition,
            Map<String, Object> blueprintCondition) {
        return correlatedNextCallsOutcome(
                condition,
                blueprintCondition,
                "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].departureTime");
    }

    private AlertVerificationOutcome correlatedNextCallsOutcome(
            Map<String, Object> condition,
            Map<String, Object> blueprintCondition,
            String temporalField) {
        Map<String, Object> coverage = Map.of(
                "requirements", List.of(
                        Map.of(
                                "text", "arrivo evento a Genova",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.ongroundServiceEvent.eventsType", "payload.ongroundServiceEvent.stopPoint.nameLong")),
                        Map.of(
                                "text", "partira a Gorla",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].stopPoint.nameLong")),
                        Map.of(
                                "text", "tra le 11:30 e le 12:35",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of(temporalField))),
                "allRequiredRequirementsMapped", true);
        AlertVerificationOutcome base = outcomeWithConditionAndCoverage(condition, coverage);
        Map<String, Object> blueprint = new java.util.LinkedHashMap<>(base.agentBlueprintPreview());
        blueprint.put("parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", blueprintCondition));
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> nextCallTemporalValue(Map<String, Object> technicalSpecification) {
        Map<String, Object> condition = (Map<String, Object>) technicalSpecification.get("condition");
        List<Map<String, Object>> all = (List<Map<String, Object>>) condition.get("all");
        Map<String, Object> anyElement = (Map<String, Object>) all.get(2).get("anyElement");
        Map<String, Object> conditions = (Map<String, Object>) anyElement.get("conditions");
        List<Map<String, Object>> relativeLeaves = (List<Map<String, Object>>) conditions.get("all");
        return (Map<String, Object>) relativeLeaves.get(1).get("value");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nextCallTemporalValueFromBlueprint(Map<String, Object> blueprint) {
        Map<String, Object> parameters = (Map<String, Object>) blueprint.get("parameters");
        return nextCallTemporalValue(Map.of("condition", parameters.get("condition")));
    }
}
