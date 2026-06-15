package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatusResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;

@ApplicationScoped
public class AgentCompilationMapper {

    @Inject
    AgentCompilationStepStatusMapper stepStatusMapper;

    @Inject
    AgentArtifactMapper agentArtifactMapper;

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
                .artifact(artifactMapper().toArtifact(definition))
                .errors(toErrors(compilation))
                .warnings(toWarnings(compilation));
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

    private List<String> toErrors(AgentCompilation compilation) {
        String status = compilation.getSglStatus() == null ? null : compilation.getSglStatus().getSglStatus();
        if ("READY".equals(status)) {
            return List.of();
        }
        LinkedHashSet<String> errors = new LinkedHashSet<>();
        Map<String, Object> result = compilation.getJsnResult();
        addStringList(errors, result == null ? null : result.get("errors"));
        addStringList(errors, result == null ? null : result.get("runtimeCompatibilityErrors"));
        String errorMessage = compilation.getDscErrormessage();
        if (errorMessage != null && !errorMessage.isBlank()) {
            errors.add(errorMessage);
        }
        return List.copyOf(errors);
    }

    private List<String> toWarnings(AgentCompilation compilation) {
        LinkedHashSet<String> warnings = new LinkedHashSet<>();
        Map<String, Object> result = compilation.getJsnResult();
        addStringList(warnings, result == null ? null : result.get("warnings"));
        addStringList(warnings, result == null ? null : result.get("runtimeCompatibilityWarnings"));
        return List.copyOf(warnings);
    }

    private void addStringList(LinkedHashSet<String> target, Object value) {
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                String text = item == null ? null : String.valueOf(item).trim();
                if (text != null && !text.isBlank()) {
                    target.add(text);
                }
            }
        } else {
            String text = value == null ? null : String.valueOf(value).trim();
            if (text != null && !text.isBlank()) {
                target.add(text);
            }
        }
    }

    private AgentArtifactMapper artifactMapper() {
        return agentArtifactMapper == null ? new AgentArtifactMapper() : agentArtifactMapper;
    }
}
