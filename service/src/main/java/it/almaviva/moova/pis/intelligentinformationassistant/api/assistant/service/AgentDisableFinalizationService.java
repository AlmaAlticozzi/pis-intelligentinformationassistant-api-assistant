package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimePackageRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
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

    @Inject
    AgentRuntimePackageRepository runtimePackageRepository;

    public AgentDisableFinalizationService() {
    }

    AgentDisableFinalizationService(
            AgentDefinitionRepository agentDefinitionRepository,
            RuntimeCatalogLifecyclePublisher runtimeCatalogLifecyclePublisher) {
        this.agentDefinitionRepository = agentDefinitionRepository;
        this.runtimeCatalogLifecyclePublisher = runtimeCatalogLifecyclePublisher;
    }

    @Transactional
    public FinalizationResult finalizeAcceptedDisable(String agentDefinitionId, String runtimePackageId,
            long packageVersion, String submissionId, String packageFingerprint,
            OffsetDateTime activationEpoch, OffsetDateTime disabledAt) {
        AgentDefinition definition = agentDefinitionRepository.findByDefinitionIdForUpdate(agentDefinitionId)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
        AgentRuntimePackage runtimePackage = runtimePackageRepository.findByIdOptional(runtimePackageId)
                .orElseThrow(() -> new AgentDisableTechnicalException("Current Runtime Agent Package was not found during disable finalization."));
        boolean identity = runtimePackageId.equals(definition.getCodCurrentruntimepackage())
                && runtimePackage.getNumPackageversion() == packageVersion
                && runtimePackage.getCodSubmissionid().equals(submissionId)
                && runtimePackage.getDscPackagefingerprint().equals(packageFingerprint);
        if (!identity) throw new AgentDisableTechnicalException("Current Runtime Agent Package identity changed during disable finalization.");
        String status = definition.getSglStatus().getSglStatus();
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status))
            throw new AgentDisableRejectedException(status, "Agent Definition status changed during disable.");
        OffsetDateTime lifecycleEpoch = "ACTIVE".equals(status) ? disabledAt : definition.getDtUpdatedat();
        if ("ACTIVE".equals(status)) {
            definition.setSglStatus(agentDefinitionRepository.statusReference("DISABLED"));
            definition.setDtUpdatedat(lifecycleEpoch);
        }
        AgentRuntimeCatalogChangeDecision decision = runtimeCatalogLifecyclePublisher.appendRemoveWithDecision(
                agentDefinitionId, "DISABLED", lifecycleEpoch).decision();
        System.out.println("[IIA][AGENT_DISABLE][FINALIZATION] agentDefinitionId=" + agentDefinitionId
                + " previousStatus=" + status + " targetStatus=DISABLED currentRuntimePackageId=" + runtimePackageId
                + " catalogAction=REMOVE removalReason=NOT_ACTIVE catalogChangeDecision=" + decision + " committed=true");
        return "ACTIVE".equals(status) ? FinalizationResult.COMMITTED : FinalizationResult.ALREADY_APPLIED;
    }

    @Transactional
    public boolean finalizeAcceptedDisable(String agentDefinitionId, String expectedStatus,
            String targetStatus, OffsetDateTime sourceUpdatedAt) {
        int updated = agentDefinitionRepository.transitionStatus(agentDefinitionId, expectedStatus, targetStatus, sourceUpdatedAt);
        if (updated != 1) return false;
        runtimeCatalogLifecyclePublisher.appendRemove(agentDefinitionId, targetStatus, sourceUpdatedAt);
        return true;
    }

    public enum FinalizationResult { COMMITTED, ALREADY_APPLIED }
}
