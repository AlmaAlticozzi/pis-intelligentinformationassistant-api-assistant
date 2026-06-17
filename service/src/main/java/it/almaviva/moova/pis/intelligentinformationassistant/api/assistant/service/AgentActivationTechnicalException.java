package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentActivationTechnicalException extends RuntimeException {

    public AgentActivationTechnicalException(String message) {
        super(message);
    }

    public AgentActivationTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
