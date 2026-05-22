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
