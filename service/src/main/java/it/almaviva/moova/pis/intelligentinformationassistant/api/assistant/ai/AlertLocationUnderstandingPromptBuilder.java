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
                - Functional words without a proper stop/place name are not locations: "in arrivo", "in partenza", "arrivo", "partenza", "destinazione", "destino", "origine", "transito", "arrival", "departure", "destination", "origin", "transit".
                - Delay direction phrases are non-location constraints, not stop names: "ritardo in arrivo", "ritardo di arrivo", "arrival delay", "delay on arrival" mean arrival delay; "ritardo in partenza", "ritardo di partenza", "departure delay" mean departure delay.
                - Do not infer MAIN_EVENT_PHASE from delay direction alone. "ritardo in arrivo" describes arrivalDelay, not necessarily a current ARRIVING event.
                - "arriva a destinazione", "arriva a destino", "at destination" and "at final destination" without a named place are PASSING_TYPE constraints, not DESTINATION_LOCATION.
                - "parte dall'origine", "parte da una localita di origine" without a named place are PASSING_TYPE ORIGIN constraints, not ORIGIN_LOCATION.
                - "passa in transito" without a named place is PASSING_TYPE TRANSIT, not TRANSIT_LOCATION.
                - Distinguish the main event location, journey origin, journey destination, route or next-call locations, transit locations, cancelled/suppressed call locations, replacement locations, and generic locations when the role is uncertain.
                - A preposition such as Italian "da" or English "from" is not enough to classify a location as journey origin.
                - MAIN_EVENT_LOCATION means the stop/location of the current operational event being monitored.
                - In a departure main event, wording such as "e in partenza da X", "sta partendo da X", "parte da X", "partenza da X", "is departing from X", "about to depart from X" and "departs from X" normally means X is MAIN_EVENT_LOCATION, not ORIGIN_LOCATION.
                - ORIGIN_LOCATION requires explicit journey-origin wording such as "ha origine a X", "origine X", "localita di origine X", "corsa con origine X", "journey origin X" or "originating from X".
                - In an arrival main event, wording such as "arriva a X", "e in arrivo a X", "sta arrivando a X", "arrival at X", "is arriving at X" and "arrives at X" normally means X is MAIN_EVENT_LOCATION, not DESTINATION_LOCATION.
                - DESTINATION_LOCATION requires explicit journey-destination wording such as "destinazione X", "destino X", "ha destinazione X", "corsa con destinazione X" or "destination X".
                - If a prompt contains both a current event location and a route location, keep them as separate locations with distinct roles.
                - Example pattern: "a service is departing from X and will pass through Y" -> X MAIN_EVENT_LOCATION, Y ROUTE_OR_NEXT_CALL_LOCATION.
                - If a prompt contains current stop, journey origin and journey destination, return three separate constraints.
                - Example pattern: "a service is arriving at X, has origin Y and destination Z" -> X MAIN_EVENT_LOCATION, Y ORIGIN_LOCATION, Z DESTINATION_LOCATION.
                - ROUTE_OR_NEXT_CALL_LOCATION is for future route/call wording such as "passera da X", "passa da X" when it is not the current event stop, "via X", "fermera a X", "will pass through X" and "will call at X".
                - TRANSIT_LOCATION is only for explicit transit wording such as "in transito da X", "transitera da X" or "passing through X as transit".
                - CANCELLED_CALL_LOCATION is for cancelled/suppressed stop wording such as "soppressa a X", "cancellata a X" or "fermata cancellata a X" when X is not the current event location.
                - Infer main event phase from wording: progressive departure ("e in partenza", "sta partendo", "in partenza", "is departing", "about to depart") -> PROGRESSIVE/DEPARTING.
                - Infer main event phase from wording: completed departure ("parte", "e partita", "ha lasciato", "departs", "has departed") -> COMPLETED/DEPARTED.
                - Infer main event phase from wording: progressive arrival ("e in arrivo", "sta arrivando", "in arrivo", "is arriving", "about to arrive") -> PROGRESSIVE/ARRIVING.
                - Infer main event phase from wording: completed arrival ("arriva", "e arrivata", "arrives", "has arrived") -> COMPLETED/ARRIVED.
                - If the event phase is truly ambiguous, keep it ambiguous; downstream verification may use coherent CONTAINS_ANY.
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
                - EVENT_LOCATION
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
                - PASSING_TYPE
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

                Generic examples:
                - Prompt pattern: "una corsa e in partenza da X e passera da Y"
                  mainEvent.eventIntent: DEPARTURE
                  X: role MAIN_EVENT_LOCATION, relationToMainEvent EVENT_LOCATION, requiredCoverage true, polarity INCLUDE
                  Y: role ROUTE_OR_NEXT_CALL_LOCATION, relationToMainEvent FUTURE_ROUTE_CONSTRAINT, requiredCoverage true, polarity INCLUDE
                - Prompt pattern: "una corsa parte da X e passera da Y"
                  mainEvent.eventIntent: DEPARTURE
                  X: role MAIN_EVENT_LOCATION, relationToMainEvent EVENT_LOCATION
                  Y: role ROUTE_OR_NEXT_CALL_LOCATION, relationToMainEvent FUTURE_ROUTE_CONSTRAINT
                - Prompt pattern: "una corsa ha origine a X e passera da Y"
                  X: role ORIGIN_LOCATION, relationToMainEvent ORIGIN_CONSTRAINT
                  Y: role ROUTE_OR_NEXT_CALL_LOCATION, relationToMainEvent FUTURE_ROUTE_CONSTRAINT
                - Prompt pattern: "una corsa e in arrivo a X e ha destinazione Z"
                  mainEvent.eventIntent: ARRIVAL
                  X: role MAIN_EVENT_LOCATION, relationToMainEvent EVENT_LOCATION
                  Z: role DESTINATION_LOCATION, relationToMainEvent DESTINATION_CONSTRAINT
                - Prompt pattern: "una corsa ha origine a X e destinazione Z"
                  X: role ORIGIN_LOCATION, relationToMainEvent ORIGIN_CONSTRAINT
                  Z: role DESTINATION_LOCATION, relationToMainEvent DESTINATION_CONSTRAINT
                  Do not invent a MAIN_EVENT_LOCATION when no current event location is present.
                """.formatted(escapeForPrompt(prompt));
    }

    private String escapeForPrompt(String prompt) {
        return prompt == null ? "" : prompt.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
