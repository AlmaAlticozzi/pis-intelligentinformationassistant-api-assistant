package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import org.junit.jupiter.api.Test;

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
        return service;
    }
}
