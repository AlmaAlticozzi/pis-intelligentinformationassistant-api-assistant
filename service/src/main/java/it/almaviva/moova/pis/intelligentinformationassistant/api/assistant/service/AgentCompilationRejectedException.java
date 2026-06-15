package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentCompilationRejectedException extends RuntimeException {

    public enum Reason {
        CONFLICT,
        UNPROCESSABLE
    }

    private final Reason reason;

    public AgentCompilationRejectedException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
