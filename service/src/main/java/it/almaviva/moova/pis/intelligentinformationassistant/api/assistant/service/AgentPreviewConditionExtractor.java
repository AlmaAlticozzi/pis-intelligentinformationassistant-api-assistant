package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class AgentPreviewConditionExtractor {

    private static final Set<String> LEAF_FIELDS = Set.of("type", "field", "operator", "value", "values");
    private static final Set<String> GROUP_FIELDS = Set.of("type", "all", "any");

    ConditionSummary extract(AlertAgentGenerationPreviewData data) {
        Map<String, Object> parameters = mapValue(data.agentBlueprintPreview().get("parameters"));
        Map<String, Object> condition = mapValue(parameters.get("condition"));
        if (condition.isEmpty()) {
            condition = mapValue(data.technicalSpecification().get("condition"));
        }

        String conditionType = firstString(
                parameters.get("conditionType"),
                condition.get("type"),
                data.technicalSpecification().get("conditionType"));
        List<ConditionLeaf> leaves = new ArrayList<>();
        boolean partial = !condition.isEmpty() && !collectLeaves(condition, leaves);
        String location = leaves.stream()
                .filter(leaf -> leaf.field() != null && leaf.field().contains("stopPoint.nameLong"))
                .map(ConditionLeaf::value)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .or(() -> leaves.stream()
                        .filter(leaf -> leaf.field() != null && leaf.field().contains("stopPoint.nameShort"))
                        .map(ConditionLeaf::value)
                        .filter(value -> value != null && !value.isBlank())
                        .findFirst())
                .orElse(null);
        List<ConditionLeaf> cancellationLeaves = leaves.stream()
                .filter(this::isCancellationLeaf)
                .toList();
        Set<String> dslOperators = leaves.stream()
                .map(ConditionLeaf::operator)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        return new ConditionSummary(
                condition,
                conditionType,
                location,
                !cancellationLeaves.isEmpty(),
                cancellationLeaves,
                dslOperators,
                partial);
    }

    private boolean collectLeaves(Map<String, Object> node, List<ConditionLeaf> leaves) {
        if (isLeaf(node)) {
            leaves.add(new ConditionLeaf(
                    stringValue(node.get("field")),
                    stringValue(node.get("operator")),
                    values(node)));
            return LEAF_FIELDS.containsAll(node.keySet());
        }
        boolean supported = GROUP_FIELDS.containsAll(node.keySet());
        boolean foundGroup = false;
        for (String group : List.of("all", "any")) {
            Object children = node.get(group);
            if (children instanceof List<?> list) {
                foundGroup = true;
                for (Object child : list) {
                    if (!(child instanceof Map<?, ?> map)) {
                        supported = false;
                        continue;
                    }
                    supported &= collectLeaves(mapValue(map), leaves);
                }
            }
        }
        return foundGroup && supported;
    }

    private boolean isLeaf(Map<String, Object> node) {
        return node.containsKey("field")
                && node.containsKey("operator")
                && (node.get("value") != null
                || (node.get("values") instanceof List<?> values && !values.isEmpty()));
    }

    private boolean isCancellationLeaf(ConditionLeaf leaf) {
        return leaf.values().stream()
                .map(String::toUpperCase)
                .anyMatch(value -> value.contains("CANCELLATION") || value.contains("CANCELLED"));
    }

    private List<String> values(Map<String, Object> node) {
        if (node.get("value") != null) {
            return List.of(String.valueOf(node.get("value")));
        }
        if (node.get("values") instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, nestedValue) -> result.put(String.valueOf(key), nestedValue));
        return result;
    }

    private String firstString(Object... values) {
        for (Object value : values) {
            String result = stringValue(value);
            if (result != null && !result.isBlank()) {
                return result;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    record ConditionLeaf(String field, String operator, List<String> values) {
        String value() {
            return values.isEmpty() ? null : values.getFirst();
        }
    }

    record ConditionSummary(
            Map<String, Object> condition,
            String conditionType,
            String location,
            boolean cancellation,
            List<ConditionLeaf> cancellationLeaves,
            Set<String> dslOperators,
            boolean partial) {
    }
}
