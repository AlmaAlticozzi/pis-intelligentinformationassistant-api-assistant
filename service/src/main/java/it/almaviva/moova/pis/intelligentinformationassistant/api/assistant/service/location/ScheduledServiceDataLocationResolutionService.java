package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationMention;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationPolarity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertMonitoringScope;
import jakarta.enterprise.context.ApplicationScoped;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ApplicationScoped
public class ScheduledServiceDataLocationResolutionService {

    private final PointResolver pointResolver;

    public ScheduledServiceDataLocationResolutionService() {
        this(new PointResolver());
    }

    public ScheduledServiceDataLocationResolutionService(PointResolver pointResolver) {
        this.pointResolver = pointResolver;
    }

    public ScheduledServiceDataLocationContext resolve(ScheduledAlertLocationUnderstandingResult understandingResult) {
        ScheduledAlertLocationUnderstandingResult safeResult = understandingResult == null
                ? new ScheduledAlertLocationUnderstandingResult(
                false,
                "",
                ScheduledAlertMonitoringScope.UNSPECIFIED,
                List.of(),
                List.of(),
                List.of())
                : understandingResult;
        ScheduledAlertMonitoringScope monitoringScope = safeResult.monitoringScope();
        boolean requiresAllKnownStopPoints = monitoringScope == ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS;

        List<String> warnings = new ArrayList<>(safeResult.warnings());
        List<ScheduledServiceDataResolvedLocation> monitoredLocations = new ArrayList<>();
        List<ScheduledServiceDataResolvedLocation> filterLocations = new ArrayList<>();
        List<ScheduledServiceDataResolvedLocation> excludedLocations = new ArrayList<>();
        Set<String> serviceDataApiStopPoints = new LinkedHashSet<>();
        List<String> unresolvedRequiredMonitoredTexts = new ArrayList<>();

        if (requiresAllKnownStopPoints) {
            warnings.add("All known stop points scope requested; ids will be materialized by runtime or later verification phase.");
        }

        for (ScheduledAlertLocationMention mention : safeResult.locations()) {
            if (isPseudoJourneyCancellationFilterLocation(mention.role(), mention.rawText())) {
                System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][FILTER_SANITIZER] action=dropped_pseudo_location rawText="
                        + mention.rawText()
                        + " role="
                        + mention.role()
                        + " reason=journey_cancellation_intent_not_location");
                continue;
            }
            ScheduledServiceDataResolvedLocation resolved = resolveMention(mention, monitoringScope);
            if (!resolved.warningReason().isBlank()) {
                warnings.add(resolved.warningReason());
            }

            if (isMonitored(mention.role())) {
                monitoredLocations.add(resolved);
                if (!requiresAllKnownStopPoints
                        && resolved.resolutionStatus() == ScheduledServiceDataLocationResolutionStatus.UNRESOLVED
                        && mention.requiredCoverage()) {
                    unresolvedRequiredMonitoredTexts.add(mention.rawText());
                }
                if (!requiresAllKnownStopPoints
                        && resolved.resolutionStatus() != ScheduledServiceDataLocationResolutionStatus.NOT_APPLICABLE) {
                    serviceDataApiStopPoints.addAll(resolved.selectedPointIds());
                }
            } else {
                filterLocations.add(resolved);
            }

            if (mention.polarity() == ScheduledAlertLocationPolarity.EXCLUDE
                    || mention.role() == ScheduledAlertLocationRole.EXCLUDED_STOP_POINT) {
                excludedLocations.add(resolved);
            }

            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][RESOLUTION] rawText="
                    + resolved.rawText()
                    + " role="
                    + resolved.scheduledRole()
                    + " status="
                    + resolved.resolutionStatus()
                    + " selectedIds="
                    + resolved.selectedPointIds());
        }

        List<String> apiStopPoints = List.copyOf(serviceDataApiStopPoints);
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][API_QUERY] monitoringScope="
                + monitoringScope
                + " requiresAllKnownStopPoints="
                + requiresAllKnownStopPoints
                + " serviceDataApiStopPoints="
                + apiStopPoints);
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][API_QUERY] unresolvedRequiredMonitoredStopPoints="
                + unresolvedRequiredMonitoredTexts);
        System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][CONTEXT] monitoringScope="
                + monitoringScope
                + " monitoredCount="
                + monitoredLocations.size()
                + " filterCount="
                + filterLocations.size()
                + " serviceDataApiStopPointCount="
                + apiStopPoints.size()
                + " unresolvedMonitored="
                + unresolvedRequiredMonitoredTexts);

        return new ScheduledServiceDataLocationContext(
                monitoringScope,
                monitoredLocations,
                filterLocations,
                excludedLocations,
                apiStopPoints,
                requiresAllKnownStopPoints,
                !unresolvedRequiredMonitoredTexts.isEmpty(),
                unresolvedRequiredMonitoredTexts,
                warnings,
                new ScheduledServiceDataApiQueryContext(monitoringScope, apiStopPoints, requiresAllKnownStopPoints));
    }

    private ScheduledServiceDataResolvedLocation resolveMention(
            ScheduledAlertLocationMention mention,
            ScheduledAlertMonitoringScope monitoringScope) {
        List<String> targetFieldHints = targetFieldHints(mention.role());
        if (isPlatformLike(mention.rawText())) {
            String warning = "Location mention '" + mention.rawText()
                    + "' looks like a platform/binario constraint and was not resolved as a stop point.";
            return new ScheduledServiceDataResolvedLocation(
                    mention.rawText(),
                    mention.normalizedText(),
                    mention.role(),
                    mention.polarity(),
                    false,
                    mention.requiredCoverage(),
                    ScheduledServiceDataLocationResolutionStatus.NOT_APPLICABLE,
                    List.of(),
                    List.of(),
                    false,
                    false,
                    targetFieldHints,
                    warning);
        }

        if (monitoringScope == ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS
                && isMonitored(mention.role())) {
            return new ScheduledServiceDataResolvedLocation(
                    mention.rawText(),
                    mention.normalizedText(),
                    mention.role(),
                    mention.polarity(),
                    mention.requiredForApiQuery(),
                    mention.requiredCoverage(),
                    ScheduledServiceDataLocationResolutionStatus.NOT_APPLICABLE,
                    List.of(),
                    List.of(),
                    false,
                    false,
                    targetFieldHints,
                    "");
        }

        PointResolutionResult result = pointResolver.resolve(mention.rawText());
        List<String> selectedPointIds = selectedPointIds(result);
        ScheduledServiceDataLocationResolutionStatus status = mapStatus(result.status());
        boolean monitored = isMonitored(mention.role());
        boolean fallbackAllowed = !monitored
                && status == ScheduledServiceDataLocationResolutionStatus.UNRESOLVED
                && supportsTextualFallback(mention.role());
        boolean fallbackToNameLong = fallbackAllowed;
        String warning = warningReason(mention, status, fallbackAllowed);

        return new ScheduledServiceDataResolvedLocation(
                mention.rawText(),
                result.normalizedText() == null || result.normalizedText().isBlank()
                        ? mention.normalizedText()
                        : result.normalizedText(),
                mention.role(),
                mention.polarity(),
                monitored && mention.requiredForApiQuery(),
                mention.requiredCoverage(),
                status,
                selectedPointIds,
                result.candidates(),
                fallbackToNameLong,
                fallbackAllowed,
                targetFieldHints,
                warning);
    }

    private List<String> selectedPointIds(PointResolutionResult result) {
        if (result.status() == PointResolutionStatus.UNRESOLVED) {
            return List.of();
        }
        if (result.status() == PointResolutionStatus.RESOLVED_AMBIGUOUS) {
            return result.candidates().stream()
                    .map(PointCandidate::id)
                    .toList();
        }
        return result.candidates().stream()
                .filter(PointCandidate::selected)
                .map(PointCandidate::id)
                .toList();
    }

    private ScheduledServiceDataLocationResolutionStatus mapStatus(PointResolutionStatus status) {
        return switch (status) {
            case RESOLVED -> ScheduledServiceDataLocationResolutionStatus.RESOLVED;
            case RESOLVED_AMBIGUOUS -> ScheduledServiceDataLocationResolutionStatus.RESOLVED_AMBIGUOUS;
            case UNRESOLVED -> ScheduledServiceDataLocationResolutionStatus.UNRESOLVED;
        };
    }

    private String warningReason(
            ScheduledAlertLocationMention mention,
            ScheduledServiceDataLocationResolutionStatus status,
            boolean fallbackAllowed) {
        if (status != ScheduledServiceDataLocationResolutionStatus.UNRESOLVED) {
            return "";
        }
        if (isMonitored(mention.role())) {
            return "Monitored stop point '" + mention.rawText()
                    + "' could not be resolved to a ServiceData stop point id.";
        }
        if (fallbackAllowed) {
            return "Filter stop point '" + mention.rawText()
                    + "' could not be resolved; textual name fallback is allowed for future scheduled verification.";
        }
        return "Filter stop point '" + mention.rawText()
                + "' could not be resolved and no textual fallback is available for role "
                + mention.role()
                + ".";
    }

    private boolean isMonitored(ScheduledAlertLocationRole role) {
        return role == ScheduledAlertLocationRole.MONITORED_STOP_POINT;
    }

    private boolean supportsTextualFallback(ScheduledAlertLocationRole role) {
        return switch (role) {
            case FILTER_CURRENT_STOP_POINT,
                 FILTER_ORIGIN_STOP_POINT,
                 FILTER_TIMETABLED_ORIGIN_STOP_POINT,
                 FILTER_DESTINATION_STOP_POINT,
                 FILTER_TIMETABLED_DESTINATION_STOP_POINT,
                 FILTER_ROUTE_STOP_POINT,
                 FILTER_TRANSIT_STOP_POINT,
                 FILTER_CANCELLED_CALL_STOP_POINT,
                 EXCLUDED_STOP_POINT -> true;
            case FILTER_REPLACEMENT_STOP_POINT,
                 FILTER_REPLACEMENT_SOURCE_START_STOP_POINT,
                 FILTER_REPLACEMENT_SOURCE_END_STOP_POINT,
                 MONITORED_STOP_POINT,
                 UNKNOWN_LOCATION_ROLE -> false;
        };
    }

    private List<String> targetFieldHints(ScheduledAlertLocationRole role) {
        return switch (role) {
            case MONITORED_STOP_POINT -> List.of("body.stopPoints[]");
            case FILTER_CURRENT_STOP_POINT -> List.of("stopPoint.id", "stopPoint.nameLong");
            case FILTER_ORIGIN_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].callStart.stopPoint.id",
                    "stopPointsJourneyDetails[].callStart.stopPoint.nameLong");
            case FILTER_TIMETABLED_ORIGIN_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id",
                    "stopPointsJourneyDetails[].timetabledCallStart.stopPoint.nameLong");
            case FILTER_DESTINATION_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].callEnd.stopPoint.id",
                    "stopPointsJourneyDetails[].callEnd.stopPoint.nameLong");
            case FILTER_TIMETABLED_DESTINATION_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id",
                    "stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.nameLong");
            case FILTER_ROUTE_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].nextCalls[].stopPoint.id",
                    "stopPointsJourneyDetails[].nextCalls[].stopPoint.nameLong");
            case FILTER_TRANSIT_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id",
                    "stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.nameLong");
            case FILTER_CANCELLED_CALL_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id",
                    "stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.nameLong");
            case FILTER_REPLACEMENT_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id",
                    "stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[].stopPointId.id");
            case FILTER_REPLACEMENT_SOURCE_START_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].isReplacementOf[].timetabledCallStart",
                    "future mapping requires catalog confirmation");
            case FILTER_REPLACEMENT_SOURCE_END_STOP_POINT -> List.of(
                    "stopPointsJourneyDetails[].isReplacementOf[].timetabledCallEnd",
                    "future mapping requires catalog confirmation");
            case EXCLUDED_STOP_POINT, UNKNOWN_LOCATION_ROLE -> List.of();
        };
    }

    private boolean isPlatformLike(String rawText) {
        String normalized = normalize(rawText);
        if (normalized == null) {
            return false;
        }
        return normalized.matches(".*\\b(binario|platform|track|quay|banchina|marciapiede)\\b.*");
    }

    private boolean isPseudoJourneyCancellationFilterLocation(
            ScheduledAlertLocationRole role,
            String rawText) {
        if (role != ScheduledAlertLocationRole.FILTER_CANCELLED_CALL_STOP_POINT) {
            return false;
        }
        String normalized = normalize(rawText);
        if (normalized == null) {
            return false;
        }
        if (normalized.matches(".*\\b(fermata|fermate|stop|stops|station|stations|stazione|stazioni|call|calls)\\b.*")) {
            return false;
        }
        boolean journeyWord = normalized.matches(".*\\b(corsa|corse|treno|treni|servizio|servizi|journey|journeys|train|trains|service|services)\\b.*");
        boolean cancellationWord = normalized.matches(".*\\b(soppressa|soppresse|soppresso|soppressi|cancellata|cancellate|cancellato|cancellati|cancellazione|cancellazioni|suppressed|cancelled|canceled|cancellation|cancellations)\\b.*");
        return journeyWord && cancellationWord;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
