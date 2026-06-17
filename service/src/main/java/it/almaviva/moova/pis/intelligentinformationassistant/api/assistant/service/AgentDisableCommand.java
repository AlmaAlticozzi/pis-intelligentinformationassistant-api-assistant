package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public record AgentDisableCommand(
        String agentDefinitionId,
        String reason,
        boolean stopRunningAgents,
        int gracePeriodSeconds) {

    public AgentDisableCommand {
        agentDefinitionId = requireText(agentDefinitionId, "agentDefinitionId is required");
        reason = normalizeOptionalText(reason);
        if (gracePeriodSeconds < AgentLifecycleCommandFactory.MIN_GRACE_PERIOD_SECONDS) {
            throw new IllegalArgumentException("gracePeriodSeconds must be greater than or equal to "
                    + AgentLifecycleCommandFactory.MIN_GRACE_PERIOD_SECONDS);
        }
        if (gracePeriodSeconds > AgentLifecycleCommandFactory.MAX_GRACE_PERIOD_SECONDS) {
            throw new IllegalArgumentException("gracePeriodSeconds must be less than or equal to "
                    + AgentLifecycleCommandFactory.MAX_GRACE_PERIOD_SECONDS);
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
