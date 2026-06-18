package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AlertRouteUnderstandingFallbackClassifier {

    private static final String SERVICE_DATA = "SERVICE_DATA";
    private static final double FALLBACK_CONFIDENCE = 0.70;

    public Optional<AlertRouteUnderstandingResult> classify(
            String alertId,
            String prompt,
            AlertRouteUnderstandingHints hints,
            String technicalReason) {
        AlertRouteUnderstandingHints safeHints = hints == null ? AlertRouteUnderstandingHints.empty() : hints;
        String reason = technicalReason == null || technicalReason.isBlank()
                ? "unknown route LLM failure"
                : technicalReason;
        System.out.println("[IIA][ALERT_ROUTE][FALLBACK] LLM route failed alertId=" + alertId + " reason=" + reason);

        if (hasStrongUnsupportedSignal(safeHints)) {
            System.out.println("[IIA][ALERT_ROUTE][FALLBACK] applicable=true reason=technical-route-failure+unsupported-hints decision=REJECTED");
            return Optional.empty();
        }
        if (isScheduledFallbackApplicable(safeHints)) {
            System.out.println("[IIA][ALERT_ROUTE][FALLBACK] applicable=true reason=technical-route-failure+scheduled-hints decision=SCHEDULED_INTERPRETER"
                    + " alertId=" + alertId
                    + " polling=" + safeHints.containsPollingExpression()
                    + " report=" + safeHints.containsCountOrReportExpression()
                    + " snapshot=" + safeHints.containsSnapshotStateExpression());
            return Optional.of(scheduledRoute(reason, safeHints));
        }
        if (isEventFallbackApplicable(safeHints)) {
            System.out.println("[IIA][ALERT_ROUTE][FALLBACK] applicable=true reason=technical-route-failure+event-hints decision=EVENT_INTERPRETER"
                    + " alertId=" + alertId
                    + " event=" + safeHints.containsEventOccurrenceExpression());
            return Optional.of(eventRoute(reason, safeHints));
        }

        System.out.println("[IIA][ALERT_ROUTE][FALLBACK] not applicable reason=insufficient-hints alertId=" + alertId);
        return Optional.empty();
    }

    private boolean isScheduledFallbackApplicable(AlertRouteUnderstandingHints hints) {
        return hints.containsPollingExpression()
                || hints.containsCountOrReportExpression()
                || hints.containsSnapshotStateExpression()
                || hints.containsCardinalityThresholdExpression()
                || hints.containsAttributeThresholdExpression();
    }

    private boolean isEventFallbackApplicable(AlertRouteUnderstandingHints hints) {
        return !hints.containsPollingExpression()
                && (hints.containsEventOccurrenceExpression()
                || hints.containsPlatformExpression()
                || hints.containsPlatformChangeExpression()
                || hints.containsChangeCancellationExclusionExpression());
    }

    private boolean hasStrongUnsupportedSignal(AlertRouteUnderstandingHints hints) {
        return hints.containsUnsupportedWeatherExpression()
                || hints.containsUnsupportedWifiOrOnboardFeatureExpression()
                || hints.containsGenericQuestionExpression()
                || hints.containsUnsupportedAbsenceOverTimeExpression();
    }

    private AlertRouteUnderstandingResult scheduledRoute(String reason, AlertRouteUnderstandingHints hints) {
        boolean report = hints.containsCountOrReportExpression()
                && !hints.containsCardinalityThresholdExpression()
                && !hints.containsAttributeThresholdExpression();
        String warning = "Route LLM failed with " + reason
                + "; deterministic fallback selected SCHEDULED_INTERPRETER from hints.";
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of(SERVICE_DATA),
                SERVICE_DATA,
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                report ? AlertRouteIntentKind.SNAPSHOT_REPORT : AlertRouteIntentKind.SNAPSHOT_CONDITION,
                report ? AlertRouteOutputMode.EVERY_RUN_REPORT : AlertRouteOutputMode.ON_MATCH,
                true,
                true,
                false,
                true,
                hints.containsCardinalityThresholdExpression(),
                hints.containsCountOrReportExpression(),
                FALLBACK_CONFIDENCE,
                "Deterministic fallback route selected SCHEDULED_INTERPRETER after route LLM failure.",
                null,
                List.of(warning));
    }

    private AlertRouteUnderstandingResult eventRoute(String reason, AlertRouteUnderstandingHints hints) {
        String warning = "Route LLM failed with " + reason
                + "; deterministic fallback selected EVENT_INTERPRETER from hints.";
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                List.of(SERVICE_DATA),
                SERVICE_DATA,
                AlertRouteInterpreterType.EVENT_INTERPRETER,
                AlertRouteAccessMode.KAFKA_EVENT,
                AlertRouteIntentKind.EVENT_CONDITION,
                AlertRouteOutputMode.ON_MATCH,
                false,
                false,
                true,
                false,
                false,
                false,
                FALLBACK_CONFIDENCE,
                "Deterministic fallback route selected EVENT_INTERPRETER after route LLM failure.",
                null,
                List.of(warning));
    }
}
