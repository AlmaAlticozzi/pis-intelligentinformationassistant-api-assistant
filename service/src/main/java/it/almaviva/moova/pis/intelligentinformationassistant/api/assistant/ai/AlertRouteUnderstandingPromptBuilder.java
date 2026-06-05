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
        AiConfiguration.AlertVerify alertVerifyConfiguration = aiConfiguration.alertVerify();
        return new LlmRequest(
                AiUseCase.ALERT_ROUTE_UNDERSTANDING,
                systemPrompt(),
                userPrompt(alert),
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

    private String userPrompt(AlertVerificationPromptData alert) {
        return """
                Alert prompt:
                - alertId: %s
                - originalPrompt: %s
                - name: %s
                - description: %s

                Supported data domain:
                - SERVICE_DATA is currently the only supported data domain for routing to technical verification.
                - SERVICE_DATA includes public transport journey status information: journeys, stop points, origin,
                  destination, route calls, delays, cancellations, platform, statuses, exclusions, replacement
                  information and similar passenger information operations.

                Unsupported domains:
                - Reject weather, sports, generic Q&A, devices, audio messages, CMS content, broadcast history,
                  video playback, passenger profiles, external APIs and any other non-ServiceData domain.
                - Reject ServiceData-looking prompts that require uncontrolled attributes not listed above, such as
                  wifi availability, passenger profiles, passenger counts, vehicle color, onboard amenities or arbitrary
                  external metadata.

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
                - Do not rely on exact wording or one language. Classify the meaning semantically.

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
                nullToEmpty(alert.description()));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
