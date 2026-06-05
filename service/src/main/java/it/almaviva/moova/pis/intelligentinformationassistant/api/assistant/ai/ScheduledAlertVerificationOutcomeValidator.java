package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ScheduledServiceDataCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@ApplicationScoped
public class ScheduledAlertVerificationOutcomeValidator {

    private static final String SERVICE_DATA = "SERVICE_DATA";
    private static final String SCHEDULED_INTERPRETER = "SCHEDULED_INTERPRETER";
    private static final String SERVICE_DATA_API_SNAPSHOT = "SERVICE_DATA_API_SNAPSHOT";
    private static final String SCHEDULE = "SCHEDULE";
    private static final String SCHEDULED_SNAPSHOT_MATCH = "SCHEDULED_SNAPSHOT_MATCH";
    private static final String INPUT_MODEL = "ServiceDataStopPointJourneysV2";
    private static final String OUTPUT_MODEL = "AgentOutput.CANDIDATE_SUGGESTION";
    private static final String TARGET_TYPE = "SERVICE_DATA_JOURNEY_AGGREGATE";
    private static final String TECHNICAL_SCHEMA_VERSION = "iia.alert.technical-specification/v2";
    private static final String BLUEPRINT_SCHEMA_VERSION = "iia.agent.blueprint/v1";
    private static final String AGENT_NAME = "ScheduledServiceDataSnapshotAlertAgent";
    private static final String OUTPUT_TYPE = "CANDIDATE_SUGGESTION";
    private static final String SERVICE_DATA_OPERATION = "POST /v2/stoppointjourneys";
    private static final String DEFAULT_REJECTED_REASON =
            "Scheduled alert verification rejected the result because it does not satisfy the Scheduled MVP contract.";
    private static final String CONDITION_TYPE = "SERVICE_DATA_SCHEDULED_FIELD_MATCH";
    private static final Set<String> ARRAY_PATHS = Set.of(
            "stopPointsJourneyDetails[]",
            "stopPointsJourneyDetails[].nextCalls[]",
            "stopPointsJourneyDetails[].nextTransitCalls[]",
            "stopPointsJourneyDetails[].nextCancelledCalls[]",
            "stopPointsJourneyDetails[].replacement.stopPointReplacements[]",
            "stopPointsJourneyDetails[].externalReplacement.stopPointReplacements[]");
    private static final Set<String> CHILD_ARRAY_PATHS = Set.of(
            "nextCalls[]",
            "nextTransitCalls[]",
            "nextCancelledCalls[]",
            "replacement.stopPointReplacements[]",
            "externalReplacement.stopPointReplacements[]");
    private static final Set<String> NUMERIC_OPERATORS = Set.of(
            "GREATER_THAN", "GREATER_OR_EQUAL", "LESS_THAN", "LESS_OR_EQUAL");
    private static final Set<String> PLATFORM_SINGLE_VALUE_OPERATORS = Set.of(
            "EQUAL_PLATFORM", "NOT_EQUAL_PLATFORM");
    private static final Set<String> PLATFORM_VALUES_OPERATORS = Set.of(
            "IN_PLATFORMS", "NOT_IN_PLATFORMS");
    private static final Set<String> PLATFORM_FIELD_OPERATORS = Set.of(
            "PLATFORM_EQUALS_FIELD", "PLATFORM_NOT_EQUALS_FIELD");
    private static final Set<String> PLATFORM_NUMERIC_OPERATORS = Set.of(
            "PLATFORM_NUMBER_GREATER_THAN",
            "PLATFORM_NUMBER_GREATER_OR_EQUAL",
            "PLATFORM_NUMBER_LESS_THAN",
            "PLATFORM_NUMBER_LESS_OR_EQUAL",
            "PLATFORM_NUMBER_MULTIPLE_OF");
    private static final Set<String> PLATFORM_VALUELESS_OPERATORS = Set.of(
            "PLATFORM_NUMBER_EVEN",
            "PLATFORM_NUMBER_ODD",
            "PLATFORM_NUMBER_DOUBLE_DIGIT",
            "PLATFORM_HAS_LETTER_SUFFIX");
    private static final Set<String> POSITIVE_LOCATION_OPERATORS = Set.of(
            "EQUALS", "IN", "CONTAINS_NORMALIZED", "EQUALS_NORMALIZED");
    private static final Set<String> NEGATIVE_LOCATION_OPERATORS = Set.of(
            "NOT_IN", "NOT_CONTAINS_NORMALIZED", "NOT_EQUALS_NORMALIZED");
    private static final Pattern STOP_POINT_ID_PATTERN = Pattern.compile("TNPNTS[0-9A-Z]+");
    private static final List<String> FORBIDDEN_OUTPUT_MARKERS = List.of(
            "payload.ongroundServiceEvent",
            "ServiceDataV2",
            "EVENT_INTERPRETER",
            "STATELESS_EVENT_MATCH");

    public AlertVerificationOutcome validate(AlertVerificationOutcome outcome) {
        return validate(outcome, null, null);
    }

    public AlertVerificationOutcome validate(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext scheduledLocationContext) {
        return validate(outcome, scheduledLocationContext, null);
    }

    public AlertVerificationOutcome validate(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext scheduledLocationContext,
            AlertRouteUnderstandingResult route) {
        return validate(outcome, scheduledLocationContext, route, null);
    }

    public AlertVerificationOutcome validate(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext scheduledLocationContext,
            AlertRouteUnderstandingResult route,
            ScheduledAlertTemporalHints temporalHints) {
        if (outcome == null) {
            return fail(null, DEFAULT_REJECTED_REASON);
        }
        logBase(outcome);

        if (outcome.decision() == AlertVerificationDecision.REJECTED) {
            if (isBlank(outcome.rejectedReason())) {
                return fail(outcome, "Rejected scheduled verification outcome requires rejectedReason.");
            }
            if (outcome.technicalSpecification() != null) {
                return fail(outcome, "Rejected scheduled verification outcome must not contain technicalSpecification.");
            }
            if (outcome.agentBlueprintPreview() != null) {
                return fail(outcome, "Rejected scheduled verification outcome must not contain agentBlueprintPreview.");
            }
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] Validation passed");
            return outcome;
        }

        if (outcome.decision() != AlertVerificationDecision.VERIFIED) {
            return fail(outcome, DEFAULT_REJECTED_REASON);
        }

        String failure = validateVerified(outcome, scheduledLocationContext, route, temporalHints);
        if (failure != null) {
            return fail(outcome, failure);
        }
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] Validation passed");
        return outcome;
    }

    private String validateVerified(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext scheduledLocationContext,
            AlertRouteUnderstandingResult route,
            ScheduledAlertTemporalHints temporalHints) {
        if (!containsOnlyServiceData(outcome.requiredSources())) {
            return "Verified scheduled outcome must use only SERVICE_DATA as required source.";
        }
        if (!SCHEDULED_INTERPRETER.equals(outcome.interpreterType())) {
            return "Verified scheduled outcome requires interpreterType SCHEDULED_INTERPRETER.";
        }
        if (!INPUT_MODEL.equals(outcome.inputModel())) {
            return "Verified scheduled outcome requires inputModel ServiceDataStopPointJourneysV2.";
        }
        if (!OUTPUT_MODEL.equals(outcome.outputModel())) {
            return "Verified scheduled outcome requires outputModel AgentOutput.CANDIDATE_SUGGESTION.";
        }
        if (!SCHEDULE.equals(outcome.triggerType())) {
            return "Verified scheduled outcome requires triggerType SCHEDULE.";
        }
        if (!SCHEDULED_SNAPSHOT_MATCH.equals(outcome.evaluationMode())) {
            return "Verified scheduled outcome requires evaluationMode SCHEDULED_SNAPSHOT_MATCH.";
        }
        if (!normalizedList(outcome.interpretedTargetTypes()).contains(TARGET_TYPE)) {
            return "Verified scheduled outcome requires targetTypes to contain SERVICE_DATA_JOURNEY_AGGREGATE.";
        }
        if (outcome.confidence() != null && (outcome.confidence() < 0.0 || outcome.confidence() > 1.0)) {
            return "Verified scheduled outcome confidence must be between 0.0 and 1.0.";
        }
        if (outcome.technicalSpecification() == null || outcome.technicalSpecification().isEmpty()) {
            return "Verified scheduled outcome requires technicalSpecification.";
        }
        if (outcome.agentBlueprintPreview() == null || outcome.agentBlueprintPreview().isEmpty()) {
            return "Verified scheduled outcome requires agentBlueprintPreview.";
        }
        String contamination = forbiddenOutputContamination(outcome.technicalSpecification(), outcome.agentBlueprintPreview());
        if (contamination != null) {
            return "Verified scheduled outcome contains Event/Kafka contamination: " + contamination + ".";
        }
        String technicalFailure = validateTechnicalSpecification(outcome.technicalSpecification());
        if (technicalFailure != null) {
            return technicalFailure;
        }
        String blueprintFailure = validateAgentBlueprintPreview(outcome.agentBlueprintPreview());
        if (blueprintFailure != null) {
            return blueprintFailure;
        }
        String conditionFailure = validateScheduledConditions(outcome.technicalSpecification(), outcome.agentBlueprintPreview());
        if (conditionFailure != null) {
            return conditionFailure;
        }
        String semanticFailure = validateSnapshotEvaluationSemantics(outcome.technicalSpecification(), route);
        if (semanticFailure != null) {
            return semanticFailure;
        }
        String temporalFailure = validateTemporalSemantics(outcome.technicalSpecification(), temporalHints);
        if (temporalFailure != null) {
            return temporalFailure;
        }
        String journeyTimeFailure = validateJourneyTimeHints(outcome.technicalSpecification(), temporalHints);
        if (journeyTimeFailure != null) {
            return journeyTimeFailure;
        }
        String unsupportedTemporalFailure = validateUnsupportedTemporalClaims(outcome);
        if (unsupportedTemporalFailure != null) {
            return unsupportedTemporalFailure;
        }
        if (scheduledLocationContext != null) {
            String locationFailure = validateLocationContext(outcome, scheduledLocationContext);
            if (locationFailure != null) {
                return locationFailure;
            }
        }
        return null;
    }

    private String validateJourneyTimeHints(
            Map<String, Object> technicalSpecification,
            ScheduledAlertTemporalHints hints) {
        if (hints == null || !hints.hasJourneyTimeFilter()) {
            return null;
        }
        Map<String, Object> snapshotEvaluation = asMap(technicalSpecification.get("snapshotEvaluation"));
        Map<String, Object> condition = snapshotEvaluation == null ? null : asMap(snapshotEvaluation.get("condition"));
        List<ConditionLeaf> leaves = collectConditionLeaves(condition, "");
        List<ConditionLeaf> localTimeLeaves = leaves.stream()
                .filter(leaf -> "LOCAL_TIME_BETWEEN".equals(leaf.operator()))
                .toList();
        if (localTimeLeaves.isEmpty()) {
            return "Scheduled journey time filter was requested but not represented in snapshotEvaluation.condition.";
        }
        for (ScheduledAlertJourneyTimeFilter hint : hints.journeyTimeFilters()) {
            boolean covered = localTimeLeaves.stream().anyMatch(leaf -> matchesJourneyTimeHint(leaf, hint));
            if (!covered) {
                return "Scheduled journey time filter was requested but not represented with a direction-compatible LOCAL_TIME_BETWEEN condition: "
                        + hint.rawText() + ".";
            }
        }
        return null;
    }

    private boolean matchesJourneyTimeHint(
            ConditionLeaf leaf,
            ScheduledAlertJourneyTimeFilter hint) {
        if (!isDirectionCompatibleTimeField(leaf.resolvedField(), hint.direction())) {
            return false;
        }
        Map<String, Object> value = asMap(leaf.value());
        if (value == null) {
            return false;
        }
        String start = stringValue(value.get("start"));
        String end = stringValue(value.get("end"));
        String timezone = stringValue(value.get("timezone"));
        return (isBlank(hint.startLocalTime()) || hint.startLocalTime().equals(start))
                && (isBlank(hint.endLocalTime()) || hint.endLocalTime().equals(end))
                && (isBlank(hint.timezone()) || hint.timezone().equals(timezone));
    }

    private boolean isDirectionCompatibleTimeField(
            String resolvedField,
            ScheduledAlertJourneyTimeFilter.Direction direction) {
        if (isBlank(resolvedField)) {
            return false;
        }
        return switch (direction) {
            case DEPARTURE -> isDepartureTimeField(resolvedField);
            case ARRIVAL -> isArrivalTimeField(resolvedField);
            case PASSING -> isPassingTimeField(resolvedField)
                    || (resolvedField.contains("nextCalls[]") && (isDepartureTimeField(resolvedField) || isArrivalTimeField(resolvedField)));
            case UNKNOWN -> isJourneyTimeField(resolvedField);
        };
    }

    private String validateUnsupportedTemporalClaims(AlertVerificationOutcome outcome) {
        String generated = String.join(" ",
                stringValue(outcome.summary()),
                stringValue(outcome.requirementCoverage()),
                stringValue(outcome.technicalSpecification()),
                stringValue(outcome.agentBlueprintPreview()))
                .toLowerCase(Locale.ROOT);
        if (generated.contains("trend")
                || generated.contains("aumentando")
                || generated.contains("increasing")
                || generated.contains("rispetto a ieri")
                || generated.contains("compared with yesterday")
                || generated.contains("historical comparison")
                || generated.contains("absence over time")
                || generated.contains("per 30 minuti non")) {
            return "Scheduled verification cannot support temporal trend, historical comparison or continuous absence requirements in this MVP.";
        }
        return null;
    }

    private String validateTemporalSemantics(
            Map<String, Object> technicalSpecification,
            ScheduledAlertTemporalHints hints) {
        if (hints == null) {
            return null;
        }
        Map<String, Object> schedule = asMap(technicalSpecification.get("schedule"));
        Map<String, Object> serviceDataQuery = asMap(technicalSpecification.get("serviceDataQuery"));
        Map<String, Object> timeWindow = serviceDataQuery == null ? null : asMap(serviceDataQuery.get("timeWindow"));
        Integer frequencySeconds = integerValue(schedule == null ? null : schedule.get("frequencySeconds"));
        Boolean scheduleDefaulted = schedule == null ? null : booleanValue(schedule.get("defaulted"));
        String scheduleRawText = stringValue(schedule == null ? null : schedule.get("rawText"));

        if (frequencySeconds == null) {
            return "technicalSpecification.schedule.frequencySeconds is required.";
        }
        if (frequencySeconds < hints.minFrequencySeconds() || frequencySeconds > hints.maxFrequencySeconds()) {
            return "schedule.frequencySeconds must be between "
                    + hints.minFrequencySeconds() + " and " + hints.maxFrequencySeconds() + ".";
        }
        if (hints.hasExplicitFrequency()) {
            if (!frequencySeconds.equals(hints.frequencySeconds())) {
                return "Scheduled frequency does not match backend-derived frequency: expected "
                        + hints.frequencySeconds() + " seconds but got " + frequencySeconds + ".";
            }
            if (!Boolean.FALSE.equals(scheduleDefaulted)) {
                return "Scheduled frequency was explicitly provided by the user but schedule.defaulted=true.";
            }
            if (isBlank(scheduleRawText)) {
                return "Scheduled frequency was explicitly provided by the user but schedule.rawText is missing.";
            }
        } else {
            if (!frequencySeconds.equals(hints.defaultFrequencySeconds())) {
                return "Default scheduled frequency must be " + hints.defaultFrequencySeconds()
                        + " seconds when no explicit frequency is provided.";
            }
            if (!Boolean.TRUE.equals(scheduleDefaulted)) {
                return "Scheduled frequency was not explicitly provided by the user but schedule.defaulted=false.";
            }
        }

        if (timeWindow == null || timeWindow.isEmpty()) {
            return "technicalSpecification.serviceDataQuery.timeWindow is required.";
        }
        String startMode = stringValue(timeWindow.get("startMode"));
        String endMode = stringValue(timeWindow.get("endMode"));
        Integer lookaheadMinutes = integerValue(timeWindow.get("lookaheadMinutes"));
        Boolean timeWindowDefaulted = booleanValue(timeWindow.get("defaulted"));
        String timeWindowRawText = stringValue(timeWindow.get("rawText"));
        if (!"NOW_TRUNCATED_TO_MINUTE".equals(startMode)) {
            return "ServiceData timeWindow.startMode must be NOW_TRUNCATED_TO_MINUTE.";
        }
        if (lookaheadMinutes == null) {
            return "ServiceData timeWindow.lookaheadMinutes is required.";
        }
        if (lookaheadMinutes < hints.minLookaheadMinutes() || lookaheadMinutes > hints.maxLookaheadMinutes()) {
            return "ServiceData timeWindow.lookaheadMinutes must be between "
                    + hints.minLookaheadMinutes() + " and " + hints.maxLookaheadMinutes() + ".";
        }
        if (hints.hasExplicitLookaheadWindow()) {
            if (!"NOW_PLUS_DURATION".equals(endMode)) {
                return "ServiceData lookahead endMode must be NOW_PLUS_DURATION when the user provides an explicit lookahead window.";
            }
            if (!lookaheadMinutes.equals(hints.lookaheadMinutes())) {
                return "ServiceData lookahead window does not match backend-derived value: expected "
                        + hints.lookaheadMinutes() + " minutes but got " + lookaheadMinutes + ".";
            }
            if (!Boolean.FALSE.equals(timeWindowDefaulted)) {
                return "ServiceData lookahead window was explicitly provided by the user but timeWindow.defaulted=true.";
            }
            if (isBlank(timeWindowRawText)) {
                return "ServiceData lookahead window was explicitly provided by the user but timeWindow.rawText is missing.";
            }
        } else {
            if (!"NOW_PLUS_DEFAULT_LOOKAHEAD".equals(endMode)) {
                return "ServiceData lookahead endMode must be NOW_PLUS_DEFAULT_LOOKAHEAD when no explicit lookahead window is provided.";
            }
            if (!lookaheadMinutes.equals(hints.defaultLookaheadMinutes())) {
                return "Default ServiceData lookahead must be " + hints.defaultLookaheadMinutes()
                        + " minutes when no explicit lookahead window is provided.";
            }
            if (!Boolean.TRUE.equals(timeWindowDefaulted)) {
                return "ServiceData lookahead window was not explicitly provided by the user but timeWindow.defaulted=false.";
            }
        }
        return null;
    }

    private String validateSnapshotEvaluationSemantics(
            Map<String, Object> technicalSpecification,
            AlertRouteUnderstandingResult route) {
        Map<String, Object> snapshotEvaluation = asMap(technicalSpecification.get("snapshotEvaluation"));
        Map<String, Object> outputPolicy = asMap(technicalSpecification.get("outputPolicy"));
        String mode = stringValue(snapshotEvaluation == null ? null : snapshotEvaluation.get("mode"));
        String emit = stringValue(outputPolicy == null ? null : outputPolicy.get("emit"));
        Object threshold = snapshotEvaluation == null ? null : snapshotEvaluation.get("threshold");
        boolean thresholdPresent = threshold instanceof Map<?, ?> thresholdMap && !thresholdMap.isEmpty();
        Boolean includeCount = outputPolicy == null ? null : booleanValue(outputPolicy.get("includeCount"));

        if (isReportRoute(route)) {
            if (!"REPORT_COUNT".equals(mode) && !"REPORT_MATCHING_JOURNEYS".equals(mode)) {
                return "SNAPSHOT_REPORT route requires REPORT_COUNT or REPORT_MATCHING_JOURNEYS, not " + mode + ".";
            }
            if (!"EVERY_RUN".equals(emit)) {
                return "SNAPSHOT_REPORT route requires outputPolicy.emit EVERY_RUN.";
            }
            if (thresholdPresent) {
                return "SNAPSHOT_REPORT route must not contain threshold.";
            }
        }

        if ("COUNT_MATCHING_JOURNEYS".equals(mode)) {
            if (!thresholdPresent) {
                return "COUNT_MATCHING_JOURNEYS requires threshold.";
            }
            if (!"ON_MATCH".equals(emit)) {
                return "COUNT_MATCHING_JOURNEYS requires outputPolicy.emit ON_MATCH.";
            }
            if (Boolean.FALSE.equals(includeCount)) {
                return "COUNT_MATCHING_JOURNEYS requires outputPolicy.includeCount=true.";
            }
            return null;
        }
        if ("REPORT_COUNT".equals(mode)) {
            if (thresholdPresent) {
                return "REPORT_COUNT must not contain threshold.";
            }
            if (!"EVERY_RUN".equals(emit)) {
                return "REPORT_COUNT requires outputPolicy.emit EVERY_RUN.";
            }
            if (!Boolean.TRUE.equals(includeCount)) {
                return "REPORT_COUNT requires outputPolicy.includeCount=true.";
            }
            return null;
        }
        if ("REPORT_MATCHING_JOURNEYS".equals(mode)) {
            if (thresholdPresent) {
                return "REPORT_MATCHING_JOURNEYS must not contain threshold for this MVP.";
            }
            if (!"EVERY_RUN".equals(emit)) {
                return "REPORT_MATCHING_JOURNEYS requires outputPolicy.emit EVERY_RUN.";
            }
            return null;
        }
        if ("BOOLEAN_EXISTS".equals(mode) && "EVERY_RUN".equals(emit)) {
            return "BOOLEAN_EXISTS must not use outputPolicy.emit EVERY_RUN for this MVP.";
        }
        return null;
    }

    private boolean isReportRoute(AlertRouteUnderstandingResult route) {
        return route != null
                && (route.intentKind() == AlertRouteIntentKind.SNAPSHOT_REPORT
                || route.outputMode() == AlertRouteOutputMode.EVERY_RUN_REPORT);
    }

    private String validateTechnicalSpecification(Map<String, Object> technicalSpecification) {
        if (!TECHNICAL_SCHEMA_VERSION.equals(stringValue(technicalSpecification.get("schemaVersion")))) {
            return "technicalSpecification.schemaVersion must be iia.alert.technical-specification/v2.";
        }
        if (!SERVICE_DATA.equals(stringValue(technicalSpecification.get("source")))) {
            return "technicalSpecification.source must be SERVICE_DATA.";
        }
        if (!SCHEDULED_INTERPRETER.equals(stringValue(technicalSpecification.get("interpreterType")))) {
            return "technicalSpecification.interpreterType must be SCHEDULED_INTERPRETER.";
        }
        if (!SERVICE_DATA_API_SNAPSHOT.equals(stringValue(technicalSpecification.get("accessMode")))) {
            return "technicalSpecification.accessMode must be SERVICE_DATA_API_SNAPSHOT.";
        }
        if (!INPUT_MODEL.equals(stringValue(technicalSpecification.get("inputModel")))) {
            return "technicalSpecification.inputModel must be ServiceDataStopPointJourneysV2.";
        }
        if (!OUTPUT_MODEL.equals(stringValue(technicalSpecification.get("outputModel")))) {
            return "technicalSpecification.outputModel must be AgentOutput.CANDIDATE_SUGGESTION.";
        }
        if (!SCHEDULE.equals(stringValue(technicalSpecification.get("triggerType")))) {
            return "technicalSpecification.triggerType must be SCHEDULE.";
        }
        if (!SCHEDULED_SNAPSHOT_MATCH.equals(stringValue(technicalSpecification.get("evaluationMode")))) {
            return "technicalSpecification.evaluationMode must be SCHEDULED_SNAPSHOT_MATCH.";
        }
        if (!presentMap(technicalSpecification.get("schedule"))) {
            return "technicalSpecification.schedule is required.";
        }
        if (!presentMap(technicalSpecification.get("serviceDataQuery"))) {
            return "technicalSpecification.serviceDataQuery is required.";
        }
        if (!presentMap(technicalSpecification.get("snapshotEvaluation"))) {
            return "technicalSpecification.snapshotEvaluation is required.";
        }
        if (!presentMap(asMap(technicalSpecification.get("snapshotEvaluation")).get("condition"))) {
            return "technicalSpecification.snapshotEvaluation.condition is required.";
        }
        if (!presentMap(technicalSpecification.get("outputPolicy"))) {
            return "technicalSpecification.outputPolicy is required.";
        }
        if (isBlank(stringValue(technicalSpecification.get("deduplicationKeyTemplate")))) {
            return "technicalSpecification.deduplicationKeyTemplate is required.";
        }
        return null;
    }

    private String validateScheduledConditions(
            Map<String, Object> technicalSpecification,
            Map<String, Object> agentBlueprintPreview) {
        Map<String, Object> snapshotEvaluation = asMap(technicalSpecification.get("snapshotEvaluation"));
        Map<String, Object> condition = snapshotEvaluation == null ? null : asMap(snapshotEvaluation.get("condition"));
        if (condition == null || condition.isEmpty()) {
            return catalogFailure("technicalSpecification.snapshotEvaluation.condition is required.");
        }
        String failure = validateConditionNode(condition, "", true);
        if (failure != null) {
            return failure;
        }

        Map<String, Object> parameters = asMap(agentBlueprintPreview.get("parameters"));
        Map<String, Object> blueprintSnapshotEvaluation =
                parameters == null ? null : asMap(parameters.get("snapshotEvaluation"));
        Map<String, Object> blueprintCondition =
                blueprintSnapshotEvaluation == null ? null : asMap(blueprintSnapshotEvaluation.get("condition"));
        if (blueprintCondition != null) {
            if (!condition.equals(blueprintCondition)) {
                return catalogFailure("agentBlueprintPreview.parameters.snapshotEvaluation.condition must equal technicalSpecification.snapshotEvaluation.condition.");
            }
            failure = validateConditionNode(blueprintCondition, "", true);
            if (failure != null) {
                return failure;
            }
        }
        return null;
    }

    private String validateConditionNode(Map<String, Object> node, String arrayContext, boolean root) {
        if (node == null || node.isEmpty()) {
            return catalogFailure("condition node must be a non-empty object.");
        }
        if (root) {
            if (!CONDITION_TYPE.equals(stringValue(node.get("type")))) {
                return catalogFailure("snapshotEvaluation.condition.type must be SERVICE_DATA_SCHEDULED_FIELD_MATCH.");
            }
        } else if (node.containsKey("type")) {
            return catalogFailure("type is allowed only on the root scheduled condition.");
        }

        boolean hasLeaf = node.containsKey("field") || node.containsKey("operator") || node.containsKey("otherField");
        boolean hasAll = node.containsKey("all");
        boolean hasAny = node.containsKey("any");
        boolean hasAnyElement = node.containsKey("anyElement");
        int formCount = (hasLeaf ? 1 : 0) + (hasAll ? 1 : 0) + (hasAny ? 1 : 0) + (hasAnyElement ? 1 : 0);
        if (formCount == 0) {
            return catalogFailure("condition node must contain leaf, all, any or anyElement.");
        }
        if (formCount > 1) {
            return catalogFailure("condition node mixes incompatible forms.");
        }
        if (hasLeaf) {
            return validateLeaf(node, arrayContext);
        }
        if (hasAll) {
            return validateChildrenArray(node.get("all"), arrayContext, "all");
        }
        if (hasAny) {
            return validateChildrenArray(node.get("any"), arrayContext, "any");
        }
        return validateAnyElement(node.get("anyElement"), arrayContext);
    }

    private String validateChildrenArray(Object value, String arrayContext, String property) {
        if (!(value instanceof Collection<?> collection) || collection.isEmpty()) {
            return catalogFailure(property + " must be a non-empty array.");
        }
        int index = 0;
        for (Object item : collection) {
            Map<String, Object> child = asMap(item);
            if (child == null) {
                return catalogFailure(property + "[" + index + "] must be an object.");
            }
            String failure = validateConditionNode(child, arrayContext, false);
            if (failure != null) {
                return failure;
            }
            index++;
        }
        return null;
    }

    private String validateAnyElement(Object value, String arrayContext) {
        Map<String, Object> anyElement = asMap(value);
        if (anyElement == null) {
            return catalogFailure("anyElement must be an object.");
        }
        String rawPath = stringValue(anyElement.get("path"));
        if (isBlank(rawPath)) {
            return catalogFailure("anyElement.path is required.");
        }
        String resolvedPath = resolveArrayPath(arrayContext, rawPath);
        if (resolvedPath == null) {
            return catalogFailure("anyElement.path is not allowed: " + rawPath);
        }
        Map<String, Object> conditions = asMap(anyElement.get("conditions"));
        if (conditions == null || conditions.isEmpty()) {
            return catalogFailure("anyElement.conditions is required.");
        }
        return validateConditionNode(conditions, resolvedPath, false);
    }

    private String resolveArrayPath(String arrayContext, String rawPath) {
        String path = rawPath.trim();
        if (ARRAY_PATHS.contains(path)) {
            if (arrayContext == null || arrayContext.isBlank()) {
                return path;
            }
            if (path.startsWith(arrayContext + ".")) {
                return path;
            }
            return null;
        }
        if ("stopPointsJourneyDetails[]".equals(path) && (arrayContext == null || arrayContext.isBlank())) {
            return path;
        }
        if ("stopPointsJourneyDetails[]".equals(arrayContext) && CHILD_ARRAY_PATHS.contains(path)) {
            return arrayContext + "." + path;
        }
        return null;
    }

    private String validateLeaf(Map<String, Object> node, String arrayContext) {
        String field = stringValue(node.get("field"));
        String operator = stringValue(node.get("operator"));
        if (isBlank(field)) {
            return catalogFailure("condition leaf field is required.");
        }
        if (isBlank(operator)) {
            return catalogFailure("condition leaf operator is required.");
        }
        String resolvedField = resolveField(arrayContext, field);
        if (resolvedField == null) {
            return catalogFailure("field is not allowed by ScheduledServiceDataCapabilityCatalog: " + field);
        }
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR][CATALOG] validating field="
                + field + " resolvedField=" + resolvedField + " operator=" + operator);
        ScheduledServiceDataCapabilityCatalog.FieldCapability capability =
                ScheduledServiceDataCapabilityCatalog.findField(resolvedField).orElse(null);
        if (capability == null) {
            return catalogFailure("field is not allowed by ScheduledServiceDataCapabilityCatalog: " + resolvedField);
        }
        if (!capability.supportsOperator(operator)) {
            return catalogFailure("operator " + operator + " is not allowed for field " + resolvedField + ".");
        }
        String shapeFailure = validateOperatorShape(node, operator, capability, resolvedField, arrayContext);
        if (shapeFailure != null) {
            return shapeFailure;
        }
        return validateEnumValues(node, operator, capability, resolvedField);
    }

    private String resolveField(String arrayContext, String field) {
        String trimmed = field.trim();
        if (arrayContext != null && !arrayContext.isBlank()
                && (trimmed.startsWith("stopPointsJourneyDetails[]")
                || trimmed.startsWith(arrayContext + "."))) {
            return null;
        }
        if (ScheduledServiceDataCapabilityCatalog.isAllowedField(trimmed)) {
            return trimmed;
        }
        if (arrayContext == null || arrayContext.isBlank()) {
            return null;
        }
        String candidate = arrayContext + "." + trimmed;
        return ScheduledServiceDataCapabilityCatalog.isAllowedField(candidate) ? candidate : null;
    }

    private boolean isJourneyTimeField(String resolvedField) {
        return isDepartureTimeField(resolvedField)
                || isArrivalTimeField(resolvedField)
                || isPassingTimeField(resolvedField);
    }

    private boolean isDepartureTimeField(String resolvedField) {
        return resolvedField != null && (resolvedField.endsWith("departureTime")
                || resolvedField.endsWith("timetabledDepartureTime")
                || resolvedField.endsWith("estimatedDepartureTime"));
    }

    private boolean isArrivalTimeField(String resolvedField) {
        return resolvedField != null && (resolvedField.endsWith("arrivalTime")
                || resolvedField.endsWith("timetabledArrivalTime")
                || resolvedField.endsWith("estimatedArrivalTime"));
    }

    private boolean isPassingTimeField(String resolvedField) {
        return resolvedField != null && resolvedField.endsWith("passingTime");
    }

    private String validateOperatorShape(
            Map<String, Object> node,
            String operator,
            ScheduledServiceDataCapabilityCatalog.FieldCapability capability,
            String resolvedField,
            String arrayContext) {
        if ("IN".equals(operator) || "NOT_IN".equals(operator) || "CONTAINS_ANY".equals(operator)) {
            if (!hasNonEmptyValues(node)) {
                return catalogFailure("operator " + operator
                        + " requires a non-empty values array; use values, not value.");
            }
            if (node.containsKey("value")) {
                return catalogFailure("operator " + operator + " requires values, not value.");
            }
            return null;
        }
        if ("CONTAINS".equals(operator)
                || "EQUALS".equals(operator)
                || "EQUALS_NORMALIZED".equals(operator)
                || "CONTAINS_NORMALIZED".equals(operator)
                || "NOT_EQUALS_NORMALIZED".equals(operator)
                || "NOT_CONTAINS_NORMALIZED".equals(operator)) {
            if (!node.containsKey("value") || node.get("value") == null) {
                return catalogFailure("operator " + operator + " requires value.");
            }
        }
        if (NUMERIC_OPERATORS.contains(operator)) {
            if (!(node.get("value") instanceof Number)) {
                return catalogFailure("numeric operator " + operator + " requires numeric value.");
            }
        }
        if (capability.type() == ScheduledServiceDataCapabilityCatalog.FieldType.BOOLEAN
                && "EQUALS".equals(operator)
                && !(node.get("value") instanceof Boolean)) {
            return catalogFailure("boolean field " + resolvedField + " requires boolean value.");
        }
        if ("LOCAL_TIME_BETWEEN".equals(operator)) {
            if (!isJourneyTimeField(resolvedField)) {
                return catalogFailure("LOCAL_TIME_BETWEEN is allowed only on journey time fields.");
            }
            return validateLocalTimeBetween(node.get("value"));
        }
        if ("LOCAL_DAY_OF_WEEK_IN".equals(operator) || "LOCAL_DAY_OF_WEEK_NOT_IN".equals(operator)) {
            return validateLocalDayOfWeek(node.get("value"));
        }
        if (PLATFORM_SINGLE_VALUE_OPERATORS.contains(operator)) {
            if (isBlank(stringValue(node.get("value")))) {
                return catalogFailure("platform operator " + operator + " requires non-empty string value.");
            }
        }
        if (PLATFORM_VALUES_OPERATORS.contains(operator) && !hasNonEmptyValues(node)) {
            return catalogFailure("platform operator " + operator + " requires non-empty values array.");
        }
        if (PLATFORM_FIELD_OPERATORS.contains(operator)) {
            String otherField = stringValue(node.get("otherField"));
            if (isBlank(otherField)) {
                return catalogFailure("platform field operator " + operator + " requires otherField.");
            }
            String resolvedOtherField = resolveField(arrayContext, otherField);
            ScheduledServiceDataCapabilityCatalog.FieldCapability otherCapability = resolvedOtherField == null
                    ? null
                    : ScheduledServiceDataCapabilityCatalog.findField(resolvedOtherField).orElse(null);
            if (otherCapability == null
                    || otherCapability.type() != ScheduledServiceDataCapabilityCatalog.FieldType.PLATFORM) {
                return catalogFailure("otherField for " + operator + " must resolve to a platform field.");
            }
        }
        if (PLATFORM_NUMERIC_OPERATORS.contains(operator)) {
            if (!(node.get("value") instanceof Number number)) {
                return catalogFailure("platform numeric operator " + operator + " requires numeric value.");
            }
            if ("PLATFORM_NUMBER_MULTIPLE_OF".equals(operator) && number.doubleValue() <= 0.0) {
                return catalogFailure("PLATFORM_NUMBER_MULTIPLE_OF requires value greater than 0.");
            }
        }
        if ("PLATFORM_NUMBER_BETWEEN".equals(operator)) {
            Map<String, Object> value = asMap(node.get("value"));
            if (value == null || !(value.get("min") instanceof Number min) || !(value.get("max") instanceof Number max)) {
                return catalogFailure("PLATFORM_NUMBER_BETWEEN requires numeric min and max.");
            }
            if (min.doubleValue() > max.doubleValue()) {
                return catalogFailure("PLATFORM_NUMBER_BETWEEN requires min less than or equal to max.");
            }
        }
        if (PLATFORM_VALUELESS_OPERATORS.contains(operator)
                && (node.containsKey("value") || node.containsKey("values"))) {
            return catalogFailure("platform valueless operator " + operator + " must not contain value or values.");
        }
        return null;
    }

    private String validateLocalTimeBetween(Object value) {
        Map<String, Object> map = asMap(value);
        if (map == null) {
            return catalogFailure("LOCAL_TIME_BETWEEN value must be an object.");
        }
        String start = stringValue(map.get("start"));
        String end = stringValue(map.get("end"));
        String timezone = stringValue(map.get("timezone"));
        if (isBlank(start) || isBlank(end) || isBlank(timezone)) {
            return catalogFailure("LOCAL_TIME_BETWEEN requires value.start, value.end and value.timezone.");
        }
        try {
            LocalTime parsedStart = LocalTime.parse(start);
            LocalTime parsedEnd = LocalTime.parse(end);
            if (parsedStart.equals(parsedEnd)) {
                return catalogFailure("LOCAL_TIME_BETWEEN start and end must not be equal for this MVP.");
            }
        } catch (DateTimeException ex) {
            return catalogFailure("LOCAL_TIME_BETWEEN start/end must use HH:mm:ss.");
        }
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException ex) {
            return catalogFailure("LOCAL_TIME_BETWEEN timezone must be a valid IANA ZoneId.");
        }
        return null;
    }

    private String validateLocalDayOfWeek(Object value) {
        Map<String, Object> map = asMap(value);
        if (map == null) {
            return catalogFailure("LOCAL_DAY_OF_WEEK value must be an object.");
        }
        if (isBlank(stringValue(map.get("timezone")))) {
            return catalogFailure("LOCAL_DAY_OF_WEEK requires value.timezone.");
        }
        Object days = map.get("days");
        if (!(days instanceof Collection<?> collection) || collection.isEmpty()) {
            return catalogFailure("LOCAL_DAY_OF_WEEK requires non-empty value.days.");
        }
        for (Object day : collection) {
            try {
                DayOfWeek.valueOf(String.valueOf(day).trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return catalogFailure("LOCAL_DAY_OF_WEEK contains invalid day: " + day);
            }
        }
        return null;
    }

    private String validateEnumValues(
            Map<String, Object> node,
            String operator,
            ScheduledServiceDataCapabilityCatalog.FieldCapability capability,
            String resolvedField) {
        if (!capability.isEnumLike() || capability.enumValues().isEmpty()) {
            return null;
        }
        if ("EQUALS".equals(operator) || "CONTAINS".equals(operator)) {
            String value = stringValue(node.get("value"));
            if (!capability.enumValues().contains(value)) {
                return catalogFailure("enum value " + value + " is not allowed for field " + resolvedField + ".");
            }
        }
        if ("IN".equals(operator) || "CONTAINS_ANY".equals(operator)) {
            for (String value : stringList(node.get("values"))) {
                if (!capability.enumValues().contains(value)) {
                    return catalogFailure("enum value " + value + " is not allowed for field " + resolvedField + ".");
                }
            }
        }
        return null;
    }

    private boolean hasNonEmptyValues(Map<String, Object> node) {
        return node.get("values") instanceof Collection<?> values && !values.isEmpty();
    }

    private String catalogFailure(String reason) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR][CATALOG] validation failed reason=" + reason);
        return reason;
    }

    private String validateAgentBlueprintPreview(Map<String, Object> preview) {
        if (!BLUEPRINT_SCHEMA_VERSION.equals(stringValue(preview.get("schemaVersion")))) {
            return "agentBlueprintPreview.schemaVersion must be iia.agent.blueprint/v1.";
        }
        if (isBlank(stringValue(preview.get("agentName")))) {
            return "agentBlueprintPreview.agentName is required.";
        }
        if (!AGENT_NAME.equals(stringValue(preview.get("agentName")))) {
            return "agentBlueprintPreview.agentName must be ScheduledServiceDataSnapshotAlertAgent.";
        }
        if (!SCHEDULE.equals(stringValue(preview.get("triggerType")))) {
            return "agentBlueprintPreview.triggerType must be SCHEDULE.";
        }
        if (!containsOnlyServiceData(stringList(preview.get("requiredSources")))) {
            return "agentBlueprintPreview.requiredSources must contain only SERVICE_DATA.";
        }
        if (!SCHEDULED_SNAPSHOT_MATCH.equals(stringValue(preview.get("evaluationMode")))) {
            return "agentBlueprintPreview.evaluationMode must be SCHEDULED_SNAPSHOT_MATCH.";
        }
        if (!normalizedList(stringList(preview.get("targetTypes"))).contains(TARGET_TYPE)) {
            return "agentBlueprintPreview.targetTypes must contain SERVICE_DATA_JOURNEY_AGGREGATE.";
        }
        Map<String, Object> parameters = asMap(preview.get("parameters"));
        if (parameters == null || parameters.isEmpty()) {
            return "agentBlueprintPreview.parameters is required.";
        }
        if (!presentMap(parameters.get("serviceDataQuery"))) {
            return "agentBlueprintPreview.parameters.serviceDataQuery is required.";
        }
        if (!presentMap(parameters.get("snapshotEvaluation"))) {
            return "agentBlueprintPreview.parameters.snapshotEvaluation is required.";
        }
        if (!presentMap(parameters.get("outputPolicy"))) {
            return "agentBlueprintPreview.parameters.outputPolicy is required.";
        }
        Map<String, Object> stateRequirements = asMap(preview.get("stateRequirements"));
        if (stateRequirements == null || !Boolean.FALSE.equals(stateRequirements.get("requiresState"))) {
            return "agentBlueprintPreview.stateRequirements.requiresState must be false.";
        }
        Map<String, Object> output = asMap(preview.get("output"));
        if (output == null || !OUTPUT_TYPE.equals(stringValue(output.get("type")))) {
            return "agentBlueprintPreview.output.type must be CANDIDATE_SUGGESTION.";
        }
        return null;
    }

    private String validateLocationContext(
            AlertVerificationOutcome outcome,
            ScheduledServiceDataLocationContext context) {
        Map<String, Object> technicalSpecification = outcome.technicalSpecification();
        Map<String, Object> serviceDataQuery = asMap(technicalSpecification.get("serviceDataQuery"));
        Map<String, Object> snapshotEvaluation = asMap(technicalSpecification.get("snapshotEvaluation"));
        Map<String, Object> condition = snapshotEvaluation == null ? null : asMap(snapshotEvaluation.get("condition"));
        List<ConditionLeaf> leaves = collectConditionLeaves(condition, "");
        List<String> queryStopPoints = stringList(serviceDataQuery == null ? null : serviceDataQuery.get("stopPoints"));
        List<String> contextStopPoints = context.serviceDataApiStopPoints();
        String monitoringScope = stringValue(serviceDataQuery == null ? null : serviceDataQuery.get("monitoringScope"));
        boolean requiresAllKnownStopPoints = Boolean.TRUE.equals(serviceDataQuery == null
                ? null
                : serviceDataQuery.get("requiresAllKnownStopPoints"));

        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR][LOCATION] monitoringScope=" + context.monitoringScope());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR][LOCATION] queryStopPoints=" + queryStopPoints);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR][LOCATION] contextStopPoints=" + contextStopPoints);

        if (context.monitoringScope() == ScheduledAlertMonitoringScope.UNSPECIFIED) {
            return locationFailure("Scheduled ServiceData verification requires explicit monitored stop points or ALL_KNOWN_STOP_POINTS scope.");
        }
        if (context.hasUnresolvedRequiredMonitoredLocations()) {
            return locationFailure("Scheduled ServiceData alert has unresolved monitored locations: "
                    + context.unresolvedRequiredMonitoredLocationTexts() + ".");
        }
        if (serviceDataQuery == null || serviceDataQuery.isEmpty()) {
            return locationFailure("technicalSpecification.serviceDataQuery is required.");
        }
        if (!SERVICE_DATA_OPERATION.equals(stringValue(serviceDataQuery.get("operation")))) {
            return locationFailure("serviceDataQuery.operation must be POST /v2/stoppointjourneys.");
        }
        if (!String.valueOf(context.monitoringScope()).equals(monitoringScope)) {
            return locationFailure("serviceDataQuery.monitoringScope must match ScheduledServiceDataLocationContext.");
        }
        if (requiresAllKnownStopPoints != context.requiresAllKnownStopPoints()) {
            return locationFailure("serviceDataQuery.requiresAllKnownStopPoints must match ScheduledServiceDataLocationContext.");
        }
        if (hasDuplicates(queryStopPoints)) {
            return locationFailure("serviceDataQuery.stopPoints must be deduplicated.");
        }

        if (context.monitoringScope() == ScheduledAlertMonitoringScope.ALL_KNOWN_STOP_POINTS) {
            if (!requiresAllKnownStopPoints) {
                return locationFailure("ALL_KNOWN_STOP_POINTS requires requiresAllKnownStopPoints=true.");
            }
            if (!queryStopPoints.isEmpty()) {
                return locationFailure("ALL_KNOWN_STOP_POINTS must not contain explicit serviceDataQuery.stopPoints.");
            }
        } else {
            if (queryStopPoints.isEmpty()) {
                return locationFailure("EXPLICIT_STOP_POINTS requires non-empty serviceDataQuery.stopPoints.");
            }
            if (!queryStopPoints.equals(contextStopPoints)) {
                return locationFailure("serviceDataQuery.stopPoints must exactly match monitored context stop points.");
            }
        }

        String filterInQueryFailure = validateFiltersNotInQuery(context, queryStopPoints);
        if (filterInQueryFailure != null) {
            return filterInQueryFailure;
        }

        String inventedIdFailure = validateNoInventedStopPointIds(technicalSpecification, allowedStopPointIds(context));
        if (inventedIdFailure != null) {
            return inventedIdFailure;
        }

        String blueprintQueryFailure = validateBlueprintServiceDataQuery(outcome.agentBlueprintPreview(), serviceDataQuery);
        if (blueprintQueryFailure != null) {
            return blueprintQueryFailure;
        }

        for (ScheduledServiceDataResolvedLocation location : requiredFilterLocations(context)) {
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR][LOCATION] checking filter rawText="
                    + location.rawText() + " role=" + location.scheduledRole() + " polarity=" + location.polarity());
            if (!isFilterCovered(location, leaves)) {
                return locationFailure("Required filter/control location '" + location.rawText()
                        + "' was not covered by snapshotEvaluation.condition.");
            }
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR][LOCATION] coverage ok rawText="
                    + location.rawText());
        }
        return null;
    }

    private String validateFiltersNotInQuery(
            ScheduledServiceDataLocationContext context,
            List<String> queryStopPoints) {
        Set<String> monitoredIds = new LinkedHashSet<>();
        context.monitoredLocations().forEach(location -> monitoredIds.addAll(location.selectedPointIds()));
        for (ScheduledServiceDataResolvedLocation filter : requiredFilterLocations(context)) {
            for (String id : filter.selectedPointIds()) {
                if (queryStopPoints.contains(id) && !monitoredIds.contains(id)) {
                    return locationFailure("Filter/control location '" + filter.rawText()
                            + "' was incorrectly used as a monitored ServiceData API stop point.");
                }
            }
        }
        return null;
    }

    private Set<String> allowedStopPointIds(ScheduledServiceDataLocationContext context) {
        Set<String> ids = new LinkedHashSet<>();
        ids.addAll(context.serviceDataApiStopPoints());
        context.monitoredLocations().forEach(location -> ids.addAll(location.selectedPointIds()));
        context.filterLocations().forEach(location -> ids.addAll(location.selectedPointIds()));
        context.excludedLocations().forEach(location -> ids.addAll(location.selectedPointIds()));
        return ids;
    }

    private String validateNoInventedStopPointIds(Object root, Set<String> allowedStopPointIds) {
        if (root instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String failure = validateNoInventedStopPointIds(entry.getKey(), allowedStopPointIds);
                if (failure != null) {
                    return failure;
                }
                failure = validateNoInventedStopPointIds(entry.getValue(), allowedStopPointIds);
                if (failure != null) {
                    return failure;
                }
            }
            return null;
        }
        if (root instanceof Collection<?> collection) {
            for (Object item : collection) {
                String failure = validateNoInventedStopPointIds(item, allowedStopPointIds);
                if (failure != null) {
                    return failure;
                }
            }
            return null;
        }
        String text = stringValue(root);
        if (text == null) {
            return null;
        }
        java.util.regex.Matcher matcher = STOP_POINT_ID_PATTERN.matcher(text);
        while (matcher.find()) {
            String id = matcher.group();
            if (!allowedStopPointIds.contains(id)) {
                return locationFailure("Technical specification uses a stop point id that was not resolved from the user prompt: "
                        + id + ".");
            }
        }
        return null;
    }

    private String validateBlueprintServiceDataQuery(
            Map<String, Object> agentBlueprintPreview,
            Map<String, Object> technicalServiceDataQuery) {
        Map<String, Object> parameters = asMap(agentBlueprintPreview.get("parameters"));
        Map<String, Object> blueprintQuery = parameters == null ? null : asMap(parameters.get("serviceDataQuery"));
        if (blueprintQuery == null || blueprintQuery.isEmpty()) {
            return null;
        }
        if (!stringValue(technicalServiceDataQuery.get("monitoringScope"))
                .equals(stringValue(blueprintQuery.get("monitoringScope")))) {
            return locationFailure("agentBlueprintPreview.parameters.serviceDataQuery.monitoringScope must match technicalSpecification.");
        }
        if (Boolean.TRUE.equals(technicalServiceDataQuery.get("requiresAllKnownStopPoints"))
                != Boolean.TRUE.equals(blueprintQuery.get("requiresAllKnownStopPoints"))) {
            return locationFailure("agentBlueprintPreview.parameters.serviceDataQuery.requiresAllKnownStopPoints must match technicalSpecification.");
        }
        if (!stringList(technicalServiceDataQuery.get("stopPoints")).equals(stringList(blueprintQuery.get("stopPoints")))) {
            return locationFailure("agentBlueprintPreview.parameters.serviceDataQuery.stopPoints must match technicalSpecification.");
        }
        return null;
    }

    private List<ScheduledServiceDataResolvedLocation> requiredFilterLocations(ScheduledServiceDataLocationContext context) {
        java.util.ArrayList<ScheduledServiceDataResolvedLocation> locations = new java.util.ArrayList<>();
        context.filterLocations().stream()
                .filter(ScheduledServiceDataResolvedLocation::requiredCoverage)
                .filter(location -> location.scheduledRole() != ScheduledAlertLocationRole.MONITORED_STOP_POINT)
                .forEach(locations::add);
        context.excludedLocations().stream()
                .filter(ScheduledServiceDataResolvedLocation::requiredCoverage)
                .filter(location -> location.scheduledRole() != ScheduledAlertLocationRole.MONITORED_STOP_POINT)
                .forEach(locations::add);
        return locations;
    }

    private boolean isFilterCovered(ScheduledServiceDataResolvedLocation location, List<ConditionLeaf> leaves) {
        for (ConditionLeaf leaf : leaves) {
            if (!location.targetFieldHints().contains(leaf.resolvedField())) {
                continue;
            }
            boolean excluded = location.polarity() == ScheduledAlertLocationPolarity.EXCLUDE;
            if (excluded && !NEGATIVE_LOCATION_OPERATORS.contains(leaf.operator())) {
                continue;
            }
            if (!excluded && !POSITIVE_LOCATION_OPERATORS.contains(leaf.operator())) {
                continue;
            }
            if (!location.selectedPointIds().isEmpty()
                    && leaf.values().stream().anyMatch(location.selectedPointIds()::contains)) {
                return true;
            }
            if (location.selectedPointIds().isEmpty() && location.fallbackToNameLong()) {
                String normalized = location.normalizedText().isBlank() ? location.rawText() : location.normalizedText();
                if (leaf.values().stream().anyMatch(value -> String.valueOf(value).equalsIgnoreCase(normalized))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<ConditionLeaf> collectConditionLeaves(Map<String, Object> condition, String arrayContext) {
        java.util.ArrayList<ConditionLeaf> leaves = new java.util.ArrayList<>();
        collectConditionLeaves(condition, arrayContext, leaves);
        return leaves;
    }

    private void collectConditionLeaves(
            Map<String, Object> node,
            String arrayContext,
            List<ConditionLeaf> leaves) {
        if (node == null || node.isEmpty()) {
            return;
        }
        if (node.containsKey("field") && node.containsKey("operator")) {
            String field = stringValue(node.get("field"));
            String resolvedField = field == null ? null : resolveField(arrayContext, field);
            String operator = stringValue(node.get("operator"));
            java.util.ArrayList<Object> values = new java.util.ArrayList<>();
            if (node.containsKey("value")) {
                values.add(node.get("value"));
            }
            values.addAll(stringList(node.get("values")));
            leaves.add(new ConditionLeaf(field, resolvedField, operator, node.get("value"), List.copyOf(values)));
            return;
        }
        collectArrayNodes(node.get("all"), arrayContext, leaves);
        collectArrayNodes(node.get("any"), arrayContext, leaves);
        Map<String, Object> anyElement = asMap(node.get("anyElement"));
        if (anyElement != null) {
            String resolvedPath = resolveArrayPath(arrayContext, stringValue(anyElement.get("path")));
            collectConditionLeaves(asMap(anyElement.get("conditions")), resolvedPath, leaves);
        }
    }

    private void collectArrayNodes(Object value, String arrayContext, List<ConditionLeaf> leaves) {
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                collectConditionLeaves(asMap(item), arrayContext, leaves);
            }
        }
    }

    private boolean hasDuplicates(List<String> values) {
        return new LinkedHashSet<>(values).size() != values.size();
    }

    private String locationFailure(String reason) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR][LOCATION] coverage failed reason=" + reason);
        return reason;
    }

    private void logBase(AlertVerificationOutcome outcome) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] decision=" + outcome.decision());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] requiredSources=" + outcome.requiredSources()
                + " interpreterType=" + outcome.interpreterType()
                + " triggerType=" + outcome.triggerType()
                + " evaluationMode=" + outcome.evaluationMode()
                + " inputModel=" + outcome.inputModel()
                + " outputModel=" + outcome.outputModel());
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] technicalSpecification present="
                + (outcome.technicalSpecification() != null && !outcome.technicalSpecification().isEmpty()));
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] agentBlueprintPreview present="
                + (outcome.agentBlueprintPreview() != null && !outcome.agentBlueprintPreview().isEmpty()));
    }

    private AlertVerificationOutcome fail(AlertVerificationOutcome outcome, String reason) {
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][VALIDATOR] Validation failed: " + reason);
        return rejected(outcome, reason);
    }

    private AlertVerificationOutcome rejected(AlertVerificationOutcome outcome, String reason) {
        String safeReason = "Scheduled ServiceData verification failed validation: " + reason;
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Scheduled ServiceData verification failed validation.",
                safeReason,
                0.0,
                outcome == null ? null : outcome.provider(),
                outcome == null ? null : outcome.model(),
                outcome == null ? "alert-scheduled-verify-mvp-v1" : outcome.promptVersion(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                outcome == null ? null : outcome.requirementCoverage(),
                List.of(safeReason),
                outcome == null || outcome.safetyChecks() == null ? List.of() : outcome.safetyChecks());
    }

    private String forbiddenOutputContamination(Object... roots) {
        for (Object root : roots) {
            String marker = findForbiddenMarker(root);
            if (marker != null) {
                return marker;
            }
        }
        return null;
    }

    private String findForbiddenMarker(Object value) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String keyMarker = findForbiddenMarker(String.valueOf(entry.getKey()));
                if (keyMarker != null) {
                    return keyMarker;
                }
                String valueMarker = findForbiddenMarker(entry.getValue());
                if (valueMarker != null) {
                    return valueMarker;
                }
            }
            String triggerType = stringValue(map.get("triggerType"));
            if ("EVENT".equals(triggerType)) {
                return "triggerType EVENT";
            }
            String inputModel = stringValue(map.get("inputModel"));
            if ("ServiceDataV2".equals(inputModel)) {
                return "inputModel ServiceDataV2";
            }
            return null;
        }
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                String marker = findForbiddenMarker(item);
                if (marker != null) {
                    return marker;
                }
            }
            return null;
        }
        String text = stringValue(value);
        if (text == null) {
            return null;
        }
        for (String marker : FORBIDDEN_OUTPUT_MARKERS) {
            if (text.contains(marker)) {
                return marker;
            }
        }
        if ("EVENT".equals(text)) {
            return "triggerType EVENT";
        }
        return null;
    }

    private boolean containsOnlyServiceData(List<String> sources) {
        List<String> normalized = normalizedList(sources);
        return normalized.size() == 1 && normalized.contains(SERVICE_DATA);
    }

    private List<String> normalizedList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .toList();
    }

    private List<String> stringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::stringValue)
                    .filter(item -> item != null && !item.isBlank())
                    .toList();
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private boolean presentMap(Object value) {
        Map<String, Object> map = asMap(value);
        return map != null && !map.isEmpty();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Boolean.parseBoolean(stringValue);
        }
        return null;
    }

    private Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ConditionLeaf(
            String field,
            String resolvedField,
            String operator,
            Object value,
            List<Object> values) {
    }
}
