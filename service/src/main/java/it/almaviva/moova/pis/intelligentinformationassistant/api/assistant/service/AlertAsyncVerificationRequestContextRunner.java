package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AlertAsyncVerificationRequestContextRunner {

    @Inject
    AlertService alertService;

    @Inject
    AlertVerificationPersistenceContextBoundary persistenceContextBoundary;

    @Inject
    TenantContext tenantContext;

    @Inject
    TransactionManager transactionManager;

    @ConfigProperty(name = "fnd.default-schema", defaultValue = "")
    String defaultSchema;

    @ActivateRequestContext
    public AlertDetail verifyCreatedAlertInRequestContext(
            String alertId,
            boolean enableAfterVerification,
            String propagatedTenantId) {
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] entry alertId=" + alertId
                + " requestContextActive=" + requestContextActive()
                + " tenantPresentBeforeRestore=" + (currentTenantId() != null)
                + " capturedTenant=" + safeTenant(normalize(propagatedTenantId)));
        boolean tenantInstalled = installTenantIfNeeded(alertId, propagatedTenantId, "runner");
        try {
            if (currentTenantId() == null) {
                throw new AlertAsyncVerificationService.MissingTenantContextException("Alert verification failed because tenant context is missing during asynchronous verification and no default schema fallback is configured.");
            }
            System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] before detached verify orchestration alertId=" + alertId
                    + " requestContextActive=" + requestContextActive()
                    + " tenantPresent=" + (currentTenantId() != null)
                    + " transactionActive=" + transactionActive());
            return verifyAndApplyEnable(alertId, enableAfterVerification, currentTenantId());
        } finally {
            if (tenantInstalled) {
                tenantContext.clear();
                System.out.println("[IIA][ALERT_VERIFY][TENANT] cleared propagated async tenant alertId=" + alertId);
                System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] runner tenant cleared alertId=" + alertId
                        + " tenantPresentAfterClear=" + (currentTenantId() != null)
                        + " requestContextActive=" + requestContextActive());
            }
        }
    }

    @ActivateRequestContext
    public void markCreatedAlertVerificationErrorInRequestContext(
            String alertId,
            String shortMessage,
            String propagatedTenantId) {
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] technical-error entry alertId=" + alertId
                + " requestContextActive=" + requestContextActive()
                + " tenantPresentBeforeRestore=" + (currentTenantId() != null)
                + " capturedTenant=" + safeTenant(normalize(propagatedTenantId)));
        boolean tenantInstalled = installTenantIfNeeded(alertId, propagatedTenantId, "technical-error");
        try {
            if (currentTenantId() == null) {
                System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] cannot mark technical error because tenant is missing"
                        + " alertId=" + alertId
                        + " defaultSchemaFallback=" + normalizedDefaultSchema());
                return;
            }
            System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] persisting technical error alertId=" + alertId
                    + " tenantPresent=" + (currentTenantId() != null)
                    + " method=persistenceContextBoundary.persistTechnicalError");
            persistenceContextBoundary.persistTechnicalError(alertId, shortMessage, currentTenantId());
        } finally {
            if (tenantInstalled) {
                tenantContext.clear();
                System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] runner technical-error tenant cleared alertId=" + alertId
                        + " tenantPresentAfterClear=" + (currentTenantId() != null));
            }
        }
    }

    protected AlertDetail verifyAndApplyEnable(String alertId, boolean enableAfterVerification, String tenant) {
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_CONTEXT] before LLM requestContextActive="
                + requestContextActive());
        AlertVerificationPromptData promptData = persistenceContextBoundary.loadAlertForVerification(alertId, tenant)
                .orElseThrow(() -> new IllegalStateException("Created alert not found during async verification."));
        AlertVerificationOutcome outcome = alertService.verifyAlertOutcome(alertId, promptData);
        AlertDetail verifiedAlert = persistenceContextBoundary.persistVerificationOutcome(
                        alertId,
                        outcome,
                        enableAfterVerification,
                        tenant)
                .orElseThrow(() -> new IllegalStateException("Created alert not found during async verification."));
        AlertStatus status = verifiedAlert.getStatus();
        AlertVerificationStatus verificationStatus = verifiedAlert.getVerification() == null
                ? null
                : verifiedAlert.getVerification().getStatus();
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] normal outcome persisted alertId=" + alertId
                + " status=" + status
                + " verificationStatus=" + verificationStatus
                + " enabled=" + verifiedAlert.getEnabled());
        boolean shouldApplyEnableAfterVerification = AlertStatus.VERIFIED.equals(status) && enableAfterVerification;
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] enableAfterVerification decision alertId=" + alertId
                + " requested=" + enableAfterVerification
                + " status=" + status
                + " willApply=" + shouldApplyEnableAfterVerification);
        if (!shouldApplyEnableAfterVerification) {
            System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] skipping updateAlertEnabledAfterCreateVerification alertId="
                    + alertId + " finalStatus=" + status);
        }
        return verifiedAlert;
    }

    protected boolean installTenantIfNeeded(String alertId, String propagatedTenantId, String phase) {
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] runner tenant before restore alertId=" + alertId
                + " phase=" + phase
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " propagatedTenant=" + safeTenant(normalize(propagatedTenantId))
                + " defaultSchemaFallback=" + normalizedDefaultSchema());
        if (tenantContext == null) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] no TenantContext bean available alertId=" + alertId);
            System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] tenant propagation skipped because TenantContext bean is unavailable alertId=" + alertId);
            return false;
        }
        if (currentTenantId() != null) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] async tenant already present alertId=" + alertId);
            System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] runner tenant after restore alertId=" + alertId
                    + " tenantPresent=true tenant=" + safeTenant(currentTenantId())
                    + " requestContextActive=" + requestContextActive()
                    + " source=already-present");
            return false;
        }
        String tenantToInstall = normalize(propagatedTenantId);
        String source = "propagatedTenant";
        if (tenantToInstall == null) {
            tenantToInstall = normalizedDefaultSchema();
            source = "defaultSchema";
        }
        if (tenantToInstall == null) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] missing propagated tenant before async verify alertId=" + alertId);
            System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] runner tenant after restore alertId=" + alertId
                    + " tenantPresent=false tenant=<none> source=none");
            return false;
        }
        if ("defaultSchema".equals(source)) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] using default schema fallback=" + tenantToInstall
                    + " alertId=" + alertId);
        }
        tenantContext.setTenantId(tenantToInstall);
        System.out.println("[IIA][ALERT_VERIFY][TENANT] restored tenant before async verify alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] runner tenant after restore alertId=" + alertId
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " requestContextActive=" + requestContextActive()
                + " source=" + source);
        return true;
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

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeTenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? "<none>" : tenantId;
    }
}
