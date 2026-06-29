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
public class AgentActivationFinalizationService {

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Inject
    AgentRuntimePackageRepository runtimePackageRepository;

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
        return finalizeInternal(
                agentDefinitionId, runtimePackage.getCodRuntimepackage(), runtimePackage.getNumPackageversion(),
                runtimePackage.getCodSubmissionid(), runtimePackage.getDscPackagefingerprint(), sourceUpdatedAt)
                == FinalizationResult.COMMITTED;
    }

    @Transactional
    public FinalizationResult finalizeAcceptedActivation(
            String agentDefinitionId,
            String runtimePackageId,
            long packageVersion,
            String submissionId,
            String packageFingerprint,
            OffsetDateTime sourceUpdatedAt) {
        return finalizeInternal(agentDefinitionId, runtimePackageId, packageVersion, submissionId,
                packageFingerprint, sourceUpdatedAt);
    }

    private FinalizationResult finalizeInternal(
            String agentDefinitionId,
            String runtimePackageId,
            long packageVersion,
            String submissionId,
            String packageFingerprint,
            OffsetDateTime sourceUpdatedAt) {
        System.out.println("[IIA][AGENT_ACTIVATION] local finalization start agentDefinitionId=" + agentDefinitionId
                + " runtimePackageId=" + runtimePackageId + " packageVersion=" + packageVersion
                + " submissionId=" + submissionId);
        AgentDefinition definition = agentDefinitionRepository.findByDefinitionIdForUpdate(agentDefinitionId)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
        AgentRuntimePackage runtimePackage = runtimePackageRepository.findByIdOptional(runtimePackageId)
                .orElseThrow(() -> new AgentActivationTechnicalException("Reserved runtime package was not found during finalization."));
        verifyPackageIdentity(definition, runtimePackage, packageVersion, submissionId, packageFingerprint);
        String currentStatus = definition.getSglStatus().getSglStatus();
        if ("ACTIVE".equals(currentStatus)) {
            if (runtimePackageId.equals(definition.getCodCurrentruntimepackage())) {
                System.out.println("[IIA][AGENT_ACTIVATION] local finalization idempotent replay agentDefinitionId="
                        + agentDefinitionId + " runtimePackageId=" + runtimePackageId + " stateChangeApplied=false");
                return FinalizationResult.ALREADY_APPLIED;
            }
            throw new AgentActivationRejectedException(currentStatus,
                    "Agent Definition is ACTIVE with a different current runtime package.");
        }
        if (!"READY".equals(currentStatus) && !"DISABLED".equals(currentStatus)) {
            throw new AgentActivationRejectedException(currentStatus,
                    "Agent Definition status no longer permits activation finalization.");
        }
        definition.setSglStatus(agentDefinitionRepository.statusReference("ACTIVE"));
        definition.setCodCurrentruntimepackage(runtimePackageId);
        definition.setDtUpdatedat(sourceUpdatedAt);
        runtimeCatalogLifecyclePublisher.appendUpsert(agentDefinitionId, runtimePackage, sourceUpdatedAt);
        System.out.println("[IIA][AGENT_ACTIVATION] local finalization committed agentDefinitionId=" + agentDefinitionId
                + " runtimePackageId=" + runtimePackageId + " stateBefore=" + currentStatus
                + " stateAfter=ACTIVE catalogAction=UPSERT stateChangeApplied=true");
        return FinalizationResult.COMMITTED;
    }

    private void verifyPackageIdentity(
            AgentDefinition definition,
            AgentRuntimePackage runtimePackage,
            long packageVersion,
            String submissionId,
            String packageFingerprint) {
        boolean matches = definition.getCodAgentdefinition().equals(
                runtimePackage.getCodAgentdefinition().getCodAgentdefinition())
                && runtimePackage.getNumPackageversion() == packageVersion
                && runtimePackage.getCodSubmissionid().equals(submissionId)
                && (packageFingerprint == null || runtimePackage.getDscPackagefingerprint().equals(packageFingerprint));
        if (!matches) {
            throw new AgentActivationTechnicalException("Reserved runtime package identity is inconsistent during finalization.");
        }
    }

    public enum FinalizationResult { COMMITTED, ALREADY_APPLIED }
}
