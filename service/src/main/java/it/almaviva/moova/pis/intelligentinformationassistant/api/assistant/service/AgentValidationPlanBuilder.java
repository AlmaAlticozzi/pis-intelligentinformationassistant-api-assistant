package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationExample;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

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
            AgentPreviewConditionExtractor.ConditionLeaf temporalLeaf = correlated.leaves().stream()
                    .filter(this::isTemporalLeaf)
                    .findFirst()
                    .orElseThrow();
            String stopPoint = correlated.leaves().stream()
                    .filter(leaf -> "stopPoint.nameLong".equals(leaf.field()))
                    .map(AgentPreviewConditionExtractor.ConditionLeaf::value)
                    .findFirst()
                    .orElse("the requested stop point");
            String window = temporalWindow(temporalLeaf);
            return new AgentValidationPlan()
                    .positiveExamples(List.of(example(
                            "ServiceData event whose same nextCall has stopPoint " + stopPoint
                                    + " and " + temporalLeaf.field() + " inside " + window + ".",
                            AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                    .negativeExamples(List.of(
                            example("ServiceData event with a nextCall for " + stopPoint
                                            + " but " + temporalLeaf.field() + " outside " + window + ".",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT),
                            example("ServiceData event with a nextCall inside " + window
                                            + " but for a stop point different from " + stopPoint + ".",
                                    AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                    .edgeCases(List.of(
                            "A matching stop point and matching time on different nextCalls do not satisfy anyElement.",
                            "A nextCall without " + temporalLeaf.field() + " does not match the temporal condition."));
        }
        AgentPreviewConditionExtractor.ConditionLeaf temporalLeaf = findTemporalLeaf(conditionSummary.condition());
        String window = temporalLeaf == null ? "the configured local time window" : temporalWindow(temporalLeaf);
        String field = temporalLeaf == null ? "event timestamp" : temporalLeaf.field();
        return new AgentValidationPlan()
                .positiveExamples(List.of(example(
                        "ServiceData event with " + field + " inside " + window + ".",
                        AgentValidationExample.ExpectedOutputEnum.CANDIDATE_SUGGESTION)))
                .negativeExamples(List.of(example(
                        "ServiceData event with " + field + " outside " + window + ".",
                        AgentValidationExample.ExpectedOutputEnum.NO_OUTPUT)))
                .edgeCases(List.of("ServiceData event without " + field + " does not match the temporal condition."));
    }

    private AgentPreviewConditionExtractor.ConditionLeaf findTemporalLeaf(Object node) {
        if (node instanceof Map<?, ?> rawNode) {
            Map<String, Object> map = mapValue(rawNode);
            if ("LOCAL_TIME_BETWEEN".equals(String.valueOf(map.get("operator")))) {
                return new AgentPreviewConditionExtractor.ConditionLeaf(
                        String.valueOf(map.get("field")),
                        String.valueOf(map.get("operator")),
                        map.get("value") == null ? List.of() : List.of(String.valueOf(map.get("value"))),
                        map.get("value"));
            }
            for (Object value : map.values()) {
                AgentPreviewConditionExtractor.ConditionLeaf leaf = findTemporalLeaf(value);
                if (leaf != null) {
                    return leaf;
                }
            }
        } else if (node instanceof List<?> list) {
            for (Object value : list) {
                AgentPreviewConditionExtractor.ConditionLeaf leaf = findTemporalLeaf(value);
                if (leaf != null) {
                    return leaf;
                }
            }
        }
        return null;
    }

    private boolean isTemporalLeaf(AgentPreviewConditionExtractor.ConditionLeaf leaf) {
        return "LOCAL_TIME_BETWEEN".equals(leaf.operator());
    }

    private String temporalWindow(AgentPreviewConditionExtractor.ConditionLeaf leaf) {
        Map<String, Object> value = mapValue(leaf.rawValue());
        return value.get("start") + "-" + value.get("end") + " local time (" + value.get("timezone") + ")";
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
