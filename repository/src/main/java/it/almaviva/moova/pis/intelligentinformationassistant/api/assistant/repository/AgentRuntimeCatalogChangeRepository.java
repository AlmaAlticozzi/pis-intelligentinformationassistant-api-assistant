package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimeCatalogChange;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class AgentRuntimeCatalogChangeRepository implements PanacheRepositoryBase<AgentRuntimeCatalogChange, Long> {

    @Inject
    EntityManager entityManager;

    public Optional<AgentRuntimeCatalogChange> findByDeduplicationKey(String deduplicationKey) {
        return entityManager.createQuery("""
                        select c
                        from AgentRuntimeCatalogChange c
                        where c.dscDeduplicationkey = :deduplicationKey
                        """, AgentRuntimeCatalogChange.class)
                .setParameter("deduplicationKey", deduplicationKey)
                .getResultStream()
                .findFirst();
    }

    @Transactional
    public AgentRuntimeCatalogChange append(AgentRuntimeCatalogChange change) {
        entityManager.persist(change);
        entityManager.flush();
        return change;
    }
}
