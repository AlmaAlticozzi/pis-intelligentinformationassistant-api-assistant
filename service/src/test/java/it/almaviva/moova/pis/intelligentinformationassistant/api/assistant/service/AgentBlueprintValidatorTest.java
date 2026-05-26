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
    void rejectsUnsupportedTriggerAndStatefulBlueprint() {
        AlertAgentGenerationPreviewData data = previewData(condition("CONTAINS_ANY", true));

        AgentBlueprintValidationResult result = validate(data, blueprint(data, "SCHEDULED", true), List.of("SERVICE_DATA"));

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(error -> error.contains("triggerType"));
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
    void rejectsTemporalAnyElementWithUnsupportedPathOrMissingTimezone() {
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
        assertThat(missingTimezoneResult.valid()).isFalse();
        assertThat(missingTimezoneResult.errors()).anyMatch(error -> error.contains("timezone is required"));
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
}
