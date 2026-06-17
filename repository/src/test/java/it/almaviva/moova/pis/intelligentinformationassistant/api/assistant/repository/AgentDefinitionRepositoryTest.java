package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactSignatureStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionAllowedTool;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionDayOfWeek;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionRequiredSource;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinitionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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

    @Test
    void findActivationSnapshotDefinitionFetchesOnlySingleValuedReferences() {
        AgentDefinitionRepository repository = new AgentDefinitionRepository();
        EntityManager entityManager = mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<AgentDefinition> query = mock(TypedQuery.class);
        repository.entityManager = entityManager;

        when(entityManager.createQuery(anyString(), eq(AgentDefinition.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(List.<AgentDefinition>of().stream());

        repository.findActivationSnapshotDefinition("AGDF1");

        ArgumentCaptor<String> jpqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpqlCaptor.capture(), eq(AgentDefinition.class));
        String jpql = jpqlCaptor.getValue();
        assertThat(jpql).contains("join fetch d.codAlert");
        assertThat(jpql).contains("join fetch d.codAgentprofile");
        assertThat(jpql).contains("left join fetch d.sglLatestcompilationstatus");
        assertThat(jpql).doesNotContain("AgentDefinitionDayOfWeek");
        assertThat(jpql).doesNotContain("AgentDefinitionRequiredSource");
        assertThat(jpql).doesNotContain("AgentDefinitionAllowedTool");
        verify(query).setParameter("agentDefinitionId", "AGDF1");
    }

    @Test
    void findLatestCompilationReferenceIdReadsRawColumnOnly() {
        AgentDefinitionRepository repository = new AgentDefinitionRepository();
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);
        repository.entityManager = entityManager;

        when(entityManager.createNativeQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of("AGCP1"));

        assertThat(repository.findLatestCompilationReferenceId("AGDF1")).contains("AGCP1");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sqlCaptor.capture(), eq(String.class));
        assertThat(sqlCaptor.getValue()).contains("cod_latestcompilation").contains("agent_definition");
        verify(query).setParameter("agentDefinitionId", "AGDF1");
    }

    @Test
    void transitionStatusUsesConditionalAtomicUpdate() {
        AgentDefinitionRepository repository = new AgentDefinitionRepository();
        EntityManager entityManager = mock(EntityManager.class);
        Query query = mock(Query.class);
        AgentDefinitionStatus disabled = definitionStatus("DISABLED");
        repository.entityManager = entityManager;

        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(entityManager.getReference(AgentDefinitionStatus.class, "DISABLED")).thenReturn(disabled);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-06-17T10:00:00Z");
        int rows = repository.transitionStatus("AGDF1", "READY", "DISABLED", updatedAt);

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<String> jpqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpqlCaptor.capture());
        assertThat(jpqlCaptor.getValue())
                .contains("update AgentDefinition d")
                .contains("d.sglStatus = :targetStatus")
                .contains("d.dtUpdatedat = :updatedAt")
                .contains("d.codAgentdefinition = :agentDefinitionId")
                .contains("d.sglStatus.sglStatus = :expectedStatus");
        verify(query).setParameter("targetStatus", disabled);
        verify(query).setParameter("updatedAt", updatedAt);
        verify(query).setParameter("agentDefinitionId", "AGDF1");
        verify(query).setParameter("expectedStatus", "READY");
    }

    @Test
    void activationRelationsUseSeparateOrderedQueries() {
        AgentDefinitionRepository repository = new AgentDefinitionRepository();
        EntityManager entityManager = mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<AgentDefinitionDayOfWeek> dayQuery = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        TypedQuery<AgentDefinitionRequiredSource> sourceQuery = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        TypedQuery<AgentDefinitionAllowedTool> toolQuery = mock(TypedQuery.class);
        repository.entityManager = entityManager;

        when(entityManager.createQuery(anyString(), eq(AgentDefinitionDayOfWeek.class))).thenReturn(dayQuery);
        when(entityManager.createQuery(anyString(), eq(AgentDefinitionRequiredSource.class))).thenReturn(sourceQuery);
        when(entityManager.createQuery(anyString(), eq(AgentDefinitionAllowedTool.class))).thenReturn(toolQuery);
        when(dayQuery.setParameter(anyString(), any())).thenReturn(dayQuery);
        when(sourceQuery.setParameter(anyString(), any())).thenReturn(sourceQuery);
        when(toolQuery.setParameter(anyString(), any())).thenReturn(toolQuery);
        when(dayQuery.getResultList()).thenReturn(List.of());
        when(sourceQuery.getResultList()).thenReturn(List.of());
        when(toolQuery.getResultList()).thenReturn(List.of());

        repository.findActivationDaysOfWeek("AGDF1");
        repository.findActivationRequiredSources("AGDF1");
        repository.findActivationAllowedTools("AGDF1");

        verify(dayQuery).setParameter("agentDefinitionId", "AGDF1");
        verify(sourceQuery).setParameter("agentDefinitionId", "AGDF1");
        verify(toolQuery).setParameter("agentDefinitionId", "AGDF1");
    }

    @Test
    void markCompilationReadyUpdatesArtifactAndLatestCompilationFields() {
        AgentDefinitionRepository repository = new AgentDefinitionRepository();
        EntityManager entityManager = mock(EntityManager.class);
        AgentDefinition definition = new AgentDefinition();
        AgentCompilation compilation = new AgentCompilation();
        AgentCompilationStatus readyCompilationStatus = compilationStatus("READY");
        repository.entityManager = entityManager;

        when(entityManager.find(AgentDefinition.class, "AGDF1")).thenReturn(definition);
        when(entityManager.getReference(AgentDefinitionStatus.class, "READY")).thenReturn(definitionStatus("READY"));
        when(entityManager.getReference(AgentArtifactType.class, "DSL")).thenReturn(artifactType("DSL"));
        when(entityManager.getReference(AgentArtifactSignatureStatus.class, "SIGNED")).thenReturn(signatureStatus("SIGNED"));
        when(entityManager.getReference(AgentCompilation.class, "AGCP1")).thenReturn(compilation);
        when(entityManager.getReference(AgentCompilationStatus.class, "READY")).thenReturn(readyCompilationStatus);

        OffsetDateTime completedAt = OffsetDateTime.parse("2026-06-15T10:15:00Z");
        repository.markCompilationReady(
                "AGDF1",
                "AGCP1",
                "iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl",
                "sha256:abc",
                "STANDARD_AGENT_DSL_EVALUATOR",
                "iia.agent.dsl/v1",
                "MVP logical signature.",
                Map.of("schemaVersion", "iia.agent.dsl/v1"),
                completedAt);

        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("READY");
        assertThat(definition.getSglArtifacttype().getSglArtifacttype()).isEqualTo("DSL");
        assertThat(definition.getDscArtifacturi()).isEqualTo("iia-agent-artifact://agent-definitions/AGDF1/compilations/AGCP1/dsl");
        assertThat(definition.getDscArtifacthash()).isEqualTo("sha256:abc");
        assertThat(definition.getSglSignaturestatus().getSglSignaturestatus()).isEqualTo("SIGNED");
        assertThat(definition.getDscRuntimeimage()).isEqualTo("STANDARD_AGENT_DSL_EVALUATOR");
        assertThat(definition.getDscSdkversion()).isEqualTo("iia.agent.dsl/v1");
        assertThat(definition.getCodLatestcompilation()).isSameAs(compilation);
        assertThat(definition.getSglLatestcompilationstatus()).isSameAs(readyCompilationStatus);
        assertThat(definition.getDscLatestcompilationstep()).isEqualTo("READY");
        assertThat(definition.getDtLatestcompilationcompletedat()).isEqualTo(completedAt);
        verify(entityManager).flush();
    }

    @Test
    void markCompilationRejectedClearsArtifactAndUpdatesLatestCompilationFields() {
        AgentDefinitionRepository repository = new AgentDefinitionRepository();
        EntityManager entityManager = mock(EntityManager.class);
        AgentDefinition definition = new AgentDefinition();
        definition.setDscArtifacturi("artifact://old");
        definition.setDscArtifacthash("sha256:old");
        AgentCompilation compilation = new AgentCompilation();
        AgentCompilationStatus rejectedCompilationStatus = compilationStatus("REJECTED");
        repository.entityManager = entityManager;

        when(entityManager.find(AgentDefinition.class, "AGDF1")).thenReturn(definition);
        when(entityManager.getReference(AgentDefinitionStatus.class, "REJECTED")).thenReturn(definitionStatus("REJECTED"));
        when(entityManager.getReference(AgentArtifactType.class, "NONE")).thenReturn(artifactType("NONE"));
        when(entityManager.getReference(AgentArtifactSignatureStatus.class, "NOT_SIGNED")).thenReturn(signatureStatus("NOT_SIGNED"));
        when(entityManager.getReference(AgentCompilation.class, "AGCP1")).thenReturn(compilation);
        when(entityManager.getReference(AgentCompilationStatus.class, "REJECTED")).thenReturn(rejectedCompilationStatus);

        OffsetDateTime completedAt = OffsetDateTime.parse("2026-06-15T10:15:00Z");
        repository.markCompilationRejected(
                "AGDF1",
                "AGCP1",
                "STATIC_ANALYSIS",
                "Runtime compatibility rejected the generated DSL artifact.",
                completedAt);

        assertThat(definition.getSglStatus().getSglStatus()).isEqualTo("REJECTED");
        assertThat(definition.getSglArtifacttype().getSglArtifacttype()).isEqualTo("NONE");
        assertThat(definition.getDscArtifacturi()).isNull();
        assertThat(definition.getDscArtifacthash()).isNull();
        assertThat(definition.getSglSignaturestatus().getSglSignaturestatus()).isEqualTo("NOT_SIGNED");
        assertThat(definition.getDscRuntimeimage()).isNull();
        assertThat(definition.getDscSdkversion()).isNull();
        assertThat(definition.getDscImplementationsummary()).isEqualTo("Runtime compatibility rejected the generated DSL artifact.");
        assertThat(definition.getCodLatestcompilation()).isSameAs(compilation);
        assertThat(definition.getSglLatestcompilationstatus()).isSameAs(rejectedCompilationStatus);
        assertThat(definition.getDscLatestcompilationstep()).isEqualTo("STATIC_ANALYSIS");
        assertThat(definition.getDtLatestcompilationcompletedat()).isEqualTo(completedAt);
        verify(entityManager).flush();
    }

    private AgentDefinitionStatus definitionStatus(String value) {
        AgentDefinitionStatus status = new AgentDefinitionStatus();
        status.setSglStatus(value);
        return status;
    }

    private AgentArtifactType artifactType(String value) {
        AgentArtifactType artifactType = new AgentArtifactType();
        artifactType.setSglArtifacttype(value);
        return artifactType;
    }

    private AgentArtifactSignatureStatus signatureStatus(String value) {
        AgentArtifactSignatureStatus signatureStatus = new AgentArtifactSignatureStatus();
        signatureStatus.setSglSignaturestatus(value);
        return signatureStatus;
    }

    private AgentCompilationStatus compilationStatus(String value) {
        AgentCompilationStatus status = new AgentCompilationStatus();
        status.setSglStatus(value);
        return status;
    }
}
