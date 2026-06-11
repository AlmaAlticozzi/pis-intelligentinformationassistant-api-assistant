package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateDiagnostics;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateLoader;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.AgentGenerationCapabilityCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentGenerationPromptBuilderTest {

    private static final String SYSTEM_TEMPLATE_PATH = "iia/prompts/agent-preview/system.md";
    private static final String USER_TEMPLATE_PATH = "iia/prompts/agent-preview/user-template.md";

    @Test
    void agentPreviewSystemTemplateContainsStaticPromptTextDirectly() {
        String template = new PromptTemplateLoader().load(SYSTEM_TEMPLATE_PATH);

        assertThat(template)
                .contains("## Mission")
                .contains("## Fixed contract")
                .contains("## Runtime contract")
                .contains("## Event and Scheduled differences")
                .contains("## Blueprint rules")
                .contains("## DSL preview rules")
                .contains("## Rejection and fallback policy")
                .contains("## JSON output contract")
                .contains("Agent Generation Preview")
                .contains("technicalSpecification is the source of truth")
                .contains("no Agent Definition")
                .contains("no Agent Run")
                .contains("no Suggestion")
                .contains("AgentBlueprintValidator")
                .contains("SERVICE_DATA_JOURNEY_AGGREGATE")
                .contains("SERVICE_DATA_SCHEDULED_FIELD_MATCH")
                .contains("snapshotEvaluation")
                .contains("serviceDataQuery")
                .contains("outputPolicy")
                .contains("{{AGENT_GENERATION_CAPABILITY_CATALOG}}")
                .doesNotContain("{{MISSION}}")
                .doesNotContain("{{SAFETY_RULES}}")
                .doesNotContain("{{OUTPUT_CONTRACT}}");
    }

    @Test
    void agentPreviewUserTemplateContainsOnlyRuntimePlaceholders() {
        String template = new PromptTemplateLoader().load(USER_TEMPLATE_PATH);

        assertThat(template)
                .contains("{{ALERT_JSON}}")
                .contains("{{TECHNICAL_SPECIFICATION_JSON}}")
                .contains("{{CURRENT_AGENT_BLUEPRINT_PREVIEW_JSON}}")
                .contains("{{INTERPRETER_TYPE}}")
                .contains("{{TRIGGER_TYPE}}")
                .contains("{{INPUT_MODEL}}")
                .contains("{{OUTPUT_MODEL}}")
                .contains("{{EVALUATION_MODE}}")
                .contains("{{TECHNICAL_SPECIFICATION_EDITED}}")
                .contains("{{GENERATION_MODE}}")
                .contains("{{OUTPUT_INSTRUCTIONS}}")
                .doesNotContain("## Mission")
                .doesNotContain("AgentBlueprintValidator");
        assertThat(PromptTemplateDiagnostics.extractPlaceholders(template))
                .isEqualTo(Set.of(
                        "ALERT_JSON",
                        "TECHNICAL_SPECIFICATION_JSON",
                        "CURRENT_AGENT_BLUEPRINT_PREVIEW_JSON",
                        "INTERPRETER_TYPE",
                        "TRIGGER_TYPE",
                        "INPUT_MODEL",
                        "OUTPUT_MODEL",
                        "EVALUATION_MODE",
                        "TECHNICAL_SPECIFICATION_EDITED",
                        "GENERATION_MODE",
                        "OUTPUT_INSTRUCTIONS"));
    }

    @Test
    void agentPreviewPromptRendersTemplatesWithCatalogRuntimePayloadAndNoUnresolvedPlaceholders() {
        LlmRequest request = builder().build(previewData(), request());
        String fullPrompt = request.systemPrompt() + "\n" + request.userPrompt();

        System.out.println("[IIA][AGENT_PREVIEW][PROMPT_TEMPLATE][TEST] systemLength="
                + request.systemPrompt().length()
                + " userLength=" + request.userPrompt().length()
                + " total=" + fullPrompt.length());

        assertThat(request.useCase()).isEqualTo(AiUseCase.AGENT_BLUEPRINT_GENERATE);
        assertThat(fullPrompt)
                .isNotBlank()
                .contains("Agent Generation Preview")
                .contains("technicalSpecification is the source of truth")
                .contains("AgentBlueprintValidator")
                .contains("SERVICE_DATA")
                .contains("READ_SERVICE_DATA")
                .contains("STATELESS_EVENT_MATCH")
                .contains("ServiceDataV2")
                .contains("Create a suggestion when a journey is cancelled.")
                .contains("\"alertId\":\"ALRT1\"")
                .contains("\"conditionType\":\"SERVICE_DATA_FIELD_MATCH\"")
                .doesNotContainPattern("\\{\\{[A-Z0-9_]+}}");
    }

    @Test
    void agentPreviewPromptSupportsScheduledRuntimeWithoutForcingEventSemantics() {
        LlmRequest request = builder().build(scheduledPreviewData(), request());
        String fullPrompt = request.systemPrompt() + "\n" + request.userPrompt();

        assertThat(fullPrompt)
                .contains("For SCHEDULED_INTERPRETER / SCHEDULED_SNAPSHOT_MATCH")
                .contains("Do not use fixed Event values for a Scheduled preview")
                .contains("Do not force Scheduled snapshotEvaluation into Event condition")
                .contains("SERVICE_DATA_JOURNEY_AGGREGATE")
                .contains("SERVICE_DATA_SCHEDULED_FIELD_MATCH")
                .contains("\"interpreterType\":\"SCHEDULED_INTERPRETER\"")
                .contains("\"triggerType\":\"SCHEDULE\"")
                .contains("\"evaluationMode\":\"SCHEDULED_SNAPSHOT_MATCH\"")
                .contains("\"inputModel\":\"ServiceDataStopPointJourneysV2\"")
                .contains("\"serviceDataQuery\"")
                .contains("\"snapshotEvaluation\"")
                .contains("\"outputPolicy\"")
                .doesNotContainPattern("\\{\\{[A-Z0-9_]+}}");
    }

    @Test
    void agentPreviewTemplateDiagnosticsHaveNoUnresolvedPlaceholders() {
        AgentGenerationCapabilityCatalog catalog = new AgentGenerationCapabilityCatalog();
        PromptTemplateLoader loader = new PromptTemplateLoader();
        PromptTemplateDiagnostics system = loader.renderWithDiagnostics(
                SYSTEM_TEMPLATE_PATH,
                Map.of("AGENT_GENERATION_CAPABILITY_CATALOG", catalog.supportedSources().toString()));
        PromptTemplateDiagnostics user = loader.renderWithDiagnostics(
                USER_TEMPLATE_PATH,
                Map.ofEntries(
                        Map.entry("ALERT_JSON", "{}"),
                        Map.entry("TECHNICAL_SPECIFICATION_JSON", "{}"),
                        Map.entry("CURRENT_AGENT_BLUEPRINT_PREVIEW_JSON", "{}"),
                        Map.entry("INTERPRETER_TYPE", "EVENT_INTERPRETER"),
                        Map.entry("TRIGGER_TYPE", "EVENT"),
                        Map.entry("INPUT_MODEL", "ServiceDataV2"),
                        Map.entry("OUTPUT_MODEL", "AgentOutput.CANDIDATE_SUGGESTION"),
                        Map.entry("EVALUATION_MODE", "STATELESS_EVENT_MATCH"),
                        Map.entry("TECHNICAL_SPECIFICATION_EDITED", "false"),
                        Map.entry("GENERATION_MODE", "DSL"),
                        Map.entry("OUTPUT_INSTRUCTIONS", "Return JSON only.")));

        assertThat(system.unresolvedPlaceholdersAfterRender()).isEmpty();
        assertThat(user.unresolvedPlaceholdersAfterRender()).isEmpty();
        assertThat(system.renderedHash()).isNotBlank();
        assertThat(user.renderedHash()).isNotBlank();
    }

    private AgentGenerationPromptBuilder builder() {
        AiConfiguration configuration = mock(AiConfiguration.class);
        AiConfiguration.AgentBlueprintGenerate generation = mock(AiConfiguration.AgentBlueprintGenerate.class);
        AiConfiguration.PromptTemplate promptTemplate = mock(AiConfiguration.PromptTemplate.class);
        when(configuration.agentBlueprintGenerate()).thenReturn(generation);
        when(generation.model()).thenReturn("gpt-4.1-mini");
        when(generation.temperature()).thenReturn(0.1);
        when(generation.maxOutputTokens()).thenReturn(2500);
        when(configuration.promptTemplate()).thenReturn(promptTemplate);
        when(promptTemplate.warnSystemLength()).thenReturn(20000);
        when(promptTemplate.warnUserLength()).thenReturn(10000);
        when(promptTemplate.warnTotalLength()).thenReturn(25000);
        return new AgentGenerationPromptBuilder(configuration, new AgentGenerationCapabilityCatalog());
    }

    private AgentGenerationPreviewRequest request() {
        return new AgentGenerationPreviewRequest()
                .preferredGenerationMode(AgentGenerationMode.DSL)
                .includeDslPreview(true)
                .includeValidationPlan(true);
    }

    private AlertAgentGenerationPreviewData previewData() {
        Map<String, Object> condition = Map.of(
                "type", "SERVICE_DATA_FIELD_MATCH",
                "field", "payload.ongroundServiceEvent.eventsType",
                "operator", "CONTAINS",
                "value", "ARRIVAL_CANCELLATION");
        Map<String, Object> technicalSpecification = Map.of(
                "source", "SERVICE_DATA",
                "interpreterType", "EVENT_INTERPRETER",
                "triggerType", "EVENT",
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "inputModel", "ServiceDataV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "condition", condition);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ServiceDataCancellationAgent",
                "description", "Detects cancelled journeys.",
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "evaluationMode", "STATELESS_EVENT_MATCH",
                "parameters", Map.of(
                        "conditionType", "SERVICE_DATA_FIELD_MATCH",
                        "condition", condition),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return new AlertAgentGenerationPreviewData(
                "ALRT1",
                "Cancelled journeys",
                "VERIFIED",
                "VERIFIED",
                false,
                null,
                1,
                "Create a suggestion when a journey is cancelled.",
                "Verified.",
                null,
                null,
                "EVENT_INTERPRETER",
                "ServiceDataV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification,
                blueprint,
                List.of("SERVICE_DATA_FIELD_MATCH"),
                List.of(),
                List.of(SuggestionTargetType.SERVICE_DATA_JOURNEY));
    }

    private AlertAgentGenerationPreviewData scheduledPreviewData() {
        Map<String, Object> scheduledCondition = Map.of(
                "type", "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                "anyElement", Map.of(
                        "path", "stopPointsJourneyDetails[]",
                        "conditions", Map.of(
                                "field", "callStart.stopPoint.id",
                                "operator", "EQUALS",
                                "value", "TNPNTS00000000000122")));
        Map<String, Object> serviceDataQuery = Map.of(
                "operation", "POST /v2/stoppointjourneys",
                "stopPoints", List.of("TNPNTS00000000000009"),
                "timeWindow", Map.of("lookaheadMinutes", 180));
        Map<String, Object> snapshotEvaluation = Map.of(
                "mode", "REPORT_COUNT",
                "journeyPath", "stopPointsJourneyDetails[]",
                "condition", scheduledCondition);
        Map<String, Object> outputPolicy = Map.of(
                "emit", "EVERY_RUN",
                "includeCount", true,
                "includeMatchingJourneys", true);
        Map<String, Object> technicalSpecification = Map.of(
                "source", "SERVICE_DATA",
                "interpreterType", "SCHEDULED_INTERPRETER",
                "triggerType", "SCHEDULE",
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "inputModel", "ServiceDataStopPointJourneysV2",
                "outputModel", "AgentOutput.CANDIDATE_SUGGESTION",
                "targetTypes", List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                "serviceDataQuery", serviceDataQuery,
                "snapshotEvaluation", snapshotEvaluation,
                "outputPolicy", outputPolicy);
        Map<String, Object> blueprint = Map.of(
                "schemaVersion", "iia.agent.blueprint/v1",
                "agentName", "ScheduledServiceDataSnapshotAlertAgent",
                "description", "Reports scheduled journeys from a ServiceData snapshot.",
                "triggerType", "SCHEDULE",
                "requiredSources", List.of("SERVICE_DATA"),
                "targetTypes", List.of("SERVICE_DATA_JOURNEY_AGGREGATE"),
                "evaluationMode", "SCHEDULED_SNAPSHOT_MATCH",
                "parameters", Map.of(
                        "serviceDataQuery", serviceDataQuery,
                        "snapshotEvaluation", snapshotEvaluation,
                        "outputPolicy", outputPolicy),
                "stateRequirements", Map.of("requiresState", false),
                "output", Map.of("type", "CANDIDATE_SUGGESTION"));
        return new AlertAgentGenerationPreviewData(
                "ALRT_SCHEDULED",
                "Scheduled count",
                "VERIFIED",
                "VERIFIED",
                false,
                null,
                1,
                "Ogni 10 minuti dimmi quante corse a Garibaldi FS hanno origine Monza nelle prossime 3 ore",
                "Verified.",
                null,
                null,
                "SCHEDULED_INTERPRETER",
                "ServiceDataStopPointJourneysV2",
                "AgentOutput.CANDIDATE_SUGGESTION",
                technicalSpecification,
                blueprint,
                List.of("SERVICE_DATA_SCHEDULED_FIELD_MATCH"),
                List.of(),
                List.of());
    }
}
