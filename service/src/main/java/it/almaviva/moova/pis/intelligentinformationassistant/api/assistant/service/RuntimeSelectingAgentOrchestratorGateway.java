package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RuntimeSelectingAgentOrchestratorGateway implements AgentOrchestratorGateway {

    private final boolean enabled;
    private final FndAgentOrchestratorGateway fndGateway;
    private final UnavailableAgentOrchestratorGateway unavailableGateway;

    @Inject
    public RuntimeSelectingAgentOrchestratorGateway(
            @ConfigProperty(name = "iia.agent-orchestrator.client.enabled", defaultValue = "false") boolean enabled,
            FndAgentOrchestratorGateway fndGateway,
            UnavailableAgentOrchestratorGateway unavailableGateway) {
        this.enabled = enabled;
        this.fndGateway = fndGateway;
        this.unavailableGateway = unavailableGateway;
    }

    @Override
    public AgentOrchestratorRuntimeAgentResult activate(AgentOrchestratorActivationRequest request) {
        return enabled ? fndGateway.activate(request) : unavailableGateway.activate(request);
    }

    @Override
    public AgentOrchestratorRuntimeAgentResult disable(AgentOrchestratorDisableRequest request) {
        return unavailableGateway.disable(request);
    }
}
