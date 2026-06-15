package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatusResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentContinuousActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDailyWindowActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentHealthStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentQualityStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRunStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRunSummary;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimeContract;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.ToolReference;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AgentDefinitionMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Inject
    AgentProfileMapper agentProfileMapper;

    @Inject
    AgentArtifactMapper agentArtifactMapper;

    public AgentDefinitionDetail toDto(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity,
            String interpreterType,
            String triggerType) {
        if (entity == null) {
            return null;
        }

        return new AgentDefinitionDetail()
                .id(entity.getCodAgentdefinition())
                .name(entity.getDscName())
                .description(entity.getDscDescription())
                .status(entity.getSglStatus() == null ? null : AgentDefinitionStatus.fromString(entity.getSglStatus().getSglStatus()))
                .alert(new AlertReference()
                        .id(entity.getCodAlert().getCodAlert())
                        .name(entity.getCodAlert().getDscName()))
                .alertVersion(entity.getNumAlertversion())
                .profile(agentProfileMapper.toDto(entity.getCodAgentprofile()))
                .generationMode(entity.getSglGenerationmode() == null ? null : AgentGenerationMode.fromString(entity.getSglGenerationmode().getSglGenerationmode()))
                .activationPolicy(convert(entity.getJsnActivationpolicy(), AgentActivationPolicy.class))
                .blueprint(convert(entity.getJsnBlueprint(), AgentBlueprint.class))
                .artifact(artifactMapper().toArtifact(entity))
                .compilation(toCompilation(entity))
                .runtimeContract(toRuntimeContract(entity, interpreterType, triggerType))
                .latestRun(toLatestRun(entity))
                .createdBy(entity.getCodCreatedby())
                .createdAt(entity.getDtCreatedat())
                .updatedAt(entity.getDtUpdatedat())
                .interpreterType(interpreterType == null ? null : AlertInterpreterType.fromString(interpreterType))
                .triggerType(triggerType)
                .inputModel(entity.getDscInputmodel())
                .outputModel(entity.getDscOutputmodel());
    }

    public AgentDefinitionSummary toSummary(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity,
            String interpreterType,
            String triggerType) {
        if (entity == null) {
            return null;
        }

        return new AgentDefinitionSummary()
                .id(entity.getCodAgentdefinition())
                .name(entity.getDscName())
                .description(entity.getDscDescription())
                .status(entity.getSglStatus() == null ? null : AgentDefinitionStatus.fromString(entity.getSglStatus().getSglStatus()))
                .alert(new AlertReference()
                        .id(entity.getCodAlert().getCodAlert())
                        .name(entity.getCodAlert().getDscName()))
                .alertVersion(entity.getNumAlertversion())
                .profile(agentProfileMapper.toDto(entity.getCodAgentprofile()))
                .generationMode(entity.getSglGenerationmode() == null ? null : AgentGenerationMode.fromString(entity.getSglGenerationmode().getSglGenerationmode()))
                .activationPolicy(toSummaryActivationPolicy(entity))
                .compilation(toCompilationSummary(entity))
                .latestRun(toLatestRun(entity))
                .createdAt(entity.getDtCreatedat())
                .updatedAt(entity.getDtUpdatedat())
                .interpreterType(interpreterType == null ? null : AlertInterpreterType.fromString(interpreterType))
                .triggerType(triggerType)
                .inputModel(entity.getDscInputmodel())
                .outputModel(entity.getDscOutputmodel());
    }

    public Map<String, Object> toMap(Object value) {
        return OBJECT_MAPPER.convertValue(value, MAP_TYPE);
    }

    private AgentCompilationStatusResponse toCompilation(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity) {
        var compilation = entity.getCodLatestcompilation();
        var statusRef = compilation == null ? entity.getSglLatestcompilationstatus() : compilation.getSglStatus();
        if (compilation == null && statusRef == null) {
            return null;
        }
        AgentCompilationStatusResponse response = new AgentCompilationStatusResponse()
                .agentDefinitionId(entity.getCodAgentdefinition())
                .status(statusRef == null ? null : AgentCompilationStatus.fromString(statusRef.getSglStatus()))
                .currentStep(compilation == null ? entity.getDscLatestcompilationstep() : compilation.getDscCurrentstep())
                .completedAt(compilation == null ? entity.getDtLatestcompilationcompletedat() : compilation.getDtCompletedat())
                .artifact(artifactMapper().toArtifact(entity));
        if (compilation != null) {
            response.startedAt(compilation.getDtStartedat());
            if (compilation.getDscErrormessage() != null && !compilation.getDscErrormessage().isBlank()) {
                response.errors(List.of(compilation.getDscErrormessage()));
            }
        }
        return response;
    }

    private AgentActivationPolicy toSummaryActivationPolicy(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity) {
        if (entity.getJsnActivationpolicy() != null && !entity.getJsnActivationpolicy().isEmpty()) {
            return convert(entity.getJsnActivationpolicy(), AgentActivationPolicy.class);
        }
        String activationType = entity.getSglActivationtype() == null ? null : entity.getSglActivationtype().getSglActivationtype();
        if ("CONTINUOUS".equals(activationType)) {
            return new AgentContinuousActivationPolicy()
                    .type(AgentActivationPolicy.TypeEnum.CONTINUOUS)
                    .timezone(entity.getDscTimezone())
                    .validFrom(entity.getDtValidfrom())
                    .validTo(entity.getDtValidto());
        }
        if ("DAILY_WINDOW".equals(activationType)) {
            return new AgentDailyWindowActivationPolicy()
                    .type(AgentActivationPolicy.TypeEnum.DAILY_WINDOW)
                    .timezone(entity.getDscTimezone())
                    .validFromDate(entity.getDValidfromdate())
                    .validToDate(entity.getDValidtodate())
                    .dailyStartTime(entity.getTDailystarttime() == null ? null : entity.getTDailystarttime().toString())
                    .dailyEndTime(entity.getTDailyendtime() == null ? null : entity.getTDailyendtime().toString());
        }
        return null;
    }

    private AgentCompilationSummary toCompilationSummary(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity) {
        var compilation = entity.getCodLatestcompilation();
        var statusRef = compilation == null ? entity.getSglLatestcompilationstatus() : compilation.getSglStatus();
        if (compilation == null && statusRef == null) {
            return null;
        }
        return new AgentCompilationSummary()
                .status(statusRef == null ? null : AgentCompilationStatus.fromString(statusRef.getSglStatus()))
                .currentStep(compilation == null ? entity.getDscLatestcompilationstep() : compilation.getDscCurrentstep())
                .completedAt(compilation == null ? entity.getDtLatestcompilationcompletedat() : compilation.getDtCompletedat());
    }

    private AgentRuntimeContract toRuntimeContract(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity,
            String interpreterType,
            String triggerType) {
        AgentRuntimeContract contract = convert(runtimeContractForDto(entity.getJsnRuntimecontract()), AgentRuntimeContract.class);
        if (contract == null) {
            contract = new AgentRuntimeContract();
        }
        if (contract.getInputModel() == null || contract.getInputModel().isBlank()) {
            contract.inputModel(entity.getDscInputmodel());
        }
        if (contract.getOutputModel() == null || contract.getOutputModel().isBlank()) {
            contract.outputModel(entity.getDscOutputmodel());
        }
        if (contract.getNetworkPolicy() == null || contract.getNetworkPolicy().isBlank()) {
            contract.networkPolicy(entity.getDscNetworkpolicy());
        }
        if (contract.getInterpreterType() == null && interpreterType != null) {
            contract.interpreterType(AlertInterpreterType.fromString(interpreterType));
        }
        if ((contract.getTriggerType() == null || contract.getTriggerType().isBlank()) && triggerType != null) {
            contract.triggerType(triggerType);
        }
        if (contract.getRuntimeImage() == null || contract.getRuntimeImage().isBlank()) {
            contract.runtimeImage(runtimeImageFallback(entity));
        }
        if (contract.getSdkVersion() == null || contract.getSdkVersion().isBlank()) {
            contract.sdkVersion(sdkVersionFallback(entity));
        }
        if ((contract.getAllowedTools() == null || contract.getAllowedTools().isEmpty())
                && entity.getJsnAllowedtools() != null
                && !entity.getJsnAllowedtools().isEmpty()) {
            contract.allowedTools(entity.getJsnAllowedtools().stream()
                    .map(String::valueOf)
                    .map(tool -> new ToolReference().toolName(tool).operations(List.of(tool)))
                    .toList());
        }
        return contract;
    }

    private Object runtimeContractForDto(Map<String, Object> runtimeContract) {
        if (runtimeContract == null) {
            return null;
        }
        Map<String, Object> normalized = new LinkedHashMap<>(runtimeContract);
        Object allowedTools = normalized.get("allowedTools");
        if (allowedTools instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
            normalized.put("allowedTools", list.stream()
                    .map(String::valueOf)
                    .map(tool -> Map.of("toolName", tool, "operations", List.of(tool)))
                    .toList());
        }
        return normalized;
    }

    private String runtimeImageFallback(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity) {
        if (entity.getDscRuntimeimage() != null && !entity.getDscRuntimeimage().isBlank()) {
            return entity.getDscRuntimeimage();
        }
        if (isDslArtifact(entity)) {
            return "STANDARD_AGENT_DSL_EVALUATOR";
        }
        return null;
    }

    private String sdkVersionFallback(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity) {
        if (entity.getDscSdkversion() != null && !entity.getDscSdkversion().isBlank()) {
            return entity.getDscSdkversion();
        }
        if (isDslArtifact(entity)) {
            return "iia.agent.dsl/v1";
        }
        return null;
    }

    private boolean isDslArtifact(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity) {
        return entity.getSglArtifacttype() != null
                && "DSL".equals(entity.getSglArtifacttype().getSglArtifacttype());
    }

    private AgentArtifactMapper artifactMapper() {
        return agentArtifactMapper == null ? new AgentArtifactMapper() : agentArtifactMapper;
    }

    private AgentRunSummary toLatestRun(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity) {
        var run = entity.getCodLatestrun();
        if (run == null) {
            return null;
        }
        return new AgentRunSummary()
                .id(run.getCodAgentrun())
                .agentDefinitionId(entity.getCodAgentdefinition())
                .agentName(entity.getDscName())
                .alert(new AlertReference()
                        .id(entity.getCodAlert().getCodAlert())
                        .name(entity.getCodAlert().getDscName()))
                .status(run.getSglStatus() == null ? null : AgentRunStatus.fromString(run.getSglStatus().getSglStatus()))
                .healthStatus(run.getSglHealthstatus() == null ? null : AgentHealthStatus.fromString(run.getSglHealthstatus().getSglHealthstatus()))
                .healthScore(run.getNumHealthscore())
                .qualityStatus(run.getSglQualitystatus() == null ? null : AgentQualityStatus.fromString(run.getSglQualitystatus().getSglQualitystatus()))
                .qualityScore(run.getNumQualityscore())
                .profileId(run.getCodProfile())
                .cpuUsagePercent(run.getNumCpuusagepercent() == null ? null : run.getNumCpuusagepercent().doubleValue())
                .memoryUsagePercent(run.getNumMemoryusagepercent() == null ? null : run.getNumMemoryusagepercent().doubleValue())
                .generatedOutputs(run.getNumCandidateoutputs())
                .createdSuggestions(run.getNumCreatedsuggestions())
                .lastHeartbeatAt(run.getDtLastheartbeatat())
                .startedAt(run.getDtStartedat() == null ? run.getDtCreatedat() : run.getDtStartedat())
                .stoppedAt(run.getDtStoppedat());
    }

    private <T> T convert(Object value, Class<T> type) {
        if (value == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(value, type);
    }
}
