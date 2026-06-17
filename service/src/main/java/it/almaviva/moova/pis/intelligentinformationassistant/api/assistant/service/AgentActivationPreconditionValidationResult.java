package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.List;

public record AgentActivationPreconditionValidationResult(
        boolean valid,
        List<AgentActivationPreconditionViolation> errors,
        List<AgentActivationPreconditionViolation> warnings) {

    public AgentActivationPreconditionValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        valid = errors.isEmpty();
    }
}
