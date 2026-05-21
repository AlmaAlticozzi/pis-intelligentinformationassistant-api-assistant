package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import io.quarkus.narayana.jta.QuarkusTransaction;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
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

    @ActivateRequestContext
    public void verifyCreatedAlertAsync(String alertId, boolean enableAfterVerification) {
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
            markCreatedAlertVerificationError(alertId, shortMessage);
            System.out.println("[IIA][ALERT_CREATE] finalStatus=ERROR enabled=false");
        }
    }

    @ActivateRequestContext
    public void markCreatedAlertVerificationError(String alertId, String shortMessage) {
        QuarkusTransaction.requiringNew().run(() -> alertRepository.markAlertVerificationTechnicalError(
                alertId,
                shortMessage,
                aiConfiguration.provider(),
                aiConfiguration.alertVerify().model()));
    }

    private AlertDetail verifyAndApplyEnableInNewTransaction(String alertId, boolean enableAfterVerification) {
        return QuarkusTransaction.requiringNew().call(() -> {
            System.out.println("[IIA][ALERT_VERIFY][ASYNC_CONTEXT] before LLM requestContextActive="
                    + io.quarkus.arc.Arc.container().requestContext().isActive());
            AlertVerificationRequest verificationRequest = new AlertVerificationRequest()
                    .force(Boolean.FALSE);
            AlertDetail verifiedAlert = alertService.verifyAlert(alertId, verificationRequest)
                    .orElseThrow(() -> new IllegalStateException("Created alert not found during async verification."));
            AlertStatus status = verifiedAlert.getStatus();
            boolean enabled = AlertStatus.VERIFIED.equals(status) && enableAfterVerification;
            return alertRepository.updateAlertEnabledAfterCreateVerification(alertId, enabled)
                    .orElse(verifiedAlert);
        });
    }

    private String shortTechnicalMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if ((message == null || message.isBlank()) && throwable.getCause() != null) {
            message = throwable.getCause().getMessage();
        }
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
