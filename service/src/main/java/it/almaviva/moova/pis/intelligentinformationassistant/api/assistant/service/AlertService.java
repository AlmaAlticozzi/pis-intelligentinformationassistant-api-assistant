package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationPolarity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingLocation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AgentGenerationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AgentGenerationPreviewOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AgentGenerationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AiUseCase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmGateway;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.LlmResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.config.AiConfiguration;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRuntimeMetadata;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummaryListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationMockEngine;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationExtractionResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationMention;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationResolution;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationResolverService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationSemanticRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.AlertLocationTargetFieldMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.PointCandidate;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.SimpleAlertLocationMentionExtractor;
import it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.ProcessingException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@ApplicationScoped
public class AlertService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AI_PROVIDER_ERROR_SUMMARY =
            "Alert verification failed because the AI provider did not respond successfully.";
    private static final String TENANT_CONTEXT_ERROR_SUMMARY =
            "Alert verification failed because tenant context was not available for database access.";

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
    SimpleAlertLocationMentionExtractor alertLocationMentionExtractor;

    @Inject
    AlertLocationResolverService alertLocationResolverService;

    @Inject
    AlertLocationUnderstandingService alertLocationUnderstandingService;

    @Inject
    AlertLocationTargetFieldMapper alertLocationTargetFieldMapper;

    @Inject
    Instance<LlmGateway> llmGateway;

    @Inject
    AiConfiguration aiConfiguration;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    AlertAsyncVerificationService alertAsyncVerificationService;

    @Inject
    TenantContext tenantContext;

    @Inject
    Instance<TenantResolver> tenantResolver;

    @Inject
    TransactionManager transactionManager;

    @Inject
    RequestContextController requestContextController;

    @Inject
    AgentGenerationPreviewMapper agentGenerationPreviewMapper;

    @Inject
    AgentGenerationPromptBuilder agentGenerationPromptBuilder;

    @Inject
    AgentGenerationLlmResponseParser agentGenerationLlmResponseParser;

    /**
     * Development safety valve for non-conforming LLM JSON. Production should set this to false.
     */
    @ConfigProperty(name = "iia.alert-verification.fallback-on-invalid-llm", defaultValue = "false")
    boolean fallbackOnInvalidLlm;

    @ConfigProperty(name = "iia.alert-verification.location-understanding.enabled", defaultValue = "false")
    boolean locationUnderstandingEnabled;

    @ConfigProperty(name = "fnd.default-schema", defaultValue = "")
    String defaultSchema;

    @ConfigProperty(name = "iia.agent-generation-preview.use-llm", defaultValue = "false")
    boolean agentGenerationPreviewUseLlm;

    @ConfigProperty(name = "iia.agent-generation-preview.fallback-to-deterministic-on-llm-error", defaultValue = "true")
    boolean agentGenerationPreviewFallbackToDeterministicOnLlmError;

    @ConfigProperty(name = "iia.agent-generation-preview.persist-validated-llm-preview", defaultValue = "false")
    boolean persistValidatedLlmPreview;

    public AlertSummaryListResponse searchAlerts(AlertSearchCriteria criteria) {
        System.out.println("[IIA][ALERT_SEARCH] criteria=" + criteria);
        return new AlertSummaryListResponse()
                .items(alertRepository.searchAlerts(criteria));
    }

    public Optional<AlertDetail> getAlert(String alertId) {
        System.out.println("[IIA][ALERT_GET] alertId=" + alertId);
        return alertRepository.getAlert(alertId);
    }

    public Optional<AgentGenerationPreviewResponse> previewAgentGenerationForAlert(
            String alertId,
            AgentGenerationPreviewRequest request) {
        Optional<AlertAgentGenerationPreviewData> previewData = alertRepository.getAlertAgentGenerationPreviewData(alertId);
        if (previewData.isEmpty()) {
            System.out.println("[IIA][AGENT_PREVIEW] Alert not found alertId=" + alertId);
            return Optional.empty();
        }

        AlertAgentGenerationPreviewData data = previewData.get();
        if (data.deletedAt() != null
                || AlertStatus.DELETED.toString().equals(data.status())
                || !AlertStatus.VERIFIED.toString().equals(data.status())
                || !AlertVerificationStatus.VERIFIED.toString().equals(data.verificationStatus())) {
            System.out.println("[IIA][AGENT_PREVIEW] Alert not verified alertId=" + alertId
                    + ", status=" + data.status()
                    + ", verificationStatus=" + data.verificationStatus());
            throw new AlertAgentGenerationPreviewRejectedException(
                    AlertAgentGenerationPreviewRejectedException.Reason.NOT_VERIFIED);
        }
        if (data.technicalSpecification() == null
                || data.technicalSpecification().isEmpty()
                || data.agentBlueprintPreview() == null
                || data.agentBlueprintPreview().isEmpty()) {
            System.out.println("[IIA][AGENT_PREVIEW] Missing technical artifacts alertId=" + alertId);
            throw new AlertAgentGenerationPreviewRejectedException(
                    AlertAgentGenerationPreviewRejectedException.Reason.MISSING_TECHNICAL_ARTIFACTS);
        }

        System.out.println("[IIA][AGENT_PREVIEW] Building read-only preview alertId=" + alertId);
        System.out.println("[IIA][AGENT_PREVIEW][CONFIG] useLlm=" + agentGenerationPreviewUseLlm
                + ", fallbackToDeterministicOnLlmError=" + agentGenerationPreviewFallbackToDeterministicOnLlmError
                + ", persistValidatedLlmPreview=" + persistValidatedLlmPreview);
        if (!agentGenerationPreviewUseLlm && persistValidatedLlmPreview) {
            System.out.println("[IIA][AGENT_PREVIEW_PERSIST] Skipped persistence because preview source is deterministic alertId=" + alertId);
        }
        AgentGenerationPreviewResponse response = agentGenerationPreviewUseLlm
                ? previewUsingLlmWithControlledFallback(data, request)
                : agentGenerationPreviewMapper.toResponse(data, request);
        System.out.println("[IIA][AGENT_PREVIEW] Preview produced alertId=" + alertId + ", generationMode=DSL, complexity=LOW");
        return Optional.of(response);
    }

    private AgentGenerationPreviewResponse previewUsingLlmWithControlledFallback(
            AlertAgentGenerationPreviewData data,
            AgentGenerationPreviewRequest request) {
        System.out.println("[IIA][AGENT_PREVIEW_LLM] LLM generation enabled alertId=" + data.alertId());
        try {
            LlmRequest llmRequest = agentGenerationPromptBuilder.build(data, request);
            System.out.println("[IIA][AGENT_PREVIEW_LLM] Calling LlmGateway useCase="
                    + AiUseCase.AGENT_BLUEPRINT_GENERATE + " alertId=" + data.alertId());
            LlmResponse response = llmGateway.get().generateText(llmRequest);
            System.out.println("[IIA][AGENT_PREVIEW_LLM] LLM response received alertId=" + data.alertId()
                    + ", provider=" + response.provider() + ", model=" + response.model());
            AgentGenerationPreviewOutcome outcome = agentGenerationLlmResponseParser.parse(response.text())
                    .orElseThrow(() -> new IllegalStateException("LLM Agent Blueprint response is not valid JSON."));
            System.out.println("[IIA][AGENT_PREVIEW_LLM] Parsed LLM blueprint alertId=" + data.alertId()
                    + ", canGenerate=" + outcome.canGenerate()
                    + ", recommendedGenerationMode=" + outcome.recommendedGenerationMode());
            if (!outcome.canGenerate()) {
                throw new AlertAgentGenerationPreviewRejectedException(
                        AlertAgentGenerationPreviewRejectedException.Reason.INVALID_BLUEPRINT);
            }
            List<String> warnings = new java.util.ArrayList<>(outcome.warnings());
            warnings.add(AgentGenerationPreviewMapper.LLM_VALIDATED_PREVIEW_WARNING);
            AgentGenerationPreviewResponse generated = agentGenerationPreviewMapper.toResponse(
                    data,
                    request,
                    outcome.blueprint(),
                    warnings);
            System.out.println("[IIA][AGENT_PREVIEW_LLM] LLM blueprint validated alertId=" + data.alertId()
                    + ", runtimeSupported=true");
            if (persistValidatedLlmPreview) {
                System.out.println("[IIA][AGENT_PREVIEW_PERSIST] Persistence requested alertId=" + data.alertId());
                persistValidatedLlmBlueprint(data.alertId(), generated);
            } else {
                System.out.println("[IIA][AGENT_PREVIEW_PERSIST] Persistence disabled alertId=" + data.alertId());
            }
            return generated;
        } catch (RuntimeException exception) {
            if (exception instanceof AlertAgentGenerationPreviewRejectedException rejectedException
                    && rejectedException.reason() == AlertAgentGenerationPreviewRejectedException.Reason.NOT_VERIFIED) {
                throw exception;
            }
            System.out.println("[IIA][AGENT_PREVIEW_LLM] LLM blueprint rejected or generation failed alertId="
                    + data.alertId() + ", error=" + exception.getMessage());
            if (!agentGenerationPreviewFallbackToDeterministicOnLlmError) {
                throw exception;
            }
            System.out.println("[IIA][AGENT_PREVIEW_LLM] Falling back to deterministic preview alertId=" + data.alertId());
            if (persistValidatedLlmPreview) {
                System.out.println("[IIA][AGENT_PREVIEW_PERSIST] Skipped persistence because LLM fallback was used alertId=" + data.alertId());
            }
            return agentGenerationPreviewMapper.toResponse(
                    data,
                    request,
                    null,
                    List.of(AgentGenerationPreviewMapper.LLM_FALLBACK_PREVIEW_WARNING));
        }
    }

    @SuppressWarnings("unchecked")
    private void persistValidatedLlmBlueprint(
            String alertId,
            AgentGenerationPreviewResponse generated) {
        Map<String, Object> blueprint = OBJECT_MAPPER.convertValue(generated.getBlueprint(), Map.class);
        System.out.println("[IIA][AGENT_PREVIEW_PERSIST] Persisting validated LLM blueprint alertId=" + alertId);
        if (!alertRepository.persistValidatedAgentBlueprintPreview(alertId, blueprint)) {
            System.out.println("[IIA][AGENT_PREVIEW_PERSIST] Skipped persistence because alert is no longer VERIFIED alertId=" + alertId);
            throw new AlertAgentGenerationPreviewRejectedException(
                    AlertAgentGenerationPreviewRejectedException.Reason.NOT_VERIFIED);
        }
        List<String> responseWarnings = new ArrayList<>(generated.getWarnings());
        int diagnosticWarningIndex = responseWarnings.indexOf(AgentGenerationPreviewMapper.DSL_DIAGNOSTIC_WARNING);
        if (!responseWarnings.contains(AgentGenerationPreviewMapper.PERSISTED_LLM_PREVIEW_WARNING)) {
            responseWarnings.add(
                    diagnosticWarningIndex < 0 ? responseWarnings.size() : diagnosticWarningIndex,
                    AgentGenerationPreviewMapper.PERSISTED_LLM_PREVIEW_WARNING);
        }
        generated.setWarnings(responseWarnings);
        System.out.println("[IIA][AGENT_PREVIEW_PERSIST] Persisted validated LLM blueprint alertId=" + alertId);
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
        logCreateTenantDebug("before-save", null);
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

    public Optional<AlertDetail> verifyAlert(String alertId, AlertVerificationRequest request) {
        System.out.println("[IIA][ALERT_VERIFY][REQUEST_CONTEXT] AlertService.verifyAlert entry alertId=" + alertId
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " transactionActive=" + transactionActive());
        Optional<AlertVerificationPromptData> promptData = alertRepository.getAlertVerificationPromptData(alertId);
        if (promptData.isEmpty()) {
            return Optional.empty();
        }

        AlertVerificationPromptData enrichedPromptData = withLocationResolutionContext(alertId, promptData.get());
        LlmRequest promptRequest = alertVerificationPromptBuilder.build(enrichedPromptData);
        System.out.println("[IIA][ALERT_VERIFY][CONFIG] fallbackOnInvalidLlm=" + fallbackOnInvalidLlm);
        System.out.println("[IIA][ALERT_VERIFY][PROMPT] Built ALERT_VERIFY prompt for alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][PROMPT][SYSTEM] " + promptRequest.systemPrompt());
        System.out.println("[IIA][ALERT_VERIFY][PROMPT][USER] " + promptRequest.userPrompt());

        AlertVerificationResolution resolution = resolveVerificationOutcome(alertId, enrichedPromptData, promptRequest);
        System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] parser outcome alertId=" + alertId
                + " decision=" + resolution.outcome().decision()
                + " parseableLlm=" + resolution.parseableLlm());
        AlertVerificationOutcome outcome = alertVerificationOutcomeValidator.validate(
                resolution.outcome(), enrichedPromptData.prompt());
        System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] validator outcome alertId=" + alertId
                + " decision=" + outcome.decision()
                + " rejectedReason=" + outcome.rejectedReason());
        if (resolution.parseableLlm() && shouldFallbackOnInvalidLlm(resolution.outcome(), outcome)) {
            System.out.println("[IIA][ALERT_VERIFY][VALIDATION] LLM outcome rejected by backend validation alertId="
                    + alertId + ", fallbackOnInvalidLlm=" + fallbackOnInvalidLlm);
        }
        if (resolution.parseableLlm() && fallbackOnInvalidLlm && shouldFallbackOnInvalidLlm(resolution.outcome(), outcome)) {
            System.out.println("[IIA][ALERT_VERIFY][LLM] Falling back to deterministic mock engine because backend validation rejected LLM output");
            outcome = alertVerificationOutcomeValidator.validate(
                    alertVerificationMockEngine.verify(alertId, enrichedPromptData.prompt())
                            .withAdditionalWarning("LLM response was empty, invalid or rejected. Deterministic mock fallback was used because fallback-on-invalid-llm is enabled."),
                    enrichedPromptData.prompt());
            System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] fallback validator outcome alertId=" + alertId
                    + " decision=" + outcome.decision());
        }
        outcome = applyLocationConfidenceAdjustment(outcome, enrichedPromptData.locationResolutionContext());
        System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] persisting normal outcome alertId=" + alertId
                + " decision=" + outcome.decision()
                + " method=alertRepository.verifyAlert");
        return persistVerificationOutcomeInTransaction(alertId, request, outcome);
    }

    protected Optional<AlertDetail> persistVerificationOutcomeInTransaction(
            String alertId,
            AlertVerificationRequest request,
            AlertVerificationOutcome outcome) {
        String resolvedTenantForHibernate = currentTenantId();
        if (resolvedTenantForHibernate == null) {
            resolvedTenantForHibernate = normalizedDefaultSchema();
        }
        String tenantForHibernate = resolvedTenantForHibernate;
        System.out.println("[IIA][ALERT_VERIFY][TX_DEBUG] before persist normal outcome"
                + " alertId=" + alertId
                + " decision=" + outcome.decision()
                + " tenantPresent=" + (currentTenantId() != null)
                + " transactionActive=" + transactionActive()
                + " requestContextActive=" + requestContextActive()
                + " thread=" + Thread.currentThread().getName()
                + " caller=AlertService.persistVerificationOutcomeInTransaction");
        if (transactionManager == null) {
            System.out.println("[IIA][ALERT_VERIFY][TX_DEBUG] TransactionManager unavailable; executing repository call directly"
                    + " alertId=" + alertId
                    + " decision=" + outcome.decision());
            return alertRepository.verifyAlert(alertId, request, outcome);
        }
        boolean activatedRequestContext = false;
        try {
            if (!requestContextActive() && requestContextController != null) {
                activatedRequestContext = requestContextController.activate();
            }
            if (tenantForHibernate != null && tenantContext != null) {
                tenantContext.setTenantId(tenantForHibernate);
            }
            logHibernateTenantResolution("before-persist-normal-outcome", alertId, tenantForHibernate);
            return QuarkusTransaction.requiringNew().call(() -> {
                logHibernateTenantResolution("inside-persist-normal-outcome-transaction", alertId, tenantForHibernate);
                System.out.println("[IIA][ALERT_VERIFY][TX_DEBUG] inside persist normal outcome transaction"
                        + " alertId=" + alertId
                        + " decision=" + outcome.decision()
                        + " tenantPresent=" + (currentTenantId() != null)
                        + " transactionActive=" + transactionActive()
                        + " requestContextActive=" + requestContextActive()
                        + " thread=" + Thread.currentThread().getName()
                        + " caller=AlertService.persistVerificationOutcomeInTransaction");
                return alertRepository.verifyAlert(alertId, request, outcome);
            });
        } finally {
            if (activatedRequestContext && requestContextController != null) {
                requestContextController.deactivate();
            }
        }
    }

    private AlertVerificationPromptData withLocationResolutionContext(String alertId, AlertVerificationPromptData promptData) {
        AlertVerificationLocationContext context = resolveLocationContext(alertId, promptData.prompt());
        return new AlertVerificationPromptData(
                promptData.alertId(),
                promptData.name(),
                promptData.description(),
                promptData.prompt(),
                context);
    }

    private AlertVerificationLocationContext resolveLocationContext(String alertId, String prompt) {
        System.out.println("[IIA][ALERT_VERIFY][LOCATION] alertId=" + alertId + " prompt=" + prompt);
        System.out.println("[IIA][ALERT_VERIFY][LOCATION_UNDERSTANDING] alertId=" + alertId
                + " enabled=" + locationUnderstandingEnabled
                + " prompt=" + prompt);
        if (locationUnderstandingEnabled) {
            Optional<AlertVerificationLocationContext> semanticContext = resolveSemanticLocationContext(alertId, prompt);
            if (semanticContext.isPresent()) {
                AlertVerificationLocationContext context = semanticContext.get();
                System.out.println("[IIA][ALERT_VERIFY][LOCATION_CONTEXT] finalContext=" + context);
                System.out.println("[IIA][ALERT_VERIFY][LOCATION_CONTEXT] compactPromptSection="
                        + context.compactPromptSection());
                return context;
            }
            System.out.println("[IIA][ALERT_VERIFY][LOCATION_UNDERSTANDING] falling back to legacy extractor alertId="
                    + alertId);
        }
        return resolveLegacyLocationContext(alertId, prompt);
    }

    private Optional<AlertVerificationLocationContext> resolveSemanticLocationContext(String alertId, String prompt) {
        try {
            AlertLocationUnderstandingService understandingService = alertLocationUnderstandingService == null
                    ? null
                    : alertLocationUnderstandingService;
            if (understandingService == null) {
                System.out.println("[IIA][ALERT_VERIFY][LOCATION_UNDERSTANDING] service unavailable alertId=" + alertId);
                return Optional.empty();
            }
            AlertLocationUnderstandingResult understanding = understandingService.understandLocations(prompt, alertId);
            System.out.println("[IIA][ALERT_VERIFY][LOCATION_UNDERSTANDING] result=" + understanding);
            for (AlertLocationUnderstandingLocation location : understanding.locations()) {
                System.out.println("[IIA][ALERT_VERIFY][LOCATION_UNDERSTANDING] location rawText="
                        + location.rawText()
                        + " role=" + location.role()
                        + " requiredCoverage=" + location.requiredCoverage()
                        + " polarity=" + location.polarity()
                        + " confidence=" + location.confidence());
            }
            if (shouldFallbackToLegacy(understanding)) {
                return Optional.empty();
            }
            return Optional.of(toPromptLocationContext(understanding));
        } catch (RuntimeException ex) {
            System.out.println("[IIA][ALERT_VERIFY][LOCATION_UNDERSTANDING] error alertId="
                    + alertId + " reason=" + shortTechnicalMessage(ex));
            return Optional.empty();
        }
    }

    private boolean shouldFallbackToLegacy(AlertLocationUnderstandingResult understanding) {
        if (understanding == null) {
            return true;
        }
        if (understanding.hasLocations() || !understanding.nonLocationConstraints().isEmpty()) {
            return false;
        }
        return understanding.warnings().stream()
                .anyMatch(warning -> warning.contains("LLM response")
                        || warning.contains("No LlmGateway")
                        || warning.contains("valid JSON"));
    }

    private AlertVerificationLocationContext resolveLegacyLocationContext(String alertId, String prompt) {
        SimpleAlertLocationMentionExtractor extractor = alertLocationMentionExtractor == null
                ? new SimpleAlertLocationMentionExtractor()
                : alertLocationMentionExtractor;
        AlertLocationResolverService resolver = alertLocationResolverService == null
                ? new AlertLocationResolverService()
                : alertLocationResolverService;

        AlertLocationExtractionResult extraction = extractor.extract(prompt);
        List<AlertLocationResolution> resolutions = extraction.hasLocationMentions()
                ? resolver.resolve(extraction.mentions())
                : List.of();
        AlertVerificationLocationContext context = toPromptLocationContext(extraction, resolutions);
        System.out.println("[IIA][ALERT_VERIFY][LOCATION] hasMentions="
                + context.hasLocationMentions() + " resolutions=" + context.resolutions().size());
        System.out.println("[IIA][ALERT_VERIFY][LOCATION] promptSection=" + context.compactPromptSection());
        return context;
    }

    private AlertVerificationLocationContext toPromptLocationContext(AlertLocationUnderstandingResult understanding) {
        AlertLocationResolverService resolver = alertLocationResolverService == null
                ? new AlertLocationResolverService()
                : alertLocationResolverService;
        List<AlertLocationMention> mentions = understanding.locations().stream()
                .map(location -> new AlertLocationMention(
                        resolutionText(location),
                        toLegacySemanticRole(location.role()),
                        location.confidence()))
                .toList();
        List<AlertLocationResolution> resolutions = mentions.isEmpty()
                ? List.of()
                : resolver.resolve(mentions);
        List<AlertVerificationLocationContext.LocationResolution> promptResolutions = new ArrayList<>();
        for (int index = 0; index < understanding.locations().size(); index++) {
            AlertLocationUnderstandingLocation location = understanding.locations().get(index);
            AlertLocationResolution resolution = resolutions.get(index);
            promptResolutions.add(toPromptLocationResolution(location, resolution));
        }
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                understanding.hasLocations(),
                promptResolutions,
                semanticNonLocationConstraints(understanding),
                understanding.warnings());
        System.out.println("[IIA][ALERT_VERIFY][LOCATION_CONTEXT] semanticContext=" + context);
        return context;
    }

    private List<AlertVerificationLocationContext.NonLocationConstraint> semanticNonLocationConstraints(
            AlertLocationUnderstandingResult understanding) {
        List<AlertVerificationLocationContext.NonLocationConstraint> constraints = new ArrayList<>(
                understanding.nonLocationConstraints().stream()
                        .map(constraint -> new AlertVerificationLocationContext.NonLocationConstraint(
                                constraint.type().name(),
                                constraint.rawText()))
                        .toList());
        if (understanding.mainEvent() != null && understanding.mainEvent().eventIntent() != null) {
            constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                    "MAIN_EVENT_INTENT",
                    understanding.mainEvent().eventIntent().name()));
        }
        return constraints;
    }

    private String resolutionText(AlertLocationUnderstandingLocation location) {
        if (location.normalizedText() != null && !location.normalizedText().isBlank()) {
            return location.normalizedText();
        }
        return location.rawText();
    }

    private AlertVerificationLocationContext toPromptLocationContext(
            AlertLocationExtractionResult extraction,
            List<AlertLocationResolution> resolutions) {
        return new AlertVerificationLocationContext(
                extraction.hasLocationMentions(),
                resolutions.stream()
                        .map(this::toPromptLocationResolution)
                        .toList());
    }

    private AlertVerificationLocationContext.LocationResolution toPromptLocationResolution(
            AlertLocationResolution resolution) {
        List<String> selectedPointIds = resolution.selectedPointIds() == null
                ? List.of()
                : resolution.selectedPointIds();
        return new AlertVerificationLocationContext.LocationResolution(
                resolution.mention().rawText(),
                "",
                resolution.mention().semanticRole().name(),
                "",
                true,
                "INCLUDE",
                "",
                resolution.mention().confidence(),
                resolution.pointResolutionResult().status().name(),
                resolution.pointResolutionResult().candidates().stream()
                        .filter(candidate -> candidate.selected()
                                || selectedPointIds.contains(candidate.id())
                                || resolution.fallbackToNameLong())
                        .map(candidate -> toPromptLocationCandidate(candidate, selectedPointIds.contains(candidate.id())))
                        .toList(),
                selectedPointIds,
                resolution.fallbackToNameLong(),
                resolution.fallbackToNameLong(),
                resolution.confidenceImpact(),
                resolution.fallbackToNameLong()
                        ? "Location unresolved; textual fallback is allowed with lower confidence."
                        : "",
                targetFieldHints(toRole(resolution.mention().semanticRole())));
    }

    private AlertVerificationLocationContext.LocationResolution toPromptLocationResolution(
            AlertLocationUnderstandingLocation location,
            AlertLocationResolution resolution) {
        List<String> selectedPointIds = resolution.selectedPointIds() == null
                ? List.of()
                : resolution.selectedPointIds();
        List<String> targetFieldHints = targetFieldHints(location.role());
        String warningReason = location.role() == AlertLocationRole.GENERIC_LOCATION
                ? "Generic semantic role; use the field most coherent with the main event or reject if unsafe."
                : "";
        if (targetFieldHints.isEmpty()) {
            warningReason = appendWarning(warningReason,
                    "No supported ServiceData target fields are available for role " + location.role() + ".");
        }
        if (resolution.fallbackToNameLong()) {
            warningReason = appendWarning(warningReason,
                    "Location unresolved; textual fallback is allowed with lower confidence.");
        }

        System.out.println("[IIA][ALERT_VERIFY][LOCATION_RESOLUTION] rawText="
                + location.rawText()
                + " role=" + location.role()
                + " status=" + resolution.pointResolutionResult().status()
                + " selectedIds=" + selectedPointIds
                + " targetFieldHints=" + targetFieldHints);

        return new AlertVerificationLocationContext.LocationResolution(
                location.rawText(),
                location.normalizedText(),
                location.role().name(),
                location.relationToMainEvent().name(),
                location.requiredCoverage() || location.polarity() == AlertLocationPolarity.EXCLUDE,
                location.polarity().name(),
                location.logicalGroup(),
                location.confidence(),
                resolution.pointResolutionResult().status().name(),
                resolution.pointResolutionResult().candidates().stream()
                        .filter(candidate -> candidate.selected()
                                || selectedPointIds.contains(candidate.id())
                                || resolution.fallbackToNameLong())
                        .map(candidate -> toPromptLocationCandidate(candidate, selectedPointIds.contains(candidate.id())))
                        .toList(),
                selectedPointIds,
                resolution.fallbackToNameLong(),
                resolution.fallbackToNameLong(),
                resolution.confidenceImpact(),
                warningReason,
                targetFieldHints);
    }

    private String appendWarning(String current, String warning) {
        if (current == null || current.isBlank()) {
            return warning;
        }
        return current + " " + warning;
    }

    private List<String> targetFieldHints(AlertLocationRole role) {
        AlertLocationTargetFieldMapper mapper = alertLocationTargetFieldMapper == null
                ? new AlertLocationTargetFieldMapper()
                : alertLocationTargetFieldMapper;
        return mapper.targetFieldHints(role);
    }

    private AlertLocationSemanticRole toLegacySemanticRole(AlertLocationRole role) {
        AlertLocationRole safeRole = role == null ? AlertLocationRole.GENERIC_LOCATION : role;
        return switch (safeRole) {
            case MAIN_EVENT_LOCATION -> AlertLocationSemanticRole.EVENT_STOP_POINT;
            case ORIGIN_LOCATION -> AlertLocationSemanticRole.ORIGIN_STOP_POINT;
            case DESTINATION_LOCATION -> AlertLocationSemanticRole.DESTINATION_STOP_POINT;
            case ROUTE_OR_NEXT_CALL_LOCATION -> AlertLocationSemanticRole.NEXT_CALL_STOP_POINT;
            case TRANSIT_LOCATION -> AlertLocationSemanticRole.NEXT_TRANSIT_STOP_POINT;
            case CANCELLED_CALL_LOCATION -> AlertLocationSemanticRole.NEXT_CANCELLED_STOP_POINT;
            case REPLACEMENT_LOCATION -> AlertLocationSemanticRole.REPLACEMENT_STOP_POINT;
            case GENERIC_LOCATION -> AlertLocationSemanticRole.GENERIC_STOP_POINT;
        };
    }

    private AlertLocationRole toRole(AlertLocationSemanticRole semanticRole) {
        if (semanticRole == null) {
            return AlertLocationRole.GENERIC_LOCATION;
        }
        return switch (semanticRole) {
            case DEPARTURE_EVENT_STOP_POINT, ARRIVAL_EVENT_STOP_POINT, EVENT_STOP_POINT ->
                    AlertLocationRole.MAIN_EVENT_LOCATION;
            case ORIGIN_STOP_POINT -> AlertLocationRole.ORIGIN_LOCATION;
            case DESTINATION_STOP_POINT -> AlertLocationRole.DESTINATION_LOCATION;
            case NEXT_CALL_STOP_POINT -> AlertLocationRole.ROUTE_OR_NEXT_CALL_LOCATION;
            case NEXT_TRANSIT_STOP_POINT -> AlertLocationRole.TRANSIT_LOCATION;
            case NEXT_CANCELLED_STOP_POINT -> AlertLocationRole.CANCELLED_CALL_LOCATION;
            case REPLACEMENT_STOP_POINT -> AlertLocationRole.REPLACEMENT_LOCATION;
            case GENERIC_STOP_POINT, UNKNOWN -> AlertLocationRole.GENERIC_LOCATION;
        };
    }

    private AlertVerificationLocationContext.LocationCandidate toPromptLocationCandidate(PointCandidate candidate) {
        return toPromptLocationCandidate(candidate, false);
    }

    private AlertVerificationLocationContext.LocationCandidate toPromptLocationCandidate(
            PointCandidate candidate,
            boolean selectedByResolution) {
        return new AlertVerificationLocationContext.LocationCandidate(
                candidate.id(),
                candidate.nameLong(),
                candidate.nameShort(),
                candidate.transportMode(),
                candidate.score(),
                candidate.matchType().name(),
                candidate.selected() || selectedByResolution);
    }

    private AlertVerificationOutcome applyLocationConfidenceAdjustment(
            AlertVerificationOutcome outcome,
            AlertVerificationLocationContext context) {
        if (outcome == null || context == null || !context.hasLocationMentions()
                || outcome.decision() != AlertVerificationDecision.VERIFIED) {
            return outcome;
        }
        double penalty = 0.0;
        List<String> warnings = new ArrayList<>(outcome.warnings() == null ? List.of() : outcome.warnings());
        if (context.resolutions().stream().anyMatch(AlertVerificationLocationContext.LocationResolution::fallbackToNameLong)) {
            penalty += 0.25;
            warnings.add("One or more alert locations were unresolved and require nameLong/nameShort fallback; confidence reduced.");
        }
        if (context.resolutions().stream().anyMatch(resolution -> selectedCandidateCount(resolution) > 1)) {
            penalty += 0.05;
            warnings.add("One or more alert locations resolved to multiple stopPoint.id candidates; confidence slightly reduced.");
        }
        if (context.resolutions().stream().anyMatch(this::hasSelectedFuzzyCandidate)) {
            penalty += 0.02;
            warnings.add("One or more alert locations were resolved by fuzzy token matching; confidence slightly reduced.");
        }
        if (penalty == 0.0) {
            return outcome;
        }
        Double confidence = adjustedLocationConfidence(outcome.confidence(), context, penalty);
        return new AlertVerificationOutcome(
                outcome.decision(),
                outcome.summary(),
                outcome.rejectedReason(),
                confidence,
                outcome.provider(),
                outcome.model(),
                outcome.promptVersion(),
                outcome.requiredSources(),
                outcome.interpreterType(),
                outcome.inputModel(),
                outcome.outputModel(),
                outcome.triggerType(),
                outcome.evaluationMode(),
                outcome.interpretedEventNames(),
                outcome.interpretedTargetTypes(),
                outcome.technicalSpecification(),
                outcome.agentBlueprintPreview(),
                outcome.requirementCoverage(),
                List.copyOf(warnings),
                outcome.safetyChecks());
    }

    private Double adjustedLocationConfidence(
            Double confidence,
            AlertVerificationLocationContext context,
            double penalty) {
        if (confidence == null) {
            return null;
        }
        double adjusted = Math.max(0.0, confidence - penalty);
        if (context.resolutions().stream().anyMatch(AlertVerificationLocationContext.LocationResolution::fallbackToNameLong)) {
            adjusted = Math.min(adjusted, 0.25);
        }
        return adjusted;
    }

    private long selectedCandidateCount(AlertVerificationLocationContext.LocationResolution resolution) {
        return resolution.candidates().stream()
                .filter(AlertVerificationLocationContext.LocationCandidate::selected)
                .count();
    }

    private boolean hasSelectedFuzzyCandidate(AlertVerificationLocationContext.LocationResolution resolution) {
        return resolution.candidates().stream()
                .anyMatch(candidate -> candidate.selected() && "FUZZY_TOKEN".equals(candidate.matchType()));
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

            AlertVerificationLlmResponseParser.ParseResult parseResult = alertVerificationLlmResponseParser.parseDetailed(
                    response == null ? null : response.text(),
                    provider,
                    model);
            if (parseResult.outcome().isPresent()) {
                logParsedOutcome(parseResult.outcome().get());
                return new AlertVerificationResolution(parseResult.outcome().get(), true);
            }

            System.out.println("[IIA][ALERT_VERIFY][PARSER] Failed to parse LLM JSON alertId=" + alertId
                    + ", reason=" + parseResult.failureReason()
                    + ", rawLength=" + parseResult.rawLength()
                    + ", looksTruncated=" + parseResult.looksTruncated());
            if (fallbackOnInvalidLlm) {
                System.out.println("[IIA][ALERT_VERIFY][LLM] Falling back to deterministic mock engine because fallback-on-invalid-llm is enabled");
                return new AlertVerificationResolution(alertVerificationMockEngine.verify(alertId, promptData.prompt())
                        .withAdditionalWarning("LLM response was empty, invalid or rejected. Deterministic mock fallback was used because fallback-on-invalid-llm is enabled."), false);
            }
            return new AlertVerificationResolution(technicalErrorOutcome(
                    "LLM response could not be parsed: " + parseResult.failureReason()
                            + " (rawLength=" + parseResult.rawLength()
                            + ", looksTruncated=" + parseResult.looksTruncated() + ").",
                    promptRequest), false);
        } catch (ProcessingException ex) {
            String shortMessage = shortTechnicalMessage(ex);
            logTechnicalVerificationError(shortMessage);
            return new AlertVerificationResolution(technicalErrorOutcome(shortMessage, promptRequest), false);
        } catch (RuntimeException ex) {
            String shortMessage = shortTechnicalMessage(ex);
            logTechnicalVerificationError(shortMessage);
            return new AlertVerificationResolution(technicalErrorOutcome(shortMessage, promptRequest), false);
        }
    }

    private void scheduleAsyncVerification(String alertId, boolean enableAfterVerification) {
        ResolvedAsyncTenant tenant = resolveTenantIdForAsyncVerification(alertId);
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] before scheduling async verification alertId=" + alertId
                + " capturedTenant=" + safeTenant(tenant.tenantId())
                + " capturedTenantSource=" + tenant.source()
                + " defaultSchemaFallback=" + normalizedDefaultSchema());
        managedExecutor.runAsync(() -> alertAsyncVerificationService.verifyCreatedAlertAsync(
                        alertId, enableAfterVerification, tenant.tenantId()))
                .exceptionally(ex -> {
                    String shortMessage = shortTechnicalMessage(ex);
                    System.out.println("[IIA][ALERT_VERIFY][ASYNC_ERROR] alertId=" + alertId + " error=" + shortMessage);
                    alertAsyncVerificationService.markCreatedAlertVerificationError(alertId, shortMessage, tenant.tenantId());
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
        String summary = isTenantContextFailure(warning) ? TENANT_CONTEXT_ERROR_SUMMARY : AI_PROVIDER_ERROR_SUMMARY;
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

    private void logTechnicalVerificationError(String shortMessage) {
        if (isTenantContextFailure(shortMessage)) {
            System.out.println("[IIA][ALERT_VERIFY][TENANT] tenant context failure during verification");
            return;
        }
        System.out.println("[IIA][ALERT_VERIFY][LLM_ERROR] " + shortMessage);
    }

    private ResolvedAsyncTenant resolveTenantIdForAsyncVerification(String alertId) {
        String tenantId = tenantContext == null ? null : tenantContext.getTenantId();
        String source = "tenantContext";
        if ((tenantId == null || tenantId.isBlank())
                && tenantResolver != null
                && !tenantResolver.isUnsatisfied()
                && !tenantResolver.isAmbiguous()) {
            tenantId = tenantResolver.get().resolveTenantId();
            source = "hibernateResolver";
        }
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = normalizedDefaultSchema();
            source = tenantId == null ? "none" : "defaultSchema";
        }
        System.out.println("[IIA][ALERT_VERIFY][TENANT] async tenant captured alertId=" + alertId
                + " source=" + source
                + " present=" + (tenantId != null && !tenantId.isBlank()));
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] async tenant captured alertId=" + alertId
                + " source=" + source
                + " tenant=" + safeTenant(tenantId)
                + " defaultSchemaFallback=" + normalizedDefaultSchema());
        return new ResolvedAsyncTenant(tenantId, source);
    }

    private void logCreateTenantDebug(String phase, String alertId) {
        System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] createAlert " + phase
                + " alertId=" + safeAlertId(alertId)
                + " requestContextActive=" + requestContextActive()
                + " tenantPresent=" + (currentTenantId() != null)
                + " tenant=" + safeTenant(currentTenantId())
                + " defaultSchema=" + normalizedDefaultSchema()
                + " x-wtf-profile=<not-accessed>"
                + " securityPrincipal=<not-accessed>");
    }

    private String currentTenantId() {
        if (tenantContext == null) {
            return null;
        }
        String tenantId = tenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? null : tenantId;
    }

    private void logHibernateTenantResolution(String phase, String alertId, String expectedTenant) {
        String resolverClass = "<unavailable>";
        String resolvedTenant = null;
        String source = "none";
        RuntimeException resolverError = null;
        if (tenantResolver != null && !tenantResolver.isUnsatisfied() && !tenantResolver.isAmbiguous()) {
            try {
                TenantResolver resolver = tenantResolver.get();
                resolverClass = resolver.getClass().getName();
                resolvedTenant = resolver.resolveTenantId();
                source = "TenantResolver.resolveTenantId";
            } catch (RuntimeException ex) {
                resolverError = ex;
                source = "TenantResolver.error";
            }
        }
        System.out.println("[IIA][HIBERNATE_TENANT_DEBUG] phase=" + phase
                + " alertId=" + alertId
                + " resolverClass=" + resolverClass
                + " method=resolveTenantId"
                + " requestContextActive=" + requestContextActive()
                + " transactionActive=" + transactionActive()
                + " thread=" + Thread.currentThread().getName()
                + " tenantContextTenant=" + safeTenant(currentTenantId())
                + " expectedTenant=" + safeTenant(expectedTenant)
                + " hibernateTenant=" + safeTenant(resolvedTenant)
                + " source=" + source
                + " defaultSchema=" + safeTenant(normalizedDefaultSchema())
                + " x-wtf-profile=<not-accessed>"
                + " resolverReads=FND TenantContext first, then RoutingContext tenant-id, then default schema");
        if (resolverError != null) {
            System.out.println("[IIA][HIBERNATE_TENANT_DEBUG] resolver error"
                    + " alertId=" + alertId
                    + " exceptionClass=" + resolverError.getClass().getName()
                    + " exceptionMessage=" + resolverError.getMessage());
        }
        if (currentTenantId() != null && resolvedTenant == null) {
            System.out.println("[IIA][HIBERNATE_TENANT_DEBUG] direct resolver probe did not return a tenant; repository persist will rely on active FND tenant context"
                    + " alertId=" + alertId
                    + " tenantContextTenant=" + safeTenant(currentTenantId())
                    + " requestContextActive=" + requestContextActive()
                    + " transactionActive=" + transactionActive());
        }
    }

    private String normalizedDefaultSchema() {
        return defaultSchema == null || defaultSchema.isBlank() ? null : defaultSchema.trim();
    }

    private String safeTenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? "<none>" : tenantId;
    }

    private String safeAlertId(String alertId) {
        return alertId == null || alertId.isBlank() ? "<not-created-yet>" : alertId;
    }

    private boolean requestContextActive() {
        try {
            return io.quarkus.arc.Arc.container().requestContext().isActive();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean transactionActive() {
        if (transactionManager == null) {
            return false;
        }
        try {
            return transactionManager.getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException ex) {
            return false;
        }
    }

    private record ResolvedAsyncTenant(String tenantId, String source) {
    }

    private boolean isTenantContextFailure(String message) {
        return message != null
                && (message.contains("tenant identifier")
                || message.contains("tenant context")
                || message.contains("multi-tenancy"));
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
