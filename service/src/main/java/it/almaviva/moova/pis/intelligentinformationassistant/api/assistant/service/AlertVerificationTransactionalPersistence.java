package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
    AiConfiguration aiConfiguration;

    @Inject
    TenantContext tenantContext;

    @Inject
    TransactionManager transactionManager;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertVerificationPromptData> doLoadAlertForVerification(String alertId) {
        logBoundary("doLoadAlertForVerification", alertId);
        Optional<AlertVerificationPromptData> promptData = alertRepository.getAlertVerificationPromptData(alertId)
                .map(this::detachedCopy);
        logDone("doLoadAlertForVerification", alertId);
        return promptData;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertDetail> doPersistVerificationOutcome(
            String alertId,
            AlertVerificationOutcome outcome,
            boolean enableAfterVerification) {
        logBoundary("doPersistVerificationOutcome", alertId);
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
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertDetail> doPersistTechnicalError(String alertId, String shortMessage) {
        logBoundary("doPersistTechnicalError", alertId);
        Optional<AlertDetail> result = alertRepository.markAlertVerificationTechnicalError(
                alertId,
                shortMessage,
                aiConfiguration.provider(),
                aiConfiguration.alertVerify().model());
        logDone("doPersistTechnicalError", alertId);
        return result;
    }

    private AlertVerificationPromptData detachedCopy(AlertVerificationPromptData promptData) {
        return new AlertVerificationPromptData(
                promptData.alertId(),
                promptData.name(),
                promptData.description(),
                promptData.prompt(),
                promptData.locationResolutionContext());
    }

    private void logBoundary(String method, String alertId) {
        System.out.println("[IIA][ALERT_VERIFY][TX_BOUNDARY]"
                + " method=" + method
                + " alertId=" + alertId
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
}
