package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Map;

public record RuntimeAgentPackageBuild(
        AgentRuntimeSubmission submission,
        Map<String, Object> runtimePackageJson,
        String packageFingerprint,
        String canonicalPackageHash,
        int canonicalPackageSizeBytes) {
}
