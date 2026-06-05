package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

@ApplicationScoped
public class AlertAsyncVerificationRequestContextRunner {

    @Inject
    AlertService alertService;

    @Inject
    AlertAsyncVerificationDbContextRunner dbContextRunner;

    @Inject
    TenantContext tenantContext;

    @Inject
    TransactionManager transactionManager;

    public AlertDetail verifyCreatedAlertInRequestContext(
            String alertId,
            boolean enableAfterVerification,
            String propagatedTenantId) {
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] entry alertId=" + alertId
                + " requestContextOwner=ASYNC_ORCHESTRATOR"
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " capturedTenant=" + safeTenant(normalize(propagatedTenantId)));
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] before detached verify orchestration alertId=" + alertId
                + " requestContextOwner=ASYNC_ORCHESTRATOR"
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " transactionActive=" + transactionActive());
        return verifyAndApplyEnable(alertId, enableAfterVerification, propagatedTenantId);
    }

    public void markCreatedAlertVerificationErrorInRequestContext(
            String alertId,
            String shortMessage,
            String propagatedTenantId) {
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] technical-error entry alertId=" + alertId
                + " requestContextOwner=ASYNC_ORCHESTRATOR"
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " capturedTenant=" + safeTenant(normalize(propagatedTenantId)));
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] persisting technical error alertId=" + alertId
                + " tenantPresent=" + (currentTenantId() != null)
                + " method=dbContextRunner.persistTechnicalError");
        dbContextRunner.persistTechnicalError(alertId, shortMessage, propagatedTenantId);
    }

    protected AlertDetail verifyAndApplyEnable(String alertId, boolean enableAfterVerification, String propagatedTenant) {
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_CONTEXT] before LLM requestContextActive="
                + requestContextActive());
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] before loadAlertForVerification alertId=" + alertId
                + " requestContextOwner=ASYNC_ORCHESTRATOR"
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " transactionActive=" + transactionActive()
                + " thread=" + Thread.currentThread().getName());
        AlertVerificationPromptData promptData = dbContextRunner.loadAlertForVerification(alertId, propagatedTenant)
                .orElseThrow(() -> new IllegalStateException("Created alert not found during async verification."));
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] after loadAlertForVerification alertId=" + alertId
                + " requestContextOwner=ASYNC_ORCHESTRATOR"
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " transactionActive=" + transactionActive()
                + " promptDataPresent=true"
                + " thread=" + Thread.currentThread().getName());
        AlertVerificationOutcome outcome = alertService.verifyAlertOutcome(alertId, promptData);
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] before persistVerificationOutcome alertId=" + alertId
                + " requestContextOwner=ASYNC_ORCHESTRATOR"
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " transactionActive=" + transactionActive()
                + " thread=" + Thread.currentThread().getName());
        AlertDetail verifiedAlert = dbContextRunner.persistVerificationOutcome(
                        alertId,
                        outcome,
                        enableAfterVerification,
                        propagatedTenant)
                .orElseThrow(() -> new IllegalStateException("Created alert not found during async verification."));
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] after persistVerificationOutcome alertId=" + alertId
                + " requestContextOwner=ASYNC_ORCHESTRATOR"
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " transactionActive=" + transactionActive()
                + " thread=" + Thread.currentThread().getName());
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

    String currentTenantId() {
        if (tenantContext == null) {
            return null;
        }
        String tenantId = tenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? null : tenantId;
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
