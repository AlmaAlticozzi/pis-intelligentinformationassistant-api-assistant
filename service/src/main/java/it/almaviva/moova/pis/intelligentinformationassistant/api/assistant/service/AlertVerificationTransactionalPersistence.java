package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class AlertVerificationTransactionalPersistence {

    @Inject
    AlertRepository alertRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TenantContext tenantContext;

    @Inject
    TransactionManager transactionManager;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertVerificationPromptData> doLoadAlertForVerification(String alertId) {
        logBoundary("doLoadAlertForVerification", alertId);
        guardReadyForDbAccess("doLoadAlertForVerification", alertId);
        logQueryBefore("doLoadAlertForVerification", alertId);
        try {
            Optional<AlertVerificationPromptData> promptData = entityManager.createQuery("""
                            select a
                            from Alert a
                            where a.codAlert = :alertId
                            and a.dtDeletedat is null
                            """, Alert.class)
                    .setParameter("alertId", alertId)
                    .getResultStream()
                    .findFirst()
                    .map(this::toDetachedPromptData);
            System.out.println("[IIA][ALERT_VERIFY][TX_QUERY_AFTER]"
                    + " method=doLoadAlertForVerification"
                    + " alertId=" + alertId
                    + " dtoCreated=" + promptData.isPresent());
            logDone("doLoadAlertForVerification", alertId);
            return promptData;
        } catch (RuntimeException ex) {
            logException("doLoadAlertForVerification", alertId, ex);
            throw ex;
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertDetail> doPersistVerificationOutcome(
            String alertId,
            AlertVerificationOutcome outcome,
            boolean enableAfterVerification) {
        logBoundary("doPersistVerificationOutcome", alertId);
        guardReadyForDbAccess("doPersistVerificationOutcome", alertId);
        try {
            AlertVerificationRequest verificationRequest = new AlertVerificationRequest()
                    .force(Boolean.FALSE);
            Optional<AlertDetail> verifiedAlert = alertRepository.verifyAlert(alertId, verificationRequest, outcome);
            Optional<AlertDetail> finalAlert = verifiedAlert;
            if (verifiedAlert.isPresent()
                    && AlertStatus.VERIFIED.equals(verifiedAlert.get().getStatus())
                    && enableAfterVerification) {
                finalAlert = alertRepository.updateAlertEnabledAfterCreateVerification(alertId, true)
                        .or(() -> verifiedAlert);
            }
            logDone("doPersistVerificationOutcome", alertId);
            return finalAlert;
        } catch (RuntimeException ex) {
            logException("doPersistVerificationOutcome", alertId, ex);
            throw ex;
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertDetail> doPersistTechnicalError(String alertId, String shortMessage) {
        logBoundary("doPersistTechnicalError", alertId);
        guardReadyForDbAccess("doPersistTechnicalError", alertId);
        try {
            Optional<AlertDetail> result = alertRepository.markAlertVerificationTechnicalError(
                    alertId,
                    shortMessage,
                    aiConfiguration.provider(),
                    aiConfiguration.alertVerify().model());
            logDone("doPersistTechnicalError", alertId);
            return result;
        } catch (RuntimeException ex) {
            logException("doPersistTechnicalError", alertId, ex);
            throw ex;
        }
    }

    private AlertVerificationPromptData toDetachedPromptData(Alert alert) {
        return new AlertVerificationPromptData(
                alert.getCodAlert(),
                alert.getDscName(),
                alert.getDscDescription(),
                alert.getDscPrompt());
    }

    private void logBoundary(String method, String alertId) {
        System.out.println("[IIA][ALERT_VERIFY][TX_BOUNDARY]"
                + " method=" + method
                + " alertId=" + alertId
                + " requestContextOwner=CALLER_REQUIRED"
                + " requestContextActive=" + requestContextActive()
                + " transactionActive=" + transactionActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " thread=" + Thread.currentThread().getName());
    }

    private void logDone(String method, String alertId) {
        System.out.println("[IIA][ALERT_VERIFY][TX_BOUNDARY_DONE]"
                + " method=" + method
                + " alertId=" + alertId
                + " success=true");
    }

    private void logQueryBefore(String method, String alertId) {
        System.out.println("[IIA][ALERT_VERIFY][TX_QUERY_BEFORE]"
                + " method=" + method
                + " alertId=" + alertId
                + " requestContextOwner=CALLER_REQUIRED"
                + " requestContextActive=" + requestContextActive()
                + " transactionActive=" + transactionActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " thread=" + Thread.currentThread().getName());
    }

    private void guardReadyForDbAccess(String method, String alertId) {
        boolean requestContextActive = requestContextActive();
        boolean tenantPresent = currentTenantId() != null;
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_DB_GUARD]"
                + " phase=transactional-persistence"
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

    private String currentTenantId() {
        if (tenantContext == null) {
            return null;
        }
        String tenantId = tenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? null : tenantId;
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

    private void logException(String method, String alertId, RuntimeException ex) {
        Throwable rootCause = rootCause(ex);
        System.out.println("[IIA][ALERT_VERIFY][TX_BOUNDARY][ERROR]"
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
            System.out.println("[IIA][ALERT_VERIFY][TX_BOUNDARY][ERROR] stack[" + index + "]="
                    + stackTrace[index]);
        }
    }
}
