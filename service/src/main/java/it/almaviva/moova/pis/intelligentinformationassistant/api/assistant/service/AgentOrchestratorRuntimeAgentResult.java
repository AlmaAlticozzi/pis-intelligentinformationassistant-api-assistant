package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.time.Instant;
import java.util.List;

public record AgentOrchestratorRuntimeAgentResult(
        String agentDefinitionId,
        String desiredStatus,
        String runtimeStatus,
        long packageVersion,
        String submissionId,
        Instant acceptedAt,
        Instant updatedAt,
        String runtimeAssignmentId,
        List<String> warnings) {

    public AgentOrchestratorRuntimeAgentResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
