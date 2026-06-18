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
    void explicitTransportOperatorSuppressesGenericJourneyPhrase() {
        AlertJourneyReferenceIntent intent = detector.detect(
                        "Avvertimi quando una corsa dell'operatore di trasporto ATM e in arrivo a Garibaldi FS")
                .orElseThrow();

        assertThat(intent.kind()).isEqualTo(AlertJourneyReferenceKind.TRANSPORT_OPERATOR);
        assertThat(intent.normalizedValue()).isEqualTo("ATM");
    }

    @Test
    void explicitLineServiceCategoryAndJourneyNameSuppressUnqualifiedDescriptor() {
        AlertJourneyReferenceIntent line = detector
                .detect("Avvertimi quando una corsa della linea M2 e in arrivo a Garibaldi FS")
                .orElseThrow();
        AlertJourneyReferenceIntent category = detector
                .detect("Avvertimi quando una corsa della categoria Intercity e in arrivo")
                .orElseThrow();
        AlertJourneyReferenceIntent journey = detector
                .detect("Avvertimi quando la corsa 125 e in arrivo")
                .orElseThrow();

        assertThat(line.kind()).isEqualTo(AlertJourneyReferenceKind.LINE);
        assertThat(line.normalizedValue()).isEqualTo("M2");
        assertThat(category.kind()).isEqualTo(AlertJourneyReferenceKind.SERVICE_CATEGORY);
        assertThat(category.normalizedValue()).isEqualTo("INTERCITY");
        assertThat(journey.kind()).isEqualTo(AlertJourneyReferenceKind.JOURNEY_NAME);
        assertThat(journey.normalizedValue()).isEqualTo("125");
    }

    @Test
    void genericJourneyPhraseWithoutDescriptorDoesNotCreateConstraint() {
        assertThat(detector.detect("Avvertimi quando una corsa e in arrivo")).isEmpty();
        assertThat(detector.detect("Avvertimi quando una corsa ha piu di 10 minuti di ritardo")).isEmpty();
        assertThat(detector.detect("una corsa con ritardo")).isEmpty();
    }

    @Test
    void englishExplicitOperatorAndUnqualifiedDescriptorAreSeparated() {
        AlertJourneyReferenceIntent operator = detector
                .detect("Notify me when a journey operated by ATM is arriving")
                .orElseThrow();
        AlertJourneyReferenceIntent unqualified = detector
                .detect("Notify me when an M2 journey is arriving")
                .orElseThrow();

        assertThat(operator.kind()).isEqualTo(AlertJourneyReferenceKind.TRANSPORT_OPERATOR);
        assertThat(operator.normalizedValue()).isEqualTo("ATM");
        assertThat(unqualified.kind()).isEqualTo(AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR);
        assertThat(unqualified.normalizedValue()).isEqualTo("M2");
    }

    @Test
    void semanticDescriptorAfterGenericJourneyNounIsAccepted() {
        AlertJourneyReferenceIntent intent = detector
                .detect("Avvertimi quando un treno Intercity e in arrivo")
                .orElseThrow();

        assertThat(intent.kind()).isEqualTo(AlertJourneyReferenceKind.UNQUALIFIED_DESCRIPTOR);
        assertThat(intent.normalizedValue()).isEqualTo("INTERCITY");
    }

    @Test
    void doesNotTreatMovementVerbsAsUnqualifiedDescriptors() {
        assertThat(detector.detect("Avvertimi quando una corsa parte da Rho Fieramilano")).isEmpty();
        assertThat(detector.detect("Avvertimi quando un treno passa da Genova Nervi")).isEmpty();
        assertThat(detector.detect("Notify me when a train departs from a platform with a letter")).isEmpty();
    }
}
