package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public record ScheduledSnapshotEvaluation(
        ScheduledSnapshotEvaluationMode mode,
        String journeyPath,
        ScheduledConditionNode condition,
        ScheduledThreshold threshold) {
}
