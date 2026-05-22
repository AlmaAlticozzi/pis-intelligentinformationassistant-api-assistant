package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
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
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

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

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    AlertAsyncVerificationService alertAsyncVerificationService;

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

    public boolean existsDeletedAlert(String alertId) {
        return alertRepository.existsDeletedAlert(alertId);
    }

    @Transactional
    public AlertDetail createDraftAlert(AlertCreateRequest request) {
        System.out.println("AlertService.createDraftAlert: request=" + request);
        return alertRepository.createDraftAlert(request);
    }

    public AlertDetail createAlert(AlertCreateRequest request) {
        if (!Boolean.TRUE.equals(request.getVerifyImmediately())) {
            AlertDetail createdAlert = createDraftAlertInNewTransaction(request);
            System.out.println("[IIA][ALERT_CREATE] Alert created as DRAFT because verifyImmediately=false alertId=" + createdAlert.getId());
            return createdAlert;
        }

        AlertDetail createdAlert = createVerifyingAlertInNewTransaction(request);
        String alertId = createdAlert.getId();

        boolean enableAfterVerification = Boolean.TRUE.equals(request.getEnableAfterVerification());
        System.out.println("[IIA][ALERT_CREATE] verifyImmediately=true, saving alert as VERIFYING alertId=" + alertId);
        System.out.println("[IIA][ALERT_CREATE] enableAfterVerification requested=" + enableAfterVerification);
        System.out.println("[IIA][ALERT_CREATE] alert saved as VERIFYING, scheduling async verification alertId=" + alertId);
        scheduleAsyncVerification(alertId, enableAfterVerification);
        System.out.println("[IIA][ALERT_CREATE] returning VERIFYING response alertId=" + alertId);
        return createdAlert;
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
            if (aiConfiguration.alertVerify().simulateProviderTimeout()) {
                throw new RuntimeException("Simulated ALERT_VERIFY provider timeout");
            }

            System.out.println("[IIA][ALERT_VERIFY][LLM] Calling LlmGateway with useCase=ALERT_VERIFY alertId=" + alertId);
            // LangChain4j/OpenAI client may already apply retry. Application-level retry must be introduced later only with idempotency and backoff.
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
        } catch (ProcessingException ex) {
            String shortMessage = shortTechnicalMessage(ex);
            System.out.println("[IIA][ALERT_VERIFY][LLM_ERROR] " + shortMessage);
            return new AlertVerificationResolution(technicalErrorOutcome(shortMessage, promptRequest), false);
        } catch (RuntimeException ex) {
            String shortMessage = shortTechnicalMessage(ex);
            System.out.println("[IIA][ALERT_VERIFY][LLM_ERROR] " + shortMessage);
            return new AlertVerificationResolution(technicalErrorOutcome(shortMessage, promptRequest), false);
        }
    }

    private void scheduleAsyncVerification(String alertId, boolean enableAfterVerification) {
        managedExecutor.runAsync(() -> alertAsyncVerificationService.verifyCreatedAlertAsync(alertId, enableAfterVerification))
                .exceptionally(ex -> {
                    String shortMessage = shortTechnicalMessage(ex);
                    System.out.println("[IIA][ALERT_VERIFY][ASYNC_ERROR] alertId=" + alertId + " error=" + shortMessage);
                    alertAsyncVerificationService.markCreatedAlertVerificationError(alertId, shortMessage);
                    System.out.println("[IIA][ALERT_CREATE] finalStatus=ERROR enabled=false");
                    return null;
                });
    }

    protected AlertDetail createDraftAlertInNewTransaction(AlertCreateRequest request) {
        return QuarkusTransaction.requiringNew()
                .call(() -> alertRepository.createDraftAlert(request));
    }

    protected AlertDetail createVerifyingAlertInNewTransaction(AlertCreateRequest request) {
        return QuarkusTransaction.requiringNew()
                .call(() -> alertRepository.createVerifyingAlert(request));
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

    private AlertVerificationOutcome technicalErrorOutcome(String warning, LlmRequest promptRequest) {
        String summary = "Alert verification failed because the AI provider did not respond successfully.";
        return new AlertVerificationOutcome(
                AlertVerificationDecision.ERROR,
                summary,
                null,
                0.0,
                aiConfiguration.provider(),
                promptRequest.model(),
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
                        "error", summary),
                Map.of(
                        "schemaVersion", "iia.agent.blueprint/v1",
                        "canGenerate", false,
                        "error", summary),
                Map.of(
                        "requirements", List.of(),
                        "allRequiredRequirementsMapped", false),
                List.of(warning),
                List.of(
                        "No executable code generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
    }

    private String shortTechnicalMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if ((message == null || message.isBlank()) && throwable.getCause() != null) {
            message = throwable.getCause().getMessage();
        }
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    public boolean existsActiveAlertWithNormalizedName(String name) {
        return alertRepository.existsActiveAlertWithNormalizedName(name);
    }

    private record AlertVerificationResolution(AlertVerificationOutcome outcome, boolean parseableLlm) {
    }
}
