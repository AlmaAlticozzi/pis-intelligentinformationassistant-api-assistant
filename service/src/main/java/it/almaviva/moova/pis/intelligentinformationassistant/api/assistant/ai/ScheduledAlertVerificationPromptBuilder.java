package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ScheduledAlertTechnicalSpecificationConstants;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ScheduledServiceDataCapabilityCatalog;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataResolvedLocation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ScheduledAlertVerificationPromptBuilder {

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TemporalConfiguration temporalConfiguration;

    @ConfigProperty(name = "iia.alert.scheduled-verify.default-frequency-seconds", defaultValue = "600")
    int defaultPollingFrequencySeconds = 600;

    @ConfigProperty(name = "iia.alert.scheduled-verify.default-lookahead-minutes", defaultValue = "480")
    int defaultLookaheadMinutes = 480;

    public LlmRequest build(ScheduledAlertVerificationPromptData data) {
        ScheduledVerifyConfiguration configuration = scheduledVerifyConfiguration();
        return new LlmRequest(
                AiUseCase.ALERT_SCHEDULED_VERIFY,
                systemPrompt(),
                userPrompt(data),
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

    private String systemPrompt() {
        return String.join("\n\n",
                missionAndSafetySection(),
                scheduledMvpContractSection(),
                outputJsonContractSection());
    }

    private String userPrompt(ScheduledAlertVerificationPromptData data) {
        if (useCompactDynamicPrompt(data)) {
            return compactDynamicUserPrompt(data);
        }
        return String.join("\n\n",
                alertInputSection(data),
                routeSection(data == null ? null : data.route()),
                temporalHintsSection(data == null ? null : data.temporalHints()),
                platformHintsSection(data == null ? null : data.platformHints()),
                changeHintsSection(data == null ? null : data.changeHints()),
                journeyCancellationHintsSection(data == null ? null : data.journeyCancellationHints()),
                cancelledCallHintsSection(data == null ? null : data.cancelledCallHints()),
                replacementHintsSection(data == null ? null : data.replacementHints()),
                scheduledLocationContextSection(data == null ? null : data.locationContext()),
                scheduledCapabilityCatalogSection(data),
                highPriorityMvpRulesSection(),
                semanticRulesSection(),
                locationMappingRulesSection(),
                journeyCorrelationRulesSection(),
                domainMappingRulesSection(),
                timeRulesSection(),
                rejectionRulesSection(),
                responseSkeletonSection());
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

    private String compactDynamicUserPrompt(ScheduledAlertVerificationPromptData data) {
        return String.join("\n\n",
                alertInputSection(data),
                routeSection(data == null ? null : data.route()),
                compactBackendDetectedConstraintsSection(data),
                temporalHintsSection(data == null ? null : data.temporalHints()),
                platformHintsSection(data == null ? null : data.platformHints()),
                changeHintsSection(data == null ? null : data.changeHints()),
                journeyCancellationHintsSection(data == null ? null : data.journeyCancellationHints()),
                scheduledLocationContextSection(data == null ? null : data.locationContext()),
                scheduledCapabilityCatalogSection(data),
                compactScheduledRulesSection(),
                compactResponseShapeSection());
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

    private String compactScheduledRulesSection() {
        return """
                Compact Scheduled verification rules:
                - Return only valid raw JSON.
                - VERIFIED requires technicalSpecification and agentBlueprintPreview; REJECTED requires both null and clear rejectedReason.
                - source SERVICE_DATA, interpreterType SCHEDULED_INTERPRETER, accessMode SERVICE_DATA_API_SNAPSHOT, triggerType SCHEDULE.
                - inputModel ServiceDataStopPointJourneysV2, evaluationMode SCHEDULED_SNAPSHOT_MATCH.
                - Report/count route: snapshotEvaluation.mode REPORT_COUNT, outputPolicy.emit EVERY_RUN, outputPolicy.includeCount true, threshold null.
                - Conditional threshold route: snapshotEvaluation.mode COUNT_MATCHING_JOURNEYS, outputPolicy.emit ON_MATCH, threshold required.
                - serviceDataQuery.stopPoints must be exactly the resolved monitored stop points from context.
                - requirementCoverage.mappedBy may use only ServiceData API query/control fields and real Scheduled catalog fields used by the condition.
                - Valid query/control mappedBy examples: serviceDataQuery.stopPoints, serviceDataQuery.timeWindow, schedule.frequencySeconds, outputPolicy.emit, outputPolicy.includeCount, outputPolicy.includeMatchingJourneys, snapshotEvaluation.mode, snapshotEvaluation.threshold.operator, snapshotEvaluation.threshold.value.
                - Valid condition mappedBy examples: stopPointsJourneyDetails[].changes, stopPointsJourneyDetails[].arrivalStatuses[].status, stopPointsJourneyDetails[].departureStatuses[].status, stopPointsJourneyDetails[].departureDelay.delay, stopPointsJourneyDetails[].arrivalDelay.delay.
                - Do not put JSON structural paths in mappedBy, such as snapshotEvaluation.condition.anyElement.path, snapshotEvaluation.condition.anyElement.conditions, snapshotEvaluation.condition.anyElement.conditions.all[].field, technicalSpecification.condition or agentBlueprintPreview.parameters.
                - Generic cancelled/suppressed journey semantics must use arrivalStatuses[].status, departureStatuses[].status and passingType; do not use changes CANCELLATION/PARTIALLY_CANCELLATION for generic cancelled/suppressed journey reports.
                - Query/control fields such as snapshotEvaluation.mode and outputPolicy.emit are not ServiceData fields and must not be used in snapshotEvaluation.condition.
                - condition.type must be SERVICE_DATA_SCHEDULED_FIELD_MATCH.
                - Use one anyElement path stopPointsJourneyDetails[] to correlate constraints on the same journey.
                - Inside stopPointsJourneyDetails[] anyElement use relative fields, not prefixed fields.
                - Do not invent ids, fields, operators or enum values.
                - Do not use payload.*, ServiceDataV2, EVENT_INTERPRETER, Kafka/event fields, runtime calls, Agent Definition, Agent Run or Suggestion.
                """;
    }

    private String compactResponseShapeSection() {
        return """
                Minimal JSON shape:
                {
                  "decision": "VERIFIED|REJECTED",
                  "summary": "...",
                  "rejectedReason": null,
                  "confidence": 0.0,
                  "requiredSources": ["SERVICE_DATA"],
                  "interpreterType": "SCHEDULED_INTERPRETER",
                  "accessMode": "SERVICE_DATA_API_SNAPSHOT",
                  "triggerType": "SCHEDULE",
                  "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH",
                  "inputModel": "ServiceDataStopPointJourneysV2",
                  "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                  "targetTypes": ["SERVICE_DATA_JOURNEY_AGGREGATE"],
                  "requirementCoverage": {"requirements": [], "allRequiredRequirementsMapped": true},
                  "technicalSpecification": {
                    "schemaVersion": "iia.alert.technical-specification/v2",
                    "source": "SERVICE_DATA",
                    "interpreterType": "SCHEDULED_INTERPRETER",
                    "accessMode": "SERVICE_DATA_API_SNAPSHOT",
                    "inputModel": "ServiceDataStopPointJourneysV2",
                    "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                    "triggerType": "SCHEDULE",
                    "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH",
                    "schedule": {"frequencySeconds": 600, "defaulted": true, "rawText": null},
                    "serviceDataQuery": {"operation": "POST /v2/stoppointjourneys", "monitoringScope": "EXPLICIT_STOP_POINTS", "stopPoints": [], "requiresAllKnownStopPoints": false, "timeWindow": {"startMode": "NOW_TRUNCATED_TO_MINUTE", "endMode": "NOW_PLUS_DEFAULT_LOOKAHEAD", "lookaheadMinutes": 480, "defaulted": true, "rawText": null}},
                    "snapshotEvaluation": {"mode": "REPORT_COUNT", "journeyPath": "stopPointsJourneyDetails[]", "condition": {}, "threshold": null},
                    "outputPolicy": {"emit": "EVERY_RUN", "includeCount": true, "includeMatchingJourneys": true},
                    "deduplicationKeyTemplate": "SERVICE_DATA_SCHEDULED:${alertId}:${queryWindowStart}:${conditionHash}"
                  },
                  "agentBlueprintPreview": {"schemaVersion": "iia.agent.blueprint/v1", "agentName": "ScheduledServiceDataSnapshotAlertAgent", "triggerType": "SCHEDULE", "requiredSources": ["SERVICE_DATA"], "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH", "targetTypes": ["SERVICE_DATA_JOURNEY_AGGREGATE"], "parameters": {"serviceDataQuery": {}, "snapshotEvaluation": {}, "outputPolicy": {}}, "stateRequirements": {"requiresState": false}, "output": {"type": "CANDIDATE_SUGGESTION"}},
                  "warnings": [],
                  "safetyChecks": ["No executable code generated.", "No Agent Definition created.", "No Agent Run created.", "No Suggestion created."]
                }
                """;
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

    private String missionAndSafetySection() {
        return """
                Mission and safety:
                - You verify a Scheduled Alert for the PIS Intelligent Information Assistant.
                - The user alert has already been routed as SERVICE_DATA + SCHEDULED_INTERPRETER.
                - Scheduled interpreter periodically queries ServiceData API snapshots.
                - The future Agent evaluates the returned snapshot, not a Kafka event.
                - Verification must not call APIs.
                - Verification must not access DB, Kafka, filesystem, external tools or live data.
                - Verification must not generate executable code.
                - Verification must not create Agent Definition, Agent Run or Suggestion.
                - Verification produces only a technical specification and blueprint preview.
                - Backend validator remains the final technical gate.
                - Return only valid raw JSON. Do not use markdown.
                - Do not invent ServiceData fields.
                - Do not invent stop point ids.
                - Use only stop point ids already resolved in ScheduledServiceDataLocationContext.
                - Do not place filter/control locations into serviceDataQuery.stopPoints unless they are also monitored stop points.
                - Do not use Event/Kafka fields.
                - Do not use payload.ongroundServiceEvent.*.
                """;
    }

    private String scheduledMvpContractSection() {
        return """
                Scheduled MVP contract:
                Allowed:
                - source: SERVICE_DATA
                - interpreterType: SCHEDULED_INTERPRETER
                - accessMode: SERVICE_DATA_API_SNAPSHOT
                - triggerType: SCHEDULE
                - inputModel: ServiceDataStopPointJourneysV2
                - outputModel: AgentOutput.CANDIDATE_SUGGESTION
                - evaluationMode: SCHEDULED_SNAPSHOT_MATCH
                - API operation: POST /v2/stoppointjourneys
                - evaluation over stopPointsJourneyDetails[]
                - report/count/boolean/threshold snapshot checks
                - no internal state for this MVP

                Not allowed:
                - Kafka event-only fields
                - EVENT_INTERPRETER
                - triggerType EVENT
                - ServiceDataV2 as input model
                - payload.ongroundServiceEvent.*
                - historical trends across multiple runs
                - absence of events across time unless represented by current snapshot arrays and supported by catalog
                - predictions
                - external APIs
                - weather, sports, devices, audio, CMS, display, broadcast
                - unsupported train attributes such as wifi, carriages/composition if not mapped by catalog
                - passenger count
                - creating Agent Definition/Run/Suggestion
                """;
    }

    private String outputJsonContractSection() {
        return """
                Output contract:
                - Return only valid raw JSON.
                - decision must be VERIFIED or REJECTED.
                - For VERIFIED, technicalSpecification and agentBlueprintPreview are mandatory.
                - For REJECTED, technicalSpecification must be null and agentBlueprintPreview must be null.
                - requirementCoverage must explain every required user constraint and mappedBy must contain only fields actually used.
                - requiredSources must be ["SERVICE_DATA"].
                - interpreterType must be SCHEDULED_INTERPRETER.
                - accessMode must be SERVICE_DATA_API_SNAPSHOT.
                - triggerType must be SCHEDULE.
                - evaluationMode must be SCHEDULED_SNAPSHOT_MATCH.
                - inputModel must be ServiceDataStopPointJourneysV2.
                - outputModel must be AgentOutput.CANDIDATE_SUGGESTION.
                """;
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

    private String semanticRulesSection() {
        return """
                Report vs conditional notification:
                - Report: user asks "how many", "number of", "count", "list", "report", or "tell me every X minutes how many".
                  outputPolicy.emit = EVERY_RUN; snapshotEvaluation.mode = REPORT_COUNT for count reports or REPORT_MATCHING_JOURNEYS for list/detail reports.
                  threshold must be absent/null.
                  includeCount=true for count reports.
                  Do not use COUNT_MATCHING_JOURNEYS for reports.
                - Conditional: user asks "notify me when/if there are at least/more than/exactly N journeys".
                  outputPolicy.emit = ON_MATCH; snapshotEvaluation.mode = COUNT_MATCHING_JOURNEYS or BOOLEAN_EXISTS.
                  threshold is required when a numeric threshold is requested.
                - Boolean check: user asks whether there is at least one matching journey.
                  outputPolicy.emit = ON_MATCH or ON_BOOLEAN_RESULT according to output mode.
                  snapshotEvaluation.mode = BOOLEAN_EXISTS or COUNT_MATCHING_JOURNEYS with threshold >= 1.
                """;
    }

    private String highPriorityMvpRulesSection() {
        return """
                High-priority Scheduled MVP rules:
                1. Report/count intent:
                - If the user asks to report, count, tell how many, list, summarize, or periodically provide a number without a trigger threshold:
                  snapshotEvaluation.mode = REPORT_COUNT when the output is a count.
                  outputPolicy.emit = EVERY_RUN.
                  outputPolicy.includeCount = true.
                  threshold must be null or absent.
                  Do not use COUNT_MATCHING_JOURNEYS for reports.
                - Phrases such as how many, count, number of, quanti/quante, numero di, dimmi quante, and fammi sapere quanti are report/count intent unless the user also expresses an explicit trigger threshold.
                - This remains a report/count intent even when the prompt includes filter/control locations such as origin X, destination Y, route includes Z, cancelled stop K, or not destination W.
                - Do not convert "for monitored stop X, tell me how many journeys have origin Y" into COUNT_MATCHING_JOURNEYS threshold >= 1.
                - Meaning example: "Every 10 minutes tell me how many delayed journeys are at stop X".
                  Expected: REPORT_COUNT + EVERY_RUN + no threshold.
                - Meaning example: "For monitored stop X, tell me how many journeys have origin Y".
                  Expected: REPORT_COUNT + EVERY_RUN + no threshold; map origin Y as a filter condition.

                2. Conditional threshold intent:
                - If the user asks to notify when/if at least/more than/exactly N journeys match:
                  snapshotEvaluation.mode = COUNT_MATCHING_JOURNEYS.
                  threshold is required.
                  outputPolicy.emit = ON_MATCH.
                  outputPolicy.includeCount = true.
                - Meaning example: "Notify me when at least two trains are arriving at X".
                  Expected: COUNT_MATCHING_JOURNEYS + threshold + ON_MATCH.

                3. Boolean exists intent:
                - If the user asks if there is at least one matching journey without asking for count/report:
                  prefer snapshotEvaluation.mode = BOOLEAN_EXISTS when supported.
                  outputPolicy.emit = ON_MATCH.
                  outputPolicy.includeMatchingJourneys should be true.
                  outputPolicy.includeCount should be true when useful, especially for "at least one" checks.
                  A candidate suggestion must contain enough information to explain why the alert matched.
                  Forbidden for VERIFIED: {"includeCount": false, "includeMatchingJourneys": false}.
                  outputPolicy.emit must not be EVERY_RUN for this MVP.

                4. Array operator JSON shape:
                - IN requires "values": [...].
                - NOT_IN requires "values": [...].
                - CONTAINS_ANY requires "values": [...].
                - Never use "value" with an array for IN, NOT_IN, or CONTAINS_ANY.
                - EQUALS and CONTAINS use a single "value".

                4a. requirementCoverage mappedBy field families:
                - requirementCoverage.mappedBy may reference serviceDataQuery.* for API query requirements.
                - requirementCoverage.mappedBy may reference stopPointsJourneyDetails[].* for journey snapshot data requirements.
                - requirementCoverage.mappedBy may reference only the specific allowed query/control fields for report/count/threshold/scheduling/output requirements.
                - count/report intent -> mappedBy may include snapshotEvaluation.mode, outputPolicy.emit, outputPolicy.includeCount.
                - cardinality threshold -> mappedBy may include snapshotEvaluation.threshold.operator and snapshotEvaluation.threshold.value.
                - polling frequency -> mappedBy may include schedule.frequencySeconds.
                - lookahead window -> mappedBy may include serviceDataQuery.timeWindow.lookaheadMinutes.
                - mappedBy describes user requirement coverage by real query/evaluation fields, not by internal JSON containers.
                - Never put JSON structural paths in mappedBy: snapshotEvaluation.condition, snapshotEvaluation.condition.type, snapshotEvaluation.condition.anyElement, snapshotEvaluation.condition.anyElement.path, snapshotEvaluation.condition.anyElement.conditions, snapshotEvaluation.condition.anyElement.conditions.all[].field, snapshotEvaluation.condition.anyElement.conditions.any[].field, technicalSpecification.condition, agentBlueprintPreview.parameters, agentBlueprintPreview.parameters.snapshotEvaluation.
                - For generic suppressed/cancelled journey report/count prompts, mappedBy should include serviceDataQuery.stopPoints, schedule.frequencySeconds, stopPointsJourneyDetails[].arrivalStatuses[].status, stopPointsJourneyDetails[].departureStatuses[].status, stopPointsJourneyDetails[].passingType, outputPolicy.emit, outputPolicy.includeCount.
                - Do not include stopPointsJourneyDetails[].changes or changes for generic suppressed/cancelled journey prompts.
                - These are not ServiceData fields and must not be put inside snapshotEvaluation.condition.

                5. LOCAL_TIME_BETWEEN JSON SHAPE - STRICT:
                The only valid leaf shape is:
                {
                  "field": "<relative-or-root-time-field>",
                  "operator": "LOCAL_TIME_BETWEEN",
                  "value": {
                    "start": "HH:mm:ss",
                    "end": "HH:mm:ss",
                    "timezone": "Europe/Rome"
                  }
                }
                Forbidden LOCAL_TIME_BETWEEN shapes:
                - Do not put start/end/timezone directly on the leaf.
                - Do not use startLocalTime or endLocalTime in output JSON.
                - startLocalTime/endLocalTime are backend internal hint names only; output JSON must use value.start and value.end.
                - Do not use value.startLocalTime or value.endLocalTime.
                - Do not omit value.

                6. anyElement JSON SHAPE - STRICT:
                Valid:
                {
                  "anyElement": {
                    "path": "stopPointsJourneyDetails[]",
                    "conditions": {
                      "all": [
                        { "...leaf...": "..." }
                      ]
                    }
                  }
                }
                Or:
                {
                  "anyElement": {
                    "path": "stopPointsJourneyDetails[]",
                    "conditions": {
                      "any": [
                        { "...leaf...": "..." }
                      ]
                    }
                  }
                }
                Forbidden anyElement shapes:
                - conditions must not be an array directly.
                - conditions must be an object containing all, any, field/operator, or nested anyElement according to DSL.
                - Do not output "conditions": [ ... ].

                7. Relative fields inside anyElement:
                - Inside anyElement path stopPointsJourneyDetails[], use relative fields such as callStart.departureTime, callEnd.arrivalTime, timetabledCallStart.departureTime, timetabledCallEnd.arrivalTime, and passingType.
                - Do not prefix fields with stopPointsJourneyDetails[] inside that anyElement.
                - Use absolute catalog fields only outside anyElement when allowed.

                8. Monitored stop point ids:
                - serviceDataQuery.stopPoints must contain monitored stop point ids from ScheduledServiceDataLocationContext.
                - serviceDataQuery.stopPoints is the authoritative representation of monitored stop points.
                - Monitored stop point ids are covered by serviceDataQuery.stopPoints.
                - MONITORED_STOP_POINT / SERVICE_DATA_API_QUERY_STOP_POINT must be represented in serviceDataQuery.stopPoints.
                - requirementCoverage.mappedBy for monitored stop point coverage must use serviceDataQuery.stopPoints or body.stopPoints[].
                - Do not add an extra snapshotEvaluation.condition on stopPoint.id merely to repeat monitored stop points.
                - Do not add a snapshotEvaluation condition on stopPoint.id only to restate the monitored stop point already used in the ServiceData API query.
                - Do not add callEnd.stopPoint.id or callStart.stopPoint.id for the same monitored stop point unless the user explicitly asks for destination/origin semantics and the location context marks that stop as a filter/control location.
                - Use snapshotEvaluation.condition only for constraints that filter the journeys returned by the API, such as arrivalStatuses, departureStatuses, delay, origin, destination, platform, changes, nextCalls, nextCancelledCalls, replacement, exclusion.
                - FILTER_* locations are different from MONITORED_STOP_POINT locations: FILTER_* locations must be represented in snapshotEvaluation.condition with the proper field and nested anyElement when needed.
                - Filter/control locations must be represented in snapshotEvaluation.condition.
                - Monitored locations need condition coverage only if the user asks a distinct field constraint not already represented by the API query scope.
                - For "arrives at monitored stop X between HH and HH", X is monitored query scope; use serviceDataQuery.stopPoints=[X], then condition arrival time only.
                - Example: "arrives at Garibaldi FS between 14:00 and 16:00" means serviceDataQuery.stopPoints=[Garibaldi id] plus an arrival time condition; do not add callEnd.stopPoint.id=Garibaldi unless Garibaldi is marked FILTER_DESTINATION_STOP_POINT.
                - For "has destination X", X is a FILTER_DESTINATION_STOP_POINT and must be condition callEnd.stopPoint.id.
                - For "origin X", X is a FILTER_ORIGIN_STOP_POINT and must be condition callStart.stopPoint.id.
                - For a multi-monitored threshold alert such as "Notify me when at stop A and stop B there are two arriving journeys":
                  serviceDataQuery.stopPoints=[A id, B id]; condition checks arrival status; threshold >= 2; no required stopPoint.id IN condition.
                - Generic report example: User asks for a count/report of journeys at a monitored stop point with an arrival cancellation.
                  Correct coverage:
                  monitored stop point -> serviceDataQuery.stopPoints
                  arrival cancellation -> arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION
                  Correct snapshotEvaluation:
                  {
                    "mode": "REPORT_COUNT",
                    "journeyPath": "stopPointsJourneyDetails[]",
                    "condition": {
                      "type": "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                      "all": [
                        {
                          "field": "arrivalStatuses[].status",
                          "operator": "CONTAINS",
                          "value": "ARRIVAL_CANCELLATION"
                        }
                      ]
                    },
                    "threshold": null
                  }
                  Wrong:
                  requirementCoverage.mappedBy = stopPointsJourneyDetails[].stopPoint.id
                  snapshotEvaluation condition with field stopPoint.id only to represent the monitored stop point.
                """;
    }

    private String locationMappingRulesSection() {
        return """
                Monitored stop points vs filter locations:
                - serviceDataQuery.stopPoints must come only from context.serviceDataApiStopPoints.
                - If monitoringScope=EXPLICIT_STOP_POINTS, stopPoints must be non-empty.
                - If monitoringScope=ALL_KNOWN_STOP_POINTS, stopPoints may be empty and requiresAllKnownStopPoints=true.
                - Monitored stop point ids are already represented by serviceDataQuery.stopPoints and should not be repeated in snapshotEvaluation.condition unless a distinct user constraint requires it.
                - Filter/control locations must be represented in snapshotEvaluation.condition, not in serviceDataQuery.stopPoints.
                - Resolved filter locations must use stop point id fields with EQUALS/IN/NOT_IN.
                - Unresolved include filter locations may use nameLong CONTAINS_NORMALIZED only if the catalog supports it.
                - Unresolved exclude filter locations may use NOT_CONTAINS_NORMALIZED only if the catalog supports it.
                - Never invent ids.
                - Never use ids not present in the scheduled location context.

                Scheduled field mapping by scheduled location role:
                - FILTER_CURRENT_STOP_POINT -> stopPoint.id or stopPoint.nameLong
                - FILTER_ORIGIN_STOP_POINT -> stopPointsJourneyDetails[].callStart.stopPoint.id/nameLong
                - FILTER_TIMETABLED_ORIGIN_STOP_POINT -> stopPointsJourneyDetails[].timetabledCallStart.stopPoint.id/nameLong
                - FILTER_DESTINATION_STOP_POINT -> stopPointsJourneyDetails[].callEnd.stopPoint.id/nameLong
                - FILTER_TIMETABLED_DESTINATION_STOP_POINT -> stopPointsJourneyDetails[].timetabledCallEnd.stopPoint.id/nameLong
                - FILTER_ROUTE_STOP_POINT -> stopPointsJourneyDetails[].nextCalls[].stopPoint.id/nameLong
                - FILTER_TRANSIT_STOP_POINT -> stopPointsJourneyDetails[].nextTransitCalls[].stopPoint.id/nameLong
                - FILTER_CANCELLED_CALL_STOP_POINT -> stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id/nameLong
                - FILTER_REPLACEMENT_STOP_POINT -> stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id
                - EXCLUDED roles use NOT_IN for resolved ids when possible.
                """;
    }

    private String journeyCorrelationRulesSection() {
        return """
                Journey condition correlation:
                - For constraints on the same journey, use anyElement with path stopPointsJourneyDetails[].
                - Inside anyElement, fields are relative to the same stopPointsJourneyDetails[] item.
                - For child arrays, use nested anyElement for nextCalls[], nextTransitCalls[], nextCancelledCalls[], replacement.stopPointReplacements[].
                - Do not create sibling conditions that lose correlation.
                - Complex condition composition: use "all" for constraints that must all be true for the same journey and "any" for alternatives.
                - Do not use separate stopPointsJourneyDetails[] anyElements for constraints that the user intends to apply to the same journey.
                - Delay + platform change + excluded destination must be one stopPointsJourneyDetails[] anyElement with all:
                  delay condition, changes CONTAINS PLATFORM_CHANGED, callEnd.stopPoint.id NOT_IN [destination id].
                - Delay + origin + journey time filter must be one stopPointsJourneyDetails[] anyElement with all:
                  callStart.stopPoint.id EQUALS origin id, delay condition, callStart.departureTime LOCAL_TIME_BETWEEN.
                - Replacement stop + replacement type + delay must keep delay in the outer journey anyElement and stopPointId.id + replacementType in the same replacement.stopPointReplacements[] anyElement.
                - Suppressed stop + destination exclusion must keep nextCancelledCalls[] stopPoint.id and callEnd.stopPoint.id NOT_IN in the same outer journey anyElement.

                Example same-journey shape:
                {
                  "type": "SERVICE_DATA_SCHEDULED_FIELD_MATCH",
                  "anyElement": {
                    "path": "stopPointsJourneyDetails[]",
                    "conditions": {
                      "all": [
                        {"field": "departureDelay.delay", "operator": "GREATER_THAN", "value": 600},
                        {"field": "timetabledDeparturePlatform.dsc", "operator": "EQUAL_PLATFORM", "value": "3"}
                      ]
                    }
                  }
                }
                """;
    }

    private String domainMappingRulesSection() {
        return """
                Delays:
                - Generic delayed with no arrival/departure direction: use any over arrivalDelay.delay and departureDelay.delay, or statuses ARRIVAL_DELAY/DEPARTURE_DELAY if no numeric threshold is present.
                - If numeric threshold is present, convert minutes to seconds: 10 minutes -> 600; 15 minutes -> 900.
                - Delay in departure maps to departureDelay.delay and/or departureStatuses[].status.
                - Delay in arrival maps to arrivalDelay.delay and/or arrivalStatuses[].status.
                - For delayed without explicit threshold, use delay EXISTS or delay GREATER_THAN 0 according to allowed catalog operators.

                Arrival/departure statuses:
                - Scheduled uses API snapshot, so do not use payload.ongroundServiceEvent.eventsType.
                - arriving -> arrivalStatuses[].status CONTAINS ARRIVING.
                - arrived -> arrivalStatuses[].status CONTAINS ARRIVED.
                - departing -> departureStatuses[].status CONTAINS DEPARTING.
                - departed -> departureStatuses[].status CONTAINS DEPARTED.

                Platforms:
                - Platform/binario/track/quay/banchina/marciapiede are not locations; they are non-location constraints.
                - Platform constraints must be placed inside snapshotEvaluation.condition.
                - Never put platform values in serviceDataQuery.stopPoints.
                - Never treat "binario 3" or "platform 3" as a stop point.
                - Use only ScheduledServiceDataCapabilityCatalog platform fields and operators.
                - Do not classify as scheduled only because a number exists; distinguish platform values from journey counts.
                - Simple departure platform equality defaults to timetabledDeparturePlatform.dsc EQUAL_PLATFORM unless actual/current/effective/real is explicit.
                  Shape: {"field": "timetabledDeparturePlatform.dsc", "operator": "EQUAL_PLATFORM", "value": "3"}.
                - Simple arrival platform equality defaults to timetabledArrivalPlatform.dsc EQUAL_PLATFORM unless actual/current/effective/real is explicit.
                  Shape: {"field": "timetabledArrivalPlatform.dsc", "operator": "EQUAL_PLATFORM", "value": "1"}.
                - If actual/current/effective/real platform is explicit, use actualDeparturePlatform.platform.dsc, actualDeparturePlatform.displayPlatform.dsc, actualArrivalPlatform.platform.dsc, or actualArrivalPlatform.displayPlatform.dsc when catalog-supported.
                - If direction is unspecified, infer departure from departure wording, arrival from arrival wording; if still unclear use an OR over arrival and departure platform fields or reject if ambiguity is too high.
                - Platform numeric/property operators must be used on platform .dsc fields:
                  PLATFORM_NUMBER_GREATER_THAN, PLATFORM_NUMBER_GREATER_OR_EQUAL, PLATFORM_NUMBER_LESS_THAN, PLATFORM_NUMBER_LESS_OR_EQUAL, PLATFORM_NUMBER_BETWEEN, PLATFORM_NUMBER_EVEN, PLATFORM_NUMBER_ODD, PLATFORM_NUMBER_DOUBLE_DIGIT, PLATFORM_HAS_LETTER_SUFFIX, PLATFORM_NUMBER_MULTIPLE_OF.
                  Shape: {"field": "timetabledDeparturePlatform.dsc", "operator": "PLATFORM_NUMBER_GREATER_THAN", "value": 2}.
                  Between shape: {"field": "timetabledDeparturePlatform.dsc", "operator": "PLATFORM_NUMBER_BETWEEN", "value": {"min": 2, "max": 5}}.
                - Platform change uses changes CONTAINS PLATFORM_CHANGED when direction is unspecified.
                - Departure platform change may use departureStatuses[].status CONTAINS DEPARTURE_PLATFORM_CHANGED or timetabledDeparturePlatform.dsc PLATFORM_NOT_EQUALS_FIELD actualDeparturePlatform.platform.dsc.
                - Arrival platform change may use arrivalStatuses[].status CONTAINS ARRIVAL_PLATFORM_CHANGED or timetabledArrivalPlatform.dsc PLATFORM_NOT_EQUALS_FIELD actualArrivalPlatform.platform.dsc.
                - PLATFORM_NOT_EQUALS_FIELD shape: {"field": "timetabledDeparturePlatform.dsc", "operator": "PLATFORM_NOT_EQUALS_FIELD", "otherField": "actualDeparturePlatform.platform.dsc"}.
                - Platform constraints on journeys must be inside the same stopPointsJourneyDetails[] anyElement as delay/time/origin conditions for the same journey.

                Origin/destination/route/cancelled calls:
                - monitored X, origin Y -> serviceDataQuery.stopPoints=[X id], condition on callStart.stopPoint.id=Y id.
                - monitored X, planned origin Y -> condition on timetabledCallStart.stopPoint.id.
                - monitored X, destination Y -> condition on callEnd.stopPoint.id.
                - monitored X, planned destination Y -> condition on timetabledCallEnd.stopPoint.id.
                - monitored X, passes through Y -> nested anyElement nextCalls[] stopPoint.id=Y.
                - monitored X, transit through Y -> nested anyElement nextTransitCalls[] stopPoint.id=Y.
                - monitored X, cancelled/suppressed stop Y -> nested anyElement nextCancelledCalls[] stopPoint.id=Y.

                Cancelled/suppressed/skipped stops using nextCancelledCalls[]:
                - Cancelled/suppressed/skipped stop constraints are different from full journey cancellation.
                - "journey/train/service is cancelled" maps to journey cancellation status fields, not changes.
                - "suppressed/cancelled/skipped stop X", "cancelled call X", "skips stop X" maps to nextCancelledCalls[].stopPoint.
                - If user asks for a specific suppressed/cancelled/skipped stop X, use a nested anyElement:
                  outer anyElement path stopPointsJourneyDetails[], inner anyElement path nextCancelledCalls[],
                  then stopPoint.id EQUALS/IN the resolved id.
                - Inside nextCancelledCalls[] anyElement, fields are relative: stopPoint.id, stopPoint.nameLong.
                - Do not use stopPointsJourneyDetails[].nextCancelledCalls[].stopPoint.id inside the nested anyElement.
                - If X is unresolved but fallback is allowed, use stopPoint.nameLong CONTAINS_NORMALIZED X.
                - If user asks for any suppressed/cancelled/skipped stop without a specific stop, use nextCancelledCalls NOT_EMPTY or EXISTS if catalog-supported.
                - Do not map "fermata soppressa X" to changes CONTAINS CANCELLATION; cancelled-call semantics are about stopPointsJourneyDetails[] calls, not whole-journey changes.
                - Do not put cancelled stop X into serviceDataQuery.stopPoints unless X is also explicitly monitored.
                - Specific cancelled stop shape:
                  {"anyElement":{"path":"stopPointsJourneyDetails[]","conditions":{"all":[{"anyElement":{"path":"nextCancelledCalls[]","conditions":{"field":"stopPoint.id","operator":"EQUALS","value":"TNPNTS..."}}}]}}}
                - Generic any cancelled stop shape:
                  {"anyElement":{"path":"stopPointsJourneyDetails[]","conditions":{"field":"nextCancelledCalls","operator":"NOT_EMPTY"}}}

                Changes, cancellations, exclusions:
                - Use changes CONTAINS CHANGED_ORIGIN for changed origin.
                - Use changes CONTAINS CHANGED_DESTINATION for changed destination.
                - Use changes CONTAINS CHANGED_PATH for changed path, route changed, path changed or changed itinerary.
                - Use changes CONTAINS EXTRA_JOURNEY for extra journey, additional journey or special/extra service.
                - Use changes CONTAINS PLATFORM_CHANGED for generic platform change.
                - Use exclusion.totalExclusion EQUALS true for total exclusion.
                - Use exclusion.timeBasedExclusion EQUALS true for time-based exclusion.
                - For scheduled snapshot alerts about generic cancelled/suppressed journeys, do not use changes as the semantic signal.
                - Generic cancelled/suppressed journey semantics must be based on arrivalStatuses[].status, departureStatuses[].status and passingType.
                - A generic cancelled/suppressed journey at a stop point is:
                  arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION AND departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION;
                  OR arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION AND passingType EQUALS DESTINATION;
                  OR departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION AND passingType EQUALS ORIGIN.
                - Do not use changes CONTAINS CANCELLATION or changes CONTAINS PARTIALLY_CANCELLATION for generic "corsa soppressa" / "cancelled journey" count/report alerts.
                - Use changes only for explicit change/intention prompts such as cambio origine, cambio destinazione, cambio percorso, variazione, changes, changed origin, changed destination.
                - Negative cancellation/change wording must use only catalog-supported negative operators. If enum arrays do not support NOT_CONTAINS, reject rather than inventing NOT_CONTAINS.
                - Do not use NOT_CONTAINS_NORMALIZED on enum fields.
                - Do not use payload.ongroundServiceEvent.*.

                Replacement / substitute services:
                - Generic "replacement/substitute service/journey", "corsa sostitutiva", "servizio sostitutivo":
                  use isReplacementOf NOT_EMPTY inside stopPointsJourneyDetails[] anyElement when catalog-supported.
                  Shape: {"field":"isReplacementOf","operator":"NOT_EMPTY"}
                - If the user asks for journeys with replacement data, use replacement NOT_NULL or EXISTS.
                  Shape: {"field":"replacement","operator":"NOT_NULL"}
                - If the user explicitly asks for external replacement data, use externalReplacement NOT_NULL or EXISTS.
                  Shape: {"field":"externalReplacement","operator":"NOT_NULL"}
                - Specific "replacement stop X" / "fermata sostitutiva X" must use nested anyElement:
                  outer anyElement path stopPointsJourneyDetails[], inner anyElement path replacement.stopPointReplacements[],
                  condition stopPointId.id EQUALS/IN resolved id.
                - "replacement stop X" -> nested anyElement replacement.stopPointReplacements[] stopPointId.id.
                - Inside replacement.stopPointReplacements[] anyElement, fields are relative:
                  stopPointId.id, replacementType, arrivalTime, departureTime.
                - Do not use stopPointsJourneyDetails[].replacement.stopPointReplacements[].stopPointId.id inside the nested anyElement.
                - Replacement type uses replacementType EQUALS/IN ARRIVAL/DEPARTURE/ARRIVALDEPARTURE.
                - Replacement type shape uses replacementType IN/EQUALS DEPARTURE/ARRIVAL/ARRIVALDEPARTURE.
                - "in partenza" maps to DEPARTURE or IN ["DEPARTURE","ARRIVALDEPARTURE"].
                - "in arrivo" maps to ARRIVAL or IN ["ARRIVAL","ARRIVALDEPARTURE"].
                - If replacement stop and replacement type are both requested, put both leaves inside the same replacement.stopPointReplacements[] anyElement.
                - Unsupported replacement source route start/end stop points:
                  if the user asks for "tratta sostitutiva da X a Y" and no catalog stop point id fields support that source route,
                  return REJECTED with reason "Replacement source route start/end stop points are not supported by the Scheduled ServiceData snapshot catalog."
                Strict replacement stop shape:
                  {"anyElement":{"path":"stopPointsJourneyDetails[]","conditions":{"all":[{"anyElement":{"path":"replacement.stopPointReplacements[]","conditions":{"field":"stopPointId.id","operator":"EQUALS","value":"TNPNTS..."}}}]}}}
                Strict replacement stop with type shape:
                  {"anyElement":{"path":"stopPointsJourneyDetails[]","conditions":{"all":[{"anyElement":{"path":"replacement.stopPointReplacements[]","conditions":{"all":[{"field":"stopPointId.id","operator":"EQUALS","value":"TNPNTS..."},{"field":"replacementType","operator":"EQUALS","value":"DEPARTURE"}]}}}]}}}
                """;
    }

    private String timeRulesSection() {
        return """
                Time concepts:
                1. Polling frequency:
                - "every 5 minutes", "ogni 10 minuti", "every hour", "ogni ora" -> technicalSpecification.schedule.frequencySeconds.
                - If absent, use default frequencySeconds and defaulted=true.

                2. API visibility window:
                - "next 2 hours", "nelle prossime due ore" -> serviceDataQuery.timeWindow.lookaheadMinutes=120, defaulted=false.
                - If absent, use default lookahead minutes and defaulted=true.

                3. Journey time filters:
                - Journey time filters are conditions inside snapshotEvaluation.condition; they are not serviceDataQuery.timeWindow.
                - API visibility window controls how far the ServiceData API query looks ahead.
                - Journey time filters select journeys inside the returned snapshot.
                - "departures between 10:00 and 12:00" -> condition on departure timestamp field using LOCAL_TIME_BETWEEN.
                - "after 18:30" -> LOCAL_TIME_BETWEEN start=18:30:00 end=23:59:59 when no LOCAL_TIME_AFTER operator exists.
                - "before 09:00" -> LOCAL_TIME_BETWEEN start=00:00:00 end=09:00:00 when no LOCAL_TIME_BEFORE operator exists.
                - Departure time fields: callStart.departureTime, timetabledCallStart.departureTime, timetabledDepartureTime, estimatedDepartureTime, nextCalls[].departureTime.
                - Arrival time fields: callEnd.arrivalTime, timetabledCallEnd.arrivalTime, timetabledArrivalTime, estimatedArrivalTime, nextCalls[].arrivalTime.
                - Passing/transit time fields: nextTransitCalls[].passingTime; use nextCalls[].arrivalTime/departureTime for route-call filters when appropriate.
                - Planned/scheduled/timetabled/programmed departure/arrival uses timetabled fields.
                - Actual/effective/current departure/arrival uses callStart/callEnd or estimated fields.
                - If unspecified, prefer callStart.departureTime for departure and callEnd.arrivalTime for arrival when available.
                - Time filters must be correlated with other journey constraints inside the same stopPointsJourneyDetails[] anyElement.
                - Do not put a time filter outside the journey anyElement in a way that loses correlation.
                - Boolean exists prompts such as "there is at least one journey between 10:00 and 12:00" should use BOOLEAN_EXISTS + ON_MATCH, or COUNT_MATCHING_JOURNEYS threshold >= 1 when necessary.
                - Count reports with time filters use REPORT_COUNT + EVERY_RUN + no threshold.
                - Conditional thresholds with time filters use COUNT_MATCHING_JOURNEYS + threshold + ON_MATCH.
                - Use Europe/Rome unless explicit timezone is supplied.

                Reject:
                - historical trends across multiple runs
                - prediction beyond the ServiceData API visibility window
                - absence over time requiring state/history
                - historical comparison such as "compared with yesterday"
                - exact single time filters unless a supported backend policy can map them safely
                """;
    }

    private String rejectionRulesSection() {
        return """
                Rejection policy:
                - Verify only if every required user constraint is mappable with ScheduledServiceDataCapabilityCatalog.
                - If a prompt contains both supported and unsupported constraints, reject it; do not partially accept.
                - Reject weather, sports, devices, audio messages, CMS content, display/broadcast history, passenger count, wifi, unsupported composition/carriage constraints, predictions, external APIs, and history/state requirements.
                - Unsupported required constraints must be REJECTED explicitly; do not map unsupported constraints to approximate fields.
                - "wifi on board" -> REJECTED.
                - "more than 10 carriages/coaches", "train composition" -> REJECTED.
                - "crowded trains", passenger count, occupancy or seat availability -> REJECTED.
                - "weather/rain/snow" -> REJECTED.
                - "no trains for 30 minutes" or absence over continuous time -> REJECTED because it requires state/history.
                - Negative filters are allowed only with catalog-supported negative operators:
                  stop point ids use NOT_IN, name fallback can use NOT_CONTAINS_NORMALIZED, exact normalized text can use NOT_EQUALS_NORMALIZED when supported.
                - Do not invent NOT_CONTAINS on enum arrays.
                - If "arrival cancellation but not departure cancellation" requires a negative enum-array operator that is not in the catalog, return REJECTED.
                - For REJECTED, technicalSpecification must be null, agentBlueprintPreview must be null, rejectedReason must be clear, and requirementCoverage must identify the unmappable requirement.
                """;
    }

    private String responseSkeletonSection() {
        return """
                Response JSON contract:
                {
                  "decision": "VERIFIED",
                  "summary": "The alert can be evaluated by periodically querying ServiceData stop point journey snapshots.",
                  "rejectedReason": null,
                  "confidence": 0.86,
                  "requiredSources": ["SERVICE_DATA"],
                  "interpreterType": "SCHEDULED_INTERPRETER",
                  "accessMode": "SERVICE_DATA_API_SNAPSHOT",
                  "triggerType": "SCHEDULE",
                  "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH",
                  "inputModel": "ServiceDataStopPointJourneysV2",
                  "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                  "targetTypes": ["SERVICE_DATA_JOURNEY_AGGREGATE"],
                  "requirementCoverage": {
                    "requirements": [
                      {"text": "required user constraint", "required": true, "mappable": true, "mappedBy": ["stopPointsJourneyDetails[].arrivalDelay.delay"], "reason": null}
                    ],
                    "allRequiredRequirementsMapped": true
                  },
                  "technicalSpecification": {
                    "schemaVersion": "iia.alert.technical-specification/v2",
                    "source": "SERVICE_DATA",
                    "interpreterType": "SCHEDULED_INTERPRETER",
                    "accessMode": "SERVICE_DATA_API_SNAPSHOT",
                    "inputModel": "ServiceDataStopPointJourneysV2",
                    "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                    "triggerType": "SCHEDULE",
                    "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH",
                    "schedule": {"frequencySeconds": 600, "defaulted": true, "rawText": null},
                    "serviceDataQuery": {
                      "operation": "POST /v2/stoppointjourneys",
                      "monitoringScope": "EXPLICIT_STOP_POINTS",
                      "stopPoints": ["TNPNTS00000000005439"],
                      "requiresAllKnownStopPoints": false,
                      "timeWindow": {
                        "startMode": "NOW_TRUNCATED_TO_MINUTE",
                        "endMode": "NOW_PLUS_DEFAULT_LOOKAHEAD",
                        "lookaheadMinutes": 480,
                        "defaulted": true,
                        "rawText": null
                      }
                    },
                    "snapshotEvaluation": {
                      "mode": "COUNT_MATCHING_JOURNEYS",
                      "journeyPath": "stopPointsJourneyDetails[]",
                      "condition": {"type": "SERVICE_DATA_SCHEDULED_FIELD_MATCH", "all": []},
                      "threshold": {"operator": "GREATER_OR_EQUAL", "value": 2}
                    },
                    "outputPolicy": {"emit": "ON_MATCH", "includeCount": true, "includeMatchingJourneys": true},
                    "deduplicationKeyTemplate": "SERVICE_DATA_SCHEDULED:${alertId}:${queryWindowStart}:${conditionHash}"
                  },
                  "agentBlueprintPreview": {
                    "schemaVersion": "iia.agent.blueprint/v1",
                    "agentName": "ScheduledServiceDataSnapshotAlertAgent",
                    "description": "Periodically queries ServiceData stop point journeys and evaluates a snapshot condition.",
                    "triggerType": "SCHEDULE",
                    "requiredSources": ["SERVICE_DATA"],
                    "evaluationMode": "SCHEDULED_SNAPSHOT_MATCH",
                    "targetTypes": ["SERVICE_DATA_JOURNEY_AGGREGATE"],
                    "parameters": {"serviceDataQuery": {}, "snapshotEvaluation": {}, "outputPolicy": {}},
                    "stateRequirements": {"requiresState": false},
                    "output": {"type": "CANDIDATE_SUGGESTION"}
                  },
                  "warnings": [],
                  "safetyChecks": [
                    "No executable code generated.",
                    "No Agent Definition created.",
                    "No Agent Run created.",
                    "No Suggestion created."
                  ]
                }
                """;
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

    private record ScheduledVerifyConfiguration(
            String model,
            Double temperature,
            Integer maxOutputTokens) {
    }
}
