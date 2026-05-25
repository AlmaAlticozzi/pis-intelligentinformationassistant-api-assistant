package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.List;
import java.util.Set;

record AgentGenerationCapabilitySnapshot(
        List<String> sources,
        List<String> permissions,
        String triggerType,
        String evaluationMode,
        String inputModel,
        String outputModel,
        List<String> targetTypes,
        Set<String> dslOperators,
        boolean explicitlyStateless,
        String requestedGenerationMode,
        String recommendedGenerationMode) {
}
