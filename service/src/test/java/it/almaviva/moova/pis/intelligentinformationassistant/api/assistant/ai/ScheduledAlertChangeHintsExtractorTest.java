package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertChangeHintsExtractorTest {

    private final ScheduledAlertChangeHintsExtractor extractor = new ScheduledAlertChangeHintsExtractor();

    @Test
    void extractsChangedOrigin() {
        assertIntent("cambio origine", ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_ORIGIN);
    }

    @Test
    void extractsChangedDestination() {
        assertIntent("cambio destinazione", ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_DESTINATION);
    }

    @Test
    void extractsChangedPath() {
        assertIntent("cambio percorso", ScheduledAlertChangeConstraint.ChangeIntent.CHANGED_PATH);
    }

    @Test
    void extractsExtraJourney() {
        assertIntent("corsa straordinaria", ScheduledAlertChangeConstraint.ChangeIntent.EXTRA_JOURNEY);
    }

    @Test
    void extractsGenericCancellation() {
        assertIntent("treni cancellati", ScheduledAlertChangeConstraint.ChangeIntent.GENERIC_CANCELLATION);
    }

    @Test
    void extractsPartialCancellation() {
        assertIntent("cancellazione parziale", ScheduledAlertChangeConstraint.ChangeIntent.PARTIAL_CANCELLATION);
    }

    @Test
    void extractsArrivalCancellation() {
        ScheduledAlertChangeHints hints = extractor.extract("soppressione in arrivo");

        assertThat(hints.constraints()).extracting(ScheduledAlertChangeConstraint::changeIntent)
                .contains(ScheduledAlertChangeConstraint.ChangeIntent.ARRIVAL_CANCELLATION);
        assertThat(hints.constraints().get(0).direction()).isEqualTo(ScheduledAlertChangeConstraint.Direction.ARRIVAL);
    }

    @Test
    void extractsDepartureCancellation() {
        ScheduledAlertChangeHints hints = extractor.extract("soppressione in partenza");

        assertThat(hints.constraints()).extracting(ScheduledAlertChangeConstraint::changeIntent)
                .contains(ScheduledAlertChangeConstraint.ChangeIntent.DEPARTURE_CANCELLATION);
        assertThat(hints.constraints().get(0).direction()).isEqualTo(ScheduledAlertChangeConstraint.Direction.DEPARTURE);
    }

    @Test
    void extractsTotalExclusion() {
        assertIntent("total exclusion", ScheduledAlertChangeConstraint.ChangeIntent.TOTAL_EXCLUSION);
    }

    @Test
    void extractsTimeBasedExclusion() {
        assertIntent("time based exclusion", ScheduledAlertChangeConstraint.ChangeIntent.TIME_BASED_EXCLUSION);
    }

    @Test
    void extractsNegativeCancellationPolarity() {
        ScheduledAlertChangeHints hints = extractor.extract("non cancellati");

        assertThat(hints.hasChangeConstraint()).isTrue();
        assertThat(hints.constraints()).anySatisfy(constraint -> {
            assertThat(constraint.changeIntent()).isEqualTo(ScheduledAlertChangeConstraint.ChangeIntent.GENERIC_CANCELLATION);
            assertThat(constraint.polarity()).isEqualTo(ScheduledAlertChangeConstraint.Polarity.EXCLUDE);
        });
    }

    private void assertIntent(String prompt, ScheduledAlertChangeConstraint.ChangeIntent expected) {
        ScheduledAlertChangeHints hints = extractor.extract(prompt);

        assertThat(hints.hasChangeConstraint()).isTrue();
        assertThat(hints.constraints()).extracting(ScheduledAlertChangeConstraint::changeIntent)
                .contains(expected);
    }
}
