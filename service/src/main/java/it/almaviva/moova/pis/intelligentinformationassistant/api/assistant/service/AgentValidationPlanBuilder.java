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
        if (conditionSummary.platformChange()) {
            return new AgentValidationPlan()
                    .positiveExamples(List.of(
                            example("ServiceData event with departure status DEPARTURE_PLATFORM_CHANGED.",
                                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION),
                            example("ServiceData event with arrival status ARRIVAL_PLATFORM_CHANGED.",
                                    AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                    .negativeExamples(List.of(
                            example("ServiceData event without arrival or departure platform change statuses.",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                            example("ServiceData event with platform confirmed but not changed.",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                    .edgeCases(List.of(
                            "ServiceData event without vehicleJourneyName is rejected safely or produces a guarded output.",
                            "ServiceData event without stopPoint.nameLong still detects the platform change but uses a guarded reason template.",
                            "ServiceData event with both arrival and departure platform changes produces a single guarded candidate output."));
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

    private AgentValidationPlan temporalPlan(AgentPreviewConditionExtractor.ConditionSummary conditionSummary) {
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
            String elementLabel = correlated.path().endsWith("nextCalls[]") ? "same nextCall" : "same array element";
            return new AgentValidationPlan()
                    .positiveExamples(List.of(example(
                            "ServiceData event whose " + elementLabel + " has stopPoint " + stopPoint
                                    + " and " + temporalRequirement + ".",
                            AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                    .negativeExamples(List.of(
                            example("ServiceData event with a nextCall for " + stopPoint
                                            + " but " + negativeTemporalRequirement(temporalLeaf) + ".",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                            example("ServiceData event with a nextCall satisfying " + temporalRequirement
                                            + " but for a stop point different from " + stopPoint + ".",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                    .edgeCases(List.of(
                            "A matching stop point and matching temporal predicate on different "
                                    + (correlated.path().endsWith("nextCalls[]") ? "nextCalls" : "array elements")
                                    + " do not satisfy anyElement.",
                            "A nextCall without " + temporalLeaf.field() + " does not match the temporal condition."));
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
