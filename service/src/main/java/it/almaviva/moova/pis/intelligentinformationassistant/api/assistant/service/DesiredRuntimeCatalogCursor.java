package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.time.OffsetDateTime;

public record DesiredRuntimeCatalogCursor(
        int version,
        String mode,
        long catalogUpperSequence,
        OffsetDateTime catalogAsOf,
        OffsetDateTime lastSourceUpdatedAt,
        String lastAgentDefinitionId,
        String filterFingerprint) {
}
