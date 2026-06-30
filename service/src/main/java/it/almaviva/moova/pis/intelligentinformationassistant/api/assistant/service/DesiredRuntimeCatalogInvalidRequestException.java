package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class DesiredRuntimeCatalogInvalidRequestException extends RuntimeException {
    private final String source;

    public DesiredRuntimeCatalogInvalidRequestException(String source, String message) {
        super(message);
        this.source = source;
    }

    public String source() {
        return source;
    }
}
