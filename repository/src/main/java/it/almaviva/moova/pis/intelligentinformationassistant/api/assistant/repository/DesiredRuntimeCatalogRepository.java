package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimeCatalogChange;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class DesiredRuntimeCatalogRepository {

    @Inject
    EntityManager entityManager;

    public long findCurrentUpperSequence() {
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FULL][SNAPSHOT_QUERY] operation=MAX_CHANGE_SEQUENCE status=STARTED");
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
        boolean cursorPresent = lastSourceUpdatedAt != null;
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][REPOSITORY] operation=FULL_PAGE status=EXECUTING"
                + " catalogUpperSequence=" + catalogUpperSequence + " fetchLimit=" + fetchLimit
                + " cursorPresent=" + cursorPresent);
        try {
            String cursorPredicate = cursorPresent ? """
                          and (c.dtSourceupdatedat > :lastSourceUpdatedAt
                               or (c.dtSourceupdatedat = :lastSourceUpdatedAt
                                   and d.codAgentdefinition > :lastAgentDefinitionId))
                    """ : "";
            TypedQuery<AgentRuntimeCatalogChange> query = entityManager.createQuery("""
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
                        """ + cursorPredicate + """
                        order by c.dtSourceupdatedat asc, d.codAgentdefinition asc
                        """, AgentRuntimeCatalogChange.class)
                    .setParameter("upperSequence", catalogUpperSequence)
                    .setMaxResults(fetchLimit);
            if (cursorPresent) {
                query.setParameter("lastSourceUpdatedAt", lastSourceUpdatedAt)
                        .setParameter("lastAgentDefinitionId", lastAgentDefinitionId);
            }
            List<AgentRuntimeCatalogChange> changes = query.getResultList();
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][REPOSITORY] operation=FULL_PAGE"
                    + " status=QUERY_COMPLETED rawRowCount=" + changes.size());
            List<DesiredRuntimeCatalogRow> rows = changes.stream().map(this::row).toList();
            System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][REPOSITORY] operation=FULL_PAGE"
                    + " status=MAPPING_COMPLETED mappedRowCount=" + rows.size());
            return rows;
        } catch (RuntimeException ex) {
            logFailure(ex);
            throw ex;
        }
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

    private void logFailure(RuntimeException exception) {
        Throwable root = exception;
        SQLException sqlException = null;
        for (Throwable current = exception; current != null; current = current.getCause()) {
            root = current;
            if (current instanceof SQLException sql) sqlException = sql;
        }
        System.out.println("[IIA][DESIRED_RUNTIME_CATALOG][FAILURE] stage=FULL_PAGE_QUERY"
                + " exceptionClass=" + exception.getClass().getName()
                + " rootCauseClass=" + root.getClass().getName()
                + " sqlState=" + (sqlException == null ? "null" : sqlException.getSQLState())
                + " message=" + safe(exception.getMessage())
                + " rootCauseMessage=" + safe(root.getMessage()));
    }

    private String safe(String message) {
        if (message == null) return "null";
        String normalized = message.replace('\n', ' ').replace('\r', ' ');
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500);
    }
}
