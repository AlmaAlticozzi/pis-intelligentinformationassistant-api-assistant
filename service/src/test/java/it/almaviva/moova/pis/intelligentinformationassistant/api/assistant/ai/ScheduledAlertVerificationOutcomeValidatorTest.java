package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataApiQueryContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationResolutionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertVerificationOutcomeValidatorTest {

    private static final String GORLA = "TNPNTS00000000000001";
    private static final String PERO = "TNPNTS00000000000002";
    private static final String GARIBALDI = "TNPNTS00000000000003";
    private static final String BUONARROTI = "TNPNTS00000000000004";
    private static final String TRE_TORRI = "TNPNTS00000000000005";
    private static final String VAREDO = "TNPNTS00000000000006";
    private static final String PALAZZOLO = "TNPNTS00000000000007";
    private static final String INVENTED = "TNPNTS99999999999999";

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
    void acceptsArrivalPlatformEquality() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("timetabledArrivalPlatform.dsc", "EQUAL_PLATFORM", "1")));
    }

    @Test
    void acceptsPlatformNumberGreaterThan() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_THAN", 2)));
    }

    @Test
    void acceptsPlatformNumberBetween() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", Map.of("min", 2, "max", 5))));
    }

    @Test
    void acceptsPlatformNumberEven() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("field", "timetabledDeparturePlatform.dsc", "operator", "PLATFORM_NUMBER_EVEN")));
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
    void acceptsDeparturePlatformChangedStatus() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_PLATFORM_CHANGED")));
    }

    @Test
    void acceptsExclusionBoolean() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("exclusion.totalExclusion", "EQUALS", true)));
    }

    @Test
    void acceptsChangedOriginWhenHintIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("changes", "CONTAINS", "CHANGED_ORIGIN")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_ORIGIN));
    }

    @Test
    void acceptsChangedDestinationWhenHintIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("changes", "CONTAINS", "CHANGED_DESTINATION")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_DESTINATION));
    }

    @Test
    void acceptsChangedPathWhenHintIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("changes", "CONTAINS", "CHANGED_PATH")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_PATH));
    }

    @Test
    void acceptsGenericCancellationWhenAnyCancellationSignalIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]",
                        Map.of("any", List.of(
                                leaf("changes", "CONTAINS", "CANCELLATION"),
                                leaf("changes", "CONTAINS", "PARTIALLY_CANCELLATION"),
                                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                                leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION")))),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.GENERIC_CANCELLATION));
    }

    @Test
    void acceptsPartialCancellationWhenHintIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("changes", "CONTAINS", "PARTIALLY_CANCELLATION")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.PARTIAL_CANCELLATION));
    }

    @Test
    void acceptsArrivalCancellationWhenHintIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.ARRIVAL_CANCELLATION));
    }

    @Test
    void acceptsDepartureCancellationWhenHintIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.DEPARTURE_CANCELLATION));
    }

    @Test
    void acceptsTotalExclusionWhenHintIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("exclusion.totalExclusion", "EQUALS", true)),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.TOTAL_EXCLUSION));
    }

    @Test
    void acceptsTimeBasedExclusionWhenHintIsCovered() {
        assertVerifiedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("exclusion.timeBasedExclusion", "EQUALS", true)),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.TIME_BASED_EXCLUSION));
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
    void rejectsInWithValueArrayInsteadOfValues() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("callStart.stopPoint.id", "IN", List.of("TNPNTS00000000005439"))), "values, not value");
    }

    @Test
    void acceptsInWithValuesArray() {
        assertVerified(conditionAnyElement("stopPointsJourneyDetails[]",
                leafValues("callStart.stopPoint.id", "IN", List.of("TNPNTS00000000005439"))));
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
    void rejectsChangedDestinationHintCoveredByWrongChange() {
        assertRejectedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("changes", "CONTAINS", "CHANGED_ORIGIN")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_DESTINATION),
                "CHANGED_DESTINATION");
    }

    @Test
    void rejectsArrivalCancellationHintCoveredByDepartureCancellation() {
        assertRejectedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.ARRIVAL_CANCELLATION),
                "ARRIVAL_CANCELLATION");
    }

    @Test
    void rejectsTotalExclusionHintCoveredByTimeBasedExclusion() {
        assertRejectedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("exclusion.timeBasedExclusion", "EQUALS", true)),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.TOTAL_EXCLUSION),
                "TOTAL_EXCLUSION");
    }

    @Test
    void rejectsNegativeCancellationHintWithoutSafeNegativeOperator() {
        assertRejectedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("changes", "CONTAINS", "CANCELLATION")),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.GENERIC_CANCELLATION,
                        ScheduledAlertChangeConstraint.Polarity.EXCLUDE),
                "Negative change/cancellation");
    }

    @Test
    void rejectsMissingChangeCancellationExclusionConditionForHint() {
        assertRejectedWithChangeHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0)),
                changeHints(ScheduledAlertChangeConstraint.ChangeIntent.PARTIAL_CANCELLATION),
                "PARTIAL_CANCELLATION");
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
    void rejectsPlatformNumberOperatorOnNonPlatformField() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("stopPoint.id", "PLATFORM_NUMBER_GREATER_THAN", 2)), "PLATFORM_NUMBER_GREATER_THAN");
    }

    @Test
    void rejectsPlatformBetweenWithInvalidRange() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", Map.of("min", 5, "max", 2))),
                "min less than or equal to max");
    }

    @Test
    void rejectsPlatformMultipleOfZero() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                leaf("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_MULTIPLE_OF", 0)),
                "value greater than 0");
    }

    @Test
    void rejectsPlatformFieldComparisonMissingOtherField() {
        assertRejectedForCondition(conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of(
                        "field", "timetabledDeparturePlatform.dsc",
                        "operator", "PLATFORM_NOT_EQUALS_FIELD")), "otherField");
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
    void rejectsWhenPlatformHintIsNotCoveredByCondition() {
        assertRejected(
                validOutcome(technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("departureDelay.delay", "GREATER_THAN", 0))), blueprint()),
                null,
                null,
                null,
                new ScheduledAlertPlatformHints(true, List.of(new ScheduledAlertPlatformConstraint(
                        "binario 3",
                        ScheduledAlertPlatformConstraint.Direction.DEPARTURE,
                        ScheduledAlertPlatformConstraint.PlatformIntent.EQUALS,
                        "3",
                        List.of(),
                        null,
                        null,
                        null,
                        ScheduledAlertPlatformConstraint.SourcePreference.UNSPECIFIED,
                        0.9)), List.of()),
                "Platform constraint was requested");
    }

    @Test
    void rejectsPlatformValueInServiceDataQueryStopPoints() {
        assertRejected(
                validOutcome(
                        technicalSpecification(List.of("3"), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                                conditionAnyElement("stopPointsJourneyDetails[]",
                                        leaf("timetabledDeparturePlatform.dsc", "EQUAL_PLATFORM", "3"))),
                        blueprint(List.of("3"), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                null,
                null,
                null,
                platformHints(),
                "Platform values must not appear in serviceDataQuery.stopPoints");
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

    @Test
    void acceptsReportCountEveryRunWithNullThreshold() {
        AlertVerificationOutcome validated = validator.validate(validOutcome(
                technicalSpecificationWithEvaluation(
                        "REPORT_COUNT",
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0)),
                        null,
                        "EVERY_RUN",
                        true),
                blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsCountMatchingJourneysOnMatchWithThreshold() {
        AlertVerificationOutcome validated = validator.validate(validOutcome(
                technicalSpecificationWithEvaluation(
                        "COUNT_MATCHING_JOURNEYS",
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVING")),
                        Map.of("operator", "GREATER_OR_EQUAL", "value", 2),
                        "ON_MATCH",
                        true),
                blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsCountMatchingJourneysWithoutThreshold() {
        assertRejected(validOutcome(
                technicalSpecificationWithEvaluation(
                        "COUNT_MATCHING_JOURNEYS",
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVING")),
                        null,
                        "ON_MATCH",
                        true),
                blueprint()), "requires threshold");
    }

    @Test
    void rejectsCountMatchingJourneysWithEveryRun() {
        assertRejected(validOutcome(
                technicalSpecificationWithEvaluation(
                        "COUNT_MATCHING_JOURNEYS",
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVING")),
                        Map.of("operator", "GREATER_OR_EQUAL", "value", 2),
                        "EVERY_RUN",
                        true),
                blueprint()), "ON_MATCH");
    }

    @Test
    void rejectsReportCountWithThreshold() {
        assertRejected(validOutcome(
                technicalSpecificationWithEvaluation(
                        "REPORT_COUNT",
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0)),
                        Map.of("operator", "GREATER_OR_EQUAL", "value", 1),
                        "EVERY_RUN",
                        true),
                blueprint()), "must not contain threshold");
    }

    @Test
    void rejectsReportCountWithOnMatch() {
        assertRejected(validOutcome(
                technicalSpecificationWithEvaluation(
                        "REPORT_COUNT",
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0)),
                        null,
                        "ON_MATCH",
                        true),
                blueprint()), "EVERY_RUN");
    }

    @Test
    void rejectsReportCountWithoutIncludeCount() {
        assertRejected(validOutcome(
                technicalSpecificationWithEvaluation(
                        "REPORT_COUNT",
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0)),
                        null,
                        "EVERY_RUN",
                        false),
                blueprint()), "includeCount");
    }

    @Test
    void rejectsCountMatchingJourneysForSnapshotReportRoute() {
        AlertVerificationOutcome outcome = validOutcome(
                technicalSpecificationWithEvaluation(
                        "COUNT_MATCHING_JOURNEYS",
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.stopPoint.id", "EQUALS", GARIBALDI)),
                        Map.of("operator", "GREATER_OR_EQUAL", "value", 1),
                        "ON_MATCH",
                        true),
                blueprint());

        AlertVerificationOutcome validated = validator.validate(outcome, null, reportRoute());

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains("SNAPSHOT_REPORT route requires REPORT_COUNT or REPORT_MATCHING_JOURNEYS");
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    @Test
    void acceptsReportCountForSnapshotReportRouteWithOriginFilter() {
        AlertVerificationOutcome outcome = validOutcome(
                technicalSpecificationWithEvaluation(
                        "REPORT_COUNT",
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.stopPoint.id", "EQUALS", GARIBALDI)),
                        null,
                        "EVERY_RUN",
                        true),
                blueprint());

        AlertVerificationOutcome validated = validator.validate(outcome, null, reportRoute());

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsExplicitFrequencyWhenScheduleMatchesTemporalHints() {
        Map<String, Object> technical = technicalSpecification();
        withSchedule(technical, 600, false, "Ogni 10 minuti");

        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technical, blueprint()),
                null,
                null,
                explicitFrequencyHints(600));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsDefaultFrequencyWhenNoExplicitFrequencyExists() {
        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technicalSpecification(), blueprint()),
                null,
                null,
                defaultTemporalHints());

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsExplicitLookaheadWhenTimeWindowMatchesTemporalHints() {
        Map<String, Object> technical = technicalSpecification();
        withLookahead(technical, 120, false, "NOW_PLUS_DURATION", "nelle prossime 2 ore");

        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technical, blueprint()),
                null,
                null,
                explicitLookaheadHints(120));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsDefaultLookaheadWhenNoExplicitWindowExists() {
        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technicalSpecification(), blueprint()),
                null,
                null,
                defaultTemporalHints());

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsExplicitFrequencyMarkedDefaulted() {
        assertRejected(
                validOutcome(technicalSpecification(), blueprint()),
                null,
                null,
                explicitFrequencyHints(600),
                "schedule.defaulted=true");
    }

    @Test
    void rejectsExplicitFrequencyMismatch() {
        Map<String, Object> technical = technicalSpecification();
        withSchedule(technical, 300, false, "Ogni 10 minuti");

        assertRejected(
                validOutcome(technical, blueprint()),
                null,
                null,
                explicitFrequencyHints(600),
                "expected 600 seconds but got 300");
    }

    @Test
    void rejectsDefaultFrequencyMarkedExplicit() {
        Map<String, Object> technical = technicalSpecification();
        withSchedule(technical, 600, false, "Ogni 10 minuti");

        assertRejected(
                validOutcome(technical, blueprint()),
                null,
                null,
                defaultTemporalHints(),
                "schedule.defaulted=false");
    }

    @Test
    void rejectsExplicitLookaheadWithDefaultEndMode() {
        assertRejected(
                validOutcome(technicalSpecification(), blueprint()),
                null,
                null,
                explicitLookaheadHints(120),
                "NOW_PLUS_DURATION");
    }

    @Test
    void rejectsExplicitLookaheadMismatch() {
        Map<String, Object> technical = technicalSpecification();
        withLookahead(technical, 480, false, "NOW_PLUS_DURATION", "nelle prossime 2 ore");

        assertRejected(
                validOutcome(technical, blueprint()),
                null,
                null,
                explicitLookaheadHints(120),
                "expected 120 minutes but got 480");
    }

    @Test
    void rejectsDefaultLookaheadUsingDurationMode() {
        Map<String, Object> technical = technicalSpecification();
        withLookahead(technical, 480, false, "NOW_PLUS_DURATION", "nelle prossime 2 ore");

        assertRejected(
                validOutcome(technical, blueprint()),
                null,
                null,
                defaultTemporalHints(),
                "NOW_PLUS_DEFAULT_LOOKAHEAD");
    }

    @Test
    void rejectsFrequencyBelowMinimum() {
        Map<String, Object> technical = technicalSpecification();
        withSchedule(technical, 30, false, "Ogni 30 secondi");

        assertRejected(
                validOutcome(technical, blueprint()),
                null,
                null,
                explicitFrequencyHints(30),
                "between 60 and 86400");
    }

    @Test
    void rejectsLookaheadAboveMaximum() {
        Map<String, Object> technical = technicalSpecification();
        withLookahead(technical, 2000, false, "NOW_PLUS_DURATION", "nelle prossime 2000 minuti");

        assertRejected(
                validOutcome(technical, blueprint()),
                null,
                null,
                explicitLookaheadHints(2000),
                "between 1 and 1440");
    }

    @Test
    void acceptsDepartureLocalTimeBetweenOnDepartureField() {
        AlertVerificationOutcome validated = validator.validate(validOutcome(
                technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("10:00:00", "12:00:00")))),
                blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsArrivalLocalTimeBetweenOnArrivalField() {
        AlertVerificationOutcome validated = validator.validate(validOutcome(
                technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callEnd.arrivalTime", "LOCAL_TIME_BETWEEN", timeValue("14:00:00", "16:00:00")))),
                blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsDelayAndDepartureTimeFilterInSameAnyElement() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]", Map.of(
                "all", List.of(
                        leaf("departureDelay.delay", "GREATER_THAN", 0),
                        leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("10:00:00", "12:00:00")))));

        AlertVerificationOutcome validated = validator.validate(validOutcome(technicalSpecification(condition), blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsReportCountWithTimeFilter() {
        AlertVerificationOutcome validated = validator.validate(validOutcome(
                technicalSpecificationWithEvaluation(
                        "REPORT_COUNT",
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("10:00:00", "12:00:00"))),
                        null,
                        "EVERY_RUN",
                        true),
                blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsConditionalThresholdWithTimeFilter() {
        AlertVerificationOutcome validated = validator.validate(validOutcome(
                technicalSpecificationWithEvaluation(
                        "COUNT_MATCHING_JOURNEYS",
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("18:00:00", "20:00:00"))),
                        Map.of("operator", "GREATER_OR_EQUAL", "value", 5),
                        "ON_MATCH",
                        true),
                blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void acceptsBooleanExistsWithTimeFilter() {
        AlertVerificationOutcome validated = validator.validate(validOutcome(
                technicalSpecificationWithEvaluation(
                        "BOOLEAN_EXISTS",
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callEnd.arrivalTime", "LOCAL_TIME_BETWEEN", timeValue("14:00:00", "16:00:00"))),
                        null,
                        "ON_MATCH",
                        true),
                blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    @Test
    void rejectsOutputPolicyWithoutCountOrMatchingJourneys() {
        Map<String, Object> technical = technicalSpecificationWithEvaluation(
                "BOOLEAN_EXISTS",
                conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callEnd.arrivalTime", "LOCAL_TIME_BETWEEN", timeValue("14:00:00", "16:00:00"))),
                null,
                "ON_MATCH",
                false);
        technical.put("outputPolicy", Map.of(
                "emit", "ON_MATCH",
                "includeCount", false,
                "includeMatchingJourneys", false));

        assertRejected(validOutcome(technical, blueprint()),
                "Scheduled outputPolicy must include at least count or matching journeys");
    }

    @Test
    void rejectsRequestedJourneyTimeFilterWhenConditionOmitsLocalTimeBetween() {
        assertRejected(
                validOutcome(technicalSpecification(), blueprint()),
                null,
                null,
                journeyTimeHints(ScheduledAlertJourneyTimeFilter.Direction.DEPARTURE, "10:00:00", "12:00:00"),
                "Scheduled journey time filter was requested");
    }

    @Test
    void rejectsDepartureHintCoveredByArrivalField() {
        assertRejected(
                validOutcome(technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callEnd.arrivalTime", "LOCAL_TIME_BETWEEN", timeValue("10:00:00", "12:00:00")))), blueprint()),
                null,
                null,
                journeyTimeHints(ScheduledAlertJourneyTimeFilter.Direction.DEPARTURE, "10:00:00", "12:00:00"),
                "direction-compatible");
    }

    @Test
    void rejectsArrivalHintCoveredByDepartureField() {
        assertRejected(
                validOutcome(technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                        leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("14:00:00", "16:00:00")))), blueprint()),
                null,
                null,
                journeyTimeHints(ScheduledAlertJourneyTimeFilter.Direction.ARRIVAL, "14:00:00", "16:00:00"),
                "direction-compatible");
    }

    @Test
    void rejectsLocalTimeBetweenOnNonTimeField() {
        assertRejected(validOutcome(
                        technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("vehicleJourneyName", "LOCAL_TIME_BETWEEN", timeValue("10:00:00", "12:00:00")))),
                        blueprint()),
                "LOCAL_TIME_BETWEEN");
    }

    @Test
    void rejectsLocalTimeBetweenWithoutTimezone() {
        assertRejected(validOutcome(
                        technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN",
                                        Map.of("start", "10:00:00", "end", "12:00:00")))),
                        blueprint()),
                "timezone");
    }

    @Test
    void rejectsLocalTimeBetweenInvalidTime() {
        assertRejected(validOutcome(
                        technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("25:00:00", "12:00:00")))),
                        blueprint()),
                "HH:mm:ss");
    }

    @Test
    void rejectsAbsoluteFieldInsideStopPointsJourneyDetailsAnyElement() {
        assertRejected(validOutcome(
                        technicalSpecification(conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("stopPointsJourneyDetails[].callStart.departureTime",
                                        "LOCAL_TIME_BETWEEN",
                                        timeValue("10:00:00", "12:00:00")))),
                        blueprint()),
                "field is not allowed");
    }

    @Test
    void rejectsUnsupportedTemporalTrendClaims() {
        AlertVerificationOutcome outcome = validOutcome(
                technicalSpecification(),
                blueprint(),
                Map.of(
                        "requirements", List.of(Map.of(
                                "text", "trend increasing every hour",
                                "required", true,
                                "mappable", true,
                                "mappedBy", List.of("trend"))),
                        "allRequiredRequirementsMapped", true));

        assertRejected(outcome, "not supported");
    }

    @Test
    void acceptsExplicitMonitoredOnlyContext() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Gorla", GORLA)), List.of(), List.of(), List.of(GORLA), false, List.of()));
    }

    @Test
    void acceptsMonitoredAndOriginFilterContext() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.stopPoint.id", "EQUALS", GARIBALDI))),
                        blueprint(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)),
                        List.of(filter("Garibaldi FS", ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT,
                                ScheduledAlertLocationPolarity.INCLUDE, List.of(GARIBALDI),
                                List.of("stopPointsJourneyDetails[].callStart.stopPoint.id",
                                        "stopPointsJourneyDetails[].callStart.stopPoint.nameLong"), false)),
                        List.of(), List.of(PERO), false, List.of()));
    }

    @Test
    void acceptsMonitoredAndExcludedDestinationContext() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(BUONARROTI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leafValues("callEnd.stopPoint.id", "NOT_IN", List.of(TRE_TORRI)))),
                        blueprint(List.of(BUONARROTI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Buonarroti", BUONARROTI)),
                        List.of(),
                        List.of(filter("Tre Torri", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                                ScheduledAlertLocationPolarity.EXCLUDE, List.of(TRE_TORRI),
                                List.of("stopPointsJourneyDetails[].callEnd.stopPoint.id",
                                        "stopPointsJourneyDetails[].callEnd.stopPoint.nameLong"), false)),
                        List.of(BUONARROTI), false, List.of()));
    }

    @Test
    void acceptsMultiMonitoredContext() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(VAREDO, PALAZZOLO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVING"))),
                        blueprint(List.of(VAREDO, PALAZZOLO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Varedo", VAREDO), monitored("Palazzolo Milanese", PALAZZOLO)),
                        List.of(), List.of(), List.of(VAREDO, PALAZZOLO), false, List.of()));
    }

    @Test
    void acceptsMultiMonitoredThresholdWithoutRepeatingStopPointCondition() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(VAREDO, PALAZZOLO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVING"))),
                        blueprint(List.of(VAREDO, PALAZZOLO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Varedo", VAREDO), monitored("Palazzolo Milanese", PALAZZOLO)),
                        List.of(), List.of(), List.of(VAREDO, PALAZZOLO), false, List.of()));
    }

    @Test
    void acceptsAllKnownStopPointsContext() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(), ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS, true,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(), ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS, true)),
                context(ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS, true,
                        List.of(), List.of(), List.of(), List.of(), false, List.of()));
    }

    @Test
    void acceptsUnresolvedIncludeFilterFallback() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callEnd.stopPoint.nameLong", "CONTAINS_NORMALIZED", "Unknown Place"))),
                        blueprint(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)),
                        List.of(filter("Unknown Place", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                                ScheduledAlertLocationPolarity.INCLUDE, List.of(),
                                List.of("stopPointsJourneyDetails[].callEnd.stopPoint.id",
                                        "stopPointsJourneyDetails[].callEnd.stopPoint.nameLong"), true)),
                        List.of(), List.of(PERO), false, List.of()));
    }

    @Test
    void acceptsSpecificCancelledStopIdCoverage() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        cancelledStopCondition(leaf("stopPoint.id", "EQUALS", PALAZZOLO))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                cancelledStopContext(false));
    }

    @Test
    void acceptsSpecificCancelledStopInCoverage() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        cancelledStopCondition(leafValues("stopPoint.id", "IN", List.of(PALAZZOLO)))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                cancelledStopContext(false));
    }

    @Test
    void acceptsUnresolvedCancelledStopNameFallback() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        cancelledStopCondition(leaf("stopPoint.nameLong", "CONTAINS_NORMALIZED", "Palazzolo Milanese"))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                cancelledStopContext(true));
    }

    @Test
    void acceptsGenericAnyCancelledStopCondition() {
        assertVerifiedWithCancelledCallHints(
                conditionAnyElement("stopPointsJourneyDetails[]",
                        Map.of("field", "nextCancelledCalls", "operator", "NOT_EMPTY")),
                genericCancelledCallHints());
    }

    @Test
    void rejectsCancelledStopCoveredWithNextCalls() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                Map.of("all", List.of(Map.of("anyElement", Map.of(
                                        "path", "nextCalls[]",
                                        "conditions", leaf("stopPoint.id", "EQUALS", PALAZZOLO))))))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                cancelledStopContext(false),
                "not covered");
    }

    @Test
    void rejectsCancelledStopCoveredWithDestinationField() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callEnd.stopPoint.id", "EQUALS", PALAZZOLO))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                cancelledStopContext(false),
                "not covered");
    }

    @Test
    void rejectsInventedCancelledStopId() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        cancelledStopCondition(leaf("stopPoint.id", "EQUALS", INVENTED))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                cancelledStopContext(false),
                INVENTED);
    }

    @Test
    void rejectsMissingCancelledStopCondition() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("changes", "CONTAINS", "CANCELLATION"))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                cancelledStopContext(false),
                "not covered");
    }

    @Test
    void rejectsGenericCancelledStopHintWithoutNextCancelledCallsCondition() {
        assertRejectedWithCancelledCallHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("changes", "CONTAINS", "CANCELLATION")),
                genericCancelledCallHints(),
                "nextCancelledCalls");
    }

    @Test
    void acceptsGenericReplacementServiceCondition() {
        assertVerifiedWithReplacementHints(
                conditionAnyElement("stopPointsJourneyDetails[]",
                        Map.of("field", "isReplacementOf", "operator", "NOT_EMPTY")),
                replacementHints(ScheduledAlertReplacementConstraint.ReplacementIntent.GENERIC_REPLACEMENT_SERVICE,
                        ScheduledAlertReplacementConstraint.ReplacementType.UNSPECIFIED));
    }

    @Test
    void acceptsReplacementObjectCondition() {
        assertVerifiedWithReplacementHints(
                conditionAnyElement("stopPointsJourneyDetails[]",
                        Map.of("field", "replacement", "operator", "NOT_NULL")),
                replacementHints(ScheduledAlertReplacementConstraint.ReplacementIntent.HAS_REPLACEMENT_OBJECT,
                        ScheduledAlertReplacementConstraint.ReplacementType.UNSPECIFIED));
    }

    @Test
    void acceptsExternalReplacementObjectCondition() {
        assertVerifiedWithReplacementHints(
                conditionAnyElement("stopPointsJourneyDetails[]",
                        Map.of("field", "externalReplacement", "operator", "NOT_NULL")),
                replacementHints(ScheduledAlertReplacementConstraint.ReplacementIntent.HAS_EXTERNAL_REPLACEMENT_OBJECT,
                        ScheduledAlertReplacementConstraint.ReplacementType.UNSPECIFIED));
    }

    @Test
    void acceptsSpecificReplacementStopCoverage() {
        assertVerifiedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        replacementStopCondition(leaf("stopPointId.id", "EQUALS", PALAZZOLO))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                replacementStopContext());
    }

    @Test
    void acceptsReplacementTypeDepartureCondition() {
        assertVerifiedWithReplacementHints(
                replacementStopCondition(leaf("replacementType", "EQUALS", "DEPARTURE")),
                replacementHints(ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_TYPE,
                        ScheduledAlertReplacementConstraint.ReplacementType.DEPARTURE));
    }

    @Test
    void acceptsReplacementStopAndTypeSameEntry() {
        assertVerified(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        replacementStopCondition(Map.of("all", List.of(
                                leaf("stopPointId.id", "EQUALS", PALAZZOLO),
                                leaf("replacementType", "EQUALS", "DEPARTURE"))))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                replacementStopContext(),
                null,
                null,
                null,
                null,
                null,
                replacementHints(ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP_WITH_TYPE,
                        ScheduledAlertReplacementConstraint.ReplacementType.DEPARTURE));
    }

    @Test
    void rejectsGenericReplacementHintWithoutReplacementCondition() {
        assertRejectedWithReplacementHints(
                conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0)),
                replacementHints(ScheduledAlertReplacementConstraint.ReplacementIntent.GENERIC_REPLACEMENT_SERVICE,
                        ScheduledAlertReplacementConstraint.ReplacementType.UNSPECIFIED),
                "GENERIC_REPLACEMENT_SERVICE");
    }

    @Test
    void rejectsReplacementStopCoveredWithNextCancelledCalls() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        cancelledStopCondition(leaf("stopPoint.id", "EQUALS", PALAZZOLO))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                replacementStopContext(),
                "not covered");
    }

    @Test
    void rejectsReplacementStopCoveredWithDestinationField() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("callEnd.stopPoint.id", "EQUALS", PALAZZOLO))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                replacementStopContext(),
                "not covered");
    }

    @Test
    void rejectsInventedReplacementStopId() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        replacementStopCondition(leaf("stopPointId.id", "EQUALS", INVENTED))),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                replacementStopContext(),
                INVENTED);
    }

    @Test
    void rejectsInvalidReplacementTypeEnum() {
        assertRejectedForCondition(
                replacementStopCondition(leaf("replacementType", "EQUALS", "BOTH")),
                "enum value BOTH");
    }

    @Test
    void rejectsReplacementStopAndTypeSplitAcrossDifferentAnyElements() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(
                        Map.of("anyElement", Map.of(
                                "path", "replacement.stopPointReplacements[]",
                                "conditions", leaf("stopPointId.id", "EQUALS", PALAZZOLO))),
                        Map.of("anyElement", Map.of(
                                "path", "replacement.stopPointReplacements[]",
                                "conditions", leaf("replacementType", "EQUALS", "DEPARTURE"))))));

        assertRejected(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false, condition),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                replacementStopContext(),
                null,
                null,
                null,
                null,
                null,
                replacementHints(ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP_WITH_TYPE,
                        ScheduledAlertReplacementConstraint.ReplacementType.DEPARTURE),
                "REPLACEMENT_STOP_WITH_TYPE");
    }

    @Test
    void rejectsUnsupportedReplacementSourceRoute() {
        assertRejectedWithReplacementHints(
                conditionAnyElement("stopPointsJourneyDetails[]",
                        Map.of("field", "isReplacementOf", "operator", "NOT_EMPTY")),
                replacementSourceRouteHints(),
                "Replacement source route start/end stop points are not supported");
    }

    @Test
    void acceptsDelayPlatformChangeAndExcludedDestinationInSameJourneyElement() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(
                        Map.of("any", List.of(
                                leaf("arrivalDelay.delay", "GREATER_THAN", 0),
                                leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        leaf("changes", "CONTAINS", "PLATFORM_CHANGED"),
                        leafValues("callEnd.stopPoint.id", "NOT_IN", List.of(TRE_TORRI)))));

        assertVerified(
                validOutcome(technicalSpecification(List.of(BUONARROTI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false, condition),
                        blueprint(List.of(BUONARROTI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Buonarroti", BUONARROTI)),
                        List.of(),
                        List.of(filter("Tre Torri", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                                ScheduledAlertLocationPolarity.EXCLUDE,
                                List.of(TRE_TORRI),
                                List.of("stopPointsJourneyDetails[].callEnd.stopPoint.id"),
                                false)),
                        List.of(BUONARROTI),
                        false,
                        List.of()),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    void acceptsOriginDelayAndDepartureTimeFilterInSameJourneyElement() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(
                        leaf("callStart.stopPoint.id", "EQUALS", GARIBALDI),
                        leaf("departureDelay.delay", "GREATER_THAN", 600),
                        leaf("callStart.departureTime", "LOCAL_TIME_BETWEEN", timeValue("10:00:00", "12:00:00")))));

        assertVerified(
                validOutcome(technicalSpecification(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false, condition),
                        blueprint(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)),
                        List.of(filter("Garibaldi FS", ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT,
                                ScheduledAlertLocationPolarity.INCLUDE,
                                List.of(GARIBALDI),
                                List.of("stopPointsJourneyDetails[].callStart.stopPoint.id"),
                                false)),
                        List.of(),
                        List.of(PERO),
                        false,
                        List.of()),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    void acceptsReplacementStopTypeAndDelayInSameJourneyElement() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(
                        leaf("departureDelay.delay", "GREATER_THAN", 0),
                        Map.of("anyElement", Map.of(
                                "path", "replacement.stopPointReplacements[]",
                                "conditions", Map.of("all", List.of(
                                        leaf("stopPointId.id", "EQUALS", PALAZZOLO),
                                        leaf("replacementType", "EQUALS", "DEPARTURE"))))))));

        assertVerified(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false, condition),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                replacementStopContext(),
                null,
                null,
                null,
                null,
                null,
                replacementHints(ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP_WITH_TYPE,
                        ScheduledAlertReplacementConstraint.ReplacementType.DEPARTURE));
    }

    @Test
    void acceptsSuppressedStopAndExcludedDestinationInSameJourneyElement() {
        Map<String, Object> condition = conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(
                        Map.of("anyElement", Map.of(
                                "path", "nextCancelledCalls[]",
                                "conditions", leaf("stopPoint.id", "EQUALS", PALAZZOLO))),
                        leafValues("callEnd.stopPoint.id", "NOT_IN", List.of(TRE_TORRI)))));

        assertVerified(
                validOutcome(technicalSpecification(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false, condition),
                        blueprint(List.of(GORLA), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Gorla", GORLA)),
                        List.of(filter("Palazzolo Milanese", ScheduledAlertLocationRole.FILTER_CANCELLED_CALL_STOP_POINT,
                                ScheduledAlertLocationPolarity.INCLUDE,
                                List.of(PALAZZOLO),
                                List.of("stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id"),
                                false)),
                        List.of(filter("Tre Torri", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                                ScheduledAlertLocationPolarity.EXCLUDE,
                                List.of(TRE_TORRI),
                                List.of("stopPointsJourneyDetails[].callEnd.stopPoint.id"),
                                false)),
                        List.of(GORLA),
                        false,
                        List.of()),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    void rejectsSplitJourneyLevelConstraintsAcrossSiblingAnyElements() {
        Map<String, Object> condition = condition(Map.of("all", List.of(
                Map.of("anyElement", Map.of(
                        "path", "stopPointsJourneyDetails[]",
                        "conditions", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                Map.of("anyElement", Map.of(
                        "path", "stopPointsJourneyDetails[]",
                        "conditions", leaf("changes", "CONTAINS", "PLATFORM_CHANGED"))))));

        assertRejected(validOutcome(technicalSpecification(condition), blueprint()), "same stopPointsJourneyDetails[] anyElement");
    }

    @Test
    void rejectsRequirementCoverageMissingRequiredMappedBy() {
        Map<String, Object> coverage = Map.of(
                "requirements", List.of(Map.of(
                        "text", "delayed trains",
                        "required", true,
                        "mappable", true,
                        "mappedBy", List.of())),
                "allRequiredRequirementsMapped", true);

        assertRejected(validOutcome(technicalSpecification(), blueprint(), coverage), "mappedBy");
    }

    @Test
    void rejectsRequirementCoverageWithEventField() {
        Map<String, Object> coverage = Map.of(
                "requirements", List.of(Map.of(
                        "text", "arrival event",
                        "required", true,
                        "mappable", true,
                        "mappedBy", List.of("payload.ongroundServiceEvent.eventsType"))),
                "allRequiredRequirementsMapped", true);

        assertRejected(validOutcome(technicalSpecification(), blueprint(), coverage), "Event/Kafka field");
    }

    @Test
    void rejectsRequirementCoverageMappingUnsupportedWifiToSupportedField() {
        Map<String, Object> coverage = Map.of(
                "requirements", List.of(Map.of(
                        "text", "wifi on board",
                        "required", true,
                        "mappable", true,
                        "mappedBy", List.of("stopPointsJourneyDetails[].transportMode.dsc"))),
                "allRequiredRequirementsMapped", true);

        assertRejected(validOutcome(technicalSpecification(), blueprint(), coverage), "not supported");
    }

    @Test
    void rejectsExtraInventedQueryStopPoint() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(PERO, GARIBALDI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(PERO, GARIBALDI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)), List.of(), List.of(), List.of(PERO), false, List.of()),
                "stopPoints");
    }

    @Test
    void rejectsMissingMonitoredStopPoint() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)), List.of(), List.of(), List.of(PERO), false, List.of()),
                "non-empty");
    }

    @Test
    void rejectsFilterLocationWronglyInQueryStopPoints() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(PERO, GARIBALDI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.stopPoint.id", "EQUALS", GARIBALDI))),
                        blueprint(List.of(PERO, GARIBALDI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)),
                        List.of(filter("Garibaldi FS", ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT,
                                ScheduledAlertLocationPolarity.INCLUDE, List.of(GARIBALDI),
                                List.of("stopPointsJourneyDetails[].callStart.stopPoint.id"), false)),
                        List.of(), List.of(PERO, GARIBALDI), false, List.of()),
                "Filter/control location");
    }

    @Test
    void rejectsFilterOriginNotCovered() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)),
                        List.of(filter("Garibaldi FS", ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT,
                                ScheduledAlertLocationPolarity.INCLUDE, List.of(GARIBALDI),
                                List.of("stopPointsJourneyDetails[].callStart.stopPoint.id"), false)),
                        List.of(), List.of(PERO), false, List.of()),
                "not covered");
    }

    @Test
    void rejectsFilterOriginCoveredWithWrongField() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callEnd.stopPoint.id", "EQUALS", GARIBALDI))),
                        blueprint(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)),
                        List.of(filter("Garibaldi FS", ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT,
                                ScheduledAlertLocationPolarity.INCLUDE, List.of(GARIBALDI),
                                List.of("stopPointsJourneyDetails[].callStart.stopPoint.id"), false)),
                        List.of(), List.of(PERO), false, List.of()),
                "not covered");
    }

    @Test
    void rejectsExcludedDestinationRepresentedWithPositiveIn() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(BUONARROTI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leafValues("callEnd.stopPoint.id", "IN", List.of(TRE_TORRI)))),
                        blueprint(List.of(BUONARROTI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Buonarroti", BUONARROTI)), List.of(),
                        List.of(filter("Tre Torri", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                                ScheduledAlertLocationPolarity.EXCLUDE, List.of(TRE_TORRI),
                                List.of("stopPointsJourneyDetails[].callEnd.stopPoint.id"), false)),
                        List.of(BUONARROTI), false, List.of()),
                "not covered");
    }

    @Test
    void rejectsIncludedDestinationRepresentedOnlyWithNotIn() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(BUONARROTI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leafValues("callEnd.stopPoint.id", "NOT_IN", List.of(TRE_TORRI)))),
                        blueprint(List.of(BUONARROTI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Buonarroti", BUONARROTI)),
                        List.of(filter("Tre Torri", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT,
                                ScheduledAlertLocationPolarity.INCLUDE, List.of(TRE_TORRI),
                                List.of("stopPointsJourneyDetails[].callEnd.stopPoint.id"), false)),
                        List.of(), List.of(BUONARROTI), false, List.of()),
                "not covered");
    }

    @Test
    void rejectsInventedIdInCondition() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]",
                                leaf("callStart.stopPoint.id", "EQUALS", INVENTED))),
                        blueprint(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)), List.of(), List.of(), List.of(PERO), false, List.of()),
                INVENTED);
    }

    @Test
    void rejectsAllKnownStopPointsWithExplicitQueryStopPoint() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(PERO), ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS, true,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(PERO), ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS, true)),
                context(ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS, true,
                        List.of(), List.of(), List.of(), List.of(), false, List.of()),
                "ALL_KNOWN_STOP_POINTS");
    }

    @Test
    void rejectsBlueprintServiceDataQueryDiffers() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(PERO), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(GARIBALDI), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(monitored("Pero", PERO)), List.of(), List.of(), List.of(PERO), false, List.of()),
                "agentBlueprintPreview");
    }

    @Test
    void rejectsUnspecifiedMonitoringScopeInVerified() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(), ScheduledAlertMonitoringScope.UNSPECIFIED, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(), ScheduledAlertMonitoringScope.UNSPECIFIED, false)),
                context(ScheduledAlertMonitoringScope.UNSPECIFIED, false,
                        List.of(), List.of(), List.of(), List.of(), false, List.of()),
                "explicit monitored stop points");
    }

    @Test
    void rejectsUnresolvedMonitoredContextEvenWithGuessedId() {
        assertRejectedWithContext(
                validOutcome(technicalSpecification(List.of(INVENTED), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        conditionAnyElement("stopPointsJourneyDetails[]", leaf("departureDelay.delay", "GREATER_THAN", 0))),
                        blueprint(List.of(INVENTED), ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false)),
                context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                        List.of(), List.of(), List.of(), List.of(), true, List.of("Fermata Inventata")),
                "unresolved monitored");
    }

    private void assertRejected(AlertVerificationOutcome outcome, String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(outcome);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    private void assertRejected(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext context,
            AlertRouteUnderstandingResult route,
            ScheduledAlertTemporalHints temporalHints,
            String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(outcome, context, route, temporalHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    private void assertRejected(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext context,
            AlertRouteUnderstandingResult route,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(outcome, context, route, temporalHints, platformHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    private void assertRejected(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext context,
            AlertRouteUnderstandingResult route,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints,
            ScheduledAlertCancelledCallHints cancelledCallHints,
            ScheduledAlertReplacementHints replacementHints,
            String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(
                outcome, context, route, temporalHints, platformHints, changeHints, cancelledCallHints, replacementHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    private void assertVerified(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext context,
            AlertRouteUnderstandingResult route,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints,
            ScheduledAlertCancelledCallHints cancelledCallHints,
            ScheduledAlertReplacementHints replacementHints) {
        AlertVerificationOutcome validated = validator.validate(
                outcome, context, route, temporalHints, platformHints, changeHints, cancelledCallHints, replacementHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertVerified(Map<String, Object> condition) {
        AlertVerificationOutcome validated = validator.validate(validOutcome(technicalSpecification(condition), blueprint()));

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertVerifiedWithChangeHints(Map<String, Object> condition, ScheduledAlertChangeHints changeHints) {
        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technicalSpecification(condition), blueprint()),
                null,
                null,
                null,
                null,
                changeHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertRejectedWithChangeHints(
            Map<String, Object> condition,
            ScheduledAlertChangeHints changeHints,
            String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technicalSpecification(condition), blueprint()),
                null,
                null,
                null,
                null,
                changeHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    private void assertVerifiedWithCancelledCallHints(
            Map<String, Object> condition,
            ScheduledAlertCancelledCallHints cancelledCallHints) {
        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technicalSpecification(condition), blueprint()),
                null,
                null,
                null,
                null,
                null,
                cancelledCallHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertRejectedWithCancelledCallHints(
            Map<String, Object> condition,
            ScheduledAlertCancelledCallHints cancelledCallHints,
            String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technicalSpecification(condition), blueprint()),
                null,
                null,
                null,
                null,
                null,
                cancelledCallHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    private void assertVerifiedWithReplacementHints(
            Map<String, Object> condition,
            ScheduledAlertReplacementHints replacementHints) {
        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technicalSpecification(condition), blueprint()),
                null,
                null,
                null,
                null,
                null,
                null,
                replacementHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertRejectedWithReplacementHints(
            Map<String, Object> condition,
            ScheduledAlertReplacementHints replacementHints,
            String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(
                validOutcome(technicalSpecification(condition), blueprint()),
                null,
                null,
                null,
                null,
                null,
                null,
                replacementHints);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
    }

    private void assertVerifiedWithContext(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext context) {
        AlertVerificationOutcome validated = validator.validate(outcome, context);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
    }

    private void assertRejectedWithContext(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext context,
            String reasonFragment) {
        AlertVerificationOutcome validated = validator.validate(outcome, context);

        assertThat(validated.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(validated.rejectedReason()).contains(reasonFragment);
        assertThat(validated.technicalSpecification()).isNull();
        assertThat(validated.agentBlueprintPreview()).isNull();
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
        return validOutcome(technicalSpecification, blueprint, coverage());
    }

    private AlertVerificationOutcome validOutcome(
            Map<String, Object> technicalSpecification,
            Map<String, Object> blueprint,
            Map<String, Object> requirementCoverage) {
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
                requirementCoverage,
                List.of(),
                List.of("No executable code generated."));
    }

    private AlertRouteUnderstandingResult reportRoute() {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of("SERVICE_DATA"),
                "SERVICE_DATA",
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                AlertRouteIntentKind.SNAPSHOT_REPORT,
                AlertRouteOutputMode.EVERY_RUN_REPORT,
                true,
                true,
                false,
                true,
                false,
                true,
                0.95,
                "Scheduled report route.",
                null,
                List.of());
    }

    private Map<String, Object> technicalSpecification() {
        return technicalSpecification(validCondition());
    }

    private Map<String, Object> technicalSpecification(Map<String, Object> condition) {
        return technicalSpecification(List.of("TNPNTS00000000005439"),
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                false,
                condition);
    }

    private Map<String, Object> technicalSpecificationWithEvaluation(
            String mode,
            Map<String, Object> condition,
            Map<String, Object> threshold,
            String emit,
            boolean includeCount) {
        Map<String, Object> technical = new LinkedHashMap<>(technicalSpecification(condition));
        Map<String, Object> snapshotEvaluation = new LinkedHashMap<>(map(technical.get("snapshotEvaluation")));
        snapshotEvaluation.put("mode", mode);
        snapshotEvaluation.put("threshold", threshold);
        technical.put("snapshotEvaluation", snapshotEvaluation);
        technical.put("outputPolicy", Map.of(
                "emit", emit,
                "includeCount", includeCount,
                "includeMatchingJourneys", true));
        return technical;
    }

    private Map<String, Object> timeValue(String start, String end) {
        return Map.of(
                "start", start,
                "end", end,
                "timezone", "Europe/Rome");
    }

    private ScheduledAlertTemporalHints journeyTimeHints(
            ScheduledAlertJourneyTimeFilter.Direction direction,
            String start,
            String end) {
        return new ScheduledAlertTemporalHints(
                false,
                600,
                null,
                true,
                false,
                480,
                null,
                true,
                600,
                60,
                86400,
                480,
                1,
                1440,
                true,
                List.of(new ScheduledAlertJourneyTimeFilter(
                        "tra le " + start.substring(0, 5) + " e le " + end.substring(0, 5),
                        ScheduledAlertJourneyTimeFilter.TimeRelation.BETWEEN,
                        start,
                        end,
                        null,
                        direction,
                        ScheduledAlertJourneyTimeFilter.TimetabledPreference.UNSPECIFIED,
                        ScheduledAlertJourneyTimeFilter.TargetRoleHint.UNKNOWN,
                        "Europe/Rome")),
                List.of());
    }

    private ScheduledAlertPlatformHints platformHints() {
        return new ScheduledAlertPlatformHints(true, List.of(new ScheduledAlertPlatformConstraint(
                "binario 3",
                ScheduledAlertPlatformConstraint.Direction.DEPARTURE,
                ScheduledAlertPlatformConstraint.PlatformIntent.EQUALS,
                "3",
                List.of(),
                null,
                null,
                null,
                ScheduledAlertPlatformConstraint.SourcePreference.UNSPECIFIED,
                0.9)), List.of());
    }

    private ScheduledAlertChangeHints changeHints(ScheduledAlertChangeConstraint.ChangeIntent intent) {
        return changeHints(intent, ScheduledAlertChangeConstraint.Polarity.INCLUDE);
    }

    private ScheduledAlertChangeHints changeHints(
            ScheduledAlertChangeConstraint.ChangeIntent intent,
            ScheduledAlertChangeConstraint.Polarity polarity) {
        return new ScheduledAlertChangeHints(true, List.of(new ScheduledAlertChangeConstraint(
                intent.name(),
                intent,
                ScheduledAlertChangeConstraint.Direction.UNSPECIFIED,
                polarity,
                0.9)), List.of());
    }

    private ScheduledAlertCancelledCallHints genericCancelledCallHints() {
        return new ScheduledAlertCancelledCallHints(true, List.of(new ScheduledAlertCancelledCallConstraint(
                "fermate soppresse",
                false,
                null,
                ScheduledAlertCancelledCallConstraint.Polarity.INCLUDE,
                true,
                0.9)), List.of());
    }

    private ScheduledAlertReplacementHints replacementHints(
            ScheduledAlertReplacementConstraint.ReplacementIntent intent,
            ScheduledAlertReplacementConstraint.ReplacementType type) {
        return new ScheduledAlertReplacementHints(true, List.of(new ScheduledAlertReplacementConstraint(
                intent.name(),
                intent,
                type,
                intent == ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP
                        || intent == ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP_WITH_TYPE,
                "Bovisa",
                ScheduledAlertReplacementConstraint.Polarity.INCLUDE,
                null,
                0.9)), List.of());
    }

    private ScheduledAlertReplacementHints replacementSourceRouteHints() {
        return new ScheduledAlertReplacementHints(true, List.of(new ScheduledAlertReplacementConstraint(
                "tratta sostitutiva da Porto di Mare a Milano Affori",
                ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_SOURCE_ROUTE,
                ScheduledAlertReplacementConstraint.ReplacementType.UNSPECIFIED,
                false,
                null,
                ScheduledAlertReplacementConstraint.Polarity.INCLUDE,
                "Replacement source route start/end stop points are not supported by the Scheduled ServiceData snapshot catalog.",
                0.9)), List.of());
    }

    private void withSchedule(
            Map<String, Object> technical,
            int frequencySeconds,
            boolean defaulted,
            String rawText) {
        Map<String, Object> schedule = new LinkedHashMap<>();
        schedule.put("frequencySeconds", frequencySeconds);
        schedule.put("defaulted", defaulted);
        schedule.put("rawText", rawText);
        technical.put("schedule", schedule);
    }

    private void withLookahead(
            Map<String, Object> technical,
            int lookaheadMinutes,
            boolean defaulted,
            String endMode,
            String rawText) {
        Map<String, Object> serviceDataQuery = new LinkedHashMap<>(map(technical.get("serviceDataQuery")));
        Map<String, Object> timeWindow = new LinkedHashMap<>();
        timeWindow.put("startMode", "NOW_TRUNCATED_TO_MINUTE");
        timeWindow.put("endMode", endMode);
        timeWindow.put("lookaheadMinutes", lookaheadMinutes);
        timeWindow.put("defaulted", defaulted);
        timeWindow.put("rawText", rawText);
        serviceDataQuery.put("timeWindow", timeWindow);
        technical.put("serviceDataQuery", serviceDataQuery);
    }

    private ScheduledAlertTemporalHints defaultTemporalHints() {
        return new ScheduledAlertTemporalHints(
                false,
                600,
                null,
                true,
                false,
                480,
                null,
                true,
                600,
                60,
                86400,
                480,
                1,
                1440,
                List.of());
    }

    private ScheduledAlertTemporalHints explicitFrequencyHints(int frequencySeconds) {
        return new ScheduledAlertTemporalHints(
                true,
                frequencySeconds,
                "Ogni 10 minuti",
                false,
                false,
                480,
                null,
                true,
                600,
                60,
                86400,
                480,
                1,
                1440,
                List.of());
    }

    private ScheduledAlertTemporalHints explicitLookaheadHints(int lookaheadMinutes) {
        return new ScheduledAlertTemporalHints(
                false,
                600,
                null,
                true,
                true,
                lookaheadMinutes,
                "nelle prossime 2 ore",
                false,
                600,
                60,
                86400,
                480,
                1,
                1440,
                List.of());
    }

    private Map<String, Object> technicalSpecification(
            List<String> stopPoints,
            ScheduledAlertMonitoringScope monitoringScope,
            boolean requiresAllKnownStopPoints,
            Map<String, Object> condition) {
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
                        "monitoringScope", String.valueOf(monitoringScope),
                        "stopPoints", stopPoints,
                        "requiresAllKnownStopPoints", requiresAllKnownStopPoints,
                        "timeWindow", Map.of(
                                "startMode", "NOW_TRUNCATED_TO_MINUTE",
                                "endMode", "NOW_PLUS_DEFAULT_LOOKAHEAD",
                                "lookaheadMinutes", 480,
                                "defaulted", true))),
                Map.entry("snapshotEvaluation", Map.of(
                        "mode", "COUNT_MATCHING_JOURNEYS",
                        "journeyPath", "stopPointsJourneyDetails[]",
                        "condition", condition,
                        "threshold", Map.of("operator", "GREATER_OR_EQUAL", "value", 2))),
                Map.entry("outputPolicy", Map.of("emit", "ON_MATCH", "includeCount", true, "includeMatchingJourneys", true)),
                Map.entry("deduplicationKeyTemplate", "SERVICE_DATA_SCHEDULED:${alertId}:${queryWindowStart}:${conditionHash}")));
    }

    private Map<String, Object> blueprint() {
        return blueprint(List.of("TNPNTS00000000005439"),
                ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS,
                false);
    }

    private Map<String, Object> blueprint(
            List<String> stopPoints,
            ScheduledAlertMonitoringScope monitoringScope,
            boolean requiresAllKnownStopPoints) {
        return new LinkedHashMap<>(Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ScheduledServiceDataSnapshotAlertAgent",
                "triggerType", "SCHEDULE",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "targetTypes", List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                "parameters", Map.of(
                        "serviceDataQuery", Map.of(
                                "operation", "POST /v2/stoppointjourneys",
                                "monitoringScope", String.valueOf(monitoringScope),
                                "stopPoints", stopPoints,
                                "requiresAllKnownStopPoints", requiresAllKnownStopPoints),
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

    private Map<String, Object> cancelledStopCondition(Map<String, Object> leaf) {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(Map.of("anyElement", Map.of(
                        "path", "nextCancelledCalls[]",
                        "conditions", leaf)))));
    }

    private Map<String, Object> replacementStopCondition(Map<String, Object> leaf) {
        return conditionAnyElement("stopPointsJourneyDetails[]",
                Map.of("all", List.of(Map.of("anyElement", Map.of(
                        "path", "replacement.stopPointReplacements[]",
                        "conditions", leaf)))));
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

    private ScheduledServiceDataLocationContext context(
            ScheduledAlertMonitoringScope monitoringScope,
            boolean requiresAllKnownStopPoints,
            List<ScheduledServiceDataResolvedLocation> monitored,
            List<ScheduledServiceDataResolvedLocation> filters,
            List<ScheduledServiceDataResolvedLocation> excluded,
            List<String> serviceDataApiStopPoints,
            boolean hasUnresolvedRequiredMonitoredLocations,
            List<String> unresolvedRequiredMonitoredLocationTexts) {
        return new ScheduledServiceDataLocationContext(
                monitoringScope,
                monitored,
                filters,
                excluded,
                serviceDataApiStopPoints,
                requiresAllKnownStopPoints,
                hasUnresolvedRequiredMonitoredLocations,
                unresolvedRequiredMonitoredLocationTexts,
                List.of(),
                new ScheduledServiceDataApiQueryContext(
                        monitoringScope,
                        serviceDataApiStopPoints,
                        requiresAllKnownStopPoints));
    }

    private ScheduledServiceDataResolvedLocation monitored(String rawText, String id) {
        return new ScheduledServiceDataResolvedLocation(
                rawText,
                rawText,
                ScheduledAlertLocationRole.MONITORED_STOP_POINT,
                ScheduledAlertLocationPolarity.INCLUDE,
                true,
                true,
                ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                List.of(id),
                List.of(),
                false,
                false,
                List.of("body.stopPoints[]"),
                "");
    }

    private ScheduledServiceDataResolvedLocation filter(
            String rawText,
            ScheduledAlertLocationRole role,
            ScheduledAlertLocationPolarity polarity,
            List<String> selectedPointIds,
            List<String> targetFieldHints,
            boolean fallbackToNameLong) {
        return new ScheduledServiceDataResolvedLocation(
                rawText,
                rawText,
                role,
                polarity,
                false,
                true,
                selectedPointIds.isEmpty()
                        ? ScheduledServiceDataLocationResolutionStatus.UNRESOLVED
                        : ScheduledServiceDataLocationResolutionStatus.RESOLVED,
                selectedPointIds,
                List.of(),
                fallbackToNameLong,
                fallbackToNameLong,
                targetFieldHints,
                "");
    }

    private ScheduledServiceDataLocationContext cancelledStopContext(boolean fallbackToNameLong) {
        return context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                List.of(monitored("Gorla", GORLA)),
                List.of(filter("Palazzolo Milanese", ScheduledAlertLocationRole.FILTER_CANCELLED_CALL_STOP_POINT,
                        ScheduledAlertLocationPolarity.INCLUDE,
                        fallbackToNameLong ? List.of() : List.of(PALAZZOLO),
                        List.of("stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id",
                                "stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.nameLong"),
                        fallbackToNameLong)),
                List.of(),
                List.of(GORLA),
                false,
                List.of());
    }

    private ScheduledServiceDataLocationContext replacementStopContext() {
        return context(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS, false,
                List.of(monitored("Gorla", GORLA)),
                List.of(filter("Bovisa", ScheduledAlertLocationRole.FILTER_REPLACEMENT_STOP_POINT,
                        ScheduledAlertLocationPolarity.INCLUDE,
                        List.of(PALAZZOLO),
                        List.of("stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id",
                                "stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].stopPointId.id"),
                        false)),
                List.of(),
                List.of(GORLA),
                false,
                List.of());
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
