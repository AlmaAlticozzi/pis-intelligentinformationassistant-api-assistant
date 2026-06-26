package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;

@ApplicationScoped
public class AgentDisableFinalizationService {

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Inject
    RuntimeCatalogLifecyclePublisher runtimeCatalogLifecyclePublisher;

    public AgentDisableFinalizationService() {
    }

    AgentDisableFinalizationService(
            AgentDefinitionRepository agentDefinitionRepository,
            RuntimeCatalogLifecyclePublisher runtimeCatalogLifecyclePublisher) {
        this.agentDefinitionRepository = agentDefinitionRepository;
        this.runtimeCatalogLifecyclePublisher = runtimeCatalogLifecyclePublisher;
    }

    @Transactional
    public boolean finalizeAcceptedDisable(
            String agentDefinitionId,
            String expectedStatus,
            String targetStatus,
            OffsetDateTime sourceUpdatedAt) {
        int updated = agentDefinitionRepository.transitionStatus(
                agentDefinitionId,
                expectedStatus,
                targetStatus,
                sourceUpdatedAt);
        if (updated != 1) {
            return false;
        }
        runtimeCatalogLifecyclePublisher.appendRemove(agentDefinitionId, targetStatus, sourceUpdatedAt);
        return true;
    }
}
