package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AlertRuntimeStateChangeRejectedException extends RuntimeException {

    private final Reason reason;

    public AlertRuntimeStateChangeRejectedException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        DELETED,
        STATUS_NOT_VERIFIED,
        VERIFICATION_NOT_VERIFIED,
        ALREADY_ENABLED,
        MISSING_OPERATIONAL_METADATA,
        VERIFYING,
        ALREADY_DISABLED
    }
}
