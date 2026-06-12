package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentDefinitionRepositoryTest {

    @Test
    void searchAppliesAllFiltersWithStableOrdering() {
        AgentDefinitionRepository repository = new AgentDefinitionRepository();
        EntityManager entityManager = mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<AgentDefinition> query = mock(TypedQuery.class);
        repository.entityManager = entityManager;

        when(entityManager.createQuery(anyString(), eq(AgentDefinition.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new AgentDefinition()));

        List<AgentDefinition> result = repository.search("DRAFT", "ALRT1", "DSL", "SMALL", "gerusalemme");

        ArgumentCaptor<String> jpqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(AgentDefinition.class));
        String jpql = jpqlCaptor.getValue();
        assertThat(jpql).contains("join fetch d.codAlert");
        assertThat(jpql).contains("join fetch d.codAgentprofile");
        assertThat(jpql).contains("d.sglStatus.sglStatus = :status");
        assertThat(jpql).contains("d.codAlert.codAlert = :alertId");
        assertThat(jpql).contains("d.sglGenerationmode.sglGenerationmode = :generationMode");
        assertThat(jpql).contains("d.codAgentprofile.codAgentprofile = :profileId");
        assertThat(jpql).contains("lower(d.dscName) like :textPattern");
        assertThat(jpql).contains("lower(coalesce(d.dscDescription, '')) like :textPattern");
        assertThat(jpql).contains("lower(d.codAlert.dscName) like :textPattern");
        assertThat(jpql).contains("order by d.dtCreatedat desc, d.codAgentdefinition desc");
        verify(query).setParameter("status", "DRAFT");
        verify(query).setParameter("alertId", "ALRT1");
        verify(query).setParameter("generationMode", "DSL");
        verify(query).setParameter("profileId", "SMALL");
        verify(query).setParameter("textPattern", "%gerusalemme%");
        assertThat(result).hasSize(1);
    }

    @Test
    void searchWithoutFiltersReturnsEmptyResultListWhenNoRowsMatch() {
        AgentDefinitionRepository repository = new AgentDefinitionRepository();
        EntityManager entityManager = mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<AgentDefinition> query = mock(TypedQuery.class);
        repository.entityManager = entityManager;

        when(entityManager.createQuery(anyString(), eq(AgentDefinition.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        List<AgentDefinition> result = repository.search(null, null, null, null, null);

        ArgumentCaptor<String> jpqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(AgentDefinition.class));
        assertThat(jpqlCaptor.getValue()).doesNotContain(" :status").doesNotContain(" :alertId").doesNotContain(" :textPattern");
        assertThat(result).isEmpty();
    }
}
