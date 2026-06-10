package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRouteUnderstandingHintsTest {

    @Test
    void platformNumberIsNotJourneyCardinalityForArrival() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvisami quando una corsa arriva a Garibaldi sul binario 1");

        assertThat(hints.containsPlatformExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
        assertThat(hints.containsSnapshotStateExpression()).isFalse();
        assertThat(hints.containsCountOrReportExpression()).isFalse();
    }

    @Test
    void platformNumberIsNotJourneyCardinalityForDeparture() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvisami quando una corsa parte da Cenisio dal binario 7");

        assertThat(hints.containsPlatformExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
    }

    @Test
    void countReportFromPlatformStillRequiresScheduledSemantics() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere quanti treni partono dal binario 7 a Cenisio");

        assertThat(hints.containsPlatformExpression()).isTrue();
        assertThat(hints.containsCountOrReportExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
    }

    @Test
    void countReportWithOriginFilterIsNotCardinalityThreshold() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Per la localita Pero fammi sapere quanti hanno come origine Garibaldi FS");

        assertThat(hints.containsCountOrReportExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
    }

    @Test
    void multiLocationJourneyCardinalityKeepsSnapshotSignals() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere quando a Varedo e Palazzolo Milanese ci sono due treni in arrivo");

        assertThat(hints.containsCardinalityThresholdExpression()).isTrue();
        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsMultiMonitoredLocationExpression()).isTrue();
    }

    @Test
    void alternativeMultiLocationArrivalSuppressionIsEventOccurrenceSignal() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvertimi quando a Bignami o San Siro stadio o Bologna c'e un treno in arrivo con una soppressione in partenza");

        assertThat(hints.containsEventOccurrenceExpression()).isTrue();
        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsPollingExpression()).isFalse();
        assertThat(hints.containsCountOrReportExpression()).isFalse();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
    }

    @Test
    void alternativeMultiLocationGenericCancellationIsEventOccurrenceSignal() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvisami quando a Lecco o Milano Centrale viene soppresso un treno");

        assertThat(hints.containsEventOccurrenceExpression()).isTrue();
        assertThat(hints.containsPollingExpression()).isFalse();
        assertThat(hints.containsCountOrReportExpression()).isFalse();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
    }

    @Test
    void thresholdJourneyCardinalityKeepsSnapshotSignals() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere se a Garibaldi FS ci sono piu di cinque treni in ritardo");

        assertThat(hints.containsCardinalityThresholdExpression()).isTrue();
        assertThat(hints.containsSnapshotStateExpression()).isTrue();
    }

    @Test
    void delayThresholdForSingleDepartureIsAttributeNotCardinality() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvisami quando una corsa parte da Garibaldi FS con ritardo di almeno 5 min");

        assertThat(hints.containsEventOccurrenceExpression()).isTrue();
        assertThat(hints.containsAttributeThresholdExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
        assertThat(hints.containsPollingExpression()).isFalse();
        assertThat(hints.containsSnapshotStateExpression()).isFalse();
    }

    @Test
    void englishDelayThresholdForSingleDepartureIsAttributeNotCardinality() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Notify me when a journey departs from Garibaldi FS with a delay of at least 5 minutes");

        assertThat(hints.containsEventOccurrenceExpression()).isTrue();
        assertThat(hints.containsAttributeThresholdExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
        assertThat(hints.containsPollingExpression()).isFalse();
    }

    @Test
    void delayThresholdForSingleArrivalIsAttributeNotCardinality() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvisami quando una corsa arriva a Garibaldi FS con ritardo superiore a 10 minuti");

        assertThat(hints.containsEventOccurrenceExpression()).isTrue();
        assertThat(hints.containsAttributeThresholdExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
    }

    @Test
    void snapshotPresenceWithDelayThresholdIsScheduledButNotCardinality() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Ci sono corse a Garibaldi FS con ritardo di almeno 5 min");

        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsAttributeThresholdExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isFalse();
    }

    @Test
    void countQuestionWithDelayThresholdIsScheduledReport() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Quanti treni a Garibaldi FS hanno ritardo di almeno 5 min");

        assertThat(hints.containsCountOrReportExpression()).isTrue();
        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsAttributeThresholdExpression()).isTrue();
    }

    @Test
    void journeyCountThresholdRemainsCardinality() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvisami quando ci sono almeno 3 corse in ritardo a Garibaldi FS");

        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isTrue();
    }

    @Test
    void periodicCountReportKeepsPollingAndReportSignals() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Ogni 10 minuti dimmi quante corse in ritardo ci sono a Garibaldi FS");

        assertThat(hints.containsPollingExpression()).isTrue();
        assertThat(hints.containsCountOrReportExpression()).isTrue();
        assertThat(hints.containsSnapshotStateExpression()).isTrue();
    }

    @Test
    void allLocationsHourlyReportIsServiceDataSnapshotNotWeather() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere ogni ora quanti treni sono in ritardo in tutte le località");

        assertThat(hints.containsPollingExpression()).isTrue();
        assertThat(hints.containsCountOrReportExpression()).isTrue();
        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsAllLocationsExpression()).isTrue();
        assertThat(hints.containsUnsupportedWeatherExpression()).isFalse();
    }

    @Test
    void weatherPromptStillSetsWeatherHint() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvisami quando piove a Garibaldi");

        assertThat(hints.containsUnsupportedWeatherExpression()).isTrue();
    }

    @Test
    void allStopsHourlyReportIsServiceDataSnapshotNotWeather() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere ogni ora quanti treni sono in ritardo in tutte le fermate");

        assertThat(hints.containsAllLocationsExpression()).isTrue();
        assertThat(hints.containsUnsupportedWeatherExpression()).isFalse();
    }

    @Test
    void snapshotPlatformChangeBooleanKeepsSnapshotSignals() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere se a San Siro Stadio ci sono treni che hanno subito un cambio di binario");

        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsPlatformChangeExpression()).isTrue();
        assertThat(hints.containsEventOccurrenceExpression()).isFalse();
    }

    @Test
    void platformChangeEventOccurrenceKeepsEventSignal() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Avvisami quando una corsa cambia binario a San Siro Stadio");

        assertThat(hints.containsPlatformChangeExpression()).isTrue();
        assertThat(hints.containsSnapshotStateExpression()).isFalse();
        assertThat(hints.containsEventOccurrenceExpression()).isTrue();
    }

    @Test
    void snapshotCancellationSetsSnapshotAndChangeSignals() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere se a Garibaldi FS ci sono treni cancellati");

        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsChangeCancellationExclusionExpression()).isTrue();
        assertThat(hints.containsEventOccurrenceExpression()).isFalse();
    }

    @Test
    void complexScheduledConditionKeepsSnapshotSignals() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere se il numero di treni in ritardo e che hanno subito un cambio di binario a Buonarroti e maggiore di 5. L'importante e che non hanno come destinazione Tre Torri");

        assertThat(hints.containsCountOrReportExpression()).isTrue();
        assertThat(hints.containsCardinalityThresholdExpression()).isTrue();
        assertThat(hints.containsSnapshotStateExpression()).isTrue();
        assertThat(hints.containsPlatformChangeExpression()).isTrue();
        assertThat(hints.containsEventOccurrenceExpression()).isFalse();
    }

    @Test
    void unsupportedWifiReportDoesNotLookLikeEventOccurrence() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere il numero di treni con wifi a bordo e in ritardo in partenza maggiore di 10 min a Portello");

        assertThat(hints.containsCountOrReportExpression()).isTrue();
        assertThat(hints.containsUnsupportedWifiOrOnboardFeatureExpression()).isTrue();
        assertThat(hints.containsEventOccurrenceExpression()).isFalse();
    }
}
