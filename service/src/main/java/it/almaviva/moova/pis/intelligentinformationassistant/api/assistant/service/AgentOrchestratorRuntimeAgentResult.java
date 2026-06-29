package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

public record AgentOrchestratorRuntimeAgentResult(
        String agentDefinitionId,
        String desiredStatus,
        String runtimeStatus,
        long packageVersion,
        String submissionId,
        Instant acceptedAt,
        Instant updatedAt,
        String runtimeAssignmentId,
        List<String> warnings,
        int httpStatus,
        boolean responseReceived,
        String rawResponseBody,
        JsonNode parsedResponseBody,
        String outcomeCategory) {

    public AgentOrchestratorRuntimeAgentResult(
            String agentDefinitionId, String desiredStatus, String runtimeStatus, long packageVersion,
            String submissionId, Instant acceptedAt, Instant updatedAt, String runtimeAssignmentId,
            List<String> warnings) {
        this(agentDefinitionId, desiredStatus, runtimeStatus, packageVersion, submissionId, acceptedAt,
                updatedAt, runtimeAssignmentId, warnings, 0, false, null, null, null);
    }

    public AgentOrchestratorRuntimeAgentResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
