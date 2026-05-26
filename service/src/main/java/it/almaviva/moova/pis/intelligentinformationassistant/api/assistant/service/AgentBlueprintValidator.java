package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AgentBlueprintValidator {

    private final AgentGenerationCapabilityCatalog capabilityCatalog;

    @Inject
    public AgentBlueprintValidator(AgentGenerationCapabilityCatalog capabilityCatalog) {
        this.capabilityCatalog = capabilityCatalog;
    }

    AgentBlueprintValidationResult validate(
            String alertId,
            AlertAgentGenerationPreviewData data,
            AgentBlueprint blueprint,
            AgentPreviewConditionExtractor.ConditionSummary conditionSummary,
            List<String> sources,
            List<String> permissions,
            String requestedGenerationMode,
            String recommendedGenerationMode) {
        System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Validating blueprint alertId=" + alertId);
        Set<String> detectedSources = new LinkedHashSet<>(sources);
        Set<String> detectedTargets = new LinkedHashSet<>();
        if (blueprint.getTargetTypes() != null) {
            blueprint.getTargetTypes().forEach(target -> detectedTargets.add(String.valueOf(target)));
        }
        String triggerType = blueprint.getTriggerType() == null ? null : blueprint.getTriggerType().toString();
        String evaluationMode = firstString(blueprint.get("evaluationMode"), data.technicalSpecification().get("evaluationMode"));
        String inputModel = firstString(data.inputModel(), data.technicalSpecification().get("inputModel"));
        Map<String, Object> output = mapValue(blueprint.get("output"));
        String outputModel = firstString(output.get("type"), data.outputModel(), data.technicalSpecification().get("outputModel"));
        boolean requiresState = Boolean.TRUE.equals(blueprint.getStateRequirements().get("requiresState"));
        boolean explicitlyStateless = Boolean.FALSE.equals(blueprint.getStateRequirements().get("requiresState"));

        System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Detected capabilities alertId=" + alertId
                + ", sources=" + detectedSources
                + ", trigger=" + triggerType
                + ", evaluationMode=" + evaluationMode
                + ", operators=" + conditionSummary.dslOperators());

        List<String> errors = new ArrayList<>();
        if (!hasText(firstString(blueprint.get("schemaVersion"), "iia.agent.blueprint/v1"))) {
            errors.add("Blueprint schemaVersion is missing.");
        }
        if (!capabilityCatalog.isSupportedTriggerType(triggerType)) {
            errors.add("Unsupported triggerType: " + triggerType);
        }
        if (detectedSources.isEmpty() || !detectedSources.contains("SERVICE_DATA")) {
            errors.add("Blueprint must require SERVICE_DATA.");
        }
        detectedSources.stream().filter(source -> !capabilityCatalog.isSupportedSource(source))
                .forEach(source -> errors.add("Unsupported source: " + source));
        if (detectedTargets.isEmpty()) {
            errors.add("Blueprint targetTypes are missing.");
        }
        detectedTargets.stream().filter(target -> !capabilityCatalog.isSupportedTargetType(target))
                .forEach(target -> errors.add("Unsupported targetType: " + target));
        if (!capabilityCatalog.isSupportedEvaluationMode(evaluationMode)) {
            errors.add("Unsupported evaluationMode: " + evaluationMode);
        }
        if (!explicitlyStateless) {
            errors.add("Blueprint requires state or does not explicitly declare requiresState=false.");
        }
        if (hasText(inputModel) && !capabilityCatalog.isSupportedInputModel(inputModel)) {
            errors.add("Unsupported inputModel: " + inputModel);
        }
        if (!capabilityCatalog.isSupportedOutputModel(outputModel)) {
            errors.add("Unsupported output type: " + outputModel);
        }
        if (!hasText(firstString(output.get("type")))) {
            errors.add("Blueprint output.type is missing.");
        }
        if (!"SERVICE_DATA_FIELD_MATCH".equals(conditionSummary.conditionType())) {
            errors.add("Unsupported or missing conditionType: " + conditionSummary.conditionType());
        }
        if (conditionSummary.condition().isEmpty() || conditionSummary.partial()) {
            if (conditionSummary.renderIssues().isEmpty()) {
                errors.add("Condition tree cannot be rendered completely by the deterministic DSL renderer.");
            } else {
                conditionSummary.renderIssues().forEach(issue -> {
                    String detail = "Condition tree cannot be rendered completely by the deterministic DSL renderer at "
                            + issue.path() + ": " + issue.reason() + "; keys=" + issue.keys() + ".";
                    errors.add(detail);
                    System.out.println("[IIA][AGENT_DSL_RENDERER] Unsupported condition node alertId=" + alertId
                            + ", path=" + issue.path()
                            + ", reason=" + issue.reason()
                            + ", operator=" + issue.operator()
                            + ", keys=" + issue.keys()
                            + ", node=" + issue.snippet());
                    System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Condition renderability failed alertId="
                            + alertId + ", path=" + issue.path() + ", reason=" + issue.reason());
                });
            }
        }
        conditionSummary.dslOperators().stream()
                .filter(operator -> !capabilityCatalog.isSupportedDslOperator(operator))
                .forEach(operator -> errors.add("Unsupported DSL operator: " + operator));

        Set<String> forbidden = new LinkedHashSet<>();
        findForbidden(blueprint, forbidden);
        findForbidden(data.technicalSpecification(), forbidden);
        sources.stream()
                .filter(capabilityCatalog::isForbiddenCapability)
                .forEach(forbidden::add);
        forbidden.forEach(capability -> errors.add("Forbidden capability: " + capability));

        AgentGenerationCapabilitySnapshot snapshot = new AgentGenerationCapabilitySnapshot(
                List.copyOf(detectedSources),
                permissions,
                triggerType,
                evaluationMode,
                inputModel,
                outputModel,
                List.copyOf(detectedTargets),
                conditionSummary.dslOperators(),
                explicitlyStateless,
                requestedGenerationMode,
                recommendedGenerationMode);
        AgentGenerationCapabilityCatalog.RuntimeSupportEvaluation runtime =
                capabilityCatalog.evaluateRuntimeSupport(snapshot);
        LinkedHashSet<String> unsupported = new LinkedHashSet<>(runtime.unsupportedCapabilities());
        unsupported.addAll(forbidden);
        boolean valid = errors.isEmpty();
        boolean runtimeSupported = valid && runtime.supported() && unsupported.isEmpty();
        if (valid) {
            System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Blueprint valid alertId=" + alertId
                    + ", runtimeSupported=" + runtimeSupported);
        } else {
            System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Blueprint invalid alertId=" + alertId
                    + ", errors=" + errors);
        }
        if (!unsupported.isEmpty()) {
            System.out.println("[IIA][AGENT_BLUEPRINT_VALIDATOR] Unsupported capabilities alertId=" + alertId
                    + ", unsupported=" + unsupported);
        }
        return new AgentBlueprintValidationResult(
                valid,
                runtimeSupported,
                List.copyOf(unsupported),
                List.copyOf(errors),
                List.of(),
                Set.copyOf(conditionSummary.dslOperators()),
                Set.copyOf(detectedSources),
                Set.copyOf(detectedTargets),
                requiresState,
                triggerType,
                evaluationMode,
                inputModel,
                outputModel);
    }

    private void findForbidden(Object value, Set<String> forbidden) {
        if (value instanceof Map<?, ?> map) {
            map.values().forEach(nested -> findForbidden(nested, forbidden));
        } else if (value instanceof List<?> list) {
            list.forEach(nested -> findForbidden(nested, forbidden));
        } else if (value != null && capabilityCatalog.isForbiddenCapability(String.valueOf(value))) {
            forbidden.add(String.valueOf(value));
        }
    }

    private Map<String, Object> mapValue(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        map.forEach((key, nestedValue) -> result.put(String.valueOf(key), nestedValue));
        return result;
    }

    private String firstString(Object... values) {
        for (Object value : values) {
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
