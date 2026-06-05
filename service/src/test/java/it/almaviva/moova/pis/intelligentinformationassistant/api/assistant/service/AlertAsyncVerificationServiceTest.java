package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import io.smallrye.context.api.ManagedExecutorConfig;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertAsyncVerificationServiceTest {

    @Test
    void dbContextRunnerOwnsShortRequestContextsAndAsyncRunnerDoesNot() throws NoSuchMethodException {
        Method method = AlertAsyncVerificationRequestContextRunner.class.getMethod(
                "verifyCreatedAlertInRequestContext",
                String.class,
                boolean.class,
                String.class);

        assertThat(method.isAnnotationPresent(ActivateRequestContext.class)).isFalse();
        assertThat(AlertAsyncVerificationRequestContextRunner.class.getMethod(
                "markCreatedAlertVerificationErrorInRequestContext",
                String.class,
                String.class,
                String.class).isAnnotationPresent(ActivateRequestContext.class)).isFalse();
        assertThat(AlertAsyncVerificationDbContextRunner.class.getMethod(
                "loadAlertForVerification",
                String.class,
                String.class).isAnnotationPresent(ActivateRequestContext.class)).isTrue();
        assertThat(AlertAsyncVerificationDbContextRunner.class.getMethod(
                "persistVerificationOutcome",
                String.class,
                AlertVerificationOutcome.class,
                boolean.class,
                String.class).isAnnotationPresent(ActivateRequestContext.class)).isTrue();
        assertThat(AlertAsyncVerificationDbContextRunner.class.getMethod(
                "persistTechnicalError",
                String.class,
                String.class,
                String.class).isAnnotationPresent(ActivateRequestContext.class)).isTrue();
    }

    @Test
    void alertAsyncVerificationManagedExecutorClearsCdiAndTransactionContext() throws NoSuchFieldException {
        Field field = AlertAsyncVerificationService.class.getDeclaredField("managedExecutor");
        ManagedExecutorConfig config = field.getAnnotation(ManagedExecutorConfig.class);

        assertThat(config).isNotNull();
        assertThat(Arrays.asList(config.cleared()))
                .contains(ThreadContext.CDI, ThreadContext.TRANSACTION);
    }

    @Test
    void asyncRunnerIsOnlyRequestContextBoundaryAndTransactionalPersistenceOwnsTransactions() throws NoSuchMethodException {
        assertCallerRequiredContextBoundary("loadAlertForVerification", String.class, String.class);
        assertCallerRequiredContextBoundary("persistVerificationOutcome",
                String.class,
                AlertVerificationOutcome.class,
                boolean.class,
                String.class);
        assertCallerRequiredContextBoundary("persistTechnicalError", String.class, String.class, String.class);

        assertTxBoundary("doLoadAlertForVerification", String.class);
        assertTxBoundary("doPersistVerificationOutcome", String.class, AlertVerificationOutcome.class, boolean.class);
        assertTxBoundary("doPersistTechnicalError", String.class, String.class);
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
    void serviceSchedulesAsyncVerificationThroughCdiClearedManagedExecutor() {
        ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
        AlertAsyncVerificationRequestContextRunner runner = mock(AlertAsyncVerificationRequestContextRunner.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService();
        service.managedExecutor = managedExecutor;
        service.requestContextRunner = runner;
        when(runner.verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a"))
                .thenReturn(new AlertDetail().id("ALRT1").status(AlertStatus.VERIFIED).enabled(true));
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        when(managedExecutor.runAsync(task.capture())).thenReturn(CompletableFuture.completedFuture(null));

        service.scheduleCreatedAlertVerification("ALRT1", true, "tenant-a");

        task.getValue().run();
        verify(managedExecutor).runAsync(org.mockito.ArgumentMatchers.any(Runnable.class));
        verify(runner).verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a");
    }

    @Test
    void consecutiveAsyncVerificationSchedulesIndependentCdiClearedManagedExecutorTasks() {
        ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
        AlertAsyncVerificationRequestContextRunner runner = mock(AlertAsyncVerificationRequestContextRunner.class);
        AlertAsyncVerificationService service = new AlertAsyncVerificationService();
        service.managedExecutor = managedExecutor;
        service.requestContextRunner = runner;
        when(runner.verifyCreatedAlertInRequestContext(org.mockito.ArgumentMatchers.anyString(), eq(true), eq("tenant-a")))
                .thenAnswer(invocation -> new AlertDetail()
                        .id(invocation.getArgument(0))
                        .status(AlertStatus.VERIFIED)
                        .enabled(true));
        ArgumentCaptor<Runnable> tasks = ArgumentCaptor.forClass(Runnable.class);
        when(managedExecutor.runAsync(tasks.capture())).thenReturn(CompletableFuture.completedFuture(null));

        for (int index = 0; index < 6; index++) {
            service.scheduleCreatedAlertVerification("ALRT" + index, true, "tenant-a");
        }

        verify(managedExecutor, org.mockito.Mockito.times(6)).runAsync(org.mockito.ArgumentMatchers.any(Runnable.class));
        tasks.getAllValues().forEach(Runnable::run);
        for (int index = 0; index < 6; index++) {
            verify(runner).verifyCreatedAlertInRequestContext("ALRT" + index, true, "tenant-a");
        }
    }

    @Test
    void tenantRestoreHappensInsideDbContextRunnerBeforeLoad() {
        TenantContext tenantContext = mock(TenantContext.class);
        AlertVerificationPersistenceContextBoundary contextBoundary = mock(AlertVerificationPersistenceContextBoundary.class);
        AlertVerificationPromptData promptData = new AlertVerificationPromptData(
                "ALRT1",
                "name",
                "description",
                "prompt",
                null);
        when(contextBoundary.loadAlertForVerification("ALRT1", "tenant-a")).thenReturn(Optional.of(promptData));
        AlertAsyncVerificationDbContextRunner dbContextRunner = new AlertAsyncVerificationDbContextRunner() {
            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        dbContextRunner.tenantContext = tenantContext;
        dbContextRunner.persistenceContextBoundary = contextBoundary;
        configureTenantStorage(tenantContext);

        Optional<AlertVerificationPromptData> result = dbContextRunner.loadAlertForVerification("ALRT1", "tenant-a");

        assertThat(result).contains(promptData);
        verify(tenantContext).setTenantId("tenant-a");
        verify(tenantContext).clear();
        verify(contextBoundary).loadAlertForVerification("ALRT1", "tenant-a");
    }

    @Test
    void dbContextRunnerUsesDefaultSchemaFallbackWhenCapturedTenantIsMissing() {
        TenantContext tenantContext = mock(TenantContext.class);
        AlertVerificationPersistenceContextBoundary contextBoundary = mock(AlertVerificationPersistenceContextBoundary.class);
        AlertVerificationPromptData promptData = new AlertVerificationPromptData(
                "ALRT1",
                "name",
                "description",
                "prompt",
                null);
        when(contextBoundary.loadAlertForVerification("ALRT1", "pis_intelligentinformationassistant"))
                .thenReturn(Optional.of(promptData));
        AlertAsyncVerificationDbContextRunner dbContextRunner = new AlertAsyncVerificationDbContextRunner() {
            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        dbContextRunner.tenantContext = tenantContext;
        dbContextRunner.persistenceContextBoundary = contextBoundary;
        dbContextRunner.defaultSchema = "pis_intelligentinformationassistant";
        configureTenantStorage(tenantContext);

        Optional<AlertVerificationPromptData> result = dbContextRunner.loadAlertForVerification("ALRT1", null);

        assertThat(result).contains(promptData);
        verify(tenantContext).setTenantId("pis_intelligentinformationassistant");
        verify(tenantContext).clear();
    }

    @Test
    void runnerDoesNotCallVerificationWhenDbLoadFailsWithoutTenantAndWithoutDefaultSchema() {
        TenantContext tenantContext = mock(TenantContext.class);
        AlertAsyncVerificationDbContextRunner dbContextRunner = mock(AlertAsyncVerificationDbContextRunner.class);
        AlertService alertService = mock(AlertService.class);
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected boolean requestContextActive() {
                return false;
            }
        };
        runner.tenantContext = tenantContext;
        runner.dbContextRunner = dbContextRunner;
        runner.alertService = alertService;
        when(dbContextRunner.loadAlertForVerification("ALRT1", null))
                .thenThrow(new AlertAsyncVerificationService.MissingTenantContextException("tenant missing"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        runner.verifyCreatedAlertInRequestContext("ALRT1", true, null))
                .isInstanceOf(AlertAsyncVerificationService.MissingTenantContextException.class);

        verify(alertService, never()).verifyAlertOutcome(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
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
        configureTenantStorage(tenantContext);

        AlertDetail result = runner.verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a");

        assertThat(result.getStatus()).isEqualTo(AlertStatus.REJECTED);
        assertThat(result.getEnabled()).isFalse();
        assertThat(enableFlagSeen.get()).isTrue();
    }

    @Test
    void verifiedEnableFalseCompletesNormally() {
        TenantContext tenantContext = mock(TenantContext.class);
        AtomicReference<String> tenantSeen = new AtomicReference<>();
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected AlertDetail verifyAndApplyEnable(String alertId, boolean enableAfterVerification, String tenant) {
                tenantSeen.set(tenant);
                return new AlertDetail()
                        .id(alertId)
                        .status(AlertStatus.VERIFIED)
                        .enabled(enableAfterVerification);
            }

            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.tenantContext = tenantContext;

        AlertDetail result = runner.verifyCreatedAlertInRequestContext("ALRT1", false, "tenant-a");

        assertThat(tenantSeen.get()).isEqualTo("tenant-a");
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
        configureTenantStorage(tenantContext);

        AlertDetail result = runner.verifyCreatedAlertInRequestContext("ALRT1", true, "tenant-a");

        assertThat(enableFlagSeen.get()).isTrue();
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    void runnerLoadsAndPersistsThroughDbContextRunnerWithoutDirectPersistenceBoundaryAccess() {
        AlertAsyncVerificationDbContextRunner dbContextRunner = mock(AlertAsyncVerificationDbContextRunner.class);
        AlertVerificationPersistenceContextBoundary contextBoundary = mock(AlertVerificationPersistenceContextBoundary.class);
        AlertService alertService = mock(AlertService.class);
        AlertVerificationPromptData promptData = new AlertVerificationPromptData(
                "ALRT1",
                "name",
                "description",
                "prompt",
                null);
        AlertVerificationOutcome outcome = mock(AlertVerificationOutcome.class);
        AlertDetail persisted = new AlertDetail().id("ALRT1").status(AlertStatus.VERIFIED).enabled(false);
        when(dbContextRunner.loadAlertForVerification("ALRT1", "tenant-a")).thenReturn(Optional.of(promptData));
        when(alertService.verifyAlertOutcome("ALRT1", promptData)).thenReturn(outcome);
        when(dbContextRunner.persistVerificationOutcome(
                org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(outcome),
                org.mockito.ArgumentMatchers.eq(false),
                org.mockito.ArgumentMatchers.eq("tenant-a")))
                .thenReturn(Optional.of(persisted));
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.dbContextRunner = dbContextRunner;
        runner.alertService = alertService;

        AlertDetail result = runner.verifyAndApplyEnable("ALRT1", false, "tenant-a");

        assertThat(result).isSameAs(persisted);
        verify(dbContextRunner).loadAlertForVerification("ALRT1", "tenant-a");
        verify(alertService).verifyAlertOutcome("ALRT1", promptData);
        verify(dbContextRunner).persistVerificationOutcome(
                org.mockito.ArgumentMatchers.eq("ALRT1"),
                org.mockito.ArgumentMatchers.eq(outcome),
                org.mockito.ArgumentMatchers.eq(false),
                org.mockito.ArgumentMatchers.eq("tenant-a"));
        org.mockito.Mockito.verifyNoInteractions(contextBoundary);
    }

    @Test
    void runnerMarksTechnicalErrorThroughDbContextRunner() {
        TenantContext tenantContext = mock(TenantContext.class);
        AlertAsyncVerificationDbContextRunner dbContextRunner = mock(AlertAsyncVerificationDbContextRunner.class);
        AlertAsyncVerificationRequestContextRunner runner = new AlertAsyncVerificationRequestContextRunner() {
            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        runner.tenantContext = tenantContext;
        runner.dbContextRunner = dbContextRunner;
        configureTenantStorage(tenantContext);

        runner.markCreatedAlertVerificationErrorInRequestContext("ALRT1", "provider timeout", "tenant-a");

        verify(dbContextRunner).persistTechnicalError("ALRT1", "provider timeout", "tenant-a");
        verify(tenantContext, never()).clear();
    }

    @Test
    void transactionalLoadBuildsDetachedDtoWithoutRepositoryPromptRead() {
        EntityManager entityManager = mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Alert> query = mock(TypedQuery.class);
        AlertRepository repository = mock(AlertRepository.class);
        TenantContext tenantContext = mock(TenantContext.class);
        Alert alert = new Alert();
        alert.setCodAlert("ALRT1");
        alert.setDscName("name");
        alert.setDscDescription("description");
        alert.setDscPrompt("prompt");
        when(entityManager.createQuery(anyString(), eq(Alert.class))).thenReturn(query);
        when(query.setParameter("alertId", "ALRT1")).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(alert));
        when(tenantContext.getTenantId()).thenReturn("tenant-a");
        AlertVerificationTransactionalPersistence persistence = new AlertVerificationTransactionalPersistence() {
            @Override
            protected boolean requestContextActive() {
                return true;
            }
        };
        persistence.entityManager = entityManager;
        persistence.alertRepository = repository;
        persistence.tenantContext = tenantContext;

        Optional<AlertVerificationPromptData> result = persistence.doLoadAlertForVerification("ALRT1");

        assertThat(result).isPresent();
        assertThat(result.get().alertId()).isEqualTo("ALRT1");
        assertThat(result.get().name()).isEqualTo("name");
        assertThat(result.get().description()).isEqualTo("description");
        assertThat(result.get().prompt()).isEqualTo("prompt");
        assertThat(result.get().locationResolutionContext().resolutions()).isEmpty();
        verify(repository, never()).getAlertVerificationPromptData(anyString());
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

    private void assertCallerRequiredContextBoundary(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = AlertVerificationPersistenceContextBoundary.class.getMethod(methodName, parameterTypes);
        assertThat(method.isAnnotationPresent(ActivateRequestContext.class))
                .as(methodName + " must use the caller-owned CDI request context")
                .isFalse();
        assertThat(method.isAnnotationPresent(Transactional.class))
                .as(methodName + " must not be transactional on the request context boundary")
                .isFalse();
    }

    private void assertTxBoundary(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = AlertVerificationTransactionalPersistence.class.getMethod(methodName, parameterTypes);
        assertThat(method.isAnnotationPresent(ActivateRequestContext.class))
                .as(methodName + " must not activate request context on the transaction boundary")
                .isFalse();
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional)
                .as(methodName + " is transactional")
                .isNotNull();
        assertThat(transactional.value())
                .as(methodName + " uses an isolated transaction boundary")
                .isEqualTo(Transactional.TxType.REQUIRES_NEW);
    }
}

