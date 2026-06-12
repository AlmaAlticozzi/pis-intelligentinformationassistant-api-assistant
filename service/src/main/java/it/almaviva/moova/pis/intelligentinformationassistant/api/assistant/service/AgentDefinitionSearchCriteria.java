package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentDefinitionStatus;
import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.AgentGenerationMode;

public record AgentDefinitionSearchCriteria(
        AgentDefinitionStatus status,
        String alertId,
        AgentGenerationMode generationMode,
        String profileId,
        String text) {
}
