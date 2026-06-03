package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import io.quarkus.arc.Arc;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreter;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRequiredData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertRuntimeMetadata;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSchedule;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertUpdateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertRequiredDatum;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertRequiredTool;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertTargetTypeRel;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertTargetTypeRelId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVersionHistory;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVersionHistoryId;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationDecision;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationOutcome;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification.AlertVerificationPromptData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.preview.AlertAgentGenerationPreviewData;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.view.AlertSummaryView;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionManager;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AlertRepository implements PanacheRepositoryBase<Alert, String> {

    @Inject
    EntityManager entityManager;

    @Inject
    TransactionManager transactionManager;

    @Inject
    CriteriaBuilderFactory criteriaBuilderFactory;

    @Inject
    EntityViewManager entityViewManager;

    public List<AlertSummary> searchAlerts(AlertSearchCriteria criteria) {
        CriteriaBuilder<Alert> query = criteriaBuilderFactory.create(entityManager, Alert.class, "alert");
        query.where("dtDeletedat").isNull();

        if (criteria.getStatus() != null) {
            query.where("sglStatus.sglStatus").eq(criteria.getStatus().toString());
        }
        if (criteria.getEnabled() != null) {
            query.where("flgEnabled").eq(criteria.getEnabled());
        }
        if (criteria.getInterpreterType() != null) {
            query.where("sglInterpretertype.sglInterpretertype").eq(criteria.getInterpreterType().toString());
        }
        if (criteria.getText() != null && !criteria.getText().isBlank()) {
            String textFilter = "%" + criteria.getText().toLowerCase() + "%";
            query.whereOr()
                    .where("LOWER(dscName)").like().value(textFilter).noEscape()
                    .where("LOWER(dscPrompt)").like().value(textFilter).noEscape()
                    .endOr();
        }

        query.orderByDesc("dtCreatedat");

        List<AlertSummaryView> views = entityViewManager
                .applySetting(EntityViewSetting.create(AlertSummaryView.class), query)
                .getResultList();

        return views.stream()
                .map(this::toAlertSummary)
                .toList();
    }

    public Optional<AlertDetail> getAlert(String alertId) {
        Optional<Alert> alert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        return alert.map(this::toAlertDetail);
    }

    @Transactional
    public Optional<AlertVerificationPromptData> getAlertVerificationPromptData(String alertId) {
        return find("codAlert = ?1 and dtDeletedat is null", alertId)
                .firstResultOptional()
                .map(alert -> new AlertVerificationPromptData(
                        alert.getCodAlert(),
                        alert.getDscName(),
                        alert.getDscDescription(),
                        alert.getDscPrompt()));
    }

    public Optional<AlertAgentGenerationPreviewData> getAlertAgentGenerationPreviewData(String alertId) {
        return find("codAlert = ?1", alertId)
                .firstResultOptional()
                .map(alert -> new AlertAgentGenerationPreviewData(
                        alert.getCodAlert(),
                        alert.getDscName(),
                        alert.getSglStatus() == null ? null : alert.getSglStatus().getSglStatus(),
                        alert.getSglVerificationstatus() == null ? null : alert.getSglVerificationstatus().getSglVerificationstatus(),
                        alert.getFlgEnabled(),
                        alert.getDtDeletedat(),
                        alert.getNumVersion(),
                        alert.getDscPrompt(),
                        alert.getDscVerificationsummary(),
                        alert.getDscRejectedreason(),
                        alert.getNumVerificationconfidence(),
                        alert.getSglInterpretertype() == null ? null : alert.getSglInterpretertype().getSglInterpretertype(),
                        alert.getDscInputmodel(),
                        alert.getDscOutputmodel(),
                        toStringObjectMap(alert.getJsnTechnicalspecification()),
                        toStringObjectMap(alert.getJsnAgentblueprintpreview()),
                        toStringList(alert.getJsnInterpretedeventnames()),
                        toStringList(alert.getJsnVerificationwarnings()),
                        findTargetTypes(alert.getCodAlert())));
    }

    @Transactional
    public boolean persistValidatedAgentBlueprintPreview(String alertId, Map<String, Object> blueprint) {
        Optional<Alert> maybeAlert = find("codAlert = ?1", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()
                || !applyValidatedAgentBlueprintPreview(maybeAlert.get(), blueprint, OffsetDateTime.now())) {
            return false;
        }
        flush();
        return true;
    }

    boolean applyValidatedAgentBlueprintPreview(
            Alert alert,
            Map<String, Object> blueprint,
            OffsetDateTime updatedAt) {
        if (alert.getDtDeletedat() != null
                || alert.getSglStatus() == null
                || !"VERIFIED".equals(alert.getSglStatus().getSglStatus())
                || alert.getSglVerificationstatus() == null
                || !"VERIFIED".equals(alert.getSglVerificationstatus().getSglVerificationstatus())) {
            return false;
        }
        alert.setJsnAgentblueprintpreview(blueprint);
        alert.setDtUpdatedat(updatedAt);
        return true;
    }

    public boolean existsDeletedAlert(String alertId) {
        return count("""
                codAlert = ?1
                and (dtDeletedat is not null or sglStatus.sglStatus = 'DELETED')
                """, alertId) > 0;
    }

    public boolean softDeleteAlert(String alertId) {
        Optional<Alert> maybeAlert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()) {
            return false;
        }

        Alert alert = maybeAlert.get();
        OffsetDateTime now = OffsetDateTime.now();
        alert.setSglStatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus.class,
                "DELETED"));
        alert.setFlgEnabled(false);
        alert.setDtDeletedat(now);
        alert.setDtUpdatedat(now);
        flush();
        return true;
    }

    @Transactional
    public Optional<AlertDetail> verifyAlert(String alertId, AlertVerificationRequest request, AlertVerificationOutcome outcome) {
        Optional<Alert> maybeAlert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        Alert alert = maybeAlert.get();
        OffsetDateTime now = OffsetDateTime.now();

        System.out.println("[IIA][ALERT_VERIFY] Starting verification for alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] repository persisting normal outcome alertId=" + alertId
                + " decision=" + outcome.decision());
        System.out.println("[IIA][ALERT_VERIFY][TX_DEBUG] inside AlertRepository.verifyAlert"
                + " alertId=" + alertId
                + " decision=" + outcome.decision()
                + " transactionActive=" + transactionActive()
                + " entityManagerOpen=" + entityManagerOpen()
                + " tenantPresent=" + (currentTenantId() != null)
                + " statusToSave=" + outcome.decision().name()
                + " verificationStatusToSave=" + outcome.decision().name());

        alert.setSglStatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus.class,
                "VERIFYING"));
        alert.setDtUpdatedat(now);
        flush();

        alert.setSglStatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus.class,
                outcome.decision().name()));
        alert.setSglVerificationstatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus.class,
                outcome.decision().name()));
        alert.setDscVerificationsummary(outcome.summary());
        alert.setDscRejectedreason(outcome.rejectedReason());
        alert.setNumVerificationconfidence(outcome.confidence() == null ? null : BigDecimal.valueOf(outcome.confidence()));
        if (outcome.decision() == AlertVerificationDecision.ERROR) {
            alert.setFlgEnabled(false);
        }
        applyVerifiedInterpreterMetadata(alert, outcome);
        alert.setDscLlmprovider(outcome.provider());
        alert.setDscLlmmodel(outcome.model());
        alert.setDscPromptversion(outcome.promptVersion());
        alert.setDtVerifiedat(now);
        alert.setJsnInterpretedeventnames(outcome.interpretedEventNames());
        alert.setJsnVerificationwarnings(outcome.warnings());
        alert.setJsnSafetychecks(outcome.safetyChecks());
        alert.setDtUpdatedat(now);

        replaceInterpretedTargetTypes(alert, outcome.interpretedTargetTypes());
        persistAlertVersionHistorySnapshot(
                alert,
                alert.getJsnTechnicalspecification() instanceof Map<?, ?> technicalSpecification
                        ? (Map<String, Object>) technicalSpecification
                        : null,
                alert.getJsnAgentblueprintpreview() instanceof Map<?, ?> agentBlueprintPreview
                        ? (Map<String, Object>) agentBlueprintPreview
                        : null,
                now);
        flush();

        System.out.println("[IIA][ALERT_VERIFY] Verification persisted for alertId=" + alertId);
        System.out.println("[IIA][ALERT_VERIFY] Verification completed for alertId=" + alertId + " decision=" + outcome.decision());
        System.out.println("[IIA][ALERT_VERIFY][OUTCOME_FLOW] repository persisted finalStatus="
                + (alert.getSglStatus() == null ? null : alert.getSglStatus().getSglStatus())
                + " verificationStatus="
                + (alert.getSglVerificationstatus() == null ? null : alert.getSglVerificationstatus().getSglVerificationstatus())
                + " enabled=" + alert.getFlgEnabled());
        return Optional.of(toAlertDetailForVerification(alert));
    }

    void applyVerifiedInterpreterMetadata(Alert alert, AlertVerificationOutcome outcome) {
        if (outcome.decision() != AlertVerificationDecision.VERIFIED) {
            alert.setSglInterpretertype(null);
            alert.setDscInputmodel(null);
            alert.setDscOutputmodel(null);
            alert.setDscImplementationsummary(null);
            alert.setDscInterpreterclassname(null);
            alert.setDscContractversion(null);
            alert.setCodCoderef(null);
            alert.setJsnTechnicalspecification(null);
            alert.setJsnAgentblueprintpreview(null);
            return;
        }

        alert.setSglInterpretertype(outcome.interpreterType() == null ? null : entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType.class,
                outcome.interpreterType()));
        alert.setDscInputmodel(outcome.inputModel());
        alert.setDscOutputmodel(outcome.outputModel());
        alert.setDscImplementationsummary("MVP ServiceData event interpreter for stateless alert verification.");
        alert.setJsnTechnicalspecification(outcome.technicalSpecification());
        alert.setJsnAgentblueprintpreview(outcome.agentBlueprintPreview());
    }

    void clearVerificationArtifacts(Alert alert) {
        alert.setDscVerificationsummary(null);
        alert.setDscRejectedreason(null);
        alert.setNumVerificationconfidence(null);
        alert.setDtVerifiedat(null);
        alert.setCodPrompt(null);
        alert.setDscPromptversion(null);
        alert.setDscLlmprovider(null);
        alert.setDscLlmmodel(null);
        alert.setJsnVerificationwarnings(null);
        alert.setJsnInterpretedeventnames(null);
        alert.setJsnSafetychecks(null);
        alert.setJsnTechnicalspecification(null);
        alert.setJsnAgentblueprintpreview(null);
        alert.setSglInterpretertype(null);
        alert.setDscInterpreterclassname(null);
        alert.setDscContractversion(null);
        alert.setCodCoderef(null);
        alert.setDscImplementationsummary(null);
        alert.setDscInputmodel(null);
        alert.setDscOutputmodel(null);
    }

    public boolean existsActiveAlertWithNormalizedName(String name) {
        String normalizedName = normalizeName(name);
        return count("""
                lower(trim(dscName)) = ?1
                and dtDeletedat is null
                and sglStatus.sglStatus <> 'DELETED'
                """, normalizedName) > 0;
    }

    public boolean existsActiveAlertWithNormalizedNameExcludingAlertId(String name, String alertId) {
        String normalizedName = normalizeName(name);
        return count("""
                lower(trim(dscName)) = ?1
                and codAlert <> ?2
                and dtDeletedat is null
                and sglStatus.sglStatus <> 'DELETED'
                """, normalizedName, alertId) > 0;
    }

    public Optional<AlertDetail> updateAlertMetadataWithoutPromptChange(String alertId, AlertUpdateRequest request) {
        Optional<Alert> maybeAlert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        Alert alert = maybeAlert.get();
        alert.setDscName(request.getName().trim());
        alert.setDscDescription(trimToNull(request.getDescription()));
        alert.setDtUpdatedat(OffsetDateTime.now());
        flush();
        return Optional.of(toAlertDetail(alert));
    }

    public Optional<AlertDetail> updateAlertDraftAfterPromptChange(String alertId, AlertUpdateRequest request) {
        return updateAlertAfterPromptChange(alertId, request, "DRAFT");
    }

    public Optional<AlertDetail> updateAlertVerifyingAfterPromptChange(String alertId, AlertUpdateRequest request) {
        return updateAlertAfterPromptChange(alertId, request, "VERIFYING");
    }

    private Optional<AlertDetail> updateAlertAfterPromptChange(String alertId, AlertUpdateRequest request, String status) {
        Optional<Alert> maybeAlert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        Alert alert = maybeAlert.get();
        OffsetDateTime now = OffsetDateTime.now();
        alert.setDscName(request.getName().trim());
        alert.setDscDescription(trimToNull(request.getDescription()));
        alert.setDscPrompt(request.getPrompt().trim());
        alert.setNumVersion(alert.getNumVersion() == null ? 1 : alert.getNumVersion() + 1);
        alert.setSglStatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus.class,
                status));
        alert.setSglVerificationstatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus.class,
                "PENDING"));
        alert.setFlgEnabled(false);
        clearVerificationArtifacts(alert);
        replaceInterpretedTargetTypes(alert, List.of());
        alert.setDtUpdatedat(now);
        persistAlertVersionHistorySnapshot(alert, null, null, now);
        flush();
        return Optional.of(toAlertDetail(alert));
    }

    public AlertDetail createDraftAlert(AlertCreateRequest request) {
        return createAlertWithInitialStatus(request, "DRAFT");
    }

    public AlertDetail createVerifyingAlert(AlertCreateRequest request) {
        return createAlertWithInitialStatus(request, "VERIFYING");
    }

    private AlertDetail createAlertWithInitialStatus(AlertCreateRequest request, String status) {
        OffsetDateTime now = OffsetDateTime.now();

        Alert alert = new Alert();
        alert.setDscName(request.getName().trim());
        alert.setDscDescription(trimToNull(request.getDescription()));
        alert.setDscPrompt(request.getPrompt().trim());
        alert.setNumVersion(1);
        alert.setFlgEnabled(false);
        alert.setNumExecutioncount(0L);
        alert.setSglStatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus.class,
                status));
        alert.setSglVerificationstatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus.class,
                "PENDING"));
        alert.setDtCreatedat(now);
        alert.setDtUpdatedat(now);

        persist(alert);
        flush();
        return toAlertDetailForVerification(alert);
    }

    @Transactional
    public Optional<AlertDetail> updateAlertEnabledAfterCreateVerification(String alertId, boolean enabled) {
        Optional<Alert> maybeAlert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        Alert alert = maybeAlert.get();
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] repository update enabled after create alertId=" + alertId
                + " requestedEnabled=" + enabled
                + " currentStatus=" + (alert.getSglStatus() == null ? null : alert.getSglStatus().getSglStatus()));
        alert.setFlgEnabled(enabled && isVerified(alert));
        alert.setDtUpdatedat(OffsetDateTime.now());
        flush();
        return Optional.of(toAlertDetailForVerification(alert));
    }

    public boolean hasOperationalVerificationMetadata(String alertId) {
        return find("codAlert = ?1 and dtDeletedat is null", alertId)
                .firstResultOptional()
                .filter(alert -> alert.getSglInterpretertype() != null
                        && alert.getDscInputmodel() != null
                        && alert.getDscOutputmodel() != null
                        && alert.getJsnTechnicalspecification() != null
                        && alert.getJsnAgentblueprintpreview() != null)
                .isPresent();
    }

    public Optional<AlertDetail> updateAlertEnabled(String alertId, boolean enabled) {
        Optional<Alert> maybeAlert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        Alert alert = maybeAlert.get();
        alert.setFlgEnabled(enabled);
        alert.setDtUpdatedat(OffsetDateTime.now());
        flush();
        return Optional.of(toAlertDetail(alert));
    }

    @Transactional
    public Optional<AlertDetail> markAlertVerificationTechnicalError(String alertId, String warning, String provider, String model) {
        Optional<Alert> maybeAlert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        OffsetDateTime now = OffsetDateTime.now();
        Alert alert = maybeAlert.get();
        System.out.println("[IIA][ALERT_VERIFY][ASYNC_FLOW] repository persisting technical error alertId=" + alertId
                + " warning=" + warning);
        alert.setSglStatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus.class,
                "ERROR"));
        alert.setSglVerificationstatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus.class,
                "ERROR"));
        alert.setFlgEnabled(false);
        alert.setDscVerificationsummary(technicalErrorSummary(warning));
        alert.setDscRejectedreason(null);
        alert.setNumVerificationconfidence(BigDecimal.ZERO);
        alert.setDscLlmprovider(provider);
        alert.setDscLlmmodel(model);
        alert.setDscPromptversion("alert-verify-mvp-v1");
        alert.setDtVerifiedat(now);
        alert.setJsnVerificationwarnings(List.of(warning));
        alert.setJsnInterpretedeventnames(List.of());
        alert.setJsnSafetychecks(List.of(
                "No executable code generated.",
                "No Agent Definition created.",
                "No Suggestion created."));
        alert.setDtUpdatedat(now);
        applyVerifiedInterpreterMetadata(alert, new AlertVerificationOutcome(
                AlertVerificationDecision.ERROR,
                alert.getDscVerificationsummary(),
                null,
                0.0,
                provider,
                model,
                "alert-verify-mvp-v1",
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
                List.of(warning),
                toStringList(alert.getJsnSafetychecks())));
        flush();
        return Optional.of(toAlertDetailForVerification(alert));
    }

    String technicalErrorSummary(String warning) {
        if (warning != null && warning.contains("tenant context")) {
            return "Alert verification failed because tenant context was not available for database access.";
        }
        return "Alert verification failed because the AI provider did not respond successfully.";
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

    private boolean entityManagerOpen() {
        try {
            return entityManager != null && entityManager.isOpen();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String currentTenantId() {
        try {
            Class<?> tenantContextClass = Class.forName("it.almaviva.fnd.core.lib.quarkuscommon.multitenancy.TenantContext");
            Object tenantContext = Arc.container().select(tenantContextClass).get();
            Object tenantId = tenantContextClass.getMethod("getTenantId").invoke(tenantContext);
            String value = tenantId instanceof String stringValue ? stringValue : null;
            return value == null || value.isBlank() ? null : value;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return null;
        }
    }

    private AlertSummary toAlertSummary(AlertSummaryView view) {
        AlertSummary summary = new AlertSummary()
                .id(view.getId())
                .name(view.getName())
                .enabled(view.getEnabled())
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .version(view.getVersion());

        if (view.getStatus() != null) {
            summary.status(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus.fromString(view.getStatus()));
        }
        if (view.getInterpreterType() != null) {
            summary.interpreterType(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType.fromString(view.getInterpreterType()));
        }
        if (view.getConfidence() != null) {
            summary.confidence(view.getConfidence().doubleValue());
        }
        summary.lastVerification(toAlertVerificationSummary(view));

        return summary;
    }

    private AlertVerificationSummary toAlertVerificationSummary(AlertSummaryView view) {
        if (view.getVerificationStatus() == null
                && view.getRejectedReason() == null
                && view.getConfidence() == null
                && view.getVerifiedAt() == null) {
            return null;
        }

        AlertVerificationSummary summary = new AlertVerificationSummary()
                .rejectedReason(view.getRejectedReason())
                .confidence(toDouble(view.getConfidence()))
                .verifiedAt(view.getVerifiedAt());

        if (view.getVerificationStatus() != null) {
            summary.status(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus.fromString(view.getVerificationStatus()));
        }

        return summary;
    }

    private AlertDetail toAlertDetail(Alert alert) {
        AlertDetail detail = new AlertDetail()
                .id(alert.getCodAlert())
                .name(alert.getDscName())
                .description(alert.getDscDescription())
                .prompt(alert.getDscPrompt())
                .enabled(alert.getFlgEnabled())
                .createdBy(alert.getCodCreatedby())
                .createdAt(alert.getDtCreatedat())
                .updatedAt(alert.getDtUpdatedat())
                .deletedAt(alert.getDtDeletedat())
                .version(alert.getNumVersion())
                .verification(toAlertVerificationResult(alert))
                .interpreter(toAlertInterpreter(alert))
                .requiredData(findRequiredData(alert.getCodAlert()))
                .schedule(toAlertSchedule(alert))
                .runtime(toAlertRuntimeMetadata(alert))
                .agentDefinitions(findAgentDefinitions(alert));

        if (alert.getSglStatus() != null) {
            detail.status(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus.fromString(alert.getSglStatus().getSglStatus()));
        }

        return detail;
    }

    private AlertDetail toAlertDetailForVerification(Alert alert) {
        AlertDetail detail = new AlertDetail()
                .id(alert.getCodAlert())
                .name(alert.getDscName())
                .description(alert.getDscDescription())
                .prompt(alert.getDscPrompt())
                .enabled(alert.getFlgEnabled())
                .createdBy(alert.getCodCreatedby())
                .createdAt(alert.getDtCreatedat())
                .updatedAt(alert.getDtUpdatedat())
                .deletedAt(alert.getDtDeletedat())
                .version(alert.getNumVersion())
                .verification(toAlertVerificationResult(alert))
                .interpreter(toAlertInterpreter(alert))
                .requiredData(findRequiredData(alert.getCodAlert()))
                .schedule(toAlertSchedule(alert))
                .runtime(toAlertRuntimeMetadata(alert))
                .agentDefinitions(List.of());

        if (alert.getSglStatus() != null) {
            detail.status(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertStatus.fromString(alert.getSglStatus().getSglStatus()));
        }

        return detail;
    }

    private void replaceInterpretedTargetTypes(Alert alert, List<String> interpretedTargetTypes) {
        entityManager.createQuery("""
                        delete from AlertTargetTypeRel targetType
                        where targetType.id.codAlert = :alertId
                        """)
                .setParameter("alertId", alert.getCodAlert())
                .executeUpdate();

        for (String interpretedTargetType : interpretedTargetTypes) {
            String dbTargetType = toDbTargetType(interpretedTargetType);
            AlertTargetTypeRelId id = new AlertTargetTypeRelId();
            id.setCodAlert(alert.getCodAlert());
            id.setSglTargettype(dbTargetType);

            AlertTargetTypeRel targetTypeRel = new AlertTargetTypeRel();
            targetTypeRel.setId(id);
            targetTypeRel.setCodAlert(alert);
            targetTypeRel.setSglTargettype(entityManager.getReference(
                    it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.SuggestionTargetType.class,
                    dbTargetType));
            entityManager.persist(targetTypeRel);
        }
    }

    private String toDbTargetType(String interpretedTargetType) {
        if ("SERVICE_DATA_JOURNEY".equals(interpretedTargetType)) {
            return "JOURNEY";
        }
        if ("SERVICE_DATA_JOURNEY_AGGREGATE".equals(interpretedTargetType)) {
            return "JOURNEY_GROUP";
        }
        if ("MONITORED_AUDIO_MESSAGE".equals(interpretedTargetType)) {
            return "AUDIO_MESSAGE";
        }
        if ("MONITORED_AUDIO_MESSAGE_AGGREGATE".equals(interpretedTargetType)) {
            return "AUDIO_MESSAGE_GROUP";
        }
        return interpretedTargetType;
    }

    private void persistAlertVersionHistorySnapshot(
            Alert alert,
            Map<String, Object> technicalSpecification,
            Map<String, Object> agentBlueprintPreview,
            OffsetDateTime now) {
        AlertVersionHistoryId id = new AlertVersionHistoryId();
        id.setCodAlert(alert.getCodAlert());
        id.setNumVersion(alert.getNumVersion());

        AlertVersionHistory history = entityManager.find(AlertVersionHistory.class, id);
        boolean isNewSnapshot = false;
        if (history == null) {
            history = new AlertVersionHistory();
            history.setId(id);
            history.setCodAlert(alert);
            history.setDtCreatedat(now);
            isNewSnapshot = true;
        }

        history.setDscName(alert.getDscName());
        history.setDscDescription(alert.getDscDescription());
        history.setDscPrompt(alert.getDscPrompt());
        history.setSglVerificationstatus(alert.getSglVerificationstatus());
        history.setJsnTechnicalspecification(technicalSpecification);
        history.setJsnAgentblueprintpreview(agentBlueprintPreview);
        history.setCodCreatedby(alert.getCodCreatedby());

        if (isNewSnapshot) {
            entityManager.persist(history);
        }
    }

    private AlertVerificationResult toAlertVerificationResult(Alert alert) {
        if (alert.getSglVerificationstatus() == null
                && alert.getDscVerificationsummary() == null
                && alert.getDscRejectedreason() == null
                && alert.getNumVerificationconfidence() == null
                && alert.getDtVerifiedat() == null) {
            return null;
        }

        AlertVerificationResult verification = new AlertVerificationResult()
                .summary(alert.getDscVerificationsummary())
                .rejectedReason(alert.getDscRejectedreason())
                .confidence(toDouble(alert.getNumVerificationconfidence()))
                .interpretedTargetTypes(findTargetTypes(alert.getCodAlert()))
                .interpretedEventNames(toStringList(alert.getJsnInterpretedeventnames()))
                .requiredTools(findRequiredTools(alert.getCodAlert()))
                .safetyChecks(toStringList(alert.getJsnSafetychecks()))
                .warnings(toStringList(alert.getJsnVerificationwarnings()))
                .verifiedAt(alert.getDtVerifiedat())
                .promptCode(alert.getCodPrompt())
                .promptVersion(alert.getDscPromptversion())
                .llmProvider(alert.getDscLlmprovider())
                .llmModel(alert.getDscLlmmodel());

        if (alert.getSglVerificationstatus() != null) {
            verification.status(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationStatus.fromString(alert.getSglVerificationstatus().getSglVerificationstatus()));
        }

        return verification;
    }

    private AlertInterpreter toAlertInterpreter(Alert alert) {
        if (alert.getSglInterpretertype() == null
                && alert.getDscInterpreterclassname() == null
                && alert.getDscContractversion() == null
                && alert.getCodCoderef() == null
                && alert.getDscImplementationsummary() == null
                && alert.getDscInputmodel() == null
                && alert.getDscOutputmodel() == null) {
            return null;
        }

        AlertInterpreter interpreter = new AlertInterpreter()
                .className(alert.getDscInterpreterclassname())
                .contractVersion(alert.getDscContractversion())
                .codeRef(alert.getCodCoderef())
                .implementationSummary(alert.getDscImplementationsummary())
                .inputModel(alert.getDscInputmodel())
                .outputModel(alert.getDscOutputmodel());

        if (alert.getSglInterpretertype() != null) {
            interpreter.interpreterType(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType.fromString(alert.getSglInterpretertype().getSglInterpretertype()));
        }

        return interpreter;
    }

    private AlertSchedule toAlertSchedule(Alert alert) {
        if (alert.getNumFrequencyseconds() == null
                && alert.getNumTimewindowminutes() == null
                && alert.getFlgEnabledonlyinservicehours() == null
                && alert.getDscCronexpression() == null) {
            return null;
        }

        return new AlertSchedule()
                .frequencySeconds(alert.getNumFrequencyseconds())
                .timeWindowMinutes(alert.getNumTimewindowminutes())
                .enabledOnlyInServiceHours(alert.getFlgEnabledonlyinservicehours())
                .cronExpression(alert.getDscCronexpression());
    }

    private AlertRuntimeMetadata toAlertRuntimeMetadata(Alert alert) {
        AlertRuntimeMetadata runtime = new AlertRuntimeMetadata()
                .lastExecutionAt(alert.getDtLastexecutionat())
                .executionCount(alert.getNumExecutioncount())
                .errorMessage(alert.getDscRuntimeerrormessage());

        if (alert.getSglDeploymentstatus() != null) {
            runtime.deploymentStatus(AlertRuntimeMetadata.DeploymentStatusEnum.fromString(alert.getSglDeploymentstatus().getSglDeploymentstatus()));
        }
        if (alert.getSglLastexecutionstatus() != null) {
            runtime.lastExecutionStatus(AlertRuntimeMetadata.LastExecutionStatusEnum.fromString(alert.getSglLastexecutionstatus().getSglExecutionstatus()));
        }
        if (alert.getCodLastgeneratedsuggestion() != null) {
            runtime.lastGeneratedSuggestionId(alert.getCodLastgeneratedsuggestion().getCodSuggestion());
        }

        return runtime;
    }

    private List<AlertRequiredData> findRequiredData(String alertId) {
        return entityManager.createQuery("""
                        select requiredData
                        from AlertRequiredDatum requiredData
                        join fetch requiredData.sglCategory category
                        where requiredData.id.codAlert = :alertId
                        order by category.numSortorder
                        """, AlertRequiredDatum.class)
                .setParameter("alertId", alertId)
                .getResultList()
                .stream()
                .map(this::toAlertRequiredData)
                .toList();
    }

    private AlertRequiredData toAlertRequiredData(AlertRequiredDatum requiredData) {
        return new AlertRequiredData()
                .category(AlertRequiredData.CategoryEnum.fromString(requiredData.getSglCategory().getSglCategory()))
                .required(requiredData.getFlgRequired())
                .description(requiredData.getDscDescription());
    }

    private List<SuggestionTargetType> findTargetTypes(String alertId) {
        return entityManager.createQuery("""
                        select targetType
                        from AlertTargetTypeRel targetType
                        where targetType.id.codAlert = :alertId
                        order by targetType.id.sglTargettype
                        """, AlertTargetTypeRel.class)
                .setParameter("alertId", alertId)
                .getResultList()
                .stream()
                .map(targetType -> toSuggestionTargetType(targetType.getId().getSglTargettype()))
                .toList();
    }

    private SuggestionTargetType toSuggestionTargetType(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "JOURNEY" -> SuggestionTargetType.SERVICE_DATA_JOURNEY;
            case "JOURNEY_GROUP" -> SuggestionTargetType.SERVICE_DATA_JOURNEY_AGGREGATE;
            case "AUDIO_MESSAGE" -> SuggestionTargetType.MONITORED_AUDIO_MESSAGE;
            case "AUDIO_MESSAGE_GROUP" -> SuggestionTargetType.MONITORED_AUDIO_MESSAGE_AGGREGATE;
            default -> SuggestionTargetType.fromString(value);
        };
    }

    private List<ToolReference> findRequiredTools(String alertId) {
        return entityManager.createQuery("""
                        select requiredTool
                        from AlertRequiredTool requiredTool
                        where requiredTool.id.codAlert = :alertId
                        order by requiredTool.id.dscToolname
                        """, AlertRequiredTool.class)
                .setParameter("alertId", alertId)
                .getResultList()
                .stream()
                .map(requiredTool -> new ToolReference()
                        .toolName(requiredTool.getId().getDscToolname())
                        .operations(toStringList(requiredTool.getJsnOperations())))
                .toList();
    }

    private List<AgentDefinitionSummary> findAgentDefinitions(Alert alert) {
        return entityManager.createQuery("""
                        select agentDefinition
                        from AgentDefinition agentDefinition
                        join fetch agentDefinition.codAgentprofile profile
                        join fetch agentDefinition.sglStatus status
                        join fetch agentDefinition.sglGenerationmode generationMode
                        left join fetch agentDefinition.sglLatestcompilationstatus compilationStatus
                        where agentDefinition.codAlert.codAlert = :alertId
                        and agentDefinition.dtArchivedat is null
                        order by agentDefinition.dtCreatedat desc
                        """, it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition.class)
                .setParameter("alertId", alert.getCodAlert())
                .getResultList()
                .stream()
                .map(agentDefinition -> toAgentDefinitionSummary(alert, agentDefinition))
                .toList();
    }

    private AgentDefinitionSummary toAgentDefinitionSummary(
            Alert alert,
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition agentDefinition) {
        AgentDefinitionSummary summary = new AgentDefinitionSummary()
                .id(agentDefinition.getCodAgentdefinition())
                .name(agentDefinition.getDscName())
                .description(agentDefinition.getDscDescription())
                .alert(new AlertReference()
                        .id(alert.getCodAlert())
                        .name(alert.getDscName()))
                .alertVersion(agentDefinition.getNumAlertversion())
                .profile(toAgentProfile(agentDefinition.getCodAgentprofile()))
                .createdAt(agentDefinition.getDtCreatedat())
                .updatedAt(agentDefinition.getDtUpdatedat());

        if (agentDefinition.getSglStatus() != null) {
            summary.status(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus.fromString(agentDefinition.getSglStatus().getSglStatus()));
        }
        if (agentDefinition.getSglGenerationmode() != null) {
            summary.generationMode(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode.fromString(agentDefinition.getSglGenerationmode().getSglGenerationmode()));
        }
        if (agentDefinition.getSglLatestcompilationstatus() != null || agentDefinition.getDscLatestcompilationstep() != null || agentDefinition.getDtLatestcompilationcompletedat() != null) {
            AgentCompilationSummary compilation = new AgentCompilationSummary()
                    .currentStep(agentDefinition.getDscLatestcompilationstep())
                    .completedAt(agentDefinition.getDtLatestcompilationcompletedat());
            if (agentDefinition.getSglLatestcompilationstatus() != null) {
                compilation.status(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus.fromString(agentDefinition.getSglLatestcompilationstatus().getSglStatus()));
            }
            summary.compilation(compilation);
        }

        return summary;
    }

    private AgentProfile toAgentProfile(it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentProfile profile) {
        return new AgentProfile()
                .id(profile.getCodAgentprofile())
                .name(profile.getDscName())
                .description(profile.getDscDescription())
                .enabled(profile.getFlgEnabled())
                .recommendedFor(profile.getJsnRecommendedfor().stream().map(String::valueOf).toList())
                .cpuRequestMillicores(profile.getNumCpurequestmillicores())
                .cpuLimitMillicores(profile.getNumCpulimitmillicores())
                .memoryRequestMiB(profile.getNumMemoryrequestmib())
                .memoryLimitMiB(profile.getNumMemorylimitmib())
                .networkPolicy(profile.getDscNetworkpolicy())
                .maxRuntimeConcurrency(profile.getNumMaxruntimeconcurrency());
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private List<String> toStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        if (value instanceof Map<?, ?> map) {
            Object nestedValues = firstPresent(map, "items", "values", "operations", "warnings", "safetyChecks", "eventNames");
            if (nestedValues != null) {
                return toStringList(nestedValues);
            }
            return map.values().stream().map(String::valueOf).toList();
        }
        return List.of(String.valueOf(value));
    }

    private Object firstPresent(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private Map<String, Object> toStringObjectMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, nestedValue) -> result.put(String.valueOf(key), nestedValue));
        return result;
    }

    private String normalizeName(String name) {
        return Normalizer.normalize(name == null ? "" : name, Normalizer.Form.NFKC)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private boolean isVerified(Alert alert) {
        return alert.getSglStatus() != null && "VERIFIED".equals(alert.getSglStatus().getSglStatus());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
