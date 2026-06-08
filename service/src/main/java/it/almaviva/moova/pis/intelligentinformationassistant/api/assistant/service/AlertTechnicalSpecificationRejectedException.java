package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AlertTechnicalSpecificationRejectedException extends RuntimeException {

    private final Reason reason;

    public AlertTechnicalSpecificationRejectedException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        DELETED,
        NOT_VERIFIED,
        CONCURRENT_UPDATE,
        INVALID_TECHNICAL_SPECIFICATION,
        UNSUPPORTED_TECHNICAL_SPECIFICATION
    }
}
