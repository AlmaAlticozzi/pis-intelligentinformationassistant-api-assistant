package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class AgentDefinitionLifecycleStateLoader {

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Transactional
    public Optional<AgentDefinitionLifecycleState> load(String agentDefinitionId) {
        return agentDefinitionRepository.findLifecycleState(agentDefinitionId)
                .map(row -> new AgentDefinitionLifecycleState(
                        row.agentDefinitionId(),
                        row.status(),
                        row.updatedAt() == null ? null : row.updatedAt().toInstant(),
                        row.currentRuntimePackageId()));
    }
}
