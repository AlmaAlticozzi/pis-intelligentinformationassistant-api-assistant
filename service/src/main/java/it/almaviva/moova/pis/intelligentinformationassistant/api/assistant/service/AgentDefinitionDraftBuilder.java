package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentComplexity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDslPreview;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentValidationPlan;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
class AgentDefinitionDraftBuilder {

    private final AgentGenerationCapabilityCatalog capabilityCatalog;

    AgentDefinitionDraftBuilder() {
        this(new AgentGenerationCapabilityCatalog());
    }

    @Inject
    AgentDefinitionDraftBuilder(AgentGenerationCapabilityCatalog capabilityCatalog) {
        this.capabilityCatalog = capabilityCatalog;
    }

    AgentDefinitionDraftCandidate prepare(
            AlertAgentGenerationPreviewData data,
            AgentBlueprint blueprint,
            AgentGenerationMode generationMode,
            AgentComplexity complexity,
            List<String> requiredSources,
            List<String> requiredPermissions,
            AgentBlueprintValidationResult validationResult,
            AgentDslPreview dslPreview,
            AgentValidationPlan validationPlan,
            PreviewSource previewSource) {
        System.out.println("[IIA][AGENT_DEFINITION_PREP] Preparing Agent Definition metadata alertId="
                + data.alertId() + ", alertVersion=" + data.version());
        Map<String, Object> parameters = new LinkedHashMap<>(blueprint.getParameters());
        Map<String, Object> generationContext = generationContext(data, generationMode, previewSource);
        Map<String, Object> runtimeContract = runtimeContract(
                requiredSources,
                requiredPermissions,
                validationResult);
        Map<String, Object> generationReadiness = generationReadiness(
                validationResult,
                dslPreview);
        parameters.put("generationContext", generationContext);
        parameters.put("runtimeContract", runtimeContract);
        parameters.put("generationReadiness", generationReadiness);
        blueprint.setParameters(parameters);

        System.out.println("[IIA][AGENT_DEFINITION_PREP] Runtime contract prepared alertId=" + data.alertId()
                + ", source=" + firstOrNull(requiredSources)
                + ", generationMode=" + generationMode);
        System.out.println("[IIA][AGENT_DEFINITION_PREP] Generation readiness alertId=" + data.alertId()
                + ", readyForAgentDefinition=" + generationReadiness.get("readyForAgentDefinition")
                + ", nextStep=CREATE_AGENT_DEFINITION");

        AgentDefinitionDraftCandidate candidate = new AgentDefinitionDraftCandidate(
                data.alertId(),
                data.version(),
                blueprint.getAgentName(),
                blueprint.getDescription(),
                generationMode,
                complexity,
                blueprint,
                dslPreview,
                requiredSources,
                requiredPermissions,
                runtimeContract,
                validationPlan);
        System.out.println("[IIA][AGENT_DEFINITION_PREP] Agent Definition draft candidate prepared alertId="
                + data.alertId() + ", agentName=" + blueprint.getAgentName());
        return candidate;
    }

    private Map<String, Object> generationContext(
            AlertAgentGenerationPreviewData data,
            AgentGenerationMode generationMode,
            PreviewSource previewSource) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sourceAlertId", data.alertId());
        result.put("sourceAlertVersion", data.version());
        result.put("sourceAlertStatus", data.status());
        result.put("verificationStatus", data.verificationStatus());
        result.put("generationMode", generationMode.toString());
        result.put("previewSource", previewSource.toString());
        return result;
    }

    private Map<String, Object> runtimeContract(
            List<String> requiredSources,
            List<String> requiredPermissions,
            AgentBlueprintValidationResult validationResult) {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean scheduled = "SCHEDULE".equals(validationResult.detectedTriggerType())
                || "SCHEDULED".equals(validationResult.detectedTriggerType())
                || "SCHEDULED_SNAPSHOT_MATCH".equals(validationResult.detectedEvaluationMode());
        result.put("schemaVersion", "iia.agent.runtime-contract/v1");
        result.put("executionModel", scheduled ? "SCHEDULED_POLLING" : "EVENT_DRIVEN");
        result.put("triggerType", validationResult.detectedTriggerType());
        result.put("source", firstOrNull(requiredSources));
        result.put("inputModel", validationResult.detectedInputModel());
        result.put("outputModel", canonicalOutputModel(validationResult.detectedOutputModel()));
        result.put("evaluationMode", validationResult.detectedEvaluationMode());
        result.put("requiresState", validationResult.detectedRequiresState());
        result.put("requiresScheduler", scheduled);
        result.put("requiresExternalTools", false);
        result.put("requiresNetworkAccess", false);
        result.put("requiresFilesystemAccess", false);
        result.put("requiredPermissions", requiredPermissions);
        result.put("allowedSources", requiredSources);
        result.put("allowedDslOperators", List.copyOf(validationResult.detectedDslOperators()));
        result.put("forbiddenCapabilities", List.copyOf(capabilityCatalog.forbiddenCapabilities()));
        return result;
    }

    private Map<String, Object> generationReadiness(
            AgentBlueprintValidationResult validationResult,
            AgentDslPreview dslPreview) {
        boolean ready = validationResult.valid()
                && validationResult.runtimeSupported()
                && dslPreview != null
                && Boolean.TRUE.equals(dslPreview.getSupportedByRuntime())
                && validationResult.unsupportedCapabilities().isEmpty();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("readyForAgentDefinition", ready);
        result.put("blockingReasons", ready ? List.of() : validationResult.errors());
        result.put("recommendedNextStep", ready ? "CREATE_AGENT_DEFINITION" : "REVIEW_PREVIEW");
        result.put("requiresHumanReview", true);
        result.put("requiresCompilation", true);
        result.put("requiresRuntimeActivation", true);
        result.put("nextSteps", List.of(
                "REVIEW_PREVIEW",
                "CREATE_AGENT_DEFINITION",
                "COMPILE_AGENT_ARTIFACT",
                "ACTIVATE_AGENT_DEFINITION"));
        return result;
    }

    private String firstOrNull(List<String> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    private String canonicalOutputModel(String outputModel) {
        return "CANDIDATE_SUGGESTION".equals(outputModel)
                ? "AgentOutput.CANDIDATE_SUGGESTION"
                : outputModel;
    }

    enum PreviewSource {
        DETERMINISTIC,
        LLM_VALIDATED,
        LLM_FALLBACK
    }
}
