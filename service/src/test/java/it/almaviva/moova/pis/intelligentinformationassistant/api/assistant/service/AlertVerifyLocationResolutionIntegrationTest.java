package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationMockEngine;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationResolverService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.SimpleAlertLocationMentionExtractor;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertVerifyLocationResolutionIntegrationTest {

    private static final String RHO_ID = "TNPNTS00000000005467";
    private static final String MALPENSA_T1_ID = "TNPNTS00000000000028";
    private static final String MALPENSA_T2_ID = "TNPNTS00000000000029";

    @Test
    void rhoExactProducesStopPointIdEquals() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da Rho Fieramilano",
                verifiedOutcomeJson(stopPointEqualsCondition(RHO_ID), coverage("payload.ongroundServiceEvent.stopPoint.id")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("payload.ongroundServiceEvent.stopPoint.id", "EQUALS", RHO_ID);
        assertThat(outcome.confidence()).isEqualTo(0.8);
    }

    @Test
    void malpensaProducesStopPointIdInAndLightWarning() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da Malpensa",
                verifiedOutcomeJson(
                        stopPointInCondition(List.of(MALPENSA_T1_ID, MALPENSA_T2_ID)),
                        coverage("payload.ongroundServiceEvent.stopPoint.id")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("payload.ongroundServiceEvent.stopPoint.id", "IN", MALPENSA_T1_ID, MALPENSA_T2_ID);
        assertThat(outcome.warnings())
                .contains("One or more alert locations resolved to multiple stopPoint.id candidates; confidence slightly reduced.");
        assertThat(outcome.confidence()).isEqualTo(0.75);
    }

    @Test
    void genovaNerviUnresolvedUsesNameLongFallbackAndLowConfidence() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando un treno passa da Genova Nervi",
                verifiedOutcomeJson(
                        nameLongFallbackCondition("Genova Nervi"),
                        coverage("payload.ongroundServiceEvent.stopPoint.nameLong")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().toString())
                .contains("payload.ongroundServiceEvent.stopPoint.nameLong", "CONTAINS_NORMALIZED", "Genova Nervi");
        assertThat(outcome.warnings())
                .contains("One or more alert locations were unresolved and require nameLong/nameShort fallback; confidence reduced.");
        assertThat(outcome.confidence()).isEqualTo(0.25);
    }

    @Test
    void noLocationPromptDoesNotInventLocationFilter() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Dimmi quando una metro e in ritardo di oltre 10 min",
                verifiedOutcomeJson(
                        eventsTypeCondition("DEPARTED"),
                        coverage("payload.ongroundServiceEvent.eventsType")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.technicalSpecification().get("condition").toString()).doesNotContain("stopPoint");
        assertThat(outcome.warnings()).isEmpty();
    }

    @Test
    void unknownStopPointIdFromFakeLlmIsRejectedByValidator() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da Rho Fieramilano",
                verifiedOutcomeJson(
                        stopPointEqualsCondition("TNPNTS99999999999999"),
                        coverage("payload.ongroundServiceEvent.stopPoint.id")));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.REJECTED);
        assertThat(outcome.rejectedReason())
                .contains("Unsupported or unknown stopPoint id in technicalSpecification: TNPNTS99999999999999");
    }

    @Test
    void nameLongUsedDespiteResolvedLocationProducesValidatorWarning() {
        AlertVerificationOutcome outcome = verifyWithLlmOutcome(
                "Avvertimi quando una corsa parte da Rho Fieramilano",
                verifiedOutcomeJson(
                        nameLongFallbackCondition("Rho Fieramilano"),
                        coverage("payload.ongroundServiceEvent.stopPoint.nameLong"),
                        locationResolution(RHO_ID)));

        assertThat(outcome.decision()).isEqualTo(AlertVerificationDecision.VERIFIED);
        assertThat(outcome.warnings())
                .contains("locationResolution contains resolved mentions but technicalSpecification does not use stopPoint.id.");
    }

    @SuppressWarnings("unchecked")
    private AlertVerificationOutcome verifyWithLlmOutcome(String prompt, String llmJson) {
        AlertService service = new AlertService();
        service.alertRepository = mock(AlertRepository.class);
        when(service.alertRepository.getAlertVerificationPromptData("ALRT1"))
                .thenReturn(Optional.of(new AlertVerificationPromptData("ALRT1", "Alert", null, prompt)));
        when(service.alertRepository.verifyAlert(any(), any(), any())).thenReturn(Optional.empty());

        service.alertVerificationPromptBuilder = mock(AlertVerificationPromptBuilder.class);
        when(service.alertVerificationPromptBuilder.build(any())).thenReturn(
                new LlmRequest(AiUseCase.ALERT_VERIFY, "system", "user", "gpt-4.1-mini", 0.1, 5000, "ALRT1"));
        service.alertVerificationLlmResponseParser = new AlertVerificationLlmResponseParser();
        service.alertVerificationOutcomeValidator = new AlertVerificationOutcomeValidator();
        service.alertVerificationMockEngine = mock(AlertVerificationMockEngine.class);
        service.alertLocationMentionExtractor = new SimpleAlertLocationMentionExtractor();
        service.alertLocationResolverService = new AlertLocationResolverService();
        service.llmGateway = mock(Instance.class);
        LlmGateway gateway = mock(LlmGateway.class);
        when(service.llmGateway.isUnsatisfied()).thenReturn(false);
        when(service.llmGateway.get()).thenReturn(gateway);
        when(gateway.generateText(any())).thenReturn(new LlmResponse(llmJson, "FAKE", "fake-model", null, null, null));
        service.aiConfiguration = mock(AiConfiguration.class);
        AiConfiguration.AlertVerify alertVerify = mock(AiConfiguration.AlertVerify.class);
        when(alertVerify.simulateProviderTimeout()).thenReturn(false);
        when(service.aiConfiguration.alertVerify()).thenReturn(alertVerify);
        service.fallbackOnInvalidLlm = false;

        service.verifyAlert("ALRT1", new AlertVerificationRequest());

        ArgumentCaptor<AlertVerificationOutcome> outcome = ArgumentCaptor.forClass(AlertVerificationOutcome.class);
        verify(service.alertRepository).verifyAlert(any(), any(), outcome.capture());
        return outcome.getValue();
    }

    private String verifiedOutcomeJson(String condition, String requirementCoverage) {
        return verifiedOutcomeJson(condition, requirementCoverage, "");
    }

    private String verifiedOutcomeJson(String condition, String requirementCoverage, String extraTechnicalSpecification) {
        return """
                {
                  "decision": "VERIFIED",
                  "summary": "The alert can be evaluated on realtime ServiceData events.",
                  "confidence": 0.80,
                  "provider": "fake",
                  "model": "fake-model",
                  "promptVersion": "alert-verify-mvp-v1",
                  "requiredSources": ["SERVICE_DATA"],
                  "interpreterType": "EVENT_INTERPRETER",
                  "inputModel": "ServiceDataV2",
                  "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                  "triggerType": "EVENT",
                  "evaluationMode": "STATELESS_EVENT_MATCH",
                  "interpretedEventNames": ["SERVICE_DATA_FIELD_MATCH"],
                  "interpretedTargetTypes": ["SERVICE_DATA_JOURNEY"],
                  "technicalSpecification": {
                    "schemaVersion": "iia.alert.technical-specification/v2",
                    "source": "SERVICE_DATA",
                    "inputModel": "ServiceDataV2",
                    "outputModel": "AgentOutput.CANDIDATE_SUGGESTION",
                    "triggerType": "EVENT",
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "condition": %s,
                    "deduplicationKeyTemplate": "SERVICE_DATA:${journeyId}:${stopPointId}:${conditionHash}"%s
                  },
                  "agentBlueprintPreview": {
                    "schemaVersion": "iia.agent.blueprint/v1",
                    "agentName": "ServiceDataFieldMatchAlertAgent",
                    "triggerType": "EVENT",
                    "requiredSources": ["SERVICE_DATA"],
                    "evaluationMode": "STATELESS_EVENT_MATCH",
                    "targetTypes": ["SERVICE_DATA_JOURNEY"],
                    "stateRequirements": {"requiresState": false},
                    "output": {"type": "CANDIDATE_SUGGESTION"}
                  },
                  "requirementCoverage": %s,
                  "warnings": [],
                  "safetyChecks": ["No executable code generated.", "No Agent Definition created.", "No Suggestion created."]
                }
                """.formatted(condition, extraTechnicalSpecification, requirementCoverage);
    }

    private String stopPointEqualsCondition(String id) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTED"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.id", "operator": "EQUALS", "value": "%s"}
                  ]
                }
                """.formatted(id);
    }

    private String stopPointInCondition(List<String> ids) {
        String values = ids.stream()
                .map(id -> "\"" + id + "\"")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTED"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.id", "operator": "IN", "values": [%s]}
                  ]
                }
                """.formatted(values);
    }

    private String nameLongFallbackCondition(String value) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "DEPARTED"},
                    {"field": "payload.ongroundServiceEvent.stopPoint.nameLong", "operator": "CONTAINS_NORMALIZED", "value": "%s"}
                  ]
                }
                """.formatted(value);
    }

    private String eventsTypeCondition(String value) {
        return """
                {
                  "type": "SERVICE_DATA_FIELD_MATCH",
                  "all": [
                    {"field": "payload.ongroundServiceEvent.eventsType", "operator": "CONTAINS", "value": "%s"}
                  ]
                }
                """.formatted(value);
    }

    private String coverage(String field) {
        return """
                {
                  "requirements": [
                    {"text": "condition", "required": true, "mappable": true, "mappedBy": ["%s"], "reason": ""}
                  ],
                  "allRequiredRequirementsMapped": true
                }
                """.formatted(field);
    }

    private String locationResolution(String id) {
        return """
                ,
                    "locationResolution": {
                      "resolutions": [
                        {"rawText": "Rho Fieramilano", "status": "RESOLVED", "selectedPointIds": ["%s"]}
                      ]
                    }
                """.formatted(id);
    }
}
