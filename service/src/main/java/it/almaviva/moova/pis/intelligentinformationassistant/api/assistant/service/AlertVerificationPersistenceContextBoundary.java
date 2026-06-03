package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
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

    @ActivateRequestContext
    public Optional<AlertVerificationPromptData> loadAlertForVerification(String alertId, String tenant) {
        TenantRestore tenantRestore = restoreTenant(alertId, tenant, "loadAlertForVerification");
        try {
            logBoundary("loadAlertForVerification", alertId);
            return transactionalPersistence.doLoadAlertForVerification(alertId);
        } finally {
            tenantRestore.restore();
        }
    }

    @ActivateRequestContext
    public Optional<AlertDetail> persistVerificationOutcome(
            String alertId,
            AlertVerificationOutcome outcome,
            boolean enableAfterVerification,
            String tenant) {
        TenantRestore tenantRestore = restoreTenant(alertId, tenant, "persistVerificationOutcome");
        try {
            logBoundary("persistVerificationOutcome", alertId);
            return transactionalPersistence.doPersistVerificationOutcome(alertId, outcome, enableAfterVerification);
        } finally {
            tenantRestore.restore();
        }
    }

    @ActivateRequestContext
    public Optional<AlertDetail> persistTechnicalError(String alertId, String shortMessage, String tenant) {
        TenantRestore tenantRestore = restoreTenant(alertId, tenant, "persistTechnicalError");
        try {
            logBoundary("persistTechnicalError", alertId);
            return transactionalPersistence.doPersistTechnicalError(alertId, shortMessage);
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
                + " requestContextActive=" + requestContextActive()
                + " transactionActive=" + transactionActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " thread=" + Thread.currentThread().getName());
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
