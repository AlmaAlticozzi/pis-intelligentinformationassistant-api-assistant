package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledAlertLocationUnderstandingFallbackClassifierTest {

    private final ScheduledAlertLocationUnderstandingFallbackClassifier classifier =
            new ScheduledAlertLocationUnderstandingFallbackClassifier();

    @Test
    void extractsSimpleScheduledMonitoringLocation() {
        ScheduledAlertLocationUnderstandingResult result = classify(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme");

        assertMonitoring(result, "Gerusalemme");
        assertThat(result.locations()).noneMatch(location ->
                location.role() == ScheduledAlertLocationRole.FILTER_CANCELLED_CALL_STOP_POINT);
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("IIA-UTL-TXI-503-001"));
    }

    @Test
    void extractsMonitoringAndDestinationFilter() {
        ScheduledAlertLocationUnderstandingResult result = classify(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme con destinazione Bignami");

        assertMonitoring(result, "Gerusalemme");
        assertLocation(result, "Bignami", ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT);
        assertThat(location(result, "Gerusalemme").role()).isEqualTo(ScheduledAlertLocationRole.MONITORED_STOP_POINT);
        assertThat(location(result, "Bignami").role()).isEqualTo(ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT);
    }

    @Test
    void extractsMonitoringAndRouteFilter() {
        ScheduledAlertLocationUnderstandingResult result = classify(
                "Avvertimi ogni 10 min su quante corse a Gerusalemme passano da Bignami");

        assertMonitoring(result, "Gerusalemme");
        assertLocation(result, "Bignami", ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT);
    }

    @Test
    void extractsDepartureMonitoringLocation() {
        ScheduledAlertLocationUnderstandingResult result = classify(
                "Avvertimi ogni 10 min su quante corse in partenza ci sono da Lecco");

        assertMonitoring(result, "Lecco");
        assertThat(result.locations()).noneMatch(location ->
                location.rawText().equals("Lecco")
                        && location.role() == ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT);
    }

    @Test
    void extractsArrivalMonitoringLocation() {
        ScheduledAlertLocationUnderstandingResult result = classify(
                "Avvertimi ogni 10 min su quante corse in arrivo ci sono a Milano Garibaldi");

        assertMonitoring(result, "Milano Garibaldi");
    }

    @Test
    void preservesMultiwordMonitoringLocation() {
        ScheduledAlertLocationUnderstandingResult result = classify(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a San Siro Stadio");

        assertMonitoring(result, "San Siro Stadio");
    }

    @Test
    void doesNotApplyFallbackForAmbiguousBetweenLocations() {
        Optional<ScheduledAlertLocationUnderstandingResult> result = classifier.classify(
                "ALRT1",
                "Avvertimi ogni 10 min su quante corse soppresse ci sono tra Gerusalemme e Bignami",
                ScheduledAlertLocationUnderstandingHints.fromPrompt(
                        "Avvertimi ogni 10 min su quante corse soppresse ci sono tra Gerusalemme e Bignami"),
                "IIA-UTL-TXI-503-001");

        assertThat(result).isEmpty();
    }

    @Test
    void doesNotApplyFallbackWhenMonitoringLocationIsMissing() {
        Optional<ScheduledAlertLocationUnderstandingResult> result = classifier.classify(
                "ALRT1",
                "Avvertimi ogni 10 min su quante corse soppresse ci sono",
                ScheduledAlertLocationUnderstandingHints.fromPrompt(
                        "Avvertimi ogni 10 min su quante corse soppresse ci sono"),
                "IIA-UTL-TXI-503-001");

        assertThat(result).isEmpty();
    }

    private ScheduledAlertLocationUnderstandingResult classify(String prompt) {
        return classifier.classify(
                        "ALRT1",
                        prompt,
                        ScheduledAlertLocationUnderstandingHints.fromPrompt(prompt),
                        "IIA-UTL-TXI-503-001")
                .orElseThrow();
    }

    private void assertMonitoring(ScheduledAlertLocationUnderstandingResult result, String text) {
        assertThat(result.monitoringScope()).isEqualTo(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS);
        assertLocation(result, text, ScheduledAlertLocationRole.MONITORED_STOP_POINT);
        assertThat(location(result, text).requiredForApiQuery()).isTrue();
    }

    private void assertLocation(
            ScheduledAlertLocationUnderstandingResult result,
            String text,
            ScheduledAlertLocationRole role) {
        assertThat(result.locations()).anySatisfy(location -> {
            assertThat(location.rawText()).isEqualTo(text);
            assertThat(location.role()).isEqualTo(role);
        });
    }

    private ScheduledAlertLocationMention location(ScheduledAlertLocationUnderstandingResult result, String text) {
        return result.locations().stream()
                .filter(location -> location.rawText().equals(text))
                .findFirst()
                .orElseThrow();
    }
}
