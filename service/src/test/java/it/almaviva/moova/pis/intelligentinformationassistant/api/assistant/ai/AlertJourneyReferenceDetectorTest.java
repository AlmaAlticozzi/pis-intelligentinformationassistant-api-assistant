package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertJourneyReferenceDetectorTest {

    private final AlertJourneyReferenceDetector detector = new AlertJourneyReferenceDetector();

    @Test
    void classifiesUnqualifiedDescriptorAttachedToJourney() {
        AlertJourneyReferenceIntent intent = detector
                .detect("Avvertimi quando una corsa M2 e in arrivo a Garibaldi FS")
                .orElseThrow();

        assertThat(intent.kind()).isEqualTo(AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR);
        assertThat(intent.normalizedValue()).isEqualTo("M2");
    }

    @Test
    void classifiesExplicitSemanticQualifiers() {
        assertThat(detector.detect("la corsa 125 e in arrivo").orElseThrow().kind())
                .isEqualTo(AlertJourneyReferenceKind.JOURNEY_NAME);
        assertThat(detector.detect("una corsa della linea M2 e in arrivo").orElseThrow().kind())
                .isEqualTo(AlertJourneyReferenceKind.LINE);
        assertThat(detector.detect("service category Intercity").orElseThrow().kind())
                .isEqualTo(AlertJourneyReferenceKind.SERVICE_CATEGORY);
        assertThat(detector.detect("operated by ATM").orElseThrow().kind())
                .isEqualTo(AlertJourneyReferenceKind.TRANSPORT_OPERATOR);
    }

    @Test
    void doesNotTreatMovementVerbsAsUnqualifiedDescriptors() {
        assertThat(detector.detect("Avvertimi quando una corsa parte da Rho Fieramilano")).isEmpty();
        assertThat(detector.detect("Avvertimi quando un treno passa da Genova Nervi")).isEmpty();
        assertThat(detector.detect("Notify me when a train departs from a platform with a letter")).isEmpty();
    }
}
