package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentValidationPlanBuilderTest {

    private final AgentPreviewConditionExtractor extractor = new AgentPreviewConditionExtractor();
    private final AgentValidationPlanBuilder builder = new AgentValidationPlanBuilder();

    @Test
    void platformConstraintPlanDocumentsNormalizedAndInPlatformMatching() {
        AgentValidationPlan plan = plan(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                        "conditions", Map.of(
                                "field", "timetabledDeparturePlatform.dsc",
                                "operator", "IN_PLATFORMS",
                                "values", List.of("1", "4")))));

        assertThat(descriptions(plan.getPositiveExamples()))
                .contains(
                        "ServiceData platform \"Platform 1\" matches user platform \"1\".",
                        "ServiceData platform \"Platform 4\" matches IN_PLATFORMS [\"1\", \"4\"].");
        assertThat(descriptions(plan.getNegativeExamples()))
                .contains(
                        "ServiceData platform \"Platform 11\" does not match user platform \"1\".",
                        "ServiceData platform \"Platform 14\" does not match IN_PLATFORMS [\"1\", \"4\"].");
        assertThat(plan.getEdgeCases())
                .contains("ServiceData platform \"Platform 01\" matches normalized user platform \"1\".");
    }

    @Test
    void platformChangeAndMovementPlanDocumentsStructuralGuards() {
        AgentValidationPlan plan = plan(Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.ongroundServiceEvent.eventsType",
                                "operator", "CONTAINS",
                                "value", "DEPARTURE_PLATFORM_CHANGED"),
                        Map.of(
                                "anyElement", Map.of(
                                        "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                                        "conditions", Map.of("all", List.of(
                                                Map.of(
                                                        "field", "timetabledDeparturePlatform.dsc",
                                                        "operator", "PLATFORM_NOT_EQUALS_FIELD",
                                                        "otherField", "actualDeparturePlatform.platform.dsc"),
                                                Map.of(
                                                        "field", "previousDeparturePlatform.platform.dsc",
                                                        "operator", "EQUAL_PLATFORM",
                                                        "value", "5"),
                                                Map.of(
                                                        "field", "actualDeparturePlatform.platform.dsc",
                                                        "operator", "IN_PLATFORMS",
                                                        "values", List.of("7", "8")))))))));

        assertThat(descriptions(plan.getPositiveExamples()))
                .contains(
                        "ServiceData current eventsType contains DEPARTURE_PLATFORM_CHANGED and timetabled departure platform differs from actual departure platform.",
                        "ServiceData previous departure platform is \"5\" and actual departure platform matches IN_PLATFORMS [\"7\", \"8\"].");
        assertThat(descriptions(plan.getNegativeExamples()))
                .contains(
                        "ServiceData current eventsType contains DEPARTURE_PLATFORM_CHANGED but timetabled departure platform equals actual departure platform.",
                        "ServiceData previous departure platform is \"5\" but actual departure platform is \"9\".");
    }

    private AgentValidationPlan plan(Map<String, Object> condition) {
        AlertAgentGenerationPreviewData data = new AlertAgentGenerationPreviewData(
                "ALRT1", "Platform", "VERIFIED", "VERIFIED", false, null, 1,
                "Prompt", "Verified.", null, null, "EVENT_INTERPRETER", "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                Map.of("condition", condition),
                Map.of("parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition)),
                List.of("SERVICE_DATA_FIELD_MATCH"), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
        return builder.build(data, extractor.extract(data));
    }

    private List<String> descriptions(List<it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationExample> examples) {
        return examples.stream().map(example -> example.getDescription()).toList();
    }
}
