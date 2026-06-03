package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlertLocationUnderstandingServiceTest {

    @Test
    void understandsMainEventAndRouteLocationFromFakeLlm() {
        CapturingGateway gateway = new CapturingGateway("""
                {
                  "hasLocations": true,
                  "mainEvent": {"eventIntent": "DEPARTURE", "confidence": 0.9},
                  "locations": [
                    {"rawText": "Garibaldi", "role": "MAIN_EVENT_LOCATION", "relationToMainEvent": "EVENT_STOP_POINT", "requiredCoverage": true, "confidence": 0.9},
                    {"rawText": "Venezia", "role": "ROUTE_OR_NEXT_CALL_LOCATION", "relationToMainEvent": "FUTURE_ROUTE_CONSTRAINT", "requiredCoverage": true, "confidence": 0.8}
                  ],
                  "nonLocationConstraints": [],
                  "warnings": []
                }
                """);

        AlertLocationUnderstandingResult result = service(gateway)
                .understandLocations("Dimmi quando parte da Garibaldi e passera da Venezia", "ALRT1");

        assertThat(gateway.lastRequest.useCase()).isEqualTo(AiUseCase.ALERT_LOCATION_UNDERSTANDING);
        assertThat(result.hasLocations()).isTrue();
        assertThat(result.locations()).extracting(AlertLocationUnderstandingLocation::role)
                .containsExactly(AlertLocationRole.MAIN_EVENT_LOCATION, AlertLocationRole.ROUTE_OR_NEXT_CALL_LOCATION);
    }

    @Test
    void understandsPromptWithoutLocationsFromFakeLlm() {
        CapturingGateway gateway = new CapturingGateway("""
                {
                  "hasLocations": false,
                  "mainEvent": {"eventIntent": "DELAY", "confidence": 0.8},
                  "locations": [],
                  "nonLocationConstraints": [],
                  "warnings": []
                }
                """);

        AlertLocationUnderstandingResult result = service(gateway)
                .understandLocations("Avvisa quando un treno ha 10 minuti di ritardo", "ALRT2");

        assertThat(result.hasLocations()).isFalse();
        assertThat(result.locations()).isEmpty();
        assertThat(result.mainEvent().eventIntent()).isEqualTo(AlertLocationMainEventIntent.DELAY);
    }

    @Test
    void keepsPlatformAsNonLocationConstraint() {
        CapturingGateway gateway = new CapturingGateway("""
                {
                  "hasLocations": false,
                  "mainEvent": {"eventIntent": "ARRIVAL", "confidence": 0.8},
                  "locations": [],
                  "nonLocationConstraints": [{"type": "PLATFORM", "rawText": "binario 1"}],
                  "warnings": []
                }
                """);

        AlertLocationUnderstandingResult result = service(gateway)
                .understandLocations("Avvisa quando arriva una corsa sul binario 1", "ALRT3");

        assertThat(result.locations()).isEmpty();
        assertThat(result.nonLocationConstraints()).hasSize(1);
        assertThat(result.nonLocationConstraints().getFirst().type())
                .isEqualTo(AlertLocationNonLocationConstraintType.PLATFORM);
    }

    @SuppressWarnings("unchecked")
    private AlertLocationUnderstandingService service(LlmGateway gateway) {
        Instance<LlmGateway> instance = mock(Instance.class);
        when(instance.isUnsatisfied()).thenReturn(false);
        when(instance.get()).thenReturn(gateway);
        return new AlertLocationUnderstandingService(
                new AlertLocationUnderstandingPromptBuilder(),
                new AlertLocationUnderstandingLlmResponseParser(),
                instance);
    }

    private static class CapturingGateway implements LlmGateway {
        private final String responseText;
        private LlmRequest lastRequest;

        private CapturingGateway(String responseText) {
            this.responseText = responseText;
        }

        @Override
        public LlmResponse generateText(LlmRequest request) {
            lastRequest = request;
            return new LlmResponse(responseText, "FAKE", request.model(), null, null, null);
        }
    }
}
