package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Test;

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
}
