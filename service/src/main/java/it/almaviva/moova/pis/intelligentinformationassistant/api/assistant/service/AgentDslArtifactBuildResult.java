package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public record AgentDslArtifactBuildResult(
        boolean success,
        AgentDslArtifact artifact,
        String errorMessage) {

    public static AgentDslArtifactBuildResult success(AgentDslArtifact artifact) {
        return new AgentDslArtifactBuildResult(true, artifact, null);
    }

    public static AgentDslArtifactBuildResult failure(String errorMessage) {
        return new AgentDslArtifactBuildResult(false, null, errorMessage);
    }
}
