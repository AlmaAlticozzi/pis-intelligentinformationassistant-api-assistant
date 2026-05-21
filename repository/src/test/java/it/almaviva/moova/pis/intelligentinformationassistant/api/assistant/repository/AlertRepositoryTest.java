package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreter;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.view.AlertSummaryView;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertRepositoryTest {

    @Test
    void rejectedImmediateVerificationDoesNotExposeInterpreterContract() throws Exception {
        AlertRepository repository = new AlertRepository();
        Alert alert = alertWithExistingInterpreterMetadata();

        repository.applyVerifiedInterpreterMetadata(alert, rejectedOutcome());

        assertThat(alert.getSglInterpretertype()).isNull();
        assertThat(alert.getDscInputmodel()).isNull();
        assertThat(alert.getDscOutputmodel()).isNull();
        assertThat(alert.getDscImplementationsummary()).isNull();
        assertThat(alert.getDscInterpreterclassname()).isNull();
        assertThat(alert.getDscContractversion()).isNull();
        assertThat(alert.getCodCoderef()).isNull();
        assertThat(alert.getJsnTechnicalspecification()).isNull();
        assertThat(alert.getJsnAgentblueprintpreview()).isNull();
        assertThat(toAlertInterpreter(repository, alert)).isNull();
    }

    @Test
    void verifiedImmediateVerificationExposesServiceDataInterpreter() throws Exception {
        AlertRepository repository = new AlertRepository();
        repository.entityManager = mock(EntityManager.class);

        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType interpreterType =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType();
        interpreterType.setSglInterpretertype("EVENT_INTERPRETER");
        when(repository.entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType.class,
                "EVENT_INTERPRETER"))
                .thenReturn(interpreterType);

        Alert alert = new Alert();
        repository.applyVerifiedInterpreterMetadata(alert, verifiedOutcome());

        AlertInterpreter interpreter = toAlertInterpreter(repository, alert);

        assertThat(interpreter).isNotNull();
        assertThat(interpreter.getInterpreterType())
                .isEqualTo(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType.EVENT_INTERPRETER);
        assertThat(interpreter.getInputModel()).isEqualTo("ServiceDataV2");
        assertThat(interpreter.getOutputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
    }

    @Test
    void alertSummaryIncludesLastVerification() throws Exception {
        AlertRepository repository = new AlertRepository();
        AlertSummaryView view = mock(AlertSummaryView.class);
        OffsetDateTime verifiedAt = OffsetDateTime.parse("2026-05-21T10:15:30Z");
        when(view.getId()).thenReturn("ALRT1");
        when(view.getName()).thenReturn("Rejected weather alert");
        when(view.getEnabled()).thenReturn(false);
        when(view.getStatus()).thenReturn("REJECTED");
        when(view.getVerificationStatus()).thenReturn("REJECTED");
        when(view.getRejectedReason()).thenReturn("The request is outside the supported PIS domain.");
        when(view.getConfidence()).thenReturn(BigDecimal.valueOf(0.0));
        when(view.getVerifiedAt()).thenReturn(verifiedAt);

        AlertSummary summary = toAlertSummary(repository, view);

        assertThat(summary.getLastVerification()).isNotNull();
        assertThat(summary.getLastVerification().getStatus())
                .isEqualTo(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus.REJECTED);
        assertThat(summary.getLastVerification().getRejectedReason())
                .isEqualTo("The request is outside the supported PIS domain.");
        assertThat(summary.getLastVerification().getConfidence()).isEqualTo(0.0);
        assertThat(summary.getLastVerification().getVerifiedAt()).isEqualTo(verifiedAt);
    }

    private Alert alertWithExistingInterpreterMetadata() {
        Alert alert = new Alert();
        it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType interpreterType =
                new it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType();
        interpreterType.setSglInterpretertype("EVENT_INTERPRETER");
        alert.setSglInterpretertype(interpreterType);
        alert.setDscInputmodel("ServiceDataV2");
        alert.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");
        alert.setDscImplementationsummary("Existing interpreter.");
        alert.setDscInterpreterclassname("ExistingInterpreter");
        alert.setDscContractversion("1");
        alert.setCodCoderef("artifact://existing");
        alert.setJsnTechnicalspecification(Map.of("source", "SERVICE_DATA"));
        alert.setJsnAgentblueprintpreview(Map.of("agentName", "ExistingAgent"));
        return alert;
    }

    private AlertVerificationOutcome rejectedOutcome() {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Rejected.",
                "The request is outside the supported PIS domain.",
                0.0,
                "mock",
                "mock-alert-verify",
                "alert-verify-mvp-v1",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                Map.of("decision", "REJECTED"),
                Map.of("canGenerate", false),
                List.of("The request is outside the supported PIS domain."),
                List.of("No Agent Definition created."));
    }

    private AlertVerificationOutcome verifiedOutcome() {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "Verified.",
                null,
                0.86,
                "OPENAI",
                "gpt-4.1-mini",
                "alert-verify-mvp-v1",
                List.of("SERVICE_DATA"),
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "EVENT",
                "STATELESS_EVENT_MATCH",
                List.of("JOURNEY_CANCELLED"),
                List.of("SERVICE_DATA_JOURNEY"),
                Map.of("source", "SERVICE_DATA"),
                Map.of("agentName", "CancelledJourneyServiceDataAgent"),
                List.of(),
                List.of("No Agent Definition created."));
    }

    private AlertInterpreter toAlertInterpreter(AlertRepository repository, Alert alert) throws Exception {
        Method method = AlertRepository.class.getDeclaredMethod("toAlertInterpreter", Alert.class);
        method.setAccessible(true);
        return (AlertInterpreter) method.invoke(repository, alert);
    }

    private AlertSummary toAlertSummary(AlertRepository repository, AlertSummaryView view) throws Exception {
        Method method = AlertRepository.class.getDeclaredMethod("toAlertSummary", AlertSummaryView.class);
        method.setAccessible(true);
        return (AlertSummary) method.invoke(repository, view);
    }
}
