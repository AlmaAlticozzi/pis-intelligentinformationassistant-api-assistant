package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertCancelledCallHintsExtractorTest {

    private final ScheduledAlertCancelledCallHintsExtractor extractor = new ScheduledAlertCancelledCallHintsExtractor();

    @Test
    void extractsSpecificSuppressedStop() {
        ScheduledAlertCancelledCallHints hints = extractor.extract("fermata soppressa Palazzolo Milanese");

        assertThat(hints.hasCancelledCallConstraint()).isTrue();
        assertThat(hints.constraints().get(0).hasSpecificCancelledStop()).isTrue();
        assertThat(hints.constraints().get(0).cancelledStopRawText()).contains("palazzolo");
        assertThat(hints.constraints().get(0).genericAnyCancelledCallRequested()).isFalse();
    }

    @Test
    void extractsSkippedStop() {
        ScheduledAlertCancelledCallHints hints = extractor.extract("Per Garibaldi FS dimmi quante corse saltano la fermata Bovisa");

        assertThat(hints.hasCancelledCallConstraint()).isTrue();
        assertThat(hints.constraints().get(0).hasSpecificCancelledStop()).isTrue();
    }

    @Test
    void extractsGenericSuppressedStops() {
        ScheduledAlertCancelledCallHints hints = extractor.extract("treni con fermate soppresse");

        assertThat(hints.hasCancelledCallConstraint()).isTrue();
        assertThat(hints.genericAnyCancelledCallRequested()).isTrue();
    }

    @Test
    void extractsNegativePolarity() {
        ScheduledAlertCancelledCallHints hints = extractor.extract("senza fermate soppresse");

        assertThat(hints.hasCancelledCallConstraint()).isTrue();
        assertThat(hints.constraints().get(0).polarity())
                .isEqualTo(ScheduledAlertCancelledCallConstraint.Polarity.EXCLUDE);
    }
}
