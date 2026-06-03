package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import io.quarkus.narayana.jta.QuarkusTransaction;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
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

    @Inject
    TransactionManager transactionManager;

    @Inject
    RequestContextController requestContextController;

    @ConfigProperty(name = "fnd.default-schema", defaultValue = "")
    String defaultSchema;

    @ActivateRequestContext
    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification) {
        verifyCreatedAlertAsync(alertId, enableAfterVerification, currentTenantId());
    }

    @ActivateRequestContext
    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification, String propagatedTenantId) {
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT] async task entry alertId=" + alertId
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " propagatedTenant=" + safeTenant(normalize(propagatedTenantId)));
        boolean requestContextActivated = activateRequestContextIfNeeded(alertId);
        boolean tenantInstalled = installTenantIfNeeded(alertId, propagatedTenantId);
        System.out.println("[IIA][ALERT_VERIFY][ASYNC] Started async verification alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] started alertId=" + alertId
                + " enableAfterVerification=" + enableAfterVerification
                + " tenantPresent=" + (currentTenantId() != null)
                + " requestContextActive=" + requestContextActive());
        try {
            if (currentTenantId() == null) {
                throw new MissingTenantContextException("Alert verification failed because tenant context is missing during asynchronous verification and no default schema fallback is configured.");
            }
            System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT] before alertService.verifyAlert alertId=" + alertId
                    + " requestContextActive=" + requestContextActive()
                    + " tenantPresent=" + (currentTenantId() != null)
                    + " transactionActive=" + transactionActive());
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
            System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] technical exception alertId=" + alertId
                    + " exceptionClass=" + ex.getClass().getName()
                    + " exceptionMessage=" + ex.getMessage()
                    + " rootCauseClass=" + rootCause(ex).getClass().getName()
                    + " rootCauseMessage=" + rootCause(ex).getMessage()
                    + " tenantPresent=" + (currentTenantId() != null)
                    + " transactionActive=" + transactionActive()
                    + " willCallMarkTechnicalError=true");
            printStackTracePreview(ex);
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
            if (requestContextActivated) {
                requestContextController.deactivate();
                System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT] deactivated async request context alertId=" + alertId
                        + " requestContextActive=" + requestContextActive()
                        + " tenantCleared=true");
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
            System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] persisting technical error alertId=" + alertId
                    + " tenantPresent=" + (currentTenantId() != null)
                    + " method=markAlertVerificationTechnicalError");
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
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_CONTEXT] before LLM requestContextActive="
                + requestContextActive());
        AlertVerificationRequest verificationRequest = new AlertVerificationRequest()
                .force(Boolean.FALSE);
        logBeforeRepositoryOperation(alertId, "verifyAlert");
        AlertDetail verifiedAlert = alertService.verifyAlert(alertId, verificationRequest)
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
            return verifiedAlert;
        }
        return QuarkusTransaction.requiringNew().call(() -> {
            logBeforeRepositoryOperation(alertId, "updateAlertEnabledAfterCreateVerification");
            return alertRepository.updateAlertEnabledAfterCreateVerification(alertId, true)
                    .orElse(verifiedAlert);
        });
    }

    private String shortTechnicalMessage(Throwable throwable) {
        if (isMissingTenantFailure(throwable) && currentTenantId() == null) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] missing tenant context caused async verification failure");
            return "Alert verification failed because tenant context is missing during asynchronous verification.";
        }
        if (isHibernateTenantResolverFailure(throwable)) {
            return "Hibernate tenant resolver returned no tenant because CDI request context is inactive or tenant was not restored in the active request context.";
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
                + " requestContextActive=" + requestContextActive()
                + " source=" + source);
        return true;
    }

    private boolean activateRequestContextIfNeeded(String alertId) {
        if (requestContextActive()) {
            System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT] async request context already active alertId=" + alertId
                    + " requestContextActive=true"
                    + " tenantPresent=" + (currentTenantId() != null));
            return false;
        }
        if (requestContextController == null) {
            System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT] request context controller unavailable alertId=" + alertId
                    + " requestContextActive=false");
            return false;
        }
        boolean activated = requestContextController.activate();
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT] async request context activated alertId=" + alertId
                + " activated=" + activated
                + " requestContextActive=" + requestContextActive()
                + " tenantPresentBeforeRestore=" + (currentTenantId() != null));
        return activated;
    }

    private void logBeforeRepositoryOperation(String alertId, String operation) {
        System.out.println("[IIA][ALERT_VERIFY][TENANT] before repository operation=" + operation
                + " alertId=" + alertId
                + " tenantPresent=" + (currentTenantId() != null)
                + " requestContextActive=" + requestContextActive());
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] before repository operation=" + operation
                + " alertId=" + alertId
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " requestContextActive=" + requestContextActive()
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

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private void printStackTracePreview(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int limit = Math.min(10, stackTrace.length);
        for (int index = 0; index < limit; index++) {
            System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] stack[" + index + "]=" + stackTrace[index]);
        }
    }

    private boolean isMissingTenantFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof MissingTenantContextException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null
                    && (message.contains("tenant identifier")
                    || message.contains("multi-tenancy")
                    || message.contains("tenant context is missing"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isHibernateTenantResolverFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null
                    && message.contains("SessionFactory configured for multi-tenancy, but no tenant identifier specified")) {
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
