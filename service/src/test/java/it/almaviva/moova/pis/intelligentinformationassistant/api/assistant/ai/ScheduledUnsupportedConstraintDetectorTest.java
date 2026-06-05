package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledUnsupportedConstraintDetectorTest {

    private final ScheduledUnsupportedConstraintDetector detector = new ScheduledUnsupportedConstraintDetector();

    @Test
    void detectsWifi() {
        assertThat(detector.detect("Fammi sapere il numero di treni con wifi a bordo e in ritardo in partenza maggiore di 10 min a Portello"))
                .extracting(ScheduledUnsupportedConstraint::concept)
                .contains("wifi");
    }

    @Test
    void detectsCarriages() {
        assertThat(detector.detect("Fammi sapere il numero di treni con più di 10 carrozze a Gerusalemme"))
                .extracting(ScheduledUnsupportedConstraint::concept)
                .contains("carriages/composition");
    }

    @Test
    void detectsCrowding() {
        assertThat(detector.detect("Fammi sapere se a Garibaldi FS ci sono treni affollati"))
                .extracting(ScheduledUnsupportedConstraint::concept)
                .contains("passenger occupancy");
    }

    @Test
    void detectsWeather() {
        assertThat(detector.detect("Fammi sapere se a Garibaldi FS piove"))
                .extracting(ScheduledUnsupportedConstraint::concept)
                .contains("weather");
    }

    @Test
    void detectsAbsenceOverDuration() {
        assertThat(detector.detect("Fammi sapere se per 30 minuti non passa nessun treno a Garibaldi FS"))
                .extracting(ScheduledUnsupportedConstraint::concept)
                .contains("absence over duration");
    }

    @Test
    void allowsValidDelayPrompt() {
        assertThat(detector.detect("Fammi sapere quanti treni in ritardo a Garibaldi FS")).isEmpty();
    }
}
