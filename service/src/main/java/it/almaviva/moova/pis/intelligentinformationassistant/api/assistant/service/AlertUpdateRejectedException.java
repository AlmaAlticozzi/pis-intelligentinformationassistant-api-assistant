package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AlertUpdateRejectedException extends RuntimeException {

    private final Reason reason;

    public AlertUpdateRejectedException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        DELETED,
        VERIFYING,
        DUPLICATE_NAME
    }
}
