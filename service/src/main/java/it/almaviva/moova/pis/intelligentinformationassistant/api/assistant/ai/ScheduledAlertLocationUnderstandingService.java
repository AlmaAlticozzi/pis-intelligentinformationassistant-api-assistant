package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ScheduledAlertLocationUnderstandingService {

    @Inject
    ScheduledAlertLocationUnderstandingPromptBuilder promptBuilder;

    @Inject
    ScheduledAlertLocationUnderstandingResponseParser parser;

    @Inject
    Instance<LlmGateway> llmGateway;

    public ScheduledAlertLocationUnderstandingService() {
    }

    ScheduledAlertLocationUnderstandingService(
            ScheduledAlertLocationUnderstandingPromptBuilder promptBuilder,
            ScheduledAlertLocationUnderstandingResponseParser parser,
            Instance<LlmGateway> llmGateway) {
        this.promptBuilder = promptBuilder;
        this.parser = parser;
        this.llmGateway = llmGateway;
    }

    public ScheduledAlertLocationUnderstandingResult understandLocations(String prompt, String correlationId) {
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] prompt=" + prompt);
        if (llmGateway == null || llmGateway.isUnsatisfied()) {
            ScheduledAlertLocationUnderstandingResult result = ScheduledAlertLocationUnderstandingResult.emptyWithWarnings(
                    List.of("No LlmGateway available for ALERT_SCHEDULED_LOCATION_UNDERSTANDING."));
            printParsed(result);
            return result;
        }

        LlmRequest request = promptBuilder.build(prompt, correlationId);
        LlmResponse response = llmGateway.get().generateText(request);
        String raw = response == null ? null : response.text();
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] raw LLM response=" + truncate(raw));
        ScheduledAlertLocationUnderstandingResult result = parser.parse(raw);
        printParsed(result);
        return result;
    }

    private void printParsed(ScheduledAlertLocationUnderstandingResult result) {
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] parsed result=" + result);
        for (ScheduledAlertLocationMention location : result.locations()) {
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] location rawText="
                    + location.rawText()
                    + " role="
                    + location.role()
                    + " relationToSnapshot="
                    + location.relationToSnapshot()
                    + " requiredForApiQuery="
                    + location.requiredForApiQuery());
        }
        for (ScheduledAlertNonLocationConstraint constraint : result.nonLocationConstraints()) {
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] nonLocationConstraint type="
                    + constraint.type()
                    + " rawText="
                    + constraint.rawText());
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        int maxLength = 4000;
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...[truncated]";
    }
}
