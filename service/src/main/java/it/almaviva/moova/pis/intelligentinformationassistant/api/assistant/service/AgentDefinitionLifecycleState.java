package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.time.Instant;

public record AgentDefinitionLifecycleState(
        String agentDefinitionId,
        String status,
        Instant updatedAt) {
}
