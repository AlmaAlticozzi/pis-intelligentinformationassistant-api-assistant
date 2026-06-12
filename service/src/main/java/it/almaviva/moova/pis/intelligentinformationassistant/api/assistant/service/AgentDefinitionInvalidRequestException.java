package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentDefinitionInvalidRequestException extends RuntimeException {

    private final String source;

    public AgentDefinitionInvalidRequestException(String source, String message) {
        super(message);
        this.source = source;
    }

    public String source() {
        return source;
    }
}
