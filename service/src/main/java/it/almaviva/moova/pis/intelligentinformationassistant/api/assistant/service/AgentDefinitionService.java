package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionCreateRequest;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionListResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatusResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentProfile;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DayOfWeek;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentCompilationStatuses;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentDefinitionRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.AgentProfileRepository;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.Alert;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AgentDefinitionService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final String COMPILE_NOT_IMPLEMENTED_MESSAGE =
            "Agent compilation pipeline is not implemented yet. Create the Agent Definition with compileImmediately=false.";
    private static final String SCHEDULED_TOOL = "SERVICE_DATA_API.POST_/v2/stoppointjourneys";
    private static final String DSL_ARTIFACT_NOT_IMPLEMENTED = "DSL artifact generation is not implemented yet.";
    private static final String DSL_RUNTIME_COMPATIBILITY_VALIDATOR_NOT_IMPLEMENTED =
            "DSL runtime compatibility validator is not implemented yet.";
    private static final String SCHEDULED_DSL_ARTIFACT_NOT_IMPLEMENTED =
            "Scheduled DSL artifact generation is not implemented yet.";

    @Inject
    AgentDefinitionRepository agentDefinitionRepository;

    @Inject
    AgentCompilationRepository agentCompilationRepository;

    @Inject
    AgentProfileRepository agentProfileRepository;

    @Inject
    AgentProfileMapper agentProfileMapper;

    @Inject
    AgentDefinitionMapper agentDefinitionMapper;

    @Inject
    AgentCompilationMapper agentCompilationMapper;

    @Inject
    AgentCompilationPreconditionValidator agentCompilationPreconditionValidator;

    @Inject
    AgentDslArtifactBuilder agentDslArtifactBuilder;

    @Transactional
    public AgentDefinitionDetail getAgentDefinition(String agentDefinitionId) {
        if (isBlank(agentDefinitionId)) {
            throw invalid("agentDefinitionId", "The agentDefinitionId path parameter is empty or contains only whitespace characters.");
        }
        String id = agentDefinitionId.trim();
        System.out.println("[IIA][AGENT_DEFINITION_GET] requested agentDefinitionId=" + id);

        AgentDefinition definition = agentDefinitionRepository.findByDefinitionId(id)
                .orElseThrow(() -> {
                    System.out.println("[IIA][AGENT_DEFINITION_GET][LOAD] agentDefinitionId=" + id + " found=false");
                    return new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found.");
                });

        RuntimeShape runtimeShape = runtimeShape(definition);

        System.out.println("[IIA][AGENT_DEFINITION_GET][LOAD] agentDefinitionId=" + id
                + " found=true"
                + " status=" + (definition.getSglStatus() == null ? null : definition.getSglStatus().getSglStatus())
                + " alertId=" + (definition.getCodAlert() == null ? null : definition.getCodAlert().getCodAlert())
                + " alertVersion=" + definition.getNumAlertversion()
                + " agentProfileId=" + (definition.getCodAgentprofile() == null ? null : definition.getCodAgentprofile().getCodAgentprofile())
                + " interpreterType=" + runtimeShape.interpreterType()
                + " triggerType=" + runtimeShape.triggerType()
                + " inputModel=" + definition.getDscInputmodel()
                + " outputModel=" + definition.getDscOutputmodel());

        AgentDefinitionDetail detail = agentDefinitionMapper.toDto(definition, runtimeShape.interpreterType(), runtimeShape.triggerType());
        System.out.println("[IIA][AGENT_DEFINITION_GET][MAP] agentDefinitionId=" + id
                + " status=" + (detail == null ? null : detail.getStatus())
                + " alertId=" + (detail == null || detail.getAlert() == null ? null : detail.getAlert().getId())
                + " alertVersion=" + (detail == null ? null : detail.getAlertVersion())
                + " agentProfileId=" + (detail == null || detail.getProfile() == null ? null : detail.getProfile().getId())
                + " interpreterType=" + (detail == null ? null : detail.getInterpreterType())
                + " triggerType=" + (detail == null ? null : detail.getTriggerType())
                + " inputModel=" + (detail == null ? null : detail.getInputModel())
                + " outputModel=" + (detail == null ? null : detail.getOutputModel()));
        return detail;
    }

    @Transactional
    public AgentCompilationStatusResponse getAgentDefinitionCompilation(String agentDefinitionId) {
        if (isBlank(agentDefinitionId)) {
            throw invalid("agentDefinitionId", "The agentDefinitionId path parameter is empty or contains only whitespace characters.");
        }
        String id = agentDefinitionId.trim();
        if (id.length() > 50) {
            throw invalid("agentDefinitionId", "The agentDefinitionId path parameter exceeds 50 characters.");
        }

        System.out.println("[IIA][AGENT_COMPILATION][GET] start agentDefinitionId=" + id);
        AgentDefinition definition = agentDefinitionRepository.findByDefinitionId(id)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
        System.out.println("[IIA][AGENT_COMPILATION][GET] agent definition found agentDefinitionId=" + id);

        var compilation = agentCompilationRepository.findLatestByAgentDefinitionId(id)
                .orElseThrow(() -> {
                    System.out.println("[IIA][AGENT_COMPILATION][GET] no compilation found agentDefinitionId=" + id);
                    return new AgentDefinitionNotFoundException(
                            "agentDefinitionId",
                            "No compilation found for Agent Definition " + id + ".");
                });
        System.out.println("[IIA][AGENT_COMPILATION][GET] latest compilation found agentDefinitionId=" + id
                + " compilationId=" + compilation.getCodAgentcompilation()
                + " status=" + (compilation.getSglStatus() == null ? null : compilation.getSglStatus().getSglStatus()));

        var steps = agentCompilationRepository.findStepsByCompilationId(compilation.getCodAgentcompilation());
        AgentCompilationStatusResponse response = agentCompilationMapper.toResponse(definition, compilation, steps);
        System.out.println("[IIA][AGENT_COMPILATION][GET] response mapped agentDefinitionId=" + id + " steps=" + steps.size());
        return response;
    }

    @Transactional
    public AgentCompilationStatusResponse compileAgentDefinition(
            String agentDefinitionId,
            AgentCompilationRequest request) {
        if (isBlank(agentDefinitionId)) {
            throw invalid("agentDefinitionId", "The agentDefinitionId path parameter is empty or contains only whitespace characters.");
        }
        String id = agentDefinitionId.trim();
        if (id.length() > 50) {
            throw invalid("agentDefinitionId", "The agentDefinitionId path parameter exceeds 50 characters.");
        }

        System.out.println("[IIA][AGENT_COMPILATION][POST] start agentDefinitionId=" + id);
        boolean force = request != null && Boolean.TRUE.equals(request.getForce());
        boolean runSimulation = request != null && Boolean.TRUE.equals(request.getRunSimulation());
        AgentGenerationMode requestedMode = request == null ? null : request.getGenerationMode();
        String note = request == null ? null : trimToNull(request.getNote());
        System.out.println("[IIA][AGENT_COMPILATION][POST] request force=" + force
                + " generationMode=" + requestedMode
                + " runSimulation=" + runSimulation);

        AgentDefinition definition = agentDefinitionRepository.findByDefinitionId(id)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentDefinitionId", "Agent Definition not found."));
        String status = definition.getSglStatus() == null ? null : definition.getSglStatus().getSglStatus();
        System.out.println("[IIA][AGENT_COMPILATION][POST] agent definition found agentDefinitionId=" + id + " status=" + status);
        validateCompilableStatus(id, status);

        if (agentCompilationRepository.existsRunningCompilation(id)) {
            if (force) {
                throw rejectedCompilation(
                        AgentCompilationRejectedException.Reason.CONFLICT,
                        "Force recompilation of a running compilation is not implemented yet.");
            }
            throw rejectedCompilation(
                    AgentCompilationRejectedException.Reason.CONFLICT,
                    "A compilation is already running for Agent Definition " + id + ".");
        }

        String effectiveGenerationMode = effectiveCompilationGenerationMode(definition, requestedMode);
        System.out.println("[IIA][AGENT_COMPILATION][POST] effectiveGenerationMode=" + effectiveGenerationMode);

        OffsetDateTime requestedAt = OffsetDateTime.now();
        Map<String, Object> requestJson = new LinkedHashMap<>();
        requestJson.put("force", force);
        requestJson.put("generationMode", effectiveGenerationMode);
        requestJson.put("requestedGenerationMode", requestedMode == null ? effectiveGenerationMode : requestedMode.toString());
        requestJson.put("effectiveGenerationMode", effectiveGenerationMode);
        requestJson.put("runSimulation", runSimulation);
        if (note != null) {
            requestJson.put("note", note);
        }
        requestJson.put("skeleton", true);
        requestJson.put("agentDefinitionId", id);
        requestJson.put("requestedAt", requestedAt.toString());
        requestJson.put("apiVersion", "v1");
        requestJson.put("phase", "AGENT_COMPILATION_DSL_SKELETON");

        var compilation = agentCompilationRepository.createCompilation(
                id,
                effectiveGenerationMode,
                force,
                requestJson,
                null);
        String compilationId = compilation.getCodAgentcompilation();
        System.out.println("[IIA][AGENT_COMPILATION][POST] compilation created compilationId=" + compilationId);

        agentCompilationRepository.addStep(
                compilationId,
                1,
                "REQUEST_ACCEPTED",
                AgentCompilationStatuses.READY,
                "Compilation request accepted by api-assistant control plane.",
                Map.of("skeleton", true),
                requestedAt,
                requestedAt);
        System.out.println("[IIA][AGENT_COMPILATION][POST] step added compilationId=" + compilationId + " step=REQUEST_ACCEPTED status=READY");

        OffsetDateTime validationStartedAt = OffsetDateTime.now();
        agentCompilationRepository.markStarted(
                compilationId,
                AgentCompilationStatuses.VALIDATING_BLUEPRINT,
                AgentCompilationStatuses.VALIDATING_BLUEPRINT,
                validationStartedAt);

        AgentCompilationPreconditionValidationResult validation =
                agentCompilationPreconditionValidator.validate(definition, effectiveGenerationMode);
        if (!validation.valid()) {
            agentCompilationRepository.addStep(
                    compilationId,
                    2,
                    "VALIDATING_BLUEPRINT",
                    AgentCompilationStatuses.REJECTED,
                    "Compilation preconditions rejected.",
                    validation.toDetailsJson(),
                    validationStartedAt,
                    OffsetDateTime.now());
            System.out.println("[IIA][AGENT_COMPILATION][POST] preconditions rejected compilationId="
                    + compilationId + " errors=" + validation.errors());

            OffsetDateTime completedAt = OffsetDateTime.now();
            String errorMessage = String.join("; ", validation.errors());
            agentCompilationRepository.markFailed(
                    compilationId,
                    AgentCompilationStatuses.REJECTED,
                    AgentCompilationStatuses.VALIDATING_BLUEPRINT,
                    errorMessage,
                    Map.of(
                            "preconditionsValid", false,
                            "artifactGenerated", false,
                            "agentDefinitionStatusUpdated", false,
                            "errors", validation.errors(),
                            "warnings", validation.warnings()),
                    completedAt);
            System.out.println("[IIA][AGENT_COMPILATION][POST] completed skeleton compilationId="
                    + compilationId + " finalStatus=REJECTED");

            var latestCompilation = agentCompilationRepository.findLatestByAgentDefinitionId(id)
                    .orElse(compilation);
            var steps = agentCompilationRepository.findStepsByCompilationId(latestCompilation.getCodAgentcompilation());
            return agentCompilationMapper.toResponse(definition, latestCompilation, steps);
        }

        agentCompilationRepository.addStep(
                compilationId,
                2,
                "VALIDATING_BLUEPRINT",
                AgentCompilationStatuses.READY,
                "Compilation preconditions validated successfully.",
                validation.toDetailsJson(),
                validationStartedAt,
                OffsetDateTime.now());
        System.out.println("[IIA][AGENT_COMPILATION][POST] preconditions valid compilationId=" + compilationId);
        System.out.println("[IIA][AGENT_COMPILATION][POST] step added compilationId=" + compilationId + " step=VALIDATING_BLUEPRINT status=READY");

        OffsetDateTime artifactStartedAt = OffsetDateTime.now();
        agentCompilationRepository.updateStatus(
                compilationId,
                AgentCompilationStatuses.GENERATING_ARTIFACT,
                AgentCompilationStatuses.GENERATING_ARTIFACT);
        if ("SCHEDULED_INTERPRETER".equals(validation.interpreterType())) {
            agentCompilationRepository.addStep(
                    compilationId,
                    3,
                    "GENERATING_ARTIFACT",
                    AgentCompilationStatuses.FAILED,
                    SCHEDULED_DSL_ARTIFACT_NOT_IMPLEMENTED,
                    Map.of(
                            "artifactGenerationImplemented", false,
                            "nextImplementationStep", "Scheduled AgentDslArtifactBuilder"),
                    artifactStartedAt,
                    OffsetDateTime.now());
            System.out.println("[IIA][AGENT_COMPILATION][POST] step added compilationId=" + compilationId + " step=GENERATING_ARTIFACT status=FAILED");

            OffsetDateTime completedAt = OffsetDateTime.now();
            agentCompilationRepository.markFailed(
                    compilationId,
                    AgentCompilationStatuses.FAILED,
                    AgentCompilationStatuses.GENERATING_ARTIFACT,
                    SCHEDULED_DSL_ARTIFACT_NOT_IMPLEMENTED,
                    Map.of(
                            "preconditionsValid", true,
                            "artifactGenerated", false,
                            "interpreterType", "SCHEDULED_INTERPRETER",
                            "agentDefinitionStatusUpdated", false,
                            "reason", SCHEDULED_DSL_ARTIFACT_NOT_IMPLEMENTED),
                    completedAt);
            System.out.println("[IIA][AGENT_COMPILATION][POST] completed skeleton compilationId=" + compilationId + " finalStatus=FAILED");

            var latestCompilation = agentCompilationRepository.findLatestByAgentDefinitionId(id)
                    .orElse(compilation);
            var steps = agentCompilationRepository.findStepsByCompilationId(latestCompilation.getCodAgentcompilation());
            return agentCompilationMapper.toResponse(definition, latestCompilation, steps);
        }

        System.out.println("[IIA][AGENT_COMPILATION][POST] generating event DSL artifact compilationId=" + compilationId);
        AgentDslArtifactBuildResult buildResult =
                agentDslArtifactBuilder.buildEventArtifact(definition, validation, artifactStartedAt);
        if (!buildResult.success()) {
            agentCompilationRepository.addStep(
                    compilationId,
                    3,
                    "GENERATING_ARTIFACT",
                    AgentCompilationStatuses.FAILED,
                    buildResult.errorMessage(),
                    Map.of(
                            "artifactGenerated", false,
                            "interpreterType", validation.interpreterType(),
                            "reason", buildResult.errorMessage()),
                    artifactStartedAt,
                    OffsetDateTime.now());
            System.out.println("[IIA][AGENT_COMPILATION][POST] step added compilationId=" + compilationId + " step=GENERATING_ARTIFACT status=FAILED");

            OffsetDateTime completedAt = OffsetDateTime.now();
            agentCompilationRepository.markFailed(
                    compilationId,
                    AgentCompilationStatuses.FAILED,
                    AgentCompilationStatuses.GENERATING_ARTIFACT,
                    buildResult.errorMessage(),
                    Map.of(
                            "preconditionsValid", true,
                            "artifactGenerated", false,
                            "interpreterType", validation.interpreterType(),
                            "agentDefinitionStatusUpdated", false,
                            "reason", buildResult.errorMessage()),
                    completedAt);
            System.out.println("[IIA][AGENT_COMPILATION][POST] completed skeleton compilationId=" + compilationId + " finalStatus=FAILED");

            var latestCompilation = agentCompilationRepository.findLatestByAgentDefinitionId(id)
                    .orElse(compilation);
            var steps = agentCompilationRepository.findStepsByCompilationId(latestCompilation.getCodAgentcompilation());
            return agentCompilationMapper.toResponse(definition, latestCompilation, steps);
        }

        AgentDslArtifact artifact = buildResult.artifact();
        agentCompilationRepository.addStep(
                compilationId,
                3,
                "GENERATING_ARTIFACT",
                AgentCompilationStatuses.READY,
                "Event DSL artifact generated successfully.",
                Map.of(
                        "schemaVersion", artifact.schemaVersion(),
                        "artifactType", artifact.artifactType(),
                        "interpreterType", artifact.interpreterType(),
                        "triggerType", artifact.triggerType(),
                        "inputModel", artifact.inputModel(),
                        "evaluationMode", artifact.evaluationMode(),
                        "artifactGenerated", true),
                artifactStartedAt,
                OffsetDateTime.now());
        System.out.println("[IIA][AGENT_COMPILATION][POST] event DSL artifact generated compilationId=" + compilationId);
        System.out.println("[IIA][AGENT_COMPILATION][POST] step added compilationId=" + compilationId + " step=GENERATING_ARTIFACT status=READY");

        OffsetDateTime staticAnalysisStartedAt = OffsetDateTime.now();
        agentCompilationRepository.updateStatus(
                compilationId,
                AgentCompilationStatuses.STATIC_ANALYSIS,
                AgentCompilationStatuses.STATIC_ANALYSIS);
        agentCompilationRepository.addStep(
                compilationId,
                4,
                "STATIC_ANALYSIS",
                AgentCompilationStatuses.FAILED,
                DSL_RUNTIME_COMPATIBILITY_VALIDATOR_NOT_IMPLEMENTED,
                Map.of(
                        "runtimeCompatibilityValidatorImplemented", false,
                        "nextImplementationStep", "AgentDslRuntimeCompatibilityValidator"),
                staticAnalysisStartedAt,
                OffsetDateTime.now());

        OffsetDateTime completedAt = OffsetDateTime.now();
        Map<String, Object> resultJson = new LinkedHashMap<>();
        resultJson.put("preconditionsValid", true);
        resultJson.put("artifactGenerated", true);
        resultJson.put("artifactType", artifact.artifactType());
        resultJson.put("schemaVersion", artifact.schemaVersion());
        resultJson.put("interpreterType", artifact.interpreterType());
        resultJson.put("triggerType", artifact.triggerType());
        resultJson.put("inputModel", artifact.inputModel());
        resultJson.put("evaluationMode", artifact.evaluationMode());
        resultJson.put("runtimeCompatibilityValidated", false);
        resultJson.put("agentDefinitionStatusUpdated", false);
        resultJson.put("dslArtifact", artifact.artifact());
        resultJson.put("reason", DSL_RUNTIME_COMPATIBILITY_VALIDATOR_NOT_IMPLEMENTED);
        agentCompilationRepository.markFailed(
                compilationId,
                AgentCompilationStatuses.FAILED,
                AgentCompilationStatuses.STATIC_ANALYSIS,
                DSL_RUNTIME_COMPATIBILITY_VALIDATOR_NOT_IMPLEMENTED,
                resultJson,
                completedAt);
        System.out.println("[IIA][AGENT_COMPILATION][POST] static analysis pending compilationId=" + compilationId + " finalStatus=FAILED");

        var latestCompilation = agentCompilationRepository.findLatestByAgentDefinitionId(id)
                .orElse(compilation);
        var steps = agentCompilationRepository.findStepsByCompilationId(latestCompilation.getCodAgentcompilation());
        return agentCompilationMapper.toResponse(definition, latestCompilation, steps);
    }

    @Transactional
    public AgentDefinitionListResponse searchAgentDefinitions(AgentDefinitionSearchCriteria criteria) {
        AgentDefinitionSearchCriteria normalized = normalizeSearchCriteria(criteria);
        System.out.println("[IIA][AGENT_DEFINITION_SEARCH] requested");
        System.out.println("[IIA][AGENT_DEFINITION_SEARCH][CRITERIA] status=" + normalized.status()
                + " alertId=" + normalized.alertId()
                + " generationMode=" + normalized.generationMode()
                + " profileId=" + normalized.profileId()
                + " textPresent=" + (normalized.text() != null));

        List<AgentDefinition> definitions = agentDefinitionRepository.search(
                normalized.status() == null ? null : normalized.status().toString(),
                normalized.alertId(),
                normalized.generationMode() == null ? null : normalized.generationMode().toString(),
                normalized.profileId(),
                normalized.text());

        var items = definitions.stream()
                .map(definition -> {
                    RuntimeShape runtimeShape = runtimeShape(definition);
                    return agentDefinitionMapper.toSummary(definition, runtimeShape.interpreterType(), runtimeShape.triggerType());
                })
                .toList();

        System.out.println("[IIA][AGENT_DEFINITION_SEARCH][RESULT] status=" + normalized.status()
                + " alertId=" + normalized.alertId()
                + " generationMode=" + normalized.generationMode()
                + " profileId=" + normalized.profileId()
                + " textPresent=" + (normalized.text() != null)
                + " resultCount=" + items.size());
        return new AgentDefinitionListResponse().items(items);
    }

    @Transactional
    public AgentDefinitionDetail createAgentDefinition(AgentDefinitionCreateRequest request) {
        validateBasicRequest(request);

        String alertId = request.getAlertId().trim();
        String agentProfileId = request.getAgentProfileId().trim();
        Integer requestedAlertVersion = request.getAlertVersion();
        boolean compileImmediately = !Boolean.FALSE.equals(request.getCompileImmediately());

        System.out.println("[IIA][AGENT_DEFINITION_CREATE] requested alertId=" + alertId
                + " requestedAlertVersion=" + requestedAlertVersion
                + " agentProfileId=" + agentProfileId
                + " compileImmediately=" + request.getCompileImmediately());

        if (compileImmediately) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.COMPILATION_NOT_IMPLEMENTED,
                    COMPILE_NOT_IMPLEMENTED_MESSAGE);
        }

        AgentGenerationMode generationMode = request.getGenerationMode() == null
                ? AgentGenerationMode.AUTO
                : request.getGenerationMode();
        if (generationMode == AgentGenerationMode.JAVA_TEMPLATE) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.UNSUPPORTED_GENERATION_MODE,
                    "JAVA_TEMPLATE generation mode is not supported in this MVP.");
        }

        validateActivationPolicy(request.getActivationPolicy());

        Alert alert = agentDefinitionRepository.findAlert(alertId)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("alertId", "Alert not found."));
        validateAlert(alert, requestedAlertVersion);
        int effectiveAlertVersion = alert.getNumVersion() == null ? 1 : alert.getNumVersion();

        Map<String, Object> technicalSpecification = technicalSpecification(alert);
        TechnicalContract contract = deriveTechnicalContract(alert, technicalSpecification);
        validateSupportedContract(contract);
        validateScheduledProfileLimits(contract, technicalSpecification);

        var profileEntity = agentProfileRepository.findByProfileId(agentProfileId)
                .orElseThrow(() -> new AgentDefinitionNotFoundException("agentProfileId", "Agent profile not found."));
        AgentProfile profile = agentProfileMapper.toDto(profileEntity);
        if (!Boolean.TRUE.equals(profile.getEnabled())) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.PROFILE_DISABLED,
                    "The selected Agent Profile is disabled.");
        }

        String networkPolicy = isBlank(profile.getNetworkPolicy()) ? "TOOL_GATEWAY_ONLY" : profile.getNetworkPolicy();
        List<String> allowedToolNames = contract.isScheduled() ? List.of(SCHEDULED_TOOL) : List.of();
        Map<String, Object> runtimeContract = runtimeContract(contract, allowedToolNames, networkPolicy);
        System.out.println("[IIA][AGENT_DEFINITION_CREATE][RUNTIME_CONTRACT] alertId=" + alertId
                + " interpreterType=" + contract.interpreterType()
                + " triggerType=" + contract.triggerType()
                + " inputModel=" + contract.inputModel()
                + " outputModel=" + contract.outputModel());

        AgentDefinition definition = new AgentDefinition();
        OffsetDateTime now = OffsetDateTime.now();
        definition.setCodAlert(alert);
        definition.setNumAlertversion(effectiveAlertVersion);
        definition.setCodAgentprofile(profileEntity);
        definition.setDscName(firstNonBlank(request.getName(), alert.getDscName(), "Agent for " + alertId));
        definition.setDscDescription(firstNonBlank(request.getDescription(), alert.getDscDescription(), null));
        definition.setSglStatus(agentDefinitionRepository.statusReference("DRAFT"));
        definition.setSglGenerationmode(agentDefinitionRepository.generationModeReference(generationMode.toString()));
        Map<String, Object> blueprint = blueprint(alert, technicalSpecification, contract);
        definition.setJsnBlueprint(blueprint);
        definition.setJsnDslpreview(mapValue(blueprint.get("dslPreview")));
        definition.setJsnValidationplan(mapValue(blueprint.get("validationPlan")));
        definition.setSglComplexity(agentDefinitionRepository.complexityReference(complexity(blueprint, contract)));
        definition.setJsnRequiredsources(new ArrayList<>(List.of("SERVICE_DATA")));
        definition.setJsnRequiredpermissions(new ArrayList<>(List.of("SERVICE_DATA_READ")));
        definition.setJsnGenerationwarnings(generationWarnings(alert));
        applyActivationPolicy(definition, request.getActivationPolicy());
        definition.setSglArtifacttype(agentDefinitionRepository.artifactTypeReference("NONE"));
        definition.setSglSignaturestatus(agentDefinitionRepository.signatureStatusReference("NOT_SIGNED"));
        definition.setDscInputmodel(contract.inputModel());
        definition.setDscOutputmodel(contract.outputModel());
        definition.setJsnAllowedtools(new ArrayList<>(allowedToolNames));
        definition.setDscNetworkpolicy(networkPolicy);
        definition.setJsnRuntimecontract(runtimeContract);
        definition.setDtCreatedat(now);
        definition.setDtUpdatedat(now);

        System.out.println("[IIA][AGENT_DEFINITION_CREATE][PERSISTENCE] persisting alertId=" + alertId
                + " effectiveAlertVersion=" + effectiveAlertVersion
                + " agentProfileId=" + agentProfileId
                + " status=DRAFT");

        AgentDefinition created = agentDefinitionRepository.create(
                definition,
                List.of("SERVICE_DATA"),
                daysOfWeek(request.getActivationPolicy()),
                allowedToolNames);

        System.out.println("[IIA][AGENT_DEFINITION_CREATE][PERSISTENCE] created alertId=" + alertId
                + " effectiveAlertVersion=" + effectiveAlertVersion
                + " agentProfileId=" + agentProfileId
                + " status=DRAFT"
                + " agentDefinitionId=" + created.getCodAgentdefinition());

        return agentDefinitionMapper.toDto(created, contract.interpreterType(), contract.triggerType());
    }

    private void validateBasicRequest(AgentDefinitionCreateRequest request) {
        if (request == null) {
            throw invalid("body", "The request body is missing.");
        }
        if (isBlank(request.getAlertId())) {
            throw invalid("alertId", "The alertId field is missing, empty or contains only whitespace characters.");
        }
        if (isBlank(request.getAgentProfileId())) {
            throw invalid("agentProfileId", "The agentProfileId field is missing, empty or contains only whitespace characters.");
        }
        if (request.getActivationPolicy() == null) {
            throw invalid("activationPolicy", "The activationPolicy field is required.");
        }
    }

    private AgentDefinitionSearchCriteria normalizeSearchCriteria(AgentDefinitionSearchCriteria criteria) {
        if (criteria == null) {
            return new AgentDefinitionSearchCriteria(null, null, null, null, null);
        }
        String alertId = optionalTrimmed(criteria.alertId(), "alertId", 50);
        String profileId = optionalTrimmed(criteria.profileId(), "profileId", 50);
        String text = optionalTrimmed(criteria.text(), "text", 200);
        return new AgentDefinitionSearchCriteria(
                criteria.status(),
                alertId,
                criteria.generationMode(),
                profileId,
                text);
    }

    private String optionalTrimmed(String value, String source, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw invalid(source, source + " must not exceed " + maxLength + " characters.");
        }
        return trimmed;
    }

    private void validateAlert(Alert alert, Integer requestedAlertVersion) {
        String status = alert.getSglStatus() == null ? null : alert.getSglStatus().getSglStatus();
        String verificationStatus = alert.getSglVerificationstatus() == null ? null : alert.getSglVerificationstatus().getSglVerificationstatus();
        if (alert.getDtDeletedat() != null || "DELETED".equals(status)) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.ALERT_DELETED,
                    "The alert has been deleted and cannot be used to create an Agent Definition.");
        }
        if (!"VERIFIED".equals(status) || !"VERIFIED".equals(verificationStatus)) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.ALERT_NOT_VERIFIED,
                    "Only an Alert with status VERIFIED and verificationStatus VERIFIED can create an Agent Definition.");
        }
        int currentVersion = alert.getNumVersion() == null ? 1 : alert.getNumVersion();
        if (requestedAlertVersion != null && requestedAlertVersion != currentVersion) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.ALERT_VERSION_MISMATCH,
                    "The requested Alert version is not the current Alert version.");
        }
    }

    private Map<String, Object> technicalSpecification(Alert alert) {
        Map<String, Object> technicalSpecification = mapValue(alert.getJsnTechnicalspecification());
        if (technicalSpecification == null || technicalSpecification.isEmpty()) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.MISSING_TECHNICAL_SPECIFICATION,
                    "The verified Alert does not contain a valid technicalSpecification.");
        }
        return technicalSpecification;
    }

    private TechnicalContract deriveTechnicalContract(Alert alert, Map<String, Object> technicalSpecification) {
        String source = firstNonBlank(stringValue(technicalSpecification.get("source")),
                containsValue(technicalSpecification.get("requiredSources"), "SERVICE_DATA") ? "SERVICE_DATA" : null);
        String interpreterType = firstNonBlank(stringValue(technicalSpecification.get("interpreterType")),
                alert.getSglInterpretertype() == null ? null : alert.getSglInterpretertype().getSglInterpretertype());
        String triggerType = firstNonBlank(stringValue(technicalSpecification.get("triggerType")),
                "EVENT_INTERPRETER".equals(interpreterType) ? "EVENT" : "SCHEDULED_INTERPRETER".equals(interpreterType) ? "SCHEDULE" : null);
        String inputModel = firstNonBlank(stringValue(technicalSpecification.get("inputModel")), alert.getDscInputmodel());
        String outputModel = firstNonBlank(stringValue(technicalSpecification.get("outputModel")), alert.getDscOutputmodel());
        String evaluationMode = stringValue(technicalSpecification.get("evaluationMode"));

        System.out.println("[IIA][AGENT_DEFINITION_CREATE][VALIDATION] source=" + source
                + " interpreterType=" + interpreterType
                + " triggerType=" + triggerType
                + " inputModel=" + inputModel
                + " outputModel=" + outputModel
                + " evaluationMode=" + evaluationMode);

        if (isBlank(source) || isBlank(interpreterType) || isBlank(triggerType)
                || isBlank(inputModel) || isBlank(outputModel) || isBlank(evaluationMode)) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.UNSUPPORTED_TECHNICAL_SPECIFICATION,
                    "The technicalSpecification does not contain the required runtime contract fields.");
        }
        return new TechnicalContract(source, interpreterType, triggerType, inputModel, outputModel, evaluationMode);
    }

    private void validateSupportedContract(TechnicalContract contract) {
        boolean supportedEvent = "SERVICE_DATA".equals(contract.source())
                && "EVENT_INTERPRETER".equals(contract.interpreterType())
                && "EVENT".equals(contract.triggerType())
                && "ServiceDataV2".equals(contract.inputModel())
                && "AgentOutput.CANDIDATE_SUGGESTION".equals(contract.outputModel())
                && "STATELESS_EVENT_MATCH".equals(contract.evaluationMode());
        boolean supportedScheduled = "SERVICE_DATA".equals(contract.source())
                && "SCHEDULED_INTERPRETER".equals(contract.interpreterType())
                && "SCHEDULE".equals(contract.triggerType())
                && "ServiceDataStopPointJourneysV2".equals(contract.inputModel())
                && "AgentOutput.CANDIDATE_SUGGESTION".equals(contract.outputModel())
                && "SCHEDULED_SNAPSHOT_MATCH".equals(contract.evaluationMode());
        if (!supportedEvent && !supportedScheduled) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.UNSUPPORTED_TECHNICAL_SPECIFICATION,
                    "The technicalSpecification is not supported by the Agent Definition MVP.");
        }
    }

    private void validateScheduledProfileLimits(TechnicalContract contract, Map<String, Object> technicalSpecification) {
        if (!contract.isScheduled()) {
            return;
        }
        Map<String, Object> schedule = mapValue(technicalSpecification.get("schedule"));
        Integer frequencySeconds = integerValue(schedule == null ? null : schedule.get("frequencySeconds"));
        if (frequencySeconds != null && frequencySeconds < 300) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.SCHEDULE_TOO_AGGRESSIVE,
                    "Scheduled Agent Definitions require schedule.frequencySeconds >= 300 in this MVP.");
        }
        Map<String, Object> serviceDataQuery = mapValue(technicalSpecification.get("serviceDataQuery"));
        Object stopPoints = serviceDataQuery == null ? null : serviceDataQuery.get("stopPoints");
        if (stopPoints instanceof List<?> list && list.size() > 50) {
            throw rejected(AgentDefinitionCreateRejectedException.Reason.TOO_MANY_STOP_POINTS,
                    "Scheduled Agent Definitions support at most 50 serviceDataQuery.stopPoints in this MVP.");
        }
    }

    private void validateActivationPolicy(AgentActivationPolicy policy) {
        if (policy.getType() == null) {
            throw invalid("activationPolicy.type", "The activation policy type is required.");
        }
        validateTimezone(policy.getTimezone());
        if (policy.getType() == AgentActivationPolicy.TypeEnum.CONTINUOUS) {
            if (policy.getValidFrom() == null || policy.getValidTo() == null || !policy.getValidTo().isAfter(policy.getValidFrom())) {
                throw invalid("activationPolicy", "CONTINUOUS activation policy requires validFrom, validTo and validTo > validFrom.");
            }
            return;
        }
        if (policy.getValidFromDate() == null || policy.getValidToDate() == null
                || policy.getValidToDate().isBefore(policy.getValidFromDate())) {
            throw invalid("activationPolicy", "DAILY_WINDOW activation policy requires validFromDate, validToDate and validToDate >= validFromDate.");
        }
        LocalTime start = parseTime(policy.getDailyStartTime(), "activationPolicy.dailyStartTime");
        LocalTime end = parseTime(policy.getDailyEndTime(), "activationPolicy.dailyEndTime");
        if (!end.isAfter(start)) {
            throw invalid("activationPolicy", "DAILY_WINDOW activation policy requires dailyEndTime > dailyStartTime.");
        }
        if (policy.getDaysOfWeek() != null) {
            for (DayOfWeek day : policy.getDaysOfWeek()) {
                if (day == null) {
                    throw invalid("activationPolicy.daysOfWeek", "daysOfWeek contains an invalid enum value.");
                }
            }
        }
    }

    private void applyActivationPolicy(AgentDefinition definition, AgentActivationPolicy policy) {
        definition.setSglActivationtype(agentDefinitionRepository.activationTypeReference(policy.getType().toString()));
        definition.setDscTimezone(policy.getTimezone());
        definition.setDtValidfrom(policy.getValidFrom());
        definition.setDtValidto(policy.getValidTo());
        definition.setDValidfromdate(policy.getValidFromDate());
        definition.setDValidtodate(policy.getValidToDate());
        definition.setTDailystarttime(policy.getDailyStartTime() == null ? null : parseTime(policy.getDailyStartTime(), "activationPolicy.dailyStartTime"));
        definition.setTDailyendtime(policy.getDailyEndTime() == null ? null : parseTime(policy.getDailyEndTime(), "activationPolicy.dailyEndTime"));
        definition.setJsnActivationpolicy(OBJECT_MAPPER.convertValue(policy, MAP_TYPE));
    }

    private Map<String, Object> blueprint(Alert alert, Map<String, Object> technicalSpecification, TechnicalContract contract) {
        Map<String, Object> preview = mapValue(alert.getJsnAgentblueprintpreview());
        if (preview != null && !preview.isEmpty()) {
            return preview;
        }
        Map<String, Object> blueprint = new LinkedHashMap<>();
        blueprint.put("schemaVersion", "iia.agent.blueprint/v1");
        blueprint.put("agentName", firstNonBlank(alert.getDscName(), "Agent for " + alert.getCodAlert()));
        blueprint.put("description", alert.getDscDescription());
        blueprint.put("triggerType", contract.triggerType());
        blueprint.put("requiredSources", List.of("SERVICE_DATA"));
        blueprint.put("requiredPermissions", List.of("SERVICE_DATA_READ"));
        blueprint.put("parameters", Map.of(
                "technicalSpecificationSource", "alert.jsn_technicalspecification",
                "technicalSpecification", technicalSpecification));
        return blueprint;
    }

    private Map<String, Object> runtimeContract(TechnicalContract contract, List<String> allowedToolNames, String networkPolicy) {
        List<Map<String, Object>> allowedTools = allowedToolNames.stream()
                .map(toolName -> Map.<String, Object>of("toolName", toolName, "operations", List.of(toolName)))
                .toList();
        Map<String, Object> runtimeContract = new LinkedHashMap<>();
        runtimeContract.put("runtimeExecutionModel", "STANDARD_DSL_EVALUATOR");
        runtimeContract.put("interpreterType", contract.interpreterType());
        runtimeContract.put("triggerType", contract.triggerType());
        runtimeContract.put("inputModel", contract.inputModel());
        runtimeContract.put("outputModel", contract.outputModel());
        runtimeContract.put("evaluationMode", contract.evaluationMode());
        runtimeContract.put("allowedTools", allowedTools);
        runtimeContract.put("networkPolicy", networkPolicy);
        runtimeContract.put("forbiddenCapabilities", List.of(
                "ARBITRARY_CODE_EXECUTION",
                "EXTERNAL_HTTP",
                "DB_QUERY",
                "FILESYSTEM",
                "SHELL"));
        runtimeContract.put("orchestratorCompatibility", Map.of(
                "minimumRuntimeVersion", "1.0.0",
                "runtimeClass", "STANDARD_DSL_RUNTIME"));
        return runtimeContract;
    }

    private List<Object> generationWarnings(Alert alert) {
        if (Boolean.TRUE.equals(alert.getFlgTechnicalspecificationedited())) {
            return new ArrayList<>(List.of("Technical specification was manually edited; Agent Definition was created from current technicalSpecification and existing blueprint may be derived."));
        }
        return new ArrayList<>();
    }

    private String complexity(Map<String, Object> blueprint, TechnicalContract contract) {
        String value = firstNonBlank(stringValue(blueprint.get("complexity")), stringValue(blueprint.get("agentComplexity")));
        if ("LOW".equals(value) || "MEDIUM".equals(value) || "HIGH".equals(value)) {
            return value;
        }
        return contract.isScheduled() ? "MEDIUM" : "LOW";
    }

    private List<String> daysOfWeek(AgentActivationPolicy policy) {
        if (policy.getType() != AgentActivationPolicy.TypeEnum.DAILY_WINDOW || policy.getDaysOfWeek() == null) {
            return List.of();
        }
        return policy.getDaysOfWeek().stream().map(DayOfWeek::toString).toList();
    }

    private Map<String, Object> mapValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            return OBJECT_MAPPER.convertValue(map, MAP_TYPE);
        }
        return null;
    }

    private boolean containsValue(Object value, String expected) {
        return value instanceof List<?> list && list.stream().map(String::valueOf).anyMatch(expected::equals);
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Integer integerValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private void validateCompilableStatus(String agentDefinitionId, String status) {
        if ("ACTIVE".equals(status)
                || "ARCHIVED".equals(status)
                || "SUPERSEDED".equals(status)
                || "DELETED".equals(status)) {
            throw rejectedCompilation(
                    AgentCompilationRejectedException.Reason.CONFLICT,
                    "Agent Definition " + agentDefinitionId + " cannot be compiled while status is " + status + ".");
        }
    }

    private String effectiveCompilationGenerationMode(AgentDefinition definition, AgentGenerationMode requestedMode) {
        AgentGenerationMode mode = requestedMode;
        if (mode == null && definition.getSglGenerationmode() != null && definition.getSglGenerationmode().getSglGenerationmode() != null) {
            try {
                mode = AgentGenerationMode.fromString(definition.getSglGenerationmode().getSglGenerationmode());
            } catch (IllegalArgumentException ex) {
                throw rejectedCompilation(
                        AgentCompilationRejectedException.Reason.UNPROCESSABLE,
                        "Unsupported generation mode " + definition.getSglGenerationmode().getSglGenerationmode() + ".");
            }
        }
        if (mode == null || mode == AgentGenerationMode.AUTO) {
            return AgentGenerationMode.DSL.toString();
        }
        if (mode == AgentGenerationMode.DSL) {
            return AgentGenerationMode.DSL.toString();
        }
        if (mode == AgentGenerationMode.JAVA_TEMPLATE) {
            throw rejectedCompilation(
                    AgentCompilationRejectedException.Reason.UNPROCESSABLE,
                    "Generation mode JAVA_TEMPLATE is not supported by the DSL compilation MVP.");
        }
        throw rejectedCompilation(
                AgentCompilationRejectedException.Reason.UNPROCESSABLE,
                "Unsupported generation mode " + mode + ".");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateTimezone(String timezone) {
        if (isBlank(timezone)) {
            throw invalid("activationPolicy.timezone", "A valid IANA timezone is required.");
        }
        try {
            ZoneId.of(timezone);
        } catch (ZoneRulesException ex) {
            throw invalid("activationPolicy.timezone", "A valid IANA timezone is required.");
        }
    }

    private LocalTime parseTime(String value, String source) {
        if (isBlank(value)) {
            throw invalid(source, "A local time is required.");
        }
        try {
            return LocalTime.parse(value.length() == 5 ? value + ":00" : value);
        } catch (RuntimeException ex) {
            throw invalid(source, "The local time must use HH:mm or HH:mm:ss format.");
        }
    }

    private AgentDefinitionInvalidRequestException invalid(String source, String message) {
        return new AgentDefinitionInvalidRequestException(source, message);
    }

    private AgentDefinitionCreateRejectedException rejected(AgentDefinitionCreateRejectedException.Reason reason, String message) {
        return new AgentDefinitionCreateRejectedException(reason, message);
    }

    private AgentCompilationRejectedException rejectedCompilation(AgentCompilationRejectedException.Reason reason, String message) {
        return new AgentCompilationRejectedException(reason, message);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String interpreterTypeFromInputModel(String inputModel) {
        return switch (inputModel == null ? "" : inputModel) {
            case "ServiceDataV2" -> "EVENT_INTERPRETER";
            case "ServiceDataStopPointJourneysV2" -> "SCHEDULED_INTERPRETER";
            default -> null;
        };
    }

    private String firstNonBlank(String first, String second) {
        return firstNonBlank(first, second, null);
    }

    private String firstNonBlank(String first, String second, String third) {
        if (!isBlank(first)) {
            return first.trim();
        }
        if (!isBlank(second)) {
            return second.trim();
        }
        return third;
    }

    private RuntimeShape runtimeShape(AgentDefinition definition) {
        Map<String, Object> runtimeContract = mapValue(definition.getJsnRuntimecontract());
        String interpreterType = firstNonBlank(
                stringValue(runtimeContract == null ? null : runtimeContract.get("interpreterType")),
                definition.getCodAlert() == null || definition.getCodAlert().getSglInterpretertype() == null
                        ? null
                        : definition.getCodAlert().getSglInterpretertype().getSglInterpretertype(),
                interpreterTypeFromInputModel(definition.getDscInputmodel()));
        String triggerType = firstNonBlank(
                stringValue(runtimeContract == null ? null : runtimeContract.get("triggerType")),
                "EVENT_INTERPRETER".equals(interpreterType)
                        ? "EVENT"
                        : "SCHEDULED_INTERPRETER".equals(interpreterType) ? "SCHEDULE" : null);
        return new RuntimeShape(interpreterType, triggerType);
    }

    private record RuntimeShape(String interpreterType, String triggerType) {
    }

    private record TechnicalContract(
            String source,
            String interpreterType,
            String triggerType,
            String inputModel,
            String outputModel,
            String evaluationMode) {
        boolean isScheduled() {
            return "SCHEDULED_INTERPRETER".equals(interpreterType);
        }
    }
}
