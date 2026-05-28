package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataTemporalCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class AgentPreviewConditionExtractor {

    private static final Set<String> LEAF_FIELDS = Set.of("type", "field", "operator", "value", "values");
    private static final Set<String> GROUP_FIELDS = Set.of("type", "all", "any");
    private static final Set<String> ANY_ELEMENT_FIELDS = Set.of("type", "anyElement");
    private static final Set<String> VALUELESS_OPERATORS = Set.of("EXISTS", "NOT_NULL", "NOT_EMPTY");
    private static final Pattern LOCATION_IN_TEXT = Pattern.compile(
            "(?i)\\b(?:at|a)\\s+([A-Z\\p{L}][\\p{L}0-9 '\\-]+?)(?:[.!?,]|$)");

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
        List<ArrayElementCondition> arrayConditions = new ArrayList<>();
        List<RenderIssue> renderIssues = new ArrayList<>();
        boolean partial = !condition.isEmpty()
                && !collectLeaves(condition, "condition", leaves, arrayConditions, renderIssues);
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
        location = readableLocation(location);
        List<ConditionLeaf> cancellationLeaves = leaves.stream()
                .filter(this::isCancellationLeaf)
                .toList();
        List<ConditionLeaf> platformChangeLeaves = leaves.stream()
                .filter(this::isPlatformChangeLeaf)
                .toList();
        boolean delay = leaves.stream().anyMatch(this::isDelayLeaf)
                || containsDelay(data.interpretedEventNames())
                || containsDelay(Arrays.asList(data.prompt(), stringValue(mapValue(data.agentBlueprintPreview().get("output"))
                        .get("reasonTemplate"))));
        String serviceType = findServiceType(leaves, data);
        if (location == null && delay) {
            location = extractLocationFromText(firstString(
                    mapValue(data.agentBlueprintPreview().get("output")).get("reasonTemplate"),
                    data.prompt()));
        }
        Set<String> dslOperators = leaves.stream()
                .map(ConditionLeaf::operator)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        boolean temporalFilter = dslOperators.stream().anyMatch(ServiceDataTemporalCapabilityCatalog::isTemporalOperator);
        if (temporalFilter) {
            System.out.println("[IIA][AGENT_PREVIEW][TEMPORAL] recognized operators=" + dslOperators);
        }
        if (!arrayConditions.isEmpty()) {
            System.out.println("[IIA][AGENT_PREVIEW][ARRAY] extracted anyElement paths="
                    + arrayConditions.stream().map(ArrayElementCondition::path).toList());
        }

        return new ConditionSummary(
                condition,
                conditionType,
                location,
                !cancellationLeaves.isEmpty(),
                cancellationLeaves,
                delay,
                !platformChangeLeaves.isEmpty(),
                platformChangeLeaves,
                serviceType,
                dslOperators,
                temporalFilter,
                List.copyOf(arrayConditions),
                partial,
                List.copyOf(renderIssues));
    }

    private boolean collectLeaves(
            Map<String, Object> node,
            String path,
            List<ConditionLeaf> leaves,
            List<ArrayElementCondition> arrayConditions,
            List<RenderIssue> issues) {
        if (isLeaf(node)) {
            leaves.add(new ConditionLeaf(
                    stringValue(node.get("field")),
                    stringValue(node.get("operator")),
                    values(node),
                    node.get("value")));
            return LEAF_FIELDS.containsAll(node.keySet());
        }
        if (node.containsKey("field") || node.containsKey("operator")) {
            String operator = stringValue(node.get("operator"));
            String reason;
            if (!node.containsKey("field")) {
                reason = "missing field";
            } else if (!node.containsKey("operator")) {
                reason = "missing operator";
            } else {
                reason = "missing value/values for operator " + operator;
            }
            issues.add(issue(path, reason, operator, node));
            return false;
        }
        if (node.containsKey("anyElement")) {
            if (!(node.get("anyElement") instanceof Map<?, ?> rawArrayCondition)) {
                issues.add(issue(path + ".anyElement", "expected anyElement object", null, node));
                return false;
            }
            Map<String, Object> arrayCondition = mapValue(rawArrayCondition);
            String arrayPath = stringValue(arrayCondition.get("path"));
            Map<String, Object> nestedCondition = mapValue(arrayCondition.get("conditions"));
            if (arrayPath == null || arrayPath.isBlank() || nestedCondition.isEmpty()) {
                issues.add(issue(path + ".anyElement", "expected path and conditions", null, arrayCondition));
                return false;
            }
            List<ConditionLeaf> elementLeaves = new ArrayList<>();
            boolean supported = ANY_ELEMENT_FIELDS.containsAll(node.keySet())
                    && collectLeaves(nestedCondition, path + ".anyElement.conditions",
                    elementLeaves, arrayConditions, issues);
            leaves.addAll(elementLeaves);
            arrayConditions.add(new ArrayElementCondition(arrayPath, nestedCondition, List.copyOf(elementLeaves)));
            return supported;
        }
        boolean supported = GROUP_FIELDS.containsAll(node.keySet());
        boolean foundGroup = false;
        for (String group : List.of("all", "any")) {
            Object children = node.get(group);
            if (children instanceof List<?> list) {
                foundGroup = true;
                for (int index = 0; index < list.size(); index++) {
                    Object child = list.get(index);
                    String childPath = path + "." + group + "[" + index + "]";
                    if (!(child instanceof Map<?, ?> map)) {
                        issues.add(issue(childPath, "expected condition object", null, Map.of("value", child)));
                        supported = false;
                        continue;
                    }
                    supported &= collectLeaves(mapValue(map), childPath, leaves, arrayConditions, issues);
                }
            }
        }
        if (!foundGroup) {
            issues.add(issue(path, "expected all/any or field/operator/value node",
                    stringValue(node.get("operator")), node));
        } else if (!supported && issues.isEmpty()) {
            issues.add(issue(path, "unsupported condition node keys", stringValue(node.get("operator")), node));
        }
        return foundGroup && supported;
    }

    private boolean isLeaf(Map<String, Object> node) {
        return node.containsKey("field")
                && node.containsKey("operator")
                && (node.get("value") != null
                || (node.get("values") instanceof List<?> values && !values.isEmpty())
                || VALUELESS_OPERATORS.contains(stringValue(node.get("operator"))));
    }

    private RenderIssue issue(String path, String reason, String operator, Map<String, Object> node) {
        String snippet = String.valueOf(node);
        if (snippet.length() > 240) {
            snippet = snippet.substring(0, 240) + "...";
        }
        return new RenderIssue(path, reason, operator, List.copyOf(node.keySet()), snippet);
    }

    private boolean isCancellationLeaf(ConditionLeaf leaf) {
        return leaf.values().stream()
                .map(String::toUpperCase)
                .anyMatch(value -> value.contains("CANCELLATION") || value.contains("CANCELLED"));
    }

    private boolean isDelayLeaf(ConditionLeaf leaf) {
        String field = leaf.field() == null ? "" : leaf.field().toUpperCase();
        return field.contains("DELAY") || leaf.values().stream()
                .map(String::toUpperCase)
                .anyMatch(value -> value.contains("DELAY") || value.contains("RITARD"));
    }

    private boolean isPlatformChangeLeaf(ConditionLeaf leaf) {
        String field = leaf.field() == null ? "" : leaf.field().toUpperCase();
        return field.contains("PLATFORM") && field.contains("CHANGE")
                || leaf.values().stream()
                        .map(String::toUpperCase)
                        .anyMatch(value -> value.contains("PLATFORM_CHANGED")
                                || value.contains("PLATFORM_CHANGE"));
    }

    private boolean containsDelay(List<String> values) {
        return values != null && values.stream()
                .filter(value -> value != null)
                .map(String::toUpperCase)
                .anyMatch(value -> value.contains("DELAY") || value.contains("RITARD"));
    }

    private String findServiceType(List<ConditionLeaf> leaves, AlertAgentGenerationPreviewData data) {
        boolean intercity = leaves.stream()
                .flatMap(leaf -> leaf.values().stream())
                .anyMatch(this::isIntercity)
                || containsIntercity(data.prompt())
                || containsIntercity(String.valueOf(data.agentBlueprintPreview()))
                || containsIntercity(String.valueOf(data.technicalSpecification()));
        return intercity ? "Intercity" : null;
    }

    private boolean isIntercity(String value) {
        return value != null && ("INTERCITY".equalsIgnoreCase(value.trim()) || "IC".equalsIgnoreCase(value.trim()));
    }

    private boolean containsIntercity(String value) {
        return value != null && Pattern.compile("(?i)\\bintercity\\b").matcher(value).find();
    }

    private String extractLocationFromText(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = LOCATION_IN_TEXT.matcher(text);
        return matcher.find() ? readableLocation(matcher.group(1).trim()) : null;
    }

    private String readableLocation(String location) {
        if (location == null || !location.equals(location.toLowerCase())) {
            return location;
        }
        String[] words = location.split("\\s+");
        return IntStream.range(0, words.length)
                .mapToObj(index -> words[index].isEmpty()
                        ? words[index]
                        : Character.toUpperCase(words[index].charAt(0)) + words[index].substring(1))
                .collect(Collectors.joining(" "));
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

    record ConditionLeaf(String field, String operator, List<String> values, Object rawValue) {
        String value() {
            return values.isEmpty() ? null : values.getFirst();
        }
    }

    record ArrayElementCondition(String path, Map<String, Object> condition, List<ConditionLeaf> leaves) {
    }

    record ConditionSummary(
            Map<String, Object> condition,
            String conditionType,
            String location,
            boolean cancellation,
            List<ConditionLeaf> cancellationLeaves,
            boolean delay,
            boolean platformChange,
            List<ConditionLeaf> platformChangeLeaves,
            String serviceType,
            Set<String> dslOperators,
            boolean temporalFilter,
            List<ArrayElementCondition> arrayConditions,
            boolean partial,
            List<RenderIssue> renderIssues) {
    }

    record RenderIssue(
            String path,
            String reason,
            String operator,
            List<String> keys,
            String snippet) {
    }
}
