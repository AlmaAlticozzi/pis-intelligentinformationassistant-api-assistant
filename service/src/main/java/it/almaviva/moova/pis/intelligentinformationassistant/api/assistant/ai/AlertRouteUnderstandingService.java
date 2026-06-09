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
            AlertRouteUnderstandingResult rejected = AlertRouteUnderstandingResult.rejected(
                    "Alert route understanding cannot run because no LLM gateway is available.");
            AlertRouteUnderstandingResult validated = validator.validate(rejected, prompt, hints);
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
                AlertRouteUnderstandingResult rejected = AlertRouteUnderstandingResult.rejected(
                        "Alert route response could not be parsed: " + parseResult.failureReason());
                AlertRouteUnderstandingResult validated = validator.validate(rejected, prompt, hints);
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
            return routeError(shortTechnicalMessage(ex), prompt);
        } catch (RuntimeException ex) {
            return routeError(shortTechnicalMessage(ex), prompt);
        }
    }

    private AlertRouteUnderstandingResult routeError(String message, String prompt) {
        String reason = "Alert route understanding failed before technical verification: " + message;
        System.out.println("[IIA][ALERT_ROUTE] validation result=ERROR reason=" + reason);
        AlertRouteUnderstandingResult rejected = AlertRouteUnderstandingResult.rejected(reason);
        AlertRouteUnderstandingResult validated = validator.validate(
                rejected,
                prompt,
                AlertRouteUnderstandingHints.fromPrompt(prompt));
        System.out.println("[IIA][ALERT_ROUTE][VALIDATED] " + validated);
        printFinal(validated);
        return validated;
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
