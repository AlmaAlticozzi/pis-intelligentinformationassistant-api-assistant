package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.ServiceDataCapabilityCatalog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AlertVerificationPromptBuilder {

    @Inject
    AiConfiguration aiConfiguration;

    public LlmRequest build(AlertVerificationPromptData alert) {
        AiConfiguration.AlertVerify alertVerifyConfiguration = aiConfiguration.alertVerify();
        String model = alertVerifyConfiguration.model();
        Double temperature = alertVerifyConfiguration.temperature();
        Integer maxOutputTokens = alertVerifyConfiguration.maxOutputTokens();
        System.out.println("[IIA][ALERT_VERIFY][CONFIG] model="
                + model
                + " temperature="
                + temperature
                + " maxOutputTokens="
                + maxOutputTokens);
        return new LlmRequest(
                AiUseCase.ALERT_VERIFY,
                systemPrompt(),
                userPrompt(alert),
                model,
                temperature,
                maxOutputTokens,
                alert.alertId());
    }

    private String systemPrompt() {
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
                - Time windows are not supported.
                - Absence of events is not supported.
                - Audio, video, device, display, broadcast, and content sources are not supported.
                - The MVP accepts any alert that can be expressed as a stateless boolean condition over the allowed ServiceData fields listed in the ServiceData Capability Catalog.
                - You must only use fields and operators listed in the ServiceData Capability Catalog.
                - If the user asks for a condition that can be mapped to one or more allowed fields/operators, decision must be VERIFIED.
                - If the user asks for absence of events over time, historical memory, external data, audio/video/device/display/broadcast/content, or free DB/API access, decision must be REJECTED.
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
                """;
    }

    private String userPrompt(AlertVerificationPromptData alert) {
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
                - Requests requiring internal state, time-window evaluation, or absence of events.
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
                - requiredSources must be only SERVICE_DATA.
                - interpreterType must be EVENT_INTERPRETER.
                - evaluationMode must be STATELESS_EVENT_MATCH.
                - stateRequirements.requiresState must be false.

                interpretedEventNames:
                - Use one or more of JOURNEY_CANCELLED, JOURNEY_DELAYED, PLATFORM_EVENT, JOURNEY_TRANSIT, JOURNEY_REPLACEMENT, JOURNEY_ROUTE_MATCH, JOURNEY_ORIGIN, SERVICE_DATA_FIELD_MATCH.
                - If no specific functional name fits, use SERVICE_DATA_FIELD_MATCH.
                - Do not base the technical validation on interpretedEventNames; the condition is authoritative.

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
                catalog);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
