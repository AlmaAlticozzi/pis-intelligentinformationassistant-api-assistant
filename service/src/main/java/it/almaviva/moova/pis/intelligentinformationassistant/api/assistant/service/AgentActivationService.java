package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class AgentActivationService {

    private final AgentLifecycleCommandFactory commandFactory = new AgentLifecycleCommandFactory();
    private final AgentLifecycleTransitionValidator transitionValidator = new AgentLifecycleTransitionValidator();

    @Inject
    AgentActivationSnapshotLoader snapshotLoader;

    @Inject
    AgentActivationPreconditionValidator preconditionValidator;

    @Inject
    AgentRuntimePackageBuilder runtimePackageBuilder;

    @Inject
    AgentOrchestratorGateway orchestratorGateway;

    @Inject
    AgentRuntimePackageVersionProvider packageVersionProvider;

    @ConfigProperty(
            name = "iia.agent-activation.fallback-submitted-by",
            defaultValue = "pis-intelligentinformationassistant-api-assistant")
    String fallbackSubmittedBy;

    Clock clock = Clock.systemUTC();

    public AgentActivationService() {
    }

    AgentActivationService(
            AgentActivationSnapshotLoader snapshotLoader,
            AgentActivationPreconditionValidator preconditionValidator,
            AgentRuntimePackageBuilder runtimePackageBuilder,
            AgentOrchestratorGateway orchestratorGateway,
            AgentRuntimePackageVersionProvider packageVersionProvider,
            Clock clock,
            String fallbackSubmittedBy) {
        this.snapshotLoader = snapshotLoader;
        this.preconditionValidator = preconditionValidator;
        this.runtimePackageBuilder = runtimePackageBuilder;
        this.orchestratorGateway = orchestratorGateway;
        this.packageVersionProvider = packageVersionProvider;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.fallbackSubmittedBy = fallbackSubmittedBy;
    }

    public AgentDefinitionDetail activate(String agentDefinitionId, AgentActivationRequest request) {
        System.out.println("[IIA][AGENT_ACTIVATION] start agentDefinitionId=" + agentDefinitionId
                + " requestBodyPresent=" + (request != null));
        AgentActivationCommand command = createCommand(agentDefinitionId, request);
        System.out.println("[IIA][AGENT_ACTIVATION] command normalized agentDefinitionId=" + command.agentDefinitionId()
                + " startImmediatelyIfAllowed=" + command.startImmediatelyIfAllowed()
                + " notePresent=" + (command.note() != null));

        AgentActivationSnapshot snapshot = snapshotLoader.load(command.agentDefinitionId())
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));

        AgentLifecycleTransitionDecision transition =
                transitionValidator.validate(snapshot.status(), AgentLifecycleAction.ACTIVATE);
        if (!transition.allowed()) {
            System.out.println("[IIA][AGENT_ACTIVATION] rejected agentDefinitionId=" + command.agentDefinitionId()
                    + " outcome=LIFECYCLE_CONFLICT currentStatus=" + transition.currentStatus()
                    + " stateChangeApplied=false");
            throw new AgentActivationRejectedException(transition.currentStatus(), transition.reason());
        }
        System.out.println("[IIA][AGENT_ACTIVATION] lifecycle validated agentDefinitionId=" + command.agentDefinitionId()
                + " currentStatus=" + transition.currentStatus()
                + " allowed=true executionMode=" + transition.executionMode()
                + " targetStatus=" + transition.targetStatus());

        AgentActivationPreconditionValidationResult preconditions = preconditionValidator.validate(snapshot);
        System.out.println("[IIA][AGENT_ACTIVATION] preconditions validated agentDefinitionId="
                + command.agentDefinitionId()
                + " valid=" + preconditions.valid()
                + " errors=" + preconditions.errors().size()
                + " warnings=" + preconditions.warnings().size());
        if (!preconditions.valid()) {
            List<String> codes = preconditions.errors().stream()
                    .map(violation -> violation.code().name())
                    .toList();
            System.out.println("[IIA][AGENT_ACTIVATION] rejected agentDefinitionId=" + command.agentDefinitionId()
                    + " outcome=PRECONDITION_FAILED violationCodes=" + codes
                    + " stateChangeApplied=false");
            throw new AgentActivationPreconditionFailedException(preconditions.errors());
        }

        AgentRuntimePackageBuildResult packageBuildResult = buildRuntimePackage(snapshot, command);
        AgentOrchestratorActivationRequest gatewayRequest = new AgentOrchestratorActivationRequest(
                command.agentDefinitionId(),
                packageBuildResult.submission(),
                packageBuildResult.canonicalPackageHash());
        try {
            AgentOrchestratorRuntimeAgentResult result = orchestratorGateway.activate(gatewayRequest);
            System.out.println("[IIA][AGENT_ACTIVATION] failed agentDefinitionId=" + command.agentDefinitionId()
                    + " outcome=RUNTIME_ACCEPTANCE_NOT_SUPPORTED httpStatus=500 stateChangeApplied=false");
            throw new AgentActivationRuntimeAcceptanceNotSupportedException(
                    "Runtime activation was accepted, but control-plane activation finalization is not enabled in this phase.");
        } catch (AgentOrchestratorUnavailableException ex) {
            System.out.println("[IIA][AGENT_ACTIVATION] failed agentDefinitionId=" + command.agentDefinitionId()
                    + " outcome=ORCHESTRATOR_UNAVAILABLE httpStatus=503 stateChangeApplied=false");
            throw ex;
        }
    }

    private AgentActivationCommand createCommand(String agentDefinitionId, AgentActivationRequest request) {
        try {
            return commandFactory.createActivationCommand(agentDefinitionId, request);
        } catch (IllegalArgumentException ex) {
            throw new AgentDefinitionInvalidRequestException("agentDefinitionId", ex.getMessage());
        }
    }

    private AgentRuntimePackageBuildResult buildRuntimePackage(
            AgentActivationSnapshot snapshot,
            AgentActivationCommand command) {
        try {
            long packageVersion = packageVersionProvider.resolvePackageVersion(snapshot);
            AgentRuntimePackageBuildContext context = new AgentRuntimePackageBuildContext(
                    packageVersion,
                    Instant.now(clock),
                    submittedBy());
            AgentRuntimePackageBuildResult result = runtimePackageBuilder.build(snapshot, command, context);
            System.out.println("[IIA][AGENT_ACTIVATION] runtime package prepared agentDefinitionId="
                    + command.agentDefinitionId()
                    + " packageVersion=" + packageVersion
                    + " submissionId=" + result.submission().submissionId()
                    + " packageHashPrefix=" + packageHashPrefix(result.canonicalPackageHash()));
            return result;
        } catch (AgentRuntimePackageBuildException ex) {
            throw new AgentActivationPreconditionFailedException(List.of(
                    new AgentActivationPreconditionViolation(
                            AgentActivationPreconditionCode.RUNTIME_CONTRACT_INVALID,
                            "runtimePackage",
                            sanitize(ex.getMessage()))));
        } catch (AgentActivationPreconditionFailedException | AgentActivationTechnicalException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AgentActivationTechnicalException("Runtime package construction failed.", ex);
        }
    }

    private String submittedBy() {
        if (fallbackSubmittedBy == null || fallbackSubmittedBy.isBlank()) {
            throw new AgentActivationTechnicalException("Activation submittedBy fallback is not configured.");
        }
        return fallbackSubmittedBy.trim();
    }

    private String packageHashPrefix(String canonicalPackageHash) {
        if (canonicalPackageHash == null) {
            return null;
        }
        String hash = canonicalPackageHash.startsWith("sha256:")
                ? canonicalPackageHash.substring("sha256:".length())
                : canonicalPackageHash;
        return hash.substring(0, Math.min(16, hash.length()));
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return "Runtime package data is invalid.";
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
