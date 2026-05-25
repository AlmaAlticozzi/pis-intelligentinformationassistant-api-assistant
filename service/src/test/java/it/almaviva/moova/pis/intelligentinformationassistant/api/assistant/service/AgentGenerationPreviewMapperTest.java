package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentGenerationPreviewMapperTest {

    private final AgentGenerationPreviewMapper mapper = new AgentGenerationPreviewMapper();

    @Test
    void defaultRequestIncludesDslAndValidationPlanUsingPersistedArtifacts() {
        AgentGenerationPreviewResponse response = mapper.toResponse(previewData(), null);

        assertThat(response.getCanGenerate()).isTrue();
        assertThat(response.getRecommendedGenerationMode()).isEqualTo(AgentGenerationMode.DSL);
        assertThat(response.getRequiredSources()).containsExactly(AgentDataSource.SERVICE_DATA);
        assertThat(response.getRequiredPermissions()).containsExactly("READ_SERVICE_DATA");
        assertThat(response.getBlueprint().getAgentName()).isEqualTo("CancelledJourneyServiceDataAgent");
        assertThat(response.getBlueprint().getTargetTypes()).containsExactly(SuggestionTargetType.SERVICE_DATA_JOURNEY);
        assertThat(response.getDslPreview().getDsl()).contains("JOURNEY_CANCELLED", "STATELESS_EVENT_MATCH");
        assertThat(response.getValidationPlan().getPositiveExamples()).hasSize(1);
        assertThat(response.getWarnings()).contains(
                "Read-only preview generated from verified Alert artifacts; no Agent Definition has been created.",
                "DSL preview is diagnostic and has not been compiled or executed.");
    }

    @Test
    void javaTemplatePreferenceDoesNotGenerateJavaAndOptionalSectionsCanBeExcluded() {
        AgentGenerationPreviewRequest request = new AgentGenerationPreviewRequest()
                .preferredGenerationMode(AgentGenerationMode.JAVA_TEMPLATE)
                .includeDslPreview(false)
                .includeValidationPlan(false);

        AgentGenerationPreviewResponse response = mapper.toResponse(previewData(), request);

        assertThat(response.getRecommendedGenerationMode()).isEqualTo(AgentGenerationMode.DSL);
        assertThat(response.getDslPreview()).isNull();
        assertThat(response.getValidationPlan()).isNull();
        assertThat(response.getWarnings()).anyMatch(warning -> warning.contains("JAVA_TEMPLATE"));
    }

    @Test
    void cancellationConditionProducesSpecificBlueprintDslAndValidationPlan() {
        AgentGenerationPreviewResponse response = mapper.toResponse(cancellationPreviewData(), null);

        assertThat(response.getBlueprint().getAgentName()).isEqualTo("JourneyCancellationMilanoMalpensaT1Agent");
        assertThat(response.getBlueprint().getDescription())
                .isEqualTo("Detects cancelled journeys at Milano Malpensa T1 from realtime ServiceData events.");
        assertThat(response.getBlueprint().getSuggestionIntent())
                .containsEntry("type", "INFORM_OPERATOR")
                .containsEntry("category", "JOURNEY_CANCELLATION")
                .containsEntry("candidateOutput", "CANDIDATE_SUGGESTION")
                .containsEntry("operatorAction", "CHECK_PASSENGER_INFORMATION_PROCEDURES");
        assertThat(response.getDslPreview().getSupportedByRuntime()).isTrue();
        assertThat(response.getDslPreview().getSummary())
                .isEqualTo("Deterministic DSL preview for a stateless ServiceData event-match Agent.");
        assertThat(response.getDslPreview().getDsl())
                .contains("agent:\n  name: JourneyCancellationMilanoMalpensaT1Agent")
                .contains("match:\n  evaluationMode: STATELESS_EVENT_MATCH\n  conditionType: SERVICE_DATA_FIELD_MATCH\n  all:\n    - any:\n        - field:")
                .contains("      operator: EQUALS_NORMALIZED\n      value: Milano Malpensa T1")
                .contains("runtime:\n  requiresState: false\n  supportedByRuntime: true");
        assertThat(response.getValidationPlan().getPositiveExamples()).hasSize(2);
        assertThat(response.getValidationPlan().getNegativeExamples()).hasSize(2);
        assertThat(response.getValidationPlan().getEdgeCases()).hasSize(3);
        assertThat(response.getValidationPlan().getPositiveExamples().getFirst().getDescription())
                .contains("ARRIVAL_CANCELLATION", "Milano Malpensa T1");
    }

    @Test
    void unsupportedConditionNodeReturnsPartialDslWithWarning() {
        Map<String, Object> technicalSpecification = new LinkedHashMap<>(previewData().technicalSpecification());
        technicalSpecification.put("condition", Map.of("unsupportedExpression", "custom"));
        AlertAgentGenerationPreviewData data = dataWithArtifacts(
                technicalSpecification,
                previewData().agentBlueprintPreview());

        AgentGenerationPreviewResponse response = mapper.toResponse(data, null);

        assertThat(response.getWarnings()).contains(
                "DSL preview is partial because some condition nodes are not supported by the deterministic renderer.");
        assertThat(response.getDslPreview().getDsl()).contains("schemaVersion: iia.agent.dsl/v1");
    }

    private AlertAgentGenerationPreviewData previewData() {
        return new AlertAgentGenerationPreviewData(
                "ALRT1",
                "Cancelled journeys",
                "VERIFIED",
                "VERIFIED",
                false,
                null,
                1,
                "Create a suggestion when a journey is cancelled.",
                "Verified.",
                null,
                null,
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                Map.of(
                        "triggerType", "EVENT",
                        "evaluationMode", "STATELESS_EVENT_MATCH",
                        "inputModel", "ServiceDataV2",
                        "outputModel", "AgentOutput.CANDIDATE_SUGGESTION"),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "agentName", "CancelledJourneyServiceDataAgent",
                        "description", "Detects cancelled journeys.",
                        "triggerType", "EVENT",
                        "requiredSources", List.of("SERVICE_DATA")),
                List.of("JOURNEY_CANCELLED"),
                List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData cancellationPreviewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "all", List.of(
                        Map.of("any", List.of(
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].arrivalStatuses[].status",
                                        "operator", "CONTAINS",
                                        "value", "ARRIVAL_CANCELLATION"),
                                Map.of(
                                        "field", "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status",
                                        "operator", "CONTAINS",
                                        "value", "DEPARTURE_CANCELLATION"))),
                        Map.of(
                                "field", "payload.stopPointJourney.stopPoint.nameLong",
                                "operator", "EQUALS_NORMALIZED",
                                "value", "Milano Malpensa T1")));
        Map<String, Object> technicalSpecification = Map.of(
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataFieldMatchAlertAgent",
                "description", "Detects matching ServiceData events using the verified stateless condition.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH", "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of(
                        "type", "CANDIDATE_SUGGESTION",
                        "reasonTemplate", "Journey ${payload.stopPointJourney.stopPointsJourneyDetails[].vehicleJourneyName} is cancelled at Milano Malpensa T1.",
                        "operatorAdviceTemplate", "Check journey cancellation and passenger information procedures."));
        return dataWithArtifacts(technicalSpecification, blueprint);
    }

    private AlertAgentGenerationPreviewData dataWithArtifacts(
            Map<String, Object> technicalSpecification,
            Map<String, Object> blueprint) {
        return new AlertAgentGenerationPreviewData(
                "ALRT1",
                "Cancelled journeys",
                "VERIFIED",
                "VERIFIED",
                false,
                null,
                1,
                "Create a suggestion when a journey is cancelled.",
                "Verified.",
                null,
                null,
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification,
                blueprint,
                List.of("SERVICE_DATA_FIELD_MATCH"),
                List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }
}
