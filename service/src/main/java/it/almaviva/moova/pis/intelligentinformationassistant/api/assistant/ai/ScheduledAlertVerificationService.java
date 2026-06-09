package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.ProcessingException;

import java.util.List;
import java.util.Map;

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
    ScheduledAlertTemporalHintsExtractor temporalHintsExtractor;

    @Inject
    ScheduledAlertPlatformHintsExtractor platformHintsExtractor;

    @Inject
    ScheduledAlertChangeHintsExtractor changeHintsExtractor;

    @Inject
    ScheduledAlertJourneyCancellationHintsExtractor journeyCancellationHintsExtractor;

    @Inject
    ScheduledAlertCancelledCallHintsExtractor cancelledCallHintsExtractor;

    @Inject
    ScheduledAlertReplacementHintsExtractor replacementHintsExtractor;

    @ConfigProperty(name = "iia.alert-scheduled-verification.prompt-size-warning-threshold", defaultValue = "25000")
    int promptSizeWarningThreshold = 25000;

    @Inject
    ScheduledUnsupportedConstraintDetector unsupportedConstraintDetector;

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
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][REQUEST] alertId=" + alertId
                + " route.intentKind=" + (route == null ? null : route.intentKind())
                + " route.outputMode=" + (route == null ? null : route.outputMode())
                + " monitoringScope=" + (locationContext == null ? null : locationContext.monitoringScope())
                + " serviceDataApiStopPoints.count=" + serviceDataStopPointCount(locationContext)
                + " requiresAllKnownStopPoints=" + (locationContext != null && locationContext.requiresAllKnownStopPoints())
                + " featureFlag.enabled=true");
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

        ScheduledAlertTemporalHints temporalHints = temporalHints(originalPrompt);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][TEMPORAL_HINTS] hasExplicitFrequency="
                + temporalHints.hasExplicitFrequency()
                + " frequencySeconds=" + temporalHints.frequencySeconds()
                + " rawText=" + temporalHints.frequencyRawText());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][TEMPORAL_HINTS] hasExplicitLookaheadWindow="
                + temporalHints.hasExplicitLookaheadWindow()
                + " lookaheadMinutes=" + temporalHints.lookaheadMinutes()
                + " rawText=" + temporalHints.lookaheadRawText());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][TEMPORAL_HINTS] defaults frequencySeconds="
                + temporalHints.defaultFrequencySeconds()
                + " lookaheadMinutes=" + temporalHints.defaultLookaheadMinutes());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][TEMPORAL_HINTS] hasJourneyTimeFilter="
                + temporalHints.hasJourneyTimeFilter()
                + " journeyTimeFilters=" + temporalHints.journeyTimeFilters());
        ScheduledAlertPlatformHints platformHints = platformHints(originalPrompt);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PLATFORM_HINTS] hasPlatformConstraint="
                + platformHints.hasPlatformConstraint()
                + " constraints=" + platformHints.constraints());
        ScheduledAlertChangeHints changeHints = changeHints(originalPrompt);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][CHANGE_HINTS] hasChangeConstraint="
                + changeHints.hasChangeConstraint()
                + " constraints=" + changeHints.constraints());
        ScheduledAlertJourneyCancellationHints journeyCancellationHints = journeyCancellationHints(originalPrompt);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][JOURNEY_CANCELLATION_HINTS] hasJourneyCancellationConstraint="
                + journeyCancellationHints.hasJourneyCancellationConstraint()
                + " constraints=" + journeyCancellationHints.constraints());
        if (journeyCancellationHints.hasGenericJourneyCancellation()) {
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][CANCELLATION_SEMANTIC] intent=GENERIC_JOURNEY_CANCELLATION strategy=statuses-plus-passingType");
        }
        ScheduledAlertCancelledCallHints cancelledCallHints = cancelledCallHints(originalPrompt);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][CANCELLED_CALL_HINTS] hasCancelledCallConstraint="
                + cancelledCallHints.hasCancelledCallConstraint()
                + " constraints=" + cancelledCallHints.constraints());
        ScheduledAlertReplacementHints replacementHints = replacementHints(originalPrompt);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][REPLACEMENT_HINTS] hasReplacementConstraint="
                + replacementHints.hasReplacementConstraint()
                + " constraints=" + replacementHints.constraints());
        String unsupportedReason = unsupportedConstraintReason(originalPrompt);

        LlmRequest request = promptBuilder.build(new ScheduledAlertVerificationPromptData(
                alertId,
                name,
                description,
                originalPrompt,
                route,
                locationContext,
                temporalHints,
                platformHints,
                changeHints,
                journeyCancellationHints,
                cancelledCallHints,
                replacementHints));
        int systemPromptLength = promptLength(request.systemPrompt());
        int userPromptLength = promptLength(request.userPrompt());
        int totalPromptLength = systemPromptLength + userPromptLength;
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PROMPT] systemLength="
                + systemPromptLength
                + " userLength=" + userPromptLength
                + " total=" + totalPromptLength
                + " catalogMode=DYNAMIC"
                + " relevantCapabilities=" + relevantCapabilities(platformHints, changeHints, journeyCancellationHints, cancelledCallHints, replacementHints)
                + " locationContext=" + locationContextSummary(locationContext));
        if (totalPromptLength > promptSizeWarningThreshold) {
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PROMPT][WARN] total prompt length exceeds configured warning threshold: "
                    + totalPromptLength + " > " + promptSizeWarningThreshold);
        }

        try {
            LlmResponse response = llmGateway.get().generateText(request);
            String rawResponse = response == null ? null : response.text();
            String provider = response == null ? null : response.provider();
            String model = response == null ? request.model() : response.model();
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][LLM] raw response="
                    + truncate(rawResponse, 5000));

            ScheduledAlertVerificationResponseParser.ParseResult parseResult =
                    responseParser.parseDetailed(rawResponse, provider, model);
            if (parseResult.outcome().isEmpty()) {
                String reason = parserRejectedReason(parseResult.failureReason());
                System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PARSER] parsed decision=<empty> reason="
                        + reason + " rawLength=" + parseResult.rawLength());
                return rejected(alertId, "Scheduled alert verification response could not be parsed.", reason, provider, model);
            }

            AlertVerificationOutcome parsed = parseResult.outcome().get();
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PARSER] decision=" + parsed.decision()
                    + " technicalSpecificationPresent=" + (parsed.technicalSpecification() != null)
                    + " agentBlueprintPreviewPresent=" + (parsed.agentBlueprintPreview() != null));
            if (parsed.decision() == AlertVerificationDecision.VERIFIED && unsupportedReason != null) {
                System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][UNSUPPORTED] reason=" + unsupportedReason);
                return rejected(
                        alertId,
                        "Scheduled ServiceData verification rejected an unsupported required constraint.",
                        unsupportedReason,
                        parsed.provider(),
                        parsed.model());
            }
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] decision before validation=" + parsed.decision());
            AlertVerificationOutcome validated = outcomeValidator.validate(parsed, locationContext, route, temporalHints, platformHints, changeHints, journeyCancellationHints, cancelledCallHints, replacementHints);
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] final validator decision="
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

    private ScheduledAlertTemporalHints temporalHints(String originalPrompt) {
        ScheduledAlertTemporalHintsExtractor extractor = temporalHintsExtractor;
        if (extractor == null) {
            extractor = new ScheduledAlertTemporalHintsExtractor();
        }
        return extractor.extract(originalPrompt);
    }

    private ScheduledAlertPlatformHints platformHints(String originalPrompt) {
        ScheduledAlertPlatformHintsExtractor extractor = platformHintsExtractor;
        if (extractor == null) {
            extractor = new ScheduledAlertPlatformHintsExtractor();
        }
        return extractor.extract(originalPrompt);
    }

    private ScheduledAlertChangeHints changeHints(String originalPrompt) {
        ScheduledAlertChangeHintsExtractor extractor = changeHintsExtractor;
        if (extractor == null) {
            extractor = new ScheduledAlertChangeHintsExtractor();
        }
        return extractor.extract(originalPrompt);
    }

    private ScheduledAlertJourneyCancellationHints journeyCancellationHints(String originalPrompt) {
        ScheduledAlertJourneyCancellationHintsExtractor extractor = journeyCancellationHintsExtractor;
        if (extractor == null) {
            extractor = new ScheduledAlertJourneyCancellationHintsExtractor();
        }
        return extractor.extract(originalPrompt);
    }

    private ScheduledAlertCancelledCallHints cancelledCallHints(String originalPrompt) {
        ScheduledAlertCancelledCallHintsExtractor extractor = cancelledCallHintsExtractor;
        if (extractor == null) {
            extractor = new ScheduledAlertCancelledCallHintsExtractor();
        }
        return extractor.extract(originalPrompt);
    }

    private ScheduledAlertReplacementHints replacementHints(String originalPrompt) {
        ScheduledAlertReplacementHintsExtractor extractor = replacementHintsExtractor;
        if (extractor == null) {
            extractor = new ScheduledAlertReplacementHintsExtractor();
        }
        return extractor.extract(originalPrompt);
    }

    private String unsupportedConstraintReason(String originalPrompt) {
        ScheduledUnsupportedConstraintDetector detector = unsupportedConstraintDetector;
        if (detector == null) {
            detector = new ScheduledUnsupportedConstraintDetector();
        }
        return detector.rejectionReason(originalPrompt);
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
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][ERROR] provider failure alertId="
                + alertId + " reason=" + shortTechnicalMessage(ex));
        AlertVerificationOutcome outcome = technicalErrorOutcome(request == null ? null : request.model());
        printOutcome(outcome);
        return outcome;
    }

    private AlertVerificationOutcome technicalErrorOutcome(String model) {
        return new AlertVerificationOutcome(
                AlertVerificationDecision.ERROR,
                "Alert verification could not complete because the AI provider was unavailable or returned an invalid technical response.",
                null,
                0.0,
                null,
                model,
                PROMPT_VERSION,
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                Map.of(
                        "schemaVersion", "iia.alert.technical-specification/v2",
                        "decision", "ERROR",
                        "error", "Scheduled alert verification could not complete due to a technical AI provider error."),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "canGenerate", false,
                        "error", "Scheduled alert verification could not complete due to a technical AI provider error."),
                Map.of(
                        "requirements", List.of(),
                        "allRequiredRequirementsMapped", false),
                List.of("Scheduled alert verification could not complete due to a technical AI provider error."),
                List.of(
                        "No executable code generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
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
                + " summary=" + (outcome == null ? null : outcome.summary())
                + " rejectedReason=" + (outcome == null ? null : outcome.rejectedReason())
                + " technicalSpecificationPresent="
                + (outcome != null && outcome.technicalSpecification() != null)
                + " agentBlueprintPreviewPresent="
                + (outcome != null && outcome.agentBlueprintPreview() != null));
    }

    private int serviceDataStopPointCount(ScheduledServiceDataLocationContext context) {
        return context == null || context.serviceDataApiStopPoints() == null ? 0 : context.serviceDataApiStopPoints().size();
    }

    private List<String> relevantCapabilities(
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints,
            ScheduledAlertJourneyCancellationHints journeyCancellationHints,
            ScheduledAlertCancelledCallHints cancelledCallHints,
            ScheduledAlertReplacementHints replacementHints) {
        java.util.ArrayList<String> capabilities = new java.util.ArrayList<>();
        capabilities.add("QUERY");
        capabilities.add("REPORT_OUTPUT");
        if (changeHints != null && changeHints.hasChangeConstraint()) {
            capabilities.add("CANCELLATION");
        }
        if (journeyCancellationHints != null && journeyCancellationHints.hasJourneyCancellationConstraint()) {
            capabilities.add("JOURNEY_CANCELLATION");
        }
        if (platformHints != null && platformHints.hasPlatformConstraint()) {
            capabilities.add("PLATFORM");
        }
        if (cancelledCallHints != null && cancelledCallHints.hasCancelledCallConstraint()) {
            capabilities.add("CANCELLED_CALL");
        }
        if (replacementHints != null && replacementHints.hasReplacementConstraint()) {
            capabilities.add("REPLACEMENT");
        }
        return capabilities;
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

    private String locationContextSummary(ScheduledServiceDataLocationContext context) {
        if (context == null) {
            return "null";
        }
        return "monitoringScope=" + context.monitoringScope()
                + ", monitoredCount=" + (context.monitoredLocations() == null ? 0 : context.monitoredLocations().size())
                + ", filterCount=" + (context.filterLocations() == null ? 0 : context.filterLocations().size())
                + ", serviceDataApiStopPoints.count=" + serviceDataStopPointCount(context)
                + ", requiresAllKnownStopPoints=" + context.requiresAllKnownStopPoints();
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

    private String parserRejectedReason(String failureReason) {
        String safe = safeReason(failureReason);
        if (safe.toLowerCase(java.util.Locale.ROOT).contains("json")) {
            return "Scheduled alert verification failed because the LLM response was not valid JSON.";
        }
        return "Scheduled alert verification failed because the LLM response did not match the required Scheduled verification contract: "
                + safe;
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
