package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertAsyncVerificationServiceTest {

    @Test
    void asyncVerifyWithPropagatedTenantReinstallsTenantAndCleansItAfterTask() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicBoolean verifyCalledWithTenant = new AtomicBoolean(false);
        AlertAsyncVerificationService service = serviceCheckingTenant("tenant-a", tenantContext, verifyCalledWithTenant);

        service.verifyCreatedAlertAsync("ALRT1", true, "tenant-a");

        assertThat(verifyCalledWithTenant).isTrue();
        verify(tenantContext).setTenantId("tenant-a");
        verify(tenantContext).clear();
    }

    @Test
    void asyncVerifyWithoutPropagatedTenantUsesDefaultSchemaFallback() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicBoolean verifyCalledWithTenant = new AtomicBoolean(false);
        AlertAsyncVerificationService service = serviceCheckingTenant(
                "pis_intelligentinformationassistant", tenantContext, verifyCalledWithTenant);
        service.defaultSchema = "pis_intelligentinformationassistant";

        service.verifyCreatedAlertAsync("ALRT1", false, null);

        assertThat(verifyCalledWithTenant).isTrue();
        verify(tenantContext).setTenantId("pis_intelligentinformationassistant");
        verify(tenantContext).clear();
    }

    @Test
    void asyncVerifyWithoutTenantAndWithoutDefaultSchemaDoesNotCallVerification() {
        TenantContext tenantContext = mock(TenantContext.class);
        AlertRepository repository = mock(AlertRepository.class);
        AtomicBoolean verifyCalled = new AtomicBoolean(false);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService() {
            @Override
            protected AlertDetail verifyAndApplyEnableInNewTransaction(String alertId, boolean enableAfterVerification) {
                verifyCalled.set(true);
                return new AlertDetail().id(alertId).status(AlertStatus.VERIFIED).enabled(enableAfterVerification);
            }
        };
        service.tenantContext = tenantContext;
        service.alertRepository = repository;
        service.defaultSchema = "";

        service.verifyCreatedAlertAsync("ALRT1", true, null);

        assertThat(verifyCalled).isFalse();
        verify(tenantContext, never()).setTenantId(org.mockito.ArgumentMatchers.any());
        verify(repository, never()).markAlertVerificationTechnicalError(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void asyncVerifyWithValidRejectedOutcomeDoesNotApplyEnableAndDoesNotMarkTechnicalError() {
        TenantContext tenantContext = mock(TenantContext.class);
        AlertRepository repository = mock(AlertRepository.class);
        AlertService alertService = mock(AlertService.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService();
        service.tenantContext = tenantContext;
        service.alertRepository = repository;
        service.alertService = alertService;
        service.defaultSchema = "";
        configureTenantStorage(tenantContext);
        AlertDetail rejectedAlert = new AlertDetail()
                .id("ALRT1")
                .status(AlertStatus.REJECTED)
                .enabled(false)
                .verification(new AlertVerificationResult()
                        .status(AlertVerificationStatus.REJECTED)
                        .rejectedReason("Unsupported constraint."));
        when(alertService.verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"), org.mockito.ArgumentMatchers.any(AlertVerificationRequest.class)))
                .thenReturn(Optional.of(rejectedAlert));

        service.verifyCreatedAlertAsync("ALRT1", true, "tenant-a");

        verify(alertService).verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"), org.mockito.ArgumentMatchers.any(AlertVerificationRequest.class));
        verify(repository, never()).updateAlertEnabledAfterCreateVerification(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean());
        verify(repository, never()).markAlertVerificationTechnicalError(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void asyncVerifyWithTechnicalExceptionStillMarksTechnicalError() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicBoolean technicalErrorMarked = new AtomicBoolean(false);
        AlertService alertService = mock(AlertService.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService() {
            @Override
            public void markCreatedAlertVerificationError(String alertId, String shortMessage, String propagatedTenantId) {
                technicalErrorMarked.set(true);
            }
        };
        service.tenantContext = tenantContext;
        service.alertService = alertService;
        service.defaultSchema = "";
        configureTenantStorage(tenantContext);
        when(alertService.verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"), org.mockito.ArgumentMatchers.any(AlertVerificationRequest.class)))
                .thenThrow(new RuntimeException("provider timeout"));

        service.verifyCreatedAlertAsync("ALRT1", true, "tenant-a");

        assertThat(technicalErrorMarked).isTrue();
    }

    @Test
    void hibernateExceptionWithTenantPresentIsNotReportedAsTenantMissing() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicReference<String> capturedTechnicalMessage = new AtomicReference<>();
        AlertService alertService = mock(AlertService.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService() {
            @Override
            public void markCreatedAlertVerificationError(String alertId, String shortMessage, String propagatedTenantId) {
                capturedTechnicalMessage.set(shortMessage);
            }
        };
        service.tenantContext = tenantContext;
        service.alertService = alertService;
        service.defaultSchema = "";
        configureTenantStorage(tenantContext);
        when(alertService.verifyAlert(org.mockito.ArgumentMatchers.eq("ALRT1"), org.mockito.ArgumentMatchers.any(AlertVerificationRequest.class)))
                .thenThrow(new HibernateException("Cannot use the EntityManager/Session because neither a transaction nor a CDI request context is active"));

        service.verifyCreatedAlertAsync("ALRT1", true, "tenant-a");

        assertThat(capturedTechnicalMessage.get())
                .contains("Cannot use the EntityManager/Session")
                .doesNotContain("tenant context is missing");
    }

    private AlertAsyncVerificationService serviceCheckingTenant(
            String expectedTenant,
            TenantContext tenantContext,
            AtomicBoolean verifyCalledWithTenant) {
        AlertAsyncVerificationService service = new AlertAsyncVerificationService() {
            @Override
            protected AlertDetail verifyAndApplyEnableInNewTransaction(String alertId, boolean enableAfterVerification) {
                verifyCalledWithTenant.set(expectedTenant.equals(currentTenantId()));
                return new AlertDetail().id(alertId).status(AlertStatus.VERIFIED).enabled(enableAfterVerification);
            }
        };
        service.tenantContext = tenantContext;
        service.defaultSchema = "";
        configureTenantStorage(tenantContext);
        return service;
    }

    private void configureTenantStorage(TenantContext tenantContext) {
        AtomicReference<String> tenantStorage = new AtomicReference<>();
        when(tenantContext.getTenantId()).thenAnswer(invocation -> tenantStorage.get());
        doAnswer(invocation -> {
            tenantStorage.set(invocation.getArgument(0));
            return null;
        }).when(tenantContext).setTenantId(org.mockito.ArgumentMatchers.any());
        doAnswer(invocation -> {
            tenantStorage.set(null);
            return null;
        }).when(tenantContext).clear();
    }
}
