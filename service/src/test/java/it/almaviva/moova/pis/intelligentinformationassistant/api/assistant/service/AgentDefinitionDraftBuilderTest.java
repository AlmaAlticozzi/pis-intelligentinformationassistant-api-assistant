package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDslPreview;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AgentDefinitionDraftBuilderTest {

    @Test
    void validPreviewCreatesFutureDefinitionCandidateWithoutRuntimeActivation() {
        AgentDefinitionDraftBuilder builder = new AgentDefinitionDraftBuilder(new AgentGenerationCapabilityCatalog());
        AgentBlueprint blueprint = new AgentBlueprint()
                .agentName("PlatformChangeAnyLocationAgent")
                .description("Detects platform changes.")
                .parameters(Map.of("conditionType", "SERVICE_DATA_FIELD_MATCH"));
        AgentDslPreview dslPreview = new AgentDslPreview().supportedByRuntime(true);
        AgentBlueprintValidationResult validationResult = new AgentBlueprintValidationResult(
                true,
                true,
                List.of(),
                List.of(),
                List.of(),
                Set.of("CONTAINS"),
                Set.of("SERVICE_DATA"),
                Set.of("SERVICE_DATA_JOURNEY"),
                false,
                "EVENT",
                "STATELESS_EVENT_MATCH",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION");

        AgentDefinitionDraftCandidate candidate = builder.prepare(
                previewData(),
                blueprint,
                AgentGenerationMode.DSL,
                AgentComplexity.LOW,
                List.of("SERVICE_DATA"),
                List.of("READ_SERVICE_DATA"),
                validationResult,
                dslPreview,
                new AgentValidationPlan(),
                AgentDefinitionDraftBuilder.PreviewSource.LLM_VALIDATED);

        assertThat(candidate.sourceAlertId()).isEqualTo("ALRT1");
        assertThat(candidate.sourceAlertVersion()).isEqualTo(3);
        assertThat(candidate.generationMode()).isEqualTo(AgentGenerationMode.DSL);
        assertThat(candidate.blueprint()).isSameAs(blueprint);
        assertThat(candidate.dslPreview()).isSameAs(dslPreview);
        assertThat(candidate.runtimeContract())
                .containsEntry("source", "SERVICE_DATA")
                .containsEntry("requiresExternalTools", false)
                .containsEntry("requiresNetworkAccess", false)
                .containsEntry("requiresFilesystemAccess", false);
        assertThat(section(blueprint, "generationContext")).containsEntry("previewSource", "LLM_VALIDATED");
        assertThat(section(blueprint, "generationReadiness"))
                .containsEntry("readyForAgentDefinition", true)
                .containsEntry("requiresHumanReview", true);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> section(AgentBlueprint blueprint, String key) {
        return (Map<String, Object>) blueprint.getParameters().get(key);
    }

    private AlertAgentGenerationPreviewData previewData() {
        return new AlertAgentGenerationPreviewData(
                "ALRT1",
                "Platform changes",
                "VERIFIED",
                "VERIFIED",
                false,
                null,
                3,
                "Prompt",
                "Verified",
                null,
                null,
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                List.of());
    }
}
