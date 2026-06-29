package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.LinkedHashMap;

public record AgentOrchestratorActivationRequest(
        String agentDefinitionId,
        AgentRuntimeSubmission submission,
        String canonicalPackageHash,
        String runtimePackageId,
        Map<String, Object> persistedPayload) {

    public AgentOrchestratorActivationRequest(
            String agentDefinitionId,
            AgentRuntimeSubmission submission,
            String canonicalPackageHash) {
        this(agentDefinitionId, submission, canonicalPackageHash, null, null);
    }

    public AgentOrchestratorActivationRequest {
        agentDefinitionId = requireText(agentDefinitionId, "agentDefinitionId is required");
        submission = Objects.requireNonNull(submission, "submission is required");
        canonicalPackageHash = requireText(canonicalPackageHash, "canonicalPackageHash is required");
        runtimePackageId = normalizeOptionalText(runtimePackageId);
        persistedPayload = persistedPayload == null
                ? null
                : Collections.unmodifiableMap(new LinkedHashMap<>(persistedPayload));

        String bodyAgentDefinitionId = submission.agentDefinition() == null
                ? null
                : normalizeOptionalText(submission.agentDefinition().id());
        if (!agentDefinitionId.equals(bodyAgentDefinitionId)) {
            throw new IllegalArgumentException("agentDefinitionId must match submission.agentDefinition.id");
        }
    }

    private static String requireText(String value, String message) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
