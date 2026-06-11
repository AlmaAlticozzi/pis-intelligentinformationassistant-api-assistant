package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.prompt.PromptTemplateLoader;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ScheduledServiceDataCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class ScheduledAlertVerificationPromptBuilder {

    private static final String SYSTEM_PROMPT_TEMPLATE_PATH = "iia/prompts/alert/scheduled/system.md";
    private static final String USER_PROMPT_TEMPLATE_PATH = "iia/prompts/alert/scheduled/user-template.md";
    private static final String DEFAULT_PROMPT_TEMPLATE_VERSION = "scheduled-file-v1";
    private static final String REMAINING_DYNAMIC_PLACEHOLDERS = String.join(",",
            "SCHEDULED_SERVICE_DATA_CAPABILITY_CATALOG",
            "ALERT_INPUT",
            "ROUTE_CONTEXT_JSON",
            "TEMPORAL_HINTS_JSON",
            "PLATFORM_HINTS_JSON",
            "CHANGE_HINTS_JSON",
            "CANCELLED_CALL_HINTS_JSON",
            "REPLACEMENT_HINTS_JSON",
            "LOCATION_CONTEXT_JSON",
            "RESOLVED_LOCATION_BINDINGS_JSON",
            "SERVICE_DATA_QUERY_CONTEXT_JSON",
            "UNSUPPORTED_CONSTRAINTS_JSON",
            "OUTPUT_INSTRUCTIONS");

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TemporalConfiguration temporalConfiguration;

    @Inject
    PromptTemplateLoader promptTemplateLoader;

    @ConfigProperty(name = "iia.alert.scheduled-verify.default-frequency-seconds", defaultValue = "600")
    int defaultPollingFrequencySeconds = 600;

    @ConfigProperty(name = "iia.alert.scheduled-verify.default-lookahead-minutes", defaultValue = "480")
    int defaultLookaheadMinutes = 480;

    @ConfigProperty(name = "iia.alert-verification.scheduled.prompt-template-version", defaultValue = DEFAULT_PROMPT_TEMPLATE_VERSION)
    String promptTemplateVersion = DEFAULT_PROMPT_TEMPLATE_VERSION;

    public LlmRequest build(ScheduledAlertVerificationPromptData data) {
        ScheduledVerifyConfiguration configuration = scheduledVerifyConfiguration();
        String systemPrompt = systemPrompt(data);
        String userPrompt = userPrompt(data);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PROMPT_TEMPLATE] systemPath="
                + SYSTEM_PROMPT_TEMPLATE_PATH + " userPath=" + USER_PROMPT_TEMPLATE_PATH);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PROMPT_TEMPLATE] systemLength="
                + systemPrompt.length() + " userLength=" + userPrompt.length()
                + " total=" + (systemPrompt.length() + userPrompt.length()));
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PROMPT_TEMPLATE] promptVersion="
                + defaultString(promptTemplateVersion, DEFAULT_PROMPT_TEMPLATE_VERSION));
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PROMPT_TEMPLATE] staticBlocksMovedToFile=true");
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][PROMPT_TEMPLATE] remainingDynamicPlaceholders="
                + REMAINING_DYNAMIC_PLACEHOLDERS);
        return new LlmRequest(
                AiUseCase.ALERT_SCHEDULED_VERIFY,
                systemPrompt,
                userPrompt,
                configuration.model(),
                configuration.temperature(),
                configuration.maxOutputTokens(),
                data == null ? null : data.alertId());
    }

    private ScheduledVerifyConfiguration scheduledVerifyConfiguration() {
        if (aiConfiguration == null) {
            return new ScheduledVerifyConfiguration("gpt-4.1-mini", 0.1, 3500);
        }
        AiConfiguration.AlertScheduledVerify scheduledVerify = aiConfiguration.alertScheduledVerify();
        if (scheduledVerify != null) {
            return new ScheduledVerifyConfiguration(
                    defaultString(scheduledVerify.model(), "gpt-4.1-mini"),
                    scheduledVerify.temperature() == null ? 0.1 : scheduledVerify.temperature(),
                    scheduledVerify.maxOutputTokens() == null ? 3500 : scheduledVerify.maxOutputTokens());
        }
        AiConfiguration.AlertVerify alertVerify = aiConfiguration.alertVerify();
        if (alertVerify != null) {
            return new ScheduledVerifyConfiguration(
                    defaultString(alertVerify.model(), "gpt-4.1-mini"),
                    alertVerify.temperature() == null ? 0.1 : alertVerify.temperature(),
                    alertVerify.maxOutputTokens() == null ? 5000 : alertVerify.maxOutputTokens());
        }
        return new ScheduledVerifyConfiguration("gpt-4.1-mini", 0.1, 3500);
    }

    private String systemPrompt(ScheduledAlertVerificationPromptData data) {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("SCHEDULED_SERVICE_DATA_CAPABILITY_CATALOG", scheduledCapabilityCatalogSection(data));
        return promptTemplateLoader().render(SYSTEM_PROMPT_TEMPLATE_PATH, variables);
    }

    private String userPrompt(ScheduledAlertVerificationPromptData data) {
        if (useCompactDynamicPrompt(data)) {
            return renderCompactUserPrompt(data);
        }
        return renderFullUserPrompt(data);
    }

    private String renderFullUserPrompt(ScheduledAlertVerificationPromptData data) {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("ALERT_NAME", data == null ? "" : value(data.name()));
        variables.put("ALERT_DESCRIPTION", data == null ? "" : value(data.description()));
        variables.put("ALERT_PROMPT", data == null ? "" : value(data.originalPrompt()));
        variables.put("ALERT_INPUT", alertInputSection(data));
        variables.put("ROUTE_CONTEXT_JSON", routeSection(data == null ? null : data.route()));
        variables.put("TEMPORAL_HINTS_JSON", temporalHintsSection(data == null ? null : data.temporalHints()));
        variables.put("PLATFORM_HINTS_JSON", platformHintsSection(data == null ? null : data.platformHints()));
        variables.put("CHANGE_HINTS_JSON", changeHintsSection(data == null ? null : data.changeHints()));
        variables.put("CANCELLED_CALL_HINTS_JSON", String.join("\n\n",
                journeyCancellationHintsSection(data == null ? null : data.journeyCancellationHints()),
                cancelledCallHintsSection(data == null ? null : data.cancelledCallHints())));
        variables.put("REPLACEMENT_HINTS_JSON", replacementHintsSection(data == null ? null : data.replacementHints()));
        variables.put("LOCATION_CONTEXT_JSON", scheduledLocationContextSection(data == null ? null : data.locationContext()));
        variables.put("RESOLVED_LOCATION_BINDINGS_JSON", resolvedLocationBindingsSection(data == null ? null : data.locationContext()));
        variables.put("SERVICE_DATA_QUERY_CONTEXT_JSON", serviceDataQueryRuntimeSection(data));
        variables.put("UNSUPPORTED_CONSTRAINTS_JSON", unsupportedConstraintsRuntimeSection());
        variables.put("OUTPUT_INSTRUCTIONS", outputInstructionsRuntimeSection());
        return promptTemplateLoader().render(USER_PROMPT_TEMPLATE_PATH, variables);
    }

    private String renderCompactUserPrompt(ScheduledAlertVerificationPromptData data) {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("ALERT_NAME", data == null ? "" : value(data.name()));
        variables.put("ALERT_DESCRIPTION", data == null ? "" : value(data.description()));
        variables.put("ALERT_PROMPT", data == null ? "" : value(data.originalPrompt()));
        variables.put("ALERT_INPUT", alertInputSection(data));
        variables.put("ROUTE_CONTEXT_JSON", routeSection(data == null ? null : data.route()));
        variables.put("TEMPORAL_HINTS_JSON", temporalHintsSection(data == null ? null : data.temporalHints()));
        variables.put("PLATFORM_HINTS_JSON", platformHintsSection(data == null ? null : data.platformHints()));
        variables.put("CHANGE_HINTS_JSON", String.join("\n\n",
                compactBackendDetectedConstraintsSection(data),
                changeHintsSection(data == null ? null : data.changeHints())));
        variables.put("CANCELLED_CALL_HINTS_JSON", journeyCancellationHintsSection(data == null ? null : data.journeyCancellationHints()));
        variables.put("REPLACEMENT_HINTS_JSON", replacementHintsSection(data == null ? null : data.replacementHints()));
        variables.put("LOCATION_CONTEXT_JSON", scheduledLocationContextSection(data == null ? null : data.locationContext()));
        variables.put("RESOLVED_LOCATION_BINDINGS_JSON", resolvedLocationBindingsSection(data == null ? null : data.locationContext()));
        variables.put("SERVICE_DATA_QUERY_CONTEXT_JSON", serviceDataQueryRuntimeSection(data));
        variables.put("UNSUPPORTED_CONSTRAINTS_JSON", unsupportedConstraintsRuntimeSection());
        variables.put("OUTPUT_INSTRUCTIONS", outputInstructionsRuntimeSection());
        return promptTemplateLoader().render(USER_PROMPT_TEMPLATE_PATH, variables);
    }

    private String serviceDataQueryRuntimeSection(ScheduledAlertVerificationPromptData data) {
        ScheduledServiceDataLocationContext context = data == null ? null : data.locationContext();
        if (context == null || context.apiQueryContext() == null) {
            return """
                    ServiceData API query runtime context: unavailable.
                    - Use ScheduledServiceDataLocationContext if present.
                    - Use defaults from Alert to verify and backend temporal hints.
                    """;
        }
        return """
                ServiceData API query runtime context:
                - monitoringScope: %s
                - serviceDataApiStopPoints: %s
                - requiresAllKnownStopPoints: %s
                - defaultPollingFrequencySeconds: %s
                - defaultServiceDataApiLookaheadMinutes: %s
                """.formatted(
                context.apiQueryContext().monitoringScope(),
                context.apiQueryContext().stopPoints(),
                context.apiQueryContext().requiresAllKnownStopPoints(),
                defaultPollingFrequencySeconds,
                defaultLookaheadMinutes);
    }

    private String unsupportedConstraintsRuntimeSection() {
        return """
                Backend-detected unsupported constraints:
                - No additional unsupported-constraint payload is provided to this prompt builder.
                - Apply the Unsupported constraints and Rejection policy from the system prompt.
                """;
    }

    private String outputInstructionsRuntimeSection() {
        return """
                Output instructions:
                - Return only valid raw JSON.
                - Use the JSON output contract from the system prompt.
                - Do not use markdown.
                """;
    }

    private boolean useCompactDynamicPrompt(ScheduledAlertVerificationPromptData data) {
        boolean hasChangeConstraint = data != null
                && data.changeHints() != null
                && data.changeHints().hasChangeConstraint();
        boolean hasJourneyCancellationConstraint = data != null
                && data.journeyCancellationHints() != null
                && data.journeyCancellationHints().hasJourneyCancellationConstraint();
        return data != null
                && data.platformHints() != null
                && data.platformHints().hasPlatformConstraint()
                && (hasChangeConstraint || hasJourneyCancellationConstraint);
    }
    private String compactBackendDetectedConstraintsSection(ScheduledAlertVerificationPromptData data) {
        StringBuilder section = new StringBuilder("""
                Backend detected required constraints:
                All backend detected required constraints must be represented in snapshotEvaluation.condition, except monitored stop points, which are represented in serviceDataQuery.stopPoints.
                """);
        ScheduledServiceDataLocationContext context = data == null ? null : data.locationContext();
        if (context != null) {
            section.append("- monitored stop: serviceDataQuery.stopPoints=")
                    .append(context.serviceDataApiStopPoints()).append('\n');
        }
        if (data != null && data.platformHints() != null && data.platformHints().hasPlatformConstraint()) {
            section.append("- platform constraints: ").append(data.platformHints().constraints()).append('\n');
        }
        if (data != null && data.changeHints() != null && data.changeHints().hasChangeConstraint()) {
            section.append("- change/exclusion constraints: ").append(data.changeHints().constraints()).append('\n');
        }
        if (data != null && data.journeyCancellationHints() != null && data.journeyCancellationHints().hasJourneyCancellationConstraint()) {
            section.append("- journey cancellation constraints: ").append(data.journeyCancellationHints().constraints()).append('\n');
        }
        return section.toString();
    }

    private String temporalHintsSection(ScheduledAlertTemporalHints hints) {
        if (hints == null) {
            return """
                    Backend-derived temporal hints: unavailable.
                    - Use default schedule.frequencySeconds and default ServiceData API lookahead values from the Defaults section.
                    - schedule.defaulted must be true when no explicit user frequency is known.
                    - timeWindow.defaulted must be true when no explicit user visibility window is known.
                    """;
        }
        return hints.compactPromptSection();
    }

    private String platformHintsSection(ScheduledAlertPlatformHints hints) {
        if (hints == null) {
            return """
                    Backend-derived platform/binario hints: unavailable.
                    - Still treat platform/binario/track/quay/banchina/marciapiede as non-location constraints.
                    - Never place platform values in serviceDataQuery.stopPoints.
                    """;
        }
        return hints.compactPromptSection();
    }

    private String changeHintsSection(ScheduledAlertChangeHints hints) {
        if (hints == null) {
            return """
                    Backend-derived change/cancellation/exclusion hints: unavailable.
                    - Still map change and exclusion wording only with Scheduled snapshot catalog fields.
                    - Generic journey cancellation/suppression belongs to journey cancellation hints, not change hints.
                    - Do not use payload.ongroundServiceEvent.*.
                    """;
        }
        return hints.compactPromptSection();
    }

    private String journeyCancellationHintsSection(ScheduledAlertJourneyCancellationHints hints) {
        if (hints == null) {
            return """
                    Backend-derived journey cancellation/suppression hints: unavailable.
                    - Still distinguish generic cancelled/suppressed journeys from changed-origin/destination/path signals.
                    - Generic cancelled/suppressed journeys must be based on arrival/departure status fields and passingType, not changes.
                    """;
        }
        return hints.compactPromptSection();
    }

    private String cancelledCallHintsSection(ScheduledAlertCancelledCallHints hints) {
        if (hints == null) {
            return """
                    Backend-derived cancelled/suppressed/skipped stop hints: unavailable.
                    - Still distinguish full journey cancellation from cancelled/suppressed/skipped stops.
                    - Cancelled/suppressed/skipped stops must use nextCancelledCalls[] when requested.
                    """;
        }
        return hints.compactPromptSection();
    }

    private String replacementHintsSection(ScheduledAlertReplacementHints hints) {
        if (hints == null) {
            return """
                    Backend-derived replacement/substitute service hints: unavailable.
                    - Still map replacement/substitute service constraints only with Scheduled snapshot catalog fields.
                    - Do not use payload.ongroundServiceEvent.*.
                    """;
        }
        return hints.compactPromptSection();
    }

    private String alertInputSection(ScheduledAlertVerificationPromptData data) {
        return """
                Alert to verify:
                - alertId: %s
                - originalPrompt: %s

                Metadata, not additional constraints:
                - name: %s
                - description: %s

                Defaults:
                - defaultPollingFrequencySeconds: %d
                - defaultServiceDataApiLookaheadMinutes: %d
                - defaultTimezone: %s
                """.formatted(
                value(data == null ? null : data.alertId()),
                value(data == null ? null : data.originalPrompt()),
                value(data == null ? null : data.name()),
                value(data == null ? null : data.description()),
                defaultPollingFrequencySeconds,
                defaultLookaheadMinutes,
                defaultTimezone());
    }

    private String routeSection(AlertRouteUnderstandingResult route) {
        if (route == null) {
            return "Alert Route Understanding result: unavailable.";
        }
        return """
                Alert Route Understanding result:
                - decision: %s
                - dataDomains: %s
                - primaryDataDomain: %s
                - interpreterType: %s
                - accessMode: %s
                - intentKind: %s
                - outputMode: %s
                - requiresPolling: %s
                - requiresServiceDataApi: %s
                - hasAggregation: %s
                - hasCardinalityThreshold: %s
                - hasReportIntent: %s
                - confidence: %s
                - summary: %s
                """.formatted(
                route.decision(),
                route.dataDomains(),
                route.primaryDataDomain(),
                route.interpreterType(),
                route.accessMode(),
                route.intentKind(),
                route.outputMode(),
                route.requiresPolling(),
                route.requiresServiceDataApi(),
                route.hasAggregation(),
                route.hasCardinalityThreshold(),
                route.hasReportIntent(),
                route.confidence(),
                value(route.summary()));
    }

    private String scheduledLocationContextSection(ScheduledServiceDataLocationContext context) {
        StringBuilder section = new StringBuilder("""
                ScheduledServiceDataLocationContext:
                Context is authoritative for locations.
                Every required location constraint must be represented either in serviceDataQuery or snapshotEvaluation.condition.
                serviceDataQuery.stopPoints must be exactly from context.serviceDataApiStopPoints unless monitoringScope=ALL_KNOWN_STOP_POINTS.
                Filter locations must not be placed into serviceDataQuery.stopPoints.
                """);
        if (context == null) {
            section.append("- unavailable\n");
            return section.toString();
        }
        section.append("- monitoringScope: ").append(context.monitoringScope()).append('\n');
        section.append("- requiresAllKnownStopPoints: ").append(context.requiresAllKnownStopPoints()).append('\n');
        section.append("- serviceDataApiStopPoints: ").append(context.serviceDataApiStopPoints()).append('\n');
        appendLocations(section, "monitoredLocations", context.monitoredLocations());
        appendLocations(section, "filterLocations", context.filterLocations());
        appendLocations(section, "excludedLocations", context.excludedLocations());
        section.append("- warnings: ").append(context.warnings()).append('\n');
        section.append("""

                ALL_KNOWN_STOP_POINTS:
                - stopPoints may be empty.
                - requiresAllKnownStopPoints=true is not an error.
                - The runtime or later verification phase will materialize all known stop point ids.
                """);
        return section.toString();
    }

    private String resolvedLocationBindingsSection(ScheduledServiceDataLocationContext context) {
        String json = resolvedLocationBindingsJson(context);
        int count = resolvedLocationBindingCount(context);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][LOCATION_BINDINGS] count=" + count);
        System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][LOCATION_BINDINGS] json=" + json);
        return """
                RESOLVED_LOCATION_BINDINGS_JSON:
                %s
                """.formatted(json);
    }

    private int resolvedLocationBindingCount(ScheduledServiceDataLocationContext context) {
        if (context == null) {
            return 0;
        }
        return context.monitoredLocations().size()
                + context.filterLocations().size()
                + context.excludedLocations().size();
    }

    private String resolvedLocationBindingsJson(ScheduledServiceDataLocationContext context) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"monitoredLocations\": ");
        appendLocationBindingArray(json, context == null ? List.of() : context.monitoredLocations(), true);
        json.append(",\n");
        json.append("  \"filterLocations\": ");
        List<ScheduledServiceDataResolvedLocation> filterLocations = new ArrayList<>();
        if (context != null) {
            filterLocations.addAll(context.filterLocations());
            filterLocations.addAll(context.excludedLocations());
        }
        appendLocationBindingArray(json, filterLocations, false);
        json.append("\n}");
        return json.toString();
    }

    private void appendLocationBindingArray(
            StringBuilder json,
            List<ScheduledServiceDataResolvedLocation> locations,
            boolean monitored) {
        json.append("[");
        if (locations == null || locations.isEmpty()) {
            json.append("]");
            return;
        }
        for (int i = 0; i < locations.size(); i++) {
            ScheduledServiceDataResolvedLocation location = locations.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("\n    {\n");
            appendJsonProperty(json, "role", String.valueOf(location.scheduledRole()), 6, true);
            appendJsonProperty(json, "rawText", location.rawText(), 6, true);
            appendJsonProperty(json, "normalizedText", location.normalizedText(), 6, true);
            appendJsonArrayProperty(json, "selectedPointIds", location.selectedPointIds(), 6, true);
            if (monitored) {
                appendJsonProperty(json, "target", "serviceDataQuery.stopPoints", 6, true);
                appendJsonProperty(json, "note",
                        "These ids are already applied to serviceDataQuery.stopPoints and must not be repeated in snapshotEvaluation.condition unless a distinct filter requires it.",
                        6, false);
            } else {
                appendRecommendedCondition(json, location, 6);
            }
            json.append("\n    }");
        }
        json.append("\n  ]");
    }

    private void appendRecommendedCondition(
            StringBuilder json,
            ScheduledServiceDataResolvedLocation location,
            int indent) {
        String field = recommendedConditionField(location);
        String operator = recommendedOperator(location);
        String insideAnyElementPath = insideAnyElementPath(field);
        json.append(" ".repeat(indent)).append("\"recommendedCondition\": {\n");
        appendJsonProperty(json, "field", relativeConditionField(field), indent + 2, true);
        appendJsonProperty(json, "operator", operator, indent + 2, true);
        if ("EQUALS".equals(operator)) {
            appendJsonProperty(json, "value", location.selectedPointIds().isEmpty() ? "" : location.selectedPointIds().getFirst(), indent + 2, true);
        } else {
            appendJsonArrayProperty(json, "values", location.selectedPointIds(), indent + 2, true);
        }
        appendJsonBooleanProperty(json, "relativeFieldInsideStopPointsJourneyDetailsAnyElement", insideAnyElementPath != null, indent + 2, true);
        appendJsonProperty(json, "insideAnyElementPath", insideAnyElementPath == null ? "" : insideAnyElementPath, indent + 2, false);
        json.append("\n").append(" ".repeat(indent)).append("}");
    }

    private String recommendedConditionField(ScheduledServiceDataResolvedLocation location) {
        if (location.targetFieldHints() != null) {
            for (String field : location.targetFieldHints()) {
                if (field != null && (field.endsWith(".stopPoint.id") || field.endsWith(".stopPointId.id"))) {
                    return field;
                }
            }
        }
        return switch (location.scheduledRole()) {
            case FILTER_CURRENT_STOP_POINT -> "stopPointsJourneyDetails[].stopPoint.id";
            case FILTER_ORIGIN_STOP_POINT -> "stopPointsJourneyDetails[].callStart.stopPoint.id";
            case FILTER_TIMETABLED_ORIGIN_STOP_POINT -> "stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id";
            case FILTER_DESTINATION_STOP_POINT -> "stopPointsJourneyDetails[].callEnd.stopPoint.id";
            case FILTER_TIMETABLED_DESTINATION_STOP_POINT -> "stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id";
            case FILTER_ROUTE_STOP_POINT -> "stopPointsJourneyDetails[].nextCalls[].stopPoint.id";
            case FILTER_TRANSIT_STOP_POINT -> "stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id";
            case FILTER_CANCELLED_CALL_STOP_POINT -> "stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id";
            case FILTER_REPLACEMENT_STOP_POINT,
                    FILTER_REPLACEMENT_SOURCE_START_STOP_POINT,
                    FILTER_REPLACEMENT_SOURCE_END_STOP_POINT -> "stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id";
            default -> "stopPointsJourneyDetails[].stopPoint.id";
        };
    }

    private String recommendedOperator(ScheduledServiceDataResolvedLocation location) {
        if (location.polarity() == ScheduledAlertLocationPolarity.EXCLUDE) {
            return "NOT_IN";
        }
        return location.selectedPointIds().size() == 1 ? "EQUALS" : "IN";
    }

    private String insideAnyElementPath(String field) {
        if (field == null || !field.startsWith("stopPointsJourneyDetails[].")) {
            return null;
        }
        return "stopPointsJourneyDetails[]";
    }

    private String relativeConditionField(String field) {
        if (field == null) {
            return "";
        }
        String prefix = "stopPointsJourneyDetails[].";
        if (field.startsWith(prefix)) {
            return field.substring(prefix.length());
        }
        return field;
    }

    private void appendJsonProperty(StringBuilder json, String name, String value, int indent, boolean comma) {
        json.append(" ".repeat(indent))
                .append("\"").append(name).append("\": \"")
                .append(escapeJson(value))
                .append("\"");
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private void appendJsonArrayProperty(StringBuilder json, String name, List<String> values, int indent, boolean comma) {
        json.append(" ".repeat(indent)).append("\"").append(name).append("\": [");
        List<String> safeValues = values == null ? List.of() : values;
        for (int i = 0; i < safeValues.size(); i++) {
            if (i > 0) {
                json.append(", ");
            }
            json.append("\"").append(escapeJson(safeValues.get(i))).append("\"");
        }
        json.append("]");
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private void appendJsonBooleanProperty(StringBuilder json, String name, boolean value, int indent, boolean comma) {
        json.append(" ".repeat(indent)).append("\"").append(name).append("\": ").append(value);
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private String escapeJson(String value) {
        return value == null ? "" : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private void appendLocations(StringBuilder section, String label, List<ScheduledServiceDataResolvedLocation> locations) {
        section.append("- ").append(label).append(":\n");
        if (locations == null || locations.isEmpty()) {
            section.append("  - none\n");
            return;
        }
        for (ScheduledServiceDataResolvedLocation location : locations) {
            section.append("  - rawText: ").append(value(location.rawText())).append('\n');
            section.append("    normalizedText: ").append(value(location.normalizedText())).append('\n');
            section.append("    role: ").append(location.scheduledRole()).append('\n');
            section.append("    polarity: ").append(location.polarity()).append('\n');
            section.append("    resolutionStatus: ").append(location.resolutionStatus()).append('\n');
            section.append("    selectedPointIds: ").append(location.selectedPointIds()).append('\n');
            section.append("    fallbackAllowed: ").append(location.fallbackAllowed()).append('\n');
            section.append("    fallbackToNameLong: ").append(location.fallbackToNameLong()).append('\n');
            section.append("    targetFieldHints: ").append(location.targetFieldHints()).append('\n');
            section.append("    warningReason: ").append(value(location.warningReason())).append('\n');
        }
    }

    private String scheduledCapabilityCatalogSection(ScheduledAlertVerificationPromptData data) {
        Set<String> capabilities = relevantCapabilities(data);
        List<String> fields = relevantScheduledFields(data, capabilities);
        StringBuilder section = new StringBuilder("""
                Relevant Scheduled ServiceData fields:
                Only these relevant fields/operators should be used for this prompt. Reject required constraints outside the Scheduled catalog.
                requirementCoverage.mappedBy may also use query/control fields listed here.
                Query/control coverage fields:
                - serviceDataQuery.stopPoints, serviceDataQuery.stopPoints[], body.stopPoints[]
                - serviceDataQuery.timeWindow, serviceDataQuery.timeWindow.lookaheadMinutes, serviceDataQuery.timeWindow.startMode, serviceDataQuery.timeWindow.endMode, serviceDataQuery.timeWindow.defaulted, serviceDataQuery.timeWindow.rawText
                - snapshotEvaluation.mode, snapshotEvaluation.threshold, snapshotEvaluation.threshold.operator, snapshotEvaluation.threshold.value
                - outputPolicy.emit, outputPolicy.includeCount, outputPolicy.includeMatchingJourneys
                - schedule.frequencySeconds, schedule.defaulted, schedule.rawText

                Relevant capabilities: %s
                Fields:
                """.formatted(capabilities));
        for (String field : fields) {
            ScheduledServiceDataCapabilityCatalog.FieldCapability capability =
                    ScheduledServiceDataCapabilityCatalog.findField(field).orElse(null);
            if (capability == null) {
                continue;
            }
            section.append("- ").append(field)
                    .append(" operators=").append(capability.operators());
            if (!capability.enumValues().isEmpty()) {
                section.append(" values=").append(capability.enumValues());
            }
            section.append('\n');
        }
        section.append("""

                DSL reminders:
                - Evaluate journeys at journeyPath stopPointsJourneyDetails[].
                - Put constraints on the same journey inside one anyElement path stopPointsJourneyDetails[].
                - Inside that anyElement use relative fields, e.g. changes, arrivalStatuses[].status, timetabledDeparturePlatform.dsc.
                - Query/control fields are valid only in requirementCoverage.mappedBy, never inside snapshotEvaluation.condition.
                """);
        if ((capabilities.contains("CANCELLATION") || capabilities.contains("JOURNEY_CANCELLATION"))
                && capabilities.contains("PLATFORM")) {
            section.append("""

                    Cancellation + platform:
                    If backend hints include both cancellation and platform, snapshotEvaluation.condition must combine them with ALL over the same stopPointsJourneyDetails[] element:
                    - one ANY branch for cancellation signals
                    - one ANY branch for platform signals
                    Do not place only cancellation and omit platform.
                    Preferred shape:
                    {"anyElement":{"path":"stopPointsJourneyDetails[]","conditions":{"all":[
                      {"any":[
                        {"all":[{"field":"arrivalStatuses[].status","operator":"CONTAINS","value":"ARRIVAL_CANCELLATION"},{"field":"departureStatuses[].status","operator":"CONTAINS","value":"DEPARTURE_CANCELLATION"}]},
                        {"all":[{"field":"arrivalStatuses[].status","operator":"CONTAINS","value":"ARRIVAL_CANCELLATION"},{"field":"passingType","operator":"EQUALS","value":"DESTINATION"}]},
                        {"all":[{"field":"departureStatuses[].status","operator":"CONTAINS","value":"DEPARTURE_CANCELLATION"},{"field":"passingType","operator":"EQUALS","value":"ORIGIN"}]}]},
                      {"any":[
                        {"field":"timetabledDeparturePlatform.dsc","operator":"EQUAL_PLATFORM","value":"5"},
                        {"field":"timetabledArrivalPlatform.dsc","operator":"EQUAL_PLATFORM","value":"5"},
                        {"field":"actualDeparturePlatform.platform.dsc","operator":"EQUAL_PLATFORM","value":"5"},
                        {"field":"actualArrivalPlatform.platform.dsc","operator":"EQUAL_PLATFORM","value":"5"}]}
                    ]}}}
                    """);
        }
        return section.toString();
    }

    private Set<String> relevantCapabilities(ScheduledAlertVerificationPromptData data) {
        Set<String> capabilities = new LinkedHashSet<>();
        capabilities.add("REPORT_OUTPUT");
        capabilities.add("QUERY");
        if (data == null) {
            return capabilities;
        }
        if (data.changeHints() != null && data.changeHints().hasChangeConstraint()) {
            capabilities.add("CANCELLATION");
        }
        if (data.journeyCancellationHints() != null && data.journeyCancellationHints().hasJourneyCancellationConstraint()) {
            capabilities.add("JOURNEY_CANCELLATION");
        }
        if (data.platformHints() != null && data.platformHints().hasPlatformConstraint()) {
            capabilities.add("PLATFORM");
        }
        if (data.cancelledCallHints() != null && data.cancelledCallHints().hasCancelledCallConstraint()) {
            capabilities.add("CANCELLED_CALL");
        }
        if (data.replacementHints() != null && data.replacementHints().hasReplacementConstraint()) {
            capabilities.add("REPLACEMENT");
        }
        if (data.temporalHints() != null && data.temporalHints().hasJourneyTimeFilter()) {
            capabilities.add("JOURNEY_TIME");
        }
        ScheduledServiceDataLocationContext context = data.locationContext();
        if (context != null) {
            context.filterLocations().stream()
                    .flatMap(location -> location.targetFieldHints().stream())
                    .forEach(field -> {
                        if (field.contains("callStart") || field.contains("callEnd")) {
                            capabilities.add("ORIGIN_DESTINATION");
                        }
                        if (field.contains("nextCalls") || field.contains("nextTransitCalls")) {
                            capabilities.add("ROUTE_CALL");
                        }
                    });
        }
        return capabilities;
    }

    private List<String> relevantScheduledFields(ScheduledAlertVerificationPromptData data, Set<String> capabilities) {
        LinkedHashSet<String> fields = new LinkedHashSet<>();
        fields.add("stopPointsJourneyDetails[].vehicleJourneyName");
        if (capabilities.contains("CANCELLATION")) {
            fields.add("stopPointsJourneyDetails[].changes");
            fields.add("stopPointsJourneyDetails[].exclusion.totalExclusion");
            fields.add("stopPointsJourneyDetails[].exclusion.timeBasedExclusion");
        }
        if (capabilities.contains("JOURNEY_CANCELLATION")) {
            fields.add("stopPointsJourneyDetails[].arrivalStatuses[].status");
            fields.add("stopPointsJourneyDetails[].departureStatuses[].status");
            fields.add("stopPointsJourneyDetails[].passingType");
        }
        if (capabilities.contains("PLATFORM")) {
            fields.add("stopPointsJourneyDetails[].timetabledDeparturePlatform.dsc");
            fields.add("stopPointsJourneyDetails[].timetabledArrivalPlatform.dsc");
            fields.add("stopPointsJourneyDetails[].actualDeparturePlatform.platform.dsc");
            fields.add("stopPointsJourneyDetails[].actualArrivalPlatform.platform.dsc");
        }
        if (capabilities.contains("JOURNEY_TIME")) {
            fields.add("stopPointsJourneyDetails[].callStart.departureTime");
            fields.add("stopPointsJourneyDetails[].callEnd.arrivalTime");
            fields.add("stopPointsJourneyDetails[].timetabledCallStart.departureTime");
            fields.add("stopPointsJourneyDetails[].timetabledCallEnd.arrivalTime");
        }
        if (capabilities.contains("ORIGIN_DESTINATION")) {
            fields.add("stopPointsJourneyDetails[].callStart.stopPoint.id");
            fields.add("stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id");
            fields.add("stopPointsJourneyDetails[].callEnd.stopPoint.id");
            fields.add("stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id");
        }
        if (capabilities.contains("ROUTE_CALL")) {
            fields.add("stopPointsJourneyDetails[].nextCalls[].stopPoint.id");
            fields.add("stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id");
        }
        if (capabilities.contains("CANCELLED_CALL")) {
            fields.add("stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id");
            fields.add("stopPointsJourneyDetails[].nextCancelledCalls");
        }
        if (capabilities.contains("REPLACEMENT")) {
            fields.add("stopPointsJourneyDetails[].isReplacementOf");
            fields.add("stopPointsJourneyDetails[].replacement");
            fields.add("stopPointsJourneyDetails[].externalReplacement");
            fields.add("stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id");
            fields.add("stopPointsJourneyDetails[].replacement.stopPointReplacements[].replacementType");
        }
        if (data != null && data.locationContext() != null) {
            data.locationContext().filterLocations().stream()
                    .flatMap(location -> location.targetFieldHints().stream())
                    .filter(ScheduledServiceDataCapabilityCatalog::isAllowedField)
                    .forEach(fields::add);
            data.locationContext().excludedLocations().stream()
                    .flatMap(location -> location.targetFieldHints().stream())
                    .filter(ScheduledServiceDataCapabilityCatalog::isAllowedField)
                    .forEach(fields::add);
        }
        return new ArrayList<>(fields);
    }

    private String defaultTimezone() {
        if (temporalConfiguration == null
                || temporalConfiguration.defaultZone() == null
                || temporalConfiguration.defaultZone().isBlank()) {
            return "Europe/Rome";
        }
        return temporalConfiguration.defaultZone();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private PromptTemplateLoader promptTemplateLoader() {
        if (promptTemplateLoader == null) {
            promptTemplateLoader = new PromptTemplateLoader();
        }
        return promptTemplateLoader;
    }

    private record ScheduledVerifyConfiguration(
            String model,
            Double temperature,
            Integer maxOutputTokens) {
    }
}
