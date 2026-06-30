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
            return objectMapper.readValue(Base64.getUrlDecoder().decode(encoded), DesiredRuntimeCatalogCheckpoint.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid checkpoint.", ex);
        }
    }
}
