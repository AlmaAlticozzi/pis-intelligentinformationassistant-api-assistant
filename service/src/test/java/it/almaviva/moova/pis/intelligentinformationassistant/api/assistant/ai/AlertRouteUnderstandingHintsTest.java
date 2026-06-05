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
    void thresholdJourneyCardinalityKeepsSnapshotSignals() {
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(
                "Fammi sapere se a Garibaldi FS ci sono piu di cinque treni in ritardo");

        assertThat(hints.containsCardinalityThresholdExpression()).isTrue();
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
}
