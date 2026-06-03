package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ApplicationScoped
public class AlertVerificationPersistenceGateway {

    @Inject
    AlertRepository alertRepository;

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TenantContext tenantContext;

    @Inject
    TransactionManager transactionManager;

    @ConfigProperty(name = "fnd.default-schema", defaultValue = "")
    String defaultSchema;

    @ActivateRequestContext
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertVerificationPromptData> loadAlertForVerification(String alertId, String tenant) {
        installTenant(alertId, tenant, "loadAlertForVerification");
        logBoundary("loadAlertForVerification", alertId);
        return alertRepository.getAlertVerificationPromptData(alertId);
    }

    @ActivateRequestContext
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertDetail> persistVerificationOutcome(
            String alertId,
            AlertVerificationRequest request,
            AlertVerificationOutcome outcome,
            String tenant) {
        installTenant(alertId, tenant, "persistVerificationOutcome");
        logBoundary("persistVerificationOutcome", alertId);
        return alertRepository.verifyAlert(alertId, request, outcome);
    }

    @ActivateRequestContext
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertDetail> updateAlertEnabledAfterCreateVerification(String alertId, String tenant) {
        installTenant(alertId, tenant, "updateAlertEnabledAfterCreateVerification");
        logBoundary("updateAlertEnabledAfterCreateVerification", alertId);
        return alertRepository.updateAlertEnabledAfterCreateVerification(alertId, true);
    }

    @ActivateRequestContext
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Optional<AlertDetail> persistTechnicalError(String alertId, String shortMessage, String tenant) {
        installTenant(alertId, tenant, "persistTechnicalError");
        logBoundary("persistTechnicalError", alertId);
        return alertRepository.markAlertVerificationTechnicalError(
                alertId,
                shortMessage,
                aiConfiguration.provider(),
                aiConfiguration.alertVerify().model());
    }

    private void installTenant(String alertId, String tenant, String method) {
        String tenantToInstall = normalize(tenant);
        if (tenantToInstall == null) {
            tenantToInstall = normalizedDefaultSchema();
        }
        if (tenantToInstall != null && tenantContext != null) {
            tenantContext.setTenantId(tenantToInstall);
        }
        if (currentTenantId() == null) {
            throw new AlertAsyncVerificationService.MissingTenantContextException(
                    "Alert verification DB boundary " + method + " cannot run because tenant context is missing. alertId=" + alertId);
        }
    }

    private void logBoundary(String method, String alertId) {
        System.out.println("[IIA][ALERT_VERIFY][DB_BOUNDARY]"
                + " method=" + method
                + " alertId=" + alertId
                + " requestContextActive=" + requestContextActive()
                + " transactionActive=" + transactionActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " thread=" + Thread.currentThread().getName());
    }

    private String currentTenantId() {
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
}
