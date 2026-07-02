package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Objects;

@ApplicationScoped
public class UnavailableAgentOrchestratorGateway {

    private static final String ACTIVATE_METHOD = "PUT";
    private static final String DISABLE_METHOD = "POST";
    private static final String RUNTIME_AGENT_DEFINITIONS_PATH = "/v1/runtime-agent-definitions/";
    private static final boolean HTTP_CALL_EXECUTED = false;

    public AgentOrchestratorRuntimeAgentResult activate(AgentOrchestratorActivationRequest request) {
        Objects.requireNonNull(request, "request is required");
        String targetPath = RUNTIME_AGENT_DEFINITIONS_PATH + request.agentDefinitionId();
        AgentRuntimeSubmission submission = request.submission();
        logActivation(request, targetPath, submission);
        throw unavailable(AgentOrchestratorOperation.ACTIVATE, request.agentDefinitionId(), ACTIVATE_METHOD, targetPath);
    }

    public AgentOrchestratorRuntimeAgentResult disable(AgentOrchestratorDisableRequest request) {
        Objects.requireNonNull(request, "request is required");
        String targetPath = RUNTIME_AGENT_DEFINITIONS_PATH + request.agentDefinitionId() + "/disable";
        logDisable(request, targetPath);
        throw unavailable(AgentOrchestratorOperation.DISABLE, request.agentDefinitionId(), DISABLE_METHOD, targetPath);
    }

    private void logActivation(
            AgentOrchestratorActivationRequest request,
            String targetPath,
            AgentRuntimeSubmission submission) {
        System.out.println("================================================================================");
        System.out.println("[IIA][AGENT_ORCHESTRATOR][INTENTIONALLY_UNAVAILABLE]");
        System.out.println("operation=ACTIVATE");
        System.out.println("agentDefinitionId=" + request.agentDefinitionId());
        System.out.println("targetMethod=" + ACTIVATE_METHOD);
        System.out.println("targetPath=" + targetPath);
        System.out.println("submissionId=" + submission.submissionId());
        System.out.println("packageVersion=" + submission.packageVersion());
        System.out.println("desiredStatus=" + submission.desiredStatus());
        System.out.println("runtimePackagePrepared=true");
        System.out.println("httpCallExecuted=false");
        System.out.println("outcome=ORCHESTRATOR_UNAVAILABLE");
        System.out.println("================================================================================");
    }

    private void logDisable(AgentOrchestratorDisableRequest request, String targetPath) {
        System.out.println("================================================================================");
        System.out.println("[IIA][AGENT_ORCHESTRATOR][INTENTIONALLY_UNAVAILABLE]");
        System.out.println("operation=DISABLE");
        System.out.println("agentDefinitionId=" + request.agentDefinitionId());
        System.out.println("targetMethod=" + DISABLE_METHOD);
        System.out.println("targetPath=" + targetPath);
        System.out.println("stopRunningAgents=" + request.stopRunningAgents());
        System.out.println("gracePeriodSeconds=" + request.gracePeriodSeconds());
        System.out.println("requestedAt=" + request.requestedAt());
        System.out.println("httpCallExecuted=false");
        System.out.println("outcome=ORCHESTRATOR_UNAVAILABLE");
        System.out.println("================================================================================");
    }

    private AgentOrchestratorUnavailableException unavailable(
            AgentOrchestratorOperation operation,
            String agentDefinitionId,
            String targetMethod,
            String targetPath) {
        return new AgentOrchestratorUnavailableException(
                operation,
                agentDefinitionId,
                targetMethod,
                targetPath,
                HTTP_CALL_EXECUTED);
    }

}
