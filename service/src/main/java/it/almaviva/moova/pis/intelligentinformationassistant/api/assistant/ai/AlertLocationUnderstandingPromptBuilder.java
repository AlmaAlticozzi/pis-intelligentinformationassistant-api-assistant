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
                - Journey references are non-location constraints of type JOURNEY_REFERENCE.
                - JOURNEY_REFERENCE.kind values: JOURNEY_NAME for explicit journey name/number; LINE for explicit line; SERVICE_CATEGORY for explicit service category; TRANSPORT_OPERATOR for explicit operator; UNQUALIFIED_DESCRIPTOR for a descriptor attached to the journey without an explicit qualifier.
                - Examples: "journey 125"/"corsa 125" -> JOURNEY_NAME; "line M2"/"linea M2" -> LINE; "service category Intercity" -> SERVICE_CATEGORY; "operator ATM"/"operated by ATM" -> TRANSPORT_OPERATOR; "an M2 journey"/"una corsa M2" -> UNQUALIFIED_DESCRIPTOR.
                - Generic entity nouns identify the monitored object and are not filters. Do not emit a JOURNEY_REFERENCE for a bare train, journey, bus, corsa, treno, autobus or equivalent entity mention.
                - Emit UNQUALIFIED_DESCRIPTOR only when one or more actual attribute values are attached to that entity. The descriptor value must be semantically distinct from the generic entity head.
                - Event/state expressions are not descriptor values. Articles, determiners, auxiliaries, prepositions and conjunctions are not descriptor values.
                - Examples: "a journey is arriving" -> no JOURNEY_REFERENCE; "una corsa e partita" -> no JOURNEY_REFERENCE; "an M2 journey" -> UNQUALIFIED_DESCRIPTOR values=[M2]; "una corsa M2" -> UNQUALIFIED_DESCRIPTOR values=[M2]; "journey 125" -> JOURNEY_NAME values=[125]; "line M2" -> LINE values=[M2]; "operated by ATM" -> TRANSPORT_OPERATOR values=[ATM].
                - For JOURNEY_REFERENCE include kind, rawText, entityHeadText, descriptorValueTexts, normalizedValue, normalizedValues, valueCombination, requiredCoverage=true and confidence.
                - Use normalizedValues for the semantic values in source order. One value means valueCombination=SINGLE; alternatives connected as OR/ANY mean valueCombination=ANY. Preserve ANY as alternatives, not mandatory independent constraints.
                - Functional words without a proper stop/place name are not locations: "in arrivo", "in partenza", "arrivo", "partenza", "destinazione", "destino", "origine", "transito", "arrival", "departure", "destination", "origin", "transit".
                - Universal/all/any location wording is not a station, stop, or location mention: "qualsiasi localita", "qualunque localita", "ogni localita", "tutte le localita", "qualsiasi fermata", "ovunque", "any location", "all locations", "every location", "anywhere", "any stop" and "all stops" mean no location predicate and no required location coverage.
                - Delay direction phrases are non-location constraints, not stop names: "ritardo in arrivo", "ritardo di arrivo", "arrival delay", "delay on arrival" mean DELAY_DIRECTION=ARRIVAL and DELAY_EVENT_TYPE=ARRIVAL_DELAY; "ritardo in partenza", "ritardo alla partenza", "ritardo di partenza", "departure delay" mean DELAY_DIRECTION=DEPARTURE and DELAY_EVENT_TYPE=DEPARTURE_DELAY.
                - Generic delay wording without arrival/departure direction, such as "ha piu di N minuti di ritardo", "e in ritardo di N minuti", "delay greater than N" or "train delay over N minutes", means DELAY_DIRECTION=GENERIC and DELAY_EVENT_TYPE=BOTH.
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
                - In delay prompts with route/transit wording, locations after "passera/via/will pass through" remain ROUTE_OR_NEXT_CALL_LOCATION and locations after explicit transit wording remain TRANSIT_LOCATION. Do not invent MAIN_EVENT_LOCATION from delay direction.
                - If a prompt contains current stop, journey origin and journey destination, return three separate constraints.
                - Example pattern: "a service is arriving at X, has origin Y and destination Z" -> X MAIN_EVENT_LOCATION, Y ORIGIN_LOCATION, Z DESTINATION_LOCATION.
                - ROUTE_OR_NEXT_CALL_LOCATION is for future route/call wording such as "passera da X", "passa da X" when it is not the current event stop, "via X", "fermera a X", "will pass through X" and "will call at X".
                - Route-only prompts such as "will pass through X", "will pass through X and then Y", "via X" or "passera da X" use ROUTE_OR_NEXT_CALL_LOCATION or TRANSIT_LOCATION locations and should not create an unmappable required main event.
                - TRANSIT_LOCATION is only for explicit transit wording such as "in transito da X", "transitera da X" or "passing through X as transit".
                - Cancellation/suppression of a journey at a location means the named location is the monitored/current event location: use MAIN_EVENT_LOCATION with relation EVENT_STOP_POINT.
                - Cancellation/suppression of a stop/call inside the journey means the named location is a cancelled/skipped call constraint: use CANCELLED_CALL_LOCATION with relation CANCELLED_CALL_CONSTRAINT.
                - Do not classify "cancelled journey/train/service at X", "suppressed journey/train/service at X", or equivalent cross-language wording as CANCELLED_CALL_LOCATION. Those are journey cancellation at the monitored stop X.
                - Use CANCELLED_CALL_LOCATION only when the wording explicitly says the stop/call itself is cancelled, suppressed or skipped inside the journey, such as "cancelled stop X", "skipped stop X", "cancelled call X", "stop X is cancelled/skipped", "fermata X soppressa", "fermata soppressa a X", "call soppressa a X" or "next cancelled call at X".
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

                Functional words are not locations:
                - Do not emit a location object for pure functional transport words such as arrival, departure, origin, destination, transit, stop, call, journey, train, cancellation, suppression or platform.
                - In Italian, do not emit locations for "in arrivo", "arrivo", "in partenza", "partenza", "origine", "destinazione", "destino", "transito", "fermata", "corsa", "treno", "soppressa" or "cancellata".
                - These words must become mainEvent or nonLocationConstraints, never unresolved location fallbacks.
                - A location requires a proper stop/station/place name, such as "Bologna", "Lecco", "Genova P.P." or "Rho Fieramilano".
                - If a phrase contains both a functional word and a station name, emit only the station name as location and use the functional word to set role, event intent, phase, direction or state filter.

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
                - JOURNEY_REFERENCE
                - LINE
                - DELAY
                - PASSING_TYPE
                - DELAY_DIRECTION
                - DELAY_EVENT_TYPE
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
                    },
                    {
                      "type": "JOURNEY_REFERENCE",
                      "kind": "UNQUALIFIED_DESCRIPTOR",
                      "rawText": "una corsa M2 o M3",
                      "entityHeadText": "corsa",
                      "descriptorValueTexts": ["M2", "M3"],
                      "normalizedValue": "M2",
                      "normalizedValues": ["M2", "M3"],
                      "valueCombination": "ANY",
                      "requiredCoverage": true,
                      "confidence": 0.92
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
                - Prompt pattern: "a train is cancelled at X" or "there is a cancelled journey at X"
                  mainEvent.eventIntent: CANCELLATION
                  X: role MAIN_EVENT_LOCATION, relationToMainEvent EVENT_STOP_POINT
                - Prompt pattern: "a journey has stop X cancelled/skipped" or "next cancelled call at X"
                  mainEvent.eventIntent: CANCELLATION
                  X: role CANCELLED_CALL_LOCATION, relationToMainEvent CANCELLED_CALL_CONSTRAINT
                """.formatted(escapeForPrompt(prompt));
    }

    private String escapeForPrompt(String prompt) {
        return prompt == null ? "" : prompt.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
