package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentDefinitionNotFoundException extends RuntimeException {

    private final String source;

    public AgentDefinitionNotFoundException(String source, String message) {
        super(message);
        this.source = source;
    }

    public String source() {
        return source;
    }
}
