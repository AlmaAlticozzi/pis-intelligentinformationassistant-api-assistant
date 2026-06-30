package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.model.assistant.DesiredRuntimeCatalogMode;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DesiredRuntimeCatalogRequestValidator {
    public static final int DEFAULT_LIMIT = 100;

    public int validate(DesiredRuntimeCatalogRequest request) {
        if (request.mode() == null) throw invalid("mode", "Desired Runtime Catalog mode is required.");
        if (request.mode() != DesiredRuntimeCatalogMode.FULL) {
            throw invalid("mode", "Desired Runtime Catalog mode " + request.mode()
                    + " is not implemented in phase 4.11.13-C1.");
        }
        if (request.changedAfter() != null) throw invalid("changedAfter", "changedAfter is not allowed for FULL mode.");
        if (request.checkpoint() != null) throw invalid("checkpoint", "checkpoint is not allowed for FULL mode.");
        if (request.agentDefinitionIds() != null && !request.agentDefinitionIds().isEmpty()) {
            throw invalid("agentDefinitionId", "agentDefinitionId is not allowed for FULL mode.");
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
