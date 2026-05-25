package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AlertAgentGenerationPreviewRejectedException extends RuntimeException {

    private final Reason reason;

    public AlertAgentGenerationPreviewRejectedException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        NOT_VERIFIED,
        MISSING_TECHNICAL_ARTIFACTS,
        INVALID_BLUEPRINT
    }
}
