package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDslPreview;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;

import java.util.List;
import java.util.Map;

record AgentDefinitionDraftCandidate(
        String sourceAlertId,
        Integer sourceAlertVersion,
        String name,
        String description,
        AgentGenerationMode generationMode,
        AgentComplexity complexity,
        AgentBlueprint blueprint,
        AgentDslPreview dslPreview,
        List<String> requiredSources,
        List<String> requiredPermissions,
        Map<String, Object> runtimeContract,
        AgentValidationPlan validationPlan) {
}
