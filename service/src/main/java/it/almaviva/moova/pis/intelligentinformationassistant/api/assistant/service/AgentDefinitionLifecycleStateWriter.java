package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class AgentDefinitionLifecycleStateWriter {

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Transactional
    public boolean transition(
            String agentDefinitionId,
            String expectedStatus,
            String targetStatus,
            Instant updatedAt) {
        OffsetDateTime timestamp = updatedAt == null
                ? OffsetDateTime.now(ZoneOffset.UTC)
                : OffsetDateTime.ofInstant(updatedAt, ZoneOffset.UTC);
        return agentDefinitionRepository.transitionStatus(agentDefinitionId, expectedStatus, targetStatus, timestamp) == 1;
    }
}
