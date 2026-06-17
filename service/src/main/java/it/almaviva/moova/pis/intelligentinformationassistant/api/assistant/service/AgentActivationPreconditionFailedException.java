package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.util.List;

public class AgentActivationPreconditionFailedException extends RuntimeException {

    private final List<AgentActivationPreconditionViolation> violations;

    public AgentActivationPreconditionFailedException(List<AgentActivationPreconditionViolation> violations) {
        super(toMessage(violations));
        this.violations = violations == null ? List.of() : List.copyOf(violations);
    }

    public List<AgentActivationPreconditionViolation> violations() {
        return violations;
    }

    private static String toMessage(List<AgentActivationPreconditionViolation> violations) {
        List<AgentActivationPreconditionViolation> safeViolations = violations == null ? List.of() : violations;
        if (safeViolations.isEmpty()) {
            return "Agent Definition activation preconditions failed.";
        }
        return "Agent Definition activation preconditions failed: "
                + safeViolations.stream()
                .map(violation -> violation.code() + fieldSuffix(violation) + " - " + violation.message())
                .limit(10)
                .toList();
    }

    private static String fieldSuffix(AgentActivationPreconditionViolation violation) {
        return violation.field() == null || violation.field().isBlank() ? "" : "[" + violation.field() + "]";
    }
}
