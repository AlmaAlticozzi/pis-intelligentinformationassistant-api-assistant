package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummaryListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationMockEngine;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AlertService {

    @Inject
    AlertRepository alertRepository;

    @Inject
    AlertVerificationPromptBuilder alertVerificationPromptBuilder;

    @Inject
    AlertVerificationLlmResponseParser alertVerificationLlmResponseParser;

    @Inject
    AlertVerificationMockEngine alertVerificationMockEngine;

    @Inject
    AlertVerificationOutcomeValidator alertVerificationOutcomeValidator;

    @Inject
    Instance<LlmGateway> llmGateway;

    /**
     * Development safety valve for non-conforming LLM JSON. Production should set this to false.
     */
    @ConfigProperty(name = "iia.alert-verification.fallback-on-invalid-llm", defaultValue = "true")
    boolean fallbackOnInvalidLlm;

    public AlertSummaryListResponse searchAlerts(AlertSearchCriteria criteria) {
        System.out.println("AlertService.searchAlerts: criteria=" + criteria);
        return new AlertSummaryListResponse()
                .items(alertRepository.searchAlerts(criteria));
    }

    public Optional<AlertDetail> getAlert(String alertId) {
        System.out.println("AlertService.getAlert: alertId=" + alertId);
        return alertRepository.getAlert(alertId);
    }

    @Transactional
    public AlertDetail createDraftAlert(AlertCreateRequest request) {
        System.out.println("AlertService.createDraftAlert: request=" + request);
        return alertRepository.createDraftAlert(request);
    }

    @Transactional
    public Optional<AlertDetail> verifyAlert(String alertId, AlertVerificationRequest request) {
        Optional<AlertVerificationPromptData> promptData = alertRepository.getAlertVerificationPromptData(alertId);
        if (promptData.isEmpty()) {
            return Optional.empty();
        }

        LlmRequest promptRequest = alertVerificationPromptBuilder.build(promptData.get());
        System.out.println("[IIA][ALERT_VERIFY][PROMPT] Built ALERT_VERIFY prompt for alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][PROMPT][SYSTEM] " + promptRequest.systemPrompt());
        System.out.println("[IIA][ALERT_VERIFY][PROMPT][USER] " + promptRequest.userPrompt());

        AlertVerificationResolution resolution = resolveVerificationOutcome(alertId, promptData.get(), promptRequest);
        AlertVerificationOutcome outcome = alertVerificationOutcomeValidator.validate(resolution.outcome());
        if (resolution.parseableLlm() && fallbackOnInvalidLlm && shouldFallbackOnInvalidLlm(resolution.outcome(), outcome)) {
            System.out.println("[IIA][ALERT_VERIFY][LLM] Falling back to deterministic mock engine");
            outcome = alertVerificationOutcomeValidator.validate(alertVerificationMockEngine.verify(alertId, promptData.get().prompt())
                    .withAdditionalWarning("LLM response was invalid for the current MVP contract. Deterministic mock fallback was used."));
        }
        return alertRepository.verifyAlert(alertId, request, outcome);
    }

    private AlertVerificationResolution resolveVerificationOutcome(
            String alertId,
            AlertVerificationPromptData promptData,
            LlmRequest promptRequest) {
        if (llmGateway.isUnsatisfied()) {
            System.out.println("[IIA][ALERT_VERIFY][LLM] Falling back to deterministic mock engine");
            return new AlertVerificationResolution(alertVerificationMockEngine.verify(alertId, promptData.prompt()), false);
        }

        try {
            System.out.println("[IIA][ALERT_VERIFY][LLM] Calling LlmGateway with useCase=ALERT_VERIFY alertId=" + alertId);
            LlmResponse response = llmGateway.get().generateText(promptRequest);
            String provider = response == null ? null : response.provider();
            String model = response == null ? null : response.model();
            System.out.println("[IIA][ALERT_VERIFY][LLM] LlmGateway response provider=" + provider + " model=" + model);
            System.out.println("[IIA][ALERT_VERIFY][LLM_RAW] " + (response == null ? null : response.text()));

            Optional<AlertVerificationOutcome> parsedOutcome = alertVerificationLlmResponseParser.parse(
                    response == null ? null : response.text(),
                    provider,
                    model);
            if (parsedOutcome.isPresent()) {
                logParsedOutcome(parsedOutcome.get());
                return new AlertVerificationResolution(parsedOutcome.get(), true);
            }

            System.out.println("[IIA][ALERT_VERIFY][LLM] Falling back to deterministic mock engine");
            return new AlertVerificationResolution(alertVerificationMockEngine.verify(alertId, promptData.prompt())
                    .withAdditionalWarning("LLM response was empty or not parseable. Deterministic mock fallback was used."), false);
        } catch (Exception ex) {
            System.out.println("[IIA][ALERT_VERIFY][LLM] LlmGateway failed for alertId=" + alertId + " error=" + ex.getMessage());
            return new AlertVerificationResolution(technicalErrorOutcome(ex), false);
        }
    }

    private void logParsedOutcome(AlertVerificationOutcome outcome) {
        System.out.println("[IIA][ALERT_VERIFY][PARSER] decision=" + outcome.decision());
        System.out.println("[IIA][ALERT_VERIFY][PARSER] technicalSpecification present=" + (outcome.technicalSpecification() != null));
        System.out.println("[IIA][ALERT_VERIFY][PARSER] agentBlueprintPreview present=" + (outcome.agentBlueprintPreview() != null));
        System.out.println("[IIA][ALERT_VERIFY][PARSER] requiredSources=" + outcome.requiredSources());
        System.out.println("[IIA][ALERT_VERIFY][PARSER] interpreterType=" + outcome.interpreterType());
        System.out.println("[IIA][ALERT_VERIFY][PARSER] interpretedEventNames=" + outcome.interpretedEventNames());
        System.out.println("[IIA][ALERT_VERIFY][PARSER] targetTypes=" + outcome.interpretedTargetTypes());
    }

    private boolean shouldFallbackOnInvalidLlm(AlertVerificationOutcome rawOutcome, AlertVerificationOutcome validatedOutcome) {
        return rawOutcome.decision() == AlertVerificationDecision.VERIFIED
                && validatedOutcome.decision() == AlertVerificationDecision.REJECTED;
    }

    private AlertVerificationOutcome technicalErrorOutcome(Exception ex) {
        String reason = "Alert verification failed due to a technical LLM gateway error.";
        return new AlertVerificationOutcome(
                AlertVerificationDecision.ERROR,
                reason,
                ex.getMessage(),
                0.0,
                "llm-gateway",
                null,
                "alert-verify-mvp-v1",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                Map.of(
                        "schemaVersion", "iia.alert.technical-specification/v1",
                        "decision", "ERROR",
                        "error", reason),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "canGenerate", false,
                        "error", reason),
                List.of(reason),
                List.of(
                        "No executable code generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
    }

    public boolean existsActiveAlertWithNormalizedName(String name) {
        return alertRepository.existsActiveAlertWithNormalizedName(name);
    }

    private record AlertVerificationResolution(AlertVerificationOutcome outcome, boolean parseableLlm) {
    }
}
