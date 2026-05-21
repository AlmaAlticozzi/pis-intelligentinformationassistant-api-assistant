package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertVerificationPromptBuilder {

    public LlmRequest build(AlertVerificationPromptData alert) {
        return new LlmRequest(
                AiUseCase.ALERT_VERIFY,
                systemPrompt(),
                userPrompt(alert),
                "mock-alert-verify",
                0.1,
                2000,
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

                Expected JSON schema:
                {
                  "decision": "VERIFIED | REJECTED",
                  "summary": "...",
                  "rejectedReason": null,
                  "confidence": 0.0,
                  "requiredSources": ["SERVICE_DATA"],
                  "interpreterType": "EVENT_INTERPRETER",
                  "triggerType": "EVENT",
                  "evaluationMode": "STATELESS_EVENT_MATCH",
                  "targetTypes": ["SERVICE_DATA_JOURNEY"],
                  "interpretedEventNames": ["JOURNEY_CANCELLED"],
                  "technicalSpecification": {},
                  "agentBlueprintPreview": {},
                  "warnings": [],
                  "safetyChecks": []
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
