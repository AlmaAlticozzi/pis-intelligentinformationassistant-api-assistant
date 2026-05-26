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
}
