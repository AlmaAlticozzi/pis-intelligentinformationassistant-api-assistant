package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import jakarta.persistence.EntityManager;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentCompilationRepositoryTest {

    @Test
    void createCompilationCreatesPendingRecord() {
        AgentCompilationRepository repository = repositoryWithEntityManager();
        AgentDefinition definition = new AgentDefinition();
        AgentCompilationStatus pending = status("PENDING");
        when(repository.entityManager.getReference(AgentDefinition.class, "AGDF1")).thenReturn(definition);
        when(repository.entityManager.getReference(AgentCompilationStatus.class, "PENDING")).thenReturn(pending);

        AgentCompilation compilation = repository.createCompilation(
                "AGDF1",
                "DSL",
                true,
                Map.of("force", true),
                "user1");

        assertThat(compilation.getCodAgentdefinition()).isSameAs(definition);
        assertThat(compilation.getSglStatus().getSglStatus()).isEqualTo("PENDING");
        assertThat(compilation.getDscRequestedmode()).isEqualTo("DSL");
        assertThat(compilation.getFlgForce()).isTrue();
        assertThat(compilation.getJsnRequest()).containsEntry("force", true);
        verify(repository.entityManager).persist(compilation);
        verify(repository.entityManager).flush();
    }

    @Test
    void addStepCreatesStepWithCompositeId() {
        AgentCompilationRepository repository = repositoryWithEntityManager();
        AgentCompilation compilation = new AgentCompilation();
        AgentCompilationStatus status = status("GENERATING_BLUEPRINT");
        OffsetDateTime startedAt = OffsetDateTime.parse("2026-06-15T10:15:30Z");
        OffsetDateTime completedAt = OffsetDateTime.parse("2026-06-15T10:16:30Z");
        when(repository.entityManager.getReference(AgentCompilation.class, "AGCP1")).thenReturn(compilation);
        when(repository.entityManager.getReference(AgentCompilationStatus.class, "GENERATING_BLUEPRINT")).thenReturn(status);

        AgentCompilationStep step = repository.addStep(
                "AGCP1",
                2,
                "GENERATE_BLUEPRINT",
                "GENERATING_BLUEPRINT",
                "ok",
                Map.of("attempt", 1),
                startedAt,
                completedAt);

        assertThat(step.getId().getCodAgentcompilation()).isEqualTo("AGCP1");
        assertThat(step.getId().getNumSteporder()).isEqualTo(2);
        assertThat(step.getDscStepname()).isEqualTo("GENERATE_BLUEPRINT");
        assertThat(step.getSglStatus().getSglStatus()).isEqualTo("GENERATING_BLUEPRINT");
        assertThat(step.getJsnDetails()).containsEntry("attempt", 1);
        assertThat(step.getDtStartedat()).isEqualTo(startedAt);
        assertThat(step.getDtCompletedat()).isEqualTo(completedAt);
        verify(repository.entityManager).persist(step);
        verify(repository.entityManager).flush();
    }

    @Test
    void findLatestByAgentDefinitionIdOrdersByRequestedAtAndId() {
        AgentCompilationRepository repository = repositoryWithEntityManager();
        @SuppressWarnings("unchecked")
        TypedQuery<AgentCompilation> query = mock(TypedQuery.class);
        AgentCompilation latest = new AgentCompilation();
        when(repository.entityManager.createQuery(anyString(), eq(AgentCompilation.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultStream()).thenReturn(List.of(latest).stream());

        assertThat(repository.findLatestByAgentDefinitionId("AGDF1")).containsSame(latest);

        ArgumentCaptor<String> jpqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository.entityManager).createQuery(jpqlCaptor.capture(), eq(AgentCompilation.class));
        assertThat(jpqlCaptor.getValue()).contains("order by compilation.dtRequestedat desc, compilation.codAgentcompilation desc");
        verify(query).setParameter("agentDefinitionId", "AGDF1");
        verify(query).setMaxResults(1);
    }

    @Test
    void findStepsByCompilationIdOrdersByStepOrder() {
        AgentCompilationRepository repository = repositoryWithEntityManager();
        @SuppressWarnings("unchecked")
        TypedQuery<AgentCompilationStep> query = mock(TypedQuery.class);
        when(repository.entityManager.createQuery(anyString(), eq(AgentCompilationStep.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        repository.findStepsByCompilationId("AGCP1");

        ArgumentCaptor<String> jpqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository.entityManager).createQuery(jpqlCaptor.capture(), eq(AgentCompilationStep.class));
        assertThat(jpqlCaptor.getValue()).contains("order by step.id.numSteporder");
        verify(query).setParameter("compilationId", "AGCP1");
    }

    @Test
    void existsRunningCompilationReturnsTrueOnlyWhenRunningRowsExist() {
        AgentCompilationRepository repository = repositoryWithEntityManager();
        @SuppressWarnings("unchecked")
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(repository.entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L, 0L);

        assertThat(repository.existsRunningCompilation("AGDF1")).isTrue();
        assertThat(repository.existsRunningCompilation("AGDF1")).isFalse();
        verify(query, times(2)).setParameter("runningStatuses", AgentCompilationStatuses.RUNNING);
    }

    @Test
    void markCompletedSetsFinalStatusResultAndCompletedAt() {
        AgentCompilationRepository repository = repositoryWithEntityManager();
        AgentCompilation compilation = new AgentCompilation();
        AgentCompilationStatus ready = status("READY");
        OffsetDateTime completedAt = OffsetDateTime.parse("2026-06-15T11:00:00Z");
        when(repository.entityManager.find(AgentCompilation.class, "AGCP1")).thenReturn(compilation);
        when(repository.entityManager.getReference(AgentCompilationStatus.class, "READY")).thenReturn(ready);

        repository.markCompleted("AGCP1", "READY", "SIGNING", Map.of("artifactUri", "artifact://1"), completedAt);

        assertThat(compilation.getSglStatus().getSglStatus()).isEqualTo("READY");
        assertThat(compilation.getDscCurrentstep()).isEqualTo("SIGNING");
        assertThat(compilation.getJsnResult()).containsEntry("artifactUri", "artifact://1");
        assertThat(compilation.getDscErrormessage()).isNull();
        assertThat(compilation.getDtCompletedat()).isEqualTo(completedAt);
        verify(repository.entityManager).flush();
    }

    @Test
    void markFailedSetsFinalStatusErrorMessageAndCompletedAt() {
        AgentCompilationRepository repository = repositoryWithEntityManager();
        AgentCompilation compilation = new AgentCompilation();
        AgentCompilationStatus failed = status("FAILED");
        OffsetDateTime completedAt = OffsetDateTime.parse("2026-06-15T11:00:00Z");
        when(repository.entityManager.find(AgentCompilation.class, "AGCP1")).thenReturn(compilation);
        when(repository.entityManager.getReference(AgentCompilationStatus.class, "FAILED")).thenReturn(failed);

        repository.markFailed("AGCP1", "FAILED", "TESTING", "tests failed", Map.of("failedTests", 2), completedAt);

        assertThat(compilation.getSglStatus().getSglStatus()).isEqualTo("FAILED");
        assertThat(compilation.getDscCurrentstep()).isEqualTo("TESTING");
        assertThat(compilation.getDscErrormessage()).isEqualTo("tests failed");
        assertThat(compilation.getJsnResult()).containsEntry("failedTests", 2);
        assertThat(compilation.getDtCompletedat()).isEqualTo(completedAt);
        verify(repository.entityManager).flush();
    }

    @Test
    void nullRequestJsonDefaultsToEmptyObject() {
        AgentCompilationRepository repository = new AgentCompilationRepository();

        assertThat(repository.toJsonMapOrEmpty(null)).isEmpty();
        assertThat(repository.toNullableJsonMap(null)).isNull();
    }

    private AgentCompilationRepository repositoryWithEntityManager() {
        AgentCompilationRepository repository = new AgentCompilationRepository();
        repository.entityManager = mock(EntityManager.class);
        return repository;
    }

    private AgentCompilationStatus status(String value) {
        AgentCompilationStatus status = new AgentCompilationStatus();
        status.setSglStatus(value);
        status.setDscStatus(value);
        status.setNumSortorder(1);
        return status;
    }
}
