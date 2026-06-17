package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public record AgentActivationCommand(
        String agentDefinitionId,
        String note,
        boolean startImmediatelyIfAllowed) {

    public AgentActivationCommand {
        agentDefinitionId = requireText(agentDefinitionId, "agentDefinitionId is required");
        note = normalizeOptionalText(note);
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
