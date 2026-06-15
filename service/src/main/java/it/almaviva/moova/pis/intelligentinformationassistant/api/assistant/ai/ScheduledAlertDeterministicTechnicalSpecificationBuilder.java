package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ScheduledAlertDeterministicTechnicalSpecificationBuilder {

    private static final String TECHNICAL_SCHEMA_VERSION = "iia.alert.technical-specification/v2";
    private static final String BLUEPRINT_SCHEMA_VERSION = "iia.agent.blueprint/v1";
    private static final String SERVICE_DATA = "SERVICE_DATA";
    private static final String SCHEDULED_INTERPRETER = "SCHEDULED_INTERPRETER";
    private static final String SERVICE_DATA_API_SNAPSHOT = "SERVICE_DATA_API_SNAPSHOT";
    private static final String SCHEDULE = "SCHEDULE";
    private static final String SCHEDULED_SNAPSHOT_MATCH = "SCHEDULED_SNAPSHOT_MATCH";
    private static final String INPUT_MODEL = "ServiceDataStopPointJourneysV2";
    private static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    private static final String TARGET_TYPE = "SERVICE_DATA_JOURNEY_AGGREGATE";
    private static final String CONDITION_TYPE = "SERVICE_DATA_SCHEDULED_FIELD_MATCH";
    private static final String SERVICE_DATA_OPERATION = "POST /v2/stoppointjourneys";
    private static final String REQUIRED_TOOL = "SERVICE_DATA_API.POST_/v2/stoppointjourneys";

    public Optional<AlertVerificationOutcome> build(
            String alertId,
            String originalPrompt,
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints,
            ScheduledAlertJourneyCancellationHints journeyCancellationHints,
            ScheduledAlertCancelledCallHints cancelledCallHints,
            ScheduledAlertReplacementHints replacementHints,
            String unsupportedReason,
            String providerFailureReason,
            String model,
            String promptVersion) {
        String notApplicableReason = notApplicableReason(
                route,
                locationContext,
                temporalHints,
                platformHints,
                changeHints,
                journeyCancellationHints,
                cancelledCallHints,
                replacementHints,
                unsupportedReason);
        if (notApplicableReason != null) {
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][DETERMINISTIC_FALLBACK] not applicable alertId="
                    + alertId + " reason=" + notApplicableReason);
            return Optional.empty();
        }

        int frequencySeconds = frequencySeconds(temporalHints);
        int lookaheadMinutes = lookaheadMinutes(temporalHints);
        List<String> stopPoints = deduplicated(locationContext.serviceDataApiStopPoints());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][DETERMINISTIC_FALLBACK] applicable alertId="
                + alertId
                + " routeIntent=" + route.intentKind()
                + " monitoredStopPoints=" + stopPoints);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][DETERMINISTIC_FALLBACK] serviceDataQuery stopPoints="
                + stopPoints
                + " frequencySeconds=" + frequencySeconds
                + " lookaheadMinutes=" + lookaheadMinutes);

        Map<String, Object> serviceDataQuery = serviceDataQuery(locationContext, stopPoints, temporalHints, lookaheadMinutes);
        Map<String, Object> schedule = schedule(temporalHints, frequencySeconds);
        Map<String, Object> condition = condition(locationContext, journeyCancellationHints);
        Map<String, Object> snapshotEvaluation = new LinkedHashMap<>();
        snapshotEvaluation.put("mode", "REPORT_COUNT");
        snapshotEvaluation.put("journeyPath", "stopPointsJourneyDetails[]");
        snapshotEvaluation.put("condition", condition);
        snapshotEvaluation.put("threshold", null);
        Map<String, Object> outputPolicy = outputPolicy();

        Map<String, Object> technicalSpecification = new LinkedHashMap<>();
        technicalSpecification.put("schemaVersion", TECHNICAL_SCHEMA_VERSION);
        technicalSpecification.put("source", SERVICE_DATA);
        technicalSpecification.put("interpreterType", SCHEDULED_INTERPRETER);
        technicalSpecification.put("accessMode", SERVICE_DATA_API_SNAPSHOT);
        technicalSpecification.put("inputModel", INPUT_MODEL);
        technicalSpecification.put("outputModel", OUTPUT_MODEL);
        technicalSpecification.put("triggerType", SCHEDULE);
        technicalSpecification.put("evaluationMode", SCHEDULED_SNAPSHOT_MATCH);
        technicalSpecification.put("schedule", schedule);
        technicalSpecification.put("serviceDataQuery", serviceDataQuery);
        technicalSpecification.put("snapshotEvaluation", snapshotEvaluation);
        technicalSpecification.put("outputPolicy", outputPolicy);
        technicalSpecification.put("deduplicationKeyTemplate", "SERVICE_DATA_SCHEDULED:${alertId}:${queryWindowStart}:${conditionHash}");

        Map<String, Object> agentBlueprintPreview = agentBlueprintPreview(schedule, serviceDataQuery, snapshotEvaluation, outputPolicy);
        Map<String, Object> requirementCoverage = requirementCoverage(journeyCancellationHints, locationContext);
        List<String> warnings = List.of("Scheduled technical specification generated by deterministic fallback because AI provider failed with "
                + safeReason(providerFailureReason) + ".");
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][DETERMINISTIC_FALLBACK] technical specification generated alertId="
                + alertId);

        return Optional.of(new AlertVerificationOutcome(
                AlertVerificationDecision.VERIFIED,
                "Scheduled ServiceData technical specification generated by deterministic fallback.",
                null,
                0.72,
                "DETERMINISTIC",
                model,
                promptVersion,
                List.of(SERVICE_DATA),
                SCHEDULED_INTERPRETER,
                INPUT_MODEL,
                OUTPUT_MODEL,
                SCHEDULE,
                SCHEDULED_SNAPSHOT_MATCH,
                List.of(),
                List.of(TARGET_TYPE),
                technicalSpecification,
                agentBlueprintPreview,
                requirementCoverage,
                warnings,
                List.of(
                        "No executable code generated.",
                        "No Agent Definition created.",
                        "No Suggestion created.")));
    }

    private String notApplicableReason(
            AlertRouteUnderstandingResult route,
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertTemporalHints temporalHints,
            ScheduledAlertPlatformHints platformHints,
            ScheduledAlertChangeHints changeHints,
            ScheduledAlertJourneyCancellationHints journeyCancellationHints,
            ScheduledAlertCancelledCallHints cancelledCallHints,
            ScheduledAlertReplacementHints replacementHints,
            String unsupportedReason) {
        if (route == null
                || route.decision() != AlertRouteDecision.ROUTED
                || route.interpreterType() != AlertRouteInterpreterType.SCHEDULED_INTERPRETER
                || route.accessMode() != AlertRouteAccessMode.SERVICE_DATA_API_SNAPSHOT
                || !route.requiresPolling()
                || route.intentKind() != AlertRouteIntentKind.SNAPSHOT_REPORT
                || route.outputMode() != AlertRouteOutputMode.EVERY_RUN_REPORT) {
            return "route-not-supported";
        }
        if (unsupportedReason != null && !unsupportedReason.isBlank()) {
            return "unsupported-constraints: " + unsupportedReason;
        }
        if (temporalHints == null || frequencySeconds(temporalHints) == null) {
            return "schedule-frequency-missing";
        }
        if (locationContext == null
                || locationContext.monitoringScope() != ScheduledAlertMonitoringScope.EXPLICIT_STOP_POINTS
                || locationContext.serviceDataApiStopPoints().isEmpty()
                || locationContext.hasUnresolvedRequiredMonitoredLocations()) {
            return "location-context-not-fully-resolved";
        }
        if (platformHints != null && platformHints.hasPlatformConstraint()) {
            return "platform-constraints-not-supported";
        }
        if (changeHints != null && changeHints.hasChangeConstraint()) {
            return "change-constraints-not-supported";
        }
        if (cancelledCallHints != null && cancelledCallHints.hasCancelledCallConstraint()) {
            return "cancelled-call-constraints-not-supported";
        }
        if (replacementHints != null && replacementHints.hasReplacementConstraint()) {
            return "replacement-constraints-not-supported";
        }
        if (journeyCancellationHints == null
                || !journeyCancellationHints.hasJourneyCancellationConstraint()
                || firstIncludedJourneyCancellationConstraint(journeyCancellationHints).isEmpty()) {
            return "journey-cancellation-constraint-missing";
        }
        if (hasUnsupportedFilterLocation(locationContext)) {
            return "filter-location-role-not-supported";
        }
        return null;
    }

    private boolean hasUnsupportedFilterLocation(ScheduledServiceDataLocationContext context) {
        List<ScheduledServiceDataResolvedLocation> filters = new ArrayList<>();
        filters.addAll(context.filterLocations());
        filters.addAll(context.excludedLocations());
        return filters.stream()
                .filter(ScheduledServiceDataResolvedLocation::requiredCoverage)
                .anyMatch(location -> (location.scheduledRole() != ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT
                        && location.scheduledRole() != ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT
                        && location.scheduledRole() != ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT
                        && location.scheduledRole() != ScheduledAlertLocationRole.FILTER_TRANSIT_STOP_POINT)
                        || location.selectedPointIds().isEmpty());
    }

    private Map<String, Object> serviceDataQuery(
            ScheduledServiceDataLocationContext locationContext,
            List<String> stopPoints,
            ScheduledAlertTemporalHints temporalHints,
            int lookaheadMinutes) {
        Map<String, Object> timeWindow = new LinkedHashMap<>();
        timeWindow.put("startMode", "NOW_TRUNCATED_TO_MINUTE");
        timeWindow.put("endMode", temporalHints.hasExplicitLookaheadWindow()
                ? "NOW_PLUS_DURATION"
                : "NOW_PLUS_DEFAULT_LOOKAHEAD");
        timeWindow.put("lookaheadMinutes", lookaheadMinutes);
        timeWindow.put("defaulted", !temporalHints.hasExplicitLookaheadWindow());
        if (temporalHints.hasExplicitLookaheadWindow() && temporalHints.lookaheadRawText() != null) {
            timeWindow.put("rawText", temporalHints.lookaheadRawText());
        }

        Map<String, Object> serviceDataQuery = new LinkedHashMap<>();
        serviceDataQuery.put("operation", SERVICE_DATA_OPERATION);
        serviceDataQuery.put("monitoringScope", String.valueOf(locationContext.monitoringScope()));
        serviceDataQuery.put("stopPoints", stopPoints);
        serviceDataQuery.put("requiresAllKnownStopPoints", false);
        serviceDataQuery.put("timeWindow", timeWindow);
        return serviceDataQuery;
    }

    private Map<String, Object> schedule(ScheduledAlertTemporalHints temporalHints, int frequencySeconds) {
        Map<String, Object> schedule = new LinkedHashMap<>();
        schedule.put("frequencySeconds", frequencySeconds);
        schedule.put("defaulted", !temporalHints.hasExplicitFrequency());
        schedule.put("timezone", "Europe/Rome");
        if (temporalHints.hasExplicitFrequency() && temporalHints.frequencyRawText() != null) {
            schedule.put("rawText", temporalHints.frequencyRawText());
        }
        return schedule;
    }

    private Map<String, Object> condition(
            ScheduledServiceDataLocationContext locationContext,
            ScheduledAlertJourneyCancellationHints journeyCancellationHints) {
        List<Map<String, Object>> filterConditions = new ArrayList<>();
        for (ScheduledServiceDataResolvedLocation location : supportedFilterLocations(locationContext)) {
            filterConditions.add(locationCondition(location));
        }
        ScheduledAlertJourneyCancellationConstraint constraint = firstIncludedJourneyCancellationConstraint(journeyCancellationHints)
                .orElseThrow();
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][DETERMINISTIC_FALLBACK] add journey cancellation condition direction="
                + constraint.direction());
        Map<String, Object> cancellationConditions = journeyCancellationConditions(constraint);
        Map<String, Object> conditions;
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.GENERIC_JOURNEY_CANCELLATION
                && cancellationConditions.get("any") instanceof List<?> branches
                && !filterConditions.isEmpty()) {
            conditions = genericJourneyCancellationWithFilters(filterConditions, branches);
        } else if (filterConditions.isEmpty()) {
            conditions = cancellationConditions;
        } else {
            List<Map<String, Object>> all = new ArrayList<>(filterConditions);
            all.add(cancellationConditions);
            conditions = new LinkedHashMap<>();
            conditions.put("all", all);
        }

        Map<String, Object> anyElement = new LinkedHashMap<>();
        anyElement.put("path", "stopPointsJourneyDetails[]");
        anyElement.put("conditions", conditions);

        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("type", CONDITION_TYPE);
        condition.put("anyElement", anyElement);
        return condition;
    }

    private Map<String, Object> genericJourneyCancellationWithFilters(
            List<Map<String, Object>> filterConditions,
            List<?> cancellationBranches) {
        List<Map<String, Object>> mergedBranches = new ArrayList<>();
        for (Object branchItem : cancellationBranches) {
            Map<String, Object> branch = copyMap(branchItem);
            if (branch.isEmpty()) {
                continue;
            }
            List<Map<String, Object>> branchAll = new ArrayList<>(filterConditions);
            if (branch.get("all") instanceof List<?> existingAll) {
                for (Object item : existingAll) {
                    Map<String, Object> child = copyMap(item);
                    if (!child.isEmpty()) {
                        branchAll.add(child);
                    }
                }
            } else {
                branchAll.add(branch);
            }
            mergedBranches.add(Map.of("all", branchAll));
        }
        return Map.of("any", mergedBranches);
    }

    private List<ScheduledServiceDataResolvedLocation> supportedFilterLocations(ScheduledServiceDataLocationContext context) {
        List<ScheduledServiceDataResolvedLocation> locations = new ArrayList<>();
        context.filterLocations().stream()
                .filter(ScheduledServiceDataResolvedLocation::requiredCoverage)
                .filter(this::isSupportedFilterLocation)
                .forEach(locations::add);
        context.excludedLocations().stream()
                .filter(ScheduledServiceDataResolvedLocation::requiredCoverage)
                .filter(this::isSupportedFilterLocation)
                .forEach(locations::add);
        return locations;
    }

    private boolean isSupportedFilterLocation(ScheduledServiceDataResolvedLocation location) {
        return location.scheduledRole() == ScheduledAlertLocationRole.FILTER_DESTINATION_STOP_POINT
                || location.scheduledRole() == ScheduledAlertLocationRole.FILTER_ORIGIN_STOP_POINT
                || location.scheduledRole() == ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT
                || location.scheduledRole() == ScheduledAlertLocationRole.FILTER_TRANSIT_STOP_POINT;
    }

    private Map<String, Object> locationCondition(ScheduledServiceDataResolvedLocation location) {
        List<String> ids = location.selectedPointIds();
        boolean excluded = location.polarity() == ScheduledAlertLocationPolarity.EXCLUDE;
        String operator = ids.size() == 1
                ? (excluded ? "NOT_IN" : "EQUALS")
                : (excluded ? "NOT_IN" : "IN");
        String field = switch (location.scheduledRole()) {
            case FILTER_ORIGIN_STOP_POINT -> "callStart.stopPoint.id";
            case FILTER_DESTINATION_STOP_POINT -> "callEnd.stopPoint.id";
            case FILTER_ROUTE_STOP_POINT, FILTER_TRANSIT_STOP_POINT -> "nextCalls[].stopPoint.id";
            default -> "callEnd.stopPoint.id";
        };
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][DETERMINISTIC_FALLBACK] add filter role="
                + location.scheduledRole()
                + " field=" + field
                + " ids=" + ids);
        if (location.scheduledRole() == ScheduledAlertLocationRole.FILTER_ROUTE_STOP_POINT
                || location.scheduledRole() == ScheduledAlertLocationRole.FILTER_TRANSIT_STOP_POINT) {
            Map<String, Object> inner = new LinkedHashMap<>();
            inner.put("field", "stopPoint.id");
            inner.put("operator", operator);
            if (ids.size() == 1 && !excluded) {
                inner.put("value", ids.get(0));
            } else {
                inner.put("values", ids);
            }
            return Map.of("anyElement", Map.of(
                    "path", "nextCalls[]",
                    "conditions", inner));
        }
        Map<String, Object> leaf = new LinkedHashMap<>();
        leaf.put("field", field);
        leaf.put("operator", operator);
        if (ids.size() == 1 && !excluded) {
            leaf.put("value", ids.get(0));
        } else {
            leaf.put("values", ids);
        }
        return leaf;
    }

    private Map<String, Object> journeyCancellationConditions(ScheduledAlertJourneyCancellationConstraint constraint) {
        return switch (constraint.cancellationIntent()) {
            case ARRIVAL_JOURNEY_CANCELLATION -> Map.of("all", List.of(
                    leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION")));
            case ARRIVAL_ONLY_JOURNEY_CANCELLATION -> Map.of("all", List.of(
                    leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                    leaf("departureStatuses[].status", "NOT_CONTAINS", "DEPARTURE_CANCELLATION")));
            case DEPARTURE_JOURNEY_CANCELLATION -> Map.of("all", List.of(
                    leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION")));
            case DEPARTURE_ONLY_JOURNEY_CANCELLATION -> Map.of("all", List.of(
                    leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"),
                    leaf("arrivalStatuses[].status", "NOT_CONTAINS", "ARRIVAL_CANCELLATION")));
            case GENERIC_JOURNEY_CANCELLATION -> Map.of("any", List.of(
                    Map.of("all", List.of(
                            leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                            leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"))),
                    Map.of("all", List.of(
                            leaf("arrivalStatuses[].status", "CONTAINS", "ARRIVAL_CANCELLATION"),
                            leaf("passingType", "EQUALS", "DESTINATION"))),
                    Map.of("all", List.of(
                            leaf("departureStatuses[].status", "CONTAINS", "DEPARTURE_CANCELLATION"),
                            leaf("passingType", "EQUALS", "ORIGIN")))));
        };
    }

    private Map<String, Object> leaf(String field, String operator, Object value) {
        Map<String, Object> leaf = new LinkedHashMap<>();
        leaf.put("field", field);
        leaf.put("operator", operator);
        leaf.put("value", value);
        return leaf;
    }

    private Map<String, Object> outputPolicy() {
        Map<String, Object> outputPolicy = new LinkedHashMap<>();
        outputPolicy.put("emit", "EVERY_RUN");
        outputPolicy.put("outputMode", "EVERY_RUN_REPORT");
        outputPolicy.put("includeCount", true);
        outputPolicy.put("includeMatchingJourneys", true);
        return outputPolicy;
    }

    private Map<String, Object> agentBlueprintPreview(
            Map<String, Object> schedule,
            Map<String, Object> serviceDataQuery,
            Map<String, Object> snapshotEvaluation,
            Map<String, Object> outputPolicy) {
        Map<String, Object> runtimeContract = runtimeContract();
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("schedule", schedule);
        parameters.put("serviceDataQuery", serviceDataQuery);
        parameters.put("snapshotEvaluation", snapshotEvaluation);
        parameters.put("outputPolicy", outputPolicy);
        parameters.put("runtimeContract", runtimeContract);

        Map<String, Object> blueprint = new LinkedHashMap<>();
        blueprint.put("schemaVersion", BLUEPRINT_SCHEMA_VERSION);
        blueprint.put("agentName", "ScheduledServiceDataSnapshotAlertAgent");
        blueprint.put("triggerType", SCHEDULE);
        blueprint.put("requiredSources", List.of(SERVICE_DATA));
        blueprint.put("evaluationMode", SCHEDULED_SNAPSHOT_MATCH);
        blueprint.put("targetTypes", List.of(TARGET_TYPE));
        blueprint.put("parameters", parameters);
        blueprint.put("runtimeContract", runtimeContract);
        blueprint.put("stateRequirements", Map.of("requiresState", false));
        blueprint.put("output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return blueprint;
    }

    private Map<String, Object> runtimeContract() {
        Map<String, Object> runtimeContract = new LinkedHashMap<>();
        runtimeContract.put("executionModel", "SCHEDULED_POLLING");
        runtimeContract.put("source", SERVICE_DATA);
        runtimeContract.put("accessMode", SERVICE_DATA_API_SNAPSHOT);
        runtimeContract.put("requiresScheduler", true);
        runtimeContract.put("requiresState", false);
        runtimeContract.put("requiresExternalTools", false);
        runtimeContract.put("allowedTools", List.of(REQUIRED_TOOL));
        runtimeContract.put("requiredPermissions", List.of("READ_SERVICE_DATA"));
        return runtimeContract;
    }

    private Map<String, Object> requirementCoverage(
            ScheduledAlertJourneyCancellationHints journeyCancellationHints,
            ScheduledServiceDataLocationContext locationContext) {
        ScheduledAlertJourneyCancellationConstraint constraint = firstIncludedJourneyCancellationConstraint(journeyCancellationHints)
                .orElse(null);
        List<String> mappedBy = new ArrayList<>();
        mappedBy.add("serviceDataQuery.stopPoints");
        mappedBy.add("schedule.frequencySeconds");
        if (constraint != null) {
            mappedBy.addAll(journeyCancellationMappedBy(constraint));
        }
        for (ScheduledServiceDataResolvedLocation location : supportedFilterLocations(locationContext)) {
            mappedBy.addAll(location.targetFieldHints());
        }
        mappedBy.add("outputPolicy.emit");
        mappedBy.add("outputPolicy.includeCount");
        return Map.of(
                "requirements", List.of(Map.of(
                        "text", "Scheduled ServiceData snapshot report constraints derived from backend context.",
                        "required", true,
                        "mappable", true,
                        "mappedBy", deduplicated(mappedBy))),
                "allRequiredRequirementsMapped", true);
    }

    private List<String> journeyCancellationMappedBy(ScheduledAlertJourneyCancellationConstraint constraint) {
        List<String> mappedBy = new ArrayList<>();
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_JOURNEY_CANCELLATION
                || constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_ONLY_JOURNEY_CANCELLATION) {
            mappedBy.add("stopPointsJourneyDetails[].departureStatuses[].status");
        } else {
            mappedBy.add("stopPointsJourneyDetails[].arrivalStatuses[].status");
        }
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.ARRIVAL_ONLY_JOURNEY_CANCELLATION) {
            mappedBy.add("stopPointsJourneyDetails[].departureStatuses[].status");
        }
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.DEPARTURE_ONLY_JOURNEY_CANCELLATION) {
            mappedBy.add("stopPointsJourneyDetails[].arrivalStatuses[].status");
        }
        if (constraint.cancellationIntent() == ScheduledAlertJourneyCancellationConstraint.CancellationIntent.GENERIC_JOURNEY_CANCELLATION) {
            mappedBy.add("stopPointsJourneyDetails[].departureStatuses[].status");
            mappedBy.add("stopPointsJourneyDetails[].passingType");
        }
        return mappedBy;
    }

    private Optional<ScheduledAlertJourneyCancellationConstraint> firstIncludedJourneyCancellationConstraint(
            ScheduledAlertJourneyCancellationHints hints) {
        if (hints == null || hints.constraints() == null) {
            return Optional.empty();
        }
        return hints.constraints().stream()
                .filter(constraint -> constraint.polarity() == ScheduledAlertJourneyCancellationConstraint.Polarity.INCLUDE)
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> copyMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        return new LinkedHashMap<>();
    }

    private Integer frequencySeconds(ScheduledAlertTemporalHints temporalHints) {
        if (temporalHints == null) {
            return null;
        }
        return temporalHints.frequencySeconds() == null
                ? temporalHints.defaultFrequencySeconds()
                : temporalHints.frequencySeconds();
    }

    private int lookaheadMinutes(ScheduledAlertTemporalHints temporalHints) {
        if (temporalHints == null) {
            return 480;
        }
        return temporalHints.lookaheadMinutes() == null
                ? temporalHints.defaultLookaheadMinutes()
                : temporalHints.lookaheadMinutes();
    }

    private List<String> deduplicated(List<String> values) {
        return List.copyOf(new LinkedHashSet<>(values == null ? List.of() : values));
    }

    private String safeReason(String value) {
        return value == null || value.isBlank() ? "unknown provider error" : value;
    }
}
