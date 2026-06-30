package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDisableRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentDisableServiceTest {

    private static final String AGENT_DEFINITION_ID = "AGDF1";
    private static final Instant UPDATED_AT = Instant.parse("2026-06-17T09:00:00Z");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-17T10:00:00Z"), ZoneOffset.UTC);
    private static final String PACKAGE_ID = "RTPK_TEST_CURRENT";
    private static final String SUBMISSION_ID = "ACTIVATE:AGDF1:1:0123456789abcdef";
    private static final String FINGERPRINT = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private final AgentDefinitionLifecycleStateLoader lifecycleStateLoader = mock(AgentDefinitionLifecycleStateLoader.class);
    private final AgentDefinitionLifecycleStateWriter lifecycleStateWriter = mock(AgentDefinitionLifecycleStateWriter.class);
    private final AgentDefinitionService agentDefinitionService = mock(AgentDefinitionService.class);
    private final AgentOrchestratorGateway orchestratorGateway = mock(AgentOrchestratorGateway.class);
    private final AgentDisableFinalizationService finalizationService = mock(AgentDisableFinalizationService.class);
    private final PersistedRuntimePackageReader persistedRuntimePackageReader = mock(PersistedRuntimePackageReader.class);
    private final AgentOrchestratorDisableResultInterpreter resultInterpreter = mock(AgentOrchestratorDisableResultInterpreter.class);
    private final AgentDisableService service = new AgentDisableService(
            lifecycleStateLoader,
            lifecycleStateWriter,
            agentDefinitionService,
            orchestratorGateway,
            finalizationService,
            persistedRuntimePackageReader,
            resultInterpreter,
            CLOCK);

    @Test
    void notFoundStopsBeforeWriterAndGateway() {
        when(lifecycleStateLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.disable(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentDefinitionNotFoundException.class);

        verify(lifecycleStateWriter, never()).transition(any(), any(), any(), any());
        verify(orchestratorGateway, never()).disable(any());
        verify(agentDefinitionService, never()).getAgentDefinition(any());
    }

    @Test
    void readyPerformsConditionalLocalTransitionAndReturnsUpdatedDetail() {
        when(lifecycleStateLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(state("READY")));
        when(lifecycleStateWriter.transition(AGENT_DEFINITION_ID, "READY", "DISABLED", CLOCK.instant())).thenReturn(true);
        when(agentDefinitionService.getAgentDefinition(AGENT_DEFINITION_ID)).thenReturn(detail(AgentDefinitionStatus.DISABLED));

        AgentDefinitionDetail detail = service.disable(AGENT_DEFINITION_ID, null);

        assertThat(detail.getStatus()).isEqualTo(AgentDefinitionStatus.DISABLED);
        verify(lifecycleStateWriter).transition(AGENT_DEFINITION_ID, "READY", "DISABLED", CLOCK.instant());
        verify(orchestratorGateway, never()).disable(any());
    }

    @Test
    void readyZeroRowsThenDisabledBecomesIdempotentWithoutSecondUpdate() {
        when(lifecycleStateLoader.load(AGENT_DEFINITION_ID))
                .thenReturn(Optional.of(state("READY")))
                .thenReturn(Optional.of(state("DISABLED")));
        when(lifecycleStateWriter.transition(AGENT_DEFINITION_ID, "READY", "DISABLED", CLOCK.instant())).thenReturn(false);
        when(agentDefinitionService.getAgentDefinition(AGENT_DEFINITION_ID)).thenReturn(detail(AgentDefinitionStatus.DISABLED));

        AgentDefinitionDetail detail = service.disable(AGENT_DEFINITION_ID, null);

        assertThat(detail.getStatus()).isEqualTo(AgentDefinitionStatus.DISABLED);
        verify(lifecycleStateWriter).transition(AGENT_DEFINITION_ID, "READY", "DISABLED", CLOCK.instant());
        verify(orchestratorGateway, never()).disable(any());
    }

    @Test
    void readyZeroRowsThenOtherStatusIsConflict() {
        when(lifecycleStateLoader.load(AGENT_DEFINITION_ID))
                .thenReturn(Optional.of(state("READY")))
                .thenReturn(Optional.of(state("ACTIVE")));
        when(lifecycleStateWriter.transition(AGENT_DEFINITION_ID, "READY", "DISABLED", CLOCK.instant())).thenReturn(false);

        assertThatThrownBy(() -> service.disable(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentDisableRejectedException.class);

        verify(orchestratorGateway, never()).disable(any());
    }

    @Test
    void disabledIsIdempotentAndDoesNotWriteOrCallGateway() {
        when(lifecycleStateLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(state("DISABLED")));
        when(agentDefinitionService.getAgentDefinition(AGENT_DEFINITION_ID)).thenReturn(detail(AgentDefinitionStatus.DISABLED));

        AgentDefinitionDetail detail = service.disable(AGENT_DEFINITION_ID, null);

        assertThat(detail.getStatus()).isEqualTo(AgentDefinitionStatus.DISABLED);
        verify(lifecycleStateWriter, never()).transition(any(), any(), any(), any());
        verify(orchestratorGateway, never()).disable(any());
    }

    @Test
    void activeInvokesGatewayOutsideLocalWriterAndPropagatesUnavailable() {
        arrangeActive();
        when(orchestratorGateway.disable(any())).thenThrow(new AgentOrchestratorUnavailableException(
                AgentOrchestratorOperation.DISABLE,
                AGENT_DEFINITION_ID,
                "POST",
                "/v1/runtime-agent-definitions/" + AGENT_DEFINITION_ID + "/disable",
                false));

        assertThatThrownBy(() -> service.disable(AGENT_DEFINITION_ID, new AgentDisableRequest().reason(" do not log ")))
                .isInstanceOf(AgentOrchestratorUnavailableException.class);

        ArgumentCaptor<AgentOrchestratorDisableRequest> requestCaptor =
                ArgumentCaptor.forClass(AgentOrchestratorDisableRequest.class);
        verify(orchestratorGateway).disable(requestCaptor.capture());
        assertThat(requestCaptor.getValue().requestedAt()).isEqualTo(CLOCK.instant());
        assertThat(requestCaptor.getValue().stopRunningAgents()).isTrue();
        assertThat(requestCaptor.getValue().gracePeriodSeconds()).isEqualTo(60);
        assertThat(requestCaptor.getValue().reason()).isEqualTo("do not log");
        verify(lifecycleStateWriter, never()).transition(any(), any(), any(), any());
        verify(finalizationService, never()).finalizeAcceptedDisable(any(), any(), anyLong(), any(), any(), any(), any());
    }

    @Test
    void activeExplicitStopFalseAndGraceZeroReachGateway() {
        arrangeActive();
        when(orchestratorGateway.disable(any())).thenThrow(new AgentOrchestratorUnavailableException(
                AgentOrchestratorOperation.DISABLE, AGENT_DEFINITION_ID, "POST", "path", false));

        assertThatThrownBy(() -> service.disable(AGENT_DEFINITION_ID,
                new AgentDisableRequest().stopRunningAgents(false).gracePeriodSeconds(0)))
                .isInstanceOf(AgentOrchestratorUnavailableException.class);

        ArgumentCaptor<AgentOrchestratorDisableRequest> requestCaptor =
                ArgumentCaptor.forClass(AgentOrchestratorDisableRequest.class);
        verify(orchestratorGateway).disable(requestCaptor.capture());
        assertThat(requestCaptor.getValue().stopRunningAgents()).isFalse();
        assertThat(requestCaptor.getValue().gracePeriodSeconds()).isZero();
    }

    @Test
    void activeAcceptedGatewaySuccessIsFinalizedWithCurrentPackageIdentity() {
        arrangeActive();
        AgentOrchestratorRuntimeAgentResult accepted = new AgentOrchestratorRuntimeAgentResult(
                AGENT_DEFINITION_ID, "DISABLED", "DISABLED", 1, SUBMISSION_ID,
                CLOCK.instant(), CLOCK.instant(), null, List.of());
        doReturn(accepted).when(orchestratorGateway).disable(any());
        when(resultInterpreter.validate(any(), eq(accepted), any())).thenReturn(
                new AgentOrchestratorDisableResultInterpreter.DisableAcceptance(1L, SUBMISSION_ID, null));
        AgentDefinitionDetail expected = detail(AgentDefinitionStatus.DISABLED);
        when(agentDefinitionService.getAgentDefinition(AGENT_DEFINITION_ID)).thenReturn(expected);

        assertThat(service.disable(AGENT_DEFINITION_ID, null)).isSameAs(expected);

        verify(lifecycleStateWriter, never()).transition(any(), any(), any(), any());
        var order = inOrder(orchestratorGateway, resultInterpreter, finalizationService);
        order.verify(orchestratorGateway).disable(any());
        order.verify(resultInterpreter).validate(any(), eq(accepted), any());
        order.verify(finalizationService).finalizeAcceptedDisable(
                AGENT_DEFINITION_ID, PACKAGE_ID, 1L, SUBMISSION_ID, FINGERPRINT,
                OffsetDateTime.ofInstant(UPDATED_AT, ZoneOffset.UTC),
                OffsetDateTime.ofInstant(CLOCK.instant(), ZoneOffset.UTC));
    }

    @Test
    void activeRuntimeRejectionPropagatesWithoutWriter() {
        arrangeActive();
        when(orchestratorGateway.disable(any())).thenThrow(new AgentOrchestratorCommandRejectedException(
                AgentOrchestratorOperation.DISABLE,
                AGENT_DEFINITION_ID,
                "STOP_POLICY_REJECTED",
                "Runtime rejected stop policy."));

        assertThatThrownBy(() -> service.disable(AGENT_DEFINITION_ID, null))
                .isInstanceOf(AgentOrchestratorCommandRejectedException.class);

        verify(lifecycleStateWriter, never()).transition(any(), any(), any(), any());
        verify(finalizationService, never()).finalizeAcceptedDisable(any(), any(), anyLong(), any(), any(), any(), any());
    }

    @Test
    void invalidLifecycleStatesAreRejectedBeforeWriterAndGateway() {
        for (String status : List.of("DRAFT", "COMPILING", "REJECTED", "ARCHIVED")) {
            when(lifecycleStateLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(state(status)));

            assertThatThrownBy(() -> service.disable(AGENT_DEFINITION_ID, null))
                    .isInstanceOf(AgentDisableRejectedException.class);
        }

        verify(lifecycleStateWriter, never()).transition(any(), any(), any(), any());
        verify(orchestratorGateway, never()).disable(any());
    }

    @Test
    void invalidRequestIsBadRequestBeforeGateway() {
        assertThatThrownBy(() -> service.disable(AGENT_DEFINITION_ID, new AgentDisableRequest().gracePeriodSeconds(-1)))
                .isInstanceOfSatisfying(AgentDefinitionInvalidRequestException.class,
                        ex -> assertThat(ex.source()).isEqualTo("gracePeriodSeconds"));
        assertThatThrownBy(() -> service.disable(AGENT_DEFINITION_ID, new AgentDisableRequest().gracePeriodSeconds(601)))
                .isInstanceOf(AgentDefinitionInvalidRequestException.class);

        verify(orchestratorGateway, never()).disable(any());
    }

    @Test
    void logsDoNotContainReason() {
        arrangeActive();
        when(orchestratorGateway.disable(any())).thenThrow(new AgentOrchestratorUnavailableException(
                AgentOrchestratorOperation.DISABLE, AGENT_DEFINITION_ID, "POST", "path", false));

        Captured captured = captureSystemOut(() -> service.disable(AGENT_DEFINITION_ID,
                new AgentDisableRequest().reason("sensitive operator reason")));

        assertThat(captured.thrown()).isInstanceOf(AgentOrchestratorUnavailableException.class);
        assertThat(captured.output()).contains("reasonPresent=true");
        assertThat(captured.output()).doesNotContain("sensitive operator reason");
    }

    private AgentDefinitionLifecycleState state(String status) {
        return new AgentDefinitionLifecycleState(AGENT_DEFINITION_ID, status, UPDATED_AT);
    }

    private void arrangeActive() {
        when(lifecycleStateLoader.load(AGENT_DEFINITION_ID)).thenReturn(Optional.of(
                new AgentDefinitionLifecycleState(AGENT_DEFINITION_ID, "ACTIVE", UPDATED_AT, PACKAGE_ID)));
        when(persistedRuntimePackageReader.read(PACKAGE_ID, AGENT_DEFINITION_ID)).thenReturn(
                new PersistedRuntimePackageSnapshot(PACKAGE_ID, AGENT_DEFINITION_ID, SUBMISSION_ID, 1L,
                        FINGERPRINT, Map.of("agentDefinition", Map.of("artifact", Map.of("hash", "hash")))));
    }

    private AgentDefinitionDetail detail(AgentDefinitionStatus status) {
        return new AgentDefinitionDetail()
                .id(AGENT_DEFINITION_ID)
                .status(status);
    }

    private Captured captureSystemOut(Runnable runnable) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            Throwable thrown = catchThrowable(runnable::run);
            return new Captured(output.toString(StandardCharsets.UTF_8), thrown);
        } finally {
            System.setOut(original);
        }
    }

    private record Captured(String output, Throwable thrown) {
    }
}
