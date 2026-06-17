package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Objects;

public class AgentOrchestratorCommandRejectedException extends RuntimeException {

    private final AgentOrchestratorOperation operation;
    private final String agentDefinitionId;
    private final String rejectionCode;

    public AgentOrchestratorCommandRejectedException(
            AgentOrchestratorOperation operation,
            String agentDefinitionId,
            String rejectionCode,
            String message) {
        super(message);
        this.operation = Objects.requireNonNull(operation, "operation is required");
        this.agentDefinitionId = agentDefinitionId;
        this.rejectionCode = rejectionCode;
    }

    public AgentOrchestratorOperation operation() {
        return operation;
    }

    public String agentDefinitionId() {
        return agentDefinitionId;
    }

    public String rejectionCode() {
        return rejectionCode;
    }
}
