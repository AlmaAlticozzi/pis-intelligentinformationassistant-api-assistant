package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record AgentCompilationPreconditionValidationResult(
        boolean valid,
        List<String> errors,
        List<String> warnings,
        String interpreterType,
        String triggerType,
        String inputModel,
        String outputModel,
        String evaluationMode,
        String effectiveGenerationMode,
        String executionModel,
        Map<String, Object> diagnosticDetails) {

    public Map<String, Object> toDetailsJson() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("valid", valid);
        details.put("errors", errors == null ? List.of() : errors);
        details.put("warnings", warnings == null ? List.of() : warnings);
        putIfNotNull(details, "interpreterType", interpreterType);
        putIfNotNull(details, "triggerType", triggerType);
        putIfNotNull(details, "inputModel", inputModel);
        putIfNotNull(details, "outputModel", outputModel);
        putIfNotNull(details, "evaluationMode", evaluationMode);
        putIfNotNull(details, "effectiveGenerationMode", effectiveGenerationMode);
        putIfNotNull(details, "executionModel", executionModel);
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
