package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDisableRequest;

public class AgentLifecycleCommandFactory {

    public static final boolean DEFAULT_START_IMMEDIATELY_IF_ALLOWED = true;
    public static final boolean DEFAULT_STOP_RUNNING_AGENTS = true;
    public static final int DEFAULT_GRACE_PERIOD_SECONDS = 60;
    public static final int MIN_GRACE_PERIOD_SECONDS = 0;
    public static final int MAX_GRACE_PERIOD_SECONDS = 600;

    public AgentActivationCommand createActivationCommand(
            String agentDefinitionId,
            AgentActivationRequest request) {
        boolean startImmediatelyIfAllowed = request == null || request.getStartImmediatelyIfAllowed() == null
                ? DEFAULT_START_IMMEDIATELY_IF_ALLOWED
                : request.getStartImmediatelyIfAllowed();
        String note = request == null ? null : request.getNote();
        return new AgentActivationCommand(agentDefinitionId, note, startImmediatelyIfAllowed);
    }

    public AgentDisableCommand createDisableCommand(
            String agentDefinitionId,
            AgentDisableRequest request) {
        boolean stopRunningAgents = request == null || request.getStopRunningAgents() == null
                ? DEFAULT_STOP_RUNNING_AGENTS
                : request.getStopRunningAgents();
        int gracePeriodSeconds = request == null || request.getGracePeriodSeconds() == null
                ? DEFAULT_GRACE_PERIOD_SECONDS
                : request.getGracePeriodSeconds();
        String reason = request == null ? null : request.getReason();
        return new AgentDisableCommand(agentDefinitionId, reason, stopRunningAgents, gracePeriodSeconds);
    }
}
