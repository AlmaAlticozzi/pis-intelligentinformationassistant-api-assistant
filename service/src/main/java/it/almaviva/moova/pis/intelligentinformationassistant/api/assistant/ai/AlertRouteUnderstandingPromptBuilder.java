package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AlertRouteUnderstandingPromptBuilder {

    @Inject
    AiConfiguration aiConfiguration;

    public LlmRequest build(AlertVerificationPromptData alert) {
        return build(alert, AlertRouteUnderstandingHints.empty());
    }

    public LlmRequest build(AlertVerificationPromptData alert, AlertRouteUnderstandingHints hints) {
        AiConfiguration.AlertVerify alertVerifyConfiguration = aiConfiguration.alertVerify();
        return new LlmRequest(
                AiUseCase.ALERT_ROUTE_UNDERSTANDING,
                systemPrompt(),
                userPrompt(alert, hints == null ? AlertRouteUnderstandingHints.empty() : hints),
                alertVerifyConfiguration.model(),
                alertVerifyConfiguration.temperature(),
                Math.min(alertVerifyConfiguration.maxOutputTokens(), 1200),
                alert.alertId());
    }

    private String systemPrompt() {
        return """
                You are the preliminary router for Alert Verify in the PIS Intelligent Information Assistant.
                The assistant verifies alert prompts for PIS / passenger information operations.

                Return only valid raw JSON. Do not use markdown. Do not produce a technicalSpecification.
                Do not invent data sources, tools, APIs, payload fields or operational capabilities.
                Reject prompts outside the currently supported routing domain.
                The routing result is an internal classification only; technical verification happens later.
                """;
    }

    private String userPrompt(AlertVerificationPromptData alert, AlertRouteUnderstandingHints hints) {
        return """
                Alert prompt:
                - alertId: %s
                - originalPrompt: %s
                - name: %s
                - description: %s

                %s

                Supported data domain:
                - dataDomains must always be an array.
                - SERVICE_DATA is currently the only supported data domain for routing to technical verification.
                - Currently the only technically supported data domain is SERVICE_DATA.
                - SERVICE_DATA means journey, stop point and passenger information operational data: journeys, stop
                  points, arrival, departure, delay, cancellation, platform, origin, destination, route calls,
                  exclusions, replacement and service category.

                Unsupported domains:
                - Reject weather, sports, generic Q&A, devices, audio messages, CMS content, broadcast history,
                  video playback, passenger profiles, external APIs and any other non-ServiceData domain.
                - Reject ServiceData-looking prompts that require uncontrolled attributes not listed above, such as
                  wifi availability, passenger profiles, passenger counts, vehicle color, onboard amenities or arbitrary
                  external metadata.
                - Do not classify weather, sports, generic Q&A, passenger comfort features not present in ServiceData,
                  wifi onboard, train composition/carriages, passenger profiles, ticketing, device state, CMS content,
                  audio messages or broadcast history as supported SERVICE_DATA for this phase.
                - If a prompt contains both supported and unsupported constraints, reject it. Do not partially accept the
                  supported part.

                Interpreter routing:
                - EVENT_INTERPRETER is for alerts triggered by individual ServiceData/Kafka domain events, such as
                  arrival, departure, delay event, cancellation, changed origin, changed destination, changed path,
                  platform changed, platform update or platform confirmed.
                - Known ServiceData event concepts include ARRIVING, ARRIVAL_DELAY, ARRIVED,
                  ARRIVAL_PLATFORM_CONFIRMED, ARRIVAL_PLATFORM_CHANGED, ARRIVAL_PLATFORM_UPDATE, DEPARTING,
                  DEPARTURE_DELAY, DEPARTED, DEPARTURE_PLATFORM_CONFIRMED, DEPARTURE_PLATFORM_CHANGED,
                  DEPARTURE_PLATFORM_UPDATE, CANCELLATION, ARRIVAL_CANCELLATION, DEPARTURE_CANCELLATION,
                  CHANGED_ORIGIN, CHANGED_DESTINATION, CHANGED_PATH and RELOAD_JOURNEY.
                - SCHEDULED_INTERPRETER is for alerts that require periodic snapshot queries to the ServiceData API,
                  usually involving every X minutes/hours, counts, reports, aggregate conditions, thresholds over
                  multiple journeys, boolean checks over the current situation, multiple monitored stop points evaluated
                  together, or a current/future visibility window over stop point journeys.
                - EVENT_INTERPRETER is only for a single ServiceData event emitted by Kafka.
                - A single Kafka ServiceData event represents one current operational event context, not a global snapshot
                  across multiple monitored stop points.
                - If the user asks whether there are N journeys, services, trains, buses or trams matching a condition at
                  one or more stop points, this is an aggregate snapshot condition and must be SCHEDULED_INTERPRETER.
                - If the user asks for a count, total, number, quantity, "how many", or any cardinality over journeys,
                  services, trains, buses or trams, this is SCHEDULED_INTERPRETER unless the wording is clearly about
                  one single event.
                - If the user mentions multiple monitored stop points to be evaluated together in one condition, this is
                  SCHEDULED_INTERPRETER.
                - If the prompt says that at stop point A and stop point B there are N arriving, departing, delayed or
                  cancelled journeys, classify as SCHEDULED_INTERPRETER.
                - The phrase "when there are two trains arriving at A and B" means a snapshot/cardinality condition, not
                  two independent ServiceData Kafka events.
                - Do not route such prompts to EVENT_INTERPRETER just because ARRIVING or DEPARTING exists as a
                  ServiceData event type.
                - The presence of event-like words such as arriving, departing, delayed or cancelled does not automatically
                  imply EVENT_INTERPRETER when the request also contains aggregation, count, threshold or multiple
                  monitored stop points.
                - Platform-change wording does not automatically imply EVENT_INTERPRETER. Classify the user meaning:
                  snapshot state, count or report prompts about journeys that have changed platform at a monitored
                  location are SCHEDULED_INTERPRETER; single emitted platform-change event prompts are EVENT_INTERPRETER.
                - If the user asks "are there trains with platform changes at X", "there are trains that changed
                  platform at X", "how many trains at X changed platform", or equivalent wording in any language, route
                  to SCHEDULED_INTERPRETER with SERVICE_DATA_API_SNAPSHOT.
                - If the user asks "notify me when a journey changes platform at X" or "when the platform of a journey
                  is changed at X", and there is no snapshot/count/report wording, route to EVENT_INTERPRETER.
                - Snapshot state signals such as "ci sono", "are there", "sono presenti", "presenti", "quanti",
                  "how many" or "number of" win over event-like platform-change wording.
                - The same snapshot-vs-event distinction applies to changed origin, changed destination, changed path,
                  extra journey, cancellation, partial cancellation, arrival/departure cancellation, total exclusion and
                  time-based exclusion.
                - If the user asks whether there are cancelled/changed/excluded journeys at a monitored location, or asks
                  how many such journeys exist, route to SCHEDULED_INTERPRETER with SERVICE_DATA_API_SNAPSHOT.
                - If the user asks for one emitted occurrence such as "notify me when a journey is cancelled at X" or
                  "when a journey changes destination at X", and there is no snapshot/count/report wording, route to
                  EVENT_INTERPRETER.
                - Do not classify a prompt as SCHEDULED_INTERPRETER just because it contains a number.
                - Numbers used as platform, binario, track, quay, banchina or marciapiede values are ServiceData field
                  constraints and can be evaluated by EVENT_INTERPRETER when the rest of the prompt describes a single
                  ServiceData event.
                - "A journey arrives or departs at stop X on platform N" is EVENT_INTERPRETER.
                - "There are N journeys arriving, departing or delayed at stop X" is SCHEDULED_INTERPRETER.
                - The distinction is whether N modifies the platform/track/quay or the number of journeys.
                - Do not rely on exact wording or one language. Classify the meaning semantically.
                - The routing step must not generate technicalSpecification.
                - The routing step must not produce field paths.
                - The routing step must not resolve locations.
                - The routing step only classifies the alert.

                Compact example:
                - Input meaning: "Notify me when at stop point A and stop point B there are two arriving journeys"
                - Expected route: SCHEDULED_INTERPRETER, SERVICE_DATA_API_SNAPSHOT, SNAPSHOT_CONDITION,
                  hasAggregation=true, hasCardinalityThreshold=true.

                Output rules:
                - Use decision ROUTED only when the prompt is in SERVICE_DATA and one interpreter can be selected.
                - Use decision REJECTED for unsupported or generic prompts.
                - Use dataDomains as an array, even when it contains only SERVICE_DATA.
                - For EVENT_INTERPRETER use accessMode KAFKA_EVENT, intentKind EVENT_OCCURRENCE, outputMode ON_MATCH,
                  requiresKafkaEvent true, requiresServiceDataApi false and requiresPolling false.
                - For SCHEDULED_INTERPRETER use accessMode SERVICE_DATA_API_SNAPSHOT, requiresPolling true,
                  requiresServiceDataApi true and requiresKafkaEvent false.
                - For scheduled periodic reports use intentKind SNAPSHOT_REPORT and outputMode EVERY_RUN_REPORT.
                - For scheduled aggregate/threshold conditions use intentKind SNAPSHOT_CONDITION and outputMode ON_MATCH.
                - For scheduled current-situation boolean checks use intentKind SNAPSHOT_BOOLEAN_CHECK and outputMode BOOLEAN_REPORT.
                - Set hasAggregation for counts, groups or evaluation across multiple journeys/stop points.
                - Set hasCardinalityThreshold for numeric thresholds such as at least N, more than N, fewer than N.
                - Set hasReportIntent when the user asks for a report, summary or count to be returned each run.

                Response JSON contract:
                {
                  "decision": "ROUTED",
                  "dataDomains": ["SERVICE_DATA"],
                  "primaryDataDomain": "SERVICE_DATA",
                  "interpreterType": "EVENT_INTERPRETER",
                  "accessMode": "KAFKA_EVENT",
                  "intentKind": "EVENT_OCCURRENCE",
                  "outputMode": "ON_MATCH",
                  "requiresPolling": false,
                  "requiresServiceDataApi": false,
                  "requiresKafkaEvent": true,
                  "hasAggregation": false,
                  "hasCardinalityThreshold": false,
                  "hasReportIntent": false,
                  "confidence": 0.86,
                  "summary": "The alert can be routed to event-based ServiceData verification.",
                  "rejectedReason": null,
                  "warnings": []
                }
                """.formatted(
                nullToEmpty(alert.alertId()),
                nullToEmpty(alert.prompt()),
                nullToEmpty(alert.name()),
                nullToEmpty(alert.description()),
                hints.compactPromptSection());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
