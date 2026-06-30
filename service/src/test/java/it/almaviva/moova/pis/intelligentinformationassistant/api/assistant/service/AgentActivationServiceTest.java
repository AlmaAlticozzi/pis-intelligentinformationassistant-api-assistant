package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentRuntimePackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentActivationServiceTest {

    private static final String AGENT_ID = "AGDF1";
    private static final String PACKAGE_ID = "RTPK_TEST_CURRENT";
    private static final String SUBMISSION_ID = "ACTIVATE:AGDF1:1:0123456789abcdef";
    private static final String FINGERPRINT = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-17T10:00:00Z"), ZoneOffset.UTC);

    private AgentActivationSnapshotLoader snapshotLoader;
    private AgentActivationPreconditionValidator preconditionValidator;
    private RuntimePackageIdentityService runtimePackageIdentityService;
    private PersistedRuntimePackageReader persistedRuntimePackageReader;
    private AgentOrchestratorGateway orchestratorGateway;
    private AgentOrchestratorActivationResultInterpreter resultInterpreter;
    private AgentActivationFinalizationService finalizationService;
    private AgentDefinitionService agentDefinitionService;
    private AgentActivationService service;

    @BeforeEach
    void setUp() {
        snapshotLoader = mock(AgentActivationSnapshotLoader.class);
        preconditionValidator = mock(AgentActivationPreconditionValidator.class);
        runtimePackageIdentityService = mock(RuntimePackageIdentityService.class);
        persistedRuntimePackageReader = mock(PersistedRuntimePackageReader.class);
        orchestratorGateway = mock(AgentOrchestratorGateway.class);
        resultInterpreter = mock(AgentOrchestratorActivationResultInterpreter.class);
        finalizationService = mock(AgentActivationFinalizationService.class);
        agentDefinitionService = mock(AgentDefinitionService.class);

        service = new AgentActivationService();
        service.snapshotLoader = snapshotLoader;
        service.preconditionValidator = preconditionValidator;
        service.runtimePackageIdentityService = runtimePackageIdentityService;
        service.persistedRuntimePackageReader = persistedRuntimePackageReader;
        service.orchestratorGateway = orchestratorGateway;
        service.activationResultInterpreter = resultInterpreter;
        service.activationFinalizationService = finalizationService;
        service.agentDefinitionService = agentDefinitionService;
        service.clock = CLOCK;
        service.fallbackSubmittedBy = "pis-intelligentinformationassistant-api-assistant";
    }

    @Test
    void notFoundStopsBeforeMaterializationAndGateway() {
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activate(AGENT_ID, null))
                .isInstanceOf(AgentDefinitionNotFoundException.class);

        verify(preconditionValidator, never()).validate(any());
        verify(runtimePackageIdentityService, never()).materializeOrReuse(any(), any());
        verify(orchestratorGateway, never()).activate(any());
    }

    @Test
    void draftAndActiveAreRejectedBeforeMaterializationAndGateway() {
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshot("DRAFT")));
        assertThatThrownBy(() -> service.activate(AGENT_ID, null)).isInstanceOf(AgentActivationRejectedException.class);

        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshot("ACTIVE")));
        assertThatThrownBy(() -> service.activate(AGENT_ID, null)).isInstanceOf(AgentActivationRejectedException.class);

        verify(runtimePackageIdentityService, never()).materializeOrReuse(any(), any());
        verify(orchestratorGateway, never()).activate(any());
    }

    @Test
    void invalidPreconditionsStopBeforeMaterializationAndGateway() {
        AgentActivationPreconditionViolation violation = new AgentActivationPreconditionViolation(
                AgentActivationPreconditionCode.DSL_ARTIFACT_MISSING, "dslArtifact", "DSL artifact is missing.");
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshot("READY")));
        when(preconditionValidator.validate(any())).thenReturn(
                new AgentActivationPreconditionValidationResult(false, List.of(violation), List.of()));

        assertThatThrownBy(() -> service.activate(AGENT_ID, null))
                .isInstanceOf(AgentActivationPreconditionFailedException.class);
        verify(runtimePackageIdentityService, never()).materializeOrReuse(any(), any());
        verify(orchestratorGateway, never()).activate(any());
    }

    @Test
    void readyValidAgentMaterializesBeforeGatewayAndUnavailableDoesNotFinalize() {
        arrangeValidFlow("READY", true);
        when(orchestratorGateway.activate(any())).thenThrow(unavailable());

        assertThatThrownBy(() -> service.activate(AGENT_ID, new AgentActivationRequest().note(" go ")))
                .isInstanceOf(AgentOrchestratorUnavailableException.class);

        ArgumentCaptor<AgentActivationCommand> command = ArgumentCaptor.forClass(AgentActivationCommand.class);
        ArgumentCaptor<AgentOrchestratorActivationRequest> request =
                ArgumentCaptor.forClass(AgentOrchestratorActivationRequest.class);
        InOrder order = inOrder(runtimePackageIdentityService, orchestratorGateway);
        order.verify(runtimePackageIdentityService).materializeOrReuse(eq(AGENT_ID), command.capture());
        order.verify(orchestratorGateway).activate(request.capture());
        assertThat(command.getValue().note()).isEqualTo("go");
        assertThat(request.getValue().runtimePackageId()).isEqualTo(PACKAGE_ID);
        assertThat(request.getValue().canonicalPackageHash()).isEqualTo("sha256:" + FINGERPRINT);
        verify(finalizationService, never()).finalizeAcceptedActivation(any(), any(), anyLong(), any(), any(), any());
    }

    @Test
    void disabledValidAgentPreservesExplicitFalse() {
        arrangeValidFlow("DISABLED", false);
        when(orchestratorGateway.activate(any())).thenThrow(unavailable());

        assertThatThrownBy(() -> service.activate(AGENT_ID,
                new AgentActivationRequest().startImmediatelyIfAllowed(false)))
                .isInstanceOf(AgentOrchestratorUnavailableException.class);

        ArgumentCaptor<AgentActivationCommand> command = ArgumentCaptor.forClass(AgentActivationCommand.class);
        verify(runtimePackageIdentityService).materializeOrReuse(eq(AGENT_ID), command.capture());
        assertThat(command.getValue().startImmediatelyIfAllowed()).isFalse();
    }

    @Test
    void requestNullUsesActivationDefaultsAndReachesGateway() {
        arrangeValidFlow("READY", true);
        when(orchestratorGateway.activate(any())).thenThrow(unavailable());

        assertThatThrownBy(() -> service.activate(AGENT_ID, null))
                .isInstanceOf(AgentOrchestratorUnavailableException.class);

        ArgumentCaptor<AgentActivationCommand> command = ArgumentCaptor.forClass(AgentActivationCommand.class);
        verify(runtimePackageIdentityService).materializeOrReuse(eq(AGENT_ID), command.capture());
        verify(orchestratorGateway).activate(any());
        assertThat(command.getValue().note()).isNull();
        assertThat(command.getValue().startImmediatelyIfAllowed()).isTrue();
    }

    @Test
    void invalidMaterializationIsTechnicalAndDoesNotInvokeGatewayOrFinalizer() {
        arrangeSnapshot("READY");
        when(runtimePackageIdentityService.materializeOrReuse(any(), any()))
                .thenThrow(new AgentActivationTechnicalException("bad package version"));

        assertThatThrownBy(() -> service.activate(AGENT_ID, null))
                .isInstanceOf(AgentActivationTechnicalException.class);
        verify(orchestratorGateway, never()).activate(any());
        verify(finalizationService, never()).finalizeAcceptedActivation(any(), any(), anyLong(), any(), any(), any());
    }

    @Test
    void packageBuildPreconditionFailureDoesNotInvokeGatewayOrFinalizer() {
        arrangeSnapshot("READY");
        when(runtimePackageIdentityService.materializeOrReuse(any(), any()))
                .thenThrow(new AgentActivationPreconditionFailedException(List.of(
                        new AgentActivationPreconditionViolation(AgentActivationPreconditionCode.RUNTIME_CONTRACT_INVALID,
                                "runtimePackage", "runtime contract invalid"))));

        assertThatThrownBy(() -> service.activate(AGENT_ID, null))
                .isInstanceOf(AgentActivationPreconditionFailedException.class)
                .hasMessageContaining("RUNTIME_CONTRACT_INVALID");
        verify(orchestratorGateway, never()).activate(any());
        verify(finalizationService, never()).finalizeAcceptedActivation(any(), any(), anyLong(), any(), any(), any());
    }

    @Test
    void acceptedGatewayResultIsValidatedThenFinalizedAndReturned() {
        arrangeValidFlow("READY", true);
        AgentOrchestratorRuntimeAgentResult accepted = accepted("ACTIVE");
        AgentDefinitionDetail expected = new AgentDefinitionDetail().id(AGENT_ID);
        when(orchestratorGateway.activate(any())).thenReturn(accepted);
        when(resultInterpreter.validate(any(), eq(accepted))).thenReturn(
                new AgentOrchestratorActivationResultInterpreter.ValidatedActivationAcceptance("ACTIVE", 200));
        when(agentDefinitionService.getAgentDefinition(AGENT_ID)).thenReturn(expected);

        assertThat(service.activate(AGENT_ID, null)).isSameAs(expected);

        InOrder order = inOrder(runtimePackageIdentityService, orchestratorGateway, resultInterpreter, finalizationService);
        order.verify(runtimePackageIdentityService).materializeOrReuse(eq(AGENT_ID), any());
        order.verify(orchestratorGateway).activate(any());
        order.verify(resultInterpreter).validate(any(), eq(accepted));
        order.verify(finalizationService).finalizeAcceptedActivation(
                AGENT_ID, PACKAGE_ID, 1L, SUBMISSION_ID, FINGERPRINT,
                OffsetDateTime.ofInstant(CLOCK.instant(), ZoneOffset.UTC));
    }

    private void arrangeSnapshot(String status) {
        AgentActivationSnapshot snapshot = snapshot(status);
        when(snapshotLoader.load(AGENT_ID)).thenReturn(Optional.of(snapshot));
        when(preconditionValidator.validate(snapshot)).thenReturn(
                new AgentActivationPreconditionValidationResult(true, List.of(), List.of()));
    }

    private void arrangeValidFlow(String status, boolean startImmediately) {
        arrangeSnapshot(status);
        AgentRuntimePackage runtimePackage = mock(AgentRuntimePackage.class);
        when(runtimePackage.getCodRuntimepackage()).thenReturn(PACKAGE_ID);
        when(runtimePackageIdentityService.materializeOrReuse(eq(AGENT_ID), any())).thenReturn(runtimePackage);
        when(persistedRuntimePackageReader.read(PACKAGE_ID, AGENT_ID)).thenReturn(new PersistedRuntimePackageSnapshot(
                PACKAGE_ID, AGENT_ID, SUBMISSION_ID, 1L, FINGERPRINT, payload(startImmediately)));
    }

    private Map<String, Object> payload(boolean startImmediately) {
        return Map.of(
                "submissionId", SUBMISSION_ID, "desiredStatus", "ACTIVE", "packageVersion", 1L,
                "submittedAt", CLOCK.instant().toString(), "submittedBy", "tester",
                "startImmediatelyIfAllowed", startImmediately,
                "agentDefinition", Map.ofEntries(
                        Map.entry("id", AGENT_ID), Map.entry("name", "Agent"),
                        Map.entry("description", "Description"),
                        Map.entry("interpreterType", "EVENT_INTERPRETER"), Map.entry("triggerType", "EVENT"),
                        Map.entry("inputModel", "ServiceDataV2"),
                        Map.entry("outputModel", "AgentOutput.CANDIDATE_SUGGESTION"),
                        Map.entry("source", Map.of("controlPlaneComponent", "ASSISTANT",
                                "agentCompilationId", "AGCP1", "agentCompilationVersion", 1L)),
                        Map.entry("dataDomain", "SERVICE_DATA"),
                        Map.entry("runtimeContract", Map.of(
                                "runtimeExecutionModel", "STANDARD_DSL_EVALUATOR",
                                "allowedTools", List.of()))));
    }

    private AgentOrchestratorUnavailableException unavailable() {
        return new AgentOrchestratorUnavailableException(AgentOrchestratorOperation.ACTIVATE, AGENT_ID,
                "PUT", "/v1/runtime-agent-definitions/" + AGENT_ID, false);
    }

    private AgentOrchestratorRuntimeAgentResult accepted(String runtimeStatus) {
        return new AgentOrchestratorRuntimeAgentResult(AGENT_ID, "ACTIVE", runtimeStatus, 1L, SUBMISSION_ID,
                CLOCK.instant(), CLOCK.instant(), null, List.of());
    }

    private AgentActivationSnapshot snapshot(String status) {
        return new AgentActivationSnapshot(AGENT_ID, "Agent", "Description", status, "DSL", "MEDIUM",
                "EVENT_INTERPRETER", "EVENT", "ServiceDataV2", "AgentOutput.CANDIDATE_SUGGESTION", "tester",
                OffsetDateTime.parse("2026-06-17T09:00:00Z"), OffsetDateTime.parse("2026-06-17T09:30:00Z"),
                null, null, null, AgentActivationSnapshot.AgentActivationRequirementsSnapshot.empty(), null,
                new AgentActivationSnapshot.AgentActivationCompilationSummarySnapshot("AGCP1", "READY", "READY", null),
                null);
    }
}
