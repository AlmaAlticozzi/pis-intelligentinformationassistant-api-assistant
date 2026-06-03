package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class AlertLocationUnderstandingService {

    @Inject
    AlertLocationUnderstandingPromptBuilder promptBuilder;

    @Inject
    AlertLocationUnderstandingLlmResponseParser parser;

    @Inject
    Instance<LlmGateway> llmGateway;

    public AlertLocationUnderstandingService() {
    }

    AlertLocationUnderstandingService(
            AlertLocationUnderstandingPromptBuilder promptBuilder,
            AlertLocationUnderstandingLlmResponseParser parser,
            Instance<LlmGateway> llmGateway) {
        this.promptBuilder = promptBuilder;
        this.parser = parser;
        this.llmGateway = llmGateway;
    }

    public AlertLocationUnderstandingResult understandLocations(String prompt, String correlationId) {
        System.out.println("[IIA][ALERT_LOCATION_UNDERSTANDING] prompt=" + prompt);
        if (llmGateway == null || llmGateway.isUnsatisfied()) {
            AlertLocationUnderstandingResult result = new AlertLocationUnderstandingResult(
                    false,
                    "",
                    AlertLocationUnderstandingMainEvent.unknown(),
                    List.of(),
                    List.of(),
                    List.of("No LlmGateway available for ALERT_LOCATION_UNDERSTANDING."));
            System.out.println("[IIA][ALERT_LOCATION_UNDERSTANDING] parsedResult=" + result);
            return result;
        }

        LlmRequest request = promptBuilder.build(prompt, correlationId);
        LlmResponse response = llmGateway.get().generateText(request);
        String raw = response == null ? null : response.text();
        System.out.println("[IIA][ALERT_LOCATION_UNDERSTANDING] rawResponse=" + raw);
        AlertLocationUnderstandingResult result = parser.parse(raw);
        System.out.println("[IIA][ALERT_LOCATION_UNDERSTANDING] parsedResult=" + result);
        return result;
    }
}
