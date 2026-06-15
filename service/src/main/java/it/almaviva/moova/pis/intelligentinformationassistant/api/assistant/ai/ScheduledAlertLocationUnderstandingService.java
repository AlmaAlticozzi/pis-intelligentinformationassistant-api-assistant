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
    ScheduledAlertLocationUnderstandingValidator validator;

    @Inject
    ScheduledAlertLocationUnderstandingFallbackClassifier fallbackClassifier;

    @Inject
    Instance<LlmGateway> llmGateway;

    public ScheduledAlertLocationUnderstandingService() {
    }

    ScheduledAlertLocationUnderstandingService(
            ScheduledAlertLocationUnderstandingPromptBuilder promptBuilder,
            ScheduledAlertLocationUnderstandingResponseParser parser,
            ScheduledAlertLocationUnderstandingValidator validator,
            ScheduledAlertLocationUnderstandingFallbackClassifier fallbackClassifier,
            Instance<LlmGateway> llmGateway) {
        this.promptBuilder = promptBuilder;
        this.parser = parser;
        this.validator = validator;
        this.fallbackClassifier = fallbackClassifier;
        this.llmGateway = llmGateway;
    }

    public ScheduledAlertLocationUnderstandingResult understandLocations(String prompt, String correlationId) {
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] prompt=" + prompt);
        ScheduledAlertLocationUnderstandingHints hints = ScheduledAlertLocationUnderstandingHints.fromPrompt(prompt);
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][HINTS] " + hints);
        if (llmGateway == null || llmGateway.isUnsatisfied()) {
            String reason = "No LlmGateway available for ALERT_SCHEDULED_LOCATION_UNDERSTANDING.";
            ScheduledAlertLocationUnderstandingResult result = fallback(prompt, correlationId, hints, reason)
                    .orElseGet(() -> ScheduledAlertLocationUnderstandingResult.emptyWithWarnings(List.of(reason)));
            printParsed(result);
            return result;
        }

        try {
            LlmRequest request = promptBuilder.build(prompt, correlationId, hints);
            LlmResponse response = llmGateway.get().generateText(request);
            String raw = response == null ? null : response.text();
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] raw LLM response=" + truncate(raw));
            ScheduledAlertLocationUnderstandingResponseParser.ParseResult parseResult = parser.parseDetailed(raw);
            ScheduledAlertLocationUnderstandingResult result;
            if (parseResult.parsed()) {
                result = validate(parseResult.result(), prompt, hints);
            } else {
                String reason = parseResult.warnings().isEmpty()
                        ? "Scheduled location LLM response could not be parsed."
                        : String.join("; ", parseResult.warnings());
                result = fallback(prompt, correlationId, hints, reason)
                        .orElseGet(() -> validate(parseResult.result(), prompt, hints));
            }
            printParsed(result);
            return result;
        } catch (RuntimeException ex) {
            String reason = shortTechnicalMessage(ex);
            ScheduledAlertLocationUnderstandingResult result = fallback(prompt, correlationId, hints, reason)
                    .orElseGet(() -> ScheduledAlertLocationUnderstandingResult.emptyWithWarnings(
                            List.of("Scheduled location understanding failed: " + reason)));
            printParsed(result);
            return result;
        }
    }

    private ScheduledAlertLocationUnderstandingResult validate(
            ScheduledAlertLocationUnderstandingResult result,
            String prompt,
            ScheduledAlertLocationUnderstandingHints hints) {
        return validator == null ? result : validator.validate(result, prompt, hints);
    }

    private java.util.Optional<ScheduledAlertLocationUnderstandingResult> fallback(
            String prompt,
            String correlationId,
            ScheduledAlertLocationUnderstandingHints hints,
            String reason) {
        ScheduledAlertLocationUnderstandingFallbackClassifier classifier = fallbackClassifier == null
                ? new ScheduledAlertLocationUnderstandingFallbackClassifier()
                : fallbackClassifier;
        return classifier.classify(correlationId, prompt, hints, reason)
                .map(result -> validate(result, prompt, hints));
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

    private String shortTechnicalMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if ((message == null || message.isBlank()) && throwable.getCause() != null) {
            message = throwable.getCause().getMessage();
        }
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
