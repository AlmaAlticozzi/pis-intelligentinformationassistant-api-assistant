package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import java.util.List;
import java.util.Map;

public record ScheduledAgentBlueprintPreview(
        String schemaVersion,
        String agentName,
        String description,
        String triggerType,
        List<String> requiredSources,
        String evaluationMode,
        List<String> targetTypes,
        Map<String, Object> parameters,
        ScheduledAgentStateRequirements stateRequirements,
        ScheduledAgentOutput output) {

    public record ScheduledAgentStateRequirements(boolean requiresState) {
    }

    public record ScheduledAgentOutput(String type) {
    }
}
