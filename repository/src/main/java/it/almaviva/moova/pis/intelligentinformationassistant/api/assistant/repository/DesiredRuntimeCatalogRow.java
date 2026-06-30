package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import java.time.OffsetDateTime;
import java.util.Map;

public record DesiredRuntimeCatalogRow(
        long catalogChangeSequence,
        String catalogChangeId,
        String agentDefinitionId,
        String action,
        String sourceAgentStatus,
        OffsetDateTime sourceUpdatedAt,
        String runtimePackageId,
        long catalogPackageVersion,
        String catalogPackageFingerprint,
        String packageAgentDefinitionId,
        long persistedPackageVersion,
        String persistedSubmissionId,
        String persistedPackageFingerprint,
        Map<String, Object> persistedRuntimePackage) {
}
