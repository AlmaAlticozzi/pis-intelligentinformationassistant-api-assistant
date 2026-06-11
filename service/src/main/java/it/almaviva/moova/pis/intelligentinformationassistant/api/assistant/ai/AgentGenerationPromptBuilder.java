package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateDiagnostics;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateLoader;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentGenerationCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class AgentGenerationPromptBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SYSTEM_TEMPLATE_PATH = "iia/prompts/agent-preview/system.md";
    private static final String USER_TEMPLATE_PATH = "iia/prompts/agent-preview/user-template.md";
    private static final String DEFAULT_PROMPT_TEMPLATE_VERSION = "agent-preview-file-v1";

    private final AiConfiguration aiConfiguration;
    private final AgentGenerationCapabilityCatalog capabilityCatalog;
    private final PromptTemplateLoader promptTemplateLoader;
    private final String promptTemplateVersion;

    @Inject
    public AgentGenerationPromptBuilder(
            AiConfiguration aiConfiguration,
            AgentGenerationCapabilityCatalog capabilityCatalog,
            PromptTemplateLoader promptTemplateLoader,
            @ConfigProperty(
                    name = "iia.agent-generation-preview.prompt-template-version",
                    defaultValue = DEFAULT_PROMPT_TEMPLATE_VERSION)
            String promptTemplateVersion) {
        this.aiConfiguration = aiConfiguration;
        this.capabilityCatalog = capabilityCatalog;
        this.promptTemplateLoader = promptTemplateLoader;
        this.promptTemplateVersion = promptTemplateVersion;
    }

    public AgentGenerationPromptBuilder(
            AiConfiguration aiConfiguration,
            AgentGenerationCapabilityCatalog capabilityCatalog) {
        this(aiConfiguration, capabilityCatalog, new PromptTemplateLoader(), DEFAULT_PROMPT_TEMPLATE_VERSION);
    }

    public LlmRequest build(AlertAgentGenerationPreviewData alert, AgentGenerationPreviewRequest request) {
        AiConfiguration.AgentBlueprintGenerate configuration = aiConfiguration.agentBlueprintGenerate();
        System.out.println("[IIA][AGENT_PREVIEW_LLM] Building prompt alertId=" + alert.alertId()
                + ", model=" + configuration.model());
        PromptTemplateDiagnostics systemDiagnostics = systemPrompt();
        PromptTemplateDiagnostics userDiagnostics = userPrompt(alert, request);
        String systemPrompt = systemDiagnostics.renderedText();
        String userPrompt = userDiagnostics.renderedText();
        int totalLength = systemPrompt.length() + userPrompt.length();
        String totalPromptHash = PromptTemplateDiagnostics.shortSha256(systemPrompt + "\n" + userPrompt);
        logTemplateDiagnostics(systemDiagnostics, userDiagnostics, totalPromptHash, totalLength);
        warnPromptLength("system", systemPrompt.length(), promptTemplateConfiguration().warnSystemLength());
        warnPromptLength("user", userPrompt.length(), promptTemplateConfiguration().warnUserLength());
        warnPromptLength("total", totalLength, promptTemplateConfiguration().warnTotalLength());
        return new LlmRequest(
                AiUseCase.AGENT_BLUEPRINT_GENERATE,
                systemPrompt,
                userPrompt,
                configuration.model(),
                configuration.temperature(),
                configuration.maxOutputTokens(),
                alert.alertId());
    }

    private PromptTemplateDiagnostics systemPrompt() {
        return promptTemplateLoader.renderWithDiagnostics(
                SYSTEM_TEMPLATE_PATH,
                Map.of("AGENT_GENERATION_CAPABILITY_CATALOG", capabilityCatalogPrompt()));
    }

    private PromptTemplateDiagnostics userPrompt(AlertAgentGenerationPreviewData alert, AgentGenerationPreviewRequest request) {
        return promptTemplateLoader.renderWithDiagnostics(USER_TEMPLATE_PATH, userVariables(alert, request));
    }

    private Map<String, String> userVariables(AlertAgentGenerationPreviewData alert, AgentGenerationPreviewRequest request) {
        Map<String, Object> technicalSpecification = alert.technicalSpecification() == null
                ? Map.of()
                : alert.technicalSpecification();
        return Map.ofEntries(
                Map.entry("ALERT_JSON", json(alertPayload(alert, request))),
                Map.entry("TECHNICAL_SPECIFICATION_JSON", json(technicalSpecification)),
                Map.entry("CURRENT_AGENT_BLUEPRINT_PREVIEW_JSON", json(alert.agentBlueprintPreview())),
                Map.entry("INTERPRETER_TYPE", stringValue(alert.interpreterType())),
                Map.entry("TRIGGER_TYPE", stringValue(firstString(technicalSpecification.get("triggerType"), "EVENT"))),
                Map.entry("INPUT_MODEL", stringValue(firstString(alert.inputModel(), technicalSpecification.get("inputModel")))),
                Map.entry("OUTPUT_MODEL", stringValue(firstString(alert.outputModel(), technicalSpecification.get("outputModel")))),
                Map.entry("EVALUATION_MODE", stringValue(firstString(technicalSpecification.get("evaluationMode")))),
                Map.entry("TECHNICAL_SPECIFICATION_EDITED", "false"),
                Map.entry("GENERATION_MODE", stringValue(request == null ? null : request.getPreferredGenerationMode())),
                Map.entry("OUTPUT_INSTRUCTIONS", outputInstructions()));
    }

    private Map<String, Object> alertPayload(AlertAgentGenerationPreviewData alert, AgentGenerationPreviewRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("alertId", alert.alertId());
        payload.put("name", alert.name());
        payload.put("prompt", alert.prompt());
        payload.put("status", alert.status());
        payload.put("verificationStatus", alert.verificationStatus());
        payload.put("version", alert.version());
        payload.put("verificationSummary", alert.verificationSummary());
        payload.put("interpreterType", alert.interpreterType());
        payload.put("inputModel", alert.inputModel());
        payload.put("outputModel", alert.outputModel());
        payload.put("interpretedEventNames", alert.interpretedEventNames());
        payload.put("targetTypes", alert.targetTypes());
        payload.put("preferredGenerationMode", request == null ? null : request.getPreferredGenerationMode());
        payload.put("includeDslPreview", request == null ? null : request.getIncludeDslPreview());
        payload.put("includeValidationPlan", request == null ? null : request.getIncludeValidationPlan());
        return payload;
    }

    private String capabilityCatalogPrompt() {
        return """
                Allowed Agent Generation MVP capabilities:
                - sources: %s
                - permissions: %s
                - generationModes: %s
                - previewOnlyGenerationModes: %s
                - dslOperators: %s
                - dslLogicalNodes: %s
                - dslFunctions: %s
                - temporalFields: %s
                - arrayPaths: %s
                - nextCallsRelativeFields: %s
                - forbiddenCapabilities: %s
                - triggerTypes: [EVENT, SCHEDULE, SCHEDULED]
                - evaluationModes: [STATELESS_EVENT_MATCH, SCHEDULED_SNAPSHOT_MATCH]
                - inputModels: [ServiceDataV2, ServiceDataStopPointJourneysV2]
                - outputTypes: [CANDIDATE_SUGGESTION, AgentOutput.CANDIDATE_SUGGESTION]
                - targetTypes: [SERVICE_DATA_JOURNEY, SERVICE_DATA_JOURNEY_AGGREGATE]
                - requiresState: false
                """.formatted(
                capabilityCatalog.supportedSources(),
                capabilityCatalog.supportedPermissions(),
                capabilityCatalog.supportedGenerationModes(),
                capabilityCatalog.previewOnlyGenerationModes(),
                capabilityCatalog.supportedDslOperators(),
                capabilityCatalog.supportedDslLogicalNodes(),
                capabilityCatalog.supportedDslFunctions(),
                capabilityCatalog.supportedTemporalFields(),
                capabilityCatalog.supportedArrayPaths(),
                capabilityCatalog.supportedArrayRelativeFields(
                        "payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]"),
                capabilityCatalog.forbiddenCapabilities());
    }

    private String outputInstructions() {
        return "Return one raw JSON object only. Preserve the verified condition tree and use only the declared catalog.";
    }

    private void logTemplateDiagnostics(
            PromptTemplateDiagnostics systemDiagnostics,
            PromptTemplateDiagnostics userDiagnostics,
            String totalPromptHash,
            int totalLength) {
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] systemPath=" + SYSTEM_TEMPLATE_PATH
                + " userPath=" + USER_TEMPLATE_PATH);
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] promptVersion=" + promptTemplateVersion);
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] staticBlocksMovedToFile=true");
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] declaredSystemPlaceholders="
                + systemDiagnostics.declaredPlaceholdersLogValue());
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] declaredUserPlaceholders="
                + userDiagnostics.declaredPlaceholdersLogValue());
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] unresolvedSystemPlaceholdersAfterRender="
                + systemDiagnostics.unresolvedPlaceholdersAfterRenderLogValue());
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] unresolvedUserPlaceholdersAfterRender="
                + userDiagnostics.unresolvedPlaceholdersAfterRenderLogValue());
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] unusedSystemVariables="
                + systemDiagnostics.unusedVariablesLogValue());
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] unusedUserVariables="
                + userDiagnostics.unusedVariablesLogValue());
        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE] promptVersion=" + promptTemplateVersion
                + " rawSystemTemplateHash=" + systemDiagnostics.templateHash()
                + " renderedSystemHash=" + systemDiagnostics.renderedHash()
                + " renderedUserHash=" + userDiagnostics.renderedHash()
                + " totalPromptHash=" + totalPromptHash
                + " systemLength=" + systemDiagnostics.renderedLength()
                + " userLength=" + userDiagnostics.renderedLength()
                + " total=" + totalLength);
    }

    private void warnPromptLength(String label, int actual, int threshold) {
        if (threshold > 0 && actual > threshold) {
            System.out.println("[IIA][PROMPT_TEMPLATE][WARN] " + label
                    + " prompt length exceeds threshold: " + actual + " > " + threshold);
        }
    }

    private AiConfiguration.PromptTemplate promptTemplateConfiguration() {
        if (aiConfiguration != null && aiConfiguration.promptTemplate() != null) {
            return aiConfiguration.promptTemplate();
        }
        return new DefaultPromptTemplateConfiguration();
    }

    private String json(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private String firstString(Object... values) {
        for (Object value : values) {
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private record DefaultPromptTemplateConfiguration() implements AiConfiguration.PromptTemplate {
        @Override
        public boolean failOnUnresolvedPlaceholders() {
            return false;
        }

        @Override
        public int warnTotalLength() {
            return 25000;
        }

        @Override
        public int warnSystemLength() {
            return 20000;
        }

        @Override
        public int warnUserLength() {
            return 10000;
        }
    }
}
