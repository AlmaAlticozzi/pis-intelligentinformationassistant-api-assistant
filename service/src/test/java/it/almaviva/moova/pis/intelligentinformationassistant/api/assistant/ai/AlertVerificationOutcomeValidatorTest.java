package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AlertVerificationOutcomeValidatorTest {

    private static final String RHO_FIERAMILANO_ID = "TNPNTS00000000005467";
    private static final String MALPENSA_T1_ID = "TNPNTS00000000000028";
    private static final String MALPENSA_T2_ID = "TNPNTS00000000000029";

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
    void acceptsEqualsOnKnownOngroundStopPointId() {
        String field = "payload.ongroundServiceEvent.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "EQUALS",
                        "value", RHO_FIERAMILANO_ID))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsInOnKnownMalpensaIds() {
        String field = "payload.stopPointJourney.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "IN",
                        "values", List.of(MALPENSA_T1_ID, MALPENSA_T2_ID)))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsNotInOnKnownResolvedStopPointIds() {
        String field = "payload.stopPointJourney.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "NOT_IN",
                        "values", List.of(MALPENSA_T1_ID, MALPENSA_T2_ID)))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsNotInOnKnownOngroundStopPointIds() {
        String field = "payload.ongroundServiceEvent.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "NOT_IN",
                        "values", List.of(MALPENSA_T1_ID, MALPENSA_T2_ID)))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsEmptyNotInValuesOnStopPointId() {
        String field = "payload.stopPointJourney.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "NOT_IN",
                        "values", List.of()))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("requires a non-empty values array");
    }

    @Test
    void rejectsContainsNormalizedOnStopPointId() {
        String field = "payload.ongroundServiceEvent.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "CONTAINS_NORMALIZED",
                        "value", RHO_FIERAMILANO_ID))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("operator is not allowed");
    }

    @Test
    void rejectsUnknownStopPointId() {
        String field = "payload.ongroundServiceEvent.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "EQUALS",
                        "value", "TNPNTS99999999999999"))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason())
                .contains("Unsupported or unknown stopPoint id in technicalSpecification: TNPNTS99999999999999");
    }

    @Test
    void rejectsEmptyInValuesOnStopPointId() {
        String field = "payload.ongroundServiceEvent.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "IN",
                        "values", List.of()))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("requires a non-empty values array");
    }

    @Test
    void stillAcceptsFallbackNameLongContainsNormalized() {
        String field = "payload.ongroundServiceEvent.stopPoint.nameLong";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "CONTAINS_NORMALIZED",
                        "value", "Genova Nervi"))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void validatorRejectsResolvedLocationUsingNameLongIfYouImplementedBlockingRule() {
        String field = "payload.ongroundServiceEvent.stopPoint.nameLong";
        AlertVerificationOutcome base = outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "CONTAINS_NORMALIZED",
                        "value", "Rho Fieramilano"))), coverageFor(field));
        Map<String, Object> technicalSpecification = new java.util.LinkedHashMap<>(base.technicalSpecification());
        technicalSpecification.put("locationResolution", Map.of(
                "resolutions", List.of(Map.of(
                        "rawText", "Rho Fieramilano",
                        "status", "RESOLVED",
                        "selectedPointIds", List.of(RHO_FIERAMILANO_ID)))));

        AlertVerificationOutcome validated = validator.validate(new AlertVerificationOutcome(
                base.decision(),
                base.summary(),
                base.rejectedReason(),
                base.confidence(),
                base.provider(),
                base.model(),
                base.promptVersion(),
                base.requiredSources(),
                base.interpreterType(),
                base.inputModel(),
                base.outputModel(),
                base.triggerType(),
                base.evaluationMode(),
                base.interpretedEventNames(),
                base.interpretedTargetTypes(),
                technicalSpecification,
                base.agentBlueprintPreview(),
                base.requirementCoverage(),
                base.warnings(),
                base.safetyChecks()));

        // TODO: make this a hard rejection once locationResolution is always persisted into technicalSpecification.
        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(validated.warnings())
                .contains("locationResolution contains resolved mentions but technicalSpecification does not use stopPoint.id.");
    }

    @Test
    void noLocationPromptWithoutLocationResolutionStillValidIfOtherConditionsAreValid() {
        AlertVerificationOutcome validated = validator.validate(outcomeWithCondition(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].passingType",
                        "operator", "EQUALS",
                        "value", "TRANSIT")))));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(validated.technicalSpecification()).doesNotContainKey("locationResolution");
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
    void acceptsLocalDayOfWeekInWithTuesday() {
        Map<String, Object> condition = temporalDepartureDayCondition("LOCAL_DAY_OF_WEEK_IN", Map.of(
                "days", List.of("TUESDAY"),
                "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(temporalValue(validated.technicalSpecification()))
                .containsEntry("days", List.of("TUESDAY"))
                .containsEntry("timezone", "Europe/Rome");
    }

    @Test
    void acceptsLocalDayOfWeekInWithWeekendDays() {
        Map<String, Object> condition = temporalDepartureDayCondition("LOCAL_DAY_OF_WEEK_IN", Map.of(
                "days", List.of("SATURDAY", "SUNDAY"),
                "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsLocalDayOfWeekNotInWithWeekendDays() {
        Map<String, Object> condition = temporalDepartureDayCondition("LOCAL_DAY_OF_WEEK_NOT_IN", Map.of(
                "days", List.of("SATURDAY", "SUNDAY"),
                "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsInvalidLocalDayOfWeek() {
        Map<String, Object> condition = temporalDepartureDayCondition("LOCAL_DAY_OF_WEEK_IN", Map.of(
                "days", List.of("MARTEDI"),
                "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("day is not valid");
    }

    @Test
    void rejectsInvalidLocalDayOfWeekTimezone() {
        Map<String, Object> condition = temporalDepartureDayCondition("LOCAL_DAY_OF_WEEK_IN", Map.of(
                "days", List.of("TUESDAY"),
                "timezone", "Europe/NotAZone"));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("timezone is not a valid zone id");
    }

    @Test
    void rejectsLocalDayOfWeekOnNonWhitelistedField() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName",
                        "operator", "LOCAL_DAY_OF_WEEK_IN",
                        "value", Map.of("days", List.of("TUESDAY"), "timezone", "Europe/Rome"))));

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("temporal condition is not supported");
    }

    @Test
    void rejectsCorrelatedArrayTemporalPathWithoutAnyElement() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova P.P"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.departureTime",
                                "operator", "LOCAL_DAY_OF_WEEK_IN",
                                "value", Map.of("days", List.of("SATURDAY", "SUNDAY"), "timezone", "Europe/Rome"))));

        AlertVerificationOutcome validated = validator.validate(arrayTemporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("anyElement");
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
                "payload.stopPointJourney.stopPointsJourneyDetails[].unknownCalls[]",
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
    void acceptsNestedChildArrayAnyElementPreservingStopPointJourneyDetailsCorrelation() {
        Map<String, Object> condition = nestedTransitTuesdayCondition();

        AlertVerificationOutcome validated = validator.validate(nestedTransitOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsFlattenedSiblingAnyElementsAcrossStopPointJourneyDetailsAndChildArray() {
        Map<String, Object> condition = flattenedTransitTuesdayCondition();

        AlertVerificationOutcome validated = validator.validate(nestedTransitOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("nested anyElement is required")
                .contains("same stopPointsJourneyDetails element");
    }

    @Test
    void rejectsExistsOnNestedTransitPassingTime() {
        Map<String, Object> condition = nestedTransitPassingTimeExistsCondition();

        AlertVerificationOutcome validated = validator.validate(nestedTransitOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("operator is not allowed for relative field passingTime: EXISTS");
    }

    @Test
    void acceptsNestedWeekdayOriginDepartureWithTransitStopWithoutPassingTime() {
        Map<String, Object> condition = nestedWeekdayOriginTransitStopCondition();

        AlertVerificationOutcome validated = validator.validate(nestedWeekdayTransitOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
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
    void normalizesNaturalHourTemporalBoundaries() {
        Map<String, Object> value = new java.util.LinkedHashMap<>();
        value.put("start", "2");
        value.put("end", "10");
        Map<String, Object> condition = temporalDepartureCondition(value);

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(temporalValue(validated.technicalSpecification()))
                .containsEntry("start", "02:00:00")
                .containsEntry("end", "10:00:00")
                .containsEntry("timezone", "Europe/Rome");
        assertThat(temporalValueFromBlueprint(validated.agentBlueprintPreview()))
                .containsEntry("start", "02:00:00")
                .containsEntry("end", "10:00:00")
                .containsEntry("timezone", "Europe/Rome");
    }

    @Test
    void normalizesNaturalMinuteBoundaryAndPreservesSecondPrecision() {
        Map<String, Object> value = new java.util.LinkedHashMap<>();
        value.put("start", "2:30");
        value.put("end", "02:30:15");
        value.put("timezone", "Europe/Rome");
        Map<String, Object> condition = temporalDepartureCondition(value);

        AlertVerificationOutcome validated = validator.validate(temporalOutcome(condition, condition));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(temporalValue(validated.technicalSpecification()))
                .containsEntry("start", "02:30:00")
                .containsEntry("end", "02:30:15");
    }

    @Test
    void rejectsOutOfRangeNaturalHourAndMinuteValues() {
        Map<String, Object> hourCondition = temporalDepartureCondition(Map.of(
                "start", "25", "end", "10", "timezone", "Europe/Rome"));
        Map<String, Object> minuteCondition = temporalDepartureCondition(Map.of(
                "start", "2", "end", "10:99", "timezone", "Europe/Rome"));

        AlertVerificationOutcome invalidHour = validator.validate(temporalOutcome(hourCondition, hourCondition));
        AlertVerificationOutcome invalidMinute = validator.validate(temporalOutcome(minuteCondition, minuteCondition));

        assertThat(invalidHour.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(invalidMinute.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
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
                "start", "02:00:00",
                "end", "10:00:00",
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
                temporalDepartureCondition(Map.of("start", "02:00:00", "end", "10:00:00", "timezone", "Europe/Rome"))));

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
    void rejectsAlertActivationWindowEvenWhenLlmReturnsSupportedTemporalTree() {
        Map<String, Object> condition = temporalArrivalCondition(Map.of(
                "start", "11:30:00", "end", "12:35:00", "timezone", "Europe/Rome"));

        AlertVerificationOutcome validated = validator.validate(
                temporalOutcome(condition, condition),
                "Attiva questo alert solo tra le 11:30 e le 12:35 quando una corsa arriva a Genova.");

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).isEqualTo(
                "Activation time windows are not supported in the current Alert Verify MVP. "
                        + "Only stateless temporal predicates evaluated on ServiceData event timestamps are supported.");
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

    @Test
    void acceptsEqualPlatformOnTimetabledDeparturePlatform() {
        assertPlatformConditionIsVerified(platformLeaf(
                "timetabledDeparturePlatform.dsc",
                "EQUAL_PLATFORM",
                "value",
                "1"));
    }

    @Test
    void acceptsInPlatformsOnActualDeparturePlatform() {
        assertPlatformConditionIsVerified(platformLeaf(
                "actualDeparturePlatform.platform.dsc",
                "IN_PLATFORMS",
                "values",
                List.of("1", "4")));
    }

    @Test
    void acceptsNotInPlatformsOnTimetabledArrivalPlatform() {
        assertPlatformConditionIsVerified(platformLeaf(
                "timetabledArrivalPlatform.dsc",
                "NOT_IN_PLATFORMS",
                "values",
                List.of("11", "12")));
    }

    @Test
    void acceptsPlatformNotEqualsFieldOnDeparturePlatforms() {
        assertPlatformConditionIsVerified(platformFieldComparisonLeaf(
                "timetabledDeparturePlatform.dsc",
                "PLATFORM_NOT_EQUALS_FIELD",
                "actualDeparturePlatform.platform.dsc"));
    }

    @Test
    void acceptsPlatformNotEqualsFieldOnArrivalPlatforms() {
        assertPlatformConditionIsVerified(platformFieldComparisonLeaf(
                "timetabledArrivalPlatform.dsc",
                "PLATFORM_NOT_EQUALS_FIELD",
                "actualArrivalPlatform.platform.dsc"));
    }

    @Test
    void acceptsPlatformEqualsFieldOnDescriptionPlatforms() {
        assertPlatformConditionIsVerified(platformFieldComparisonLeaf(
                "previousDeparturePlatform.platform.dsc",
                "PLATFORM_EQUALS_FIELD",
                "actualDeparturePlatform.platform.dsc"));
    }

    @Test
    void rejectsPlatformNotEqualsFieldWithoutOtherField() {
        assertPlatformConditionIsRejected(
                Map.of(
                        "field", "timetabledDeparturePlatform.dsc",
                        "operator", "PLATFORM_NOT_EQUALS_FIELD"),
                "otherField must be a non-empty string");
    }

    @Test
    void rejectsPlatformNotEqualsFieldWithStopPointIdOtherField() {
        assertPlatformConditionIsRejected(platformFieldComparisonLeaf(
                        "timetabledDeparturePlatform.dsc",
                        "PLATFORM_NOT_EQUALS_FIELD",
                        "timetabledCallStart.stopPoint.id"),
                "otherField must be a whitelisted platform description field");
    }

    @Test
    void rejectsPlatformNotEqualsFieldOnNonPlatformField() {
        assertPlatformConditionIsRejected(platformFieldComparisonLeaf(
                        "passingType",
                        "PLATFORM_NOT_EQUALS_FIELD",
                        "actualDeparturePlatform.platform.dsc"),
                "operator is not allowed");
    }

    @Test
    void rejectsPlatformNotEqualsFieldOnTechnicalIdField() {
        assertPlatformConditionIsRejected(platformFieldComparisonLeaf(
                        "timetabledDeparturePlatform.id",
                        "PLATFORM_NOT_EQUALS_FIELD",
                        "actualDeparturePlatform.platform.dsc"),
                "platform technical id field cannot be used");
    }

    @Test
    void rejectsPlatformNotEqualsFieldWithTechnicalIdOtherField() {
        assertPlatformConditionIsRejected(platformFieldComparisonLeaf(
                        "timetabledDeparturePlatform.dsc",
                        "PLATFORM_NOT_EQUALS_FIELD",
                        "actualDeparturePlatform.platform.id"),
                "otherField cannot be a platform technical id field");
    }

    @Test
    void acceptsCurrentArrivalStopEventAndPlatformWithCompleteRequirementCoverage() {
        String stopPointField = "payload.stopPointJourney.stopPoint.id";
        String eventField = "payload.ongroundServiceEvent.eventsType";
        String platformField =
                "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledArrivalPlatform.dsc";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", stopPointField,
                                "operator", "IN",
                                "values", List.of(RHO_FIERAMILANO_ID)),
                        Map.of(
                                "field", eventField,
                                "operator", "CONTAINS",
                                "value", "ARRIVED"),
                        Map.of("anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of(
                                        "field", "timetabledArrivalPlatform.dsc",
                                        "operator", "EQUAL_PLATFORM",
                                        "value", "1"))))),
                coverageFor(stopPointField, eventField, platformField)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsCurrentDeparturePlatformChangeEventWithStructuralComparison() {
        String eventField = "payload.ongroundServiceEvent.eventsType";
        String timetabledField =
                "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc";
        String actualField =
                "payload.stopPointJourney.stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", eventField,
                                "operator", "CONTAINS",
                                "value", "DEPARTURE_PLATFORM_CHANGED"),
                        Map.of("anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", platformFieldComparisonLeaf(
                                        "timetabledDeparturePlatform.dsc",
                                        "PLATFORM_NOT_EQUALS_FIELD",
                                        "actualDeparturePlatform.platform.dsc"))))),
                coverageFor(eventField, timetabledField, actualField)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsUndirectedMovedPlatformEventWithDepartureAndArrivalBranches() {
        String eventField = "payload.ongroundServiceEvent.eventsType";
        String detailsPath = "payload.stopPointJourney.stopPointsJourneyDetails[]";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", eventField,
                                "operator", "CONTAINS_ANY",
                                "values", List.of("DEPARTURE_PLATFORM_CHANGED", "ARRIVAL_PLATFORM_CHANGED")),
                        Map.of("anyElement", Map.of(
                                "path", detailsPath,
                                "conditions", Map.of("any", List.of(
                                        Map.of("all", List.of(
                                                platformLeaf(
                                                        "previousDeparturePlatform.platform.dsc",
                                                        "EQUAL_PLATFORM",
                                                        "value",
                                                        "5"),
                                                platformLeaf(
                                                        "actualDeparturePlatform.platform.dsc",
                                                        "IN_PLATFORMS",
                                                        "values",
                                                        List.of("7", "8")))),
                                        Map.of("all", List.of(
                                                platformLeaf(
                                                        "previousArrivalPlatform.platform.dsc",
                                                        "EQUAL_PLATFORM",
                                                        "value",
                                                        "5"),
                                                platformLeaf(
                                                        "actualArrivalPlatform.platform.dsc",
                                                        "IN_PLATFORMS",
                                                        "values",
                                                        List.of("7", "8")))))))))),
                coverageFor(
                        eventField,
                        detailsPath + ".previousDeparturePlatform.platform.dsc",
                        detailsPath + ".actualDeparturePlatform.platform.dsc",
                        detailsPath + ".previousArrivalPlatform.platform.dsc",
                        detailsPath + ".actualArrivalPlatform.platform.dsc")));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsEqualPlatformWithoutValue() {
        assertPlatformConditionIsRejected(
                Map.of("field", "timetabledDeparturePlatform.dsc", "operator", "EQUAL_PLATFORM"),
                "requires value");
    }

    @Test
    void rejectsInPlatformsWithoutValues() {
        assertPlatformConditionIsRejected(
                Map.of("field", "actualDeparturePlatform.platform.dsc", "operator", "IN_PLATFORMS"),
                "requires a non-empty values array");
    }

    @Test
    void rejectsInPlatformsWithEmptyValues() {
        assertPlatformConditionIsRejected(platformLeaf(
                        "actualDeparturePlatform.platform.dsc",
                        "IN_PLATFORMS",
                        "values",
                        List.of()),
                "requires a non-empty values array");
    }

    @Test
    void rejectsEqualPlatformOnStopPointId() {
        String field = "payload.ongroundServiceEvent.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "EQUAL_PLATFORM",
                        "value", "1"))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("operator is not allowed");
    }

    @Test
    void rejectsContainsOnPlatformDescription() {
        assertPlatformConditionIsRejected(platformLeaf(
                        "timetabledDeparturePlatform.dsc",
                        "CONTAINS",
                        "value",
                        "1"),
                "operator is not allowed");
    }

    @Test
    void rejectsEqualsNormalizedOnActualArrivalDisplayPlatformTechnicalId() {
        assertPlatformConditionIsRejected(platformLeaf(
                        "actualArrivalPlatform.displayPlatform.id",
                        "EQUALS_NORMALIZED",
                        "value",
                        "1"),
                "platform technical id field cannot be used for user platform matching");
    }

    @Test
    void rejectsEqualsOnActualDepartureDisplayPlatformTechnicalId() {
        assertPlatformConditionIsRejected(platformLeaf(
                        "actualDeparturePlatform.displayPlatform.id",
                        "EQUALS",
                        "value",
                        "1"),
                "platform technical id field cannot be used for user platform matching");
    }

    @Test
    void rejectsContainsNormalizedOnActualArrivalPlatformTechnicalId() {
        assertPlatformConditionIsRejected(platformLeaf(
                        "actualArrivalPlatform.platform.id",
                        "CONTAINS_NORMALIZED",
                        "value",
                        "1"),
                "platform technical id field cannot be used for user platform matching");
    }

    @Test
    void rejectsInOnPreviousDeparturePlatformTechnicalId() {
        assertPlatformConditionIsRejected(platformLeaf(
                        "previousDeparturePlatform.platform.id",
                        "IN",
                        "values",
                        List.of("1", "4")),
                "platform technical id field cannot be used for user platform matching");
    }

    @Test
    void acceptsEqualPlatformOnActualArrivalDisplayPlatformDescription() {
        assertPlatformConditionIsVerified(platformLeaf(
                "actualArrivalPlatform.displayPlatform.dsc",
                "EQUAL_PLATFORM",
                "value",
                "1"));
    }

    @Test
    void acceptsAdvancedPlatformNumericOperatorsOnDescriptionField() {
        for (Map<String, Object> leaf : List.of(
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_THAN", "value", 5),
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_OR_EQUAL", "value", 5),
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_LESS_THAN", "value", 5),
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_LESS_OR_EQUAL", "value", 5),
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3, "max", 8)),
                platformValuelessLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_EVEN"),
                platformValuelessLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_ODD"),
                platformValuelessLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_DOUBLE_DIGIT"),
                platformValuelessLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_HAS_LETTER_SUFFIX"),
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_MULTIPLE_OF", "value", 3))) {
            assertPlatformConditionWithEventIsVerified("DEPARTED", leaf);
        }
    }

    @Test
    void acceptsAdvancedPlatformNumericOperatorsBoundToCoherentCurrentEvents() {
        assertPlatformConditionWithEventIsVerified("DEPARTING",
                platformLeaf("actualDeparturePlatform.platform.dsc", "PLATFORM_NUMBER_GREATER_THAN", "value", 5));
        assertPlatformConditionWithEventIsVerified("DEPARTED",
                platformValuelessLeaf("actualDeparturePlatform.platform.dsc", "PLATFORM_NUMBER_EVEN"));
        assertPlatformConditionWithEventIsVerified("ARRIVED",
                platformLeaf("actualArrivalPlatform.platform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3, "max", 8)));
        assertPlatformConditionWithEventIsVerified("DEPARTED",
                platformValuelessLeaf("actualDeparturePlatform.platform.dsc", "PLATFORM_HAS_LETTER_SUFFIX"));
        assertPlatformConditionWithEventIsVerified("DEPARTED",
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_THAN", "value", 5));
    }

    @Test
    void acceptsResolvedStopPointWithCompletedDepartureAndEvenActualPlatform() {
        String stopPointField = "payload.stopPointJourney.stopPoint.id";
        String eventField = "payload.ongroundServiceEvent.eventsType";
        String detailsPath = "payload.stopPointJourney.stopPointsJourneyDetails[]";
        String platformField = detailsPath + ".actualDeparturePlatform.platform.dsc";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("field", stopPointField, "operator", "IN",
                                "values", List.of("TNPNTS00000000005467")),
                        Map.of("field", eventField, "operator", "CONTAINS", "value", "DEPARTED"),
                        Map.of("anyElement", Map.of(
                                "path", detailsPath,
                                "conditions", platformValuelessLeaf(
                                        "actualDeparturePlatform.platform.dsc", "PLATFORM_NUMBER_EVEN"))))),
                coverageFor(stopPointField, eventField, platformField)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsAdvancedPlatformNumericOperatorsWithoutCurrentEventBinding() {
        for (Map<String, Object> leaf : List.of(
                platformLeaf("actualDeparturePlatform.platform.dsc", "PLATFORM_NUMBER_GREATER_THAN", "value", 5),
                platformLeaf("actualArrivalPlatform.platform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3, "max", 8)),
                platformValuelessLeaf("actualDeparturePlatform.platform.dsc", "PLATFORM_HAS_LETTER_SUFFIX"))) {
            assertPlatformConditionIsRejected(leaf,
                    "Platform numeric/property predicates must include payload.ongroundServiceEvent.eventsType "
                            + "to bind the predicate to a current ServiceData event.");
        }
    }

    @Test
    void rejectsInvalidAdvancedPlatformNumericShapes() {
        assertPlatformConditionIsRejected(
                platformValuelessLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_THAN"),
                "requires value");
        assertPlatformConditionIsRejected(
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("max", 8)),
                "requires value with numeric min and max");
        assertPlatformConditionIsRejected(
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3)),
                "requires value with numeric min and max");
        assertPlatformConditionIsRejected(
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 8, "max", 3)),
                "requires min less than or equal to max");
        assertPlatformConditionIsRejected(
                platformLeaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_MULTIPLE_OF", "value", 0),
                "greater than 0");
        assertPlatformConditionIsRejected(
                platformLeaf("timetabledDeparturePlatform.id", "PLATFORM_NUMBER_GREATER_THAN", "value", 5),
                "platform technical id field cannot be used");
        assertPlatformConditionIsRejected(
                platformLeaf("passingType", "PLATFORM_NUMBER_GREATER_THAN", "value", 5),
                "operator is not allowed");
    }

    @Test
    void rejectsBayPlatformPromptWithCatalogReason() {
        AlertVerificationOutcome validated = validator.validate(
                outcomeWithCondition(Map.of(
                        "type", "SERVICE_DATA_FIELD_MATCH",
                        "all", List.of(Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].passingType",
                                "operator", "EQUALS",
                                "value", "TRANSIT")))),
                "Avvertimi quando una corsa parte da un binario tronco");

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason())
                .isEqualTo("Bay/terminal/dead-end platform is not available in the ServiceData Capability Catalog.");
    }

    @Test
    void acceptsInOnKnownStopPointIdAfterPlatformTechnicalIdBlocking() {
        String field = "payload.ongroundServiceEvent.stopPoint.id";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "IN",
                        "values", List.of("TNPNTS00000000000009")))), coverageFor(field)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertPlatformConditionIsVerified(Map<String, Object> leaf) {
        AlertVerificationOutcome validated = validator.validate(platformOutcome(leaf));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertPlatformConditionIsRejected(Map<String, Object> leaf, String reason) {
        AlertVerificationOutcome validated = validator.validate(platformOutcome(leaf));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reason);
    }

    private AlertVerificationOutcome platformOutcome(Map<String, Object> leaf) {
        String field = "payload.stopPointJourney.stopPointsJourneyDetails[]." + leaf.get("field");
        return outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of("all", List.of(leaf)))))), coverageFor(field));
    }

    private void assertPlatformConditionWithEventIsVerified(String eventType, Map<String, Object> leaf) {
        String detailsPath = "payload.stopPointJourney.stopPointsJourneyDetails[]";
        String platformField = detailsPath + "." + leaf.get("field");
        String eventField = "payload.ongroundServiceEvent.eventsType";
        AlertVerificationOutcome validated = validator.validate(outcomeWithConditionAndCoverage(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("field", eventField, "operator", "CONTAINS", "value", eventType),
                        Map.of("anyElement", Map.of(
                                "path", detailsPath,
                                "conditions", leaf)))),
                coverageFor(eventField, platformField)));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private Map<String, Object> platformLeaf(String field, String operator, String valueKey, Object value) {
        return Map.of("field", field, "operator", operator, valueKey, value);
    }

    private Map<String, Object> platformValuelessLeaf(String field, String operator) {
        return Map.of("field", field, "operator", operator);
    }

    private Map<String, Object> platformFieldComparisonLeaf(String field, String operator, String otherField) {
        return Map.of("field", field, "operator", operator, "otherField", otherField);
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

    private Map<String, Object> coverageFor(String field) {
        return coverageFor(new String[]{field});
    }

    private Map<String, Object> coverageFor(String... fields) {
        return Map.of(
                "requirements", java.util.Arrays.stream(fields)
                        .map(field -> Map.of(
                                "text", field,
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of(field),
                                "reason", ""))
                        .toList(),
                "allRequiredRequirementsMapped", true);
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

    private Map<String, Object> temporalDepartureDayCondition(String operator, Map<String, Object> temporalValue) {
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
                                "operator", operator,
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

    private AlertVerificationOutcome arrayTemporalOutcome(Map<String, Object> condition, Map<String, Object> blueprintCondition) {
        Map<String, Object> coverage = Map.of(
                "requirements", List.of(
                        Map.of(
                                "text", "Genova P.P",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameLong"),
                                "reason", ""),
                        Map.of(
                                "text", "weekend",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.departureTime"),
                                "reason", "")),
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

    private Map<String, Object> nestedTransitTuesdayCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("all", List.of(
                                Map.of(
                                        "field", "timetabledCallStart.stopPoint.nameLong",
                                        "operator", "EQUALS_NORMALIZED",
                                        "value", "Genova P.P"),
                                Map.of(
                                        "anyElement", Map.of(
                                                "path", "nextTransitCalls[]",
                                                "conditions", Map.of("all", List.of(
                                                        Map.of(
                                                                "field", "stopPoint.nameLong",
                                                                "operator", "CONTAINS_NORMALIZED",
                                                                "value", "Genova Nervi"),
                                                        Map.of(
                                                                "field", "passingTime",
                                                                "operator", "LOCAL_DAY_OF_WEEK_IN",
                                                                "value", Map.of(
                                                                        "days", List.of("TUESDAY"),
                                                                        "timezone", "Europe/Rome"))))))))));
    }

    private Map<String, Object> flattenedTransitTuesdayCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of("all", List.of(Map.of(
                                                "field", "timetabledCallStart.stopPoint.nameLong",
                                                "operator", "EQUALS_NORMALIZED",
                                                "value", "Genova P.P"))))),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[]",
                                        "conditions", Map.of("all", List.of(
                                                Map.of(
                                                        "field", "stopPoint.nameLong",
                                                        "operator", "CONTAINS_NORMALIZED",
                                                        "value", "Genova Nervi"),
                                                Map.of(
                                                        "field", "passingTime",
                                                        "operator", "LOCAL_DAY_OF_WEEK_IN",
                                                        "value", Map.of(
                                                                "days", List.of("TUESDAY"),
                                                                "timezone", "Europe/Rome"))))))));
    }

    private Map<String, Object> nestedTransitPassingTimeExistsCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("all", List.of(
                                Map.of(
                                        "field", "timetabledCallStart.stopPoint.nameLong",
                                        "operator", "EQUALS_NORMALIZED",
                                        "value", "Genova P.P"),
                                Map.of(
                                        "anyElement", Map.of(
                                                "path", "nextTransitCalls[]",
                                                "conditions", Map.of("all", List.of(
                                                        Map.of(
                                                                "field", "stopPoint.nameLong",
                                                                "operator", "CONTAINS_NORMALIZED",
                                                                "value", "Genova Nervi"),
                                                        Map.of(
                                                                "field", "passingTime",
                                                                "operator", "EXISTS")))))))));
    }

    private Map<String, Object> nestedWeekdayOriginTransitStopCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("all", List.of(
                                Map.of(
                                        "field", "timetabledCallStart.stopPoint.nameLong",
                                        "operator", "EQUALS_NORMALIZED",
                                        "value", "Genova P.P"),
                                Map.of(
                                        "field", "timetabledCallStart.departureTime",
                                        "operator", "LOCAL_DAY_OF_WEEK_NOT_IN",
                                        "value", Map.of(
                                                "days", List.of("SATURDAY", "SUNDAY"),
                                                "timezone", "Europe/Rome")),
                                Map.of(
                                        "anyElement", Map.of(
                                                "path", "nextTransitCalls[]",
                                                "conditions", Map.of("all", List.of(Map.of(
                                                        "field", "stopPoint.nameLong",
                                                        "operator", "CONTAINS_NORMALIZED",
                                                        "value", "Genova Nervi")))))))));
    }

    private AlertVerificationOutcome nestedTransitOutcome(Map<String, Object> condition, Map<String, Object> blueprintCondition) {
        Map<String, Object> coverage = Map.of(
                "requirements", List.of(
                        Map.of(
                                "text", "partenza da Genova P.P",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameLong"),
                                "reason", ""),
                        Map.of(
                                "text", "transito a Genova Nervi il martedi",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of(
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.nameLong",
                                        "payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].passingTime"),
                                "reason", "")),
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

    private AlertVerificationOutcome nestedWeekdayTransitOutcome(Map<String, Object> condition, Map<String, Object> blueprintCondition) {
        Map<String, Object> coverage = Map.of(
                "requirements", List.of(
                        Map.of(
                                "text", "partenza da Genova P.P",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameLong"),
                                "reason", ""),
                        Map.of(
                                "text", "nei feriali",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.departureTime"),
                                "reason", ""),
                        Map.of(
                                "text", "transito a Genova Nervi",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("payload.stopPointJourney.stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.nameLong"),
                                "reason", "")),
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
