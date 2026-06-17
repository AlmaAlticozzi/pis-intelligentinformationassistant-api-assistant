package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service;

import java.time.Instant;

public record AgentRuntimePackageBuildContext(
        long packageVersion,
        Instant submittedAt,
        String submittedBy) {

    public AgentRuntimePackageBuildContext {
        if (packageVersion < 1) {
            throw new IllegalArgumentException("packageVersion must be greater than or equal to 1");
        }
        if (submittedAt == null) {
            throw new IllegalArgumentException("submittedAt is required");
        }
        if (submittedBy == null || submittedBy.isBlank()) {
            throw new IllegalArgumentException("submittedBy is required");
        }
        submittedBy = submittedBy.trim();
    }
}
