package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

public class AgentDefinitionCreateRejectedException extends RuntimeException {

    public enum Reason {
        COMPILATION_NOT_IMPLEMENTED,
        ALERT_DELETED,
        ALERT_NOT_VERIFIED,
        ALERT_VERSION_MISMATCH,
        MISSING_TECHNICAL_SPECIFICATION,
        UNSUPPORTED_TECHNICAL_SPECIFICATION,
        PROFILE_DISABLED,
        UNSUPPORTED_GENERATION_MODE,
        SCHEDULE_TOO_AGGRESSIVE,
        TOO_MANY_STOP_POINTS
    }

    private final Reason reason;

    public AgentDefinitionCreateRejectedException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
