package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ApplicationScoped
public class DesiredRuntimeCatalogCursorCodec {
    @Inject
    ObjectMapper objectMapper;

    public DesiredRuntimeCatalogCursorCodec() { }
    DesiredRuntimeCatalogCursorCodec(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public String encode(DesiredRuntimeCatalogCursor cursor) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(cursor));
        } catch (Exception ex) {
            throw new IllegalStateException("Desired Runtime Catalog cursor encoding failed.", ex);
        }
    }

    public DesiredRuntimeCatalogCursor decode(String encoded) {
        try {
            if (encoded == null || encoded.isBlank() || encoded.length() > 1000) throw new IllegalArgumentException();
            byte[] decoded = Base64.getUrlDecoder().decode(encoded.getBytes(StandardCharsets.US_ASCII));
            DesiredRuntimeCatalogCursor cursor = objectMapper.readValue(decoded, DesiredRuntimeCatalogCursor.class);
            validate(cursor);
            return cursor;
        } catch (DesiredRuntimeCatalogInvalidRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DesiredRuntimeCatalogInvalidRequestException("cursor", "The FULL catalog cursor is invalid.");
        }
    }

    private void validate(DesiredRuntimeCatalogCursor cursor) {
        boolean invalid = cursor == null || cursor.version() != 1 || !"FULL".equals(cursor.mode())
                || cursor.catalogUpperSequence() < 0 || cursor.catalogAsOf() == null
                || cursor.lastSourceUpdatedAt() == null || cursor.lastAgentDefinitionId() == null
                || cursor.lastAgentDefinitionId().isBlank() || cursor.lastAgentDefinitionId().length() > 50;
        if (invalid) throw new DesiredRuntimeCatalogInvalidRequestException("cursor", "The FULL catalog cursor is invalid.");
    }
}
