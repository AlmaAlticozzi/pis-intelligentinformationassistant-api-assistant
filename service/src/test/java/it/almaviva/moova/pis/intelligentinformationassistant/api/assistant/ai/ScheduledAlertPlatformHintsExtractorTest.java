package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertPlatformHintsExtractorTest {

    private final ScheduledAlertPlatformHintsExtractor extractor = new ScheduledAlertPlatformHintsExtractor();

    @Test
    void extractsDeparturePlatformEquals() {
        ScheduledAlertPlatformConstraint constraint = first("partono dal binario 3");

        assertThat(constraint.direction()).isEqualTo(ScheduledAlertPlatformConstraint.Direction.DEPARTURE);
        assertThat(constraint.platformIntent()).isEqualTo(ScheduledAlertPlatformConstraint.PlatformIntent.EQUALS);
        assertThat(constraint.platformValue()).isEqualTo("3");
    }

    @Test
    void extractsArrivalPlatformEquals() {
        ScheduledAlertPlatformConstraint constraint = first("arrivano al binario 1");

        assertThat(constraint.direction()).isEqualTo(ScheduledAlertPlatformConstraint.Direction.ARRIVAL);
        assertThat(constraint.platformIntent()).isEqualTo(ScheduledAlertPlatformConstraint.PlatformIntent.EQUALS);
        assertThat(constraint.platformValue()).isEqualTo("1");
    }

    @Test
    void extractsGreaterThan() {
        ScheduledAlertPlatformConstraint constraint = first("binario maggiore di 2");

        assertThat(constraint.platformIntent()).isEqualTo(ScheduledAlertPlatformConstraint.PlatformIntent.GREATER_THAN);
        assertThat(constraint.numericValue()).isEqualTo(2);
    }

    @Test
    void extractsBetween() {
        ScheduledAlertPlatformConstraint constraint = first("binario tra 2 e 5");

        assertThat(constraint.platformIntent()).isEqualTo(ScheduledAlertPlatformConstraint.PlatformIntent.BETWEEN);
        assertThat(constraint.minValue()).isEqualTo(2);
        assertThat(constraint.maxValue()).isEqualTo(5);
    }

    @Test
    void extractsEven() {
        assertThat(first("binario pari").platformIntent())
                .isEqualTo(ScheduledAlertPlatformConstraint.PlatformIntent.EVEN);
    }

    @Test
    void extractsOdd() {
        assertThat(first("binario dispari").platformIntent())
                .isEqualTo(ScheduledAlertPlatformConstraint.PlatformIntent.ODD);
    }

    @Test
    void extractsGenericPlatformChange() {
        ScheduledAlertPlatformConstraint constraint = first("cambio di binario");

        assertThat(constraint.platformIntent()).isEqualTo(ScheduledAlertPlatformConstraint.PlatformIntent.CHANGED);
        assertThat(constraint.direction()).isEqualTo(ScheduledAlertPlatformConstraint.Direction.UNSPECIFIED);
    }

    @Test
    void extractsDeparturePlatformChange() {
        ScheduledAlertPlatformConstraint constraint = first("cambio binario in partenza");

        assertThat(constraint.platformIntent()).isEqualTo(ScheduledAlertPlatformConstraint.PlatformIntent.CHANGED);
        assertThat(constraint.direction()).isEqualTo(ScheduledAlertPlatformConstraint.Direction.DEPARTURE);
    }

    private ScheduledAlertPlatformConstraint first(String prompt) {
        ScheduledAlertPlatformHints hints = extractor.extract(prompt);
        assertThat(hints.hasPlatformConstraint()).isTrue();
        assertThat(hints.constraints()).isNotEmpty();
        return hints.constraints().getFirst();
    }
}
