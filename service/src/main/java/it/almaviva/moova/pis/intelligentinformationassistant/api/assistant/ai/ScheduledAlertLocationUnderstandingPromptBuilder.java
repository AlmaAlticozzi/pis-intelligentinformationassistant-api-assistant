package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScheduledAlertLocationUnderstandingPromptBuilder {

    private static final String DEFAULT_MODEL = "gpt-4.1-mini";
    private static final double DEFAULT_TEMPERATURE = 0.0;
    private static final int DEFAULT_MAX_OUTPUT_TOKENS = 1800;

    public LlmRequest build(String prompt, String correlationId) {
        return build(prompt, correlationId, ScheduledAlertLocationUnderstandingHints.empty());
    }

    public LlmRequest build(String prompt, String correlationId, ScheduledAlertLocationUnderstandingHints hints) {
        ScheduledAlertLocationUnderstandingHints safeHints =
                hints == null ? ScheduledAlertLocationUnderstandingHints.empty() : hints;
        return new LlmRequest(
                AiUseCase.ALERT_SCHEDULED_LOCATION_UNDERSTANDING,
                systemPrompt(),
                userPrompt(prompt, safeHints),
                DEFAULT_MODEL,
                DEFAULT_TEMPERATURE,
                DEFAULT_MAX_OUTPUT_TOKENS,
                correlationId);
    }

    private String systemPrompt() {
        return """
                You analyze user prompts for scheduled ServiceData Alert location understanding.
                The alert was already routed as SERVICE_DATA + SCHEDULED_INTERPRETER.
                Your task is only scheduled snapshot location classification.
                Return only valid raw JSON. Do not use markdown, prose, comments, or code fences.
                Do not resolve stop point ids. Do not invent stop point ids.
                Do not generate technicalSpecification or agentBlueprintPreview.
                Do not call external services.
                """;
    }

    private String userPrompt(String prompt, ScheduledAlertLocationUnderstandingHints hints) {
        return """
                Analyze this scheduled ServiceData Alert prompt before any point resolution:
                "%s"

                %s

                Context:
                - We are analyzing a user alert already routed as SERVICE_DATA + SCHEDULED_INTERPRETER.
                - SCHEDULED_INTERPRETER means the future Agent will periodically query ServiceData API snapshot data.
                - The relevant API will be POST /v2/stoppointjourneys.
                - The API requires monitored stop point ids in the request body.
                - Distinguish monitored stop points from filter/control stop points.
                - Work semantically and multilingual. Do not rely on exact Italian wording.

                Monitored stop points:
                - MONITORED_STOP_POINT is a stop point that must be queried through ServiceData API POST /v2/stoppointjourneys.
                - It will be used in body.stopPoints[] after resolution.
                - Meanings such as "at stop X tell me how many journeys", "for location X", "in X are there delayed
                  trains", "between/among X and Y are there N arriving journeys", and "check X and Y" identify monitored
                  stop points.
                - A location governed by phrases equivalent to "at/in/for location X", when the user asks about current
                  situation, counts, reports or conditions at that place, is MONITORED_STOP_POINT.
                - "all locations", "all stop points", "all known locations" and equivalent wording means
                  monitoringScope=ALL_KNOWN_STOP_POINTS and no explicit monitored location is required.

                Filter/control stop points:
                - FILTER_CURRENT_STOP_POINT is a filter on the current stop point inside returned journeys.
                - A location governed by "origin/from where the journey starts" is FILTER_ORIGIN_STOP_POINT, unless
                  scheduled/planned/timetabled is explicit, then FILTER_TIMETABLED_ORIGIN_STOP_POINT.
                - A location governed by "destination/to where the journey ends" is FILTER_DESTINATION_STOP_POINT, unless
                  scheduled/planned/timetabled is explicit, then FILTER_TIMETABLED_DESTINATION_STOP_POINT.
                - A location governed by "passes through/calls at/via" is FILTER_ROUTE_STOP_POINT.
                - A location governed by "transits through/non-stopping transit" is FILTER_TRANSIT_STOP_POINT.
                - A location governed by "cancelled/suppressed/skipped stop" is FILTER_CANCELLED_CALL_STOP_POINT.
                - A stop point governed by "suppressed stop", "cancelled stop", "skipped stop", "cancelled call",
                  "fermata soppressa", "fermata cancellata", "salta la fermata" or equivalent wording is
                  FILTER_CANCELLED_CALL_STOP_POINT.
                - It is a filter/control location, not the monitored stop point, unless the prompt also says "at/in/for
                  location X" for monitoring.
                - Example meaning: "at monitored location X, journeys with suppressed stop Y" -> X is
                  MONITORED_STOP_POINT and Y is FILTER_CANCELLED_CALL_STOP_POINT.
                - Do not treat "fermata soppressa" itself as a location.
                - A location governed by "replacement stop / fermata sostitutiva / stop sostitutivo" is FILTER_REPLACEMENT_STOP_POINT.
                - It is a filter/control location, not the monitored stop point, unless the prompt also explicitly
                  says "at/in/for location X" for monitoring.
                - Example: "a Garibaldi FS ... fermata sostitutiva Bovisa" means Garibaldi FS is
                  MONITORED_STOP_POINT and Bovisa is FILTER_REPLACEMENT_STOP_POINT.
                - "corse/servizi sostitutivi a X" means X is MONITORED_STOP_POINT and replacement is a
                  non-location/capability constraint.
                - Replacement source route start/end means FILTER_REPLACEMENT_SOURCE_START_STOP_POINT or
                  FILTER_REPLACEMENT_SOURCE_END_STOP_POINT.
                - A location governed by exclusion, "not destination X" or equivalent negation must keep polarity=EXCLUDE
                  with a coherent filter role.

                Future mapping semantics:
                - FILTER_ORIGIN_STOP_POINT maps conceptually to callStart.stopPoint.
                - FILTER_TIMETABLED_ORIGIN_STOP_POINT maps conceptually to timetabledCallStart.stopPoint.
                - FILTER_DESTINATION_STOP_POINT maps conceptually to callEnd.stopPoint.
                - FILTER_TIMETABLED_DESTINATION_STOP_POINT maps conceptually to timetabledCallEnd.stopPoint.
                - FILTER_ROUTE_STOP_POINT maps conceptually to nextCalls[].stopPoint.
                - FILTER_TRANSIT_STOP_POINT maps conceptually to nextTransitCalls[].stopPoint.
                - FILTER_CANCELLED_CALL_STOP_POINT maps conceptually to nextCancelledCalls[].stopPoint.
                - FILTER_REPLACEMENT_STOP_POINT maps conceptually to replacement.stopPointReplacements[].

                Platform/binario is not a location:
                - Words like binario, platform, track, quay, banchina or marciapiede followed by numbers or properties
                  must be nonLocationConstraints, not locations.
                - "binario 3" means nonLocationConstraint type PLATFORM.
                - "platform 5" means nonLocationConstraint type PLATFORM.
                - "platform greater than 2" means nonLocationConstraint type PLATFORM_NUMERIC.
                - Do not extract "3" or "platform 3" as a stop point.

                Allowed monitoringScope values:
                - EXPLICIT_STOP_POINTS
                - ALL_KNOWN_STOP_POINTS
                - UNSPECIFIED

                Allowed location roles:
                - MONITORED_STOP_POINT
                - FILTER_CURRENT_STOP_POINT
                - FILTER_ORIGIN_STOP_POINT
                - FILTER_TIMETABLED_ORIGIN_STOP_POINT
                - FILTER_DESTINATION_STOP_POINT
                - FILTER_TIMETABLED_DESTINATION_STOP_POINT
                - FILTER_ROUTE_STOP_POINT
                - FILTER_TRANSIT_STOP_POINT
                - FILTER_CANCELLED_CALL_STOP_POINT
                - FILTER_REPLACEMENT_STOP_POINT
                - FILTER_REPLACEMENT_SOURCE_START_STOP_POINT
                - FILTER_REPLACEMENT_SOURCE_END_STOP_POINT
                - EXCLUDED_STOP_POINT
                - UNKNOWN_LOCATION_ROLE

                Allowed relationToSnapshot values:
                - SERVICE_DATA_API_QUERY_STOP_POINT
                - CURRENT_STOP_POINT_FILTER
                - ORIGIN_FILTER
                - TIMETABLED_ORIGIN_FILTER
                - DESTINATION_FILTER
                - TIMETABLED_DESTINATION_FILTER
                - ROUTE_FILTER
                - TRANSIT_FILTER
                - CANCELLED_CALL_FILTER
                - REPLACEMENT_FILTER
                - REPLACEMENT_SOURCE_START_FILTER
                - REPLACEMENT_SOURCE_END_FILTER
                - EXCLUSION_FILTER
                - UNKNOWN
                - UNSUPPORTED_CAPABILITY

                Allowed polarity values:
                - INCLUDE
                - EXCLUDE

                Allowed nonLocationConstraints.type values:
                - PLATFORM
                - PLATFORM_NUMERIC
                - VEHICLE_JOURNEY
                - LINE
                - DELAY
                - TEMPORAL
                - COUNT
                - THRESHOLD
                - UNKNOWN

                Output rules:
                - Return JSON only. No markdown.
                - Do not resolve stop point ids. Do not invent stop point ids.
                - Do not generate technicalSpecification.
                - Do not generate agentBlueprintPreview.
                - Return only locations explicitly mentioned by the user.
                - For MONITORED_STOP_POINT set requiredForApiQuery=true and relationToSnapshot=SERVICE_DATA_API_QUERY_STOP_POINT.
                - For filter/control stop points set requiredForApiQuery=false.
                - If a user constraint must be represented later, set requiredCoverage=true.
                - If wording is negated or excluded, set polarity=EXCLUDE.
                - Provide confidence for every location as a number from 0.0 to 1.0.

                JSON response shape:
                {
                  "hasLocations": true,
                  "language": "it",
                  "monitoringScope": "EXPLICIT_STOP_POINTS",
                  "locations": [
                    {
                      "rawText": "Garibaldi FS",
                      "normalizedText": "Garibaldi FS",
                      "role": "MONITORED_STOP_POINT",
                      "relationToSnapshot": "SERVICE_DATA_API_QUERY_STOP_POINT",
                      "requiredForApiQuery": true,
                      "requiredCoverage": true,
                      "polarity": "INCLUDE",
                      "logicalGroup": "G1",
                      "confidence": 0.95
                    }
                  ],
                  "nonLocationConstraints": [
                    {
                      "type": "PLATFORM",
                      "rawText": "binario 3"
                    }
                  ],
                  "warnings": []
                }
                """.formatted(escapeForPrompt(prompt), hints.compactPromptSection());
    }

    private String escapeForPrompt(String prompt) {
        return prompt == null ? "" : prompt.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
