package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
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
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationResult;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertVerificationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.SuggestionTargetType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.query.AlertSearchCriteria;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertRequiredDatum;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertRequiredTool;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertTargetTypeRel;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.view.AlertSummaryView;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class AlertRepository implements PanacheRepositoryBase<Alert, String> {

    @Inject
    EntityManager entityManager;

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

    public Optional<AlertDetail> verifyAlert(String alertId, AlertVerificationRequest request) {
        Optional<Alert> maybeAlert = find("codAlert = ?1 and dtDeletedat is null", alertId).firstResultOptional();
        if (maybeAlert.isEmpty()) {
            return Optional.empty();
        }

        Alert alert = maybeAlert.get();
        OffsetDateTime now = OffsetDateTime.now();

        System.out.println("[IIA][ALERT_VERIFY] Starting verification for alertId=" + alertId);

        alert.setSglStatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus.class,
                "VERIFYING"));
        alert.setDtUpdatedat(now);
        flush();

        alert.setSglStatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertStatus.class,
                "VERIFIED"));
        alert.setSglVerificationstatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus.class,
                "VERIFIED"));
        alert.setDscVerificationsummary("Mock alert verification completed successfully.");
        alert.setDscRejectedreason(null);
        alert.setNumVerificationconfidence(BigDecimal.valueOf(0.80));
        alert.setSglInterpretertype(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertInterpreterType.class,
                "EVENT_INTERPRETER"));
        alert.setDscInputmodel("ServiceDataV2");
        alert.setDscOutputmodel("AgentOutput.CANDIDATE_SUGGESTION");
        alert.setDscLlmprovider("mock");
        alert.setDscLlmmodel("mock-alert-verify");
        alert.setDscPromptversion("alert-verify-mvp-v1");
        alert.setDtVerifiedat(now);
        alert.setJsnInterpretedeventnames(List.of("MOCK_SERVICE_DATA_EVENT"));
        alert.setJsnVerificationwarnings(List.of());
        alert.setJsnSafetychecks(List.of(
                "No executable code generated.",
                "No Agent Definition created.",
                "No Suggestion created."));
        alert.setJsnTechnicalspecification(Map.of(
                "type", "mock-alert-verification",
                "version", "alert-verify-mvp-v1",
                "prompt", alert.getDscPrompt(),
                "decision", "VERIFIED"));
        alert.setJsnAgentblueprintpreview(Map.of(
                "agentName", alert.getDscName(),
                "triggerType", "EVENT",
                "requiredSources", List.of("SERVICE_DATA"),
                "targetTypes", List.of(),
                "parameters", Map.of("mock", true)));
        alert.setDtUpdatedat(now);

        flush();

        System.out.println("[IIA][ALERT_VERIFY] Verification completed for alertId=" + alertId + " decision=VERIFIED");
        return Optional.of(toAlertDetailForVerification(alert));
    }

    public boolean existsActiveAlertWithNormalizedName(String name) {
        String normalizedName = normalizeName(name);
        return count("""
                lower(trim(dscName)) = ?1
                and dtDeletedat is null
                and sglStatus.sglStatus <> 'DELETED'
                """, normalizedName) > 0;
    }

    public AlertDetail createDraftAlert(AlertCreateRequest request) {
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
                "DRAFT"));
        alert.setSglVerificationstatus(entityManager.getReference(
                it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AlertVerificationStatus.class,
                "PENDING"));
        alert.setDtCreatedat(now);
        alert.setDtUpdatedat(now);

        persist(alert);
        flush();
        return toAlertDetail(alert);
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

    private String normalizeName(String name) {
        return Normalizer.normalize(name == null ? "" : name, Normalizer.Form.NFKC)
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
