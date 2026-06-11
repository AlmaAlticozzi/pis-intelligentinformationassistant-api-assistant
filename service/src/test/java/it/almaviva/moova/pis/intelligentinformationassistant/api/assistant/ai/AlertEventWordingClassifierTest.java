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

    @Test
    void explicitMainEventKeepsArrivingBeforeDepartureCancellationAccessory() {
        AlertEventWordingClassifier.MainEventWording wording = classifier.classifyExplicitMainEvent(
                "Avvertimi quando a Bignami c'e un treno in arrivo con una soppressione in partenza");

        assertThat(wording.intent()).isEqualTo(AlertLocationMainEventIntent.ARRIVAL);
        assertThat(wording.phase()).isEqualTo(AlertEventPhase.PROGRESSIVE);
        assertThat(classifier.expectedEventType(wording.intent(), wording.phase())).isEqualTo("ARRIVING");
        assertThat(wording.accessoryStatePhrase()).contains("soppressione in partenza");
    }

    @Test
    void explicitMainEventKeepsDepartingBeforeArrivalCancellationAccessory() {
        AlertEventWordingClassifier.MainEventWording wording = classifier.classifyExplicitMainEvent(
                "Avvertimi quando a Bignami c'e un treno in partenza con una soppressione in arrivo");

        assertThat(wording.intent()).isEqualTo(AlertLocationMainEventIntent.DEPARTURE);
        assertThat(wording.phase()).isEqualTo(AlertEventPhase.PROGRESSIVE);
        assertThat(classifier.expectedEventType(wording.intent(), wording.phase())).isEqualTo("DEPARTING");
        assertThat(wording.accessoryStatePhrase()).contains("soppressione in arrivo");
    }

    @Test
    void cancellationStateAloneIsNotExplicitMovementMainEvent() {
        assertThat(classifier.classifyExplicitMainEvent(
                "Avvertimi quando a Bignami c'e una soppressione in partenza"))
                .isNull();
    }
}
