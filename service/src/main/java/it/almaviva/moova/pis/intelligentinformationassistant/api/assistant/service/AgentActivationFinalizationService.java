package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;

@ApplicationScoped
public class AgentActivationFinalizationService {

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Inject
    RuntimeCatalogLifecyclePublisher runtimeCatalogLifecyclePublisher;

    public AgentActivationFinalizationService() {
    }

    AgentActivationFinalizationService(
            AgentDefinitionRepository agentDefinitionRepository,
            RuntimeCatalogLifecyclePublisher runtimeCatalogLifecyclePublisher) {
        this.agentDefinitionRepository = agentDefinitionRepository;
        this.runtimeCatalogLifecyclePublisher = runtimeCatalogLifecyclePublisher;
    }

    @Transactional
    public boolean finalizeAcceptedActivation(
            String agentDefinitionId,
            String expectedStatus,
            AgentRuntimePackage runtimePackage,
            OffsetDateTime sourceUpdatedAt) {
        int updated = agentDefinitionRepository.transitionStatusAndCurrentRuntimePackage(
                agentDefinitionId,
                expectedStatus,
                "ACTIVE",
                runtimePackage.getCodRuntimepackage(),
                sourceUpdatedAt);
        if (updated != 1) {
            return false;
        }
        runtimeCatalogLifecyclePublisher.appendUpsert(agentDefinitionId, runtimePackage, sourceUpdatedAt);
        return true;
    }
}
