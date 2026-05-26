package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AgentGenerationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AgentGenerationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRuntimeMetadata;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationMockEngine;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class AlertServiceTest {

    @Test
    void verifyParserFailureDoesNotUseMockWhenFallbackIsDisabled() {
        AlertService service = verificationService(false);
        AlertVerificationRequest request = new AlertVerificationRequest();
        when(service.llmGateway.get().generateText(any())).thenReturn(
                new LlmResponse("{\"decision\":\"VERIFIED\"", "OPENAI", "gpt-4.1-mini", null, null, null));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        assertThat(outcome.getValue().decision()).isEqualTo(AlertVerificationDecision.ERROR);
        assertThat(outcome.getValue().warnings().getFirst()).contains("looksTruncated=true");
        verify(service.alertVerificationMockEngine, never()).verify(any(), any());
    }

    @Test
    void verifyParserFailureUsesMockOnlyWhenFallbackIsEnabled() {
        AlertService service = verificationService(true);
        AlertVerificationRequest request = new AlertVerificationRequest();
        when(service.llmGateway.get().generateText(any())).thenReturn(
                new LlmResponse("{\"decision\":\"VERIFIED\"", "OPENAI", "gpt-4.1-mini", null, null, null));
        when(service.alertVerificationMockEngine.verify("ALRT1", "Prompt"))
                .thenReturn(rejectedVerificationOutcome("MOCK"));

        service.verifyAlert("ALRT1", request);

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(request), outcome.capture());
        verify(service.alertVerificationMockEngine).verify("ALRT1", "Prompt");
        assertThat(outcome.getValue().warnings()).contains(
                "LLM response was empty, invalid or rejected. Deterministic mock fallback was used because fallback-on-invalid-llm is enabled.");
    }

    @Test
    void createWithVerifyImmediatelyFalseKeepsDraftDisabled() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertCreateRequest request = createRequest(false, true);
        AlertDetail draft = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.DRAFT)
                .enabled(false);
        doReturn(draft).when(service).createDraftAlertInNewTransaction(request);

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.DRAFT);
        assertThat(result.getEnabled()).isFalse();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void createWithVerifyImmediatelyTrueReturnsVerifyingAndSchedulesAsyncVerification() {
        AlertService service = spy(new AlertService());
        ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
        service.managedExecutor = managedExecutor;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail verifying = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFYING).enabled(false);
        doReturn(verifying).when(service).createVerifyingAlertInNewTransaction(request);
        when(managedExecutor.runAsync(any(Runnable.class))).thenReturn(CompletableFuture.completedFuture(null));

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.VERIFYING);
        assertThat(result.getEnabled()).isFalse();
        verify(managedExecutor).runAsync(any(Runnable.class));
    }

    @Test
    void createWithVerifyImmediatelyTrueDoesNotBlockForRejectedResult() {
        AlertService service = spy(new AlertService());
        ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
        service.managedExecutor = managedExecutor;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail verifying = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFYING).enabled(false);
        doReturn(verifying).when(service).createVerifyingAlertInNewTransaction(request);
        when(managedExecutor.runAsync(any(Runnable.class))).thenReturn(CompletableFuture.completedFuture(null));

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.VERIFYING);
        assertThat(result.getEnabled()).isFalse();
        verify(managedExecutor).runAsync(any(Runnable.class));
    }

    @Test
    void createWithVerifyImmediatelyTrueDoesNotBlockForProviderError() {
        AlertService service = spy(new AlertService());
        ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
        service.managedExecutor = managedExecutor;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail verifying = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFYING).enabled(false);
        doReturn(verifying).when(service).createVerifyingAlertInNewTransaction(request);
        when(managedExecutor.runAsync(any(Runnable.class))).thenReturn(CompletableFuture.completedFuture(null));

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.VERIFYING);
        assertThat(result.getEnabled()).isFalse();
        verify(managedExecutor).runAsync(any(Runnable.class));
    }

    @Test
    void updateWithUnchangedPromptUpdatesOnlyMetadata() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("  Create a suggestion when a journey is cancelled.  ")
                .enabled(true)
                .version(3);
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is cancelled.");
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt(current.getPrompt())
                .enabled(true)
                .version(3);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertMetadataWithoutPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service).updateAlertMetadataWithoutPromptChangeInNewTransaction("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyFalseResetsToDraft() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.");
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(false)
                .enableAfterVerification(true);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.DRAFT)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertDraftAfterPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service).updateAlertDraftAfterPromptChangeInNewTransaction("ALRT1", request);
        verify(repository, never()).updateAlertMetadataWithoutPromptChange("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyNullResetsToDraft() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.");
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(null)
                .enableAfterVerification(true);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.DRAFT)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertDraftAfterPromptChangeInNewTransaction("ALRT1", request);

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service).updateAlertDraftAfterPromptChangeInNewTransaction("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyTrueSavesVerifyingAndSchedulesAsyncVerification() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
        service.alertRepository = repository;
        service.managedExecutor = managedExecutor;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.")
                .enabled(true);
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(true)
                .enableAfterVerification(true);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFYING)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertVerifyingAfterPromptChangeInNewTransaction("ALRT1", request);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return CompletableFuture.completedFuture(null);
        }).when(managedExecutor).runAsync(any(Runnable.class));

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service).updateAlertVerifyingAfterPromptChangeInNewTransaction("ALRT1", request);
        verify(managedExecutor).runAsync(any(Runnable.class));
        verify(service.alertAsyncVerificationService).verifyCreatedAlertAsync("ALRT1", true);
        verify(repository, never()).updateAlertMetadataWithoutPromptChange("ALRT1", request);
        verify(repository, never()).updateAlertDraftAfterPromptChange("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyTrueDoesNotEnablePreviouslyDisabledAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
        service.alertRepository = repository;
        service.managedExecutor = managedExecutor;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.")
                .enabled(false);
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(true)
                .enableAfterVerification(true);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFYING)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertVerifyingAfterPromptChangeInNewTransaction("ALRT1", request);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return CompletableFuture.completedFuture(null);
        }).when(managedExecutor).runAsync(any(Runnable.class));

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service.alertAsyncVerificationService).verifyCreatedAlertAsync("ALRT1", false);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyTrueDoesNotEnableWhenRequestDisablesAfterVerification() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
        service.alertRepository = repository;
        service.managedExecutor = managedExecutor;
        service.alertAsyncVerificationService = mock(AlertAsyncVerificationService.class);
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.")
                .enabled(true);
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(true)
                .enableAfterVerification(false);
        AlertDetail updated = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFYING)
                .prompt(request.getPrompt())
                .enabled(false)
                .version(2);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        doReturn(java.util.Optional.of(updated)).when(service).updateAlertVerifyingAfterPromptChangeInNewTransaction("ALRT1", request);
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return CompletableFuture.completedFuture(null);
        }).when(managedExecutor).runAsync(any(Runnable.class));

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(service.alertAsyncVerificationService).verifyCreatedAlertAsync("ALRT1", false);
    }

    @Test
    void enableVerifiedOperationalAlertUpdatesOnlyEnabledFlag() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(false);
        AlertDetail enabled = verifiedAlert(true);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        when(repository.hasOperationalVerificationMetadata("ALRT1")).thenReturn(true);
        when(repository.updateAlertEnabled("ALRT1", true)).thenReturn(java.util.Optional.of(enabled));

        java.util.Optional<AlertDetail> result = service.enableAlert("ALRT1");

        assertThat(result).contains(enabled);
        assertThat(result.orElseThrow().getStatus()).isEqualTo(AlertStatus.VERIFIED);
        assertThat(result.orElseThrow().getVerification().getStatus()).isEqualTo(AlertVerificationStatus.VERIFIED);
        verify(repository).updateAlertEnabled("ALRT1", true);
    }

    @Test
    void enableRequiresVerifiedLifecycleStatus() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(false).status(AlertStatus.DRAFT);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));

        assertThatThrownBy(() -> service.enableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.STATUS_NOT_VERIFIED);

        verify(repository, never()).updateAlertEnabled("ALRT1", true);
    }

    @Test
    void enableRequiresVerifiedVerificationStatus() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(false)
                .verification(new AlertVerificationResult().status(AlertVerificationStatus.PENDING));
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));

        assertThatThrownBy(() -> service.enableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.VERIFICATION_NOT_VERIFIED);
    }

    @Test
    void enableRejectsAlreadyEnabledAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(true)));

        assertThatThrownBy(() -> service.enableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.ALREADY_ENABLED);
    }

    @Test
    void enableRequiresOperationalInterpreterMetadata() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(false)));
        when(repository.hasOperationalVerificationMetadata("ALRT1")).thenReturn(false);

        assertThatThrownBy(() -> service.enableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.MISSING_OPERATIONAL_METADATA);
    }

    @Test
    void disableEnabledAlertUpdatesOnlyEnabledFlag() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(true);
        AlertDetail disabled = verifiedAlert(false);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));
        when(repository.updateAlertEnabled("ALRT1", false)).thenReturn(java.util.Optional.of(disabled));

        java.util.Optional<AlertDetail> result = service.disableAlert("ALRT1");

        assertThat(result).contains(disabled);
        assertThat(result.orElseThrow().getStatus()).isEqualTo(AlertStatus.VERIFIED);
        assertThat(result.orElseThrow().getVerification().getStatus()).isEqualTo(AlertVerificationStatus.VERIFIED);
        verify(repository).updateAlertEnabled("ALRT1", false);
    }

    @Test
    void disableRejectsVerifyingAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = verifiedAlert(true).status(AlertStatus.VERIFYING);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));

        assertThatThrownBy(() -> service.disableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.VERIFYING);
    }

    @Test
    void disableRejectsAlreadyDisabledAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(false)));

        assertThatThrownBy(() -> service.disableAlert("ALRT1"))
                .isInstanceOf(AlertRuntimeStateChangeRejectedException.class)
                .extracting(ex -> ((AlertRuntimeStateChangeRejectedException) ex).reason())
                .isEqualTo(AlertRuntimeStateChangeRejectedException.Reason.ALREADY_DISABLED);
    }

    @Test
    void deleteSoftDeletesAllowedLifecycleState() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(true)));
        when(repository.softDeleteAlert("ALRT1")).thenReturn(true);

        boolean result = service.deleteAlert("ALRT1");

        assertThat(result).isTrue();
        verify(repository).softDeleteAlert("ALRT1");
    }

    @Test
    void deleteReturnsFalseWhenAlertDoesNotExist() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.empty());

        boolean result = service.deleteAlert("ALRT1");

        assertThat(result).isFalse();
        verify(repository, never()).softDeleteAlert("ALRT1");
    }

    @Test
    void deleteRejectsAlreadyDeletedAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.existsDeletedAlert("ALRT1")).thenReturn(true);

        assertThatThrownBy(() -> service.deleteAlert("ALRT1"))
                .isInstanceOf(AlertDeleteRejectedException.class)
                .extracting(ex -> ((AlertDeleteRejectedException) ex).reason())
                .isEqualTo(AlertDeleteRejectedException.Reason.DELETED);

        verify(repository, never()).softDeleteAlert("ALRT1");
    }

    @Test
    void deleteRejectsVerifyingAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(verifiedAlert(false).status(AlertStatus.VERIFYING)));

        assertThatThrownBy(() -> service.deleteAlert("ALRT1"))
                .isInstanceOf(AlertDeleteRejectedException.class)
                .extracting(ex -> ((AlertDeleteRejectedException) ex).reason())
                .isEqualTo(AlertDeleteRejectedException.Reason.VERIFYING);

        verify(repository, never()).softDeleteAlert("ALRT1");
    }

    @Test
    void deleteRejectsDeployingAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail deploying = verifiedAlert(false)
                .runtime(new AlertRuntimeMetadata().deploymentStatus(AlertRuntimeMetadata.DeploymentStatusEnum.DEPLOYING));
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(deploying));

        assertThatThrownBy(() -> service.deleteAlert("ALRT1"))
                .isInstanceOf(AlertDeleteRejectedException.class)
                .extracting(ex -> ((AlertDeleteRejectedException) ex).reason())
                .isEqualTo(AlertDeleteRejectedException.Reason.DEPLOYING);

        verify(repository, never()).softDeleteAlert("ALRT1");
    }

    @Test
    void previewVerifiedAlertBuildsReadOnlyResponse() {
        AlertRepository repository = mock(AlertRepository.class);
        AgentGenerationPreviewMapper mapper = mock(AgentGenerationPreviewMapper.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        service.agentGenerationPreviewMapper = mapper;
        AlertAgentGenerationPreviewData data = previewData("VERIFIED", "VERIFIED", Map.of("source", "SERVICE_DATA"), Map.of("agentName", "Agent"));
        AgentGenerationPreviewResponse response = new AgentGenerationPreviewResponse();
        when(repository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(mapper.toResponse(data, null)).thenReturn(response);

        java.util.Optional<AgentGenerationPreviewResponse> result = service.previewAgentGenerationForAlert("ALRT1", null);

        assertThat(result).contains(response);
        verify(mapper).toResponse(data, null);
    }

    @Test
    void previewMissingAlertReturnsEmpty() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.empty());

        java.util.Optional<AgentGenerationPreviewResponse> result = service.previewAgentGenerationForAlert("ALRT1", null);

        assertThat(result).isEmpty();
    }

    @Test
    void previewDraftAlertIsRejected() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlertAgentGenerationPreviewData("ALRT1"))
                .thenReturn(java.util.Optional.of(previewData("DRAFT", "PENDING", Map.of(), Map.of())));

        assertThatThrownBy(() -> service.previewAgentGenerationForAlert("ALRT1", null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.NOT_VERIFIED);
    }

    @Test
    void previewVerifiedAlertWithoutArtifactsIsUnprocessable() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        when(repository.getAlertAgentGenerationPreviewData("ALRT1"))
                .thenReturn(java.util.Optional.of(previewData("VERIFIED", "VERIFIED", null, Map.of("agentName", "Agent"))));

        assertThatThrownBy(() -> service.previewAgentGenerationForAlert("ALRT1", null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.MISSING_TECHNICAL_ARTIFACTS);
    }

    @Test
    void previewWithLlmDisabledKeepsDeterministicPath() {
        AlertRepository repository = mock(AlertRepository.class);
        AgentGenerationPreviewMapper mapper = mock(AgentGenerationPreviewMapper.class);
        LlmGateway gateway = mock(LlmGateway.class);
        Instance<LlmGateway> gatewayInstance = mock(Instance.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        service.agentGenerationPreviewMapper = mapper;
        service.llmGateway = gatewayInstance;
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(repository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(mapper.toResponse(data, null)).thenReturn(new AgentGenerationPreviewResponse());

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(mapper).toResponse(data, null);
        verify(gateway, never()).generateText(any());
        verify(repository, never()).persistValidatedAgentBlueprintPreview(any(), any());
    }

    @Test
    void previewWithValidLlmCandidatePassesCandidateToValidatedMapperPath() {
        AlertService service = llmPreviewService();
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "{\"canGenerate\":true,\"blueprint\":{\"agentName\":\"GeneratedAgent\"},\"warnings\":[]}",
                "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(new AgentGenerationPreviewResponse());

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(service.agentGenerationPreviewMapper).toResponse(
                data,
                null,
                Map.of("agentName", "GeneratedAgent"),
                List.of("Agent Blueprint preview generated by LLM and validated by backend; no Agent Definition has been created."));
        verify(service.alertRepository, never()).persistValidatedAgentBlueprintPreview(any(), any());
    }

    @Test
    void previewWithInvalidLlmResponseFallsBackToDeterministicArtifacts() {
        AlertService service = llmPreviewService();
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "invalid-json", "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(new AgentGenerationPreviewResponse());

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(service.agentGenerationPreviewMapper).toResponse(
                data,
                null,
                null,
                List.of("LLM Agent Blueprint generation failed or was rejected by backend validation; deterministic verified Alert artifacts were used instead."));
        verify(service.alertRepository, never()).persistValidatedAgentBlueprintPreview(any(), any());
    }

    @Test
    void previewWithValidatedLlmPersistsFinalBlueprintWhenEnabled() {
        AlertService service = llmPreviewService();
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        AgentBlueprint finalBlueprint = new AgentBlueprint()
                .agentName("GeneratedAgent")
                .parameters(Map.of(
                        "generationContext", Map.of("previewSource", "LLM_VALIDATED"),
                        "runtimeContract", Map.of("source", "SERVICE_DATA"),
                        "generationReadiness", Map.of("readyForAgentDefinition", true)));
        AgentGenerationPreviewResponse response = new AgentGenerationPreviewResponse()
                .blueprint(finalBlueprint)
                .warnings(List.of(
                        AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING,
                        AgentGenerationPreviewMapper.DSL_DIAGNOSTIC_WARNING));
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "{\"canGenerate\":true,\"blueprint\":{\"agentName\":\"GeneratedAgent\"},\"warnings\":[]}",
                "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any())).thenReturn(response);
        when(service.alertRepository.persistValidatedAgentBlueprintPreview(any(), any())).thenReturn(true);

        AgentGenerationPreviewResponse result = service.previewAgentGenerationForAlert("ALRT1", null).orElseThrow();

        verify(service.alertRepository).persistValidatedAgentBlueprintPreview(
                org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.argThat(blueprint -> "GeneratedAgent".equals(blueprint.get("agentName"))
                        && ((Map<?, ?>) blueprint.get("parameters")).containsKey("generationContext")
                        && ((Map<?, ?>) blueprint.get("parameters")).containsKey("runtimeContract")
                        && ((Map<?, ?>) blueprint.get("parameters")).containsKey("generationReadiness")));
        assertThat(result.getWarnings()).containsExactly(
                AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING,
                AgentGenerationPreviewMapper.PERSISTED_LLM_PREVIEW_WARNING,
                AgentGenerationPreviewMapper.DSL_DIAGNOSTIC_WARNING);
    }

    @Test
    void previewDoesNotPersistAfterLlmFallbackEvenWhenEnabled() {
        AlertService service = llmPreviewService();
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "invalid-json", "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(new AgentGenerationPreviewResponse());

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(service.alertRepository, never()).persistValidatedAgentBlueprintPreview(any(), any());
    }

    @Test
    void previewDoesNotPersistWhenParsedLlmBlueprintIsRejectedByValidation() {
        AlertService service = llmPreviewService();
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "{\"canGenerate\":true,\"blueprint\":{\"requiredSources\":[\"EXTERNAL_HTTP\"]},\"warnings\":[]}",
                "FAKE", "fake-model", null, null, null));
        org.mockito.Mockito.doThrow(new AlertAgentGenerationPreviewRejectedException(
                        AlertAgentGenerationPreviewRejectedException.Reason.INVALID_BLUEPRINT))
                .when(service.agentGenerationPreviewMapper).toResponse(
                org.mockito.ArgumentMatchers.eq(data),
                org.mockito.ArgumentMatchers.isNull(),
                any(),
                any());
        org.mockito.Mockito.doReturn(new AgentGenerationPreviewResponse())
                .when(service.agentGenerationPreviewMapper).toResponse(data, null, null, List.of(
                        "LLM Agent Blueprint generation failed or was rejected by backend validation; deterministic verified Alert artifacts were used instead."));

        service.previewAgentGenerationForAlert("ALRT1", null);

        verify(service.alertRepository, never()).persistValidatedAgentBlueprintPreview(any(), any());
        verify(service.agentGenerationPreviewMapper).toResponse(data, null, null, List.of(
                "LLM Agent Blueprint generation failed or was rejected by backend validation; deterministic verified Alert artifacts were used instead."));
    }

    @Test
    void previewPersistenceRejectsAlertThatIsNoLongerVerifiedWithoutFallback() {
        AlertService service = llmPreviewService();
        service.persistValidatedLlmPreview = true;
        AlertAgentGenerationPreviewData data = validPreviewData();
        when(service.alertRepository.getAlertAgentGenerationPreviewData("ALRT1")).thenReturn(java.util.Optional.of(data));
        when(service.agentGenerationPromptBuilder.build(data, null)).thenReturn(llmRequest());
        when(service.llmGateway.get().generateText(any())).thenReturn(new LlmResponse(
                "{\"canGenerate\":true,\"blueprint\":{\"agentName\":\"GeneratedAgent\"},\"warnings\":[]}",
                "FAKE", "fake-model", null, null, null));
        when(service.agentGenerationPreviewMapper.toResponse(any(), any(), any(), any()))
                .thenReturn(new AgentGenerationPreviewResponse()
                        .blueprint(new AgentBlueprint().agentName("GeneratedAgent"))
                        .warnings(List.of(AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING)));
        when(service.alertRepository.persistValidatedAgentBlueprintPreview(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.previewAgentGenerationForAlert("ALRT1", null))
                .isInstanceOf(AlertAgentGenerationPreviewRejectedException.class)
                .extracting(ex -> ((AlertAgentGenerationPreviewRejectedException) ex).reason())
                .isEqualTo(AlertAgentGenerationPreviewRejectedException.Reason.NOT_VERIFIED);
    }

    @SuppressWarnings("unchecked")
    private AlertService llmPreviewService() {
        AlertService service = new AlertService();
        service.alertRepository = mock(AlertRepository.class);
        service.agentGenerationPreviewMapper = mock(AgentGenerationPreviewMapper.class);
        service.agentGenerationPromptBuilder = mock(AgentGenerationPromptBuilder.class);
        service.agentGenerationLlmResponseParser = new AgentGenerationLlmResponseParser();
        service.llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.get()).thenReturn(gateway);
        service.agentGenerationPreviewUseLlm = true;
        service.agentGenerationPreviewFallbackToDeterministicOnLlmError = true;
        return service;
    }

    @SuppressWarnings("unchecked")
    private AlertService verificationService(boolean fallbackEnabled) {
        AlertService service = new AlertService();
        service.alertRepository = mock(AlertRepository.class);
        when(service.alertRepository.getAlertVerificationPromptData("ALRT1"))
                .thenReturn(java.util.Optional.of(new AlertVerificationPromptData("ALRT1", "Alert", null, "Prompt")));
        when(service.alertRepository.verifyAlert(any(), any(), any())).thenReturn(java.util.Optional.empty());
        service.alertVerificationPromptBuilder = mock(AlertVerificationPromptBuilder.class);
        when(service.alertVerificationPromptBuilder.build(any())).thenReturn(
                new LlmRequest(AiUseCase.ALERT_VERIFY, "system", "user", "gpt-4.1-mini", 0.1, 5000, "ALRT1"));
        service.alertVerificationLlmResponseParser = new AlertVerificationLlmResponseParser();
        service.alertVerificationOutcomeValidator = mock(AlertVerificationOutcomeValidator.class);
        when(service.alertVerificationOutcomeValidator.validate(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service.alertVerificationMockEngine = mock(AlertVerificationMockEngine.class);
        service.llmGateway = mock(Instance.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(mock(LlmGateway.class));
        service.aiConfiguration = mock(AiConfiguration.class);
        when(service.aiConfiguration.provider()).thenReturn("openai");
        AiConfiguration.AlertVerify alertVerify = mock(AiConfiguration.AlertVerify.class);
        when(alertVerify.simulateProviderTimeout()).thenReturn(false);
        when(service.aiConfiguration.alertVerify()).thenReturn(alertVerify);
        service.fallbackOnInvalidLlm = fallbackEnabled;
        return service;
    }

    private AlertVerificationOutcome rejectedVerificationOutcome(String provider) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED, "Rejected.", "Rejected.", 0.0, provider, "mock-model",
                "alert-verify-mvp-v1", List.of(), null, null, null, null, null, List.of(), List.of(),
                null, null, null, List.of(), List.of());
    }

    private LlmRequest llmRequest() {
        return new LlmRequest(AiUseCase.AGENT_BLUEPRINT_GENERATE, "system", "user", "fake-model", 0.1, 2500, "ALRT1");
    }

    private AlertAgentGenerationPreviewData validPreviewData() {
        return previewData(
                "VERIFIED",
                "VERIFIED",
                Map.of("source", "SERVICE_DATA"),
                Map.of("agentName", "Agent"));
    }

    private AlertCreateRequest createRequest(boolean verifyImmediately, boolean enableAfterVerification) {
        return new AlertCreateRequest()
                .name("Alert")
                .prompt("Create a suggestion when a journey is cancelled at Milano Malpensa T1.")
                .verifyImmediately(verifyImmediately)
                .enableAfterVerification(enableAfterVerification);
    }

    private AlertUpdateRequest updateRequest(String prompt) {
        return new AlertUpdateRequest()
                .name("Updated alert")
                .description("Updated description")
                .prompt(prompt)
                .verifyImmediately(true)
                .enableAfterVerification(true);
    }

    private AlertDetail verifiedAlert(boolean enabled) {
        return new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .enabled(enabled)
                .verification(new AlertVerificationResult().status(AlertVerificationStatus.VERIFIED));
    }

    private AlertAgentGenerationPreviewData previewData(
            String status,
            String verificationStatus,
            Map<String, Object> technicalSpecification,
            Map<String, Object> blueprint) {
        return new AlertAgentGenerationPreviewData(
                "ALRT1",
                "Alert",
                status,
                verificationStatus,
                false,
                null,
                1,
                "Create a suggestion when a journey is cancelled.",
                "Verified.",
                null,
                null,
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification,
                blueprint,
                List.of("JOURNEY_CANCELLED"),
                List.of(),
                List.of());
    }
}
