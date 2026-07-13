package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentBlueprintValidatorTest {

    private final AgentGenerationCapabilityCatalog catalog = new AgentGenerationCapabilityCatalog();
    private final AgentBlueprintValidator validator = new AgentBlueprintValidator(catalog);
    private final AgentPreviewConditionExtractor extractor = new AgentPreviewConditionExtractor();

    @Test
    void acceptsValidContainsAnyBlueprintWithValues() {
        AlertAgentGenerationPreviewData data = previewData(condition("CONTAINS_ANY", true));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("CONTAINS_ANY", "EQUALS_NORMALIZED");
    }

    @Test
    void acceptsScheduledSnapshotBlueprintShape() {
        AlertAgentGenerationPreviewData data = scheduledPreviewData();

        AgentBlueprintValidationResult result = validate(
                data,
                scheduledBlueprint(data, Map.of()),
                List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedTargetTypes()).contains("SERVICE_DATA_JOURNEY_AGGREGATE");
        assertThat(result.detectedDslOperators()).contains("EQUALS");
    }

    @Test
    void rejectsScheduledBlueprintWithoutServiceDataQuery() {
        AlertAgentGenerationPreviewData data = scheduledPreviewData();
        AgentBlueprint blueprint = scheduledBlueprint(data, Map.of("serviceDataQuery", Map.of()));

        AgentBlueprintValidationResult result = validate(data, blueprint, List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Scheduled blueprint parameters.serviceDataQuery is missing.");
    }

    @Test
    void rejectsScheduledBlueprintWithoutSnapshotEvaluation() {
        AlertAgentGenerationPreviewData data = scheduledPreviewData();
        AgentBlueprint blueprint = scheduledBlueprint(data, Map.of("snapshotEvaluation", Map.of()));

        AgentBlueprintValidationResult result = validate(data, blueprint, List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Scheduled blueprint parameters.snapshotEvaluation is missing.");
    }

    @Test
    void rejectsScheduledBlueprintWithInventedSnapshotConditionType() {
        AlertAgentGenerationPreviewData data = scheduledPreviewData("INVENTED_CONDITION_TYPE");

        AgentBlueprintValidationResult result = validate(
                data,
                scheduledBlueprint(data, Map.of()),
                List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors())
                .contains("Scheduled blueprint snapshotEvaluation.condition.type must be SERVICE_DATA_SCHEDULED_FIELD_MATCH.");
    }

    @Test
    void rejectsScheduledBlueprintWithInventedTargetType() {
        AlertAgentGenerationPreviewData data = scheduledPreviewData();
        AgentBlueprint blueprint = scheduledBlueprint(data, Map.of())
                .targetTypes(List.of(SuggestionTargetType.GENERIC));

        AgentBlueprintValidationResult result = validate(data, blueprint, List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Unsupported targetType: GENERIC");
    }

    @Test
    void acceptsContainsIgnoreCaseBlueprintWithValue() {
        AlertAgentGenerationPreviewData data = previewData(delayCondition(false));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("CONTAINS_IGNORE_CASE", "EQUALS_IGNORE_CASE");
    }

    @Test
    void acceptsNormalizedServiceCategoryAndNumericDelayConditions() {
        AlertAgentGenerationPreviewData data = previewData(realisticDelayCondition());

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains(
                "CONTAINS_NORMALIZED", "EQUALS_NORMALIZED", "GREATER_THAN", "CONTAINS_ANY");
    }

    @Test
    void previewValidatorPreservesNestedAnyForUnqualifiedJourneyDescriptor() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("any", List.of(
                                Map.of("field", "line.dsc", "operator", "EQUALS_NORMALIZED", "value", "M2"),
                                Map.of("field", "serviceCategory.dsc", "operator", "EQUALS_NORMALIZED", "value", "M2"),
                                Map.of("field", "transportOperator.dsc", "operator", "EQUALS_NORMALIZED", "value", "M2")))));
        AlertAgentGenerationPreviewData data = previewData(condition);

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));
        Map<String, Object> preservedCondition =
                (Map<String, Object>) ((Map<String, Object>) blueprint(data, "EVENT", false).getParameters()).get("condition");

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(preservedCondition).isEqualTo(condition);
    }

    @Test
    void previewValidatorAcceptsStopPointIdEquals() {
        AlertAgentGenerationPreviewData data = previewData(stopPointIdEqualsCondition());

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("EQUALS");
    }

    @Test
    void previewValidatorAcceptsStopPointIdIn() {
        AlertAgentGenerationPreviewData data = previewData(stopPointIdInCondition(List.of(
                "TNPNTS00000000000028",
                "TNPNTS00000000000029")));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("IN");
    }

    @Test
    void previewValidatorAcceptsStopPointIdNotIn() {
        AlertAgentGenerationPreviewData data = previewData(stopPointIdNotInCondition(List.of(
                "TNPNTS00000000000028",
                "TNPNTS00000000000029")));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("NOT_IN");
    }

    @Test
    void previewValidatorAcceptsJourneyStopPointIdNotIn() {
        AlertAgentGenerationPreviewData data = previewData(stopPointIdNotInCondition(
                "payload.stopPointJourney.stopPoint.id",
                List.of("TNPNTS00000000000028", "TNPNTS00000000000029")));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("NOT_IN");
    }

    @Test
    void previewValidatorRejectsStopPointIdNotInEmptyValues() {
        AlertAgentGenerationPreviewData data = previewData(stopPointIdNotInCondition(
                "payload.stopPointJourney.stopPoint.id",
                List.of()));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("missing value/values")
                && error.contains("NOT_IN"));
    }

    @Test
    void previewValidatorRejectsStopPointIdInEmptyValues() {
        AlertAgentGenerationPreviewData data = previewData(stopPointIdInCondition(List.of()));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("missing value/values")
                && error.contains("IN"));
    }

    @Test
    void previewValidatorAcceptsPlatformNotEqualsField() {
        AlertAgentGenerationPreviewData data = previewData(platformFieldComparisonCondition(true));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("PLATFORM_NOT_EQUALS_FIELD");
    }

    @Test
    void previewValidatorAcceptsHumanPlatformOperatorsAndMovement() {
        AlertAgentGenerationPreviewData data = previewData(platformMovementCondition());

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains(
                "EQUAL_PLATFORM", "NOT_EQUAL_PLATFORM", "IN_PLATFORMS", "NOT_IN_PLATFORMS");
    }

    @Test
    void previewValidatorAcceptsPlatformStringsRecognizedByNormalizer() {
        for (String value : List.of("1", "01", "Platform 1", "Binario 1", "PL 1", "PL1", "3A", "03A")) {
            AlertAgentGenerationPreviewData data = previewData(platformLeafCondition(
                    "timetabledDeparturePlatform.dsc", "EQUAL_PLATFORM", "value", value));

            AgentBlueprintValidationResult result =
                    validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

            assertThat(result.valid()).as(value).isTrue();
        }
    }

    @Test
    void previewValidatorRejectsPlatformStringsWithoutRecognizedNumber() {
        for (String value : List.of("Platform foo", "unknown", "-", "A")) {
            AlertAgentGenerationPreviewData data = previewData(platformLeafCondition(
                    "timetabledDeparturePlatform.dsc", "IN_PLATFORMS", "values", List.of("1", value)));

            AgentBlueprintValidationResult result =
                    validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

            assertThat(result.valid()).as(value).isFalse();
            assertThat(result.errors()).anyMatch(error -> error.contains("human platform values"));
        }
    }

    @Test
    void previewValidatorAcceptsAdvancedPlatformNumericOperators() {
        for (Map<String, Object> condition : List.of(
                platformEventBoundCondition("DEPARTING", "actualDeparturePlatform.platform.dsc",
                        "PLATFORM_NUMBER_GREATER_THAN", "value", 5),
                platformEventBoundCondition("DEPARTED", "timetabledDeparturePlatform.dsc",
                        "PLATFORM_NUMBER_GREATER_OR_EQUAL", "value", 0),
                platformEventBoundCondition("DEPARTED", "timetabledDeparturePlatform.dsc",
                        "PLATFORM_NUMBER_LESS_THAN", "value", -1),
                platformEventBoundCondition("ARRIVED", "actualArrivalPlatform.platform.dsc",
                        "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3, "max", 8)),
                platformEventBoundCondition("ARRIVED", "actualArrivalPlatform.platform.dsc",
                        "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3, "max", 3)),
                platformEventBoundValuelessCondition("DEPARTED", "actualDeparturePlatform.platform.dsc",
                        "PLATFORM_NUMBER_EVEN"))) {
            AlertAgentGenerationPreviewData data = previewData(condition);

            AgentBlueprintValidationResult result =
                    validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

            assertThat(result.valid()).isTrue();
            assertThat(result.runtimeSupported()).isTrue();
        }
    }

    @Test
    void previewValidatorRejectsAdvancedPlatformNumericOperatorWithoutEventBinding() {
        AlertAgentGenerationPreviewData data = previewData(platformLeafCondition(
                "actualDeparturePlatform.platform.dsc", "PLATFORM_NUMBER_GREATER_THAN", "value", 5));

        AgentBlueprintValidationResult result =
                validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.runtimeSupported()).isFalse();
        assertThat(result.errors()).contains(
                "Platform numeric/property predicates must include payload.ongroundServiceEvent.eventsType "
                        + "to bind the predicate to a current ServiceData event.");
    }

    @Test
    void previewValidatorRejectsInvalidAdvancedPlatformNumericConditions() {
        for (Map<String, Object> condition : List.of(
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3)),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3.5, "max", 8)),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3, "max", 8.5)),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_BETWEEN", "value", Map.of("min", 3, "max", 2147483648L)),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_MULTIPLE_OF", "value", 0),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_MULTIPLE_OF", "value", -3),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_MULTIPLE_OF", "value", 2.5),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_THAN", "value", 2.5),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_THAN", "value", 2147483648L),
                platformLeafCondition("timetabledDeparturePlatform.dsc", "PLATFORM_NUMBER_GREATER_THAN", "value", "2"),
                platformLeafCondition("timetabledDeparturePlatform.id", "PLATFORM_NUMBER_GREATER_THAN", "value", 5))) {
            AlertAgentGenerationPreviewData data = previewData(condition);

            AgentBlueprintValidationResult result =
                    validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

            assertThat(result.valid()).isFalse();
        }
    }

    @Test
    void previewValidatorRejectsEmptyPlatformValuesArrays() {
        for (String operator : List.of("IN_PLATFORMS", "NOT_IN_PLATFORMS")) {
            AlertAgentGenerationPreviewData data = previewData(platformLeafCondition(
                    "timetabledDeparturePlatform.dsc",
                    operator,
                    "values",
                    List.of()));

            AgentBlueprintValidationResult result =
                    validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

            assertThat(result.valid()).as(operator).isFalse();
            assertThat(result.errors()).as(operator)
                    .anyMatch(error -> error.contains("missing value/values") && error.contains(operator));
        }
    }

    @Test
    void previewValidatorRejectsPlatformNotEqualsFieldWithoutOtherField() {
        AlertAgentGenerationPreviewData data = previewData(platformFieldComparisonCondition(false));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("missing otherField")
                && error.contains("PLATFORM_NOT_EQUALS_FIELD"));
    }

    @Test
    void previewValidatorRejectsPlatformNotEqualsFieldWithStopPointOtherField() {
        AlertAgentGenerationPreviewData data = previewData(platformFieldComparisonCondition(
                "timetabledDeparturePlatform.dsc",
                "timetabledCallStart.stopPoint.id"));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("otherField is not a supported platform description field"));
    }

    @Test
    void previewValidatorRejectsPlatformNotEqualsFieldWithTechnicalId() {
        AlertAgentGenerationPreviewData data = previewData(platformFieldComparisonCondition(
                "timetabledDeparturePlatform.dsc",
                "actualDeparturePlatform.platform.id"));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("otherField cannot be a platform technical id field"));
    }

    @Test
    void previewValidatorRejectsPlatformNotEqualsFieldOnTechnicalId() {
        AlertAgentGenerationPreviewData data = previewData(platformFieldComparisonCondition(
                "actualDeparturePlatform.platform.id",
                "actualDeparturePlatform.platform.dsc"));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("platform technical id field is not supported"));
    }

    @Test
    void previewValidatorAcceptsCurrentDeparturePlatformChangedWithStructuralComparison() {
        AlertAgentGenerationPreviewData data = previewData(currentDeparturePlatformChangedCondition());

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("CONTAINS", "PLATFORM_NOT_EQUALS_FIELD");
    }

    @Test
    void previewValidatorAcceptsCurrentEventPlatformChangedAnyWithStructuralComparison() {
        AlertAgentGenerationPreviewData data = previewData(platformChangedAnyCondition());

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("CONTAINS_ANY", "PLATFORM_NOT_EQUALS_FIELD");
    }

    @Test
    void rejectsUnsupportedSource() {
        AlertAgentGenerationPreviewData data = previewData(condition("CONTAINS_ANY", true));

        AgentBlueprintValidationResult result = validate(
                data,
                blueprint(data, "EVENT", false),
                List.of("MONITORED_AUDIO_MESSAGE"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("Unsupported source"));
    }

    @Test
    void rejectsStatefulBlueprintWithLegacyScheduledTrigger() {
        AlertAgentGenerationPreviewData data = previewData(condition("CONTAINS_ANY", true));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "SCHEDULED", true), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("requiresState"));
    }

    @Test
    void rejectsUnsupportedDslOperator() {
        AlertAgentGenerationPreviewData data = previewData(condition("MAGIC_OPERATOR", true));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("MAGIC_OPERATOR"));
    }

    @Test
    void rejectsConditionLeafWithEmptyValues() {
        AlertAgentGenerationPreviewData data = previewData(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of("any", List.of(Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                        "operator", "CONTAINS_ANY",
                        "values", List.of()))))));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("condition.all[0].any[0]")
                && error.contains("missing value/values")
                && error.contains("CONTAINS_ANY"));
    }

    @Test
    void rejectsUnknownConditionNodeWithKeysInDiagnostic() {
        AlertAgentGenerationPreviewData data = previewData(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "customExpression", "unsupported"));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("condition")
                && error.contains("expected all/any or field/operator/value node")
                && error.contains("customExpression"));
    }

    @Test
    void acceptsCorrelatedNextCallsTemporalCondition() {
        AlertAgentGenerationPreviewData data = previewData(nextCallsTemporalCondition(
                "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]", "Europe/Rome"));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("LOCAL_TIME_BETWEEN", "EQUALS_NORMALIZED");
    }

    @Test
    void acceptsEventGenerationTimeTemporalConditionAndRejectsInventedTemporalOperator() {
        AlertAgentGenerationPreviewData validData = previewData(eventTemporalCondition("LOCAL_TIME_BETWEEN"));
        AlertAgentGenerationPreviewData invalidData = previewData(eventTemporalCondition("LOCAL_TIME_AFTER"));

        AgentBlueprintValidationResult valid = validate(
                validData, blueprint(validData, "EVENT", false), List.of("SERVICE_DATA"));
        AgentBlueprintValidationResult invalid = validate(
                invalidData, blueprint(invalidData, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(valid.valid()).isTrue();
        assertThat(valid.runtimeSupported()).isTrue();
        assertThat(invalid.valid()).isFalse();
        assertThat(invalid.errors()).anyMatch(error -> error.contains("LOCAL_TIME_AFTER"));
    }

    @Test
    void acceptsLocalDayOfWeekTemporalConditionAndAppliesDefaultTimezone() {
        AlertAgentGenerationPreviewData data = previewData(eventDayOfWeekCondition(
                "LOCAL_DAY_OF_WEEK_IN",
                List.of("TUESDAY"),
                null));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("LOCAL_DAY_OF_WEEK_IN");
    }

    @Test
    void acceptsCorrelatedStopPointJourneyDetailsWeekendCondition() {
        AlertAgentGenerationPreviewData data = previewData(stopPointJourneyDetailsWeekendCondition());

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
        assertThat(result.detectedDslOperators()).contains("LOCAL_DAY_OF_WEEK_IN", "EQUALS_NORMALIZED");
    }

    @Test
    void rejectsInvalidDayTimezoneNonTemporalFieldAndFlattenedArrayTemporalCondition() {
        AlertAgentGenerationPreviewData invalidDay = previewData(eventDayOfWeekCondition(
                "LOCAL_DAY_OF_WEEK_IN",
                List.of("MARTEDI"),
                "Europe/Rome"));
        AlertAgentGenerationPreviewData invalidTimezone = previewData(eventDayOfWeekCondition(
                "LOCAL_DAY_OF_WEEK_IN",
                List.of("TUESDAY"),
                "Europe/NotAZone"));
        AlertAgentGenerationPreviewData nonTemporal = previewData(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName",
                        "operator", "LOCAL_DAY_OF_WEEK_IN",
                        "value", Map.of("days", List.of("TUESDAY"), "timezone", "Europe/Rome")))));
        AlertAgentGenerationPreviewData flattenedArray = previewData(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova P.P"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].timetabledCallStart.departureTime",
                                "operator", "LOCAL_DAY_OF_WEEK_IN",
                                "value", Map.of("days", List.of("SATURDAY", "SUNDAY"), "timezone", "Europe/Rome")))));

        AgentBlueprintValidationResult invalidDayResult = validate(invalidDay, blueprint(invalidDay, "EVENT", false), List.of("SERVICE_DATA"));
        AgentBlueprintValidationResult invalidTimezoneResult = validate(invalidTimezone, blueprint(invalidTimezone, "EVENT", false), List.of("SERVICE_DATA"));
        AgentBlueprintValidationResult nonTemporalResult = validate(nonTemporal, blueprint(nonTemporal, "EVENT", false), List.of("SERVICE_DATA"));
        AgentBlueprintValidationResult flattenedArrayResult = validate(flattenedArray, blueprint(flattenedArray, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(invalidDayResult.valid()).isFalse();
        assertThat(invalidDayResult.errors()).anyMatch(error -> error.contains("MARTEDI"));
        assertThat(invalidTimezoneResult.valid()).isFalse();
        assertThat(invalidTimezoneResult.errors()).anyMatch(error -> error.contains("timezone"));
        assertThat(nonTemporalResult.valid()).isFalse();
        assertThat(nonTemporalResult.errors()).anyMatch(error -> error.contains("field is not supported"));
        assertThat(flattenedArrayResult.valid()).isFalse();
        assertThat(flattenedArrayResult.errors()).anyMatch(error -> error.contains("anyElement"));
    }

    @Test
    void rejectsTemporalAnyElementWithUnsupportedPathAndDefaultsMissingTimezone() {
        AlertAgentGenerationPreviewData unsupportedPath = previewData(nextCallsTemporalCondition(
                "payload.stopPointJourney.otherCalls[]", "Europe/Rome"));
        AlertAgentGenerationPreviewData missingTimezone = previewData(nextCallsTemporalCondition(
                "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]", null));

        AgentBlueprintValidationResult unsupportedResult = validate(
                unsupportedPath, blueprint(unsupportedPath, "EVENT", false), List.of("SERVICE_DATA"));
        AgentBlueprintValidationResult missingTimezoneResult = validate(
                missingTimezone, blueprint(missingTimezone, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(unsupportedResult.valid()).isFalse();
        assertThat(unsupportedResult.errors()).anyMatch(error -> error.contains("path is not supported"));
        assertThat(missingTimezoneResult.valid()).isTrue();
        assertThat(missingTimezoneResult.runtimeSupported()).isTrue();
    }

    @Test
    void rejectsTemporalAnyElementWithoutCorrelatedStopPointLeaf() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                                "conditions", Map.of("all", List.of(Map.of(
                                        "field", "departureTime",
                                        "operator", "LOCAL_TIME_BETWEEN",
                                        "value", Map.of(
                                                "start", "11:30:00",
                                                "end", "12:35:00",
                                                "timezone", "Europe/Rome"))))))));
        AlertAgentGenerationPreviewData data = previewData(condition);

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("correlate stopPoint.nameLong"));
    }

    @Test
    void acceptsNestedStopPointJourneyDetailsChildArrayAnyElement() {
        AlertAgentGenerationPreviewData data = previewData(nestedTransitTuesdayCondition());

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isTrue();
        assertThat(result.runtimeSupported()).isTrue();
    }

    @Test
    void rejectsFlattenedSiblingAnyElementsAcrossStopPointJourneyDetailsAndChildArray() {
        AlertAgentGenerationPreviewData data = previewData(flattenedTransitTuesdayCondition());

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "EVENT", false), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("nested anyElement is required")
                && error.contains("same stopPointsJourneyDetails element"));
    }

    private AgentBlueprintValidationResult validate(
            AlertAgentGenerationPreviewData data,
            AgentBlueprint blueprint,
            List<String> sources) {
        return validator.validate(
                data.alertId(),
                data,
                blueprint,
                extractor.extract(data),
                sources,
                catalog.permissionsForSources(sources),
                "DSL",
                "DSL");
    }

    private AgentBlueprint blueprint(AlertAgentGenerationPreviewData data, String triggerType, boolean requiresState) {
        return new AgentBlueprint()
                .agentName("JourneyCancellationGenovaPiazzaPrincipeAgent")
                .description("Detects cancelled journeys.")
                .triggerType(AgentBlueprint.TriggerTypeEnum.fromString(triggerType))
                .requiredSources(List.of(AgentDataSource.SERVICE_DATA))
                .targetTypes(List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY))
                .parameters(Map.of(
                        "conditionType", "SERVICE_DATA_FIELD_MATCH",
                        "condition", data.technicalSpecification().get("condition")))
                .stateRequirements(Map.of("requiresState", requiresState))
                .suggestionIntent(Map.of())
                .putAdditionalProperty("schemaVersion", "iia.agent.blueprint/v1")
                .putAdditionalProperty("evaluationMode", "STATELESS_EVENT_MATCH")
                .putAdditionalProperty("output", Map.of("type", "CANDIDATE_SUGGESTION"));
    }

    private AlertAgentGenerationPreviewData previewData(Map<String, Object> condition) {
        Map<String, Object> technical = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition));
        return new AlertAgentGenerationPreviewData(
                "ALRT1", "Alert", "VERIFIED", "VERIFIED", false, null, 1, "Prompt", "Verified",
                null, null, "EVENT_INTERPRETER", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION",
                technical, blueprint, List.of(), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AgentBlueprint scheduledBlueprint(AlertAgentGenerationPreviewData data, Map<String, Object> overrides) {
        Map<String, Object> technical = data.technicalSpecification();
        java.util.LinkedHashMap<String, Object> parameters = new java.util.LinkedHashMap<>();
        parameters.put("serviceDataQuery", technical.get("serviceDataQuery"));
        parameters.put("snapshotEvaluation", technical.get("snapshotEvaluation"));
        parameters.put("outputPolicy", technical.get("outputPolicy"));
        parameters.put("schedule", technical.get("schedule"));
        parameters.put("conditionType", "SERVICE_DATA_SCHEDULED_FIELD_MATCH");
        parameters.putAll(overrides);
        return new AgentBlueprint()
                .agentName("ScheduledServiceDataSnapshotAlertAgent")
                .description("Reports scheduled journeys.")
                .triggerType(AgentBlueprint.TriggerTypeEnum.SCHEDULE)
                .requiredSources(List.of(AgentDataSource.SERVICE_DATA))
                .targetTypes(List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY_AGGREGATE))
                .parameters(parameters)
                .stateRequirements(Map.of("requiresState", false))
                .suggestionIntent(Map.of())
                .putAdditionalProperty("schemaVersion", "iia.agent.blueprint/v1")
                .putAdditionalProperty("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH")
                .putAdditionalProperty("output", Map.of("type", "CANDIDATE_SUGGESTION"));
    }

    private AlertAgentGenerationPreviewData scheduledPreviewData() {
        return scheduledPreviewData("SERVICE_DATA_SCHEDULED_FIELD_MATCH");
    }

    private AlertAgentGenerationPreviewData scheduledPreviewData(String conditionType) {
        Map<String, Object> condition = Map.of(
                "type", conditionType,
                "all", List.of(Map.of(
                        "field", "callStart.stopPoint.id",
                        "operator", "EQUALS",
                        "value", "TNPNTS00000000000122")));
        Map<String, Object> serviceDataQuery = Map.of(
                "operation", "POST /v2/stoppointjourneys",
                "stopPoints", List.of("TNPNTS00000000000009"),
                "timeWindow", Map.of(
                        "startMode", "NOW_TRUNCATED_TO_MINUTE",
                        "endMode", "NOW_PLUS_DURATION",
                        "lookaheadMinutes", 180));
        Map<String, Object> snapshotEvaluation = Map.of(
                "mode", "REPORT_COUNT",
                "journeyPath", "stopPointsJourneyDetails[]",
                "condition", condition);
        Map<String, Object> outputPolicy = Map.of(
                "emit", "EVERY_RUN",
                "includeCount", true,
                "includeMatchingJourneys", false);
        java.util.LinkedHashMap<String, Object> technical = new java.util.LinkedHashMap<>();
        technical.put("source", "SERVICE_DATA");
        technical.put("schedule", Map.of("frequencySeconds", 600, "defaulted", false, "rawText", "Ogni 10 minuti"));
        technical.put("accessMode", "SERVICE_DATA_API_SNAPSHOT");
        technical.put("inputModel", "ServiceDataStopPointJourneysV2");
        technical.put("outputModel", "AgentOutput.CANDIDATE_SUGGESTION");
        technical.put("triggerType", "SCHEDULE");
        technical.put("outputPolicy", outputPolicy);
        technical.put("evaluationMode", "SCHEDULED_SNAPSHOT_MATCH");
        technical.put("interpreterType", "SCHEDULED_INTERPRETER");
        technical.put("serviceDataQuery", serviceDataQuery);
        technical.put("snapshotEvaluation", snapshotEvaluation);
        Map<String, Object> blueprint = Map.of(
                "parameters", Map.of(
                        "serviceDataQuery", serviceDataQuery,
                        "snapshotEvaluation", snapshotEvaluation,
                        "outputPolicy", outputPolicy,
                        "schedule", technical.get("schedule")));
        return new AlertAgentGenerationPreviewData(
                "ALRT_SCHEDULED", "Scheduled", "VERIFIED", "VERIFIED", false, null, 1, "Prompt", "Verified",
                null, null, "SCHEDULED_INTERPRETER", "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                technical, blueprint, List.of(), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY_AGGREGATE));
    }

    private Map<String, Object> condition(String statusOperator, boolean values) {
        Map<String, Object> statusLeaf = values
                ? Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                        "operator", statusOperator,
                        "values", List.of("ARRIVAL_CANCELLATION"))
                : Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                        "operator", statusOperator,
                        "value", "ARRIVAL_CANCELLATION");
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("any", List.of(statusLeaf)),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova Piazza Principe")));
    }

    private Map<String, Object> delayCondition(boolean values) {
        Map<String, Object> delayLeaf = values
                ? Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].delayStatus",
                        "operator", "CONTAINS_IGNORE_CASE",
                        "values", List.of("DELAYED"))
                : Map.of(
                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].delayStatus",
                        "operator", "CONTAINS_IGNORE_CASE",
                        "value", "DELAYED");
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        delayLeaf,
                        Map.of(
                                "field", "payload.stopPointJourney.serviceCategory",
                                "operator", "EQUALS_IGNORE_CASE",
                                "value", "Intercity"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova Nervi")));
    }

    private Map<String, Object> realisticDelayCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].serviceCategory.dsc",
                                "operator", "CONTAINS_NORMALIZED",
                                "value", "intercity"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "genova nervi"),
                        Map.of("any", List.of(
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalDelay.delay",
                                        "operator", "GREATER_THAN",
                                        "value", 0),
                        Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                                        "operator", "CONTAINS_ANY",
                                        "values", List.of("ARRIVAL_DELAY"))))));
    }

    private Map<String, Object> stopPointIdEqualsCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.ongroundServiceEvent.stopPoint.id",
                        "operator", "EQUALS",
                        "value", "TNPNTS00000000005467")));
    }

    private Map<String, Object> stopPointIdInCondition(List<String> ids) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.ongroundServiceEvent.stopPoint.id",
                        "operator", "IN",
                        "values", ids)));
    }

    private Map<String, Object> stopPointIdNotInCondition(List<String> ids) {
        return stopPointIdNotInCondition("payload.ongroundServiceEvent.stopPoint.id", ids);
    }

    private Map<String, Object> stopPointIdNotInCondition(String field, List<String> ids) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", field,
                        "operator", "NOT_IN",
                        "values", ids)));
    }

    private Map<String, Object> nextCallsTemporalCondition(String path, String timezone) {
        Map<String, Object> temporalValue = new java.util.LinkedHashMap<>();
        temporalValue.put("start", "11:30:00");
        temporalValue.put("end", "12:35:00");
        if (timezone != null) {
            temporalValue.put("timezone", timezone);
        }
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "ARRIVED"),
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
                                                                "field", "departureTime",
                                                                "operator", "LOCAL_TIME_BETWEEN",
                                                                "value", temporalValue)))))));
    }

    private Map<String, Object> platformFieldComparisonCondition(boolean includeOtherField) {
        Map<String, Object> leaf = new java.util.LinkedHashMap<>();
        leaf.put("field", "timetabledDeparturePlatform.dsc");
        leaf.put("operator", "PLATFORM_NOT_EQUALS_FIELD");
        if (includeOtherField) {
            leaf.put("otherField", "actualDeparturePlatform.platform.dsc");
        }
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", leaf));
    }

    private Map<String, Object> platformFieldComparisonCondition(String field, String otherField) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of(
                                "field", field,
                                "operator", "PLATFORM_NOT_EQUALS_FIELD",
                                "otherField", otherField)));
    }

    private Map<String, Object> platformLeafCondition(
            String field,
            String operator,
            String valueKey,
            Object value) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of(
                                "field", field,
                                "operator", operator,
                                valueKey, value)));
    }

    private Map<String, Object> platformValuelessCondition(String field, String operator) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of(
                                "field", field,
                                "operator", operator)));
    }

    private Map<String, Object> platformEventBoundCondition(
            String eventType,
            String field,
            String operator,
            String valueKey,
            Object value) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", eventType),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of(
                                                "field", field,
                                                "operator", operator,
                                                valueKey, value)))));
    }

    private Map<String, Object> platformEventBoundValuelessCondition(
            String eventType,
            String field,
            String operator) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", eventType),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of(
                                                "field", field,
                                                "operator", operator)))));
    }

    private Map<String, Object> platformMovementCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of("all", List.of(
                                Map.of(
                                        "field", "previousDeparturePlatform.platform.dsc",
                                        "operator", "EQUAL_PLATFORM",
                                        "value", "5"),
                                Map.of(
                                        "field", "actualDeparturePlatform.platform.dsc",
                                        "operator", "IN_PLATFORMS",
                                        "values", List.of("7", "8")),
                                Map.of(
                                        "field", "timetabledDeparturePlatform.dsc",
                                        "operator", "NOT_IN_PLATFORMS",
                                        "values", List.of("1", "12")),
                                Map.of(
                                        "field", "timetabledArrivalPlatform.dsc",
                                        "operator", "NOT_EQUAL_PLATFORM",
                                        "value", "3")))));
    }

    private Map<String, Object> currentDeparturePlatformChangedCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "DEPARTURE_PLATFORM_CHANGED"),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of(
                                                "field", "timetabledDeparturePlatform.dsc",
                                                "operator", "PLATFORM_NOT_EQUALS_FIELD",
                                                "otherField", "actualDeparturePlatform.platform.dsc")))));
    }

    private Map<String, Object> platformChangedAnyCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS_ANY",
                                "values", List.of("DEPARTURE_PLATFORM_CHANGED", "ARRIVAL_PLATFORM_CHANGED")),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of(
                                                "field", "timetabledDeparturePlatform.dsc",
                                                "operator", "PLATFORM_NOT_EQUALS_FIELD",
                                                "otherField", "actualDeparturePlatform.platform.dsc")))));
    }

    private Map<String, Object> eventTemporalCondition(String operator) {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.ongroundServiceEvent.eventGenerationTime",
                        "operator", operator,
                        "value", Map.of(
                                "start", "02:00:00",
                                "end", "10:00:00",
                                "timezone", "Europe/Rome"))));
    }

    private Map<String, Object> eventDayOfWeekCondition(String operator, List<String> days, String timezone) {
        Map<String, Object> value = new java.util.LinkedHashMap<>();
        value.put("days", days);
        if (timezone != null) {
            value.put("timezone", timezone);
        }
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "field", "payload.ongroundServiceEvent.eventGenerationTime",
                        "operator", operator,
                        "value", value)));
    }

    private Map<String, Object> stopPointJourneyDetailsWeekendCondition() {
        return Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                "conditions", Map.of(
                                        "all", List.of(
                                                Map.of(
                                                        "field", "timetabledCallStart.stopPoint.nameLong",
                                                        "operator", "EQUALS_NORMALIZED",
                                                        "value", "Genova P.P"),
                                                Map.of(
                                                        "field", "timetabledCallStart.departureTime",
                                                        "operator", "LOCAL_DAY_OF_WEEK_IN",
                                                        "value", Map.of(
                                                                "days", List.of("SATURDAY", "SUNDAY"),
                                                                "timezone", "Europe/Rome"))))))));
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
}
