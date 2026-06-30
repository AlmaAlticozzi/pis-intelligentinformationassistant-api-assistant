package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Base64;

@ApplicationScoped
public class DesiredRuntimeCatalogCheckpointCodec {
    @Inject ObjectMapper objectMapper;

    public DesiredRuntimeCatalogCheckpointCodec() { }
    DesiredRuntimeCatalogCheckpointCodec(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    public String encode(DesiredRuntimeCatalogCheckpoint checkpoint) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(checkpoint));
        } catch (Exception ex) {
            throw new IllegalStateException("Desired Runtime Catalog checkpoint encoding failed.", ex);
        }
    }

    DesiredRuntimeCatalogCheckpoint decode(String encoded) {
        try {
            if (encoded == null || encoded.isBlank() || encoded.length() > 2000) throw new IllegalArgumentException();
            DesiredRuntimeCatalogCheckpoint value = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(encoded), DesiredRuntimeCatalogCheckpoint.class);
            if (value == null || value.changeSequence() < 0 || value.catalogAsOf() == null) {
                throw new IllegalArgumentException();
            }
            return value;
        } catch (Exception ex) {
            throw new DesiredRuntimeCatalogInvalidRequestException("checkpoint", "The checkpoint is invalid.");
        }
    }

    public DesiredRuntimeCatalogCheckpoint decodeForIncremental(String encoded, long currentUpperSequence) {
        DesiredRuntimeCatalogCheckpoint value = decode(encoded);
        if (value.version() != 1) {
            throw new DesiredRuntimeCatalogCheckpointIncompatibleException("The checkpoint version is unsupported.");
        }
        if (value.changeSequence() > currentUpperSequence) {
            throw new DesiredRuntimeCatalogCheckpointIncompatibleException("The checkpoint is ahead of the catalog.");
        }
        return value;
    }
}
