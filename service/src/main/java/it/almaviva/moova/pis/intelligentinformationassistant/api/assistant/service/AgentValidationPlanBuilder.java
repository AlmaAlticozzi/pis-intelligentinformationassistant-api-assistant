package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationExample;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataTemporalCapabilityCatalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class AgentValidationPlanBuilder {

    AgentValidationPlan build(
            AlertAgentGenerationPreviewData data,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        if (conditionSummary.temporalFilter()) {
            return temporalPlan(conditionSummary);
        }
        if (conditionSummary.platformChange()
                || conditionSummary.platformConstraint()
                || conditionSummary.platformComparison()
                || conditionSummary.platformMovement()) {
            return platformPlan(conditionSummary);
        }
        if (conditionSummary.stopPointIds() != null && !conditionSummary.stopPointIds().isEmpty()) {
            String ids = String.join(", ", conditionSummary.stopPointIds());
            String candidate = conditionSummary.stopPointIds().getFirst();
            return new AgentValidationPlan()
                    .positiveExamples(List.of(example(
                            "ServiceData event with event.stopPoint.id equal to resolved PIS candidate " + candidate + ".",
                            AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                    .negativeExamples(List.of(example(
                            "ServiceData event with event.stopPoint.id different from resolved PIS candidate(s) " + ids + ".",
                            AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                    .edgeCases(List.of(
                            "A ServiceData event with a similar stopPoint.nameLong but a different stopPoint.id must not match the id-based location condition."));
        }
        if (conditionSummary.delay()
                && conditionSummary.location() != null
                && !conditionSummary.location().isBlank()) {
            String journey = conditionSummary.serviceType() == null
                    ? "a journey"
                    : "an " + conditionSummary.serviceType() + " journey";
            String categoryGuard = conditionSummary.serviceType() == null
                    ? "ServiceData delayed journey for a different service category."
                    : "ServiceData delayed non-" + conditionSummary.serviceType()
                            + " journey at " + conditionSummary.location() + ".";
            return new AgentValidationPlan()
                    .positiveExamples(List.of(
                            example("ServiceData event for " + journey + " delayed at "
                                            + conditionSummary.location() + ".",
                                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION),
                            example("ServiceData event with delay information for " + journey + " at "
                                            + conditionSummary.location() + ".",
                                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                    .negativeExamples(List.of(
                            example("ServiceData event for " + journey + " at "
                                            + conditionSummary.location() + " without delay.",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                            example("ServiceData delayed journey at a different stop point.",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                            example(categoryGuard, AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                    .edgeCases(List.of(
                            "ServiceData event without delay amount or delay status is handled safely.",
                            "ServiceData event without service category cannot satisfy the Intercity condition.",
                            "ServiceData event without stopPoint.nameLong does not match the location condition."));
        }
        if (conditionSummary.cancellation()
                && conditionSummary.location() != null
                && !conditionSummary.location().isBlank()
                && !conditionSummary.cancellationLeaves().isEmpty()) {
            List<AgentValidationExample> positiveExamples = new ArrayList<>();
            conditionSummary.cancellationLeaves().stream()
                    .limit(2)
                    .forEach(leaf -> positiveExamples.add(example(
                            "ServiceData event with " + statusLabel(leaf.field()) + " status " + leaf.value()
                                    + " at " + conditionSummary.location() + ".",
                            AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)));
            return new AgentValidationPlan()
                    .positiveExamples(positiveExamples)
                    .negativeExamples(List.of(
                            example("ServiceData event for " + conditionSummary.location()
                                            + " without cancellation statuses.",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                            example("ServiceData cancellation event for a different stop point.",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                    .edgeCases(List.of(
                            "ServiceData event without journeyId or vehicleJourneyName is rejected safely or produces a guarded output.",
                            "ServiceData event without stopPoint.nameLong does not match the location condition.",
                            "ServiceData event with empty arrivalStatuses and departureStatuses does not produce output."));
        }

        String eventName = data.interpretedEventNames() == null || data.interpretedEventNames().isEmpty()
                ? "a matching interpreted event"
                : data.interpretedEventNames().getFirst();
        return new AgentValidationPlan()
                .positiveExamples(List.of(example(
                        "ServiceData event with eventName " + eventName + ".",
                        AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                .negativeExamples(List.of(example(
                        "ServiceData event with an eventName outside the interpreted event set.",
                        AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                .edgeCases(List.of("ServiceData event without journeyId or with an incomplete target is rejected safely."));
    }

    private AgentValidationPlan platformPlan(AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        List<AgentValidationExample> positiveExamples = new ArrayList<>();
        List<AgentValidationExample> negativeExamples = new ArrayList<>();
        List<String> edgeCases = new ArrayList<>();
        if (conditionSummary.platformConstraint()) {
            positiveExamples.add(example(
                    "ServiceData platform \"Platform 1\" matches user platform \"1\".",
                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION));
            negativeExamples.add(example(
                    "ServiceData platform \"Platform 11\" does not match user platform \"1\".",
                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT));
            edgeCases.add("ServiceData platform \"Platform 01\" matches normalized user platform \"1\".");
        }
        if (hasPlatformOperator(conditionSummary, "PLATFORM_NUMBER_GREATER_THAN")) {
            positiveExamples.add(example(
                    currentPlatformEventPrefix(conditionSummary)
                            + " and platform \"Platform 6\" satisfies PLATFORM_NUMBER_GREATER_THAN 5.",
                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION));
            negativeExamples.add(example(
                    "ServiceData platform \"Platform 5\" does not satisfy PLATFORM_NUMBER_GREATER_THAN 5.",
                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT));
        }
        if (hasPlatformOperator(conditionSummary, "PLATFORM_NUMBER_EVEN")) {
            positiveExamples.add(example(
                    currentPlatformEventPrefix(conditionSummary)
                            + " and platform \"Platform 4\" satisfies PLATFORM_NUMBER_EVEN.",
                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION));
            negativeExamples.add(example(
                    "ServiceData platform \"Platform 5\" does not satisfy PLATFORM_NUMBER_EVEN.",
                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT));
        }
        if (hasPlatformOperator(conditionSummary, "PLATFORM_HAS_LETTER_SUFFIX")) {
            edgeCases.add("ServiceData platform \"Platform 03A\" has main number 3 and satisfies PLATFORM_HAS_LETTER_SUFFIX.");
        }
        if (conditionSummary.platformConstraintLeaves().stream()
                .anyMatch(leaf -> leaf.operator() != null
                        && (leaf.operator().startsWith("PLATFORM_NUMBER_")
                        || "PLATFORM_HAS_LETTER_SUFFIX".equals(leaf.operator())))) {
            edgeCases.add("The current ServiceData event must also match payload.ongroundServiceEvent.eventsType.");
        }
        if (conditionSummary.platformConstraintLeaves().stream()
                .anyMatch(leaf -> "IN_PLATFORMS".equals(leaf.operator())
                        || "NOT_IN_PLATFORMS".equals(leaf.operator()))) {
            positiveExamples.add(example(
                    "ServiceData platform \"Platform 4\" matches IN_PLATFORMS [\"1\", \"4\"].",
                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION));
            negativeExamples.add(example(
                    "ServiceData platform \"Platform 14\" does not match IN_PLATFORMS [\"1\", \"4\"].",
                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT));
        }
        if (conditionSummary.platformChange()) {
            positiveExamples.add(example(
                    "ServiceData current eventsType contains DEPARTURE_PLATFORM_CHANGED and timetabled departure platform differs from actual departure platform.",
                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION));
            negativeExamples.add(example(
                    "ServiceData current eventsType contains DEPARTURE_PLATFORM_CHANGED but timetabled departure platform equals actual departure platform.",
                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT));
        }
        if (conditionSummary.platformMovement()) {
            positiveExamples.add(example(
                    "ServiceData previous departure platform is \"5\" and actual departure platform matches IN_PLATFORMS [\"7\", \"8\"].",
                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION));
            negativeExamples.add(example(
                    "ServiceData previous departure platform is \"5\" but actual departure platform is \"9\".",
                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT));
        }
        if (positiveExamples.isEmpty()) {
            positiveExamples.add(example(
                    "ServiceData timetabled platform differs from the compared actual platform.",
                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION));
            negativeExamples.add(example(
                    "ServiceData timetabled platform equals the compared actual platform.",
                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT));
        }
        edgeCases.add("Human platform matching is normalized: platform \"1\" must not match platform \"11\".");
        return new AgentValidationPlan()
                .positiveExamples(List.copyOf(positiveExamples))
                .negativeExamples(List.copyOf(negativeExamples))
                .edgeCases(List.copyOf(edgeCases));
    }

    private boolean hasPlatformOperator(
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary,
            String operator) {
        return conditionSummary.platformConstraintLeaves().stream()
                .anyMatch(leaf -> operator.equals(leaf.operator()));
    }

    private String currentPlatformEventPrefix(AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        return "ServiceData current eventsType contains "
                + java.util.Optional.ofNullable(findCurrentPlatformEvent(conditionSummary.condition()))
                .orElse("DEPARTED");
    }

    private String findCurrentPlatformEvent(Object node) {
        if (node instanceof Map<?, ?> map) {
            if ("payload.ongroundServiceEvent.eventsType".equals(String.valueOf(map.get("field")))) {
                if (map.get("value") != null) {
                    return String.valueOf(map.get("value"));
                }
                if (map.get("values") instanceof List<?> values && !values.isEmpty()) {
                    return String.valueOf(values.getFirst());
                }
            }
            for (Object value : map.values()) {
                String event = findCurrentPlatformEvent(value);
                if (event != null) {
                    return event;
                }
            }
        } else if (node instanceof List<?> list) {
            for (Object value : list) {
                String event = findCurrentPlatformEvent(value);
                if (event != null) {
                    return event;
                }
            }
        }
        return null;
    }

    private AgentValidationPlan temporalPlan(AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        AgentValidationPlan nestedTransitPlan = nestedTransitTemporalPlan(conditionSummary);
        if (nestedTransitPlan != null) {
            return nestedTransitPlan;
        }
        AgentPreviewConditionExtractor.ArrayElementCondition correlated =
                conditionSummary.arrayConditions().stream()
                        .filter(condition -> condition.leaves().stream().anyMatch(this::isTemporalLeaf))
                        .findFirst()
                        .orElse(null);
        if (correlated != null) {
            List<AgentPreviewConditionExtractor.ConditionLeaf> temporalLeaves = correlated.leaves().stream()
                    .filter(this::isTemporalLeaf)
                    .toList();
            AgentPreviewConditionExtractor.ConditionLeaf temporalLeaf = temporalLeaves.getFirst();
            String stopPoint = correlated.leaves().stream()
                    .filter(leaf -> "stopPoint.nameLong".equals(leaf.field()))
                    .map(AgentPreviewConditionExtractor.ConditionLeaf::value)
                    .findFirst()
                    .orElse("the requested stop point");
            String temporalRequirement = temporalRequirement(temporalLeaves);
            String elementLabel = elementLabel(correlated.path());
            String callLabel = callLabel(correlated.path());
            return new AgentValidationPlan()
                    .positiveExamples(List.of(example(
                            "ServiceData event whose " + elementLabel + " has stopPoint " + stopPoint
                                    + " and " + temporalRequirement + ".",
                            AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                    .negativeExamples(List.of(
                            example("ServiceData event with a " + callLabel + " for " + stopPoint
                                            + " but " + negativeTemporalRequirement(temporalLeaf) + ".",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                            example("ServiceData event with a " + callLabel + " satisfying " + temporalRequirement
                                            + " but for a stop point different from " + stopPoint + ".",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                    .edgeCases(List.of(
                            "A matching stop point and matching temporal predicate on different "
                                    + elementPluralLabel(correlated.path())
                                    + " do not satisfy anyElement.",
                            "A " + callLabel + " without " + temporalLeaf.field() + " does not match the temporal condition."));
        }
        List<AgentPreviewConditionExtractor.ConditionLeaf> temporalLeaves = findTemporalLeaves(conditionSummary.condition());
        AgentPreviewConditionExtractor.ConditionLeaf temporalLeaf = temporalLeaves.isEmpty() ? null : temporalLeaves.getFirst();
        String window = temporalLeaves.isEmpty() ? "the configured temporal predicate" : temporalRequirement(temporalLeaves);
        String field = temporalLeaf == null ? "event timestamp" : temporalLeaf.field();
        return new AgentValidationPlan()
                .positiveExamples(List.of(example(
                        "ServiceData event with " + field + " satisfying " + window + ".",
                        AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                .negativeExamples(List.of(example(
                        "ServiceData event with " + field + " where " + negativeTemporalRequirement(temporalLeaf) + ".",
                        AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                .edgeCases(List.of("ServiceData event without " + field + " does not match the temporal condition."));
    }

    private AgentValidationPlan nestedTransitTemporalPlan(
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
        AgentPreviewConditionExtractor.ArrayElementCondition journeyDetail =
                conditionSummary.arrayConditions().stream()
                        .filter(condition -> condition.path().endsWith("stopPointsJourneyDetails[]"))
                        .filter(condition -> condition.leaves().stream().anyMatch(this::isTemporalLeaf))
                        .findFirst()
                        .orElse(null);
        AgentPreviewConditionExtractor.ArrayElementCondition nextTransit =
                conditionSummary.arrayConditions().stream()
                        .filter(condition -> condition.path().endsWith("nextTransitCalls[]"))
                        .filter(condition -> condition.leaves().stream().anyMatch(this::isTemporalLeaf))
                        .findFirst()
                        .orElse(null);
        if (journeyDetail == null || nextTransit == null) {
            return null;
        }
        AgentPreviewConditionExtractor.ConditionLeaf originLeaf = journeyDetail.leaves().stream()
                .filter(leaf -> "timetabledCallStart.stopPoint.nameLong".equals(leaf.field()))
                .findFirst()
                .orElse(null);
        AgentPreviewConditionExtractor.ConditionLeaf transitStopLeaf = nextTransit.leaves().stream()
                .filter(leaf -> "stopPoint.nameLong".equals(leaf.field()))
                .findFirst()
                .orElse(null);
        List<AgentPreviewConditionExtractor.ConditionLeaf> temporalLeaves = nextTransit.leaves().stream()
                .filter(this::isTemporalLeaf)
                .toList();
        if (originLeaf == null || transitStopLeaf == null || temporalLeaves.isEmpty()) {
            return null;
        }
        AgentPreviewConditionExtractor.ConditionLeaf temporalLeaf = temporalLeaves.getFirst();
        String origin = originLeaf.value();
        String transitStop = transitStopLeaf.value();
        String temporalRequirement = temporalRequirement(temporalLeaves);
        return new AgentValidationPlan()
                .positiveExamples(List.of(example(
                        "ServiceData event whose same stopPointsJourneyDetails element has origin stopPoint "
                                + origin + " and contains a same nextTransitCalls element with stopPoint "
                                + transitStop + " and " + temporalRequirement + ".",
                        AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                .negativeExamples(List.of(
                        example("ServiceData event with origin stopPoint different from " + origin
                                        + " but a nextTransitCalls element for " + transitStop
                                        + " satisfying " + temporalRequirement + ".",
                                AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                        example("ServiceData event whose same stopPointsJourneyDetails element has origin stopPoint "
                                        + origin + " but no nextTransitCalls element for " + transitStop + ".",
                                AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                        example("ServiceData event whose same stopPointsJourneyDetails element has origin stopPoint "
                                        + origin + " and nextTransitCalls stopPoint " + transitStop
                                        + " but " + negativeTemporalRequirement(temporalLeaf) + ".",
                                AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                .edgeCases(List.of(
                        "Origin and nextTransitCalls predicates on different stopPointsJourneyDetails elements do not satisfy nested anyElement.",
                        "A nextTransitCalls element without " + temporalLeaf.field()
                                + " does not match the temporal condition."));
    }

    private List<AgentPreviewConditionExtractor.ConditionLeaf> findTemporalLeaves(Object node) {
        List<AgentPreviewConditionExtractor.ConditionLeaf> leaves = new ArrayList<>();
        collectTemporalLeaves(node, leaves);
        return leaves;
    }

    private void collectTemporalLeaves(Object node, List<AgentPreviewConditionExtractor.ConditionLeaf> leaves) {
        if (node instanceof Map<?, ?> rawNode) {
            Map<String, Object> map = mapValue(rawNode);
            if (ServiceDataTemporalCapabilityCatalog.isTemporalOperator(String.valueOf(map.get("operator")))) {
                leaves.add(new AgentPreviewConditionExtractor.ConditionLeaf(
                        String.valueOf(map.get("field")),
                        String.valueOf(map.get("operator")),
                        map.get("value") == null ? List.of() : List.of(String.valueOf(map.get("value"))),
                        map.get("value")));
            }
            for (Object value : map.values()) {
                collectTemporalLeaves(value, leaves);
            }
        } else if (node instanceof List<?> list) {
            for (Object value : list) {
                collectTemporalLeaves(value, leaves);
            }
        }
    }

    private boolean isTemporalLeaf(AgentPreviewConditionExtractor.ConditionLeaf leaf) {
        return ServiceDataTemporalCapabilityCatalog.isTemporalOperator(leaf.operator());
    }

    private String elementLabel(String path) {
        if (path == null) {
            return "same array element";
        }
        if (path.endsWith("nextCalls[]")) {
            return "same next call";
        }
        if (path.endsWith("nextTransitCalls[]")) {
            return "same next transit call";
        }
        if (path.endsWith("stopPointsJourneyDetails[]")) {
            return "same stopPointsJourneyDetails element";
        }
        return "same array element";
    }

    private String callLabel(String path) {
        if (path == null) {
            return "journey detail element";
        }
        if (path.endsWith("nextCalls[]")) {
            return "next call";
        }
        if (path.endsWith("nextTransitCalls[]")) {
            return "next transit call";
        }
        if (path.endsWith("stopPointsJourneyDetails[]")) {
            return "journey detail element";
        }
        return "array element";
    }

    private String elementPluralLabel(String path) {
        if (path == null) {
            return "array elements";
        }
        if (path.endsWith("nextCalls[]")) {
            return "next calls";
        }
        if (path.endsWith("nextTransitCalls[]")) {
            return "next transit calls";
        }
        if (path.endsWith("stopPointsJourneyDetails[]")) {
            return "stopPointsJourneyDetails elements";
        }
        return "array elements";
    }

    private String temporalRequirement(List<AgentPreviewConditionExtractor.ConditionLeaf> leaves) {
        return leaves.stream().map(this::temporalDescription).collect(java.util.stream.Collectors.joining(" and "));
    }

    private String temporalDescription(AgentPreviewConditionExtractor.ConditionLeaf leaf) {
        if (leaf == null) {
            return "the configured temporal predicate is not satisfied";
        }
        Map<String, Object> value = mapValue(leaf.rawValue());
        if ("LOCAL_TIME_BETWEEN".equals(leaf.operator())) {
            return leaf.field() + " inside " + value.get("start") + "-" + value.get("end")
                    + " local time (" + value.get("timezone") + ")";
        }
        List<String> days = stringList(value.get("days"));
        if ("LOCAL_DAY_OF_WEEK_IN".equals(leaf.operator())) {
            return leaf.field() + " local day included in " + days + " (" + value.get("timezone") + ")";
        }
        return leaf.field() + " local day not included in " + days + " (" + value.get("timezone") + ")";
    }

    private String negativeTemporalRequirement(AgentPreviewConditionExtractor.ConditionLeaf leaf) {
        if (leaf == null) {
            return "the configured temporal predicate is not satisfied";
        }
        Map<String, Object> value = mapValue(leaf.rawValue());
        if ("LOCAL_TIME_BETWEEN".equals(leaf.operator())) {
            return leaf.field() + " is outside " + value.get("start") + "-" + value.get("end");
        }
        List<String> days = stringList(value.get("days"));
        if ("LOCAL_DAY_OF_WEEK_IN".equals(leaf.operator())) {
            return leaf.field() + " local day is not included in " + days;
        }
        return leaf.field() + " local day is excluded by " + days;
    }

    private List<String> stringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of(String.valueOf(value));
    }

    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, nestedValue) -> result.put(String.valueOf(key), nestedValue));
        return result;
    }

    private String statusLabel(String field) {
        if (field != null && field.toLowerCase().contains("departure")) {
            return "departure";
        }
        return "arrival";
    }

    private AgentValidationExample example(
            String description,
            AgentValidationExample.ExpectedOutputEnum expectedOutput) {
        return new AgentValidationExample().description(description).expectedOutput(expectedOutput);
    }
}
