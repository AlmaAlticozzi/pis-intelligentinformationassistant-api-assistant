package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class AlertRouteUnderstandingValidator {

    private static final String SERVICE_DATA = "SERVICE_DATA";

    public AlertRouteUnderstandingResult validate(AlertRouteUnderstandingResult route) {
        return validate(route, null);
    }

    public AlertRouteUnderstandingResult validate(AlertRouteUnderstandingResult route, String originalPrompt) {
        return validate(route, originalPrompt, AlertRouteUnderstandingHints.fromPrompt(originalPrompt));
    }

    public AlertRouteUnderstandingResult validate(
            AlertRouteUnderstandingResult route,
            String originalPrompt,
            AlertRouteUnderstandingHints hints) {
        AlertRouteUnderstandingHints safeHints = hints == null ? AlertRouteUnderstandingHints.empty() : hints;
        if (route == null) {
            return rejected("Alert route understanding did not produce a result.");
        }

        String defensiveReason = defensiveUnsupportedPromptReason(originalPrompt, safeHints, route);
        if (defensiveReason != null) {
            return rejected(defensiveReason);
        }

        if (route.decision() == AlertRouteDecision.REJECTED) {
            return ensureRejectedReason(route);
        }
        if (route.decision() != AlertRouteDecision.ROUTED) {
            return rejected("Alert route understanding returned an unsupported decision.");
        }

        List<String> domains = normalizedDomains(route.dataDomains());
        if (domains.isEmpty()) {
            return rejected("Routed alert is missing dataDomains.");
        }
        if (domains.stream().anyMatch(domain -> !SERVICE_DATA.equals(domain))) {
            return rejected("Only SERVICE_DATA alerts are currently supported by Alert Route Understanding.");
        }
        String primaryDomain = normalize(route.primaryDataDomain());
        if (primaryDomain == null) {
            return rejected("Routed alert is missing primaryDataDomain.");
        }
        if (!domains.contains(primaryDomain)) {
            return rejected("Routed alert primaryDataDomain must be included in dataDomains.");
        }
        if (!SERVICE_DATA.equals(primaryDomain)) {
            return rejected("Only SERVICE_DATA can be used as primaryDataDomain.");
        }
        if (route.confidence() == null || route.confidence() < 0.0 || route.confidence() > 1.0) {
            return rejected("Routed alert confidence must be between 0 and 1.");
        }

        if (route.interpreterType() == AlertRouteInterpreterType.EVENT_INTERPRETER) {
            if (requiresScheduledInterpreter(safeHints)) {
                if (isAlternativeEventMonitoring(originalPrompt, safeHints)) {
                    logMultiLocationSemantic(originalPrompt, safeHints, AlertRouteInterpreterType.EVENT_INTERPRETER);
                    return validateEventRoute(route, domains, primaryDomain);
                }
                return normalizeEventRouteToScheduled(route, domains, primaryDomain, safeHints);
            }
            return validateEventRoute(route, domains, primaryDomain);
        }
        if (route.interpreterType() == AlertRouteInterpreterType.SCHEDULED_INTERPRETER) {
            if (shouldNormalizeScheduledRouteToEvent(originalPrompt, safeHints)) {
                return normalizeScheduledRouteToEvent(route, domains, primaryDomain, safeHints,
                        isAlternativeEventMonitoring(originalPrompt, safeHints));
            }
            if (shouldNormalizeScheduledConditionToReport(route, safeHints)) {
                return normalizeScheduledConditionToReport(route, domains, primaryDomain);
            }
            System.out.println("[IIA][ALERT_ROUTE][NORMALIZATION] action=KEEP_SCHEDULED reason=count-report-polling-cross-location alertId=n/a");
            return validateScheduledRoute(route, domains, primaryDomain);
        }
        return rejected("Routed alert requires EVENT_INTERPRETER or SCHEDULED_INTERPRETER.");
    }

    private boolean requiresScheduledInterpreter(AlertRouteUnderstandingHints hints) {
        return hints.containsPollingExpression()
                || hints.stronglyIndicatesAggregateSnapshot()
                || hints.containsAllLocationsExpression()
                || (hints.containsCardinalityThresholdExpression() && hints.containsSnapshotStateExpression())
                || (hints.containsSnapshotStateExpression() && !hints.containsEventOccurrenceExpression())
                || (hints.containsSnapshotStateExpression()
                && hints.containsPlatformChangeExpression()
                && !hints.containsEventOccurrenceExpression())
                || (hints.containsSnapshotStateExpression()
                && hints.containsChangeCancellationExclusionExpression()
                && !hints.containsEventOccurrenceExpression());
    }

    private AlertRouteUnderstandingResult normalizeEventRouteToScheduled(
            AlertRouteUnderstandingResult route,
            List<String> domains,
            String primaryDomain,
            AlertRouteUnderstandingHints hints) {
        String warning = "Route normalized from EVENT_INTERPRETER to SCHEDULED_INTERPRETER because backend hints detected aggregate snapshot/cardinality semantics.";
        System.out.println("[IIA][ALERT_ROUTE][NORMALIZATION] from=EVENT_INTERPRETER to=SCHEDULED_INTERPRETER reason=aggregate snapshot/cardinality semantics");
        List<String> warnings = mergeWarning(route.warnings(), warning);
        boolean report = hints.containsCountOrReportExpression() || route.hasReportIntent();
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                domains,
                primaryDomain,
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                report ? AlertRouteIntentKind.SNAPSHOT_REPORT : AlertRouteIntentKind.SNAPSHOT_CONDITION,
                report ? AlertRouteOutputMode.EVERY_RUN_REPORT : AlertRouteOutputMode.ON_MATCH,
                true,
                true,
                false,
                true,
                route.hasCardinalityThreshold() || hints.containsCardinalityThresholdExpression(),
                report,
                route.confidence(),
                route.summary(),
                route.rejectedReason(),
                warnings);
    }

    private boolean shouldNormalizeScheduledRouteToEvent(String originalPrompt, AlertRouteUnderstandingHints hints) {
        if (isAlternativeEventMonitoring(originalPrompt, hints)) {
            logMultiLocationSemantic(originalPrompt, hints, AlertRouteInterpreterType.EVENT_INTERPRETER);
            return true;
        }
        boolean hasScheduledSignal = hints.containsPollingExpression()
                || hints.containsCountOrReportExpression()
                || hints.containsSnapshotStateExpression()
                || hints.containsMultiMonitoredLocationExpression()
                || hints.containsCardinalityThresholdExpression();
        return !hasScheduledSignal
                && (hints.containsEventOccurrenceExpression() || looksLikeSingleEventOccurrence(originalPrompt))
                && (hints.containsAttributeThresholdExpression()
                || hints.containsPlatformExpression()
                || looksLikeSingleEventOccurrence(originalPrompt));
    }

    private AlertRouteUnderstandingResult normalizeScheduledRouteToEvent(
            AlertRouteUnderstandingResult route,
            List<String> domains,
            String primaryDomain,
            AlertRouteUnderstandingHints hints,
            boolean alternativeEventMonitoring) {
        boolean attributeThreshold = hints.containsAttributeThresholdExpression();
        AlertRouteIntentKind intentKind = attributeThreshold
                ? AlertRouteIntentKind.EVENT_CONDITION
                : AlertRouteIntentKind.EVENT_OCCURRENCE;
        String reason = attributeThreshold
                ? "event_occurrence_with_attribute_threshold_no_snapshot"
                : "event_occurrence_no_snapshot";
        String warning = "Route normalized from SCHEDULED_INTERPRETER to EVENT_INTERPRETER because backend hints detected event occurrence and no snapshot/count/polling semantics.";
        if (alternativeEventMonitoring) {
            System.out.println("[IIA][ALERT_ROUTE][NORMALIZATION] action=RECLASSIFIED_SCHEDULED_TO_EVENT reason=multi-location-alternative-on-match alertId=n/a");
        } else {
            System.out.println("[IIA][ALERT_ROUTE][VALIDATOR_NORMALIZE] scheduled_to_event reason=" + reason);
        }
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                domains,
                primaryDomain,
                AlertRouteInterpreterType.EVENT_INTERPRETER,
                AlertRouteAccessMode.KAFKA_EVENT,
                intentKind,
                AlertRouteOutputMode.ON_MATCH,
                false,
                false,
                true,
                false,
                false,
                false,
                route.confidence(),
                route.summary(),
                route.rejectedReason(),
                mergeWarning(route.warnings(), warning));
    }

    private boolean shouldNormalizeScheduledConditionToReport(
            AlertRouteUnderstandingResult route,
            AlertRouteUnderstandingHints hints) {
        return hints.containsCountOrReportExpression()
                && !hints.containsCardinalityThresholdExpression()
                && (route.intentKind() != AlertRouteIntentKind.SNAPSHOT_REPORT
                || route.outputMode() != AlertRouteOutputMode.EVERY_RUN_REPORT
                || route.hasCardinalityThreshold()
                || !route.hasReportIntent());
    }

    private AlertRouteUnderstandingResult normalizeScheduledConditionToReport(
            AlertRouteUnderstandingResult route,
            List<String> domains,
            String primaryDomain) {
        String warning = "Route normalized to SNAPSHOT_REPORT because backend hints detected count/report intent without threshold semantics.";
        System.out.println("[IIA][ALERT_ROUTE][NORMALIZATION] intent=SNAPSHOT_REPORT outputMode=EVERY_RUN_REPORT reason=count/report intent without threshold");
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                domains,
                primaryDomain,
                AlertRouteInterpreterType.SCHEDULED_INTERPRETER,
                AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT,
                AlertRouteIntentKind.SNAPSHOT_REPORT,
                AlertRouteOutputMode.EVERY_RUN_REPORT,
                true,
                true,
                false,
                true,
                false,
                true,
                route.confidence(),
                route.summary(),
                route.rejectedReason(),
                mergeWarning(route.warnings(), warning));
    }

    private AlertRouteUnderstandingResult validateEventRoute(
            AlertRouteUnderstandingResult route,
            List<String> domains,
            String primaryDomain) {
        if (route.accessMode() != AlertRouteAccessMode.KAFKA_EVENT) {
            return rejected("EVENT_INTERPRETER requires accessMode KAFKA_EVENT.");
        }
        if (!route.requiresKafkaEvent()) {
            return rejected("EVENT_INTERPRETER requires requiresKafkaEvent=true.");
        }
        if (route.requiresServiceDataApi()) {
            return rejected("EVENT_INTERPRETER requires requiresServiceDataApi=false.");
        }
        if (route.requiresPolling()) {
            return rejected("EVENT_INTERPRETER requires requiresPolling=false.");
        }
        return rebuild(route, domains, primaryDomain, route.warnings());
    }

    private AlertRouteUnderstandingResult validateScheduledRoute(
            AlertRouteUnderstandingResult route,
            List<String> domains,
            String primaryDomain) {
        if (route.accessMode() != AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT) {
            return rejected("SCHEDULED_INTERPRETER requires accessMode SERVICE_DATA_API_SNAPSHOT.");
        }
        if (!route.requiresPolling()) {
            return rejected("SCHEDULED_INTERPRETER requires requiresPolling=true.");
        }
        if (!route.requiresServiceDataApi()) {
            return rejected("SCHEDULED_INTERPRETER requires requiresServiceDataApi=true.");
        }
        if (route.requiresKafkaEvent()) {
            return rejected("SCHEDULED_INTERPRETER requires requiresKafkaEvent=false.");
        }
        return rebuild(route, domains, primaryDomain, route.warnings());
    }

    private AlertRouteUnderstandingResult ensureRejectedReason(AlertRouteUnderstandingResult route) {
        String reason = route.rejectedReason();
        if (reason == null || reason.isBlank()) {
            reason = "Alert route understanding rejected the prompt because it is outside supported domains.";
        }
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.REJECTED,
                List.of(),
                null,
                AlertRouteInterpreterType.UNKNOWN,
                AlertRouteAccessMode.NONE,
                AlertRouteIntentKind.UNSUPPORTED,
                AlertRouteOutputMode.NONE,
                false,
                false,
                false,
                false,
                false,
                false,
                0.0,
                route.summary() == null ? "Alert route understanding rejected the prompt." : route.summary(),
                reason,
                mergeWarning(route.warnings(), reason));
    }

    private AlertRouteUnderstandingResult rebuild(
            AlertRouteUnderstandingResult route,
            List<String> domains,
            String primaryDomain,
            List<String> warnings) {
        return new AlertRouteUnderstandingResult(
                AlertRouteDecision.ROUTED,
                domains,
                primaryDomain,
                route.interpreterType(),
                route.accessMode(),
                route.intentKind(),
                route.outputMode(),
                route.requiresPolling(),
                route.requiresServiceDataApi(),
                route.requiresKafkaEvent(),
                route.hasAggregation(),
                route.hasCardinalityThreshold(),
                route.hasReportIntent(),
                route.confidence(),
                route.summary(),
                route.rejectedReason(),
                warnings == null ? List.of() : List.copyOf(warnings));
    }

    private AlertRouteUnderstandingResult rejected(String reason) {
        return AlertRouteUnderstandingResult.rejected(reason);
    }

    private boolean isAlternativeEventMonitoring(String prompt, AlertRouteUnderstandingHints hints) {
        String normalized = normalizeText(prompt);
        if (normalized == null) {
            return false;
        }
        boolean hasEventNotification = hints.containsEventOccurrenceExpression()
                || looksLikeSingleEventOccurrence(prompt)
                || containsAny(normalized, "avvisami quando", "avvertimi quando", "notify me when", "tell me when");
        if (!hasEventNotification || !hasAlternativeLocationList(normalized)) {
            return false;
        }
        return !hints.containsPollingExpression()
                && !hints.containsCountOrReportExpression()
                && !hints.containsCardinalityThresholdExpression()
                && !hints.containsAllLocationsExpression()
                && !hasCrossLocationCondition(normalized);
    }

    private boolean hasAlternativeLocationList(String normalized) {
        return containsAny(normalized, " o ", " oppure ", " or ", " any of ")
                || java.util.regex.Pattern.compile("\\b(?:a|at)\\s+[^,]+,\\s*[^,]+(?:,|\\s+(?:o|or)\\s+)")
                .matcher(normalized)
                .find();
    }

    private boolean hasCrossLocationCondition(String normalized) {
        return containsAny(normalized,
                "tutte le localita", "in tutte", "all locations", "all stop points", "every stop point",
                "contemporaneamente", "allo stesso tempo", "simultaneously", "at the same time",
                "tra queste localita", "across these locations", "numero totale", "totale", "total");
    }

    private void logMultiLocationSemantic(
            String prompt,
            AlertRouteUnderstandingHints hints,
            AlertRouteInterpreterType selectedInterpreter) {
        String normalized = normalizeText(prompt);
        boolean hasCrossLocationCondition = normalized != null && hasCrossLocationCondition(normalized);
        System.out.println("[IIA][ALERT_ROUTE][MULTI_LOCATION_SEMANTIC] locationsMode=ALTERNATIVE_EVENT_MONITORING"
                + " hasCrossLocationCondition=" + hasCrossLocationCondition
                + " hasPolling=" + hints.containsPollingExpression()
                + " hasCountOrReport=" + hints.containsCountOrReportExpression()
                + " selectedInterpreter=" + selectedInterpreter);
    }

    private List<String> normalizedDomains(List<String> domains) {
        if (domains == null) {
            return List.of();
        }
        return domains.stream()
                .map(this::normalize)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private List<String> mergeWarning(List<String> warnings, String reason) {
        List<String> merged = new ArrayList<>(warnings == null ? List.of() : warnings);
        if (reason != null && !reason.isBlank() && !merged.contains(reason)) {
            merged.add(reason);
        }
        return List.copyOf(merged);
    }

    private String defensiveUnsupportedPromptReason(
            String prompt,
            AlertRouteUnderstandingHints hints,
            AlertRouteUnderstandingResult route) {
        if (hints.containsUnsupportedWeatherExpression()) {
            return "Weather alerts are outside the currently supported ServiceData routing domain.";
        }
        if (hints.containsGenericQuestionExpression()) {
            return "Generic Q&A prompts are outside the currently supported ServiceData routing domain.";
        }
        if (hints.containsUnsupportedWifiOrOnboardFeatureExpression()) {
            return "Onboard wifi is not a controlled ServiceData capability for Alert Route Understanding.";
        }
        String normalized = normalizeText(String.join(" ",
                prompt == null ? "" : prompt,
                route == null || route.summary() == null ? "" : route.summary(),
                route == null || route.rejectedReason() == null ? "" : route.rejectedReason(),
                route == null || route.warnings() == null ? "" : String.join(" ", route.warnings())));
        if (normalized == null) {
            return null;
        }
        if (containsWeatherTerm(normalized)) {
            return "Weather alerts are outside the currently supported ServiceData routing domain.";
        }
        if (containsAny(normalized, "quanto fa", "2+2", "what is", "generic question")) {
            return "Generic Q&A prompts are outside the currently supported ServiceData routing domain.";
        }
        if (containsAny(normalized, "wifi", "wi-fi", "wireless")) {
            return "Onboard wifi is not a controlled ServiceData capability for Alert Route Understanding.";
        }
        if (containsAny(normalized,
                "carriage", "carriages", "coach", "coaches", "composizione", "carrozze", "vagoni",
                "comfort", "aria condizionata", "air conditioning", "posti", "seat", "seats",
                "passenger profile", "passenger profiles", "profilo passeggero", "profili passeggeri",
                "ticket", "ticketing", "biglietto", "biglietteria",
                "device", "devices", "display", "dispositivo", "stato dispositivo",
                "audio", "messaggio audio", "audio message",
                "cms", "content", "contenuto",
                "broadcast", "broadcast history", "storico broadcast")) {
            return "The route contains unsupported constraints for this phase and cannot be partially accepted as SERVICE_DATA.";
        }
        return null;
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsWeatherTerm(String value) {
        return java.util.regex.Pattern.compile(
                        "\\b(piove|pioggia|rain|raining|weather|meteo|snow|nevica|temporale|vento|wind)\\b")
                .matcher(value)
                .find();
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private boolean looksLikeSingleEventOccurrence(String prompt) {
        String normalized = normalizeText(prompt);
        if (normalized == null) {
            return false;
        }
        return containsAny(normalized,
                "arriva", "arrivo", "arrivi", "arrivera", "arrival", "arrive", "arrives",
                "parte", "parta", "partenza", "partira", "departure", "depart", "departs",
                "ritardo", "delayed", "delay",
                "cancell", "soppresso", "soppressa");
    }
}
