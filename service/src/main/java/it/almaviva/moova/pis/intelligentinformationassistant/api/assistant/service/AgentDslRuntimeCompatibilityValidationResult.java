package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record AgentDslRuntimeCompatibilityValidationResult(
        boolean compatible,
        List<String> errors,
        List<String> warnings,
        String schemaVersion,
        String artifactType,
        String interpreterType,
        String triggerType,
        String executionModel,
        String inputModel,
        String outputModel,
        String evaluationMode,
        Map<String, Object> diagnosticDetails) {

    public Map<String, Object> toDetailsJson() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("compatible", compatible);
        details.put("errors", errors == null ? List.of() : errors);
        details.put("warnings", warnings == null ? List.of() : warnings);
        putIfNotNull(details, "schemaVersion", schemaVersion);
        putIfNotNull(details, "artifactType", artifactType);
        putIfNotNull(details, "interpreterType", interpreterType);
        putIfNotNull(details, "triggerType", triggerType);
        putIfNotNull(details, "executionModel", executionModel);
        putIfNotNull(details, "inputModel", inputModel);
        putIfNotNull(details, "outputModel", outputModel);
        putIfNotNull(details, "evaluationMode", evaluationMode);
        if (diagnosticDetails != null && !diagnosticDetails.isEmpty()) {
            details.put("diagnosticDetails", diagnosticDetails);
        }
        return details;
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }
}
