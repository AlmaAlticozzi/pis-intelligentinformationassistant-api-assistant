package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactSignatureStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStepId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentCompilationMapperTest {

    @Test
    void mapsReadyCompilationAndReadyStepToSuccessWithArtifact() {
        AgentDefinition definition = definition("DSL");
        AgentCompilation compilation = compilation("READY");
        compilation.setDscCurrentstep("SIGNING");
        compilation.setDtStartedat(OffsetDateTime.parse("2026-06-15T10:00:00Z"));
        compilation.setDtCompletedat(OffsetDateTime.parse("2026-06-15T10:05:00Z"));

        var response = mapper().toResponse(definition, compilation, List.of(step(1, "SIGNING", "READY")));

        assertThat(response.getAgentDefinitionId()).isEqualTo("AGDF1");
        assertThat(response.getStatus().toString()).isEqualTo("READY");
        assertThat(response.getCurrentStep()).isEqualTo("SIGNING");
        assertThat(response.getSteps()).hasSize(1);
        assertThat(response.getSteps().getFirst().getStatus()).isEqualTo(AgentCompilationStep.StatusEnum.SUCCESS);
        assertThat(response.getArtifact()).isNotNull();
        assertThat(response.getArtifact().getArtifactType().toString()).isEqualTo("DSL");
        assertThat(response.getArtifact().getArtifactUri()).isEqualTo("iia-agent-artifact://AGDF1/rule.yaml");
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();
    }

    @Test
    void mapsFailedAndRejectedStepsToFailed() {
        AgentCompilationMapper mapper = mapper();
        var failed = mapper.toResponse(definition("NONE"), compilation("FAILED"), List.of(step(1, "TESTING", "FAILED")));
        var rejected = mapper.toResponse(definition("NONE"), compilation("REJECTED"), List.of(step(1, "VALIDATING_BLUEPRINT", "REJECTED")));

        assertThat(failed.getSteps().getFirst().getStatus()).isEqualTo(AgentCompilationStep.StatusEnum.FAILED);
        assertThat(rejected.getSteps().getFirst().getStatus()).isEqualTo(AgentCompilationStep.StatusEnum.FAILED);
        assertThat(failed.getArtifact()).isNull();
    }

    @Test
    void mapsIntermediateStepsToRunningAndPendingToPending() {
        var response = mapper().toResponse(
                definition("NONE"),
                compilation("GENERATING_ARTIFACT"),
                List.of(
                        step(1, "WAIT", "PENDING"),
                        step(2, "GENERATE", "GENERATING_ARTIFACT"),
                        step(3, "ANALYZE", "STATIC_ANALYSIS")));

        assertThat(response.getSteps())
                .extracting(AgentCompilationStep::getStatus)
                .containsExactly(
                        AgentCompilationStep.StatusEnum.PENDING,
                        AgentCompilationStep.StatusEnum.RUNNING,
                        AgentCompilationStep.StatusEnum.RUNNING);
    }

    @Test
    void mapsErrorMessageAndEmptySteps() {
        AgentCompilation compilation = compilation("FAILED");
        compilation.setDscErrormessage("Compilation failed.");

        var response = mapper().toResponse(definition("NONE"), compilation, List.of());

        assertThat(response.getSteps()).isEmpty();
        assertThat(response.getErrors()).containsExactly("Compilation failed.");
        assertThat(response.getArtifact()).isNull();
    }

    @Test
    void mapsRejectedErrorsAndWarningsFromResultJsonWithoutDuplicates() {
        AgentCompilation compilation = compilation("REJECTED");
        compilation.setDscErrormessage("Runtime rejected.");
        compilation.setJsnResult(Map.of(
                "errors", List.of("Runtime rejected."),
                "runtimeCompatibilityErrors", List.of("Unsupported DSL operator MAGIC_OPERATOR."),
                "warnings", List.of("Functional warning."),
                "runtimeCompatibilityWarnings", List.of("Runtime warning.")));

        var response = mapper().toResponse(definition("NONE"), compilation, List.of());

        assertThat(response.getErrors())
                .containsExactly("Runtime rejected.", "Unsupported DSL operator MAGIC_OPERATOR.");
        assertThat(response.getWarnings())
                .containsExactly("Functional warning.", "Runtime warning.");
    }

    @Test
    void ignoresHistoricErrorsWhenCompilationIsReady() {
        AgentCompilation compilation = compilation("READY");
        compilation.setDscErrormessage("Old transient message.");
        compilation.setJsnResult(Map.of("errors", List.of("Old functional error.")));

        var response = mapper().toResponse(definition("DSL"), compilation, List.of());

        assertThat(response.getErrors()).isEmpty();
    }

    private AgentCompilationMapper mapper() {
        AgentCompilationMapper mapper = new AgentCompilationMapper();
        mapper.stepStatusMapper = new AgentCompilationStepStatusMapper();
        return mapper;
    }

    private AgentDefinition definition(String artifactTypeValue) {
        AgentDefinition definition = new AgentDefinition();
        definition.setCodAgentdefinition("AGDF1");
        definition.setSglArtifacttype(artifactType(artifactTypeValue));
        definition.setSglSignaturestatus(signatureStatus("SIGNED"));
        definition.setDscArtifacturi("iia-agent-artifact://AGDF1/rule.yaml");
        definition.setDscArtifacthash("sha256:abc");
        definition.setDscRuntimeimage("runtime:1.0.0");
        definition.setDscSdkversion("1.0.0");
        definition.setDscImplementationsummary("Compiled DSL artifact.");
        return definition;
    }

    private AgentCompilation compilation(String statusValue) {
        AgentCompilation compilation = new AgentCompilation();
        compilation.setCodAgentcompilation("AGCP1");
        compilation.setSglStatus(compilationStatus(statusValue));
        return compilation;
    }

    private it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep step(
            int order,
            String name,
            String statusValue) {
        AgentCompilationStepId id = new AgentCompilationStepId();
        id.setCodAgentcompilation("AGCP1");
        id.setNumSteporder(order);

        var step = new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep();
        step.setId(id);
        step.setDscStepname(name);
        step.setSglStatus(compilationStatus(statusValue));
        step.setDscMessage("message");
        return step;
    }

    private AgentCompilationStatus compilationStatus(String value) {
        AgentCompilationStatus status = new AgentCompilationStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentArtifactType artifactType(String value) {
        AgentArtifactType artifactType = new AgentArtifactType();
        artifactType.setSglArtifacttype(value);
        return artifactType;
    }

    private AgentArtifactSignatureStatus signatureStatus(String value) {
        AgentArtifactSignatureStatus status = new AgentArtifactSignatureStatus();
        status.setSglSignaturestatus(value);
        return status;
    }
}
