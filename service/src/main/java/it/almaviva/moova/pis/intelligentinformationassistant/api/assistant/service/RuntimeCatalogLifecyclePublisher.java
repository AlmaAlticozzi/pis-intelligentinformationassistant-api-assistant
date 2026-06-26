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
                + ":" + normalized(sourceUpdatedAt);
        AgentRuntimeCatalogChange existing = catalogChangeRepository.findByDeduplicationKey(deduplicationKey).orElse(null);
        if (existing != null) {
            return existing;
        }
        AgentRuntimeCatalogChange change = AgentRuntimeCatalogChange.upsert(
                entityManager.getReference(AgentDefinition.class, agentDefinitionId),
                entityManager.getReference(AgentRuntimePackage.class, runtimePackage.getCodRuntimepackage()),
                deduplicationKey,
                sourceUpdatedAt,
                sourceUpdatedAt,
                Map.of());
        catalogChangeRepository.append(change);
        System.out.println("[IIA][RUNTIME_CATALOG_CHANGE] action=UPSERT agentDefinitionId=" + agentDefinitionId
                + " packageVersion=" + runtimePackage.getNumPackageversion()
                + " changeSequence=" + change.getNumChangesequence());
        return change;
    }

    @Transactional
    public AgentRuntimeCatalogChange appendRemove(
            String agentDefinitionId,
            String sourceAgentStatus,
            OffsetDateTime sourceUpdatedAt) {
        String deduplicationKey = "REMOVE:" + agentDefinitionId + ":" + sourceAgentStatus + ":" + normalized(sourceUpdatedAt);
        AgentRuntimeCatalogChange existing = catalogChangeRepository.findByDeduplicationKey(deduplicationKey).orElse(null);
        if (existing != null) {
            return existing;
        }
        AgentRuntimeCatalogChange change = AgentRuntimeCatalogChange.remove(
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
        return change;
    }

    private String normalized(OffsetDateTime timestamp) {
        return timestamp == null ? "null" : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(timestamp);
    }
}
