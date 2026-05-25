package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDataSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import org.junit.jupiter.api.Test;

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
}
