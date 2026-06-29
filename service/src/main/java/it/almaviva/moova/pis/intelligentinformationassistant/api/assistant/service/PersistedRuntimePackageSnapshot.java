package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Map;

public record PersistedRuntimePackageSnapshot(
        String runtimePackageId,
        String agentDefinitionId,
        String submissionId,
        long packageVersion,
        String packageFingerprint,
        Map<String, Object> payload) {
}
