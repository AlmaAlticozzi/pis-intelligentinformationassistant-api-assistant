package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.Objects;

public record AgentActivationPreconditionViolation(
        AgentActivationPreconditionCode code,
        String field,
        String message) {

    public AgentActivationPreconditionViolation {
        Objects.requireNonNull(code, "code is required");
        Objects.requireNonNull(message, "message is required");
    }
}
