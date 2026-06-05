package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

import java.util.List;

@ApplicationScoped
public class ScheduledAlertVerificationService {

    private static final String PROMPT_VERSION = "alert-scheduled-verify-mvp-v1";

    @Inject
    ScheduledAlertVerificationPromptBuilder promptBuilder;

    @Inject
    ScheduledAlertVerificationResponseParser responseParser;

    @Inject
    ScheduledAlertVerificationOutcomeValidator outcomeValidator;

    @Inject
    Instance<LlmGateway> llmGateway;

    public AlertVerificationOutcome verify(
            String alertId,
            String name,
            String description,
            String originalPrompt,
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY] start alertId=" + alertId);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY] route intentKind="
                + (route == null ? null : route.intentKind())
                + " outputMode=" + (route == null ? null : route.outputMode()));
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY] monitoringScope="
                + (locationContext == null ? null : locationContext.monitoringScope())
                + " requiresAllKnownStopPoints="
                + (locationContext != null && locationContext.requiresAllKnownStopPoints()));
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY] serviceDataApiStopPoints count="
                + serviceDataStopPointCount(locationContext)
                + " ids=" + compactStopPoints(locationContext));

        if (!isScheduledServiceDataRoute(route)) {
            return rejected(
                    alertId,
                    "Scheduled alert verification can only run for SERVICE_DATA + SCHEDULED_INTERPRETER routes.",
                    "Scheduled alert verification requires a routed SERVICE_DATA + SCHEDULED_INTERPRETER alert.",
                    null,
                    null);
        }
        if (llmGateway == null || llmGateway.isUnsatisfied()) {
            return rejected(
                    alertId,
                    "Scheduled alert verification could not complete.",
                    "Scheduled alert verification cannot run because no LLM gateway is available.",
                    null,
                    null);
        }

        LlmRequest request = promptBuilder.build(new ScheduledAlertVerificationPromptData(
                alertId,
                name,
                description,
                originalPrompt,
                route,
                locationContext));
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY] prompt length="
                + promptLength(request.systemPrompt()) + "+" + promptLength(request.userPrompt()));

        try {
            LlmResponse response = llmGateway.get().generateText(request);
            String rawResponse = response == null ? null : response.text();
            String provider = response == null ? null : response.provider();
            String model = response == null ? request.model() : response.model();
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][LLM] raw response="
                    + truncate(rawResponse, 3000));

            ScheduledAlertVerificationResponseParser.ParseResult parseResult =
                    responseParser.parseDetailed(rawResponse, provider, model);
            if (parseResult.outcome().isEmpty()) {
                String reason = "Scheduled alert verification failed because the LLM response could not be parsed or accepted: "
                        + safeReason(parseResult.failureReason());
                System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PARSER] parsed decision=<empty> reason="
                        + reason + " rawLength=" + parseResult.rawLength());
                return rejected(alertId, "Scheduled alert verification response could not be parsed.", reason, provider, model);
            }

            AlertVerificationOutcome parsed = parseResult.outcome().get();
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PARSER] parsed decision=" + parsed.decision());
            AlertVerificationOutcome validated = outcomeValidator.validate(parsed, locationContext);
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] validator decision="
                    + validated.decision() + " rejectedReason=" + validated.rejectedReason());
            printOutcome(validated);
            return validated;
        } catch (ProcessingException ex) {
            return providerFailure(alertId, request, ex);
        } catch (LlmProviderException ex) {
            return providerFailure(alertId, request, ex);
        } catch (RuntimeException ex) {
            return rejected(
                    alertId,
                    "Scheduled alert verification could not complete.",
                    "Scheduled alert verification failed validation: " + safeReason(shortTechnicalMessage(ex)),
                    null,
                    request.model());
        }
    }

    private boolean isScheduledServiceDataRoute(AlertRouteUnderstandingResult route) {
        return route != null
                && route.decision() == AlertRouteDecision.ROUTED
                && route.interpreterType() == AlertRouteInterpreterType.SCHEDULED_INTERPRETER
                && route.accessMode() == AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT
                && "SERVICE_DATA".equals(route.primaryDataDomain())
                && route.dataDomains().contains("SERVICE_DATA");
    }

    private AlertVerificationOutcome providerFailure(String alertId, LlmRequest request, RuntimeException ex) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][LLM] provider failure alertId="
                + alertId + " reason=" + shortTechnicalMessage(ex));
        return rejected(
                alertId,
                "Scheduled alert verification could not complete.",
                "Scheduled alert verification could not complete due to a technical AI provider error.",
                null,
                request == null ? null : request.model());
    }

    private AlertVerificationOutcome rejected(
            String alertId,
            String summary,
            String rejectedReason,
            String provider,
            String model) {
        AlertVerificationOutcome outcome = new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                summary,
                rejectedReason,
                0.0,
                provider,
                model,
                PROMPT_VERSION,
                List.of("SERVICE_DATA"),
                "SCHEDULED_INTERPRETER",
                "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                "SCHEDULE",
                "SCHEDULED_SNAPSHOT_MATCH",
                List.of(),
                List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                null,
                null,
                null,
                List.of("ROUTE_INTERPRETER_TYPE=SCHEDULED_INTERPRETER"),
                List.of());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][OUTCOME] alertId=" + alertId
                + " final decision=" + outcome.decision()
                + " technicalSpecificationPresent=false agentBlueprintPreviewPresent=false");
        return outcome;
    }

    private void printOutcome(AlertVerificationOutcome outcome) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][OUTCOME] final decision="
                + (outcome == null ? null : outcome.decision())
                + " technicalSpecificationPresent="
                + (outcome != null && outcome.technicalSpecification() != null)
                + " agentBlueprintPreviewPresent="
                + (outcome != null && outcome.agentBlueprintPreview() != null));
    }

    private int serviceDataStopPointCount(ScheduledServiceDataLocationContext context) {
        return context == null || context.serviceDataApiStopPoints() == null ? 0 : context.serviceDataApiStopPoints().size();
    }

    private String compactStopPoints(ScheduledServiceDataLocationContext context) {
        if (context == null || context.serviceDataApiStopPoints() == null || context.serviceDataApiStopPoints().isEmpty()) {
            return "[]";
        }
        List<String> stopPoints = context.serviceDataApiStopPoints();
        if (stopPoints.size() <= 10) {
            return stopPoints.toString();
        }
        return stopPoints.subList(0, 10) + "...[count=" + stopPoints.size() + "]";
    }

    private int promptLength(String prompt) {
        return prompt == null ? 0 : prompt.length();
    }

    private String safeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Unknown scheduled verification failure.";
        }
        return reason.length() > 500 ? reason.substring(0, 500) : reason;
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
