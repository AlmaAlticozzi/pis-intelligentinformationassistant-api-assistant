package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationPromptBuilder;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationLlmResponseParser;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertVerificationOutcomeValidator;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationMainEventIntent;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRole;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationRelation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationPolarity;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingLocation;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingMainEvent;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertRouteUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationUnderstandingResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertLocationUnderstandingService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertVerificationService;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertMonitoringScope;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertEventPhase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertEventWordingClassifier;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertDelayEventTypeNormalizer;
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
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationPreviewResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRuntimeMetadata;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummaryListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertTechnicalSpecificationResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertTechnicalSpecificationUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AlertRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
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
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationContext;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location.ScheduledServiceDataLocationResolutionService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

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
    AlertRouteUnderstandingService alertRouteUnderstandingService;

    @Inject
    SimpleAlertLocationMentionExtractor alertLocationMentionExtractor;

    @Inject
    AlertLocationResolverService alertLocationResolverService;

    @Inject
    AlertLocationUnderstandingService alertLocationUnderstandingService;

    @Inject
    ScheduledAlertLocationUnderstandingService scheduledAlertLocationUnderstandingService;

    @Inject
    ScheduledServiceDataLocationResolutionService scheduledServiceDataLocationResolutionService;

    @Inject
    ScheduledAlertVerificationService scheduledAlertVerificationService;

    @Inject
    AlertLocationTargetFieldMapper alertLocationTargetFieldMapper;

    @Inject
    AlertEventWordingClassifier alertEventWordingClassifier;

    @Inject
    Instance<LlmGateway> llmGateway;

    @Inject
    AiConfiguration aiConfiguration;

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

    @Inject
    AlertTechnicalSpecificationManualValidator alertTechnicalSpecificationManualValidator;

    /**
     * Development safety valve for non-conforming LLM JSON. Production should set this to false.
     */
    @ConfigProperty(name = "iia.alert-verification.fallback-on-invalid-llm", defaultValue = "false")
    boolean fallbackOnInvalidLlm;

    @ConfigProperty(name = "iia.alert-verification.location-understanding.enabled", defaultValue = "false")
    boolean locationUnderstandingEnabled;

    @ConfigProperty(name = "iia.alert.scheduled-verify.enabled", defaultValue = "false")
    boolean scheduledVerifyEnabled;

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

    public Optional<AlertTechnicalSpecificationResponse> getAlertTechnicalSpecification(String alertId) {
        System.out.println("[IIA][ALERT_TECH_SPEC_GET][START] alertId=" + alertId);
        Optional<Alert> maybeAlert = alertRepository.findAlertForTechnicalSpecification(alertId);
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        Alert alert = maybeAlert.get();
        String status = alert.getSglStatus() == null ? null : alert.getSglStatus().getSglStatus();
        String verificationStatus = alert.getSglVerificationstatus() == null
                ? null
                : alert.getSglVerificationstatus().getSglVerificationstatus();
        boolean deleted = alert.getDtDeletedat() != null || AlertStatus.DELETED.toString().equals(status);
        System.out.println("[IIA][ALERT_TECH_SPEC_GET][LOADED] alertId=" + alertId
                + " status=" + status
                + " verificationStatus=" + verificationStatus
                + " deleted=" + deleted);

        if (deleted) {
            throw new AlertTechnicalSpecificationRejectedException(
                    AlertTechnicalSpecificationRejectedException.Reason.DELETED);
        }
        if (!AlertStatus.VERIFIED.toString().equals(status)
                || !AlertVerificationStatus.VERIFIED.toString().equals(verificationStatus)) {
            throw new AlertTechnicalSpecificationRejectedException(
                    AlertTechnicalSpecificationRejectedException.Reason.NOT_VERIFIED);
        }

        Map<String, Object> technicalSpecification = technicalSpecificationAsMap(alert.getJsnTechnicalspecification());
        String interpreterType = alert.getSglInterpretertype() == null
                ? null
                : alert.getSglInterpretertype().getSglInterpretertype();
        boolean edited = Boolean.TRUE.equals(alert.getFlgTechnicalspecificationedited());
        System.out.println("[IIA][ALERT_TECH_SPEC_GET][OK] alertId=" + alertId
                + " interpreterType=" + interpreterType
                + " edited=" + edited);

        return Optional.of(toTechnicalSpecificationResponse(alert, technicalSpecification, List.of(), null));
    }

    public Optional<AlertTechnicalSpecificationResponse> updateAlertTechnicalSpecification(
            String alertId,
            AlertTechnicalSpecificationUpdateRequest request) {
        System.out.println("[IIA][ALERT_TECH_SPEC_PUT][START] alertId=" + alertId);
        Optional<Alert> maybeAlert = alertRepository.findAlertForTechnicalSpecification(alertId);
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        Alert alert = maybeAlert.get();
        String status = alert.getSglStatus() == null ? null : alert.getSglStatus().getSglStatus();
        String verificationStatus = alert.getSglVerificationstatus() == null
                ? null
                : alert.getSglVerificationstatus().getSglVerificationstatus();
        boolean deleted = alert.getDtDeletedat() != null || AlertStatus.DELETED.toString().equals(status);
        boolean wasEnabled = Boolean.TRUE.equals(alert.getFlgEnabled());
        System.out.println("[IIA][ALERT_TECH_SPEC_PUT][LOADED] alertId=" + alertId
                + " status=" + status
                + " verificationStatus=" + verificationStatus
                + " enabled=" + alert.getFlgEnabled());

        if (deleted) {
            throw new AlertTechnicalSpecificationRejectedException(
                    AlertTechnicalSpecificationRejectedException.Reason.DELETED);
        }
        if (AlertStatus.VERIFYING.toString().equals(status)) {
            throw new AlertTechnicalSpecificationRejectedException(
                    AlertTechnicalSpecificationRejectedException.Reason.CONCURRENT_UPDATE);
        }
        if (!AlertStatus.VERIFIED.toString().equals(status)
                || !AlertVerificationStatus.VERIFIED.toString().equals(verificationStatus)) {
            throw new AlertTechnicalSpecificationRejectedException(
                    AlertTechnicalSpecificationRejectedException.Reason.NOT_VERIFIED);
        }

        Map<String, Object> technicalSpecification = normalizeTechnicalSpecificationRequest(request);
        String requestedInterpreterType = technicalSpecification.get("interpreterType") == null
                ? null
                : String.valueOf(technicalSpecification.get("interpreterType"));
        System.out.println("[IIA][ALERT_TECH_SPEC_PUT][VALIDATION_CONTEXT] purpose=MANUAL_TECHNICAL_SPECIFICATION_UPDATE interpreterType="
                + requestedInterpreterType);
        AlertTechnicalSpecificationManualValidator.ValidationResult validation =
                alertTechnicalSpecificationManualValidator.validate(technicalSpecification);
        if (!validation.valid()) {
            System.out.println("[IIA][ALERT_TECH_SPEC_PUT][MANUAL_VALIDATION_FAILED] alertId=" + alertId
                    + " reason=" + validation.reason());
            AlertTechnicalSpecificationRejectedException.Reason reason =
                    validation.failureKind() == AlertTechnicalSpecificationManualValidator.FailureKind.UNSUPPORTED_SPECIFICATION
                            ? AlertTechnicalSpecificationRejectedException.Reason.UNSUPPORTED_TECHNICAL_SPECIFICATION
                            : AlertTechnicalSpecificationRejectedException.Reason.INVALID_TECHNICAL_SPECIFICATION;
            throw new AlertTechnicalSpecificationRejectedException(reason);
        }
        System.out.println("[IIA][ALERT_TECH_SPEC_PUT][MANUAL_VALIDATION_OK] alertId=" + alertId
                + " interpreterType=" + validation.interpreterType());

        Alert updatedAlert = alertRepository.replaceTechnicalSpecificationManually(alertId, technicalSpecification)
                .orElseThrow(() -> new IllegalStateException("Alert disappeared during technical specification update."));
        List<String> warnings = new ArrayList<>();
        warnings.add("Technical specification was replaced manually by an expert user; backend performed governance validation but did not reinterpret the original prompt.");
        if (wasEnabled) {
            warnings.add("Alert was disabled after manual technical specification replacement and must be explicitly enabled again.");
        }
        System.out.println("[IIA][ALERT_TECH_SPEC_PUT][SAVED] alertId=" + alertId
                + " edited=true"
                + " disabled=" + wasEnabled
                + " version=" + updatedAlert.getNumVersion());

        return Optional.of(toTechnicalSpecificationResponse(updatedAlert, technicalSpecification, warnings, false));
    }

    private Map<String, Object> technicalSpecificationAsMap(Object rawTechnicalSpecification) {
        if (rawTechnicalSpecification == null) {
            throw invalidTechnicalSpecification();
        }

        Object parsed = rawTechnicalSpecification;
        if (rawTechnicalSpecification instanceof String text) {
            if (text.isBlank()) {
                throw invalidTechnicalSpecification();
            }
            try {
                parsed = OBJECT_MAPPER.readValue(text, Object.class);
            } catch (JsonProcessingException ex) {
                throw invalidTechnicalSpecification();
            }
        }

        if (!(parsed instanceof Map<?, ?> map) || map.isEmpty()) {
            throw invalidTechnicalSpecification();
        }
        return OBJECT_MAPPER.convertValue(map, new TypeReference<Map<String, Object>>() {
        });
    }

    private AlertTechnicalSpecificationRejectedException invalidTechnicalSpecification() {
        return new AlertTechnicalSpecificationRejectedException(
                AlertTechnicalSpecificationRejectedException.Reason.INVALID_TECHNICAL_SPECIFICATION);
    }

    private Map<String, Object> normalizeTechnicalSpecificationRequest(AlertTechnicalSpecificationUpdateRequest request) {
        if (request == null
                || request.getTechnicalSpecification() == null
                || request.getTechnicalSpecification().isEmpty()) {
            throw invalidTechnicalSpecification();
        }
        return OBJECT_MAPPER.convertValue(request.getTechnicalSpecification(), new TypeReference<Map<String, Object>>() {
        });
    }

    private AlertTechnicalSpecificationResponse toTechnicalSpecificationResponse(
            Alert alert,
            Map<String, Object> technicalSpecification,
            List<String> warnings,
            Boolean agentBlueprintPreviewRegenerated) {
        String status = alert.getSglStatus() == null ? null : alert.getSglStatus().getSglStatus();
        String verificationStatus = alert.getSglVerificationstatus() == null
                ? null
                : alert.getSglVerificationstatus().getSglVerificationstatus();
        String interpreterType = alert.getSglInterpretertype() == null
                ? null
                : alert.getSglInterpretertype().getSglInterpretertype();
        AlertTechnicalSpecificationResponse response = new AlertTechnicalSpecificationResponse()
                .alert(new AlertReference()
                        .id(alert.getCodAlert())
                        .name(alert.getDscName()))
                .status(AlertStatus.fromString(status))
                .verificationStatus(AlertVerificationStatus.fromString(verificationStatus))
                .technicalSpecificationEdited(Boolean.TRUE.equals(alert.getFlgTechnicalspecificationedited()))
                .technicalSpecification(technicalSpecification)
                .agentBlueprintPreviewRegenerated(agentBlueprintPreviewRegenerated)
                .warnings(warnings == null ? List.of() : warnings);

        if (interpreterType != null) {
            response.interpreterType(AlertInterpreterType.fromString(interpreterType));
        }
        response.inputModel(alert.getDscInputmodel());
        response.outputModel(alert.getDscOutputmodel());
        return response;
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

        AlertVerificationOutcome outcome = verifyAlertOutcome(alertId, promptData.get());
        System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] persisting normal outcome alertId=" + alertId
                + " decision=" + outcome.decision()
                + " method=alertRepository.verifyAlert");
        return persistVerificationOutcomeInTransaction(alertId, request, outcome);
    }

    AlertVerificationOutcome verifyAlertOutcome(String alertId, AlertVerificationPromptData promptData) {
        AlertRouteUnderstandingResult route = alertRouteUnderstandingService == null
                ? null
                : alertRouteUnderstandingService.understand(promptData);
        if (route != null && route.decision() != AlertRouteDecision.ROUTED) {
            AlertVerificationOutcome routedRejection = routeRejectedOutcome(route, aiConfiguration.provider(), aiConfiguration.alertVerify().model());
            System.out.println("[IIA][ALERT_VERIFY] route REJECTED -> reject before technical verification");
            return routedRejection;
        }
        if (route != null && route.interpreterType() == AlertRouteInterpreterType.SCHEDULED_INTERPRETER) {
            return verifyScheduledRouteOutcome(alertId, promptData, route);
        }
        if (route != null && route.interpreterType() == AlertRouteInterpreterType.EVENT_INTERPRETER) {
            System.out.println("[IIA][ALERT_VERIFY] route EVENT_INTERPRETER -> continue existing event verify");
        }
        AlertVerificationPromptData enrichedPromptData = withLocationResolutionContext(alertId, promptData);
        LlmRequest promptRequest = alertVerificationPromptBuilder.build(enrichedPromptData);
        System.out.println("[IIA][ALERT_VERIFY][CONFIG] fallbackOnInvalidLlm=" + fallbackOnInvalidLlm);
        System.out.println("[IIA][ALERT_VERIFY][PROMPT] Built ALERT_VERIFY prompt for alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][PROMPT][SYSTEM] " + promptRequest.systemPrompt());
        System.out.println("[IIA][ALERT_VERIFY][PROMPT][USER] " + promptRequest.userPrompt());

        AlertVerificationResolution resolution = resolveVerificationOutcome(alertId, enrichedPromptData, promptRequest);
        System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] parser outcome alertId=" + alertId
                + " decision=" + resolution.outcome().decision()
                + " parseableLlm=" + resolution.parseableLlm());
        AlertVerificationOutcome outcome = validateOutcomeWithLocationContext(resolution.outcome(), enrichedPromptData);
        System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] validator outcome alertId=" + alertId
                + " decision=" + outcome.decision()
                + " rejectedReason=" + outcome.rejectedReason());
        if (resolution.parseableLlm() && shouldFallbackOnInvalidLlm(resolution.outcome(), outcome)) {
            System.out.println("[IIA][ALERT_VERIFY][VALIDATION] LLM outcome rejected by backend validation alertId="
                    + alertId + ", fallbackOnInvalidLlm=" + fallbackOnInvalidLlm);
        }
        if (resolution.parseableLlm() && fallbackOnInvalidLlm && shouldFallbackOnInvalidLlm(resolution.outcome(), outcome)) {
            System.out.println("[IIA][ALERT_VERIFY][LLM] Falling back to deterministic mock engine because backend validation rejected LLM output");
            outcome = validateOutcomeWithLocationContext(
                    alertVerificationMockEngine.verify(alertId, enrichedPromptData.prompt())
                            .withAdditionalWarning("LLM response was empty, invalid or rejected. Deterministic mock fallback was used because fallback-on-invalid-llm is enabled."),
                    enrichedPromptData);
            System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] fallback validator outcome alertId=" + alertId
                    + " decision=" + outcome.decision());
        }
        outcome = applyLocationConfidenceAdjustment(outcome, enrichedPromptData.locationResolutionContext());
        return outcome;
    }

    private AlertVerificationOutcome validateOutcomeWithLocationContext(
            AlertVerificationOutcome outcome,
            AlertVerificationPromptData enrichedPromptData) {
        outcome = normalizeSingleValueInOperators(outcome, enrichedPromptData.alertId());
        outcome = normalizeRequirementCoverage(outcome, enrichedPromptData.alertId());
        outcome = normalizeExpectedMainEventType(outcome, enrichedPromptData);
        AlertVerificationOutcome validated = alertVerificationOutcomeValidator.validate(
                outcome,
                enrichedPromptData.prompt(),
                enrichedPromptData.locationResolutionContext());
        if (validated == null) {
            validated = alertVerificationOutcomeValidator.validate(outcome, enrichedPromptData.prompt());
        }
        return validated;
    }

    AlertVerificationOutcome normalizeExpectedMainEventType(
            AlertVerificationOutcome outcome,
            AlertVerificationPromptData promptData) {
        if (outcome == null
                || outcome.decision() != AlertVerificationDecision.VERIFIED
                || promptData == null
                || promptData.locationResolutionContext() == null) {
            return outcome;
        }
        String expected = authoritativeEventType(promptData.locationResolutionContext());
        if (expected == null || !isSupportedAuthoritativeEvent(expected)) {
            return outcome;
        }
        DelayThreshold delayThreshold = delayThreshold(promptData.locationResolutionContext());
        Map<String, Object> technicalSpecification = mutableMap(outcome.technicalSpecification());
        Map<String, Object> agentBlueprintPreview = mutableMap(outcome.agentBlueprintPreview());
        if (technicalSpecification == null) {
            return outcome;
        }
        Object technicalCondition = technicalSpecification.get("condition");
        if (isGenericDelayEvent(expected)) {
            AlertVerificationOutcome normalized = normalizeGenericDelayEventType(
                    outcome,
                    promptData,
                    technicalSpecification,
                    agentBlueprintPreview,
                    technicalCondition);
            return normalizeMissingDelayThresholdPredicate(
                    normalized,
                    promptData,
                    technicalSpecification,
                    agentBlueprintPreview,
                    technicalCondition,
                    expected,
                    delayThreshold);
        }
        MainEventNormalization normalization = normalizeMainEventLeaf(technicalCondition, expected);
        boolean inserted = false;
        if (!normalization.changed()
                && !hasMainEventLeaf(technicalCondition)
                && hasCoherentPredicateForExpectedEvent(technicalCondition, expected)) {
            inserted = insertMainEventLeaf(technicalCondition, expected);
            if (inserted) {
                normalization = new MainEventNormalization(true, null, null, List.of());
            }
        }
        if (!normalization.changed()) {
            return normalizeMissingDelayThresholdPredicate(
                    outcome,
                    promptData,
                    technicalSpecification,
                    agentBlueprintPreview,
                    technicalCondition,
                    expected,
                    delayThreshold);
        }
        if (inserted) {
            insertBlueprintMainEventLeaf(agentBlueprintPreview, expected);
            System.out.println("[IIA][ALERT_VERIFY][MAIN_EVENT_NORMALIZATION] alertId="
                    + promptData.alertId()
                    + " expectedMainEventType=" + expected
                    + " reason=missing-eventsType-inserted-from-expected-main-event-type");
        } else {
            normalizeBlueprintMainEventLeaf(agentBlueprintPreview, expected);
            if (isOperationalMainEventType(expected)
                    && (isDepartureDelayEvent(normalization.originalValue())
                    || isArrivalDelayEvent(normalization.originalValue()))) {
                System.out.println("[IIA][ALERT_VERIFY][MAIN_EVENT_NORMALIZATION] alertId="
                        + promptData.alertId()
                        + " reason=event-primary-with-delay-overrides-delay-event-type"
                        + " expectedMainEventType=" + expected
                        + " originalEventsType=" + normalization.originalValue());
            } else {
                System.out.println("[IIA][ALERT_VERIFY][MAIN_EVENT_NORMALIZATION] alertId="
                        + promptData.alertId()
                        + " expectedMainEventType=" + expected
                        + " originalOperator=" + normalization.originalOperator()
                        + " originalValue=" + normalization.originalValue()
                        + " originalValues=" + normalization.originalValues()
                        + " normalizedOperator=CONTAINS"
                        + " normalizedValue=" + expected
                        + " normalizedValues=[]"
                        + " reason=EXPECTED_MAIN_EVENT_TYPE is authoritative for the current ServiceData event phase");
            }
        }
        AlertVerificationOutcome rebuilt = new AlertVerificationOutcome(
                outcome.decision(),
                outcome.summary(),
                outcome.rejectedReason(),
                outcome.confidence(),
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
                technicalSpecification,
                agentBlueprintPreview,
                outcome.requirementCoverage(),
                outcome.warnings(),
                outcome.safetyChecks());
        return normalizeMissingDelayThresholdPredicate(
                rebuilt,
                promptData,
                technicalSpecification,
                agentBlueprintPreview,
                technicalCondition,
                expected,
                delayThreshold);
    }

    private AlertVerificationOutcome normalizeGenericDelayEventType(
            AlertVerificationOutcome outcome,
            AlertVerificationPromptData promptData,
            Map<String, Object> technicalSpecification,
            Map<String, Object> agentBlueprintPreview,
            Object technicalCondition) {
        if (hasGenericDelayEventLeaf(technicalCondition) || !hasGenericDelayPredicate(technicalCondition)) {
            return outcome;
        }
        boolean inserted = insertGenericDelayEventLeaf(technicalCondition);
        if (!inserted) {
            return outcome;
        }
        if (agentBlueprintPreview != null) {
            Object parameters = agentBlueprintPreview.get("parameters");
            if (parameters instanceof Map<?, ?> parametersMap) {
                insertGenericDelayEventLeaf(parametersMap.get("condition"));
            }
        }
        System.out.println("[IIA][ALERT_VERIFY][DELAY_EVENT_NORMALIZATION] alertId=" + promptData.alertId()
                + " reason=generic-delay-eventsType-inserted"
                + " delayEventType=BOTH");
        return new AlertVerificationOutcome(
                outcome.decision(),
                outcome.summary(),
                outcome.rejectedReason(),
                outcome.confidence(),
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
                technicalSpecification,
                agentBlueprintPreview,
                outcome.requirementCoverage(),
                outcome.warnings(),
                outcome.safetyChecks());
    }

    private AlertVerificationOutcome normalizeMissingDelayThresholdPredicate(
            AlertVerificationOutcome outcome,
            AlertVerificationPromptData promptData,
            Map<String, Object> technicalSpecification,
            Map<String, Object> agentBlueprintPreview,
            Object technicalCondition,
            String expected,
            DelayThreshold delayThreshold) {
        if (delayThreshold == null || !supportsDelayThresholdPredicate(expected)) {
            return outcome;
        }
        if (hasRequiredDelayThresholdPredicate(technicalCondition, expected)) {
            return outcome;
        }
        boolean inserted = insertDelayThresholdPredicate(technicalCondition, expected, delayThreshold);
        if (!inserted) {
            return outcome;
        }
        if (agentBlueprintPreview != null) {
            Object parameters = agentBlueprintPreview.get("parameters");
            if (parameters instanceof Map<?, ?> parametersMap) {
                insertDelayThresholdPredicate(parametersMap.get("condition"), expected, delayThreshold);
            }
        }
        System.out.println("[IIA][ALERT_VERIFY][DELAY_THRESHOLD_NORMALIZATION] alertId=" + promptData.alertId()
                + " reason=missing-delay-threshold-predicate-inserted"
                + " delayEventType=" + expected
                + " operator=" + delayThreshold.operator()
                + " value=" + delayThreshold.value());
        return rebuildOutcome(outcome, technicalSpecification, agentBlueprintPreview, outcome.requirementCoverage());
    }

    AlertVerificationOutcome normalizeSingleValueInOperators(AlertVerificationOutcome outcome, String alertId) {
        if (outcome == null || outcome.decision() != AlertVerificationDecision.VERIFIED) {
            return outcome;
        }
        Map<String, Object> technicalSpecification = mutableMap(outcome.technicalSpecification());
        Map<String, Object> agentBlueprintPreview = mutableMap(outcome.agentBlueprintPreview());
        if (technicalSpecification == null) {
            return outcome;
        }
        boolean changed = normalizeConditionNode(technicalSpecification.get("condition"), alertId, null, true);
        if (agentBlueprintPreview != null) {
            Object parameters = agentBlueprintPreview.get("parameters");
            if (parameters instanceof Map<?, ?> parametersMap) {
                changed = normalizeConditionNode(parametersMap.get("condition"), alertId, null, true) || changed;
            }
        }
        if (!changed) {
            return outcome;
        }
        return rebuildOutcome(outcome, technicalSpecification, agentBlueprintPreview, outcome.requirementCoverage());
    }

    AlertVerificationOutcome normalizeRequirementCoverage(AlertVerificationOutcome outcome, String alertId) {
        if (outcome == null || outcome.decision() != AlertVerificationDecision.VERIFIED) {
            return outcome;
        }
        Map<String, Object> coverage = mutableMap(outcome.requirementCoverage());
        if (coverage == null || !Boolean.FALSE.equals(coverage.get("allRequiredRequirementsMapped"))) {
            return outcome;
        }
        if (containsRequiredUnmappableRequirement(coverage, outcome.technicalSpecification())) {
            return outcome;
        }
        coverage.put("allRequiredRequirementsMapped", true);
        System.out.println("[IIA][ALERT_VERIFY][COVERAGE_NORMALIZATION] alertId=" + alertId
                + " reason=backend-condition-and-location-coverage-valid"
                + " originalAllRequiredRequirementsMapped=false"
                + " normalizedAllRequiredRequirementsMapped=true");
        return rebuildOutcome(outcome, outcome.technicalSpecification(), outcome.agentBlueprintPreview(), coverage);
    }

    private void normalizeBlueprintMainEventLeaf(Map<String, Object> agentBlueprintPreview, String expected) {
        if (agentBlueprintPreview == null) {
            return;
        }
        Object parameters = agentBlueprintPreview.get("parameters");
        if (parameters instanceof Map<?, ?> parametersMap) {
            Object condition = parametersMap.get("condition");
            normalizeMainEventLeaf(condition, expected);
        }
    }

    private void insertBlueprintMainEventLeaf(Map<String, Object> agentBlueprintPreview, String expected) {
        if (agentBlueprintPreview == null) {
            return;
        }
        Object parameters = agentBlueprintPreview.get("parameters");
        if (parameters instanceof Map<?, ?> parametersMap) {
            insertMainEventLeaf(parametersMap.get("condition"), expected);
        }
    }

    private MainEventNormalization normalizeMainEventLeaf(Object node, String expected) {
        if (node instanceof Map<?, ?> rawMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) rawMap;
            String field = stringValue(map.get("field"));
            String operator = stringValue(map.get("operator"));
            if ("payload.ongroundServiceEvent.eventsType".equals(field) && "CONTAINS".equals(operator)) {
                String originalValue = stringValue(map.get("value"));
                if (originalValue != null
                        && !expected.equals(originalValue)
                        && sameArrivalDepartureDomain(expected, originalValue)) {
                    map.put("operator", "CONTAINS");
                    map.put("value", expected);
                    map.remove("values");
                    return new MainEventNormalization(true, operator, originalValue, List.of());
                }
                return MainEventNormalization.unchanged();
            }
            for (Object value : map.values()) {
                MainEventNormalization normalization = normalizeMainEventLeaf(value, expected);
                if (normalization.changed()) {
                    return normalization;
                }
            }
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                MainEventNormalization normalization = normalizeMainEventLeaf(item, expected);
                if (normalization.changed()) {
                    return normalization;
                }
            }
        }
        return MainEventNormalization.unchanged();
    }

    private boolean hasMainEventLeaf(Object node) {
        if (node instanceof Map<?, ?> map) {
            if ("payload.ongroundServiceEvent.eventsType".equals(stringValue(map.get("field")))) {
                return true;
            }
            return map.values().stream().anyMatch(this::hasMainEventLeaf);
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (hasMainEventLeaf(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean insertMainEventLeaf(Object node, String expected) {
        if (!(node instanceof Map<?, ?> rawMap)) {
            return false;
        }
        Map<String, Object> map = (Map<String, Object>) rawMap;
        Map<String, Object> eventLeaf = new java.util.LinkedHashMap<>();
        eventLeaf.put("field", "payload.ongroundServiceEvent.eventsType");
        eventLeaf.put("operator", "CONTAINS");
        eventLeaf.put("value", expected);
        Object all = map.get("all");
        if (all instanceof List<?> allList) {
            List<Object> mutableAll = new ArrayList<>();
            mutableAll.add(eventLeaf);
            mutableAll.addAll(allList);
            map.put("all", mutableAll);
            return true;
        }
        if ("SERVICE_DATA_FIELD_MATCH".equals(stringValue(map.get("type")))) {
            Map<String, Object> original = new java.util.LinkedHashMap<>(map);
            map.clear();
            map.put("type", "SERVICE_DATA_FIELD_MATCH");
            map.put("all", new ArrayList<>(List.of(eventLeaf, original)));
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean insertGenericDelayEventLeaf(Object node) {
        if (!(node instanceof Map<?, ?> rawMap)) {
            return false;
        }
        Map<String, Object> map = (Map<String, Object>) rawMap;
        Map<String, Object> eventLeaf = new java.util.LinkedHashMap<>();
        eventLeaf.put("field", "payload.ongroundServiceEvent.eventsType");
        eventLeaf.put("operator", "CONTAINS_ANY");
        eventLeaf.put("values", List.of("ARRIVAL_DELAY", "DEPARTURE_DELAY"));
        Object all = map.get("all");
        if (all instanceof List<?> allList) {
            List<Object> mutableAll = new ArrayList<>();
            mutableAll.add(eventLeaf);
            mutableAll.addAll(allList);
            map.put("all", mutableAll);
            return true;
        }
        if ("SERVICE_DATA_FIELD_MATCH".equals(stringValue(map.get("type")))) {
            Map<String, Object> original = new java.util.LinkedHashMap<>(map);
            map.clear();
            map.put("type", "SERVICE_DATA_FIELD_MATCH");
            map.put("all", new ArrayList<>(List.of(eventLeaf, original)));
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean insertDelayThresholdPredicate(Object node, String expected, DelayThreshold threshold) {
        if (!(node instanceof Map<?, ?> rawMap)) {
            return false;
        }
        Map<String, Object> map = (Map<String, Object>) rawMap;
        Map<String, Object> delayPredicate = delayThresholdPredicate(expected, threshold);
        if (delayPredicate.isEmpty()) {
            return false;
        }
        Object all = map.get("all");
        if (all instanceof List<?> allList) {
            List<Object> mutableAll = new ArrayList<>(allList);
            mutableAll.add(delayPredicate);
            map.put("all", mutableAll);
            return true;
        }
        if ("SERVICE_DATA_FIELD_MATCH".equals(stringValue(map.get("type")))) {
            Map<String, Object> original = new java.util.LinkedHashMap<>(map);
            map.clear();
            map.put("type", "SERVICE_DATA_FIELD_MATCH");
            map.put("all", new ArrayList<>(List.of(original, delayPredicate)));
            return true;
        }
        return false;
    }

    private Map<String, Object> delayThresholdPredicate(String expected, DelayThreshold threshold) {
        if (isDepartureDelayEvent(expected)) {
            return anyStopPointJourneyDetailPredicate("departureDelay.delay", threshold);
        }
        if (isArrivalDelayEvent(expected)) {
            return anyStopPointJourneyDetailPredicate("arrivalDelay.delay", threshold);
        }
        if (isDepartureEvent(expected)) {
            return anyStopPointJourneyDetailPredicate("departureDelay.delay", threshold);
        }
        if (isArrivalEvent(expected)) {
            return anyStopPointJourneyDetailPredicate("arrivalDelay.delay", threshold);
        }
        if (isGenericDelayEvent(expected)) {
            return Map.of("anyElement", Map.of(
                    "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                    "conditions", Map.of("any", List.of(
                            delayThresholdLeaf("arrivalDelay.delay", threshold),
                            delayThresholdLeaf("departureDelay.delay", threshold)))));
        }
        return Map.of();
    }

    private Map<String, Object> anyStopPointJourneyDetailPredicate(String field, DelayThreshold threshold) {
        return Map.of("anyElement", Map.of(
                "path", "payload.stopPointJourney.stopPointsJourneyDetails[]",
                "conditions", delayThresholdLeaf(field, threshold)));
    }

    private Map<String, Object> delayThresholdLeaf(String field, DelayThreshold threshold) {
        return Map.of(
                "field", field,
                "operator", threshold.operator(),
                "value", threshold.value());
    }

    private boolean hasRequiredDelayThresholdPredicate(Object node, String expected) {
        if (isDepartureDelayEvent(expected)) {
            return hasDelayThresholdField(node, "departureDelay.delay");
        }
        if (isArrivalDelayEvent(expected)) {
            return hasDelayThresholdField(node, "arrivalDelay.delay");
        }
        if (isDepartureEvent(expected)) {
            return hasDelayThresholdField(node, "departureDelay.delay");
        }
        if (isArrivalEvent(expected)) {
            return hasDelayThresholdField(node, "arrivalDelay.delay");
        }
        if (isGenericDelayEvent(expected)) {
            return hasDelayThresholdField(node, "arrivalDelay.delay")
                    && hasDelayThresholdField(node, "departureDelay.delay");
        }
        return true;
    }

    private boolean hasDelayThresholdField(Object node, String fieldFragment) {
        if (node instanceof Map<?, ?> map) {
            String field = stringValue(map.get("field"));
            if (field != null && field.contains(fieldFragment)) {
                return true;
            }
            return map.values().stream().anyMatch(value -> hasDelayThresholdField(value, fieldFragment));
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (hasDelayThresholdField(item, fieldFragment)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasCoherentPredicateForExpectedEvent(Object node, String expected) {
        if (node instanceof Map<?, ?> map) {
            String field = stringValue(map.get("field"));
            if (field != null && coherentFieldForExpectedEvent(field, expected)) {
                return true;
            }
            return map.values().stream().anyMatch(value -> hasCoherentPredicateForExpectedEvent(value, expected));
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (hasCoherentPredicateForExpectedEvent(item, expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean coherentFieldForExpectedEvent(String field, String expected) {
        if (isGenericDelayEvent(expected)) {
            return field.contains("departureDelay.") || field.contains("arrivalDelay.");
        }
        if (isDepartureDelayEvent(expected)) {
            return field.contains("departureDelay.");
        }
        if (isArrivalDelayEvent(expected)) {
            return field.contains("arrivalDelay.");
        }
        if (isDepartureEvent(expected)) {
            return field.contains("departureDelay.")
                    || field.contains("DeparturePlatform")
                    || field.contains("departureTime")
                    || field.contains("timetabledCallStart.")
                    || field.contains("callStart.")
                    || field.endsWith(".passingType");
        }
        if (isArrivalEvent(expected)) {
            return field.contains("arrivalDelay.")
                    || field.contains("ArrivalPlatform")
                    || field.contains("arrivalTime")
                    || field.contains("timetabledCallEnd.")
                    || field.contains("callEnd.")
                    || field.endsWith(".passingType");
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean normalizeConditionNode(Object node, String alertId, String currentArrayPath, boolean rootCondition) {
        boolean changed = false;
        if (node instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            if (!rootCondition && map.remove("type") != null) {
                System.out.println("[IIA][ALERT_VERIFY][CONDITION_NORMALIZATION] alertId=" + alertId
                        + " reason=remove-nested-condition-type");
                changed = true;
            }
            String operator = stringValue(map.get("operator"));
            Object rawValues = map.get("values");
            if ("IN".equals(operator) && rawValues instanceof List<?> values && values.size() == 1) {
                Object singleValue = values.getFirst();
                map.put("operator", "EQUALS");
                map.put("value", singleValue);
                map.remove("values");
                System.out.println("[IIA][ALERT_VERIFY][CONDITION_NORMALIZATION] alertId=" + alertId
                        + " reason=single-value-IN-to-EQUALS"
                        + " field=" + stringValue(map.get("field"))
                        + " originalValues=" + values
                        + " normalizedValue=" + singleValue);
                changed = true;
            }
            String nextArrayPath = currentArrayPath;
            Object anyElement = map.get("anyElement");
            if (anyElement instanceof Map<?, ?> anyElementMap) {
                nextArrayPath = resolveConditionPath(currentArrayPath, stringValue(anyElementMap.get("path")));
            }
            for (Object value : map.values()) {
                changed = normalizeConditionNode(value, alertId, nextArrayPath, false) || changed;
            }
        } else if (node instanceof Iterable<?> iterable) {
            if (node instanceof List<?> rawList) {
                List<Object> list = (List<Object>) rawList;
                for (int i = list.size() - 1; i >= 0; i--) {
                    Object item = list.get(i);
                    if (isRedundantTransitPassingType(item, currentArrayPath)) {
                        list.remove(i);
                        System.out.println("[IIA][ALERT_VERIFY][CONDITION_NORMALIZATION] alertId=" + alertId
                                + " reason=remove-redundant-passingType-inside-nextTransitCalls"
                                + " path=" + currentArrayPath);
                        changed = true;
                    }
                }
            }
            for (Object item : iterable) {
                changed = normalizeConditionNode(item, alertId, currentArrayPath, false) || changed;
            }
        }
        return changed;
    }

    private String resolveConditionPath(String parentArrayPath, String rawPath) {
        if (rawPath == null) {
            return parentArrayPath;
        }
        if (rawPath.startsWith("payload.")) {
            return rawPath;
        }
        if (parentArrayPath == null || parentArrayPath.isBlank()) {
            return rawPath;
        }
        return parentArrayPath + "." + rawPath;
    }

    private boolean isRedundantTransitPassingType(Object item, String currentArrayPath) {
        if (!(item instanceof Map<?, ?> map)
                || currentArrayPath == null
                || !currentArrayPath.endsWith("nextTransitCalls[]")) {
            return false;
        }
        return "passingType".equals(stringValue(map.get("field")))
                && "EQUALS".equals(stringValue(map.get("operator")))
                && "TRANSIT".equals(stringValue(map.get("value")));
    }

    private boolean containsRequiredUnmappableRequirement(
            Map<String, Object> coverage,
            Map<String, Object> technicalSpecification) {
        Object requirements = coverage.get("requirements");
        if (!(requirements instanceof List<?> list)) {
            return false;
        }
        boolean hasRouteOrTransitCondition = hasRouteOrTransitCondition(
                technicalSpecification == null ? null : technicalSpecification.get("condition"));
        for (Object item : list) {
            if (item instanceof Map<?, ?> requirement
                    && Boolean.TRUE.equals(requirement.get("required"))
                    && Boolean.FALSE.equals(requirement.get("mappable"))) {
                if (hasRouteOrTransitCondition && isSpuriousRouteTransitRequirement(requirement)) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private boolean isSpuriousRouteTransitRequirement(Map<?, ?> requirement) {
        String text = normalizeText(stringValue(requirement.get("text")));
        String reason = normalizeText(stringValue(requirement.get("reason")));
        return containsRouteTransitMarker(text) || containsRouteTransitMarker(reason);
    }

    private boolean containsRouteTransitMarker(String value) {
        return value != null && (value.contains("main_event_intent")
                || value.contains("route_transit")
                || value.contains("route transit"));
    }

    private AlertVerificationOutcome rebuildOutcome(
            AlertVerificationOutcome outcome,
            Map<String, Object> technicalSpecification,
            Map<String, Object> agentBlueprintPreview,
            Map<String, Object> requirementCoverage) {
        return new AlertVerificationOutcome(
                outcome.decision(),
                outcome.summary(),
                outcome.rejectedReason(),
                outcome.confidence(),
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
                technicalSpecification,
                agentBlueprintPreview,
                requirementCoverage,
                outcome.warnings(),
                outcome.safetyChecks());
    }

    private boolean sameArrivalDepartureDomain(String expected, String actual) {
        return (isDepartureEvent(expected) && isDepartureEvent(actual))
                || (isArrivalEvent(expected) && isArrivalEvent(actual))
                || (isDepartureEvent(expected) && isDepartureDelayEvent(actual))
                || (isArrivalEvent(expected) && isArrivalDelayEvent(actual))
                || (isDepartureDelayEvent(expected) && isDepartureEvent(actual))
                || (isArrivalDelayEvent(expected) && isArrivalEvent(actual));
    }

    private String authoritativeEventType(AlertVerificationLocationContext context) {
        if (isEventPrimaryDelayContext(context)) {
            return nonLocationConstraint(context, "EXPECTED_MAIN_EVENT_TYPE");
        }
        String delayEventType = nonLocationConstraint(context, "DELAY_EVENT_TYPE");
        if (delayEventType != null) {
            String normalized = AlertDelayEventTypeNormalizer.normalize(delayEventType);
            return normalized == null ? delayEventType : normalized;
        }
        return nonLocationConstraint(context, "EXPECTED_MAIN_EVENT_TYPE");
    }

    private boolean isEventPrimaryDelayContext(AlertVerificationLocationContext context) {
        String delayRole = nonLocationConstraint(context, "DELAY_ROLE");
        if ("ACCESSORY_DELAY_PREDICATE".equals(delayRole)) {
            return true;
        }
        String expected = nonLocationConstraint(context, "EXPECTED_MAIN_EVENT_TYPE");
        return isOperationalMainEventType(expected) && hasRequiredMainEventLocation(context);
    }

    private DelayThreshold delayThreshold(AlertVerificationLocationContext context) {
        String raw = nonLocationConstraint(context, "DELAY_THRESHOLD");
        if (raw == null) {
            return null;
        }
        String operator = valueAfterKey(raw, "operator");
        String value = valueAfterKey(raw, "value");
        String unit = valueAfterKey(raw, "unit");
        if (operator == null || value == null) {
            return null;
        }
        try {
            return new DelayThreshold(operator, Integer.parseInt(value), unit == null ? "SECONDS" : unit);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String valueAfterKey(String raw, String key) {
        String normalizedKey = key + "=";
        for (String part : raw.split(";")) {
            String trimmed = part.trim();
            if (trimmed.regionMatches(true, 0, normalizedKey, 0, normalizedKey.length())) {
                return trimmed.substring(normalizedKey.length()).trim().toUpperCase(Locale.ROOT);
            }
        }
        return null;
    }

    private boolean isSupportedAuthoritativeEvent(String value) {
        return isDepartureEvent(value) || isArrivalEvent(value)
                || isDepartureDelayEvent(value) || isArrivalDelayEvent(value)
                || isGenericDelayEvent(value);
    }

    private boolean isOperationalMainEventType(String value) {
        return isDepartureEvent(value) || isArrivalEvent(value);
    }

    private boolean hasRequiredMainEventLocation(AlertVerificationLocationContext context) {
        return context != null && context.resolutions().stream()
                .anyMatch(location -> location.requiredCoverage()
                        && "MAIN_EVENT_LOCATION".equalsIgnoreCase(location.semanticRole()));
    }

    private boolean isDepartureEvent(String value) {
        return "DEPARTING".equals(value) || "DEPARTED".equals(value);
    }

    private boolean isArrivalEvent(String value) {
        return "ARRIVING".equals(value) || "ARRIVED".equals(value);
    }

    private boolean isDepartureDelayEvent(String value) {
        return "DEPARTURE_DELAY".equals(value);
    }

    private boolean isArrivalDelayEvent(String value) {
        return "ARRIVAL_DELAY".equals(value);
    }

    private boolean isGenericDelayEvent(String value) {
        return "BOTH".equals(value) || "GENERIC_DELAY".equals(value);
    }

    private boolean isSupportedDelayEventType(String value) {
        return isDepartureDelayEvent(value) || isArrivalDelayEvent(value) || isGenericDelayEvent(value);
    }

    private boolean supportsDelayThresholdPredicate(String value) {
        return isSupportedDelayEventType(value) || isOperationalMainEventType(value);
    }

    private boolean hasGenericDelayPredicate(Object node) {
        return hasCoherentPredicateForExpectedEvent(node, "BOTH");
    }

    private boolean hasGenericDelayEventLeaf(Object node) {
        if (node instanceof Map<?, ?> map) {
            if ("payload.ongroundServiceEvent.eventsType".equals(stringValue(map.get("field")))) {
                List<String> values = stringList(map.get("values"));
                return values.contains("ARRIVAL_DELAY") && values.contains("DEPARTURE_DELAY");
            }
            return map.values().stream().anyMatch(this::hasGenericDelayEventLeaf);
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (hasGenericDelayEventLeaf(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasRouteOrTransitCondition(Object node) {
        if (node instanceof Map<?, ?> map) {
            String field = stringValue(map.get("field"));
            if (field != null && (field.contains("nextCalls[].stopPoint.")
                    || field.contains("nextTransitCalls[].stopPoint."))) {
                return true;
            }
            Object path = null;
            Object anyElement = map.get("anyElement");
            if (anyElement instanceof Map<?, ?> anyMap) {
                path = anyMap.get("path");
            }
            if (path != null && stringValue(path) != null
                    && (stringValue(path).contains("nextCalls[]") || stringValue(path).contains("nextTransitCalls[]"))
                    && map.values().stream().anyMatch(this::hasStopPointCondition)) {
                return true;
            }
            return map.values().stream().anyMatch(this::hasRouteOrTransitCondition);
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (hasRouteOrTransitCondition(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasStopPointCondition(Object node) {
        if (node instanceof Map<?, ?> map) {
            String field = stringValue(map.get("field"));
            if (field != null && field.contains("stopPoint.")) {
                return true;
            }
            return map.values().stream().anyMatch(this::hasStopPointCondition);
        }
        if (node instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (hasStopPointCondition(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String nonLocationConstraint(AlertVerificationLocationContext context, String type) {
        return context.nonLocationConstraints().stream()
                .filter(constraint -> type.equalsIgnoreCase(constraint.type()))
                .map(AlertVerificationLocationContext.NonLocationConstraint::rawText)
                .map(this::stringValue)
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> mutableMap(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> result.put(key, mutableValue(value)));
        return result;
    }

    private Object mutableValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            map.forEach((key, item) -> result.put(String.valueOf(key), mutableValue(item)));
            return result;
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();
            list.forEach(item -> result.add(mutableValue(item)));
            return result;
        }
        return value;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .map(this::stringValue)
                .filter(item -> item != null && !item.isBlank())
                .toList();
    }

    private record MainEventNormalization(
            boolean changed,
            String originalOperator,
            String originalValue,
            List<String> originalValues) {

        private static MainEventNormalization unchanged() {
            return new MainEventNormalization(false, null, null, List.of());
        }
    }

    private record DelayThreshold(String operator, int value, String unit) {
        private String rawText() {
            return "operator=" + operator + ";value=" + value + ";unit=" + unit;
        }
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
            AlertLocationUnderstandingResult understanding = normalizeLocationUnderstandingRoles(
                    prompt,
                    understandingService.understandLocations(prompt, alertId));
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
            return Optional.of(toPromptLocationContext(understanding, prompt));
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

    private AlertVerificationLocationContext toPromptLocationContext(
            AlertLocationUnderstandingResult understanding,
            String prompt) {
        AlertLocationResolverService resolver = alertLocationResolverService == null
                ? new AlertLocationResolverService()
                : alertLocationResolverService;
        List<AlertLocationUnderstandingLocation> coverageLocations = new ArrayList<>();
        for (AlertLocationUnderstandingLocation location : understanding.locations()) {
            if (isAllLocationsNoOp(location.rawText(), location.normalizedText())) {
                System.out.println("[IIA][ALERT_LOCATION_UNDERSTANDING][LOCATION_SCOPE_NORMALIZATION] "
                        + "reason=all-locations-no-op"
                        + " rawText=" + location.rawText()
                        + " normalizedText=" + location.normalizedText()
                        + " action=removed-from-location-coverage");
                continue;
            }
            coverageLocations.add(location);
        }
        List<AlertLocationMention> mentions = coverageLocations.stream()
                .map(location -> new AlertLocationMention(
                        resolutionText(location),
                        toLegacySemanticRole(location.role()),
                        location.confidence()))
                .toList();
        List<AlertLocationResolution> resolutions = mentions.isEmpty()
                ? List.of()
                : resolver.resolve(mentions);
        List<AlertVerificationLocationContext.LocationResolution> promptResolutions = new ArrayList<>();
        for (int index = 0; index < coverageLocations.size(); index++) {
            AlertLocationUnderstandingLocation location = coverageLocations.get(index);
            AlertLocationResolution resolution = resolutions.get(index);
            promptResolutions.add(toPromptLocationResolution(location, resolution));
        }
        AlertVerificationLocationContext context = new AlertVerificationLocationContext(
                !promptResolutions.isEmpty(),
                promptResolutions,
                semanticNonLocationConstraints(understanding, prompt),
                understanding.warnings());
        System.out.println("[IIA][ALERT_VERIFY][LOCATION_CONTEXT] semanticContext=" + context);
        for (AlertVerificationLocationContext.LocationResolution resolution : context.resolutions()) {
            System.out.println("[IIA][ALERT_VERIFY][LOCATION_CONTEXT][TARGET_FIELD_HINTS] rawText="
                    + resolution.rawText()
                    + " semanticRole=" + resolution.semanticRole()
                    + " relationToMainEvent=" + resolution.relationToMainEvent()
                    + " targetFieldHints=" + resolution.targetFieldHints());
        }
        return context;
    }

    private AlertLocationUnderstandingResult normalizeLocationUnderstandingRoles(
            String prompt,
            AlertLocationUnderstandingResult understanding) {
        if (understanding == null || understanding.locations().isEmpty()) {
            return understanding;
        }
        List<AlertLocationUnderstandingLocation> normalizedLocations = new ArrayList<>();
        boolean changed = false;
        for (AlertLocationUnderstandingLocation location : understanding.locations()) {
            AlertLocationUnderstandingLocation normalized = normalizeLocationRole(prompt, understanding.mainEvent(), location);
            normalizedLocations.add(normalized);
            changed = changed || normalized != location;
        }
        if (!changed) {
            return understanding;
        }
        return new AlertLocationUnderstandingResult(
                understanding.hasLocations(),
                understanding.language(),
                understanding.mainEvent(),
                normalizedLocations,
                understanding.nonLocationConstraints(),
                understanding.warnings());
    }

    private AlertLocationUnderstandingLocation normalizeLocationRole(
            String prompt,
            AlertLocationUnderstandingMainEvent mainEvent,
            AlertLocationUnderstandingLocation location) {
        if (prompt == null || mainEvent == null || location == null || location.rawText().isBlank()) {
            return location;
        }
        AlertLocationMainEventIntent intent = mainEvent.eventIntent();
        if (intent == AlertLocationMainEventIntent.DEPARTURE
                && location.role() == AlertLocationRole.ORIGIN_LOCATION
                && hasCurrentDepartureWordingForLocation(prompt, location.rawText())
                && !hasExplicitOriginWordingForLocation(prompt, location.rawText())) {
            return normalizedMainEventLocation(location,
                    "current departure wording indicates event stop, not journey origin");
        }
        if (intent == AlertLocationMainEventIntent.ARRIVAL
                && location.role() == AlertLocationRole.DESTINATION_LOCATION
                && hasCurrentArrivalWordingForLocation(prompt, location.rawText())
                && !hasExplicitDestinationWordingForLocation(prompt, location.rawText())) {
            return normalizedMainEventLocation(location,
                    "current arrival wording indicates event stop, not journey destination");
        }
        if (location.polarity() == AlertLocationPolarity.EXCLUDE
                && location.role() != AlertLocationRole.DESTINATION_LOCATION
                && hasExplicitDestinationExclusionWordingForLocation(prompt, location.rawText())) {
            return normalizedDestinationExclusion(location);
        }
        return location;
    }

    private AlertLocationUnderstandingLocation normalizedMainEventLocation(
            AlertLocationUnderstandingLocation location,
            String reason) {
        AlertLocationUnderstandingLocation normalized = new AlertLocationUnderstandingLocation(
                location.rawText(),
                location.normalizedText(),
                AlertLocationRole.MAIN_EVENT_LOCATION,
                AlertLocationRelation.EVENT_LOCATION,
                true,
                location.polarity(),
                location.logicalGroup(),
                location.confidence());
        System.out.println("[IIA][ALERT_LOCATION_UNDERSTANDING][ROLE_NORMALIZATION] rawText="
                + location.rawText()
                + " originalRole=" + location.role()
                + " normalizedRole=" + normalized.role()
                + " originalRelation=" + location.relationToMainEvent()
                + " normalizedRelation=" + normalized.relationToMainEvent()
                + " reason=" + reason);
        return normalized;
    }

    private AlertLocationUnderstandingLocation normalizedDestinationExclusion(
            AlertLocationUnderstandingLocation location) {
        AlertLocationUnderstandingLocation normalized = new AlertLocationUnderstandingLocation(
                location.rawText(),
                location.normalizedText(),
                AlertLocationRole.DESTINATION_LOCATION,
                AlertLocationRelation.DESTINATION_CONSTRAINT,
                true,
                AlertLocationPolarity.EXCLUDE,
                location.logicalGroup(),
                location.confidence());
        System.out.println("[IIA][ALERT_LOCATION_UNDERSTANDING][ROLE_NORMALIZATION] reason=destination-exclusion-wording"
                + " rawText=" + location.rawText()
                + " originalRole=" + location.role()
                + " normalizedRole=" + normalized.role()
                + " originalPolarity=" + location.polarity()
                + " normalizedPolarity=" + normalized.polarity()
                + " originalRelation=" + location.relationToMainEvent()
                + " normalizedRelation=" + normalized.relationToMainEvent());
        return normalized;
    }

    private boolean hasCurrentDepartureWordingForLocation(String prompt, String rawText) {
        String normalized = normalizeText(prompt);
        String location = normalizeText(rawText);
        if (normalized == null || location == null) {
            return false;
        }
        return containsAnyNearLocation(normalized, location,
                "e in partenza da", "in partenza da", "sta partendo da", "parte da", "partenza da",
                "is departing from", "about to depart from", "departs from", "departure from");
    }

    private boolean hasCurrentArrivalWordingForLocation(String prompt, String rawText) {
        String normalized = normalizeText(prompt);
        String location = normalizeText(rawText);
        if (normalized == null || location == null) {
            return false;
        }
        return containsAnyNearLocation(normalized, location,
                "e in arrivo a", "in arrivo a", "sta arrivando a", "arriva a",
                "is arriving at", "about to arrive at", "arrives at", "arrival at");
    }

    private boolean hasExplicitOriginWordingForLocation(String prompt, String rawText) {
        String normalized = normalizeText(prompt);
        String location = normalizeText(rawText);
        if (normalized == null || location == null) {
            return false;
        }
        return containsAnyNearLocation(normalized, location,
                "ha origine a", "origine a", "origine", "localita di origine",
                "corsa con origine", "journey origin", "originating from");
    }

    private boolean hasExplicitDestinationWordingForLocation(String prompt, String rawText) {
        String normalized = normalizeText(prompt);
        String location = normalizeText(rawText);
        if (normalized == null || location == null) {
            return false;
        }
        return containsAnyNearLocation(normalized, location,
                "ha destinazione", "destinazione", "destino", "corsa con destinazione", "destination");
    }

    private boolean hasExplicitDestinationExclusionWordingForLocation(String prompt, String rawText) {
        String normalized = normalizeText(prompt);
        String location = normalizeText(rawText);
        if (normalized == null || location == null) {
            return false;
        }
        return containsAnyNearLocation(normalized, location,
                "non ha destinazione",
                "non deve avere destinazione",
                "non destinazione",
                "destinazione diversa da",
                "destino diverso da",
                "destination is not",
                "not destination",
                "must not have destination");
    }

    private boolean containsAnyNearLocation(String prompt, String location, String... cues) {
        int locationIndex = prompt.indexOf(location);
        while (locationIndex >= 0) {
            int windowStart = Math.max(0, locationIndex - 80);
            String beforeAndLocation = prompt.substring(windowStart, locationIndex + location.length());
            for (String cue : cues) {
                if (beforeAndLocation.contains(cue) && beforeAndLocation.indexOf(cue) < beforeAndLocation.lastIndexOf(location)) {
                    return true;
                }
            }
            locationIndex = prompt.indexOf(location, locationIndex + location.length());
        }
        return false;
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isAllLocationsNoOp(String rawText, String normalizedText) {
        return isAllLocationsNoOpText(rawText) || isAllLocationsNoOpText(normalizedText);
    }

    private boolean isAllLocationsNoOpText(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return false;
        }
        return normalized.contains("qualsiasi localita")
                || normalized.contains("qualunque localita")
                || normalized.contains("ogni localita")
                || normalized.contains("tutte le localita")
                || normalized.contains("tutte le fermate")
                || normalized.contains("qualunque fermata")
                || normalized.contains("qualsiasi fermata")
                || normalized.contains("ovunque")
                || normalized.contains("in ogni punto")
                || normalized.contains("senza localita specifica")
                || normalized.contains("senza specificare la localita")
                || normalized.contains("any location")
                || normalized.contains("all locations")
                || normalized.contains("every location")
                || normalized.contains("anywhere")
                || normalized.contains("any stop")
                || normalized.contains("all stops");
    }

    private List<AlertVerificationLocationContext.NonLocationConstraint> semanticNonLocationConstraints(
            AlertLocationUnderstandingResult understanding,
            String prompt) {
        List<AlertVerificationLocationContext.NonLocationConstraint> constraints = new ArrayList<>(
                understanding.nonLocationConstraints().stream()
                        .map(this::toPromptNonLocationConstraint)
                        .filter(Objects::nonNull)
                        .toList());
        DelayThreshold delayThreshold = delayThreshold(prompt);
        MainEventDerivation derivation = deriveMainEventSemantics(understanding, constraints, prompt);
        if (derivation.accessoryDelay()) {
            constraints.removeIf(constraint -> "DELAY_ROLE".equalsIgnoreCase(constraint.type()));
            constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                    "DELAY_ROLE",
                    "ACCESSORY_DELAY_PREDICATE"));
        }
        if (understanding.mainEvent() != null && understanding.mainEvent().eventIntent() != null) {
            constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                    "MAIN_EVENT_INTENT",
                    derivation.intent().name()));
            if (delayThreshold != null) {
                constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_THRESHOLD",
                        delayThreshold.rawText()));
            }
            String promptDelayEventType = delayEventType(prompt);
            String derivedDelayEventType = normalizedDelayEventType(constraints);
            if (promptDelayEventType != null && !derivation.accessoryDelay()) {
                constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_ROLE",
                        "PRIMARY_DELAY_EVENT"));
                constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_EVENT_TYPE",
                        promptDelayEventType));
                constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_DIRECTION",
                        delayDirection(promptDelayEventType)));
            } else if (derivation.accessoryDelay() && derivedDelayEventType != null
                    && nonLocationConstraintValue(constraints, "DELAY_EVENT_TYPE") == null) {
                constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_EVENT_TYPE",
                        derivedDelayEventType));
                if (nonLocationConstraintValue(constraints, "DELAY_DIRECTION") == null) {
                    constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                            "DELAY_DIRECTION",
                            delayDirection(derivedDelayEventType)));
                }
            } else if (derivation.accessoryDelay() && nonLocationConstraintValue(constraints, "DELAY_DIRECTION") == null) {
                constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                        "DELAY_DIRECTION",
                        delayDirection(derivation.intent().name())));
            }
            constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                    "MAIN_EVENT_PHASE",
                    derivation.phase().name()));
            String expectedEventType = derivation.expectedEventType();
            if (expectedEventType != null) {
                constraints.add(new AlertVerificationLocationContext.NonLocationConstraint(
                        "EXPECTED_MAIN_EVENT_TYPE",
                        expectedEventType));
            }
        }
        List<AlertVerificationLocationContext.NonLocationConstraint> canonicalized =
                canonicalizeDelayEventTypeConstraints(constraints);
        logMainEventDerivation(understanding, canonicalized);
        return canonicalized;
    }

    private MainEventDerivation deriveMainEventSemantics(
            AlertLocationUnderstandingResult understanding,
            List<AlertVerificationLocationContext.NonLocationConstraint> constraints,
            String prompt) {
        AlertLocationMainEventIntent intent = understanding.mainEvent() == null
                ? AlertLocationMainEventIntent.UNKNOWN
                : understanding.mainEvent().eventIntent();
        String delayEventType = normalizedDelayEventType(constraints);
        boolean promptAccessoryDelay = isOperationalEventWithAccessoryDelay(normalizeText(prompt));
        boolean semanticAccessoryDelay = hasMainEventLocation(understanding)
                && ("ARRIVAL_DELAY".equals(delayEventType) || "DEPARTURE_DELAY".equals(delayEventType));
        boolean accessoryDelay = promptAccessoryDelay
                || (AlertLocationMainEventIntent.DELAY.equals(intent) && semanticAccessoryDelay);
        AlertLocationMainEventIntent derivedIntent = intent;
        if (accessoryDelay && AlertLocationMainEventIntent.DELAY.equals(intent)) {
            if ("ARRIVAL_DELAY".equals(delayEventType)) {
                derivedIntent = AlertLocationMainEventIntent.ARRIVAL;
            } else if ("DEPARTURE_DELAY".equals(delayEventType)) {
                derivedIntent = AlertLocationMainEventIntent.DEPARTURE;
            }
        }
        AlertEventWordingClassifier classifier = alertEventWordingClassifier == null
                ? new AlertEventWordingClassifier()
                : alertEventWordingClassifier;
        AlertEventPhase phase = classifier.classify(prompt, derivedIntent);
        String expectedEventType = classifier.expectedEventType(derivedIntent, phase);
        return new MainEventDerivation(derivedIntent, phase, expectedEventType, accessoryDelay);
    }

    private boolean hasMainEventLocation(AlertLocationUnderstandingResult understanding) {
        return understanding.locations().stream()
                .anyMatch(location -> AlertLocationRole.MAIN_EVENT_LOCATION.equals(location.role())
                        || AlertLocationRelation.EVENT_LOCATION.equals(location.relationToMainEvent())
                        || AlertLocationRelation.EVENT_STOP_POINT.equals(location.relationToMainEvent()));
    }

    private String normalizedDelayEventType(List<AlertVerificationLocationContext.NonLocationConstraint> constraints) {
        String value = nonLocationConstraintValue(constraints, "DELAY_EVENT_TYPE");
        String normalized = AlertDelayEventTypeNormalizer.normalize(value);
        if (normalized != null) {
            return normalized;
        }
        return normalizedDelayEventTypeFromDirection(nonLocationConstraintValue(constraints, "DELAY_DIRECTION"));
    }

    private String normalizedDelayEventTypeFromDirection(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        if (containsAny(normalized, "arrival", "arriving", "arrive", "arrivo", "arrivi")) {
            return "ARRIVAL_DELAY";
        }
        if (containsAny(normalized, "departure", "departing", "depart", "partenza", "partenze")) {
            return "DEPARTURE_DELAY";
        }
        if (containsAny(normalized, "both", "generic", "generico", "generica")) {
            return "BOTH";
        }
        return null;
    }

    private String nonLocationConstraintValue(
            List<AlertVerificationLocationContext.NonLocationConstraint> constraints,
            String type) {
        return constraints.stream()
                .filter(constraint -> type.equalsIgnoreCase(constraint.type()))
                .map(AlertVerificationLocationContext.NonLocationConstraint::rawText)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private void logMainEventDerivation(
            AlertLocationUnderstandingResult understanding,
            List<AlertVerificationLocationContext.NonLocationConstraint> constraints) {
        System.out.println("[IIA][ALERT_VERIFY][MAIN_EVENT_DERIVATION] rawConstraints="
                + understanding.nonLocationConstraints()
                + " derived MAIN_EVENT_INTENT=" + nonLocationConstraintValue(constraints, "MAIN_EVENT_INTENT")
                + " MAIN_EVENT_PHASE=" + nonLocationConstraintValue(constraints, "MAIN_EVENT_PHASE")
                + " EXPECTED_MAIN_EVENT_TYPE=" + nonLocationConstraintValue(constraints, "EXPECTED_MAIN_EVENT_TYPE")
                + " DELAY_ROLE=" + nonLocationConstraintValue(constraints, "DELAY_ROLE")
                + " DELAY_EVENT_TYPE=" + nonLocationConstraintValue(constraints, "DELAY_EVENT_TYPE"));
    }

    private record MainEventDerivation(
            AlertLocationMainEventIntent intent,
            AlertEventPhase phase,
            String expectedEventType,
            boolean accessoryDelay) {
    }

    private AlertVerificationLocationContext.NonLocationConstraint toPromptNonLocationConstraint(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.AlertLocationUnderstandingNonLocationConstraint constraint) {
        String type = constraint.type().name();
        String rawText = constraint.rawText();
        if ("DELAY_EVENT_TYPE".equals(type)) {
            String normalized = AlertDelayEventTypeNormalizer.normalize(rawText);
            if (normalized != null) {
                if (!normalized.equalsIgnoreCase(stringValue(rawText))) {
                    System.out.println("[IIA][ALERT_VERIFY][DELAY_EVENT_TYPE_NORMALIZATION] rawValue="
                            + rawText
                            + " normalizedValue=" + normalized
                            + " reason=canonical-delay-event-type");
                }
                rawText = normalized;
            } else {
                System.out.println("[IIA][ALERT_VERIFY][DELAY_EVENT_TYPE_NORMALIZATION] rawValue="
                        + rawText
                        + " normalizedValue=null"
                        + " action=discarded-noncanonical"
                        + " reason=canonical-delay-event-type");
                return null;
            }
        }
        return new AlertVerificationLocationContext.NonLocationConstraint(type, rawText);
    }

    private List<AlertVerificationLocationContext.NonLocationConstraint> canonicalizeDelayEventTypeConstraints(
            List<AlertVerificationLocationContext.NonLocationConstraint> constraints) {
        List<AlertVerificationLocationContext.NonLocationConstraint> canonicalized = new ArrayList<>();
        String selectedDelayEventType = null;
        String selectedRawValue = null;
        for (AlertVerificationLocationContext.NonLocationConstraint constraint : constraints) {
            if (!"DELAY_EVENT_TYPE".equalsIgnoreCase(constraint.type())) {
                canonicalized.add(constraint);
                continue;
            }
            String rawValue = stringValue(constraint.rawText());
            String normalized = AlertDelayEventTypeNormalizer.normalize(rawValue);
            if (normalized == null) {
                System.out.println("[IIA][ALERT_VERIFY][DELAY_EVENT_TYPE_NORMALIZATION] rawValue="
                        + rawValue
                        + " normalizedValue=null"
                        + " action=discarded-noncanonical"
                        + " reason=canonical-delay-event-type");
                continue;
            }
            String action = normalized.equals(rawValue) ? "kept-canonical" : "replaced-by-backend-derived";
            if (selectedDelayEventType != null) {
                action = "replaced-by-backend-derived";
            }
            System.out.println("[IIA][ALERT_VERIFY][DELAY_EVENT_TYPE_NORMALIZATION] rawValue="
                    + rawValue
                    + " normalizedValue=" + normalized
                    + " action=" + action
                    + " reason=canonical-delay-event-type");
            selectedDelayEventType = normalized;
            selectedRawValue = rawValue;
        }
        if (selectedDelayEventType != null) {
            canonicalized.add(new AlertVerificationLocationContext.NonLocationConstraint(
                    "DELAY_EVENT_TYPE",
                    selectedDelayEventType));
            if (selectedRawValue != null && !selectedDelayEventType.equals(selectedRawValue)) {
                System.out.println("[IIA][ALERT_VERIFY][DELAY_EVENT_TYPE_NORMALIZATION] rawValue="
                        + selectedRawValue
                        + " normalizedValue=" + selectedDelayEventType
                        + " action=replaced-by-backend-derived"
                        + " reason=canonical-delay-event-type");
            }
        }
        return canonicalized;
    }

    private String delayEventType(String prompt) {
        String normalized = normalizeText(prompt);
        if (normalized == null || !normalized.contains("ritardo") && !normalized.contains("delay")) {
            return null;
        }
        if (isOperationalEventWithAccessoryDelay(normalized)) {
            return null;
        }
        if (containsAny(normalized,
                "ritardo in partenza",
                "ritardo alla partenza",
                "ritardo di partenza",
                "departure delay",
                "delay on departure")) {
            return "DEPARTURE_DELAY";
        }
        if (containsAny(normalized,
                "ritardo in arrivo",
                "ritardo all arrivo",
                "ritardo di arrivo",
                "arrival delay",
                "delay on arrival")) {
            return "ARRIVAL_DELAY";
        }
        return "BOTH";
    }

    private DelayThreshold delayThreshold(String prompt) {
        String normalized = normalizeText(prompt);
        if (normalized == null || !normalized.contains("ritardo") && !normalized.contains("delay")) {
            return null;
        }
        DelayThreshold threshold = thresholdFromPattern(
                normalized,
                "(?:piu di|oltre|maggiore di|greater than|over)\\s+(\\d+)",
                "GREATER_THAN");
        if (threshold != null) {
            return threshold;
        }
        threshold = thresholdFromPattern(
                normalized,
                "(?:almeno|non meno di|at least|greater or equal)\\s+(\\d+)",
                "GREATER_OR_EQUAL");
        if (threshold != null) {
            return threshold;
        }
        return thresholdFromPattern(
                normalized,
                "(?:ritardo di|in ritardo di|delay of)\\s+(\\d+)",
                "EQUALS");
    }

    private DelayThreshold thresholdFromPattern(String normalizedPrompt, String pattern, String operator) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(normalizedPrompt);
        if (!matcher.find()) {
            return null;
        }
        int rawValue = Integer.parseInt(matcher.group(1));
        boolean seconds = containsAny(normalizedPrompt, "secondo", "secondi", "second", "seconds");
        return new DelayThreshold(operator, seconds ? rawValue : rawValue * 60, seconds ? "SECONDS" : "SECONDS");
    }

    private boolean isOperationalEventWithAccessoryDelay(String normalized) {
        boolean hasAccessoryDelay = containsAny(normalized, " con piu", " con almeno", " con oltre", " with more", " with at least")
                && (normalized.contains("ritardo") || normalized.contains("delay"));
        if (!hasAccessoryDelay) {
            return false;
        }
        return containsAny(normalized,
                "in partenza da",
                "sta partendo da",
                "parte da",
                "in arrivo a",
                "sta arrivando a",
                "arriva a",
                "is departing from",
                "departs from",
                "is arriving at",
                "arrives at");
    }

    private String delayDirection(String delayEventType) {
        if ("DEPARTURE_DELAY".equals(delayEventType) || "DEPARTURE".equals(delayEventType)) {
            return "DEPARTURE";
        }
        if ("ARRIVAL_DELAY".equals(delayEventType) || "ARRIVAL".equals(delayEventType)) {
            return "ARRIVAL";
        }
        return "GENERIC";
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
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
        alertAsyncVerificationService.scheduleCreatedAlertVerification(
                alertId,
                enableAfterVerification,
                tenant.tenantId());
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

    private AlertVerificationOutcome routeRejectedOutcome(AlertRouteUnderstandingResult route, String provider, String model) {
        String reason = route == null || route.rejectedReason() == null || route.rejectedReason().isBlank()
                ? "Alert route understanding rejected the prompt because it is outside supported domains."
                : route.rejectedReason();
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                route == null || route.summary() == null ? "Alert route understanding rejected the prompt." : route.summary(),
                reason,
                route == null ? 0.0 : route.confidence(),
                provider,
                model,
                "alert-route-understanding-v1",
                List.of(),
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                Map.of(
                        "requirements", List.of(),
                        "allRequiredRequirementsMapped", false),
                route == null || route.warnings() == null ? List.of(reason) : route.warnings(),
                List.of(
                        "No executable code generated.",
                        "No technicalSpecification generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
    }

    private AlertVerificationOutcome verifyScheduledRouteOutcome(
            String alertId,
            AlertVerificationPromptData promptData,
            AlertRouteUnderstandingResult route) {
        String provider = aiConfiguration.provider();
        String model = aiConfiguration.alertVerify().model();
        try {
            System.out.println("[IIA][ALERT_VERIFY] route SCHEDULED_INTERPRETER -> scheduled location understanding");
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] start alertId=" + alertId);
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] prompt=" + promptData.prompt());
            if (scheduledAlertLocationUnderstandingService == null) {
                return scheduledLocationValidationRejectedOutcome(
                        route,
                        provider,
                        model,
                        "Scheduled location understanding failed: service unavailable.",
                        List.of());
            }
            ScheduledAlertLocationUnderstandingResult understanding =
                    scheduledAlertLocationUnderstandingService.understandLocations(promptData.prompt(), alertId);
            if (understanding == null) {
                return scheduledLocationValidationRejectedOutcome(
                        route,
                        provider,
                        model,
                        "Scheduled location understanding failed: no result.",
                        List.of());
            }
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION] parsed result=" + understanding);
            if (scheduledServiceDataLocationResolutionService == null) {
                return scheduledLocationValidationRejectedOutcome(
                        route,
                        provider,
                        model,
                        "Scheduled location understanding failed: location resolution service unavailable.",
                        understanding.warnings());
            }
            ScheduledServiceDataLocationContext locationContext =
                    scheduledServiceDataLocationResolutionService.resolve(understanding);
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][API_QUERY] monitoringScope="
                    + locationContext.monitoringScope()
                    + " serviceDataApiStopPoints="
                    + locationContext.serviceDataApiStopPoints());
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][VALIDATION] context=" + locationContext);

            if (locationContext.hasUnresolvedRequiredMonitoredLocations()) {
                String reason = "Scheduled ServiceData alert requires monitored stop point ids, but the following monitored locations could not be resolved: "
                        + String.join(", ", locationContext.unresolvedRequiredMonitoredLocationTexts())
                        + ".";
                System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][VALIDATION] rejected reason=" + reason);
                return scheduledLocationValidationRejectedOutcome(route, provider, model, reason, locationContext.warnings());
            }
            if (locationContext.monitoringScope() == ScheduledAlertMonitoringScope.UNSPECIFIED
                    && locationContext.monitoredLocations().isEmpty()) {
                String reason = "Scheduled ServiceData alert requires at least one monitored stop point or an explicit all-locations scope.";
                System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][VALIDATION] rejected reason=" + reason);
                return scheduledLocationValidationRejectedOutcome(route, provider, model, reason, locationContext.warnings());
            }

            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][REQUEST] alertId=" + alertId
                    + " route.intentKind=" + (route == null ? null : route.intentKind())
                    + " route.outputMode=" + (route == null ? null : route.outputMode())
                    + " monitoringScope=" + locationContext.monitoringScope()
                    + " serviceDataApiStopPoints.count=" + locationContext.serviceDataApiStopPoints().size()
                    + " requiresAllKnownStopPoints=" + locationContext.requiresAllKnownStopPoints()
                    + " featureFlag.enabled=" + scheduledVerifyEnabled);
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY] enabled=" + scheduledVerifyEnabled);
            System.out.println("[IIA][ALERT_VERIFY] route SCHEDULED_INTERPRETER -> scheduled verify enabled=" + scheduledVerifyEnabled);
            if (!scheduledVerifyEnabled) {
                System.out.println("[IIA][ALERT_VERIFY] route SCHEDULED_INTERPRETER -> scheduled verify disabled, reject not implemented yet");
                return scheduledInterpreterNotImplementedOutcome(alertId, route, provider, model, locationContext);
            }
            if (scheduledAlertVerificationService == null) {
                return scheduledLocationValidationRejectedOutcome(
                        route,
                        provider,
                        model,
                        "Scheduled alert verification failed: service unavailable.",
                        locationContext.warnings());
            }

            AlertVerificationOutcome scheduledOutcome;
            try {
                scheduledOutcome = scheduledAlertVerificationService.verify(
                        alertId,
                        promptData.name(),
                        promptData.description(),
                        promptData.prompt(),
                        route,
                        locationContext);
            } catch (RuntimeException exception) {
                String reason = "Scheduled alert verification failed: " + shortTechnicalMessage(exception);
                System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][OUTCOME] decision=REJECTED reason=" + reason);
                return scheduledLocationValidationRejectedOutcome(route, provider, model, reason, locationContext.warnings());
            }
            System.out.println("[IIA][ALERT_SCHEDULED_VERIFY][OUTCOME] decision="
                    + (scheduledOutcome == null ? null : scheduledOutcome.decision()));
            System.out.println("[IIA][ALERT_VERIFY] scheduled verification outcome -> persist decision="
                    + (scheduledOutcome == null ? null : scheduledOutcome.decision()));
            if (scheduledOutcome == null) {
                return scheduledLocationValidationRejectedOutcome(
                        route,
                        provider,
                        model,
                        "Scheduled alert verification failed: no outcome.",
                        locationContext.warnings());
            }
            return scheduledOutcome;
        } catch (RuntimeException exception) {
            String reason = "Scheduled location understanding failed: " + shortTechnicalMessage(exception);
            System.out.println("[IIA][ALERT_SCHEDULED_LOCATION][VALIDATION] rejected reason=" + reason);
            return scheduledLocationValidationRejectedOutcome(route, provider, model, reason, List.of());
        }
    }

    private AlertVerificationOutcome scheduledLocationValidationRejectedOutcome(
            AlertRouteUnderstandingResult route,
            String provider,
            String model,
            String reason,
            List<String> contextWarnings) {
        List<String> warnings = new ArrayList<>(route == null || route.warnings() == null ? List.of() : route.warnings());
        if (contextWarnings != null) {
            warnings.addAll(contextWarnings);
        }
        warnings.add(reason);
        warnings.add("ROUTE_INTERPRETER_TYPE=SCHEDULED_INTERPRETER");
        warnings.add("ROUTE_DATA_DOMAINS=SERVICE_DATA");
        warnings.add("ROUTE_ACCESS_MODE=SERVICE_DATA_API_SNAPSHOT");
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                "Scheduled ServiceData location validation failed.",
                reason,
                route == null || route.confidence() == null ? 0.0 : route.confidence(),
                provider,
                model,
                "alert-scheduled-location-understanding-v1",
                List.of("SERVICE_DATA"),
                "SCHEDULED_INTERPRETER",
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                Map.of(
                        "requirements", List.of(),
                        "allRequiredRequirementsMapped", false),
                List.copyOf(warnings),
                List.of(
                        "No executable code generated.",
                        "No scheduled technicalSpecification generated.",
                        "No Agent Definition created.",
                        "No Suggestion created."));
    }

    private AlertVerificationOutcome scheduledInterpreterNotImplementedOutcome(
            String alertId,
            AlertRouteUnderstandingResult route,
            String provider,
            String model,
            ScheduledServiceDataLocationContext locationContext) {
        String summary = "The alert was recognized as a SERVICE_DATA scheduled snapshot alert and its monitored stop points were resolved.";
        String reason = "The alert was recognized as a SERVICE_DATA scheduled snapshot alert, but SCHEDULED_INTERPRETER technical verification is not implemented yet.";
        List<String> warnings = new ArrayList<>(route.warnings() == null ? List.of() : route.warnings());
        warnings.addAll(locationContext.warnings());
        warnings.add(reason);
        warnings.add("ROUTE_INTERPRETER_TYPE=SCHEDULED_INTERPRETER");
        warnings.add("ROUTE_DATA_DOMAINS=SERVICE_DATA");
        warnings.add("ROUTE_ACCESS_MODE=SERVICE_DATA_API_SNAPSHOT");
        warnings.add("ROUTE_INTENT_KIND=" + route.intentKind());
        warnings.add("ROUTE_OUTPUT_MODE=" + route.outputMode());
        warnings.add("SCHEDULED_MONITORING_SCOPE=" + locationContext.monitoringScope());
        warnings.add("SCHEDULED_SERVICE_DATA_API_STOP_POINTS=" + scheduledStopPointsMarker(locationContext));
        if (locationContext.requiresAllKnownStopPoints()) {
            warnings.add("All known stop points scope requested; scheduled technical verification is not implemented yet.");
        }
        warnings.add("SCHEDULED_TECHNICAL_VERIFICATION_NOT_IMPLEMENTED");
        System.out.println("[IIA][ALERT_ROUTE] ROUTE_INTERPRETER_TYPE=SCHEDULED_INTERPRETER");
        System.out.println("[IIA][ALERT_ROUTE] ROUTE_DATA_DOMAINS=SERVICE_DATA");
        System.out.println("[IIA][ALERT_ROUTE] ROUTE_ACCESS_MODE=SERVICE_DATA_API_SNAPSHOT");
        System.out.println("[IIA][ALERT_VERIFY][SCHEDULED_ROUTE_REJECT] alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][SCHEDULED_ROUTE_REJECT] summary=" + summary);
        System.out.println("[IIA][ALERT_VERIFY][SCHEDULED_ROUTE_REJECT] rejectedReason=" + reason);
        System.out.println("[IIA][ALERT_VERIFY][SCHEDULED_ROUTE_REJECT] interpreterType=SCHEDULED_INTERPRETER"
                + " accessMode=SERVICE_DATA_API_SNAPSHOT"
                + " intentKind=" + route.intentKind());
        System.out.println("[IIA][ALERT_VERIFY][SCHEDULED_ROUTE_REJECT] technicalSpecificationPresent=false agentBlueprintPreviewPresent=false");
        return new AlertVerificationOutcome(
                AlertVerificationDecision.REJECTED,
                summary,
                reason,
                route.confidence() == null ? 0.0 : route.confidence(),
                provider,
                model,
                "alert-route-understanding-v1",
                List.of("SERVICE_DATA"),
                "SCHEDULED_INTERPRETER",
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                Map.of(
                        "route", Map.of(
                                "interpreterType", String.valueOf(route.interpreterType()),
                                "accessMode", String.valueOf(route.accessMode()),
                                "intentKind", String.valueOf(route.intentKind()),
                                "outputMode", String.valueOf(route.outputMode())),
                        "scheduledLocation", Map.of(
                                "monitoringScope", String.valueOf(locationContext.monitoringScope()),
                                "serviceDataApiStopPoints", locationContext.serviceDataApiStopPoints(),
                                "requiresAllKnownStopPoints", locationContext.requiresAllKnownStopPoints()),
                        "allRequiredRequirementsMapped", false),
                List.copyOf(warnings),
                List.of(
                        "No executable code generated.",
                        "No scheduled technicalSpecification generated.",
                        "No Agent Definition created.",
                        "No Suggestion created.",
                        "SCHEDULED_MONITORING_SCOPE=" + locationContext.monitoringScope(),
                        "SCHEDULED_SERVICE_DATA_API_STOP_POINTS=" + scheduledStopPointsMarker(locationContext),
                        "SCHEDULED_TECHNICAL_VERIFICATION_NOT_IMPLEMENTED"));
    }

    private String scheduledStopPointsMarker(ScheduledServiceDataLocationContext locationContext) {
        if (locationContext.requiresAllKnownStopPoints()) {
            return "ALL_KNOWN_STOP_POINTS";
        }
        return locationContext.serviceDataApiStopPoints().toString();
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
            try {
                TenantResolver resolver = tenantResolver.get();
                if (requestContextActive() || !isCdiClientProxy(resolver)) {
                    tenantId = resolver.resolveTenantId();
                    source = "hibernateResolver";
                } else {
                    source = "hibernateResolver.skipped-inactive-request-context";
                }
            } catch (RuntimeException ex) {
                System.out.println("[IIA][ALERT_VERIFY][TENANT_DEBUG] async tenant resolver probe skipped after error"
                        + " alertId=" + alertId
                        + " exceptionClass=" + ex.getClass().getName()
                        + " exceptionMessage=" + ex.getMessage());
            }
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
                if (requestContextActive() || !isCdiClientProxy(resolver)) {
                    resolvedTenant = resolver.resolveTenantId();
                    source = "TenantResolver.resolveTenantId";
                } else {
                    source = "TenantResolver.skipped-inactive-request-context";
                }
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

    private boolean isCdiClientProxy(Object bean) {
        return bean != null && bean.getClass().getName().contains("_ClientProxy");
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
