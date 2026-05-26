package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.TemporalConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AlertVerificationPromptBuilder {

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    TemporalConfiguration temporalConfiguration;

    @ConfigProperty(name = "iia.alert-verification.fallback-on-invalid-llm", defaultValue = "false")
    boolean fallbackOnInvalidLlm;

    public LlmRequest build(AlertVerificationPromptData alert) {
        AiConfiguration.AlertVerify alertVerifyConfiguration = aiConfiguration.alertVerify();
        String defaultTemporalZone = defaultTemporalZone();
        String model = alertVerifyConfiguration.model();
        Double temperature = alertVerifyConfiguration.temperature();
        Integer maxOutputTokens = alertVerifyConfiguration.maxOutputTokens();
        System.out.println("[IIA][ALERT_VERIFY][CONFIG] model="
                + model
                + " temperature="
                + temperature
                + " maxOutputTokens="
                + maxOutputTokens
                + " fallbackOnInvalidLlm="
                + fallbackOnInvalidLlm);
        System.out.println("[IIA][ALERT_VERIFY][TEMPORAL] temporal default zone loaded=" + defaultTemporalZone);
        return new LlmRequest(
                AiUseCase.ALERT_VERIFY,
                systemPrompt(defaultTemporalZone),
                userPrompt(alert, defaultTemporalZone),
                model,
                temperature,
                maxOutputTokens,
                alert.alertId());
    }

    private String systemPrompt(String defaultTemporalZone) {
        return """
                You are verifying an Alert for the PIS Intelligent Information Assistant.
                An Alert is a user intent for operational monitoring.
                Verification must not generate code.
                Verification must not create Agent Definition.
                Verification must not create Agent Run.
                Verification must not create Suggestion.
                Verification must not use Agent Profile.
                Return only valid JSON.
                Do not use markdown.
                Do not invent operational data.
                Do not access DB, Kafka, HTTP, or filesystem.
                Respect only the MVP contract.

                MVP contract:
                - The only allowed data source is SERVICE_DATA.
                - The only allowed interpreter is EVENT_INTERPRETER.
                - The only allowed input model is ServiceDataV2.
                - The only allowed output model is AgentOutput.CANDIDATE_SUGGESTION.
                - The only allowed evaluation mode is STATELESS_EVENT_MATCH.
                - The allowed triggerType is EVENT.
                - Internal state is not supported.
                - Temporal conditions are supported only when evaluated statelessly on timestamps in one ServiceDataV2 event.
                - Local time windows such as "tra le 02:00 e le 10:00" must use operator LOCAL_TIME_BETWEEN and timezone %s unless the user supplies an explicit timezone.
                - Scheduled, future-existence, event-absence and historical time reasoning are not supported.
                - Absence of events is not supported.
                - Audio, video, device, display, broadcast, and content sources are not supported.
                - The MVP accepts any alert that can be expressed as a stateless boolean condition over the allowed ServiceData fields listed in the ServiceData Capability Catalog.
                - You must only use fields and operators listed in the ServiceData Capability Catalog.
                - If the user asks for a condition that can be mapped to one or more allowed fields/operators, decision must be VERIFIED.
                - If the user asks for absence of events over time, future existence, historical memory, external data, audio/video/device/display/broadcast/content, or free DB/API access, decision must be REJECTED.
                - You must first decompose the user prompt into all required operational constraints.
                - Every required constraint must be listed in requirementCoverage.requirements.
                - The alert can be VERIFIED only if every required constraint is mappable to one or more fields in the ServiceData Capability Catalog.
                - If any required constraint cannot be mapped to the catalog, decision must be REJECTED.
                - Do not silently ignore unsupported constraints.
                - Do not verify only a subset of the request.
                - Do not invent ServiceData fields that are not listed in the catalog.
                - Do not reject a request just because it contains words like "non" or "does not"; reject only if the negation requires absence of events or historical/time-window reasoning.
                - "non si ferma" / "passing through" is supported when mapped to passingType = TRANSIT.

                If decision is VERIFIED:
                - technicalSpecification is mandatory and must not be empty.
                - agentBlueprintPreview is mandatory and must not be empty.
                - technicalSpecification must contain at least: schemaVersion, source, inputModel, outputModel, triggerType, evaluationMode, condition, deduplicationKeyTemplate.
                - agentBlueprintPreview must contain at least: schemaVersion, agentName, triggerType, requiredSources, evaluationMode, targetTypes, stateRequirements, output.
                - Do not return empty objects for technicalSpecification or agentBlueprintPreview.
                - technicalSpecification.condition.type must be SERVICE_DATA_FIELD_MATCH unless using a legacy event name for backward compatibility.
                - Prefer SERVICE_DATA_FIELD_MATCH for all newly generated results.
                - Copy every extracted condition, including LOCAL_TIME_BETWEEN leaves, into agentBlueprintPreview.parameters.condition.
                - When multiple constraints must match the same nextCalls item, represent them with an anyElement node; never flatten correlated nextCalls constraints into independent leaves.
                - Never use activation policy or scheduler to model a supported temporal condition.
                """.formatted(defaultTemporalZone);
    }

    private String userPrompt(AlertVerificationPromptData alert, String defaultTemporalZone) {
        String catalog = ServiceDataCapabilityCatalog.compactPromptCatalog();
        System.out.println("[IIA][ALERT_VERIFY][CATALOG] allowedFields=" + ServiceDataCapabilityCatalog.allowedFieldCount());
        return """
                Alert to verify:
                - alertId: %s
                - name: %s
                - description: %s
                - originalPrompt: %s

                ServiceData Capability Catalog:
                %s

                Valid MVP cases:
                - Any alert that can be represented as a stateless boolean condition over the catalog fields.
                - Current stop point, journey identity, line/service/operator/mode, passing type, platforms, delays, statuses, route/next calls, replacement, info, exclusion, delivery, and monitoring checks are valid if they use catalog fields/operators.
                - "non si ferma" / "passing through" is valid when represented with passingType = TRANSIT.

                Cases to reject:
                - Empty or too short prompt.
                - Encyclopedic or non-operational questions.
                - Weather/meteo requests.
                - Requests requiring audio, video, device, display, broadcast, content, or other unsupported sources.
                - Requests about whether ServiceData says a journey was already announced are allowed only if represented with deliveryData.wasAnnounced.
                - Requests requiring internal state, scheduled/future evaluation, historical evaluation, or absence of events.
                - Requests that require creating Agent Definition, Agent Run, Suggestion, executable code, or Agent Profile.

                Condition rules:
                - Use technicalSpecification.condition.type = SERVICE_DATA_FIELD_MATCH for catalog-driven matches.
                - Conditions can contain "all" for AND, "any" for OR, or leaf checks with field/operator/value or field/operator/values.
                - For EXISTS, NOT_NULL, NOT_EMPTY no value is required.
                - For SIZE_* operators, value must be numeric.
                - For enum checks, values must be one of the enumValues listed in the catalog.
                - For "not stopping" / "non si ferma", use passingType EQUALS TRANSIT.
                - For cancelled journeys, use an allowed status field with ARRIVAL_CANCELLATION or DEPARTURE_CANCELLATION.
                - For delayed journeys, use a delay field or delay status from the catalog.
                - Stateless temporal predicates are allowed only on payload.ongroundServiceEvent.eventGenerationTime, payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].departureTime, and payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].arrivalTime.
                - Use operator LOCAL_TIME_BETWEEN with value {"start":"HH:mm:ss","end":"HH:mm:ss","timezone":"%s"} for local clock windows.
                - For current departure/arrival events use payload.ongroundServiceEvent.eventGenerationTime together with payload.ongroundServiceEvent.eventsType.
                - "parte da Genova tra le 02:00 e le 10:00" maps to eventsType CONTAINS DEPARTED, payload.ongroundServiceEvent.stopPoint.nameLong EQUALS_NORMALIZED Genova, and eventGenerationTime LOCAL_TIME_BETWEEN 02:00:00 and 10:00:00.
                - "arriva a Genova tra le 02:00 e le 10:00" maps to eventsType CONTAINS ARRIVED, payload.ongroundServiceEvent.stopPoint.nameLong EQUALS_NORMALIZED Genova, and eventGenerationTime LOCAL_TIME_BETWEEN 02:00:00 and 10:00:00.
                - For a future stop described within the received journey payload, use an anyElement node with path payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[].
                - Inside nextCalls anyElement, fields are relative to the same nextCall item: stopPoint.nameLong, departureTime, arrivalTime, or passingType.
                - A nextCalls stop point constraint and its departure/arrival time constraint must be inside the same anyElement.conditions node.
                - Do not use activation policy, scheduler, SCHEDULED_INTERPRETER, external API, or central API.
                - requiredSources must be only SERVICE_DATA.
                - interpreterType must be EVENT_INTERPRETER.
                - evaluationMode must be STATELESS_EVENT_MATCH.
                - stateRequirements.requiresState must be false.

                interpretedEventNames:
                - Use one or more of JOURNEY_CANCELLED, JOURNEY_DELAYED, PLATFORM_EVENT, JOURNEY_TRANSIT, JOURNEY_REPLACEMENT, JOURNEY_ROUTE_MATCH, JOURNEY_ORIGIN, SERVICE_DATA_FIELD_MATCH.
                - If no specific functional name fits, use SERVICE_DATA_FIELD_MATCH.
                - Do not base the technical validation on interpretedEventNames; the condition is authoritative.

                Requirement coverage:
                - requirementCoverage is mandatory for every response.
                - requirements must contain every binding condition requested by the user.
                - required=true for every constraint that is part of the Alert.
                - mappable=true only when the constraint is representable with one or more catalog fields.
                - mappedBy must contain only field paths present in the ServiceData Capability Catalog.
                - reason must explain why a required constraint is not mappable.
                - allRequiredRequirementsMapped=false when at least one required requirement has mappable=false.
                - If allRequiredRequirementsMapped=false, decision must be REJECTED.
                - If decision=VERIFIED, allRequiredRequirementsMapped must be true.

                Valid example:
                Prompt: "Avvisami quando una corsa parte da Firenze dal binario 1 e passa da Siena"
                Requirement coverage:
                - partenza da Firenze -> mappable using stopPoint / passingType ORIGIN or departure context.
                - binario 1 -> mappable using platform fields.
                - passa da Siena -> mappable using nextCalls or nextTransitCalls.
                Decision: VERIFIED

                Rejected example:
                Prompt: "Avvisami quando il treno 1253 parte da Genova e ha almeno 10 passeggeri"
                Requirement coverage:
                - treno 1253 -> mappable using vehicleJourneyName.
                - parte da Genova -> mappable using stopPoint / origin context.
                - almeno 10 passeggeri -> not mappable because no passenger count field exists in the catalog.
                Decision: REJECTED

                Rejected example:
                Prompt: "Avvisami quando il treno 1253 parte da Genova ed è rosso"
                Requirement coverage:
                - treno 1253 -> mappable using vehicleJourneyName.
                - parte da Genova -> mappable using stopPoint / origin context.
                - è rosso -> not mappable because no train color field exists in the catalog.
                Decision: REJECTED

                Valid temporal example:
                Prompt: "Fammi sapere quando una corsa parte da Genova tra le 02:00 e le 10:00"
                Condition all leaves:
                - {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"DEPARTED"}
                - {"field":"payload.ongroundServiceEvent.stopPoint.nameLong","operator":"EQUALS_NORMALIZED","value":"Genova"}
                - {"field":"payload.ongroundServiceEvent.eventGenerationTime","operator":"LOCAL_TIME_BETWEEN","value":{"start":"02:00:00","end":"10:00:00","timezone":"%s"}}
                Copy the same condition object into technicalSpecification.condition and agentBlueprintPreview.parameters.condition.
                Decision: VERIFIED

                Valid correlated nextCalls temporal example:
                Prompt: "Fammi sapere quando una corsa arriva a Genova e partirà a Gorla tra le 11:30 e le 12:35"
                Condition all children:
                - {"field":"payload.ongroundServiceEvent.eventsType","operator":"CONTAINS","value":"ARRIVED"}
                - {"field":"payload.ongroundServiceEvent.stopPoint.nameLong","operator":"EQUALS_NORMALIZED","value":"Genova"}
                - {"anyElement":{"path":"payload.stopPointJourney.stopPointsJourneyDetails[].nextCalls[]","conditions":{"all":[
                    {"field":"stopPoint.nameLong","operator":"EQUALS_NORMALIZED","value":"Gorla"},
                    {"field":"departureTime","operator":"LOCAL_TIME_BETWEEN","value":{"start":"11:30:00","end":"12:35:00","timezone":"%s"}}
                  ]}}}
                The Gorla and departureTime checks must match the same nextCall item. Copy this anyElement tree into both persisted condition objects.
                Decision: VERIFIED

                Rejected temporal example:
                Prompt: "Domani ci partono autobus da Pisa Centrale"
                Decision: REJECTED because it asks for future existence rather than matching a received ServiceDataV2 event.

                Expected JSON schema:
                {
                  "decision": "VERIFIED",
                  "summary": "The alert can be evaluated on realtime ServiceData events.",
                  "rejectedReason": null,
                  "confidence": 0.86,
                  "requiredSources": ["SERVICE_DATA"],
                  "interpreterType": "EVENT_INTERPRETER",
                  "triggerType": "EVENT",
                  "evaluationMode": "STATELESS_EVENT_MATCH",
                  "inputModel": "ServiceDataV2",
                  "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                  "targetTypes": ["SERVICE_DATA_JOURNEY"],
                  "interpretedEventNames": ["SERVICE_DATA_FIELD_MATCH"],
                  "requirementCoverage": {
                    "requirements": [
                      {
                        "text": "the train is passing through and does not stop",
                        "required": true,
                        "mappable": true,
                        "mappedBy": [
                          "payload.stopPointJourney.stopPointsJourneyDetails[].passingType"
                        ],
                        "reason": null
                      }
                    ],
                    "allRequiredRequirementsMapped": true
                  },
                  "technicalSpecification": {
                    "schemaVersion": "iia.alert.technical-specification/v2",
                    "source": "SERVICE_DATA",
                    "inputModel": "ServiceDataV2",
                    "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                    "triggerType": "EVENT",
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "condition": {
                      "type": "SERVICE_DATA_FIELD_MATCH",
                      "all": [
                        {
                          "field": "payload.stopPointJourney.stopPointsJourneyDetails[].passingType",
                          "operator": "EQUALS",
                          "value": "TRANSIT"
                        }
                      ]
                    },
                    "deduplicationKeyTemplate": "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}"
                  },
                  "agentBlueprintPreview": {
                    "schemaVersion": "iia.agent.blueprint/v1",
                    "agentName": "ServiceDataFieldMatchAlertAgent",
                    "description": "Detects matching ServiceData events using the verified stateless condition.",
                    "triggerType": "EVENT",
                    "requiredSources": ["SERVICE_DATA"],
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "targetTypes": ["SERVICE_DATA_JOURNEY"],
                    "parameters": {
                      "conditionType": "SERVICE_DATA_FIELD_MATCH",
                      "condition": {
                        "type": "SERVICE_DATA_FIELD_MATCH",
                        "all": [
                          {
                            "field": "payload.stopPointJourney.stopPointsJourneyDetails[].passingType",
                            "operator": "EQUALS",
                            "value": "TRANSIT"
                          }
                        ]
                      }
                    },
                    "stateRequirements": {
                      "requiresState": false
                    },
                    "output": {
                      "type": "CANDIDATE_SUGGESTION",
                      "reasonTemplate": "Journey ${journeyName} appears to be cancelled at ${stopPointName}.",
                      "operatorAdviceTemplate": "Verify the cancellation and passenger information workflow."
                    }
                  },
                  "warnings": [],
                  "safetyChecks": [
                    "No executable code generated.",
                    "No Agent Definition created.",
                    "No Agent Run created.",
                    "No Suggestion created."
                  ]
                }
                """.formatted(
                nullToEmpty(alert.alertId()),
                nullToEmpty(alert.name()),
                nullToEmpty(alert.description()),
                nullToEmpty(alert.prompt()),
                catalog,
                defaultTemporalZone,
                defaultTemporalZone,
                defaultTemporalZone);
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
}
