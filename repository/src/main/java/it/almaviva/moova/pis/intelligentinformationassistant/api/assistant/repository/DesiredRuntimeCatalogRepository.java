package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimeCatalogChange;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class DesiredRuntimeCatalogRepository {

    @Inject
    EntityManager entityManager;

    public long findCurrentUpperSequence() {
        Long value = entityManager.createQuery(
                        "select max(c.numChangesequence) from AgentRuntimeCatalogChange c", Long.class)
                .getSingleResult();
        return value == null ? 0L : value;
    }

    public List<DesiredRuntimeCatalogRow> findFullSnapshotPage(
            long catalogUpperSequence,
            OffsetDateTime lastSourceUpdatedAt,
            String lastAgentDefinitionId,
            int fetchLimit) {
        List<AgentRuntimeCatalogChange> changes = entityManager.createQuery("""
                        select c from AgentRuntimeCatalogChange c
                        join fetch c.codAgentdefinition d
                        join fetch c.codRuntimepackage p
                        where c.numChangesequence <= :upperSequence
                          and c.sglAction = it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.RuntimeCatalogAction.UPSERT
                          and c.numChangesequence = (
                              select max(latest.numChangesequence)
                              from AgentRuntimeCatalogChange latest
                              where latest.codAgentdefinition = c.codAgentdefinition
                                and latest.numChangesequence <= :upperSequence
                          )
                          and (:lastSourceUpdatedAt is null
                               or c.dtSourceupdatedat > :lastSourceUpdatedAt
                               or (c.dtSourceupdatedat = :lastSourceUpdatedAt
                                   and d.codAgentdefinition > :lastAgentDefinitionId))
                        order by c.dtSourceupdatedat asc, d.codAgentdefinition asc
                        """, AgentRuntimeCatalogChange.class)
                .setParameter("upperSequence", catalogUpperSequence)
                .setParameter("lastSourceUpdatedAt", lastSourceUpdatedAt)
                .setParameter("lastAgentDefinitionId", lastAgentDefinitionId)
                .setMaxResults(fetchLimit)
                .getResultList();
        return changes.stream().map(this::row).toList();
    }

    private DesiredRuntimeCatalogRow row(AgentRuntimeCatalogChange change) {
        var runtimePackage = change.getCodRuntimepackage();
        return new DesiredRuntimeCatalogRow(
                change.getNumChangesequence(), change.getCodCatalogchange(),
                change.getCodAgentdefinition().getCodAgentdefinition(), change.getSglAction().name(),
                change.getSglSourceagentstatus(), change.getDtSourceupdatedat(),
                runtimePackage == null ? null : runtimePackage.getCodRuntimepackage(),
                change.getNumPackageversion() == null ? 0L : change.getNumPackageversion(),
                change.getDscPackagefingerprint(),
                runtimePackage == null ? null : runtimePackage.getCodAgentdefinition().getCodAgentdefinition(),
                runtimePackage == null ? 0L : runtimePackage.getNumPackageversion(),
                runtimePackage == null ? null : runtimePackage.getCodSubmissionid(),
                runtimePackage == null ? null : runtimePackage.getDscPackagefingerprint(),
                runtimePackage == null ? null : runtimePackage.getJsnRuntimepackage());
    }
}
