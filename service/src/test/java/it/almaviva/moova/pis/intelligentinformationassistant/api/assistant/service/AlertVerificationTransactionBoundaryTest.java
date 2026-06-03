package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertVerificationTransactionBoundaryTest {

    @Test
    void verifyAlertServiceMethodThatCallsLlmIsNotTransactional() throws NoSuchMethodException {
        assertThat(AlertService.class
                .getMethod("verifyAlert", String.class, AlertVerificationRequest.class)
                .isAnnotationPresent(Transactional.class))
                .isFalse();
    }
}
