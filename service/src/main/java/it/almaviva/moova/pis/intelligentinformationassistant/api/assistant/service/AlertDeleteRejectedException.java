package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AlertDeleteRejectedException extends RuntimeException {

    private final Reason reason;

    public AlertDeleteRejectedException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        DELETED,
        VERIFYING,
        DEPLOYING
    }
}
