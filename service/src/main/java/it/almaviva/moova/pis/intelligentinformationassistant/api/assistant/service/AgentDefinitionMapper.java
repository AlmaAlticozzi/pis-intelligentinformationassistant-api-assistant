package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifact;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationStatusResponse;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentCompilationSummary;
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

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AgentDefinitionMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Inject
    AgentProfileMapper agentProfileMapper;

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
                .artifact(toArtifact(entity))
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
                .activationPolicy(convert(entity.getJsnActivationpolicy(), AgentActivationPolicy.class))
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

    private AgentArtifact toArtifact(
            it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition entity) {
        AgentArtifact artifact = new AgentArtifact()
                .artifactType(entity.getSglArtifacttype() == null ? null : AgentArtifactType.fromString(entity.getSglArtifacttype().getSglArtifacttype()))
                .artifactUri(entity.getDscArtifacturi())
                .artifactHash(entity.getDscArtifacthash())
                .runtimeImage(entity.getDscRuntimeimage())
                .sdkVersion(entity.getDscSdkversion())
                .implementationSummary(entity.getDscImplementationsummary());
        if (entity.getSglSignaturestatus() != null) {
            artifact.signatureStatus(AgentArtifact.SignatureStatusEnum.fromString(entity.getSglSignaturestatus().getSglSignaturestatus()));
        }
        return artifact;
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
                .artifact(toArtifact(entity));
        if (compilation != null) {
            response.startedAt(compilation.getDtStartedat());
            if (compilation.getDscErrormessage() != null && !compilation.getDscErrormessage().isBlank()) {
                response.errors(List.of(compilation.getDscErrormessage()));
            }
        }
        return response;
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
        AgentRuntimeContract contract = convert(entity.getJsnRuntimecontract(), AgentRuntimeContract.class);
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
