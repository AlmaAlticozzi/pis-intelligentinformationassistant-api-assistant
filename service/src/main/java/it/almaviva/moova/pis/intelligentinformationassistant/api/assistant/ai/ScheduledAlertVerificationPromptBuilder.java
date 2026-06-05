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

import java.util.List;

@ApplicationScoped
public class ScheduledAlertVerificationPromptBuilder {

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TemporalConfiguration temporalConfiguration;

    @ConfigProperty(name = "iia.alert-scheduled-verification.default-polling-frequency-seconds", defaultValue = "600")
    int defaultPollingFrequencySeconds = 600;

    @ConfigProperty(name = "iia.alert-scheduled-verification.default-lookahead-minutes", defaultValue = "480")
    int defaultLookaheadMinutes = 480;

    public LlmRequest build(ScheduledAlertVerificationPromptData data) {
        AiConfiguration.AlertVerify configuration = aiConfiguration == null ? null : aiConfiguration.alertVerify();
        return new LlmRequest(
                AiUseCase.ALERT_SCHEDULED_VERIFY,
                systemPrompt(),
                userPrompt(data),
                configuration == null ? "gpt-4.1-mini" : configuration.model(),
                configuration == null ? 0.1 : configuration.temperature(),
                configuration == null ? 5000 : configuration.maxOutputTokens(),
                data == null ? null : data.alertId());
    }

    private String systemPrompt() {
        return String.join("\n\n",
                missionAndSafetySection(),
                scheduledMvpContractSection(),
                outputJsonContractSection());
    }

    private String userPrompt(ScheduledAlertVerificationPromptData data) {
        return String.join("\n\n",
                alertInputSection(data),
                routeSection(data == null ? null : data.route()),
                scheduledLocationContextSection(data == null ? null : data.locationContext()),
                scheduledCapabilityCatalogSection(),
                semanticRulesSection(),
                locationMappingRulesSection(),
                journeyCorrelationRulesSection(),
                domainMappingRulesSection(),
                timeRulesSection(),
                rejectionRulesSection(),
                responseSkeletonSection());
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

    private String scheduledCapabilityCatalogSection() {
        return """
                ScheduledServiceDataCapabilityCatalog:
                Only fields/operators in this catalog may be used.
                The model must reject if a required user constraint cannot be mapped.
                Use requirementCoverage to explain mapping.

                %s
                """.formatted(ScheduledServiceDataCapabilityCatalog.compactPromptCatalog());
    }

    private String semanticRulesSection() {
        return """
                Report vs conditional notification:
                - Report: user asks "how many", "number of", "count", "list", "report", or "tell me every X minutes how many".
                  outputPolicy.emit = EVERY_RUN; snapshotEvaluation.mode = REPORT_COUNT or REPORT_MATCHING_JOURNEYS.
                  threshold should be absent/null unless the user explicitly asks to report threshold evaluation.
                  includeCount=true for count reports.
                - Conditional: user asks "notify me when/if there are at least/more than/exactly N journeys".
                  outputPolicy.emit = ON_MATCH; snapshotEvaluation.mode = COUNT_MATCHING_JOURNEYS or BOOLEAN_EXISTS.
                  threshold is required when a numeric threshold is requested.
                - Boolean check: user asks whether there is at least one matching journey.
                  outputPolicy.emit = ON_MATCH or ON_BOOLEAN_RESULT according to output mode.
                  snapshotEvaluation.mode = BOOLEAN_EXISTS or COUNT_MATCHING_JOURNEYS with threshold >= 1.
                """;
    }

    private String locationMappingRulesSection() {
        return """
                Monitored stop points vs filter locations:
                - serviceDataQuery.stopPoints must come only from context.serviceDataApiStopPoints.
                - If monitoringScope=EXPLICIT_STOP_POINTS, stopPoints must be non-empty.
                - If monitoringScope=ALL_KNOWN_STOP_POINTS, stopPoints may be empty and requiresAllKnownStopPoints=true.
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
                - Do not classify as scheduled only because a number exists; distinguish platform values from journey counts.
                - Simple departure platform equality defaults to timetabledDeparturePlatform.dsc EQUAL_PLATFORM unless actual/current/effective/real is explicit.
                - Simple arrival platform equality defaults to timetabledArrivalPlatform.dsc EQUAL_PLATFORM unless actual/current/effective/real is explicit.
                - Platform change uses changes CONTAINS PLATFORM_CHANGED when direction is unspecified.
                - Departure platform change may use departureStatuses[].status CONTAINS DEPARTURE_PLATFORM_CHANGED or timetabledDeparturePlatform.dsc PLATFORM_NOT_EQUALS_FIELD actualDeparturePlatform.platform.dsc.
                - Arrival platform change may use arrivalStatuses[].status CONTAINS ARRIVAL_PLATFORM_CHANGED or timetabledArrivalPlatform.dsc PLATFORM_NOT_EQUALS_FIELD actualArrivalPlatform.platform.dsc.

                Origin/destination/route/cancelled calls:
                - monitored X, origin Y -> serviceDataQuery.stopPoints=[X id], condition on callStart.stopPoint.id=Y id.
                - monitored X, planned origin Y -> condition on timetabledCallStart.stopPoint.id.
                - monitored X, destination Y -> condition on callEnd.stopPoint.id.
                - monitored X, planned destination Y -> condition on timetabledCallEnd.stopPoint.id.
                - monitored X, passes through Y -> nested anyElement nextCalls[] stopPoint.id=Y.
                - monitored X, transit through Y -> nested anyElement nextTransitCalls[] stopPoint.id=Y.
                - monitored X, cancelled/suppressed stop Y -> nested anyElement nextCancelledCalls[] stopPoint.id=Y.

                Changes, cancellations, exclusions:
                - Use changes CONTAINS CANCELLATION, PARTIALLY_CANCELLATION, CHANGED_ORIGIN, CHANGED_DESTINATION, CHANGED_PATH, PLATFORM_CHANGED.
                - Use exclusion.totalExclusion EQUALS true or exclusion.timeBasedExclusion EQUALS true.
                - Use arrivalStatuses[].status CONTAINS ARRIVAL_CANCELLATION.
                - Use departureStatuses[].status CONTAINS DEPARTURE_CANCELLATION.
                - If the user asks "arrival cancellation but not departure cancellation" and the catalog lacks a NOT_CONTAINS operator, reject rather than inventing an operator.

                Replacement:
                - "replacement journey/service" -> isReplacementOf NOT_EMPTY if allowed by catalog.
                - "has replacement object" -> replacement NOT_NULL/EXISTS if allowed.
                - "replacement stop X" -> nested anyElement replacement.stopPointReplacements[] stopPointId.id.
                - "replacement type departure/arrival" -> replacementType IN/EQUALS DEPARTURE/ARRIVAL/ARRIVALDEPARTURE.
                - Do not map replacement source route start/end to stop point ids if available fields are timestamps only.
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
                - "departures between 10:00 and 12:00" -> condition on departure timestamp field using LOCAL_TIME_BETWEEN.
                - Use the coherent timestamp: callStart.departureTime, timetabledCallStart.departureTime, callEnd.arrivalTime, timetabledCallEnd.arrivalTime, nextCalls[].departureTime/arrivalTime, nextTransitCalls[].passingTime.
                - Use Europe/Rome unless explicit timezone is supplied.

                Reject:
                - historical trends across multiple runs
                - prediction beyond the ServiceData API visibility window
                - absence over time requiring state/history
                """;
    }

    private String rejectionRulesSection() {
        return """
                Rejection policy:
                - Verify only if every required user constraint is mappable with ScheduledServiceDataCapabilityCatalog.
                - If a prompt contains both supported and unsupported constraints, reject it; do not partially accept.
                - Reject weather, sports, devices, audio messages, CMS content, display/broadcast history, passenger count, wifi, unsupported composition/carriage constraints, predictions, external APIs, and history/state requirements.
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
}
