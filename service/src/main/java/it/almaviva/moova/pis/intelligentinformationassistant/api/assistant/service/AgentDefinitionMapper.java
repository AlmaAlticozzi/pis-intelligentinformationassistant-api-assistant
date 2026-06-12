package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentActivationPolicy;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifact;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentBlueprint;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionDetail;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentRuntimeContract;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertInterpreterType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AlertReference;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
                .runtimeContract(convert(entity.getJsnRuntimecontract(), AgentRuntimeContract.class))
                .createdBy(entity.getCodCreatedby())
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

    private <T> T convert(Object value, Class<T> type) {
        if (value == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(value, type);
    }
}
