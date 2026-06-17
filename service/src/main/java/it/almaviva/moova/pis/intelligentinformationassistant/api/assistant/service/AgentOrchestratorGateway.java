package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public interface AgentOrchestratorGateway {

    AgentOrchestratorRuntimeAgentResult activate(AgentOrchestratorActivationRequest request);

    AgentOrchestratorRuntimeAgentResult disable(AgentOrchestratorDisableRequest request);
}
