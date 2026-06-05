package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ApplicationScoped
public class AlertVerificationPersistenceContextBoundary {

    @Inject
    AlertVerificationTransactionalPersistence transactionalPersistence;

    @Inject
    TenantContext tenantContext;

    @Inject
    TransactionManager transactionManager;

    @ConfigProperty(name = "fnd.default-schema", defaultValue = "")
    String defaultSchema;

    // The async runner owns the request context; persistence methods require the caller-owned context to be active.
    public Optional<AlertVerificationPromptData> loadAlertForVerification(String alertId, String tenant) {
        TenantRestore tenantRestore = restoreTenant(alertId, tenant, "loadAlertForVerification");
        try {
            logBoundary("loadAlertForVerification", alertId);
            guardReadyForAsyncDbAccess("loadAlertForVerification", alertId);
            return transactionalPersistence.doLoadAlertForVerification(alertId);
        } catch (RuntimeException ex) {
            logException("loadAlertForVerification", alertId, ex);
            throw ex;
        } finally {
            tenantRestore.restore();
        }
    }

    // The async runner owns the request context; persistence methods require the caller-owned context to be active.
    public Optional<AlertDetail> persistVerificationOutcome(
            String alertId,
            AlertVerificationOutcome outcome,
            boolean enableAfterVerification,
            String tenant) {
        TenantRestore tenantRestore = restoreTenant(alertId, tenant, "persistVerificationOutcome");
        try {
            logBoundary("persistVerificationOutcome", alertId);
            guardReadyForAsyncDbAccess("persistVerificationOutcome", alertId);
            return transactionalPersistence.doPersistVerificationOutcome(alertId, outcome, enableAfterVerification);
        } catch (RuntimeException ex) {
            logException("persistVerificationOutcome", alertId, ex);
            throw ex;
        } finally {
            tenantRestore.restore();
        }
    }

    // The async runner owns the request context; persistence methods require the caller-owned context to be active.
    public Optional<AlertDetail> persistTechnicalError(String alertId, String shortMessage, String tenant) {
        TenantRestore tenantRestore = restoreTenant(alertId, tenant, "persistTechnicalError");
        try {
            logBoundary("persistTechnicalError", alertId);
            guardReadyForAsyncDbAccess("persistTechnicalError", alertId);
            return transactionalPersistence.doPersistTechnicalError(alertId, shortMessage);
        } catch (RuntimeException ex) {
            logException("persistTechnicalError", alertId, ex);
            throw ex;
        } finally {
            tenantRestore.restore();
        }
    }

    private TenantRestore restoreTenant(String alertId, String tenant, String method) {
        String previousTenant = currentTenantId();
        String tenantToInstall = normalize(tenant);
        if (tenantToInstall == null) {
            tenantToInstall = normalizedDefaultSchema();
        }
        boolean changed = tenantToInstall != null
                && tenantContext != null
                && !tenantToInstall.equals(previousTenant);
        if (changed) {
            tenantContext.setTenantId(tenantToInstall);
        }
        if (currentTenantId() == null) {
            throw new AlertAsyncVerificationService.MissingTenantContextException(
                    "Alert verification context boundary " + method + " cannot run because tenant context is missing. alertId=" + alertId);
        }
        return new TenantRestore(changed, previousTenant);
    }

    private void logBoundary(String method, String alertId) {
        System.out.println("[IIA][ALERT_VERIFY][CONTEXT_BOUNDARY]"
                + " method=" + method
                + " alertId=" + alertId
                + " requestContextOwner=CALLER_REQUIRED"
                + " requestContextActive=" + requestContextActive()
                + " transactionActive=" + transactionActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " thread=" + Thread.currentThread().getName());
    }

    private void guardReadyForAsyncDbAccess(String method, String alertId) {
        boolean requestContextActive = requestContextActive();
        boolean tenantPresent = currentTenantId() != null;
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_DB_GUARD]"
                + " phase=context-boundary"
                + " method=" + method
                + " alertId=" + alertId
                + " requestContextOwner=CALLER_REQUIRED"
                + " requestContextActive=" + requestContextActive
                + " transactionActive=" + transactionActive()
                + " tenantPresent=" + tenantPresent
                + " tenant=" + safeTenant(currentTenantId())
                + " thread=" + Thread.currentThread().getName());
        if (!requestContextActive || !tenantPresent) {
            throw new IllegalStateException("Async alert verification DB access is not safe before "
                    + method
                    + ": requestContextActive=" + requestContextActive
                    + ", tenantPresent=" + tenantPresent
                    + ", alertId=" + alertId + ".");
        }
    }

    String currentTenantId() {
        if (tenantContext == null) {
            return null;
        }
        String tenantId = tenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? null : tenantId;
    }

    private String normalizedDefaultSchema() {
        return normalize(defaultSchema);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeTenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? "<none>" : tenantId;
    }

    private boolean requestContextActive() {
        try {
            return io.quarkus.arc.Arc.container().requestContext().isActive();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean transactionActive() {
        if (transactionManager == null) {
            return false;
        }
        try {
            return transactionManager.getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException ex) {
            return false;
        }
    }

    private void logException(String method, String alertId, RuntimeException ex) {
        Throwable rootCause = rootCause(ex);
        System.out.println("[IIA][ALERT_VERIFY][CONTEXT_BOUNDARY][ERROR]"
                + " method=" + method
                + " alertId=" + alertId
                + " exceptionClass=" + ex.getClass().getName()
                + " exceptionMessage=" + ex.getMessage()
                + " rootCauseClass=" + rootCause.getClass().getName()
                + " rootCauseMessage=" + rootCause.getMessage()
                + " requestContextActive=" + requestContextActive()
                + " transactionActive=" + transactionActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " thread=" + Thread.currentThread().getName());
        printStackTracePreview(ex);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private void printStackTracePreview(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int limit = Math.min(80, stackTrace.length);
        for (int index = 0; index < limit; index++) {
            System.out.println("[IIA][ALERT_VERIFY][CONTEXT_BOUNDARY][ERROR] stack[" + index + "]="
                    + stackTrace[index]);
        }
    }

    private class TenantRestore {
        private final boolean changed;
        private final String previousTenant;

        private TenantRestore(boolean changed, String previousTenant) {
            this.changed = changed;
            this.previousTenant = previousTenant;
        }

        private void restore() {
            if (!changed || tenantContext == null) {
                return;
            }
            if (previousTenant == null) {
                tenantContext.clear();
            } else {
                tenantContext.setTenantId(previousTenant);
            }
        }
    }
}
