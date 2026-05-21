package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
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

                If decision is VERIFIED:
                - technicalSpecification is mandatory and must not be empty.
                - agentBlueprintPreview is mandatory and must not be empty.
                - technicalSpecification must contain at least: schemaVersion, source, inputModel, outputModel, triggerType, evaluationMode, condition, deduplicationKeyTemplate.
                - agentBlueprintPreview must contain at least: schemaVersion, agentName, triggerType, requiredSources, evaluationMode, targetTypes, stateRequirements, output.
                - Do not return empty objects for technicalSpecification or agentBlueprintPreview.
                """;
    }

    private String userPrompt(AlertVerificationPromptData alert) {
        return """
                Alert to verify:
                - alertId: %s
                - name: %s
                - description: %s
                - originalPrompt: %s

                Valid MVP cases:
                - ServiceData journey cancellation or suppression events.
                - ServiceData journey delay events.
                - ServiceData platform/binario events.
                - Generic ServiceData journey or train operational events.

                Cases to reject:
                - Empty or too short prompt.
                - Encyclopedic or non-operational questions.
                - Weather/meteo requests.
                - Requests requiring audio, announcement, video, device, display, broadcast, content, or other unsupported sources.
                - Requests requiring internal state, time-window evaluation, or absence of events.
                - Requests that require creating Agent Definition, Agent Run, Suggestion, executable code, or Agent Profile.

                For this MVP, if the user prompt is about a cancelled journey:
                - interpretedEventNames must contain JOURNEY_CANCELLED.
                - technicalSpecification.condition.type must be JOURNEY_CANCELLED.
                - requiredSources must be only SERVICE_DATA.
                - interpreterType must be EVENT_INTERPRETER.
                - evaluationMode must be STATELESS_EVENT_MATCH.
                - stateRequirements.requiresState must be false.

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
                  "interpretedEventNames": ["JOURNEY_CANCELLED"],
                  "technicalSpecification": {
                    "schemaVersion": "iia.alert.technical-specification/v1",
                    "source": "SERVICE_DATA",
                    "inputModel": "ServiceDataV2",
                    "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                    "triggerType": "EVENT",
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "condition": {
                      "type": "JOURNEY_CANCELLED",
                      "description": "Detect a journey cancellation from a realtime ServiceData event.",
                      "eventNames": ["JOURNEY_CANCELLED"],
                      "match": {
                        "stopPointName": "MILANO MALPENSA T1"
                      }
                    },
                    "deduplicationKeyTemplate": "SERVICE_DATA_CANCELLED:${journeyId}:${stopPointId}"
                  },
                  "agentBlueprintPreview": {
                    "schemaVersion": "iia.agent.blueprint/v1",
                    "agentName": "CancelledJourneyAtMilanoMalpensaT1Agent",
                    "description": "Detects cancelled journeys at Milano Malpensa T1 from realtime ServiceData events.",
                    "triggerType": "EVENT",
                    "requiredSources": ["SERVICE_DATA"],
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "targetTypes": ["SERVICE_DATA_JOURNEY"],
                    "parameters": {
                      "stopPointName": "MILANO MALPENSA T1",
                      "condition": "JOURNEY_CANCELLED"
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
                nullToEmpty(alert.prompt()));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
