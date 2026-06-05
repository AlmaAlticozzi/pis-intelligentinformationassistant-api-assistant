package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ApplicationScoped
public class AlertAsyncVerificationDbContextRunner {

    @Inject
    AlertVerificationPersistenceContextBoundary persistenceContextBoundary;

    @Inject
    TenantContext tenantContext;

    @ConfigProperty(name = "fnd.default-schema", defaultValue = "")
    String defaultSchema;

    @ActivateRequestContext
    public Optional<AlertVerificationPromptData> loadAlertForVerification(String alertId, String propagatedTenant) {
        return runWithTenant("load", alertId, propagatedTenant,
                () -> persistenceContextBoundary.loadAlertForVerification(alertId, currentTenantId()));
    }

    @ActivateRequestContext
    public Optional<AlertDetail> persistVerificationOutcome(
            String alertId,
            AlertVerificationOutcome outcome,
            boolean enableAfterVerification,
            String propagatedTenant) {
        return runWithTenant("persist-outcome", alertId, propagatedTenant,
                () -> persistenceContextBoundary.persistVerificationOutcome(
                        alertId,
                        outcome,
                        enableAfterVerification,
                        currentTenantId()));
    }

    @ActivateRequestContext
    public Optional<AlertDetail> persistTechnicalError(String alertId, String shortMessage, String propagatedTenant) {
        return runWithTenant("persist-technical-error", alertId, propagatedTenant,
                () -> persistenceContextBoundary.persistTechnicalError(alertId, shortMessage, currentTenantId()));
    }

    private <T> T runWithTenant(String phase, String alertId, String propagatedTenant, DbOperation<T> operation) {
        System.out.println("[IIA][ALERT_VERIFY][DB_CONTEXT_RUNNER] phase=" + phase
                + " entry alertId=" + alertId
                + " requestContextActive=" + requestContextActive()
                + " tenantPresentBeforeRestore=" + (currentTenantId() != null)
                + " propagatedTenant=" + safeTenant(normalize(propagatedTenant))
                + " thread=" + Thread.currentThread().getName());
        try {
            restoreTenant(alertId, phase, propagatedTenant);
            System.out.println("[IIA][ALERT_VERIFY][DB_CONTEXT_RUNNER] phase=" + phase
                    + " after tenant restore alertId=" + alertId
                    + " requestContextActive=" + requestContextActive()
                    + " tenantPresent=" + (currentTenantId() != null)
                    + " tenant=" + safeTenant(currentTenantId())
                    + " thread=" + Thread.currentThread().getName());
            return operation.run();
        } finally {
            System.out.println("[IIA][ALERT_VERIFY][DB_CONTEXT_RUNNER] phase=" + phase
                    + " before clear tenant alertId=" + alertId
                    + " requestContextActive=" + requestContextActive()
                    + " tenantPresent=" + (currentTenantId() != null)
                    + " tenant=" + safeTenant(currentTenantId())
                    + " thread=" + Thread.currentThread().getName());
            clearTenant();
            System.out.println("[IIA][ALERT_VERIFY][DB_CONTEXT_RUNNER] phase=" + phase
                    + " after clear tenant alertId=" + alertId
                    + " requestContextActive=" + requestContextActive()
                    + " tenantPresent=" + (currentTenantId() != null)
                    + " tenant=" + safeTenant(currentTenantId())
                    + " thread=" + Thread.currentThread().getName());
        }
    }

    private void restoreTenant(String alertId, String phase, String propagatedTenant) {
        if (tenantContext == null) {
            throw new AlertAsyncVerificationService.MissingTenantContextException(
                    "Alert verification DB context cannot run because TenantContext is unavailable. alertId=" + alertId);
        }
        String tenantToInstall = normalize(propagatedTenant);
        if (tenantToInstall == null) {
            tenantToInstall = normalizedDefaultSchema();
        }
        if (tenantToInstall == null) {
            throw new AlertAsyncVerificationService.MissingTenantContextException(
                    "Alert verification DB context cannot run because tenant context is missing and no default schema fallback is configured. alertId="
                            + alertId + ", phase=" + phase + ".");
        }
        tenantContext.setTenantId(tenantToInstall);
        if (currentTenantId() == null) {
            throw new AlertAsyncVerificationService.MissingTenantContextException(
                    "Alert verification DB context failed to restore tenant. alertId=" + alertId + ", phase=" + phase + ".");
        }
    }

    private void clearTenant() {
        if (tenantContext != null) {
            tenantContext.clear();
        }
    }

    String currentTenantId() {
        if (tenantContext == null) {
            return null;
        }
        String tenantId = tenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? null : tenantId;
    }

    String normalizedDefaultSchema() {
        return normalize(defaultSchema);
    }

    protected boolean requestContextActive() {
        try {
            return io.quarkus.arc.Arc.container().requestContext().isActive();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeTenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? "<none>" : tenantId;
    }

    private interface DbOperation<T> {
        T run();
    }
}
