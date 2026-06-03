package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertAsyncVerificationServiceTest {

    @Test
    void runnerMethodHasActivateRequestContext() throws NoSuchMethodException {
        Method method = AlertAsyncVerificationRequestContextRunner.class.getMethod(
                "verifyCreatedAlertInRequestContext",
                String.class,
                boolean.class,
                String.class);

        assertThat(method.isAnnotationPresent(ActivateRequestContext.class)).isTrue();
    }

    @Test
    void persistenceGatewayDbBoundaryMethodsHaveRequestContextAndTransaction() throws NoSuchMethodException {
        assertDbBoundary("loadAlertForVerification", String.class, String.class);
        assertDbBoundary("persistVerificationOutcome",
                String.class,
                AlertVerificationRequest.class,
                AlertVerificationOutcome.class,
                String.class);
        assertDbBoundary("updateAlertEnabledAfterCreateVerification", String.class, String.class);
        assertDbBoundary("persistTechnicalError", String.class, String.class, String.class);
    }

    @Test
    void serviceDelegatesAsyncVerificationToRequestContextRunner() {
        AlertAsyncVerificationRequestContextRunner runner = mock(AlertAsyncVerificationRequestContextRunner.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService();
        service.requestContextRunner = runner;
        when(runner.verifyCreatedAlertInRequestContext("ALRT1", false, "tenant-a"))
                .thenReturn(new AlertDetail().id("ALRT1").status(AlertStatus.VERIFIED).enabled(false));

        service.verifyCreatedAlertAsync("ALRT1", false, "tenant-a");

        verify(runner).verifyCreatedAlertInRequestContext("ALRT1", false, "tenant-a");
        verify(runner, never()).markCreatedAlertVerificationErrorInRequestContext(anyString(), anyString(), anyString());
    }

    @Test
    void tenantRestoreHappensInsideRunnerBeforeAlertVerification() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicBoolean verifyCalledWithTenant = new AtomicBoolean(false);
        AlertAsyncVerificationRequestContextRunner runner = runnerCheckingTenant(
                "tenant-a",
                tenantContext,
                verifyCalledWithTenant,
                AlertStatus.VERIFIED);

        runner.verifyCreatedAlertInRequestContext("ALRT1", false, "tenant-a");

        assertThat(verifyCalledWithTenant).isTrue();
        verify(tenantContext).setTenantId("tenant-a");
        verify(tenantContext).clear();
    }

    @Test
    void runnerUsesDefaultSchemaFallbackWhenCapturedTenantIsMissing() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicBoolean verifyCalledWithTenant = new AtomicBoolean(false);
        AlertAsyncVerificationRequestContextRunner runner = runnerCheckingTenant(
                "pis_intelligentinformationassistant",
                tenantContext,
                verifyCalledWithTenant,
                AlertStatus.VERIFIED);
        runner.defaultSchema = "pis_intelligentinformationassistant";

        runner.verifyCreatedAlertInRequestContext("ALRT1", false, null);

        assertThat(verifyCalledWithTenant).isTrue();
        verify(tenantContext).setTenantId("pis_intelligentinformationassistant");
        verify(tenantContext).clear();
    }

    @Test
    void runnerDoesNotCallVerificationWithoutTenantAndWithoutDefaultSchema() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicBoolean verifyCalled = new AtomicBoolean(false);
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected AlertDetail verifyAndApplyEnable(String alertId, boolean enableAfterVerification, String tenant) {
                verifyCalled.set(true);
                return new AlertDetail().id(alertId).status(AlertStatus.VERIFIED).enabled(enableAfterVerification);
            }

            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.tenantContext = tenantContext;
        runner.defaultSchema = "";
        configureTenantStorage(tenantContext);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        runner.verifyCreatedAlertInRequestContext("ALRT1", true, null))
                .isInstanceOf(AlertAsyncVerificationService.MissingTenantContextException.class);

        assertThat(verifyCalled).isFalse();
        verify(tenantContext, never()).setTenantId(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectedOutcomeIsFunctionalAndDoesNotApplyEnableInRunner() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicReference<Boolean> enableFlagSeen = new AtomicReference<>();
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected AlertDetail verifyAndApplyEnable(String alertId, boolean enableAfterVerification, String tenant) {
                enableFlagSeen.set(enableAfterVerification);
                return new AlertDetail().id(alertId).status(AlertStatus.REJECTED).enabled(false);
            }

            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.tenantContext = tenantContext;
        runner.defaultSchema = "";
        configureTenantStorage(tenantContext);

        AlertDetail result = runner.verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a");

        assertThat(result.getStatus()).isEqualTo(AlertStatus.REJECTED);
        assertThat(result.getEnabled()).isFalse();
        assertThat(enableFlagSeen.get()).isTrue();
    }

    @Test
    void verifiedEnableFalseCompletesNormally() {
        TenantContext tenantContext = mock(TenantContext.class);
        AlertAsyncVerificationRequestContextRunner runner = runnerCheckingTenant(
                "tenant-a",
                tenantContext,
                new AtomicBoolean(false),
                AlertStatus.VERIFIED);

        AlertDetail result = runner.verifyCreatedAlertInRequestContext("ALRT1", false, "tenant-a");

        assertThat(result.getStatus()).isEqualTo(AlertStatus.VERIFIED);
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    void verifiedEnableTrueIsPassedToRunnerOperation() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicReference<Boolean> enableFlagSeen = new AtomicReference<>();
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected AlertDetail verifyAndApplyEnable(String alertId, boolean enableAfterVerification, String tenant) {
                enableFlagSeen.set(enableAfterVerification);
                return new AlertDetail().id(alertId).status(AlertStatus.VERIFIED).enabled(enableAfterVerification);
            }

            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.tenantContext = tenantContext;
        runner.defaultSchema = "";
        configureTenantStorage(tenantContext);

        AlertDetail result = runner.verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a");

        assertThat(enableFlagSeen.get()).isTrue();
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    void runnerLoadsAndPersistsThroughPersistenceGateway() {
        AlertVerificationPersistenceGateway gateway = mock(AlertVerificationPersistenceGateway.class);
        AlertService alertService = mock(AlertService.class);
        AlertVerificationPromptData promptData = new AlertVerificationPromptData(
                "ALRT1",
                "name",
                "description",
                "prompt",
                null);
        AlertVerificationOutcome outcome = mock(AlertVerificationOutcome.class);
        AlertDetail persisted = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFIED).enabled(false);
        when(gateway.loadAlertForVerification("ALRT1", "tenant-a")).thenReturn(Optional.of(promptData));
        when(alertService.verifyAlertOutcome("ALRT1", promptData)).thenReturn(outcome);
        when(gateway.persistVerificationOutcome(
                org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(outcome),
                org.mockito.ArgumentMatchers.eq("tenant-a")))
                .thenReturn(Optional.of(persisted));
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.persistenceGateway = gateway;
        runner.alertService = alertService;

        AlertDetail result = runner.verifyAndApplyEnable("ALRT1", false, "tenant-a");

        assertThat(result).isSameAs(persisted);
        verify(gateway).loadAlertForVerification("ALRT1", "tenant-a");
        verify(alertService).verifyAlertOutcome("ALRT1", promptData);
        verify(gateway).persistVerificationOutcome(
                org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(outcome),
                org.mockito.ArgumentMatchers.eq("tenant-a"));
        verify(gateway, never()).updateAlertEnabledAfterCreateVerification(anyString(), anyString());
    }

    @Test
    void runnerMarksTechnicalErrorThroughPersistenceGateway() {
        TenantContext tenantContext = mock(TenantContext.class);
        AlertVerificationPersistenceGateway gateway = mock(AlertVerificationPersistenceGateway.class);
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.tenantContext = tenantContext;
        runner.persistenceGateway = gateway;
        configureTenantStorage(tenantContext);

        runner.markCreatedAlertVerificationErrorInRequestContext("ALRT1", "provider timeout", "tenant-a");

        verify(gateway).persistTechnicalError("ALRT1", "provider timeout", "tenant-a");
        verify(tenantContext).clear();
    }

    @Test
    void technicalExceptionStillMarksTechnicalErrorThroughRunner() {
        AlertAsyncVerificationRequestContextRunner runner = mock(AlertAsyncVerificationRequestContextRunner.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService();
        service.requestContextRunner = runner;
        doThrow(new RuntimeException("provider timeout"))
                .when(runner).verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a");

        service.verifyCreatedAlertAsync("ALRT1", true, "tenant-a");

        verify(runner).markCreatedAlertVerificationErrorInRequestContext("ALRT1", "provider timeout", "tenant-a");
    }

    @Test
    void hibernateExceptionWithTenantPresentIsNotReportedAsTenantMissing() {
        AlertAsyncVerificationRequestContextRunner runner = mock(AlertAsyncVerificationRequestContextRunner.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService();
        service.requestContextRunner = runner;
        doThrow(new HibernateException("Cannot use the EntityManager/Session because neither a transaction nor a CDI request context is active"))
                .when(runner).verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a");

        service.verifyCreatedAlertAsync("ALRT1", true, "tenant-a");

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(runner).markCreatedAlertVerificationErrorInRequestContext(
                org.mockito.ArgumentMatchers.eq("ALRT1"),
                message.capture(),
                org.mockito.ArgumentMatchers.eq("tenant-a"));
        assertThat(message.getValue())
                .contains("Cannot use the EntityManager/Session")
                .doesNotContain("tenant context is missing");
    }

    @Test
    void hibernateMissingTenantIdentifierExplainsRequestContextResolverProblem() {
        AlertAsyncVerificationRequestContextRunner runner = mock(AlertAsyncVerificationRequestContextRunner.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService();
        service.requestContextRunner = runner;
        doThrow(new HibernateException("SessionFactory configured for multi-tenancy, but no tenant identifier specified"))
                .when(runner).verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a");

        service.verifyCreatedAlertAsync("ALRT1", true, "tenant-a");

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(runner).markCreatedAlertVerificationErrorInRequestContext(
                org.mockito.ArgumentMatchers.eq("ALRT1"),
                message.capture(),
                org.mockito.ArgumentMatchers.eq("tenant-a"));
        assertThat(message.getValue())
                .contains("Hibernate tenant resolver returned no tenant because CDI request context is inactive")
                .doesNotContain("tenant context is missing during asynchronous verification");
    }

    private AlertAsyncVerificationRequestContextRunner runnerCheckingTenant(
            String expectedTenant,
            TenantContext tenantContext,
            AtomicBoolean verifyCalledWithTenant,
            AlertStatus status) {
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected AlertDetail verifyAndApplyEnable(String alertId, boolean enableAfterVerification, String tenant) {
                verifyCalledWithTenant.set(expectedTenant.equals(currentTenantId()) && expectedTenant.equals(tenant));
                return new AlertDetail()
                        .id(alertId)
                        .status(status)
                        .enabled(AlertStatus.VERIFIED.equals(status) && enableAfterVerification);
            }

            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.tenantContext = tenantContext;
        runner.defaultSchema = "";
        configureTenantStorage(tenantContext);
        return runner;
    }

    private void configureTenantStorage(TenantContext tenantContext) {
        AtomicReference<String> tenantStorage = new AtomicReference<>();
        when(tenantContext.getTenantId()).thenAnswer(invocation -> tenantStorage.get());
        org.mockito.Mockito.doAnswer(invocation -> {
            tenantStorage.set(invocation.getArgument(0));
            return null;
        }).when(tenantContext).setTenantId(org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.doAnswer(invocation -> {
            tenantStorage.set(null);
            return null;
        }).when(tenantContext).clear();
    }

    private void assertDbBoundary(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = AlertVerificationPersistenceGateway.class.getMethod(methodName, parameterTypes);
        assertThat(method.isAnnotationPresent(ActivateRequestContext.class))
                .as(methodName + " activates CDI request context")
                .isTrue();
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional)
                .as(methodName + " is transactional")
                .isNotNull();
        assertThat(transactional.value())
                .as(methodName + " uses an isolated transaction boundary")
                .isEqualTo(Transactional.TxType.REQUIRES_NEW);
    }
}

