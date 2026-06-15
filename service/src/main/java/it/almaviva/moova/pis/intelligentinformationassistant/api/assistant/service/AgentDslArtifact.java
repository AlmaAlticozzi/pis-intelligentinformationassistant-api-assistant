package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Map;

public record AgentDslArtifact(
        String schemaVersion,
        String artifactType,
        String agentDefinitionId,
        String interpreterType,
        String triggerType,
        String inputModel,
        String outputModel,
        String evaluationMode,
        Map<String, Object> artifact,
        Map<String, Object> diagnosticDetails) {
}
