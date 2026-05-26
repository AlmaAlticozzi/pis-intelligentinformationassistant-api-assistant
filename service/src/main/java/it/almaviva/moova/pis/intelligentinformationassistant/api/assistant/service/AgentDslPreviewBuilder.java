package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDslPreview;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class AgentDslPreviewBuilder {

    private static final java.util.Set<String> VALUELESS_OPERATORS = java.util.Set.of("EXISTS", "NOT_NULL", "NOT_EMPTY");

    BuildResult build(
            AlertAgentGenerationPreviewData data,
            AgentBlueprint blueprint,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary,
            List<String> sources,
            AgentBlueprintValidationResult validationResult) {
        String triggerType = firstString(
                enumValue(blueprint.getTriggerType()),
                data.technicalSpecification().get("triggerType"),
                "EVENT");
        String inputModel = firstString(data.inputModel(), data.technicalSpecification().get("inputModel"), "ServiceDataV2");
        String evaluationMode = firstString(
                blueprint.get("evaluationMode"),
                data.technicalSpecification().get("evaluationMode"),
                "STATELESS_EVENT_MATCH");
        Map<String, Object> output = mapValue(blueprint.get("output"));
        String outputType = firstString(output.get("type"), data.outputModel(), data.technicalSpecification().get("outputModel"),
                "CANDIDATE_SUGGESTION");
        String yamlOutputType = outputType.startsWith("AgentOutput.") ? outputType : "AgentOutput." + outputType;
        Map<String, Object> stateRequirements = blueprint.getStateRequirements();
        boolean requiresState = Boolean.TRUE.equals(stateRequirements.get("requiresState"));
        boolean explicitlyStateless = Boolean.FALSE.equals(stateRequirements.get("requiresState"));
        List<String> targetTypes = blueprint.getTargetTypes() == null
                ? List.of()
                : blueprint.getTargetTypes().stream().map(String::valueOf).toList();
        StringBuilder dsl = new StringBuilder()
                .append("schemaVersion: iia.agent.dsl/v1\n")
                .append("agent:\n")
                .append("  name: ").append(blueprint.getAgentName()).append("\n")
                .append("trigger:\n")
                .append("  type: ").append(triggerType).append("\n")
                .append("  source: ").append(sources.isEmpty() ? "UNSPECIFIED" : sources.getFirst()).append("\n")
                .append("  inputModel: ").append(inputModel).append("\n")
                .append("match:\n")
                .append("  evaluationMode: ").append(evaluationMode).append("\n");
        if (hasText(conditionSummary.conditionType())) {
            dsl.append("  conditionType: ").append(conditionSummary.conditionType()).append("\n");
        }

        boolean partial = conditionSummary.partial();
        if (conditionSummary.condition().isEmpty()) {
            appendEvents(dsl, data.interpretedEventNames());
        } else {
            partial |= !renderRootCondition(dsl, conditionSummary.condition());
        }
        boolean supportedByRuntime = validationResult.valid()
                && validationResult.runtimeSupported()
                && !partial
                && validationResult.unsupportedCapabilities().isEmpty();

        dsl.append("output:\n")
                .append("  type: ").append(yamlOutputType).append("\n");
        appendOptionalScalar(dsl, "  reasonTemplate: ", output.get("reasonTemplate"));
        appendOptionalScalar(dsl, "  operatorAdviceTemplate: ", output.get("operatorAdviceTemplate"));
        dsl.append("runtime:\n")
                .append("  requiresState: ").append(requiresState).append("\n")
                .append("  supportedByRuntime: ").append(supportedByRuntime).append("\n");

        String summary = supportedByRuntime
                ? "Deterministic DSL preview for a stateless ServiceData event-match Agent."
                : "Read-only DSL preview derived from verified Alert artifacts.";
        return new BuildResult(
                new AgentDslPreview()
                        .schemaVersion("iia.agent.dsl/v1")
                        .summary(summary)
                        .dsl(dsl.toString())
                        .supportedByRuntime(supportedByRuntime),
                partial,
                supportedByRuntime,
                new java.util.LinkedHashSet<>(validationResult.unsupportedCapabilities()));
    }

    private boolean renderRootCondition(StringBuilder output, Map<String, Object> condition) {
        boolean rendered = false;
        boolean supported = true;
        for (String key : List.of("all", "any")) {
            if (condition.get(key) instanceof List<?> children) {
                rendered = true;
                appendLine(output, 2, key + ":");
                supported &= renderChildren(output, children, 4);
            }
        }
        if (condition.containsKey("anyElement")) {
            rendered = true;
            supported &= renderAnyElement(output, condition.get("anyElement"), 2, false);
        }
        if (!rendered && isLeaf(condition)) {
            appendLine(output, 2, "condition:");
            renderLeaf(output, condition, 4, false);
            rendered = true;
        }
        return rendered && supported;
    }

    private boolean renderChildren(StringBuilder output, List<?> children, int indent) {
        boolean supported = true;
        for (Object child : children) {
            if (!(child instanceof Map<?, ?> rawNode)) {
                supported = false;
                continue;
            }
            Map<String, Object> node = mapValue(rawNode);
            if (isLeaf(node)) {
                renderLeaf(output, node, indent, true);
            } else if (node.containsKey("anyElement")) {
                supported &= renderAnyElement(output, node.get("anyElement"), indent, true);
            } else if (node.get("all") instanceof List<?> nested) {
                appendLine(output, indent, "- all:");
                supported &= renderChildren(output, nested, indent + 4);
            } else if (node.get("any") instanceof List<?> nested) {
                appendLine(output, indent, "- any:");
                supported &= renderChildren(output, nested, indent + 4);
            } else {
                supported = false;
            }
        }
        return supported;
    }

    private boolean renderAnyElement(StringBuilder output, Object value, int indent, boolean listItem) {
        Map<String, Object> anyElement = mapValue(value);
        String path = stringValue(anyElement.get("path"));
        Map<String, Object> conditions = mapValue(anyElement.get("conditions"));
        if (!hasText(path) || conditions.isEmpty()) {
            return false;
        }
        appendLine(output, indent, (listItem ? "- " : "") + "anyElement:");
        int propertyIndent = indent + 2;
        appendLine(output, propertyIndent, "path: " + path);
        boolean rendered = false;
        boolean supported = true;
        for (String group : List.of("all", "any")) {
            if (conditions.get(group) instanceof List<?> nested) {
                rendered = true;
                appendLine(output, propertyIndent, group + ":");
                supported &= renderChildren(output, nested, propertyIndent + 2);
            }
        }
        if (!rendered && isLeaf(conditions)) {
            rendered = true;
            appendLine(output, propertyIndent, "condition:");
            renderLeaf(output, conditions, propertyIndent + 2, false);
        }
        System.out.println("[IIA][AGENT_PREVIEW][ARRAY] anyElement rendered path=" + path);
        return rendered && supported;
    }

    private void renderLeaf(StringBuilder output, Map<String, Object> leaf, int indent, boolean listItem) {
        appendLine(output, indent, (listItem ? "- " : "") + "field: " + leaf.get("field"));
        appendLine(output, indent + (listItem ? 2 : 0), "operator: " + leaf.get("operator"));
        int propertyIndent = indent + (listItem ? 2 : 0);
        if ("LOCAL_TIME_BETWEEN".equals(stringValue(leaf.get("operator")))) {
            System.out.println("[IIA][AGENT_PREVIEW][TEMPORAL] rendered operator=LOCAL_TIME_BETWEEN field="
                    + leaf.get("field"));
        }
        if (leaf.get("value") instanceof Map<?, ?> rawValue) {
            appendLine(output, propertyIndent, "value:");
            Map<String, Object> nestedValue = mapValue(rawValue);
            for (String key : List.of("start", "end", "timezone")) {
                if (nestedValue.containsKey(key)) {
                    appendLine(output, propertyIndent + 2, key + ": " + renderedValue(key, nestedValue.remove(key)));
                }
            }
            nestedValue.forEach((key, value) ->
                    appendLine(output, propertyIndent + 2, key + ": " + renderedValue(key, value)));
        } else if (leaf.containsKey("value")) {
            appendLine(output, propertyIndent, "value: " + leaf.get("value"));
        } else if (leaf.get("values") instanceof List<?> values) {
            appendLine(output, propertyIndent, "values:");
            values.forEach(value -> appendLine(output, propertyIndent + 2, "- " + value));
        }
    }

    private String renderedValue(String key, Object value) {
        String rendered = stringValue(value);
        return ("start".equals(key) || "end".equals(key)) ? "\"" + rendered + "\"" : rendered;
    }

    private void appendEvents(StringBuilder output, List<String> events) {
        if (events == null || events.isEmpty()) {
            appendLine(output, 2, "events: []");
            return;
        }
        appendLine(output, 2, "events:");
        events.forEach(event -> appendLine(output, 4, "- " + event));
    }

    private void appendOptionalScalar(StringBuilder output, String prefix, Object value) {
        String scalar = stringValue(value);
        if (hasText(scalar)) {
            output.append(prefix).append(scalar).append("\n");
        }
    }

    private void appendLine(StringBuilder output, int indent, String line) {
        output.append(" ".repeat(indent)).append(line).append("\n");
    }

    private boolean isLeaf(Map<String, Object> node) {
        return node.containsKey("field")
                && node.containsKey("operator")
                && (node.get("value") != null
                || (node.get("values") instanceof List<?> values && !values.isEmpty())
                || VALUELESS_OPERATORS.contains(stringValue(node.get("operator"))));
    }

    private String enumValue(Object value) {
        return value == null ? null : String.valueOf(value);
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
            if (hasText(result)) {
                return result;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    record BuildResult(
            AgentDslPreview preview,
            boolean partial,
            boolean supportedByRuntime,
            java.util.Set<String> unsupportedCapabilities) {
    }
}
