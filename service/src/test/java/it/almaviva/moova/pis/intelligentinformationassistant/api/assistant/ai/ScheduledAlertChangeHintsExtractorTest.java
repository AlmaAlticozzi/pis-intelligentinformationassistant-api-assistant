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
    void doesNotExtractGenericJourneyCancellationAsChange() {
        ScheduledAlertChangeHints hints = extractor.extract("treni cancellati");

        assertThat(hints.hasChangeConstraint()).isFalse();
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

        assertThat(hints.hasChangeConstraint()).isFalse();
    }

    private void assertIntent(String prompt, ScheduledAlertChangeConstraint.ChangeIntent expected) {
        ScheduledAlertChangeHints hints = extractor.extract(prompt);

        assertThat(hints.hasChangeConstraint()).isTrue();
        assertThat(hints.constraints()).extracting(ScheduledAlertChangeConstraint::changeIntent)
                .contains(expected);
    }
}
