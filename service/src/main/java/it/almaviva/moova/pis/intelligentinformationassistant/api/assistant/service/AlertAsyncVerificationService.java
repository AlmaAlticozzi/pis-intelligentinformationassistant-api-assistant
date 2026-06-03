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
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

    @ConfigProperty(name = "fnd.default-schema", defaultValue = "")
    String defaultSchema;

    @ActivateRequestContext
    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification) {
        verifyCreatedAlertAsync(alertId, enableAfterVerification, currentTenantId());
    }

    @ActivateRequestContext
    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification, String propagatedTenantId) {
        boolean tenantInstalled = installTenantIfNeeded(alertId, propagatedTenantId);
        System.out.println("[IIA][ALERT_VERIFY][ASYNC] Started async verification alertId=" + alertId);
        try {
            if (currentTenantId() == null) {
                throw new MissingTenantContextException("Alert verification failed because tenant context is missing during asynchronous verification and no default schema fallback is configured.");
            }
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
                System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] cleared propagated async tenant alertId=" + alertId
                        + " tenantPresentAfterClear=" + (currentTenantId() != null));
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
            if (currentTenantId() == null) {
                System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] cannot mark technical error because tenant is missing"
                        + " alertId=" + alertId
                        + " defaultSchemaFallback=" + normalizedDefaultSchema());
                return;
            }
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

    protected AlertDetail verifyAndApplyEnableInNewTransaction(String alertId, boolean enableAfterVerification) {
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
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] async tenant before propagation alertId=" + alertId
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
            System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] async tenant after propagation alertId=" + alertId
                    + " tenantPresent=true tenant=" + safeTenant(currentTenantId())
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
            System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] async tenant after propagation alertId=" + alertId
                    + " tenantPresent=false tenant=<none> source=none");
            return false;
        }
        if ("defaultSchema".equals(source)) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] using default schema fallback=" + tenantToInstall
                    + " alertId=" + alertId);
        }
        tenantContext.setTenantId(tenantToInstall);
        System.out.println("[IIA][ALERT_VERIFY][TENANT] restored tenant before async verify alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] async tenant after propagation alertId=" + alertId
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " source=" + source);
        return true;
    }

    private void logBeforeRepositoryOperation(String alertId, String operation) {
        System.out.println("[IIA][ALERT_VERIFY][TENANT] before repository operation=" + operation
                + " alertId=" + alertId
                + " tenantPresent=" + (currentTenantId() != null));
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] before repository operation=" + operation
                + " alertId=" + alertId
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " defaultSchemaFallback=" + normalizedDefaultSchema());
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

    static class MissingTenantContextException extends RuntimeException {
        MissingTenantContextException(String message) {
            super(message);
        }
    }
}
