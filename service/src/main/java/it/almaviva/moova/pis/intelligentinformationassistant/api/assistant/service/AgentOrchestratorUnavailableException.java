package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentOrchestratorUnavailableException extends RuntimeException {

    private final AgentOrchestratorOperation operation;
    private final String agentDefinitionId;
    private final String targetMethod;
    private final String targetPath;
    private final boolean httpCallExecuted;

    public AgentOrchestratorUnavailableException(
            AgentOrchestratorOperation operation,
            String agentDefinitionId,
            String targetMethod,
            String targetPath,
            boolean httpCallExecuted) {
        this(operation, agentDefinitionId, targetMethod, targetPath, httpCallExecuted, null);
    }

    public AgentOrchestratorUnavailableException(
            AgentOrchestratorOperation operation,
            String agentDefinitionId,
            String targetMethod,
            String targetPath,
            boolean httpCallExecuted,
            Throwable cause) {
        super(message(operation, agentDefinitionId, targetMethod, targetPath, httpCallExecuted), cause);
        this.operation = operation;
        this.agentDefinitionId = agentDefinitionId;
        this.targetMethod = targetMethod;
        this.targetPath = targetPath;
        this.httpCallExecuted = httpCallExecuted;
    }

    public AgentOrchestratorOperation operation() {
        return operation;
    }

    public String agentDefinitionId() {
        return agentDefinitionId;
    }

    public String targetMethod() {
        return targetMethod;
    }

    public String targetPath() {
        return targetPath;
    }

    public boolean httpCallExecuted() {
        return httpCallExecuted;
    }

    private static String message(
            AgentOrchestratorOperation operation,
            String agentDefinitionId,
            String targetMethod,
            String targetPath,
            boolean httpCallExecuted) {
        return "Agent Orchestrator is intentionally unavailable for operation=" + operation
                + " agentDefinitionId=" + agentDefinitionId
                + " targetMethod=" + targetMethod
                + " targetPath=" + targetPath
                + " httpCallExecuted=" + httpCallExecuted;
    }
}
