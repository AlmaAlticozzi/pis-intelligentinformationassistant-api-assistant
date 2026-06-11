package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateDiagnostics;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateLoader;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class AlertVerificationPromptBuilder {

    private static final String SYSTEM_PROMPT_TEMPLATE_PATH = "iia/prompts/alert/event/system.md";
    private static final String USER_PROMPT_TEMPLATE_PATH = "iia/prompts/alert/event/user-template.md";
    private static final String DEFAULT_PROMPT_TEMPLATE_VERSION = "event-file-v1";

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TemporalConfiguration temporalConfiguration;

    @Inject
    PromptTemplateLoader promptTemplateLoader;

    @ConfigProperty(name = "iia.alert-verification.fallback-on-invalid-llm", defaultValue = "false")
    boolean fallbackOnInvalidLlm;

    @ConfigProperty(name = "iia.alert-verification.event.prompt-template-version", defaultValue = DEFAULT_PROMPT_TEMPLATE_VERSION)
    String promptTemplateVersion = DEFAULT_PROMPT_TEMPLATE_VERSION;

    public LlmRequest build(AlertVerificationPromptData alert) {
        AiConfiguration.AlertVerify alertVerifyConfiguration = aiConfiguration.alertVerify();
        AiConfiguration.PromptTemplate promptTemplateConfiguration = promptTemplateConfiguration();
        String defaultTemporalZone = defaultTemporalZone();
        String model = alertVerifyConfiguration.model();
        Double temperature = alertVerifyConfiguration.temperature();
        Integer maxOutputTokens = alertVerifyConfiguration.maxOutputTokens();
        PromptTemplateDiagnostics systemDiagnostics = systemPrompt(defaultTemporalZone);
        PromptTemplateDiagnostics userDiagnostics = userPrompt(alert, defaultTemporalZone);
        String systemPrompt = systemDiagnostics.renderedText();
        String userPrompt = userDiagnostics.renderedText();
        int totalLength = systemPrompt.length() + userPrompt.length();
        String totalPromptHash = PromptTemplateDiagnostics.shortSha256(systemPrompt + "\n" + userPrompt);
        System.out.println("[IIA][ALERT_VERIFY][CONFIG] model="
                + model
                + " temperature="
                + temperature
                + " maxOutputTokens="
                + maxOutputTokens
                + " fallbackOnInvalidLlm="
                + fallbackOnInvalidLlm);
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL] temporal default zone loaded=" + defaultTemporalZone);
        System.out.println("[IIA][ALERT_VERIFY][PROMPT] includes temporal day-of-week support operators="
                + "LOCAL_DAY_OF_WEEK_IN,LOCAL_DAY_OF_WEEK_NOT_IN");
        System.out.println("[IIA][ALERT_VERIFY][PROMPT][SECTIONS] structured prompt sections built");
        System.out.println("[IIA][ALERT_EVENT_VERIFY][PROMPT_TEMPLATE] systemPath="
                + SYSTEM_PROMPT_TEMPLATE_PATH + " userPath=" + USER_PROMPT_TEMPLATE_PATH);
        System.out.println("[IIA][ALERT_EVENT_VERIFY][PROMPT_TEMPLATE] staticBlocksMovedToFile=true");
        logTemplateDiagnostics(
                "ALERT_EVENT_VERIFY",
                systemDiagnostics,
                userDiagnostics,
                totalPromptHash,
                totalLength);
        warnPromptLength("system", systemPrompt.length(), promptTemplateConfiguration.warnSystemLength());
        warnPromptLength("user", userPrompt.length(), promptTemplateConfiguration.warnUserLength());
        warnPromptLength("total", totalLength, promptTemplateConfiguration.warnTotalLength());
        return new LlmRequest(
                AiUseCase.ALERT_VERIFY,
                systemPrompt,
                userPrompt,
                model,
                temperature,
                maxOutputTokens,
                alert.alertId());
    }

    private PromptTemplateDiagnostics systemPrompt(String defaultTemporalZone) {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("SERVICE_DATA_CAPABILITY_CATALOG", ServiceDataCapabilityCatalog.compactPromptCatalog());
        variables.put("DEFAULT_TEMPORAL_ZONE", defaultTemporalZone);
        return promptTemplateLoader().renderWithDiagnostics(SYSTEM_PROMPT_TEMPLATE_PATH, variables);
    }

    private PromptTemplateDiagnostics userPrompt(AlertVerificationPromptData alert, String defaultTemporalZone) {
        System.out.println("[IIA][ALERT_VERIFY][CATALOG] allowedFields="
                + ServiceDataCapabilityCatalog.allowedFieldCount());
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("ALERT_INPUT", alertInputSection(alert));
        variables.put("ROUTE_CONTEXT_JSON", routeContextSection());
        variables.put("LOCATION_CONTEXT_JSON", locationResolutionSection(alert));
        variables.put("TEMPORAL_CONTEXT_JSON", temporalContextSection(defaultTemporalZone));
        variables.put("BACKEND_DERIVED_NON_LOCATION_CONSTRAINTS_JSON", nonLocationConstraintsSection(alert));
        variables.put("OUTPUT_INSTRUCTIONS", outputInstructionsSection());
        return promptTemplateLoader().renderWithDiagnostics(USER_PROMPT_TEMPLATE_PATH, variables);
    }

    private void logTemplateDiagnostics(
            String logScope,
            PromptTemplateDiagnostics systemDiagnostics,
            PromptTemplateDiagnostics userDiagnostics,
            String totalPromptHash,
            int totalLength) {
        System.out.println("[IIA][" + logScope + "][PROMPT_TEMPLATE] declaredSystemPlaceholders="
                + systemDiagnostics.declaredPlaceholdersLogValue());
        System.out.println("[IIA][" + logScope + "][PROMPT_TEMPLATE] declaredUserPlaceholders="
                + userDiagnostics.declaredPlaceholdersLogValue());
        System.out.println("[IIA][" + logScope + "][PROMPT_TEMPLATE] unresolvedSystemPlaceholdersAfterRender="
                + systemDiagnostics.unresolvedPlaceholdersAfterRenderLogValue());
        System.out.println("[IIA][" + logScope + "][PROMPT_TEMPLATE] unresolvedUserPlaceholdersAfterRender="
                + userDiagnostics.unresolvedPlaceholdersAfterRenderLogValue());
        System.out.println("[IIA][" + logScope + "][PROMPT_TEMPLATE] unusedSystemVariables="
                + systemDiagnostics.unusedVariablesLogValue());
        System.out.println("[IIA][" + logScope + "][PROMPT_TEMPLATE] unusedUserVariables="
                + userDiagnostics.unusedVariablesLogValue());
        System.out.println("[IIA][" + logScope + "][PROMPT_TEMPLATE] promptVersion="
                + defaultString(promptTemplateVersion, DEFAULT_PROMPT_TEMPLATE_VERSION)
                + " rawSystemTemplateHash=" + systemDiagnostics.templateHash()
                + " renderedSystemHash=" + systemDiagnostics.renderedHash()
                + " staticSystemHash=" + systemDiagnostics.renderedHash()
                + " renderedUserHash=" + userDiagnostics.renderedHash()
                + " totalPromptHash=" + totalPromptHash
                + " systemLength=" + systemDiagnostics.renderedLength()
                + " userLength=" + userDiagnostics.renderedLength()
                + " total=" + totalLength);
    }

    private void warnPromptLength(String label, int length, int threshold) {
        if (threshold > 0 && length > threshold) {
            System.out.println("[IIA][PROMPT_TEMPLATE][WARN] " + label
                    + " prompt length exceeds threshold: " + length + " > " + threshold);
        }
    }

    private String alertInputSection(AlertVerificationPromptData alert) {
        return """
                Alert to verify:
                - alertId: %s
                - originalPrompt: %s

                Metadata, not additional constraints:
                - name: %s
                - description: %s

                Use originalPrompt and backend-derived context as the authoritative sources of user intent.
                Metadata can help identify the alert but must not introduce extra technical requirements.
                Do not reject because metadata mentions technical expectations or because metadata names ServiceData fields.
                """.formatted(
                nullToEmpty(alert.alertId()),
                nullToEmpty(alert.prompt()),
                nullToEmpty(alert.name()),
                nullToEmpty(alert.description()));
    }

    private String locationResolutionSection(AlertVerificationPromptData alert) {
        return alert.locationResolutionContext().compactPromptSection();
    }

    private String nonLocationConstraintsSection(AlertVerificationPromptData alert) {
        StringBuilder section = new StringBuilder("Backend-derived non-location constraints:\n");
        if (alert.locationResolutionContext().nonLocationConstraints().isEmpty()) {
            section.append("- none\n");
        } else {
            for (AlertVerificationLocationContext.NonLocationConstraint constraint
                    : alert.locationResolutionContext().nonLocationConstraints()) {
                String displayRawText = nullToEmpty(constraint.rawText());
                if ("DELAY_EVENT_TYPE".equalsIgnoreCase(constraint.type())) {
                    String normalizedDelayEventType = AlertDelayEventTypeNormalizer.normalize(displayRawText);
                    if (normalizedDelayEventType == null) {
                        continue;
                    }
                    displayRawText = normalizedDelayEventType;
                }
                section.append("- ").append(nullToEmpty(constraint.type()))
                        .append("=").append(displayRawText).append("\n");
                if ("DELAY_THRESHOLD".equalsIgnoreCase(constraint.type())) {
                    appendDelayThresholdBreakdown(section, constraint.rawText());
                }
            }
        }
        return section.toString();
    }

    private void appendDelayThresholdBreakdown(StringBuilder section, String rawText) {
        String operator = valueAfterKey(rawText, "operator");
        String value = valueAfterKey(rawText, "value");
        String unit = valueAfterKey(rawText, "unit");
        if (operator != null) {
            section.append("  operator: ").append(operator).append("\n");
        }
        if (value != null) {
            section.append("  value: ").append(value).append("\n");
        }
        if (unit != null) {
            section.append("  unit: ").append(unit).append("\n");
        }
    }

    private String valueAfterKey(String rawText, String key) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        String prefix = key + "=";
        for (String part : rawText.split(";")) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return trimmed.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private String routeContextSection() {
        return """
                Event route context:
                - source: SERVICE_DATA
                - interpreterType: EVENT_INTERPRETER
                - triggerType: EVENT
                - inputModel: ServiceDataV2
                - outputModel: AgentOutput.CANDIDATE_SUGGESTION
                - evaluationMode: STATELESS_EVENT_MATCH
                - Runtime source: Kafka/Event ServiceDataV2, not snapshot API.
                """;
    }

    private String temporalContextSection(String defaultTemporalZone) {
        return """
                Event temporal runtime context:
                - defaultTimezone: %s
                - Temporal predicates must remain stateless over the current ServiceDataV2 event payload.
                """.formatted(defaultTemporalZone);
    }

    private String outputInstructionsSection() {
        return """
                Output instructions:
                - Return only valid raw JSON matching the system JSON output contract.
                - Do not wrap the JSON in markdown.
                - Do not add commentary outside the JSON object.
                """;
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private AiConfiguration.PromptTemplate promptTemplateConfiguration() {
        if (aiConfiguration != null && aiConfiguration.promptTemplate() != null) {
            return aiConfiguration.promptTemplate();
        }
        return new DefaultPromptTemplateConfiguration();
    }

    private PromptTemplateLoader promptTemplateLoader() {
        if (promptTemplateLoader == null) {
            promptTemplateLoader = new PromptTemplateLoader();
        }
        return promptTemplateLoader;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String defaultTemporalZone() {
        if (temporalConfiguration == null
                || temporalConfiguration.defaultZone() == null
                || temporalConfiguration.defaultZone().isBlank()) {
            return "Europe/Rome";
        }
        return temporalConfiguration.defaultZone();
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
