package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentCompilationStepId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AgentCompilationRepository implements PanacheRepositoryBase<AgentCompilation, String> {

    @Inject
    EntityManager entityManager;

    @Transactional
    public AgentCompilation createCompilation(
            String agentDefinitionId,
            String requestedMode,
            boolean force,
            Object requestJson,
            String requestedBy) {
        System.out.println("[IIA][AGENT_COMPILATION][REPOSITORY] create compilation agentDefinitionId="
                + agentDefinitionId + " requestedMode=" + requestedMode);
        OffsetDateTime now = OffsetDateTime.now();

        AgentCompilation compilation = new AgentCompilation();
        compilation.setCodAgentdefinition(entityManager.getReference(AgentDefinition.class, agentDefinitionId));
        compilation.setSglStatus(statusReference(AgentCompilationStatuses.PENDING));
        compilation.setDscRequestedmode(requestedMode);
        compilation.setFlgForce(force);
        compilation.setJsnRequest(toJsonMapOrEmpty(requestJson));
        compilation.setCodRequestedby(requestedBy);
        compilation.setDtRequestedat(now);
        compilation.setDtUpdatedat(now);

        entityManager.persist(compilation);
        entityManager.flush();
        return compilation;
    }

    @Transactional
    public AgentCompilationStep addStep(
            String compilationId,
            int stepOrder,
            String stepName,
            String status,
            String message,
            Object detailsJson,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt) {
        System.out.println("[IIA][AGENT_COMPILATION][REPOSITORY] add step compilationId="
                + compilationId + " order=" + stepOrder + " name=" + stepName + " status=" + status);

        AgentCompilationStepId id = new AgentCompilationStepId();
        id.setCodAgentcompilation(compilationId);
        id.setNumSteporder(stepOrder);

        AgentCompilationStep step = new AgentCompilationStep();
        step.setId(id);
        step.setCodAgentcompilation(entityManager.getReference(AgentCompilation.class, compilationId));
        step.setDscStepname(stepName);
        step.setSglStatus(statusReference(status));
        step.setDscMessage(message);
        step.setJsnDetails(toNullableJsonMap(detailsJson));
        step.setDtStartedat(startedAt);
        step.setDtCompletedat(completedAt);

        entityManager.persist(step);
        entityManager.flush();
        return step;
    }

    public Optional<AgentCompilation> findLatestByAgentDefinitionId(String agentDefinitionId) {
        System.out.println("[IIA][AGENT_COMPILATION][REPOSITORY] find latest agentDefinitionId=" + agentDefinitionId);
        return entityManager.createQuery("""
                        select compilation
                        from AgentCompilation compilation
                        join fetch compilation.sglStatus
                        where compilation.codAgentdefinition.codAgentdefinition = :agentDefinitionId
                        order by compilation.dtRequestedat desc, compilation.codAgentcompilation desc
                        """, AgentCompilation.class)
                .setParameter("agentDefinitionId", agentDefinitionId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<AgentCompilation> findByCompilationId(String compilationId) {
        return entityManager.createQuery("""
                        select compilation
                        from AgentCompilation compilation
                        join fetch compilation.sglStatus
                        join fetch compilation.codAgentdefinition definition
                        where compilation.codAgentcompilation = :compilationId
                        """, AgentCompilation.class)
                .setParameter("compilationId", compilationId)
                .getResultStream()
                .findFirst();
    }

    public List<AgentCompilationStep> findStepsByCompilationId(String compilationId) {
        return entityManager.createQuery("""
                        select step
                        from AgentCompilationStep step
                        join fetch step.sglStatus
                        where step.id.codAgentcompilation = :compilationId
                        order by step.id.numSteporder
                        """, AgentCompilationStep.class)
                .setParameter("compilationId", compilationId)
                .getResultList();
    }

    public boolean existsRunningCompilation(String agentDefinitionId) {
        System.out.println("[IIA][AGENT_COMPILATION][REPOSITORY] exists running agentDefinitionId=" + agentDefinitionId);
        Long count = entityManager.createQuery("""
                        select count(compilation)
                        from AgentCompilation compilation
                        where compilation.codAgentdefinition.codAgentdefinition = :agentDefinitionId
                        and compilation.sglStatus.sglStatus in :runningStatuses
                        """, Long.class)
                .setParameter("agentDefinitionId", agentDefinitionId)
                .setParameter("runningStatuses", AgentCompilationStatuses.RUNNING)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Transactional
    public void markStarted(String compilationId, String status, String currentStep, OffsetDateTime startedAt) {
        AgentCompilation compilation = entityManager.find(AgentCompilation.class, compilationId);
        if (compilation == null) {
            return;
        }
        compilation.setSglStatus(statusReference(status));
        compilation.setDscCurrentstep(currentStep);
        compilation.setDtStartedat(startedAt);
        compilation.setDtUpdatedat(OffsetDateTime.now());
        entityManager.flush();
    }

    @Transactional
    public void updateStatus(String compilationId, String status, String currentStep) {
        AgentCompilation compilation = entityManager.find(AgentCompilation.class, compilationId);
        if (compilation == null) {
            return;
        }
        compilation.setSglStatus(statusReference(status));
        compilation.setDscCurrentstep(currentStep);
        compilation.setDtUpdatedat(OffsetDateTime.now());
        entityManager.flush();
    }

    @Transactional
    public void markCompleted(
            String compilationId,
            String finalStatus,
            String currentStep,
            Object resultJson,
            OffsetDateTime completedAt) {
        System.out.println("[IIA][AGENT_COMPILATION][REPOSITORY] mark completed compilationId="
                + compilationId + " status=" + finalStatus);
        AgentCompilation compilation = entityManager.find(AgentCompilation.class, compilationId);
        if (compilation == null) {
            return;
        }
        compilation.setSglStatus(statusReference(finalStatus));
        compilation.setDscCurrentstep(currentStep);
        compilation.setJsnResult(toNullableJsonMap(resultJson));
        compilation.setDscErrormessage(null);
        compilation.setDtCompletedat(completedAt);
        compilation.setDtUpdatedat(OffsetDateTime.now());
        entityManager.flush();
    }

    @Transactional
    public void markFailed(
            String compilationId,
            String finalStatus,
            String currentStep,
            String errorMessage,
            Object resultJson,
            OffsetDateTime completedAt) {
        System.out.println("[IIA][AGENT_COMPILATION][REPOSITORY] mark failed compilationId="
                + compilationId + " status=" + finalStatus);
        AgentCompilation compilation = entityManager.find(AgentCompilation.class, compilationId);
        if (compilation == null) {
            return;
        }
        compilation.setSglStatus(statusReference(finalStatus));
        compilation.setDscCurrentstep(currentStep);
        compilation.setDscErrormessage(errorMessage);
        compilation.setJsnResult(toNullableJsonMap(resultJson));
        compilation.setDtCompletedat(completedAt);
        compilation.setDtUpdatedat(OffsetDateTime.now());
        entityManager.flush();
    }

    AgentCompilationStatus statusReference(String status) {
        return entityManager.getReference(AgentCompilationStatus.class, status);
    }

    Map<String, Object> toJsonMapOrEmpty(Object value) {
        Map<String, Object> map = toNullableJsonMap(value);
        return map == null ? new LinkedHashMap<>() : map;
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> toNullableJsonMap(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, nestedValue) -> result.put(String.valueOf(key), nestedValue));
            return result;
        }
        return new LinkedHashMap<>(Map.of("value", value));
    }
}
