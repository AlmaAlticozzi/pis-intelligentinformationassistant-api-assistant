package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import io.quarkus.narayana.jta.QuarkusTransaction;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

@ApplicationScoped
public class AlertAsyncVerificationService {

    @Inject
    AlertService alertService;

    @Inject
    AlertRepository alertRepository;

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TenantContext tenantContext;

    @ActivateRequestContext
    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification) {
        verifyCreatedAlertAsync(alertId, enableAfterVerification, currentTenantId());
    }

    @ActivateRequestContext
    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification, String propagatedTenantId) {
        boolean tenantInstalled = installTenantIfNeeded(alertId, propagatedTenantId);
        System.out.println("[IIA][ALERT_VERIFY][ASYNC] Started async verification alertId=" + alertId);
        try {
            AlertDetail finalAlert = verifyAndApplyEnableInNewTransaction(alertId, enableAfterVerification);
            System.out.println("[IIA][ALERT_VERIFY][ASYNC] Completed async verification alertId="
                    + alertId
                    + " finalStatus="
                    + finalAlert.getStatus()
                    + " enabled="
                    + finalAlert.getEnabled());
            if (AlertStatus.ERROR.equals(finalAlert.getStatus())) {
                System.out.println("[IIA][ALERT_CREATE] finalStatus=ERROR enabled=false");
            }
        } catch (RuntimeException ex) {
            String shortMessage = shortTechnicalMessage(ex);
            System.out.println("[IIA][ALERT_VERIFY][ASYNC_ERROR] alertId=" + alertId + " error=" + shortMessage);
            markCreatedAlertVerificationError(alertId, shortMessage, propagatedTenantId);
            System.out.println("[IIA][ALERT_CREATE] finalStatus=ERROR enabled=false");
        } finally {
            if (tenantInstalled) {
                tenantContext.clear();
                System.out.println("[IIA][ALERT_VERIFY][TENANT] cleared propagated async tenant alertId=" + alertId);
            }
        }
    }

    @ActivateRequestContext
    public void markCreatedAlertVerificationError(String alertId, String shortMessage) {
        markCreatedAlertVerificationError(alertId, shortMessage, currentTenantId());
    }

    @ActivateRequestContext
    public void markCreatedAlertVerificationError(String alertId, String shortMessage, String propagatedTenantId) {
        boolean tenantInstalled = installTenantIfNeeded(alertId, propagatedTenantId);
        try {
            QuarkusTransaction.requiringNew().run(() -> {
                logBeforeRepositoryOperation(alertId, "markAlertVerificationTechnicalError");
                alertRepository.markAlertVerificationTechnicalError(
                        alertId,
                        shortMessage,
                        aiConfiguration.provider(),
                        aiConfiguration.alertVerify().model());
            });
        } finally {
            if (tenantInstalled) {
                tenantContext.clear();
            }
        }
    }

    private AlertDetail verifyAndApplyEnableInNewTransaction(String alertId, boolean enableAfterVerification) {
        return QuarkusTransaction.requiringNew().call(() -> {
            System.out.println("[IIA][ALERT_VERIFY][ASYNC_CONTEXT] before LLM requestContextActive="
                    + io.quarkus.arc.Arc.container().requestContext().isActive());
            AlertVerificationRequest verificationRequest = new AlertVerificationRequest()
                    .force(Boolean.FALSE);
            logBeforeRepositoryOperation(alertId, "verifyAlert");
            AlertDetail verifiedAlert = alertService.verifyAlert(alertId, verificationRequest)
                    .orElseThrow(() -> new IllegalStateException("Created alert not found during async verification."));
            AlertStatus status = verifiedAlert.getStatus();
            boolean enabled = AlertStatus.VERIFIED.equals(status) && enableAfterVerification;
            logBeforeRepositoryOperation(alertId, "updateAlertEnabledAfterCreateVerification");
            return alertRepository.updateAlertEnabledAfterCreateVerification(alertId, enabled)
                    .orElse(verifiedAlert);
        });
    }

    private String shortTechnicalMessage(Throwable throwable) {
        if (isMissingTenantFailure(throwable)) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] missing tenant context caused async verification failure");
            return "Alert verification failed because tenant context is missing during asynchronous verification.";
        }
        String message = throwable.getMessage();
        if ((message == null || message.isBlank()) && throwable.getCause() != null) {
            message = throwable.getCause().getMessage();
        }
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private boolean installTenantIfNeeded(String alertId, String propagatedTenantId) {
        if (tenantContext == null) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] no TenantContext bean available alertId=" + alertId);
            return false;
        }
        if (currentTenantId() != null) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] async tenant already present alertId=" + alertId);
            return false;
        }
        if (propagatedTenantId == null || propagatedTenantId.isBlank()) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] missing propagated tenant before async verify alertId=" + alertId);
            return false;
        }
        tenantContext.setTenantId(propagatedTenantId);
        System.out.println("[IIA][ALERT_VERIFY][TENANT] restored tenant before async verify alertId=" + alertId);
        return true;
    }

    private void logBeforeRepositoryOperation(String alertId, String operation) {
        System.out.println("[IIA][ALERT_VERIFY][TENANT] before repository operation=" + operation
                + " alertId=" + alertId
                + " tenantPresent=" + (currentTenantId() != null));
    }

    private String currentTenantId() {
        if (tenantContext == null) {
            return null;
        }
        String tenantId = tenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? null : tenantId;
    }

    private boolean isMissingTenantFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null
                    && (message.contains("tenant identifier")
                    || message.contains("tenant context")
                    || message.contains("multi-tenancy"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
