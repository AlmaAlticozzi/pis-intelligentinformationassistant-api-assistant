package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Objects;

public record AgentOrchestratorActivationRequest(
        String agentDefinitionId,
        AgentRuntimeSubmission submission,
        String canonicalPackageHash) {

    public AgentOrchestratorActivationRequest {
        agentDefinitionId = requireText(agentDefinitionId, "agentDefinitionId is required");
        submission = Objects.requireNonNull(submission, "submission is required");
        canonicalPackageHash = requireText(canonicalPackageHash, "canonicalPackageHash is required");

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
