package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RuntimePackageBackfillDefinitionProcessor {

    @Inject
    RuntimePackageIdentityService runtimePackageIdentityService;

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Inject
    RuntimeCatalogLifecyclePublisher runtimeCatalogLifecyclePublisher;

    @Transactional
    public BackfillOutcome process(String agentDefinitionId) {
        RuntimePackageIdentityService.RuntimePackageMaterialization materialization =
                runtimePackageIdentityService.materializeOrReuseWithDecision(
                agentDefinitionId,
                new AgentActivationCommand(agentDefinitionId, null, true));
        AgentRuntimePackage runtimePackage = materialization.runtimePackage();
        agentDefinitionRepository.setCurrentRuntimePackage(
                agentDefinitionId,
                runtimePackage.getCodRuntimepackage(),
                runtimePackage.getDtSourceupdatedat());
        runtimeCatalogLifecyclePublisher.appendUpsert(
                agentDefinitionId,
                runtimePackage,
                runtimePackage.getDtSourceupdatedat());
        return materialization.created() ? BackfillOutcome.MATERIALIZED : BackfillOutcome.REUSED;
    }

    public enum BackfillOutcome {
        MATERIALIZED,
        REUSED
    }
}
