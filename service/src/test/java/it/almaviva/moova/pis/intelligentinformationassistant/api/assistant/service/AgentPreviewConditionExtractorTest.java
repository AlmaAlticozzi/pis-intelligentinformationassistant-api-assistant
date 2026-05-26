package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentPreviewConditionExtractorTest {

    private final AgentPreviewConditionExtractor extractor = new AgentPreviewConditionExtractor();

    @Test
    void recognizesIntercityDelayAndLocationFromConditionArtifacts() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.delayStatus",
                                "operator", "CONTAINS_IGNORE_CASE",
                                "value", "DELAYED"),
                        Map.of(
                                "field", "payload.stopPointJourney.serviceCategory",
                                "operator", "EQUALS_IGNORE_CASE",
                                "value", "INTERCITY"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Genova Nervi")));
        AgentPreviewConditionExtractor.ConditionSummary result = extractor.extract(new AlertAgentGenerationPreviewData(
                "ALRT1", "Intercity delay", "VERIFIED", "VERIFIED", false, null, 1,
                "Avverti quando un treno Intercity e in ritardo a Genova Nervi.", "Verified.",
                null, null, "EVENT_INTERPRETER", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION",
                Map.of("condition", condition),
                Map.of("parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition)),
                List.of("JOURNEY_DELAYED"), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY)));

        assertThat(result.delay()).isTrue();
        assertThat(result.cancellation()).isFalse();
        assertThat(result.serviceType()).isEqualTo("Intercity");
        assertThat(result.location()).isEqualTo("Genova Nervi");
        assertThat(result.dslOperators()).contains("CONTAINS_IGNORE_CASE", "EQUALS_IGNORE_CASE");
    }

    @Test
    void recognizesVerifiedNormalizedServiceCategoryAndNumericDelayConditions() {
        Map<String, Object> condition = Map.of(
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
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status",
                                        "operator", "CONTAINS_ANY",
                                        "values", List.of("DEPARTURE_DELAY"))))));
        AgentPreviewConditionExtractor.ConditionSummary result = extractor.extract(new AlertAgentGenerationPreviewData(
                "ALRT1", "Intercity delay", "VERIFIED", "VERIFIED", false, null, 1,
                "Avverti quando un treno Intercity e in ritardo a Genova Nervi.", "Verified.",
                null, null, "EVENT_INTERPRETER", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION",
                Map.of("condition", condition),
                Map.of("parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition)),
                List.of("JOURNEY_DELAYED"), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY)));

        assertThat(result.delay()).isTrue();
        assertThat(result.cancellation()).isFalse();
        assertThat(result.serviceType()).isEqualTo("Intercity");
        assertThat(result.location()).isEqualTo("Genova Nervi");
        assertThat(result.dslOperators()).contains(
                "CONTAINS_NORMALIZED", "EQUALS_NORMALIZED", "GREATER_THAN", "CONTAINS_ANY");
    }

    @Test
    void recognizesDepartureAndArrivalPlatformChangeWithoutInventingLocation() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "any", List.of(
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status",
                                "operator", "CONTAINS",
                                "value", "DEPARTURE_PLATFORM_CHANGED"),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                                "operator", "CONTAINS",
                                "value", "ARRIVAL_PLATFORM_CHANGED")));
        AgentPreviewConditionExtractor.ConditionSummary result = extractor.extract(new AlertAgentGenerationPreviewData(
                "ALRT1", "Platform change", "VERIFIED", "VERIFIED", false, null, 1,
                "Avverti quando un treno riceve un cambio di binario in qualsiasi localita.", "Verified.",
                null, null, "EVENT_INTERPRETER", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION",
                Map.of("condition", condition),
                Map.of("parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition)),
                List.of("PLATFORM_CHANGED"), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY)));

        assertThat(result.platformChange()).isTrue();
        assertThat(result.platformChangeLeaves()).hasSize(2);
        assertThat(result.cancellation()).isFalse();
        assertThat(result.delay()).isFalse();
        assertThat(result.location()).isNull();
    }

    @Test
    void extractsTemporalAnyElementWithoutFlatteningOrBecomingPartial() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(Map.of(
                        "anyElement", Map.of(
                                "path", "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]",
                                "conditions", Map.of("all", List.of(
                                        Map.of(
                                                "field", "stopPoint.nameLong",
                                                "operator", "EQUALS_NORMALIZED",
                                                "value", "Gorla"),
                                        Map.of(
                                                "field", "departureTime",
                                                "operator", "LOCAL_TIME_BETWEEN",
                                                "value", Map.of(
                                                        "start", "11:30:00",
                                                        "end", "12:35:00",
                                                        "timezone", "Europe/Rome"))))))));
        AgentPreviewConditionExtractor.ConditionSummary result = extractor.extract(new AlertAgentGenerationPreviewData(
                "ALRT1", "Temporal next call", "VERIFIED", "VERIFIED", false, null, 1,
                "Prompt", "Verified.", null, null, "EVENT_INTERPRETER", "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                Map.of("condition", condition),
                Map.of("parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition)),
                List.of(), List.of(), List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY)));

        assertThat(result.partial()).isFalse();
        assertThat(result.temporalFilter()).isTrue();
        assertThat(result.dslOperators()).contains("LOCAL_TIME_BETWEEN", "EQUALS_NORMALIZED");
        assertThat(result.arrayConditions()).singleElement()
                .satisfies(array -> assertThat(array.path())
                        .isEqualTo("payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]"));
    }
}
