package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class AgentRuntimePackageRepository implements PanacheRepositoryBase<AgentRuntimePackage, String> {

    @Inject
    EntityManager entityManager;

    public Optional<AgentRuntimePackage> findCurrentByAgentDefinition(String agentDefinitionId) {
        return entityManager.createQuery("""
                        select p
                        from AgentRuntimePackage p
                        join AgentDefinition d
                            on d.codAgentdefinition = p.codAgentdefinition.codAgentdefinition
                            and d.codCurrentruntimepackage = p.codRuntimepackage
                        where d.codAgentdefinition = :agentDefinitionId
                        """, AgentRuntimePackage.class)
                .setParameter("agentDefinitionId", agentDefinitionId)
                .getResultStream()
                .findFirst();
    }

    public Optional<AgentRuntimePackage> findByAgentDefinitionAndFingerprint(
            String agentDefinitionId,
            String packageFingerprint) {
        return entityManager.createQuery("""
                        select p
                        from AgentRuntimePackage p
                        where p.codAgentdefinition.codAgentdefinition = :agentDefinitionId
                            and p.dscPackagefingerprint = :packageFingerprint
                        """, AgentRuntimePackage.class)
                .setParameter("agentDefinitionId", agentDefinitionId)
                .setParameter("packageFingerprint", packageFingerprint)
                .getResultStream()
                .findFirst();
    }

    public long findMaximumPackageVersion(String agentDefinitionId) {
        Long value = entityManager.createQuery("""
                        select max(p.numPackageversion)
                        from AgentRuntimePackage p
                        where p.codAgentdefinition.codAgentdefinition = :agentDefinitionId
                        """, Long.class)
                .setParameter("agentDefinitionId", agentDefinitionId)
                .getSingleResult();
        return value == null ? 0L : value;
    }

    @Transactional
    public AgentRuntimePackage persistImmutablePackage(AgentRuntimePackage runtimePackage) {
        entityManager.persist(runtimePackage);
        entityManager.flush();
        return runtimePackage;
    }
}
