package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

@ApplicationScoped
public class AlertAsyncVerificationService {

    @Inject
    AlertAsyncVerificationRequestContextRunner requestContextRunner;

    @Inject
    TenantContext tenantContext;

    @Inject
    TransactionManager transactionManager;

    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification) {
        verifyCreatedAlertAsync(alertId, enableAfterVerification, currentTenantId());
    }

    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification, String propagatedTenantId) {
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT] async task entry alertId=" + alertId
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " propagatedTenant=" + safeTenant(normalize(propagatedTenantId)));
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT_RUNNER] before runner alertId=" + alertId
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " capturedTenant=" + safeTenant(normalize(propagatedTenantId)));
        System.out.println("[IIA][ALERT_VERIFY][ASYNC] Started async verification alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] started alertId=" + alertId
                + " enableAfterVerification=" + enableAfterVerification
                + " tenantPresent=" + (currentTenantId() != null)
                + " requestContextActive=" + requestContextActive());
        try {
            AlertDetail finalAlert = requestContextRunner.verifyCreatedAlertInRequestContext(
                    alertId,
                    enableAfterVerification,
                    propagatedTenantId);
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
        }
    }

    public void markCreatedAlertVerificationError(String alertId, String shortMessage) {
        markCreatedAlertVerificationError(alertId, shortMessage, currentTenantId());
    }

    public void markCreatedAlertVerificationError(String alertId, String shortMessage, String propagatedTenantId) {
        requestContextRunner.markCreatedAlertVerificationErrorInRequestContext(alertId, shortMessage, propagatedTenantId);
    }

    private String shortTechnicalMessage(Throwable throwable) {
        if (isHibernateTenantResolverFailure(throwable)) {
            return "Hibernate tenant resolver returned no tenant because CDI request context is inactive or tenant was not restored in the active request context.";
        }
        if (isMissingTenantFailure(throwable) && currentTenantId() == null) {
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

    String currentTenantId() {
        if (tenantContext == null) {
            return null;
        }
        String tenantId = tenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? null : tenantId;
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
