package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

@ApplicationScoped
public class AlertRouteUnderstandingService {

    @Inject
    AlertRouteUnderstandingPromptBuilder promptBuilder;

    @Inject
    AlertRouteUnderstandingResponseParser responseParser;

    @Inject
    AlertRouteUnderstandingValidator validator;

    @Inject
    AlertRouteUnderstandingFallbackClassifier fallbackClassifier;

    @Inject
    Instance<LlmGateway> llmGateway;

    public AlertRouteUnderstandingResult understand(AlertVerificationPromptData promptData) {
        String alertId = promptData == null ? null : promptData.alertId();
        String prompt = promptData == null ? null : promptData.prompt();
        System.out.println("[IIA][ALERT_ROUTE] start alertId=" + alertId);
        System.out.println("[IIA][ALERT_ROUTE] prompt=" + prompt);
        AlertRouteUnderstandingHints hints = AlertRouteUnderstandingHints.fromPrompt(prompt);
        System.out.println("[IIA][ALERT_ROUTE][HINTS] " + hints);
        System.out.println("[IIA][ALERT_ROUTE][HINTS][THRESHOLD] containsCardinalityThresholdExpression="
                + hints.containsCardinalityThresholdExpression()
                + " containsAttributeThresholdExpression="
                + hints.containsAttributeThresholdExpression());

        if (llmGateway == null || llmGateway.isUnsatisfied()) {
            String reason = "Alert route understanding cannot run because no LLM gateway is available.";
            AlertRouteUnderstandingResult validated = fallbackRoute(alertId, prompt, hints, reason)
                    .orElseGet(() -> validator.validate(AlertRouteUnderstandingResult.rejected(reason), prompt, hints));
            printFinal(validated);
            return validated;
        }

        LlmRequest routeRequest = promptBuilder.build(promptData, hints);
        try {
            LlmResponse response = llmGateway.get().generateText(routeRequest);
            String rawResponse = response == null ? null : response.text();
            System.out.println("[IIA][ALERT_ROUTE][LLM_RAW] " + truncate(rawResponse, 2000));

            AlertRouteUnderstandingResponseParser.ParseResult parseResult =
                    responseParser.parseDetailed(rawResponse);
            if (parseResult.result().isEmpty()) {
                System.out.println("[IIA][ALERT_ROUTE][PARSED] route=<empty> reason=" + parseResult.failureReason()
                        + " rawLength=" + parseResult.rawLength());
                String reason = "Alert route response could not be parsed: " + parseResult.failureReason();
                AlertRouteUnderstandingResult validated = fallbackRoute(alertId, prompt, hints, reason)
                        .orElseGet(() -> validator.validate(AlertRouteUnderstandingResult.rejected(reason), prompt, hints));
                System.out.println("[IIA][ALERT_ROUTE][VALIDATED] " + validated);
                printFinal(validated);
                return validated;
            }

            AlertRouteUnderstandingResult parsed = parseResult.result().get();
            System.out.println("[IIA][ALERT_ROUTE][PARSED] " + parsed);
            AlertRouteUnderstandingResult validated = validator.validate(parsed, prompt, hints);
            System.out.println("[IIA][ALERT_ROUTE][VALIDATED] " + validated);
            printFinal(validated);
            return validated;
        } catch (ProcessingException ex) {
            return routeError(alertId, shortTechnicalMessage(ex), prompt, hints);
        } catch (RuntimeException ex) {
            return routeError(alertId, shortTechnicalMessage(ex), prompt, hints);
        }
    }

    private AlertRouteUnderstandingResult routeError(
            String alertId,
            String message,
            String prompt,
            AlertRouteUnderstandingHints hints) {
        String reason = "Alert route understanding failed before technical verification: " + message;
        System.out.println("[IIA][ALERT_ROUTE] validation result=ERROR reason=" + reason);
        AlertRouteUnderstandingResult validated = fallbackRoute(alertId, prompt, hints, reason)
                .orElseGet(() -> validator.validate(
                        AlertRouteUnderstandingResult.rejected(reason),
                        prompt,
                        hints == null ? AlertRouteUnderstandingHints.fromPrompt(prompt) : hints));
        System.out.println("[IIA][ALERT_ROUTE][VALIDATED] " + validated);
        printFinal(validated);
        return validated;
    }

    private java.util.Optional<AlertRouteUnderstandingResult> fallbackRoute(
            String alertId,
            String prompt,
            AlertRouteUnderstandingHints hints,
            String reason) {
        AlertRouteUnderstandingFallbackClassifier classifier = fallbackClassifier == null
                ? new AlertRouteUnderstandingFallbackClassifier()
                : fallbackClassifier;
        return classifier.classify(alertId, prompt, hints, reason)
                .map(route -> validator.validate(route, prompt, hints));
    }

    private void printFinal(AlertRouteUnderstandingResult route) {
        System.out.println("[IIA][ALERT_ROUTE] final decision=" + route.decision()
                + " interpreterType=" + route.interpreterType()
                + " dataDomains=" + route.dataDomains());
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

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...[truncated length=" + value.length() + "]";
    }
}
