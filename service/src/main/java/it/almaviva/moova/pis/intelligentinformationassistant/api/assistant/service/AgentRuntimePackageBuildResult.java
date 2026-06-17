package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public record AgentRuntimePackageBuildResult(
        AgentRuntimeSubmission submission,
        String canonicalPackageJson,
        String canonicalPackageHash,
        Integer canonicalPackageSizeBytes) {
}
