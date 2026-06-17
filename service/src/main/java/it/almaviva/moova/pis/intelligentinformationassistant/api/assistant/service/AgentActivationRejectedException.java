package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentActivationRejectedException extends RuntimeException {

    private final String currentStatus;

    public AgentActivationRejectedException(String currentStatus, String message) {
        super(message);
        this.currentStatus = currentStatus;
    }

    public String currentStatus() {
        return currentStatus;
    }
}
