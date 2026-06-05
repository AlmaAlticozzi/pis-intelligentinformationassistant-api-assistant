package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertReplacementHintsExtractorTest {

    private final ScheduledAlertReplacementHintsExtractor extractor = new ScheduledAlertReplacementHintsExtractor();

    @Test
    void extractsGenericReplacementService() {
        ScheduledAlertReplacementHints hints = extractor.extract("Fammi sapere quante corse sostitutive ci sono");

        assertThat(hints.hasReplacementConstraint()).isTrue();
        assertThat(hints.constraints().getFirst().replacementIntent())
                .isEqualTo(ScheduledAlertReplacementConstraint.ReplacementIntent.GENERIC_REPLACEMENT_SERVICE);
    }

    @Test
    void extractsGenericSubstituteServices() {
        ScheduledAlertReplacementHints hints = extractor.extract("servizi sostitutivi a Porto di Mare");

        assertThat(hints.constraints().getFirst().replacementIntent())
                .isEqualTo(ScheduledAlertReplacementConstraint.ReplacementIntent.GENERIC_REPLACEMENT_SERVICE);
    }

    @Test
    void extractsReplacementStop() {
        ScheduledAlertReplacementHints hints = extractor.extract("fermata sostitutiva Bovisa");

        ScheduledAlertReplacementConstraint constraint = hints.constraints().getFirst();
        assertThat(constraint.replacementIntent())
                .isEqualTo(ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP);
        assertThat(constraint.replacementStopRawText()).contains("bovisa");
    }

    @Test
    void extractsReplacementStopWithDepartureType() {
        ScheduledAlertReplacementHints hints = extractor.extract("fermata sostitutiva Bovisa in partenza");

        ScheduledAlertReplacementConstraint constraint = hints.constraints().getFirst();
        assertThat(constraint.replacementIntent())
                .isEqualTo(ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_STOP_WITH_TYPE);
        assertThat(constraint.replacementType())
                .isEqualTo(ScheduledAlertReplacementConstraint.ReplacementType.DEPARTURE);
    }

    @Test
    void extractsReplacementStopWithArrivalType() {
        ScheduledAlertReplacementHints hints = extractor.extract("fermata sostitutiva Bovisa in arrivo");

        assertThat(hints.constraints().getFirst().replacementType())
                .isEqualTo(ScheduledAlertReplacementConstraint.ReplacementType.ARRIVAL);
    }

    @Test
    void extractsExternalReplacementObject() {
        ScheduledAlertReplacementHints hints = extractor.extract("replacement esterno");

        assertThat(hints.constraints().getFirst().replacementIntent())
                .isEqualTo(ScheduledAlertReplacementConstraint.ReplacementIntent.HAS_EXTERNAL_REPLACEMENT_OBJECT);
    }

    @Test
    void extractsUnsupportedReplacementSourceRoute() {
        ScheduledAlertReplacementHints hints = extractor.extract("tratta sostitutiva da Porto di Mare a Milano Affori");

        ScheduledAlertReplacementConstraint constraint = hints.constraints().getFirst();
        assertThat(constraint.replacementIntent())
                .isEqualTo(ScheduledAlertReplacementConstraint.ReplacementIntent.REPLACEMENT_SOURCE_ROUTE);
        assertThat(constraint.unsupportedReason()).contains("not supported");
    }
}
