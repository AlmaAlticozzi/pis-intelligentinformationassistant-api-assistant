package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertLocationUnderstandingHintsTest {

    @Test
    void detectsPlatformOriginDestinationRouteAndAllLocationsSignals() {
        ScheduledAlertLocationUnderstandingHints platform = ScheduledAlertLocationUnderstandingHints.fromPrompt(
                "Fammi sapere il numero di corse che partiranno dal binario 7 a Cenisio");
        ScheduledAlertLocationUnderstandingHints destination = ScheduledAlertLocationUnderstandingHints.fromPrompt(
                "L'importante e che non hanno come destinazione Tre Torri");
        ScheduledAlertLocationUnderstandingHints route = ScheduledAlertLocationUnderstandingHints.fromPrompt(
                "Fammi sapere se la corsa 899 passa a Gerusalemme, Tre Torri e Portello");
        ScheduledAlertLocationUnderstandingHints all = ScheduledAlertLocationUnderstandingHints.fromPrompt(
                "Fammi sapere ogni ora in tutte le fermate");

        assertThat(platform.containsPlatformExpression()).isTrue();
        assertThat(destination.containsDestinationExpression()).isTrue();
        assertThat(route.containsRouteExpression()).isTrue();
        assertThat(all.containsAllLocationsExpression()).isTrue();
    }
}
