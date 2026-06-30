package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DesiredRuntimeCatalogRequestValidator {
    public static final int DEFAULT_LIMIT = 100;

    public int validate(DesiredRuntimeCatalogRequest request) {
        if (request.mode() == null) throw invalid("mode", "Desired Runtime Catalog mode is required.");
        if (request.mode() == DesiredRuntimeCatalogMode.FULL) {
            if (request.changedAfter() != null) throw invalid("changedAfter", "changedAfter is not allowed for FULL mode.");
            if (request.checkpoint() != null) throw invalid("checkpoint", "checkpoint is not allowed for FULL mode.");
            if (request.agentDefinitionIds() != null && !request.agentDefinitionIds().isEmpty()) {
                throw invalid("agentDefinitionId", "agentDefinitionId is not allowed for FULL mode.");
            }
        } else if (request.mode() == DesiredRuntimeCatalogMode.TARGETED) {
            if (request.changedAfter() != null) throw invalid("changedAfter", "changedAfter is not allowed for TARGETED mode.");
            if (request.checkpoint() != null) throw invalid("checkpoint", "checkpoint is not allowed for TARGETED mode.");
            var ids = request.agentDefinitionIds();
            if (ids == null || ids.isEmpty() || ids.size() > 500) {
                throw invalid("agentDefinitionId", "TARGETED requires between 1 and 500 Agent Definition IDs.");
            }
            if (ids.stream().anyMatch(id -> id == null || id.isBlank() || id.length() > 50)) {
                throw invalid("agentDefinitionId", "Each Agent Definition ID must be nonblank and at most 50 characters.");
            }
        } else {
            boolean checkpointPresent = request.checkpoint() != null;
            boolean changedAfterPresent = request.changedAfter() != null;
            if (checkpointPresent == changedAfterPresent) {
                throw invalid("checkpoint", "INCREMENTAL requires exactly one of checkpoint or changedAfter.");
            }
            if (checkpointPresent && (request.checkpoint().isBlank() || request.checkpoint().length() > 2000)) {
                throw invalid("checkpoint", "checkpoint must be nonblank and at most 2000 characters.");
            }
            if (request.agentDefinitionIds() != null && !request.agentDefinitionIds().isEmpty()) {
                throw invalid("agentDefinitionId", "agentDefinitionId is not allowed for INCREMENTAL mode.");
            }
        }
        if (request.cursor() != null && request.cursor().isBlank()) throw invalid("cursor", "cursor must not be blank.");
        if (request.cursor() != null && request.cursor().length() > 1000) throw invalid("cursor", "cursor is too long.");
        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        if (limit < 1 || limit > 500) throw invalid("limit", "limit must be between 1 and 500.");
        return limit;
    }

    private DesiredRuntimeCatalogInvalidRequestException invalid(String source, String detail) {
        return new DesiredRuntimeCatalogInvalidRequestException(source, detail);
    }
}
