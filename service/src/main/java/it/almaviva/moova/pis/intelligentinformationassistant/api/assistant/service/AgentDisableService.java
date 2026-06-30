package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDisableRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class AgentDisableService {

    private static final String READY = "READY";
    private static final String ACTIVE = "ACTIVE";
    private static final String DISABLED = "DISABLED";

    private final AgentLifecycleCommandFactory commandFactory = new AgentLifecycleCommandFactory();
    private final AgentLifecycleTransitionValidator transitionValidator = new AgentLifecycleTransitionValidator();

    @Inject
    AgentDefinitionLifecycleStateLoader lifecycleStateLoader;

    @Inject
    AgentDefinitionLifecycleStateWriter lifecycleStateWriter;

    @Inject
    AgentDefinitionService agentDefinitionService;

    @Inject
    AgentOrchestratorGateway orchestratorGateway;

    @Inject
    AgentDisableFinalizationService disableFinalizationService;

    @Inject
    PersistedRuntimePackageReader persistedRuntimePackageReader;

    @Inject
    AgentOrchestratorDisableResultInterpreter disableResultInterpreter;

    Clock clock = Clock.systemUTC();

    public AgentDisableService() {
    }

    AgentDisableService(
            AgentDefinitionLifecycleStateLoader lifecycleStateLoader,
            AgentDefinitionLifecycleStateWriter lifecycleStateWriter,
            AgentDefinitionService agentDefinitionService,
            AgentOrchestratorGateway orchestratorGateway,
            Clock clock) {
        this.lifecycleStateLoader = lifecycleStateLoader;
        this.lifecycleStateWriter = lifecycleStateWriter;
        this.agentDefinitionService = agentDefinitionService;
        this.orchestratorGateway = orchestratorGateway;
        this.disableFinalizationService = null;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    AgentDisableService(AgentDefinitionLifecycleStateLoader loader, AgentDefinitionLifecycleStateWriter writer,
            AgentDefinitionService definitions, AgentOrchestratorGateway gateway,
            AgentDisableFinalizationService finalizer, PersistedRuntimePackageReader packageReader,
            AgentOrchestratorDisableResultInterpreter interpreter, Clock clock) {
        this(loader, writer, definitions, gateway, clock);
        this.disableFinalizationService = finalizer;
        this.persistedRuntimePackageReader = packageReader;
        this.disableResultInterpreter = interpreter;
    }

    public AgentDefinitionDetail disable(String agentDefinitionId, AgentDisableRequest request) {
        System.out.println("[IIA][AGENT_DISABLE] start agentDefinitionId=" + agentDefinitionId
                + " requestBodyPresent=" + (request != null));
        AgentDisableCommand command = createCommand(agentDefinitionId, request);
        System.out.println("[IIA][AGENT_DISABLE] command normalized agentDefinitionId=" + command.agentDefinitionId()
                + " stopRunningAgents=" + command.stopRunningAgents()
                + " gracePeriodSeconds=" + command.gracePeriodSeconds()
                + " reasonPresent=" + (command.reason() != null));

        AgentDefinitionLifecycleState state = loadState(command.agentDefinitionId());
        AgentLifecycleTransitionDecision decision =
                transitionValidator.validate(state.status(), AgentLifecycleAction.DISABLE);
        if (!decision.allowed()) {
            System.out.println("[IIA][AGENT_DISABLE] rejected agentDefinitionId=" + command.agentDefinitionId()
                    + " outcome=LIFECYCLE_CONFLICT currentStatus=" + decision.currentStatus()
                    + " stateChangeApplied=false");
            throw new AgentDisableRejectedException(decision.currentStatus(), decision.reason());
        }

        System.out.println("[IIA][AGENT_DISABLE] lifecycle validated agentDefinitionId=" + command.agentDefinitionId()
                + " currentStatus=" + decision.currentStatus()
                + " allowed=true executionMode=" + decision.executionMode()
                + " targetStatus=" + decision.targetStatus()
                + " idempotent=" + decision.idempotent());

        return switch (decision.executionMode()) {
            case LOCAL_STATE_CHANGE -> disableReady(command, state, decision);
            case NO_OPERATION -> disableIdempotent(command, state, decision);
            case ORCHESTRATOR_REQUIRED -> disableRuntime(command, state);
        };
    }

    private AgentDefinitionDetail disableReady(
            AgentDisableCommand command,
            AgentDefinitionLifecycleState state,
            AgentLifecycleTransitionDecision decision) {
        boolean updated = lifecycleStateWriter.transition(
                command.agentDefinitionId(),
                READY,
                decision.targetStatus(),
                Instant.now(clock));
        if (updated) {
            System.out.println("[IIA][AGENT_DISABLE] completed agentDefinitionId=" + command.agentDefinitionId()
                    + " previousStatus=" + state.status()
                    + " currentStatus=" + decision.targetStatus()
                    + " executionMode=LOCAL_STATE_CHANGE stateChangeApplied=true");
            return agentDefinitionService.getAgentDefinition(command.agentDefinitionId());
        }

        AgentDefinitionLifecycleState current = loadState(command.agentDefinitionId());
        AgentLifecycleTransitionDecision currentDecision =
                transitionValidator.validate(current.status(), AgentLifecycleAction.DISABLE);
        if (currentDecision.allowed() && currentDecision.idempotent()
                && AgentLifecycleExecutionMode.NO_OPERATION.equals(currentDecision.executionMode())) {
            System.out.println("[IIA][AGENT_DISABLE] completed agentDefinitionId=" + command.agentDefinitionId()
                    + " previousStatus=" + state.status()
                    + " currentStatus=" + current.status()
                    + " executionMode=NO_OPERATION idempotent=true stateChangeApplied=false");
            return agentDefinitionService.getAgentDefinition(command.agentDefinitionId());
        }

        System.out.println("[IIA][AGENT_DISABLE] rejected agentDefinitionId=" + command.agentDefinitionId()
                + " outcome=LIFECYCLE_CONFLICT currentStatus=" + current.status()
                + " stateChangeApplied=false");
        throw new AgentDisableRejectedException(
                currentDecision.currentStatus(),
                currentDecision.reason() == null ? "Agent Definition status changed during disable." : currentDecision.reason());
    }

    private AgentDefinitionDetail disableIdempotent(
            AgentDisableCommand command,
            AgentDefinitionLifecycleState state,
            AgentLifecycleTransitionDecision decision) {
        System.out.println("[IIA][AGENT_DISABLE] completed agentDefinitionId=" + command.agentDefinitionId()
                + " previousStatus=" + state.status()
                + " currentStatus=" + decision.targetStatus()
                + " executionMode=NO_OPERATION idempotent=true stateChangeApplied=false");
        return agentDefinitionService.getAgentDefinition(command.agentDefinitionId());
    }

    private AgentDefinitionDetail disableRuntime(
            AgentDisableCommand command,
            AgentDefinitionLifecycleState state) {
        AgentOrchestratorDisableRequest gatewayRequest = new AgentOrchestratorDisableRequest(
                command.agentDefinitionId(),
                command.reason(),
                command.stopRunningAgents(),
                command.gracePeriodSeconds(),
                Instant.now(clock));
        try {
            if (state.currentRuntimePackageId() == null) {
                throw new AgentDisableTechnicalException("ACTIVE Agent Definition has no current Runtime Agent Package.");
            }
            PersistedRuntimePackageSnapshot runtimePackage = persistedRuntimePackageReader.read(
                    state.currentRuntimePackageId(), command.agentDefinitionId());
            AgentOrchestratorRuntimeAgentResult result = orchestratorGateway.disable(gatewayRequest);
            disableResultInterpreter.validate(gatewayRequest, result, runtimePackage);
            OffsetDateTime disabledAt = OffsetDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
            disableFinalizationService.finalizeAcceptedDisable(command.agentDefinitionId(),
                    runtimePackage.runtimePackageId(), runtimePackage.packageVersion(), runtimePackage.submissionId(),
                    runtimePackage.packageFingerprint(), OffsetDateTime.ofInstant(state.updatedAt(), ZoneOffset.UTC), disabledAt);
            System.out.println("[IIA][AGENT_DISABLE] completed agentDefinitionId=" + command.agentDefinitionId()
                    + " previousStatus=" + state.status()
                    + " currentStatus=" + DISABLED
                    + " executionMode=ORCHESTRATOR_REQUIRED stateChangeApplied=true");
            return agentDefinitionService.getAgentDefinition(command.agentDefinitionId());
        } catch (AgentOrchestratorUnavailableException ex) {
            System.out.println("[IIA][AGENT_DISABLE] failed agentDefinitionId=" + command.agentDefinitionId()
                    + " previousStatus=" + state.status()
                    + " outcome=ORCHESTRATOR_UNAVAILABLE httpStatus=503 stateChangeApplied=false");
            throw ex;
        }
    }

    private AgentDefinitionLifecycleState loadState(String agentDefinitionId) {
        return lifecycleStateLoader.load(agentDefinitionId)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
    }

    private AgentDisableCommand createCommand(String agentDefinitionId, AgentDisableRequest request) {
        try {
            return commandFactory.createDisableCommand(agentDefinitionId, request);
        } catch (IllegalArgumentException ex) {
            throw new AgentDefinitionInvalidRequestException(source(ex.getMessage()), ex.getMessage());
        }
    }

    private String source(String message) {
        if (message != null && message.contains("gracePeriodSeconds")) {
            return "gracePeriodSeconds";
        }
        return "agentDefinitionId";
    }
}
