package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.provider;

import io.quarkus.arc.lookup.LookupIfProperty;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Temporary LLM gateway used until a real provider integration is available.
 */
@ApplicationScoped
@LookupIfProperty(name = "iia.ai.provider", stringValue = "fake")
public class FakeLlmGateway implements LlmGateway {

    @Override
    public LlmResponse generateText(LlmRequest request) {
        System.out.println("[IIA-AI-TEST] FakeLlmGateway invoked for useCase=" + request.useCase());
        if (request.useCase() == AiUseCase.ALERT_ROUTE_UNDERSTANDING) {
            return routeResponse(request.userPrompt());
        }
        if (request.useCase() == AiUseCase.ALERT_VERIFY && isCancellationPrompt(request.userPrompt())) {
            return new LlmResponse(
                    """
                    {
                      "decision": "VERIFIED",
                      "summary": "The alert can be evaluated on realtime ServiceData events.",
                      "rejectedReason": null,
                      "confidence": 0.86,
                      "requiredSources": ["SERVICE_DATA"],
                      "interpreterType": "EVENT_INTERPRETER",
                      "inputModel": "ServiceDataV2",
                      "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                      "triggerType": "EVENT",
                      "evaluationMode": "STATELESS_EVENT_MATCH",
                      "targetTypes": ["SERVICE_DATA_JOURNEY"],
                      "interpretedEventNames": ["JOURNEY_CANCELLED"],
                      "requirementCoverage": {
                        "requirements": [
                          {
                            "text": "journey is cancelled",
                            "required": true,
                            "mappable": true,
                            "mappedBy": [
                              "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status"
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
                              "field": "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status",
                              "operator": "CONTAINS",
                              "value": "DEPARTURE_CANCELLATION"
                            }
                          ]
                        },
                        "deduplicationKeyTemplate": "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}"
                      },
                      "agentBlueprintPreview": {
                        "schemaVersion": "iia.agent.blueprint/v1",
                        "agentName": "CancelledJourneyServiceDataAgent",
                        "description": "Detects cancelled journeys from realtime ServiceData events.",
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
                                "field": "payload.stopPointJourney.stopPointsJourneyDetails[].departureStatuses[].status",
                                "operator": "CONTAINS",
                                "value": "DEPARTURE_CANCELLATION"
                              }
                            ]
                          }
                        },
                        "stateRequirements": {
                          "requiresState": false
                        },
                        "output": {
                          "type": "CANDIDATE_SUGGESTION",
                          "reasonTemplate": "Journey ${journeyName} appears to be cancelled.",
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
                    """,
                    "FAKE",
                    "fake-model",
                    null,
                    null,
                    null);
        }
        return new LlmResponse(
                "Crea un alert quando una corsa viene cancellata e non e stato fatto nessun annuncio.",
                "FAKE",
                "fake-model",
                null,
                null,
                null);
    }

    private LlmResponse routeResponse(String userPrompt) {
        String normalized = userPrompt == null ? "" : userPrompt.toLowerCase();
        if (normalized.contains("piove") || normalized.contains("meteo") || normalized.contains("quanto fa")
                || normalized.contains("2+2") || normalized.contains("wifi")) {
            return new LlmResponse("""
                    {
                      "decision":"REJECTED",
                      "dataDomains":[],
                      "primaryDataDomain":null,
                      "interpreterType":"UNKNOWN",
                      "accessMode":"NONE",
                      "intentKind":"UNSUPPORTED",
                      "outputMode":"NONE",
                      "requiresPolling":false,
                      "requiresServiceDataApi":false,
                      "requiresKafkaEvent":false,
                      "hasAggregation":false,
                      "hasCardinalityThreshold":false,
                      "hasReportIntent":false,
                      "confidence":0.0,
                      "summary":"The prompt is outside supported ServiceData routing.",
                      "rejectedReason":"Unsupported alert routing domain.",
                      "warnings":[]
                    }
                    """, "FAKE", "fake-model", null, null, null);
        }
        if (normalized.contains("ogni ") || normalized.contains(" quante ") || normalized.contains("almeno ")
                || normalized.contains("due treni") || normalized.contains("due bus") || normalized.contains("tre bus")) {
            return new LlmResponse("""
                    {
                      "decision":"ROUTED",
                      "dataDomains":["SERVICE_DATA"],
                      "primaryDataDomain":"SERVICE_DATA",
                      "interpreterType":"SCHEDULED_INTERPRETER",
                      "accessMode":"SERVICE_DATA_API_SNAPSHOT",
                      "intentKind":"SNAPSHOT_CONDITION",
                      "outputMode":"ON_MATCH",
                      "requiresPolling":true,
                      "requiresServiceDataApi":true,
                      "requiresKafkaEvent":false,
                      "hasAggregation":true,
                      "hasCardinalityThreshold":true,
                      "hasReportIntent":false,
                      "confidence":0.82,
                      "summary":"The alert requires a ServiceData snapshot route.",
                      "rejectedReason":null,
                      "warnings":[]
                    }
                    """, "FAKE", "fake-model", null, null, null);
        }
        return new LlmResponse("""
                {
                  "decision":"ROUTED",
                  "dataDomains":["SERVICE_DATA"],
                  "primaryDataDomain":"SERVICE_DATA",
                  "interpreterType":"EVENT_INTERPRETER",
                  "accessMode":"KAFKA_EVENT",
                  "intentKind":"EVENT_OCCURRENCE",
                  "outputMode":"ON_MATCH",
                  "requiresPolling":false,
                  "requiresServiceDataApi":false,
                  "requiresKafkaEvent":true,
                  "hasAggregation":false,
                  "hasCardinalityThreshold":false,
                  "hasReportIntent":false,
                  "confidence":0.86,
                  "summary":"The alert can be routed to event-based ServiceData verification.",
                  "rejectedReason":null,
                  "warnings":[]
                }
                """, "FAKE", "fake-model", null, null, null);
    }

    private boolean isCancellationPrompt(String userPrompt) {
        if (userPrompt == null) {
            return false;
        }
        String normalizedPrompt = userPrompt.toLowerCase();
        return normalizedPrompt.contains("cancelled")
                || normalizedPrompt.contains("journey is cancelled")
                || normalizedPrompt.contains("cancellata")
                || normalizedPrompt.contains("soppressa");
    }
}
