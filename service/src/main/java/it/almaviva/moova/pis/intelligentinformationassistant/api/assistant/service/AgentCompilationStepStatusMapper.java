package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStep;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationStatuses;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgentCompilationStepStatusMapper {

    public AgentCompilationStep.StatusEnum toApiStatus(String dbStatus) {
        if (dbStatus == null) {
            return null;
        }
        if (AgentCompilationStatuses.PENDING.equals(dbStatus)) {
            return AgentCompilationStep.StatusEnum.PENDING;
        }
        if (AgentCompilationStatuses.RUNNING.contains(dbStatus)) {
            return AgentCompilationStep.StatusEnum.RUNNING;
        }
        if (AgentCompilationStatuses.READY.equals(dbStatus)) {
            return AgentCompilationStep.StatusEnum.SUCCESS;
        }
        if (AgentCompilationStatuses.FAILED.equals(dbStatus) || AgentCompilationStatuses.REJECTED.equals(dbStatus)) {
            return AgentCompilationStep.StatusEnum.FAILED;
        }
        return AgentCompilationStep.StatusEnum.RUNNING;
    }
}
