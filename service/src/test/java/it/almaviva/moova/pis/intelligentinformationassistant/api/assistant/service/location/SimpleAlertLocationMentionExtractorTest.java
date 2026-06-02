package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleAlertLocationMentionExtractorTest {

    private static final String RHO_FIERAMILANO_ID = "TNPNTS00000000005467";
    private static final String MALPENSA_T1_ID = "TNPNTS00000000000028";
    private static final String MALPENSA_T2_ID = "TNPNTS00000000000029";

    private final SimpleAlertLocationMentionExtractor extractor = new SimpleAlertLocationMentionExtractor();
    private final AlertLocationResolverService resolverService = new AlertLocationResolverService(
            new PointResolver(new PointRegistry(), new PointNormalizer()));

    @Test
    void extractDepartureLocationFromRhoPrompt() {
        AlertLocationExtractionResult result = extractor.extract("Avvertimi quando una corsa parte da Rho Fieramilano");

        assertThat(result.hasLocationMentions()).isTrue();
        assertThat(result.mentions()).singleElement()
                .extracting(AlertLocationMention::rawText, AlertLocationMention::semanticRole)
                .containsExactly("Rho Fieramilano", AlertLocationSemanticRole.DEPARTURE_EVENT_STOP_POINT);
    }

    @Test
    void extractArrivalLocation() {
        AlertLocationExtractionResult result = extractor.extract("Avvertimi quando una corsa arriva a Sesto 1 Maggio FS");

        assertThat(result.hasLocationMentions()).isTrue();
        assertThat(result.mentions()).singleElement()
                .extracting(AlertLocationMention::semanticRole)
                .isEqualTo(AlertLocationSemanticRole.ARRIVAL_EVENT_STOP_POINT);
    }

    @Test
    void extractTransitLocation() {
        AlertLocationExtractionResult result = extractor.extract("Avvertimi quando una corsa transiterà a Gorla");

        assertThat(result.hasLocationMentions()).isTrue();
        assertThat(result.mentions()).singleElement()
                .extracting(AlertLocationMention::rawText, AlertLocationMention::semanticRole)
                .containsExactly("Gorla", AlertLocationSemanticRole.NEXT_CALL_STOP_POINT);
    }

    @Test
    void noLocationPromptDoesNotFail() {
        AlertLocationExtractionResult result = extractor.extract("Dimmi quando una metro è in ritardo di oltre 10 min");

        assertThat(result.hasLocationMentions()).isFalse();
        assertThat(result.mentions()).isEmpty();
    }

    @Test
    void resolveExtractedRhoMentionToExpectedPointId() {
        AlertLocationExtractionResult extraction = extractor.extract("Avvertimi quando una corsa parte da Rho Fieramilano");
        List<AlertLocationResolution> resolutions = resolverService.resolve(extraction.mentions());

        assertThat(resolutions).singleElement()
                .satisfies(resolution -> {
                    assertThat(resolution.pointResolutionResult().status()).isEqualTo(PointResolutionStatus.RESOLVED);
                    assertThat(resolution.selectedPointIds()).containsExactly(RHO_FIERAMILANO_ID);
                    assertThat(resolution.fallbackToNameLong()).isFalse();
                });
    }

    @Test
    void resolveMalpensaMentionAsAmbiguous() {
        AlertLocationExtractionResult extraction = extractor.extract("Avvertimi quando una corsa parte da Malpensa");
        List<AlertLocationResolution> resolutions = resolverService.resolve(extraction.mentions());

        assertThat(resolutions).singleElement()
                .satisfies(resolution -> {
                    assertThat(resolution.pointResolutionResult().status()).isEqualTo(PointResolutionStatus.RESOLVED_AMBIGUOUS);
                    assertThat(resolution.selectedPointIds()).contains(MALPENSA_T1_ID, MALPENSA_T2_ID);
                    assertThat(resolution.selectedPointIds()).hasSizeGreaterThan(1);
                    assertThat(resolution.fallbackToNameLong()).isFalse();
                });
    }

    @Test
    void unresolvedGenovaNerviProducesFallbackFlag() {
        AlertLocationExtractionResult extraction = extractor.extract("Avvertimi quando un treno passa da Genova Nervi");
        List<AlertLocationResolution> resolutions = resolverService.resolve(extraction.mentions());

        assertThat(resolutions).singleElement()
                .satisfies(resolution -> {
                    assertThat(resolution.pointResolutionResult().status()).isEqualTo(PointResolutionStatus.UNRESOLVED);
                    assertThat(resolution.selectedPointIds()).isEmpty();
                    assertThat(resolution.fallbackToNameLong()).isTrue();
                });
    }

    @Test
    void arrivalLocationStopsBeforePlatformBoundary() {
        AlertLocationExtractionResult result = extractor.extract(
                "Avvertimi quando un treno arriva a Garibaldi sul binario 1");

        assertThat(result.mentions()).singleElement()
                .extracting(AlertLocationMention::rawText)
                .isEqualTo("Garibaldi");
    }

    @Test
    void departureLocationStopsBeforePlatformBoundary() {
        AlertLocationExtractionResult result = extractor.extract(
                "Avvertimi quando un treno parte da Rho Fieramilano dal binario 2");

        assertThat(result.mentions()).singleElement()
                .extracting(AlertLocationMention::rawText)
                .isEqualTo("Rho Fieramilano");
    }

    @Test
    void genericLocationStopsBeforePlatformChangeClause() {
        AlertLocationExtractionResult result = extractor.extract(
                "Avvertimi quando una corsa a LAMPUGNANO subisce un cambio di binario in partenza");

        assertThat(result.mentions()).singleElement()
                .extracting(AlertLocationMention::rawText)
                .isEqualTo("LAMPUGNANO");
    }

    @Test
    void genericAlternativeLocationsStopBeforeDeparturePlatformClause() {
        AlertLocationExtractionResult result = extractor.extract(
                "Avvertimi quando una corsa a Buonarroti o Malpensa parte dal binario 1 o 4");

        assertThat(result.mentions())
                .extracting(AlertLocationMention::rawText)
                .containsExactly("Buonarroti", "Malpensa");
    }

    @Test
    void genericLocationAfterPlatformConstraintIsPreserved() {
        AlertLocationExtractionResult result = extractor.extract(
                "Avvertimi quando un treno viene spostato dal binario 5 al binario 7 o 8 a Genova P.P.");

        assertThat(result.mentions()).singleElement()
                .extracting(AlertLocationMention::rawText)
                .isEqualTo("Genova P.P");
    }

    @Test
    void platformBoundarySupportsSynonymsAndPrepositions() {
        Map<String, String> promptsByLocation = Map.ofEntries(
                Map.entry("Garibaldi", "Avvertimi quando un treno arriva a Garibaldi al binario 1"),
                Map.entry("Gorla", "Avvertimi quando un treno arriva a Gorla in banchina 1"),
                Map.entry("Buonarroti", "Avvertimi quando un treno arriva a Buonarroti sul quay 1"),
                Map.entry("Lampugnano", "Avvertimi quando un treno arriva a Lampugnano al marciapiede 1"),
                Map.entry("Malpensa", "Avvertimi quando un treno parte da Malpensa dal tronco 1"),
                Map.entry("Rho Fieramilano", "Avvertimi quando un treno arriva a Rho Fieramilano sulla piattaforma 1"),
                Map.entry("Sesto", "Avvertimi quando un treno arriva a Sesto sul track 1"),
                Map.entry("Nervi", "Avvertimi quando un treno arriva a Nervi sul platform 1"));

        assertThat(promptsByLocation).allSatisfy((location, prompt) ->
                assertThat(extractor.extract(prompt).mentions()).singleElement()
                        .extracting(AlertLocationMention::rawText)
                        .isEqualTo(location));
    }
}
