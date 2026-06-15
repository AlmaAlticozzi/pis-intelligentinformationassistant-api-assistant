package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScheduledAlertLocationUnderstandingServiceTest {

    @Test
    void fallsBackWhenGatewayFailsForScheduledMonitoringPrompt() {
        ScheduledAlertLocationUnderstandingResult result = serviceWithGatewayFailure("IIA-UTL-TXI-503-001")
                .understandLocations(
                        "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme",
                        "ALRT1");

        assertThat(result.monitoringScope()).isEqualTo(ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS);
        assertThat(result.locations()).anySatisfy(location -> {
            assertThat(location.rawText()).isEqualTo("Gerusalemme");
            assertThat(location.role()).isEqualTo(ScheduledAlertLocationRole.MONITORED_STOP_POINT);
        });
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("IIA-UTL-TXI-503-001"));
    }

    @Test
    void fallsBackWhenResponseCannotBeParsed() {
        ScheduledAlertLocationUnderstandingResult result = serviceWithResponse("not-json")
                .understandLocations(
                        "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme con destinazione Bignami",
                        "ALRT1");

        assertThat(result.locations()).anySatisfy(location -> {
            assertThat(location.rawText()).isEqualTo("Gerusalemme");
            assertThat(location.role()).isEqualTo(ScheduledAlertLocationRole.MONITORED_STOP_POINT);
        });
        assertThat(result.locations()).anySatisfy(location -> {
            assertThat(location.rawText()).isEqualTo("Bignami");
            assertThat(location.role()).isEqualTo(ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT);
        });
    }

    @Test
    void doesNotFallbackWhenLlmReturnsValidResult() {
        ScheduledAlertLocationUnderstandingResult result = serviceWithResponse("""
                {
                  "hasLocations": true,
                  "language": "it",
                  "monitoringScope": "EXPLICIT_STOP_POINTS",
                  "locations": [
                    {
                      "rawText": "Lecco",
                      "normalizedText": "Lecco",
                      "role": "MONITORED_STOP_POINT",
                      "relationToSnapshot": "SERVICE_DATA_API_QUERY_STOP_POINT",
                      "requiredForApiQuery": true,
                      "requiredCoverage": true,
                      "polarity": "INCLUDE",
                      "logicalGroup": "G1",
                      "confidence": 0.95
                    }
                  ],
                  "nonLocationConstraints": [],
                  "warnings": []
                }
                """).understandLocations(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme",
                "ALRT1");

        assertThat(result.locations()).extracting(ScheduledAlertLocationMention::rawText)
                .containsExactly("Lecco");
    }

    @Test
    void doesNotFallbackWhenParsedSemanticResultHasNoLocations() {
        ScheduledAlertLocationUnderstandingResult result = serviceWithResponse("""
                {
                  "hasLocations": false,
                  "language": "it",
                  "monitoringScope": "UNSPECIFIED",
                  "locations": [],
                  "nonLocationConstraints": [],
                  "warnings": ["No explicit scheduled monitoring location found."]
                }
                """).understandLocations(
                "Avvertimi ogni 10 min su quante corse soppresse ci sono a Gerusalemme",
                "ALRT1");

        assertThat(result.locations()).isEmpty();
        assertThat(result.warnings()).contains("No explicit scheduled monitoring location found.");
    }

    private ScheduledAlertLocationUnderstandingService serviceWithResponse(String response) {
        Instance<LlmGateway> llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(llmGateway.isUnsatisfied()).thenReturn(false);
        when(llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenReturn(new LlmResponse(response, "FAKE", "fake-model", null, null, null));
        return service(llmGateway);
    }

    private ScheduledAlertLocationUnderstandingService serviceWithGatewayFailure(String reason) {
        Instance<LlmGateway> llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(llmGateway.isUnsatisfied()).thenReturn(false);
        when(llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenThrow(new RuntimeException(reason));
        return service(llmGateway);
    }

    private ScheduledAlertLocationUnderstandingService service(Instance<LlmGateway> llmGateway) {
        return new ScheduledAlertLocationUnderstandingService(
                new ScheduledAlertLocationUnderstandingPromptBuilder(),
                new ScheduledAlertLocationUnderstandingResponseParser(),
                new ScheduledAlertLocationUnderstandingValidator(),
                new ScheduledAlertLocationUnderstandingFallbackClassifier(),
                llmGateway);
    }
}
