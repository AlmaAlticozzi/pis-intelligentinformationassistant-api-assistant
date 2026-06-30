package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentRuntimeCatalogChangeRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimeCatalogChange;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogAction;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogRemovalReason;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class RuntimeCatalogLifecyclePublisher {

    @Inject
    AgentRuntimeCatalogChangeRepository catalogChangeRepository;

    @Inject
    EntityManager entityManager;

    public RuntimeCatalogLifecyclePublisher() {
    }

    RuntimeCatalogLifecyclePublisher(AgentRuntimeCatalogChangeRepository catalogChangeRepository,
            EntityManager entityManager) {
        this.catalogChangeRepository = catalogChangeRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public AgentRuntimeCatalogChange appendUpsert(String agentDefinitionId, AgentRuntimePackage runtimePackage,
            OffsetDateTime sourceUpdatedAt) {
        String key = upsertKey(agentDefinitionId, runtimePackage, sourceUpdatedAt);
        AgentRuntimeCatalogChange existing = catalogChangeRepository.findByDeduplicationKey(key).orElse(null);
        if (existing == null) {
            existing = catalogChangeRepository.findLatestByAgentDefinitionId(agentDefinitionId)
                    .filter(change -> equivalentUpsert(change, agentDefinitionId, runtimePackage, sourceUpdatedAt))
                    .orElse(null);
        }
        if (existing != null) {
            if (!equivalentUpsert(existing, agentDefinitionId, runtimePackage, sourceUpdatedAt)) {
                throw new AgentActivationTechnicalException(
                        "Runtime catalog UPSERT deduplication identity conflicts with existing data.");
            }
            System.out.println("[IIA][RUNTIME_CATALOG_CHANGE] agentDefinitionId=" + agentDefinitionId
                    + " runtimePackageId=" + runtimePackage.getCodRuntimepackage()
                    + " packageVersion=" + runtimePackage.getNumPackageversion()
                    + " catalogChangeId=" + existing.getCodCatalogchange() + " decision=REUSED");
            return existing;
        }
        AgentRuntimeCatalogChange change = AgentRuntimeCatalogChange.upsert(
                catalogChangeRepository.nextCatalogChangeId(),
                entityManager.getReference(AgentDefinition.class, agentDefinitionId),
                entityManager.getReference(AgentRuntimePackage.class, runtimePackage.getCodRuntimepackage()),
                key, sourceUpdatedAt, sourceUpdatedAt, Map.of());
        catalogChangeRepository.append(change);
        System.out.println("[IIA][RUNTIME_CATALOG_CHANGE] action=UPSERT agentDefinitionId=" + agentDefinitionId
                + " runtimePackageId=" + runtimePackage.getCodRuntimepackage()
                + " packageVersion=" + runtimePackage.getNumPackageversion()
                + " catalogChangeId=" + change.getCodCatalogchange()
                + " decision=CREATED changeSequence=" + change.getNumChangesequence());
        return change;
    }

    @Transactional
    public AgentRuntimeCatalogChange appendRemove(String agentDefinitionId, String sourceAgentStatus,
            OffsetDateTime sourceUpdatedAt) {
        return appendRemoveWithDecision(agentDefinitionId, sourceAgentStatus, sourceUpdatedAt).change();
    }

    @Transactional
    public RemoveAppendResult appendRemoveWithDecision(String agentDefinitionId, String sourceAgentStatus,
            OffsetDateTime sourceUpdatedAt) {
        String key = removeKey(agentDefinitionId, sourceUpdatedAt);
        AgentRuntimeCatalogChange existing = catalogChangeRepository.findByDeduplicationKey(key).orElse(null);
        if (existing == null) {
            existing = catalogChangeRepository.findLatestByAgentDefinitionId(agentDefinitionId)
                    .filter(change -> equivalentRemove(change, agentDefinitionId, sourceAgentStatus, sourceUpdatedAt))
                    .orElse(null);
        }
        if (existing != null) {
            if (!equivalentRemove(existing, agentDefinitionId, sourceAgentStatus, sourceUpdatedAt)) {
                throw new AgentDisableTechnicalException(
                        "Runtime catalog REMOVE deduplication identity conflicts with existing data.");
            }
            return new RemoveAppendResult(existing, AgentRuntimeCatalogChangeDecision.REUSED);
        }
        AgentRuntimeCatalogChange change = AgentRuntimeCatalogChange.remove(
                catalogChangeRepository.nextCatalogChangeId(),
                entityManager.getReference(AgentDefinition.class, agentDefinitionId), sourceAgentStatus,
                RuntimeCatalogRemovalReason.NOT_ACTIVE, key, sourceUpdatedAt, sourceUpdatedAt, Map.of());
        catalogChangeRepository.append(change);
        System.out.println("[IIA][RUNTIME_CATALOG_CHANGE] action=REMOVE agentDefinitionId=" + agentDefinitionId
                + " sourceStatus=" + sourceAgentStatus + " changeSequence=" + change.getNumChangesequence());
        return new RemoveAppendResult(change, AgentRuntimeCatalogChangeDecision.CREATED);
    }

    private boolean equivalentUpsert(AgentRuntimeCatalogChange existing, String agentDefinitionId,
            AgentRuntimePackage runtimePackage, OffsetDateTime sourceUpdatedAt) {
        return existing.getSglAction() == RuntimeCatalogAction.UPSERT
                && existing.getCodAgentdefinition().getCodAgentdefinition().equals(agentDefinitionId)
                && existing.getCodRuntimepackage() != null
                && existing.getCodRuntimepackage().getCodRuntimepackage().equals(runtimePackage.getCodRuntimepackage())
                && existing.getNumPackageversion().equals(runtimePackage.getNumPackageversion())
                && existing.getDscPackagefingerprint().equals(runtimePackage.getDscPackagefingerprint())
                && "ACTIVE".equals(existing.getSglSourceagentstatus())
                && sameInstant(existing.getDtSourceupdatedat(), sourceUpdatedAt);
    }

    private boolean equivalentRemove(AgentRuntimeCatalogChange existing, String agentDefinitionId,
            String sourceAgentStatus, OffsetDateTime sourceUpdatedAt) {
        return existing.getSglAction() == RuntimeCatalogAction.REMOVE
                && existing.getCodAgentdefinition().getCodAgentdefinition().equals(agentDefinitionId)
                && sourceAgentStatus.equals(existing.getSglSourceagentstatus())
                && existing.getSglRemovalreason() == RuntimeCatalogRemovalReason.NOT_ACTIVE
                && existing.getCodRuntimepackage() == null && existing.getNumPackageversion() == null
                && existing.getDscPackagefingerprint() == null
                && sameInstant(existing.getDtSourceupdatedat(), sourceUpdatedAt);
    }

    private String upsertKey(String agentDefinitionId, AgentRuntimePackage runtimePackage,
            OffsetDateTime sourceUpdatedAt) {
        String material = String.join("\u0000", "UPSERT", agentDefinitionId,
                runtimePackage.getCodRuntimepackage(), Long.toString(runtimePackage.getNumPackageversion()),
                runtimePackage.getDscPackagefingerprint().toLowerCase(Locale.ROOT), normalized(sourceUpdatedAt));
        return digestKey("UPSERT", material);
    }

    private String removeKey(String agentDefinitionId, OffsetDateTime sourceUpdatedAt) {
        String material = String.join("\u0000", "REMOVE", agentDefinitionId,
                RuntimeCatalogRemovalReason.NOT_ACTIVE.name(), normalized(sourceUpdatedAt));
        return digestKey("REMOVE", material);
    }

    private String digestKey(String action, String material) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.UTF_8));
            return "DRC:" + action + ":" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private String normalized(OffsetDateTime timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Runtime catalog lifecycle sourceUpdatedAt is required.");
        }
        return timestamp.toInstant().toString();
    }

    private boolean sameInstant(OffsetDateTime first, OffsetDateTime second) {
        return first != null && second != null && first.toInstant().equals(second.toInstant());
    }

    public record RemoveAppendResult(AgentRuntimeCatalogChange change, AgentRuntimeCatalogChangeDecision decision) { }
}
