package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AlertServiceTest {

    @Test
    void createWithVerifyImmediatelyFalseKeepsDraftDisabled() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = new AlertService();
        service.alertRepository = repository;
        AlertCreateRequest request = createRequest(false, true);
        AlertDetail draft = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.DRAFT)
                .enabled(false);
        when(repository.createDraftAlert(request)).thenReturn(draft);

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.DRAFT);
        assertThat(result.getEnabled()).isFalse();
        verify(repository).createDraftAlert(request);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void createWithVerifyImmediatelyTrueValidPromptCanEnableAlert() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail draft = new AlertDetail().id("ALRT1").status(AlertStatus.DRAFT).enabled(false);
        AlertDetail verified = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFIED).enabled(false);
        AlertDetail enabled = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFIED).enabled(true);
        when(repository.createDraftAlert(request)).thenReturn(draft);
        doReturn(Optional.of(verified)).when(service).verifyAlert(eq("ALRT1"), any(AlertVerificationRequest.class));
        when(repository.updateAlertEnabledAfterCreateVerification("ALRT1", true)).thenReturn(Optional.of(enabled));

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.VERIFIED);
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    void createWithVerifyImmediatelyTrueInvalidPromptStaysDisabled() {
        AlertRepository repository = mock(AlertRepository.class);
        AlertService service = spy(new AlertService());
        service.alertRepository = repository;
        AlertCreateRequest request = createRequest(true, true);
        AlertDetail draft = new AlertDetail().id("ALRT1").status(AlertStatus.DRAFT).enabled(false);
        AlertDetail rejected = new AlertDetail().id("ALRT1").status(AlertStatus.REJECTED).enabled(false);
        when(repository.createDraftAlert(request)).thenReturn(draft);
        doReturn(Optional.of(rejected)).when(service).verifyAlert(eq("ALRT1"), any(AlertVerificationRequest.class));
        when(repository.updateAlertEnabledAfterCreateVerification("ALRT1", false)).thenReturn(Optional.of(rejected));

        AlertDetail result = service.createAlert(request);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.REJECTED);
        assertThat(result.getEnabled()).isFalse();
    }

    private AlertCreateRequest createRequest(boolean verifyImmediately, boolean enableAfterVerification) {
        return new AlertCreateRequest()
                .name("Alert")
                .prompt("Create a suggestion when a journey is cancelled at Milano Malpensa T1.")
                .verifyImmediately(verifyImmediately)
                .enableAfterVerification(enableAfterVerification);
    }
}
