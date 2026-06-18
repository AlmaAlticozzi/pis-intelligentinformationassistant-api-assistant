package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRouteUnderstandingFallbackClassifierTest {

    private final AlertRouteUnderstandingFallbackClassifier classifier = new AlertRouteUnderstandingFallbackClassifier();

    @Test
    void selectsScheduledInterpreterForPollingCountSnapshotPromptAfterRouteLlmFailure() {
        String prompt = "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme";
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(prompt);

        Optional<AlertRouteUnderstandingResult> result = classifier.classify(
                "ALRT1",
                prompt,
                hints,
                "IIA-UTL-TXI-503-001");

        assertThat(result).isPresent();
        assertThat(result.get().decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(result.get().interpreterType()).isEqualTo(AlertRouteInterpreterType.SCHEDULED_INTERPRETER);
        assertThat(result.get().accessMode()).isEqualTo(AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT);
        assertThat(result.get().requiresPolling()).isTrue();
        assertThat(result.get().requiresServiceDataApi()).isTrue();
        assertThat(result.get().requiresKafkaEvent()).isFalse();
        assertThat(result.get().warnings()).anySatisfy(warning ->
                assertThat(warning).contains("IIA-UTL-TXI-503-001", "SCHEDULED_INTERPRETER"));
    }

    @Test
    void selectsEventInterpreterForEventHintsAfterRouteLlmFailure() {
        String prompt = "Avvertimi quando una corsa soppressa arriva a Garibaldi";
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(prompt);

        Optional<AlertRouteUnderstandingResult> result = classifier.classify(
                "ALRT2",
                prompt,
                hints,
                "timeout");

        assertThat(result).isPresent();
        assertThat(result.get().decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(result.get().interpreterType()).isEqualTo(AlertRouteInterpreterType.EVENT_INTERPRETER);
        assertThat(result.get().accessMode()).isEqualTo(AlertRouteAccessMode.KAFKA_EVENT);
        assertThat(result.get().requiresPolling()).isFalse();
        assertThat(result.get().requiresServiceDataApi()).isFalse();
        assertThat(result.get().requiresKafkaEvent()).isTrue();
        assertThat(result.get().intentKind()).isEqualTo(AlertRouteIntentKind.EVENT_CONDITION);
        assertThat(result.get().confidence()).isEqualTo(0.70);
        assertThat(result.get().warnings()).anySatisfy(warning ->
                assertThat(warning).contains("timeout", "EVENT_INTERPRETER"));
    }

    @Test
    void selectsEventInterpreterForDepartureProgressiveAfterRouteLlmFailure() {
        String prompt = "Avvertimi quando una corsa e in partenza";
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(prompt);

        Optional<AlertRouteUnderstandingResult> result = classifier.classify(
                "ALRT4",
                prompt,
                hints,
                "IIA-UTL-TXI-503-001");

        assertThat(result).isPresent();
        assertThat(result.get().decision()).isEqualTo(AlertRouteDecision.ROUTED);
        assertThat(result.get().primaryDataDomain()).isEqualTo("SERVICE_DATA");
        assertThat(result.get().dataDomains()).containsExactly("SERVICE_DATA");
        assertThat(result.get().interpreterType()).isEqualTo(AlertRouteInterpreterType.EVENT_INTERPRETER);
        assertThat(result.get().accessMode()).isEqualTo(AlertRouteAccessMode.KAFKA_EVENT);
        assertThat(result.get().intentKind()).isEqualTo(AlertRouteIntentKind.EVENT_CONDITION);
        assertThat(result.get().outputMode()).isEqualTo(AlertRouteOutputMode.ON_MATCH);
        assertThat(result.get().requiresKafkaEvent()).isTrue();
        assertThat(result.get().requiresPolling()).isFalse();
        assertThat(result.get().requiresServiceDataApi()).isFalse();
        assertThat(result.get().hasAggregation()).isFalse();
        assertThat(result.get().hasCardinalityThreshold()).isFalse();
        assertThat(result.get().warnings()).anySatisfy(warning ->
                assertThat(warning).contains("IIA-UTL-TXI-503-001", "EVENT_INTERPRETER"));
    }

    @Test
    void selectsScheduledInterpreterWhenSnapshotOrCountHintsCoexistWithEventWords() {
        for (String prompt : java.util.List.of(
                "Ogni 10 minuti dimmi quante corse sono in partenza",
                "Quante corse sono in partenza a Garibaldi?")) {
            Optional<AlertRouteUnderstandingResult> result = classifier.classify(
                    "ALRT5",
                    prompt,
                    AlertRouteUnderstandingHints.fromPrompt(prompt),
                    "IIA-UTL-TXI-503-001");

            assertThat(result).as(prompt).isPresent();
            assertThat(result.get().interpreterType()).as(prompt)
                    .isEqualTo(AlertRouteInterpreterType.SCHEDULED_INTERPRETER);
        }
    }

    @Test
    void doesNotApplyFallbackForUnsupportedHints() {
        String prompt = "Avvisami ogni 10 minuti se piove a Garibaldi";

        Optional<AlertRouteUnderstandingResult> result = classifier.classify(
                "ALRT3",
                prompt,
                AlertRouteUnderstandingHints.fromPrompt(prompt),
                "IIA-UTL-TXI-503-001");

        assertThat(result).isEmpty();
    }

    @Test
    void doesNotApplyEventFallbackForAbsenceOverTime() {
        String prompt = "Avvertimi se non ci sono corse per 30 minuti";

        Optional<AlertRouteUnderstandingResult> result = classifier.classify(
                "ALRT6",
                prompt,
                AlertRouteUnderstandingHints.fromPrompt(prompt),
                "IIA-UTL-TXI-503-001");

        assertThat(result).isEmpty();
    }
}
