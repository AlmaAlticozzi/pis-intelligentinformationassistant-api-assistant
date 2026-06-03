package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertLocationUnderstandingPromptBuilder {

    private static final String DEFAULT_MODEL = "gpt-4.1-mini";
    private static final double DEFAULT_TEMPERATURE = 0.0;
    private static final int DEFAULT_MAX_OUTPUT_TOKENS = 1800;

    public LlmRequest build(String prompt, String correlationId) {
        return new LlmRequest(
                AiUseCase.ALERT_LOCATION_UNDERSTANDING,
                systemPrompt(),
                userPrompt(prompt),
                DEFAULT_MODEL,
                DEFAULT_TEMPERATURE,
                DEFAULT_MAX_OUTPUT_TOKENS,
                correlationId);
    }

    private String systemPrompt() {
        return """
                You analyze user prompts for operational PIS/ServiceData Alerts.
                Your task is only Location Understanding: identify operational locations and their semantic roles.
                Do not generate technicalSpecification.
                Do not generate Agent definitions, code, queries, or executable instructions.
                Return only valid raw JSON. Do not use markdown, prose, comments, or code fences.
                """;
    }

    private String userPrompt(String prompt) {
        return """
                Analyze this Alert prompt semantically, before any point resolution:
                "%s"

                Conceptual rules:
                - The prompt may be Italian, English, or another language. Work cross-language and multilingual.
                - Identify operational locations even when phrasing varies; do not rely on a closed list of verbs.
                - Do not invent locations. Return only locations explicitly mentioned by the user.
                - Platform/binario/track/quay/banchina/marciapiede constraints are not locations.
                - Put platform/binario/track/quay/banchina/marciapiede and similar constraints in nonLocationConstraints.
                - Distinguish the main event location, journey origin, journey destination, route or next-call locations, transit locations, cancelled/suppressed call locations, replacement locations, and generic locations when the role is uncertain.
                - If a location is an operational user constraint, requiredCoverage must be true.
                - Negated or excluded locations are still mandatory user constraints: use requiredCoverage=true and polarity=EXCLUDE.
                - Do not set requiredCoverage=false just because polarity=EXCLUDE.
                - Use requiredCoverage=false only for non-operational, ambiguous, descriptive, or non-binding mentions.
                - If a location is negated or excluded, polarity must be EXCLUDE. Otherwise use INCLUDE.
                - If several locations are alternatives, use the same logicalGroup value.
                - Provide confidence for mainEvent and for every location, as a number from 0.0 to 1.0.
                - If there are no locations, return hasLocations=false and locations=[].

                Allowed location roles:
                - MAIN_EVENT_LOCATION
                - ORIGIN_LOCATION
                - DESTINATION_LOCATION
                - ROUTE_OR_NEXT_CALL_LOCATION
                - TRANSIT_LOCATION
                - CANCELLED_CALL_LOCATION
                - REPLACEMENT_LOCATION
                - GENERIC_LOCATION

                Allowed relationToMainEvent values:
                - EVENT_STOP_POINT
                - ORIGIN_CONSTRAINT
                - DESTINATION_CONSTRAINT
                - FUTURE_ROUTE_CONSTRAINT
                - TRANSIT_CONSTRAINT
                - CANCELLED_CALL_CONSTRAINT
                - REPLACEMENT_CONSTRAINT
                - GENERIC_ROUTE_CONSTRAINT
                - UNKNOWN

                Allowed mainEvent.eventIntent values:
                - DEPARTURE
                - ARRIVAL
                - DEPARTURE_OR_ARRIVAL
                - DELAY
                - CANCELLATION
                - PLATFORM_CHANGE
                - ROUTE_TRANSIT
                - UNKNOWN

                Allowed polarity values:
                - INCLUDE
                - EXCLUDE

                Allowed nonLocationConstraints.type values:
                - PLATFORM
                - VEHICLE_JOURNEY
                - LINE
                - DELAY
                - TEMPORAL
                - UNKNOWN

                JSON response shape:
                {
                  "hasLocations": true,
                  "language": "it",
                  "mainEvent": {
                    "eventIntent": "DEPARTURE",
                    "confidence": 0.92
                  },
                  "locations": [
                    {
                      "rawText": "Garibaldi",
                      "normalizedText": "Garibaldi",
                      "role": "MAIN_EVENT_LOCATION",
                      "relationToMainEvent": "EVENT_STOP_POINT",
                      "requiredCoverage": true,
                      "polarity": "INCLUDE",
                      "logicalGroup": "G1",
                      "confidence": 0.94
                    }
                  ],
                  "nonLocationConstraints": [
                    {
                      "type": "PLATFORM",
                      "rawText": "binario 1"
                    }
                  ],
                  "warnings": []
                }
                """.formatted(escapeForPrompt(prompt));
    }

    private String escapeForPrompt(String prompt) {
        return prompt == null ? "" : prompt.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
