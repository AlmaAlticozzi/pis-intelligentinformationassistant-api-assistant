package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertVerificationMockEngineTest {

    private final AlertVerificationMockEngine engine = new AlertVerificationMockEngine();

    @Test
    void manualVerifyAcceptsValidCancelledJourneyPrompt() {
        AlertVerificationOutcome outcome = engine.verify(
                "ALRT1",
                "Create a suggestion when a journey is cancelled at Milano Malpensa T1.");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.interpreterType()).isEqualTo("EVENT_INTERPRETER");
        assertThat(outcome.inputModel()).isEqualTo("ServiceDataV2");
        assertThat(outcome.outputModel()).isEqualTo("AgentOutput.CANDIDATE_SUGGESTION");
        assertThat(outcome.interpretedEventNames()).contains("JOURNEY_CANCELLED");
    }

    @Test
    void manualVerifyRejectsGaribaldiPrompt() {
        AlertVerificationOutcome outcome = engine.verify("ALRT1", "Chi e Garibaldi su Wikipedia?");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).isEqualTo("The request is not an operational PIS alert.");
    }

    @Test
    void manualVerifyRejectsWeatherPrompt() {
        AlertVerificationOutcome outcome = engine.verify("ALRT1", "Avvisami quando piove");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).isEqualTo("The request is outside the supported PIS domain.");
    }

    @Test
    void manualVerifyRejectsVideoOrDevicePrompt() {
        AlertVerificationOutcome outcome = engine.verify("ALRT1", "Avvisami quando un display del dispositivo e spento");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason())
                .isEqualTo("The request belongs to the PIS domain but requires a data source not available in the current MVP.");
    }

    @Test
    void manualVerifyRejectsNoJourneysPrompt() {
        AlertVerificationOutcome outcome = engine.verify("ALRT1", "Avvisami quando non ci sono corse in partenza");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason())
                .isEqualTo("The request requires stateful or time-window evaluation, which is not supported by the current stateless ServiceData interpreter.");
    }

    @Test
    void manualVerifyAcceptsTransitPrompt() {
        AlertVerificationOutcome outcome = engine.verify(
                "ALRT1",
                "Avvisami quando un treno e in transito a Genova P.P. (non si ferma)");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.interpretedEventNames()).contains("JOURNEY_TRANSIT");
        assertThat(outcome.technicalSpecification().toString()).contains("passingType", "TRANSIT");
    }

    @Test
    void manualVerifyAcceptsReplacementPrompt() {
        AlertVerificationOutcome outcome = engine.verify(
                "ALRT1",
                "Avvisami quando una corsa ha un replacement valorizzato");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.interpretedEventNames()).contains("JOURNEY_REPLACEMENT");
        assertThat(outcome.technicalSpecification().toString()).contains("replacement", "EXISTS");
    }

    @Test
    void manualVerifyAcceptsOriginWithAtLeastTwoTransitsPrompt() {
        AlertVerificationOutcome outcome = engine.verify(
                "ALRT1",
                "Avvisami quando una corsa parte dall'origine e fara almeno due transiti");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString()).contains("passingType", "ORIGIN", "nextTransitCalls", "SIZE_GREATER_OR_EQUAL");
    }

    @Test
    void manualVerifyAcceptsRouteAndPlatformPrompt() {
        AlertVerificationOutcome outcome = engine.verify(
                "ALRT1",
                "Avvisami quando una corsa e in partenza da Firenze dal binario 1 e passa da Siena");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString()).contains("Firenze", "Siena", "actualDeparturePlatform", "1");
    }

    @Test
    void manualVerifyRejectsUnsupportedPassengerCountPrompt() {
        AlertVerificationOutcome outcome = engine.verify(
                "ALRT1",
                "Avvisami quando il treno 1253 parte da Genova e ha almeno 10 passeggeri");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("ServiceData capability catalog");
    }

    @Test
    void manualVerifyRejectsUnsupportedTrainColorPrompt() {
        AlertVerificationOutcome outcome = engine.verify(
                "ALRT1",
                "Avvisami quando il treno 1253 parte da Genova ed e di colore rosso");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("ServiceData capability catalog");
    }

    @Test
    void manualVerifyRejectsUnsupportedWifiPrompt() {
        AlertVerificationOutcome outcome = engine.verify(
                "ALRT1",
                "Avvisami quando il treno 1253 parte da Genova e ha il Wi-Fi");

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason()).contains("ServiceData capability catalog");
    }
}
