package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository;

import java.time.OffsetDateTime;

public record DesiredRuntimeCatalogAgentState(String agentDefinitionId, String status, OffsetDateTime updatedAt) { }
