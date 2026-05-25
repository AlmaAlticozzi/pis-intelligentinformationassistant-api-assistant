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
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRuntimeMetadata;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummaryListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
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
        System.out.println("[IIA][ALERT_SEARCH] criteria=" + criteria);
        return new AlertSummaryListResponse()
                .items(alertRepository.searchAlerts(criteria));
    }

    public Optional<AlertDetail> getAlert(String alertId) {
        System.out.println("[IIA][ALERT_GET] alertId=" + alertId);
        return alertRepository.getAlert(alertId);
    }

    @Transactional
    public boolean deleteAlert(String alertId) {
        if (existsDeletedAlert(alertId)) {
            System.out.println("[IIA][ALERT_DELETE] Delete rejected because alert is already deleted alertId=" + alertId);
            throw new AlertDeleteRejectedException(AlertDeleteRejectedException.Reason.DELETED);
        }

        Optional<AlertDetail> currentAlert = alertRepository.getAlert(alertId);
        if (currentAlert.isEmpty()) {
            System.out.println("[IIA][ALERT_DELETE] Alert not found alertId=" + alertId);
            return false;
        }

        AlertDetail alert = currentAlert.get();
        if (AlertStatus.VERIFYING.equals(alert.getStatus())) {
            System.out.println("[IIA][ALERT_DELETE] Delete rejected because alert is VERIFYING alertId=" + alertId);
            throw new AlertDeleteRejectedException(AlertDeleteRejectedException.Reason.VERIFYING);
        }
        if (alert.getRuntime() != null
                && AlertRuntimeMetadata.DeploymentStatusEnum.DEPLOYING.equals(alert.getRuntime().getDeploymentStatus())) {
            System.out.println("[IIA][ALERT_DELETE] Delete rejected because alert deployment is DEPLOYING alertId=" + alertId);
            throw new AlertDeleteRejectedException(AlertDeleteRejectedException.Reason.DEPLOYING);
        }

        boolean deleted = alertRepository.softDeleteAlert(alertId);
        if (deleted) {
            System.out.println("[IIA][ALERT_DELETE] Alert soft-deleted alertId=" + alertId);
        }
        return deleted;
    }

    @Transactional
    public Optional<AlertDetail> enableAlert(String alertId) {
        if (existsDeletedAlert(alertId)) {
            throw new AlertRuntimeStateChangeRejectedException(AlertRuntimeStateChangeRejectedException.Reason.DELETED);
        }

        Optional<AlertDetail> currentAlert = alertRepository.getAlert(alertId);
        if (currentAlert.isEmpty()) {
            return Optional.empty();
        }

        AlertDetail alert = currentAlert.get();
        AlertVerificationStatus verificationStatus = verificationStatus(alert);
        System.out.println("[IIA][ALERT_ENABLE] loaded status=" + alert.getStatus()
                + ", verificationStatus=" + verificationStatus
                + ", enabled=" + alert.getEnabled());

        if (!AlertStatus.VERIFIED.equals(alert.getStatus())) {
            throw new AlertRuntimeStateChangeRejectedException(AlertRuntimeStateChangeRejectedException.Reason.STATUS_NOT_VERIFIED);
        }
        if (!AlertVerificationStatus.VERIFIED.equals(verificationStatus)) {
            throw new AlertRuntimeStateChangeRejectedException(AlertRuntimeStateChangeRejectedException.Reason.VERIFICATION_NOT_VERIFIED);
        }
        if (Boolean.TRUE.equals(alert.getEnabled())) {
            throw new AlertRuntimeStateChangeRejectedException(AlertRuntimeStateChangeRejectedException.Reason.ALREADY_ENABLED);
        }
        if (!alertRepository.hasOperationalVerificationMetadata(alertId)) {
            throw new AlertRuntimeStateChangeRejectedException(AlertRuntimeStateChangeRejectedException.Reason.MISSING_OPERATIONAL_METADATA);
        }

        Optional<AlertDetail> enabledAlert = alertRepository.updateAlertEnabled(alertId, true);
        enabledAlert.ifPresent(updated -> System.out.println("[IIA][ALERT_ENABLE] enabled alertId=" + alertId));
        return enabledAlert;
    }

    @Transactional
    public Optional<AlertDetail> disableAlert(String alertId) {
        if (existsDeletedAlert(alertId)) {
            throw new AlertRuntimeStateChangeRejectedException(AlertRuntimeStateChangeRejectedException.Reason.DELETED);
        }

        Optional<AlertDetail> currentAlert = alertRepository.getAlert(alertId);
        if (currentAlert.isEmpty()) {
            return Optional.empty();
        }

        AlertDetail alert = currentAlert.get();
        AlertVerificationStatus verificationStatus = verificationStatus(alert);
        System.out.println("[IIA][ALERT_DISABLE] loaded status=" + alert.getStatus()
                + ", verificationStatus=" + verificationStatus
                + ", enabled=" + alert.getEnabled());

        if (AlertStatus.VERIFYING.equals(alert.getStatus())) {
            throw new AlertRuntimeStateChangeRejectedException(AlertRuntimeStateChangeRejectedException.Reason.VERIFYING);
        }
        if (!Boolean.TRUE.equals(alert.getEnabled())) {
            throw new AlertRuntimeStateChangeRejectedException(AlertRuntimeStateChangeRejectedException.Reason.ALREADY_DISABLED);
        }

        Optional<AlertDetail> disabledAlert = alertRepository.updateAlertEnabled(alertId, false);
        disabledAlert.ifPresent(updated -> System.out.println("[IIA][ALERT_DISABLE] disabled alertId=" + alertId));
        return disabledAlert;
    }

    public Optional<AlertDetail> updateAlert(String alertId, AlertUpdateRequest request) {
        System.out.println("[IIA][ALERT_UPDATE] Loading current alert detail alertId=" + alertId);
        if (existsDeletedAlert(alertId)) {
            throw new AlertUpdateRejectedException(AlertUpdateRejectedException.Reason.DELETED);
        }

        Optional<AlertDetail> currentAlert = alertRepository.getAlert(alertId);
        if (currentAlert.isEmpty()) {
            System.out.println("[IIA][ALERT_UPDATE] Alert not found alertId=" + alertId);
            return Optional.empty();
        }

        AlertDetail alert = currentAlert.get();
        System.out.println("[IIA][ALERT_UPDATE] Current alert detail loaded alertId=" + alertId + " status=" + alert.getStatus());
        if (AlertStatus.DELETED.equals(alert.getStatus())) {
            throw new AlertUpdateRejectedException(AlertUpdateRejectedException.Reason.DELETED);
        }
        if (AlertStatus.VERIFYING.equals(alert.getStatus())) {
            throw new AlertUpdateRejectedException(AlertUpdateRejectedException.Reason.VERIFYING);
        }
        if (existsActiveAlertWithNormalizedNameExcludingAlertId(request.getName(), alertId)) {
            throw new AlertUpdateRejectedException(AlertUpdateRejectedException.Reason.DUPLICATE_NAME);
        }

        if (isPromptUnchanged(alert.getPrompt(), request.getPrompt())) {
            return updateAlertWithUnchangedPrompt(alertId, request);
        }

        return updateAlertWithChangedPrompt(alertId, request, alert);
    }

    public boolean existsDeletedAlert(String alertId) {
        return alertRepository.existsDeletedAlert(alertId);
    }

    @Transactional
    public AlertDetail createDraftAlert(AlertCreateRequest request) {
        System.out.println("[IIA][ALERT_CREATE] Creating draft alert");
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

    public boolean existsActiveAlertWithNormalizedNameExcludingAlertId(String name, String alertId) {
        return alertRepository.existsActiveAlertWithNormalizedNameExcludingAlertId(name, alertId);
    }

    private Optional<AlertDetail> updateAlertWithUnchangedPrompt(String alertId, AlertUpdateRequest request) {
        System.out.println("[IIA][ALERT_UPDATE] prompt unchanged: metadata update only alertId=" + alertId);
        Optional<AlertDetail> updatedAlert = updateAlertMetadataWithoutPromptChangeInNewTransaction(alertId, request);
        updatedAlert.ifPresent(alert -> System.out.println("[IIA][ALERT_UPDATE] update completed alertId=" + alertId + " status=" + alert.getStatus()));
        return updatedAlert;
    }

    private Optional<AlertDetail> updateAlertWithChangedPrompt(String alertId, AlertUpdateRequest request, AlertDetail currentAlert) {
        System.out.println("[IIA][ALERT_UPDATE] prompt changed alertId=" + alertId);
        if (Boolean.TRUE.equals(request.getVerifyImmediately())) {
            return updateAlertWithChangedPromptAndImmediateVerification(alertId, request, currentAlert);
        }

        System.out.println("[IIA][ALERT_UPDATE] reset verification to DRAFT/PENDING alertId=" + alertId);
        Optional<AlertDetail> updatedAlert = updateAlertDraftAfterPromptChangeInNewTransaction(alertId, request);
        updatedAlert.ifPresent(alert -> System.out.println("[IIA][ALERT_UPDATE] verification artifacts cleared alertId=" + alertId));
        updatedAlert.ifPresent(alert -> System.out.println("[IIA][ALERT_UPDATE] update completed alertId=" + alertId + " status=" + alert.getStatus()));
        return updatedAlert;
    }

    private Optional<AlertDetail> updateAlertWithChangedPromptAndImmediateVerification(String alertId, AlertUpdateRequest request, AlertDetail currentAlert) {
        System.out.println("[IIA][ALERT_UPDATE] prompt changed: immediate verification requested alertId=" + alertId);
        boolean previousEnabled = Boolean.TRUE.equals(currentAlert.getEnabled());
        boolean requestedEnableAfterVerification = Boolean.TRUE.equals(request.getEnableAfterVerification());
        boolean effectiveEnableAfterVerification = previousEnabled && requestedEnableAfterVerification;
        System.out.println("[IIA][ALERT_UPDATE] previousEnabled="
                + previousEnabled
                + ", requestedEnableAfterVerification="
                + requestedEnableAfterVerification
                + ", effectiveEnableAfterVerification="
                + effectiveEnableAfterVerification
                + " alertId="
                + alertId);

        Optional<AlertDetail> updatedAlert = updateAlertVerifyingAfterPromptChangeInNewTransaction(alertId, request);
        updatedAlert.ifPresent(alert -> System.out.println("[IIA][ALERT_UPDATE] alert saved as VERIFYING alertId=" + alertId));
        updatedAlert.ifPresent(alert -> System.out.println("[IIA][ALERT_UPDATE] verification artifacts cleared alertId=" + alertId));
        if (updatedAlert.isPresent()) {
            scheduleAsyncVerification(alertId, effectiveEnableAfterVerification);
            System.out.println("[IIA][ALERT_UPDATE] async verification scheduled alertId=" + alertId);
        }
        updatedAlert.ifPresent(alert -> System.out.println("[IIA][ALERT_UPDATE] update completed alertId=" + alertId + " status=" + alert.getStatus()));
        return updatedAlert;
    }

    protected Optional<AlertDetail> updateAlertMetadataWithoutPromptChangeInNewTransaction(String alertId, AlertUpdateRequest request) {
        return QuarkusTransaction.requiringNew()
                .call(() -> alertRepository.updateAlertMetadataWithoutPromptChange(alertId, request));
    }

    protected Optional<AlertDetail> updateAlertDraftAfterPromptChangeInNewTransaction(String alertId, AlertUpdateRequest request) {
        return QuarkusTransaction.requiringNew()
                .call(() -> alertRepository.updateAlertDraftAfterPromptChange(alertId, request));
    }

    protected Optional<AlertDetail> updateAlertVerifyingAfterPromptChangeInNewTransaction(String alertId, AlertUpdateRequest request) {
        return QuarkusTransaction.requiringNew()
                .call(() -> alertRepository.updateAlertVerifyingAfterPromptChange(alertId, request));
    }

    private boolean isPromptUnchanged(String persistedPrompt, String requestedPrompt) {
        return trimToEmpty(persistedPrompt).equals(trimToEmpty(requestedPrompt));
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private AlertVerificationStatus verificationStatus(AlertDetail alert) {
        return alert.getVerification() == null ? null : alert.getVerification().getStatus();
    }

    private record AlertVerificationResolution(AlertVerificationOutcome outcome, boolean parseableLlm) {
    }
}
