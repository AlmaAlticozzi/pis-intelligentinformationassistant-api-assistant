package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.List;
import java.util.Set;

record AgentBlueprintValidationResult(
        boolean valid,
        boolean runtimeSupported,
        List<String> unsupportedCapabilities,
        List<String> errors,
        List<String> warnings,
        Set<String> detectedDslOperators,
        Set<String> detectedSources,
        Set<String> detectedTargetTypes,
        boolean detectedRequiresState,
        String detectedTriggerType,
        String detectedEvaluationMode,
        String detectedInputModel,
        String detectedOutputModel) {
}
