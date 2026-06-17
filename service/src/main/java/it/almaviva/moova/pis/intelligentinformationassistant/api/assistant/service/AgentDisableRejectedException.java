package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentDisableRejectedException extends RuntimeException {

    private final String currentStatus;

    public AgentDisableRejectedException(String currentStatus, String message) {
        super(message);
        this.currentStatus = currentStatus;
    }

    public String currentStatus() {
        return currentStatus;
    }
}
