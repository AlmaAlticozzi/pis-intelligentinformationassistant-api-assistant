package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.time.Instant;

public record AgentDefinitionLifecycleState(
        String agentDefinitionId,
        String status,
        Instant updatedAt,
        String currentRuntimePackageId) {
    public AgentDefinitionLifecycleState(String agentDefinitionId, String status, Instant updatedAt) {
        this(agentDefinitionId, status, updatedAt, null);
    }
}
