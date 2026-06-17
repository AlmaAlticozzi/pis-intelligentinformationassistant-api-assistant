package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentDisableTechnicalException extends RuntimeException {

    public AgentDisableTechnicalException(String message) {
        super(message);
    }

    public AgentDisableTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
