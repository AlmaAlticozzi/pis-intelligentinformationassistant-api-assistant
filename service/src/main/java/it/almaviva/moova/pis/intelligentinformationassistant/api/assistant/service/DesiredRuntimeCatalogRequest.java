package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;

import java.time.OffsetDateTime;
import java.util.Set;

public record DesiredRuntimeCatalogRequest(
        DesiredRuntimeCatalogMode mode,
        OffsetDateTime changedAfter,
        String checkpoint,
        Set<String> agentDefinitionIds,
        String cursor,
        Integer limit) {
}
