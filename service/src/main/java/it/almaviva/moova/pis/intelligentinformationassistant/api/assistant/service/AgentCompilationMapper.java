package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifact;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatusResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class AgentCompilationMapper {

    @Inject
    AgentCompilationStepStatusMapper stepStatusMapper;

    public AgentCompilationStatusResponse toResponse(
            AgentDefinition definition,
            AgentCompilation compilation,
            List<it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep> steps) {
        if (definition == null || compilation == null) {
            return null;
        }
        return new AgentCompilationStatusResponse()
                .agentDefinitionId(definition.getCodAgentdefinition())
                .status(compilation.getSglStatus() == null ? null : AgentCompilationStatus.fromString(compilation.getSglStatus().getSglStatus()))
                .startedAt(compilation.getDtStartedat())
                .completedAt(compilation.getDtCompletedat())
                .currentStep(compilation.getDscCurrentstep())
                .steps(toSteps(steps))
                .artifact(toArtifact(definition))
                .errors(toErrors(compilation))
                .warnings(List.of());
    }

    private List<AgentCompilationStep> toSteps(
            List<it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }
        return steps.stream()
                .map(step -> new AgentCompilationStep()
                        .name(step.getDscStepname())
                        .status(stepStatusMapper.toApiStatus(step.getSglStatus() == null ? null : step.getSglStatus().getSglStatus()))
                        .startedAt(step.getDtStartedat())
                        .completedAt(step.getDtCompletedat())
                        .message(step.getDscMessage()))
                .toList();
    }

    private AgentArtifact toArtifact(AgentDefinition definition) {
        String artifactType = definition.getSglArtifacttype() == null ? null : definition.getSglArtifacttype().getSglArtifacttype();
        if (artifactType == null || "NONE".equals(artifactType)) {
            return null;
        }

        AgentArtifact artifact = new AgentArtifact()
                .artifactType(AgentArtifactType.fromString(artifactType))
                .artifactUri(definition.getDscArtifacturi())
                .artifactHash(definition.getDscArtifacthash())
                .runtimeImage(definition.getDscRuntimeimage())
                .sdkVersion(definition.getDscSdkversion())
                .implementationSummary(definition.getDscImplementationsummary());
        if (definition.getSglSignaturestatus() != null) {
            artifact.signatureStatus(AgentArtifact.SignatureStatusEnum.fromString(definition.getSglSignaturestatus().getSglSignaturestatus()));
        }
        return artifact;
    }

    private List<String> toErrors(AgentCompilation compilation) {
        String errorMessage = compilation.getDscErrormessage();
        if (errorMessage == null || errorMessage.isBlank()) {
            return List.of();
        }
        return List.of(errorMessage);
    }
}
