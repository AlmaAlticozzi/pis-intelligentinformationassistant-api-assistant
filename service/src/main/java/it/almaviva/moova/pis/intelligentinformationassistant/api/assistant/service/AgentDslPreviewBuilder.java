package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDslPreview;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class AgentDslPreviewBuilder {

    private final AgentGenerationCapabilityCatalog capabilityCatalog;

    AgentDslPreviewBuilder(AgentGenerationCapabilityCatalog capabilityCatalog) {
        this.capabilityCatalog = capabilityCatalog;
    }

    BuildResult build(
            AlertAgentGenerationPreviewData data,
            AgentBlueprint blueprint,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary,
            String requestedGenerationMode,
            String recommendedGenerationMode,
            List<String> sources,
            List<String> permissions) {
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
        AgentGenerationCapabilitySnapshot snapshot = new AgentGenerationCapabilitySnapshot(
                sources,
                permissions,
                triggerType,
                evaluationMode,
                inputModel,
                outputType,
                targetTypes,
                conditionSummary.dslOperators(),
                explicitlyStateless,
                requestedGenerationMode,
                recommendedGenerationMode);
        AgentGenerationCapabilityCatalog.RuntimeSupportEvaluation runtimeSupport =
                capabilityCatalog.evaluateRuntimeSupport(snapshot);
        boolean supportedByRuntime = runtimeSupport.supported();

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
                runtimeSupport.unsupportedCapabilities());
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

    private void renderLeaf(StringBuilder output, Map<String, Object> leaf, int indent, boolean listItem) {
        appendLine(output, indent, (listItem ? "- " : "") + "field: " + leaf.get("field"));
        appendLine(output, indent + (listItem ? 2 : 0), "operator: " + leaf.get("operator"));
        appendLine(output, indent + (listItem ? 2 : 0), "value: " + leaf.get("value"));
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
        return node.containsKey("field") && node.containsKey("operator") && node.containsKey("value");
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
