package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public record AgentArtifactHashResult(
        String canonicalJson,
        String artifactHash,
        String hashAlgorithm,
        Integer artifactSizeBytes) {
}
