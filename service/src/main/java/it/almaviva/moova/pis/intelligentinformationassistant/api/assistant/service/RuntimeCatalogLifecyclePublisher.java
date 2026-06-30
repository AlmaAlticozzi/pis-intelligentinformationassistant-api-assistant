package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimeCatalogChangeRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimeCatalogChange;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogRemovalReason;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@ApplicationScoped
public class RuntimeCatalogLifecyclePublisher {

    @Inject
    AgentRuntimeCatalogChangeRepository catalogChangeRepository;

    @Inject
    EntityManager entityManager;

    public RuntimeCatalogLifecyclePublisher() {
    }

    RuntimeCatalogLifecyclePublisher(
            AgentRuntimeCatalogChangeRepository catalogChangeRepository,
            EntityManager entityManager) {
        this.catalogChangeRepository = catalogChangeRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public AgentRuntimeCatalogChange appendUpsert(
            String agentDefinitionId,
            AgentRuntimePackage runtimePackage,
            OffsetDateTime sourceUpdatedAt) {
        String deduplicationKey = "UPSERT:" + agentDefinitionId + ":" + runtimePackage.getNumPackageversion()
                + ":" + runtimePackage.getDscPackagefingerprint();
        AgentRuntimeCatalogChange existing = catalogChangeRepository.findByDeduplicationKey(deduplicationKey).orElse(null);
        if (existing != null) {
            boolean equivalent = existing.getSglAction() == it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogAction.UPSERT
                    && existing.getCodAgentdefinition().getCodAgentdefinition().equals(agentDefinitionId)
                    && existing.getCodRuntimepackage() != null
                    && existing.getCodRuntimepackage().getCodRuntimepackage().equals(runtimePackage.getCodRuntimepackage())
                    && existing.getNumPackageversion().equals(runtimePackage.getNumPackageversion())
                    && existing.getDscPackagefingerprint().equals(runtimePackage.getDscPackagefingerprint())
                    && "ACTIVE".equals(existing.getSglSourceagentstatus());
            if (!equivalent) {
                throw new AgentActivationTechnicalException("Runtime catalog UPSERT deduplication identity conflicts with existing data.");
            }
            System.out.println("[IIA][RUNTIME_CATALOG_CHANGE] agentDefinitionId=" + agentDefinitionId
                    + " runtimePackageId=" + runtimePackage.getCodRuntimepackage()
                    + " packageVersion=" + runtimePackage.getNumPackageversion()
                    + " catalogChangeId=" + existing.getCodCatalogchange()
                    + " decision=REUSED");
            return existing;
        }
        AgentRuntimeCatalogChange change = AgentRuntimeCatalogChange.upsert(
                catalogChangeRepository.nextCatalogChangeId(),
                entityManager.getReference(AgentDefinition.class, agentDefinitionId),
                entityManager.getReference(AgentRuntimePackage.class, runtimePackage.getCodRuntimepackage()),
                deduplicationKey,
                sourceUpdatedAt,
                sourceUpdatedAt,
                Map.of());
        catalogChangeRepository.append(change);
        System.out.println("[IIA][RUNTIME_CATALOG_CHANGE] action=UPSERT agentDefinitionId=" + agentDefinitionId
                + " runtimePackageId=" + runtimePackage.getCodRuntimepackage()
                + " packageVersion=" + runtimePackage.getNumPackageversion()
                + " catalogChangeId=" + change.getCodCatalogchange()
                + " decision=CREATED changeSequence=" + change.getNumChangesequence());
        return change;
    }

    @Transactional
    public AgentRuntimeCatalogChange appendRemove(
            String agentDefinitionId,
            String sourceAgentStatus,
            OffsetDateTime sourceUpdatedAt) {
        return appendRemoveWithDecision(agentDefinitionId, sourceAgentStatus, sourceUpdatedAt).change();
    }

    @Transactional
    public RemoveAppendResult appendRemoveWithDecision(
            String agentDefinitionId,
            String sourceAgentStatus,
            OffsetDateTime sourceUpdatedAt) {
        String deduplicationKey = "REMOVE:" + agentDefinitionId + ":" + sourceAgentStatus + ":" + normalized(sourceUpdatedAt);
        AgentRuntimeCatalogChange existing = catalogChangeRepository.findByDeduplicationKey(deduplicationKey).orElse(null);
        if (existing != null) {
            boolean equivalent = existing.getSglAction() == it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogAction.REMOVE
                    && existing.getCodAgentdefinition().getCodAgentdefinition().equals(agentDefinitionId)
                    && sourceAgentStatus.equals(existing.getSglSourceagentstatus())
                    && existing.getSglRemovalreason() == RuntimeCatalogRemovalReason.NOT_ACTIVE
                    && existing.getCodRuntimepackage() == null && existing.getNumPackageversion() == null
                    && existing.getDscPackagefingerprint() == null;
            if (!equivalent) throw new AgentDisableTechnicalException("Runtime catalog REMOVE deduplication identity conflicts with existing data.");
            return new RemoveAppendResult(existing, AgentRuntimeCatalogChangeDecision.REUSED);
        }
        AgentRuntimeCatalogChange change = AgentRuntimeCatalogChange.remove(
                catalogChangeRepository.nextCatalogChangeId(),
                entityManager.getReference(AgentDefinition.class, agentDefinitionId),
                sourceAgentStatus,
                RuntimeCatalogRemovalReason.NOT_ACTIVE,
                deduplicationKey,
                sourceUpdatedAt,
                sourceUpdatedAt,
                Map.of());
        catalogChangeRepository.append(change);
        System.out.println("[IIA][RUNTIME_CATALOG_CHANGE] action=REMOVE agentDefinitionId=" + agentDefinitionId
                + " sourceStatus=" + sourceAgentStatus
                + " changeSequence=" + change.getNumChangesequence());
        return new RemoveAppendResult(change, AgentRuntimeCatalogChangeDecision.CREATED);
    }

    private String normalized(OffsetDateTime timestamp) {
        return timestamp == null ? "null" : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(timestamp);
    }

    public record RemoveAppendResult(AgentRuntimeCatalogChange change, AgentRuntimeCatalogChangeDecision decision) { }
}
