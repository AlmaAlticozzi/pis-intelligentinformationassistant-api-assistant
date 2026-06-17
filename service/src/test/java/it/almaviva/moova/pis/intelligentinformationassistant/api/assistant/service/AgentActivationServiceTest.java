package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentActivationServiceTest {

    private static final String AGENT_DEFINITION_ID = "AGDF1";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-17T10:00:00Z"), ZoneOffset.UTC);

    private final AgentActivationSnapshotLoader snapshotLoader = mock(AgentActivationSnapshotLoader.class);
    private final AgentActivationPreconditionValidator preconditionValidator = mock(AgentActivationPreconditionValidator.class);
    private final AgentRuntimePackageBuilder runtimePackageBuilder = mock(AgentRuntimePackageBuilder.class);
    private final AgentOrchestratorGateway orchestratorGateway = mock(AgentOrchestratorGateway.class);
    private final AgentRuntimePackageVersionProvider packageVersionProvider = mock(AgentRuntimePackageVersionProvider.class);
    private final AgentActivationService service = new AgentActivationService(
            snapshotLoader,
            preconditionValidator,
            runtimePackageBuilder,
            orchestratorGateway,
            packageVersionProvider,
            CLOCK,
            "pis-intelligentinformationassistant-api-assistant");

    @Test
    void notFoundStopsBeforeLifecyclePackageAndGateway() {
        when(snapshotLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentDefinitionNotFoundException.class);

        verify(preconditionValidator, never()).validate(any());
        verify(runtimePackageBuilder, never()).build(any(), any(), any());
        verify(orchestratorGateway, never()).activate(any());
    }

    @Test
    void draftAndActiveAreRejectedAsLifecycleConflictBeforePackageAndGateway() {
        when(snapshotLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(snapshot("DRAFT")));

        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentActivationRejectedException.class)
                .hasMessageContaining("compiled");

        verify(runtimePackageBuilder, never()).build(any(), any(), any());
        verify(orchestratorGateway, never()).activate(any());

        when(snapshotLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(snapshot("ACTIVE")));
        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentActivationRejectedException.class)
                .hasMessageContaining("already ACTIVE");
        verify(orchestratorGateway, never()).activate(any());
    }

    @Test
    void invalidPreconditionsReturnViolationsAndStopBeforePackageAndGateway() {
        AgentActivationPreconditionViolation violation = new AgentActivationPreconditionViolation(
                AgentActivationPreconditionCode.DSL_ARTIFACT_MISSING,
                "dslArtifact",
                "DSL artifact is missing.");
        when(snapshotLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(snapshot("READY")));
        when(preconditionValidator.validate(any())).thenReturn(
                new AgentActivationPreconditionValidationResult(false, List.of(violation), List.of()));

        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, null))
                .isInstanceOfSatisfying(AgentActivationPreconditionFailedException.class, ex ->
                        assertThat(ex.violations()).extracting(AgentActivationPreconditionViolation::code)
                                .containsExactly(AgentActivationPreconditionCode.DSL_ARTIFACT_MISSING));

        verify(runtimePackageBuilder, never()).build(any(), any(), any());
        verify(orchestratorGateway, never()).activate(any());
    }

    @Test
    void readyValidAgentBuildsPackageInvokesGatewayAndPropagatesUnavailableWithoutStateChange() {
        arrangeValidFlow("READY", true);

        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, new AgentActivationRequest().note(" go ")))
                .isInstanceOf(AgentOrchestratorUnavailableException.class);

        ArgumentCaptor<AgentActivationCommand> commandCaptor = ArgumentCaptor.forClass(AgentActivationCommand.class);
        ArgumentCaptor<AgentRuntimePackageBuildContext> contextCaptor =
                ArgumentCaptor.forClass(AgentRuntimePackageBuildContext.class);
        ArgumentCaptor<AgentOrchestratorActivationRequest> gatewayCaptor =
                ArgumentCaptor.forClass(AgentOrchestratorActivationRequest.class);
        verify(runtimePackageBuilder).build(any(), commandCaptor.capture(), contextCaptor.capture());
        verify(orchestratorGateway).activate(gatewayCaptor.capture());

        assertThat(commandCaptor.getValue().note()).isEqualTo("go");
        assertThat(commandCaptor.getValue().startImmediatelyIfAllowed()).isTrue();
        assertThat(contextCaptor.getValue().packageVersion()).isEqualTo(1);
        assertThat(contextCaptor.getValue().submittedAt()).isEqualTo(CLOCK.instant());
        assertThat(gatewayCaptor.getValue().canonicalPackageHash()).startsWith("sha256:");
    }

    @Test
    void disabledValidAgentAndExplicitFalseReachRuntimeSubmission() {
        arrangeValidFlow("DISABLED", false);

        assertThatThrownBy(() -> service.activate(
                AGENT_DEFINITION_ID,
                new AgentActivationRequest().startImmediatelyIfAllowed(false)))
                .isInstanceOf(AgentOrchestratorUnavailableException.class);

        ArgumentCaptor<AgentActivationCommand> commandCaptor = ArgumentCaptor.forClass(AgentActivationCommand.class);
        verify(runtimePackageBuilder).build(any(), commandCaptor.capture(), any());
        assertThat(commandCaptor.getValue().startImmediatelyIfAllowed()).isFalse();
    }

    @Test
    void requestNullUsesActivationDefaults() {
        arrangeValidFlow("READY", true);

        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentOrchestratorUnavailableException.class);

        ArgumentCaptor<AgentActivationCommand> commandCaptor = ArgumentCaptor.forClass(AgentActivationCommand.class);
        verify(runtimePackageBuilder).build(any(), commandCaptor.capture(), any());
        assertThat(commandCaptor.getValue().note()).isNull();
        assertThat(commandCaptor.getValue().startImmediatelyIfAllowed()).isTrue();
    }

    @Test
    void invalidPackageVersionIsTechnicalAndDoesNotInvokeGateway() {
        when(snapshotLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(snapshot("READY")));
        when(preconditionValidator.validate(any())).thenReturn(
                new AgentActivationPreconditionValidationResult(true, List.of(), List.of()));
        when(packageVersionProvider.resolvePackageVersion(any()))
                .thenThrow(new AgentActivationTechnicalException("bad package version"));

        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentActivationTechnicalException.class);

        verify(orchestratorGateway, never()).activate(any());
    }

    @Test
    void unexpectedGatewaySuccessIsRejectedAsTechnicalFailure() {
        arrangeValidFlow("READY", true);
        doReturn(new AgentOrchestratorRuntimeAgentResult(
                AGENT_DEFINITION_ID, "ACTIVE", "ACCEPTED", 1, "submission", CLOCK.instant(), CLOCK.instant(), null, List.of()))
                .when(orchestratorGateway).activate(any());

        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentActivationRuntimeAcceptanceNotSupportedException.class);
    }

    @Test
    void packageBuildFailureIsMappedToPreconditionFailureAndGatewayIsNotInvoked() {
        when(snapshotLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(snapshot("READY")));
        when(preconditionValidator.validate(any())).thenReturn(
                new AgentActivationPreconditionValidationResult(true, List.of(), List.of()));
        when(packageVersionProvider.resolvePackageVersion(any())).thenReturn(1L);
        when(runtimePackageBuilder.build(any(), any(), any()))
                .thenThrow(new AgentRuntimePackageBuildException("runtime contract invalid"));

        assertThatThrownBy(() -> service.activate(AGENT_DEFINITION_ID, null))
                .isInstanceOfSatisfying(AgentActivationPreconditionFailedException.class, ex -> {
                    assertThat(ex.getMessage()).contains("RUNTIME_CONTRACT_INVALID");
                    assertThat(ex.getMessage()).doesNotContain("artifactHash", "runtimeContract", "dslSecretMarker");
                });

        verify(orchestratorGateway, never()).activate(any());
    }

    private void arrangeValidFlow(String status, boolean startImmediatelyIfAllowed) {
        AgentActivationSnapshot snapshot = snapshot(status);
        AgentRuntimeSubmission submission = submission(startImmediatelyIfAllowed);
        when(snapshotLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(snapshot));
        when(preconditionValidator.validate(snapshot)).thenReturn(
                new AgentActivationPreconditionValidationResult(true, List.of(), List.of()));
        when(packageVersionProvider.resolvePackageVersion(snapshot)).thenReturn(1L);
        when(runtimePackageBuilder.build(any(), any(), any())).thenReturn(new AgentRuntimePackageBuildResult(
                submission,
                "{}",
                "sha256:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                2));
        when(orchestratorGateway.activate(any())).thenThrow(new AgentOrchestratorUnavailableException(
                AgentOrchestratorOperation.ACTIVATE,
                AGENT_DEFINITION_ID,
                "PUT",
                "/v1/runtime-agent-definitions/" + AGENT_DEFINITION_ID,
                false));
    }

    private AgentActivationSnapshot snapshot(String status) {
        return new AgentActivationSnapshot(
                AGENT_DEFINITION_ID,
                "Agent",
                "Description",
                status,
                "DSL",
                "MEDIUM",
                "EVENT_INTERPRETER",
                "EVENT",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "tester",
                OffsetDateTime.parse("2026-06-17T09:00:00Z"),
                OffsetDateTime.parse("2026-06-17T09:30:00Z"),
                null,
                null,
                null,
                AgentActivationSnapshot.AgentActivationRequirementsSnapshot.empty(),
                null,
                new AgentActivationSnapshot.AgentActivationCompilationSummarySnapshot("AGCP1", "READY", "READY", null),
                null);
    }

    private AgentRuntimeSubmission submission(boolean startImmediatelyIfAllowed) {
        return new AgentRuntimeSubmission(
                "ACTIVATE:" + AGENT_DEFINITION_ID + ":1:0123456789abcdef",
                "ACTIVE",
                1,
                CLOCK.instant(),
                "pis-intelligentinformationassistant-api-assistant",
                startImmediatelyIfAllowed,
                null,
                new AgentRuntimeSubmission.AgentRuntimeDefinitionPackage(
                        AGENT_DEFINITION_ID,
                        "Agent",
                        "Description",
                        null,
                        null,
                        null,
                        "EVENT_INTERPRETER",
                        "EVENT",
                        "ServiceDataV2",
                        "AgentOutput.CANDIDATE_SUGGESTION",
                        null,
                        null,
                        Map.of("safe", true),
                        null,
                        "SERVICE_DATA",
                        List.of()));
    }
}
