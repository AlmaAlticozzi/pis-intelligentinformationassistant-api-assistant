package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertJourneyCancellationHintsExtractorTest {

    private final ScheduledAlertJourneyCancellationHintsExtractor extractor = new ScheduledAlertJourneyCancellationHintsExtractor();

    @Test
    void extractsGenericSuppressedJourneys() {
        ScheduledAlertJourneyCancellationHints hints = extractor.extract("Avvertimi ogni 10 min su quante corse soppresse ci sono a Lecco");

        assertThat(hints.hasJourneyCancellationConstraint()).isTrue();
        assertThat(hints.constraints()).extracting(ScheduledAlertJourneyCancellationConstraint::cancellationIntent)
                .contains(ScheduledAlertJourneyCancellationConstraint.CancellationIntent.GENERIC_JOURNEY_CANCELLATION);
    }

    @Test
    void extractsGenericCancelledJourneysEnglish() {
        ScheduledAlertJourneyCancellationHints hints = extractor.extract("Tell me every 10 minutes how many cancelled journeys are at Lecco");

        assertThat(hints.hasJourneyCancellationConstraint()).isTrue();
        assertThat(hints.constraints()).extracting(ScheduledAlertJourneyCancellationConstraint::cancellationIntent)
                .contains(ScheduledAlertJourneyCancellationConstraint.CancellationIntent.GENERIC_JOURNEY_CANCELLATION);
    }

    @Test
    void extractsArrivalOnlyCancellation() {
        ScheduledAlertJourneyCancellationHints hints = extractor.extract("corse con soppressione in arrivo");

        assertThat(hints.hasJourneyCancellationConstraint()).isTrue();
        assertThat(hints.constraints()).anySatisfy(constraint -> {
            assertThat(constraint.cancellationIntent()).isEqualTo(ScheduledAlertJourneyCancellationConstraint.CancellationIntent.ARRIVAL_ONLY_CANCELLATION);
            assertThat(constraint.direction()).isEqualTo(ScheduledAlertJourneyCancellationConstraint.Direction.ARRIVAL);
        });
    }

    @Test
    void extractsDepartureOnlyCancellation() {
        ScheduledAlertJourneyCancellationHints hints = extractor.extract("corse con soppressione in partenza");

        assertThat(hints.hasJourneyCancellationConstraint()).isTrue();
        assertThat(hints.constraints()).anySatisfy(constraint -> {
            assertThat(constraint.cancellationIntent()).isEqualTo(ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_ONLY_CANCELLATION);
            assertThat(constraint.direction()).isEqualTo(ScheduledAlertJourneyCancellationConstraint.Direction.DEPARTURE);
        });
    }

    @Test
    void doesNotTreatCancelledStopsAsJourneyCancellation() {
        ScheduledAlertJourneyCancellationHints hints = extractor.extract("treni con fermate soppresse");

        assertThat(hints.hasJourneyCancellationConstraint()).isFalse();
    }
}
