package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertEventWordingClassifierTest {

    private final AlertEventWordingClassifier classifier = new AlertEventWordingClassifier();

    @Test
    void classifiesProgressiveDeparture() {
        AlertEventPhase phase = classifier.classify(
                "Avvertimi quando una corsa e in partenza da Garibaldi dal binario 1",
                AlertLocationMainEventIntent.DEPARTURE);

        assertThat(phase).isEqualTo(AlertEventPhase.PROGRESSIVE);
        assertThat(classifier.expectedEventType(AlertLocationMainEventIntent.DEPARTURE, phase))
                .isEqualTo("DEPARTING");
    }

    @Test
    void classifiesCompletedDeparture() {
        AlertEventPhase phase = classifier.classify(
                "Avvertimi quando una corsa parte da Garibaldi",
                AlertLocationMainEventIntent.DEPARTURE);

        assertThat(phase).isEqualTo(AlertEventPhase.COMPLETED);
        assertThat(classifier.expectedEventType(AlertLocationMainEventIntent.DEPARTURE, phase))
                .isEqualTo("DEPARTED");
    }
}
