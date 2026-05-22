package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        AlertService service = new AlertService();
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
        when(repository.updateAlertMetadataWithoutPromptChange("ALRT1", request)).thenReturn(java.util.Optional.of(updated));

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(repository).updateAlertMetadataWithoutPromptChange("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyFalseResetsToDraft() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
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
        when(repository.updateAlertDraftAfterPromptChange("ALRT1", request)).thenReturn(java.util.Optional.of(updated));

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(repository).updateAlertDraftAfterPromptChange("ALRT1", request);
        verify(repository, never()).updateAlertMetadataWithoutPromptChange("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyNullResetsToDraft() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
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
        when(repository.updateAlertDraftAfterPromptChange("ALRT1", request)).thenReturn(java.util.Optional.of(updated));

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(updated);
        verify(repository).updateAlertDraftAfterPromptChange("ALRT1", request);
    }

    @Test
    void updateWithChangedPromptAndVerifyImmediatelyTrueLeavesVerificationSeparated() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertDetail current = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.VERIFIED)
                .prompt("Create a suggestion when a journey is cancelled.");
        AlertUpdateRequest request = updateRequest("Create a suggestion when a journey is delayed.")
                .verifyImmediately(true);
        when(repository.getAlert("ALRT1")).thenReturn(java.util.Optional.of(current));

        java.util.Optional<AlertDetail> result = service.updateAlert("ALRT1", request);

        assertThat(result).contains(current);
        verify(repository, never()).updateAlertMetadataWithoutPromptChange("ALRT1", request);
        verify(repository, never()).updateAlertDraftAfterPromptChange("ALRT1", request);
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
}
