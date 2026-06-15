package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifact;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentArtifactType;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.entity.AgentDefinition;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgentArtifactMapper {

    public AgentArtifact toArtifact(AgentDefinition definition) {
        if (definition == null) {
            return null;
        }
        String artifactType = definition.getSglArtifacttype() == null ? null : definition.getSglArtifacttype().getSglArtifacttype();
        if (artifactType == null || "NONE".equals(artifactType)) {
            return null;
        }

        AgentArtifact artifact = new AgentArtifact()
                .artifactType(AgentArtifactType.fromString(artifactType))
                .artifactUri(definition.getDscArtifacturi())
                .artifactHash(definition.getDscArtifacthash())
                .runtimeImage(definition.getDscRuntimeimage())
                .sdkVersion(definition.getDscSdkversion())
                .implementationSummary(definition.getDscImplementationsummary());
        if (definition.getSglSignaturestatus() != null) {
            artifact.signatureStatus(AgentArtifact.SignatureStatusEnum.fromString(definition.getSglSignaturestatus().getSglSignaturestatus()));
        }
        return artifact;
    }
}
