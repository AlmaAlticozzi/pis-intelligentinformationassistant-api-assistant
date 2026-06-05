package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public record ScheduledAlertTechnicalSpecification(
        String schemaVersion,
        String source,
        String interpreterType,
        String accessMode,
        String inputModel,
        String outputModel,
        String triggerType,
        String evaluationMode,
        ScheduledSchedulePolicy schedule,
        ScheduledServiceDataQuery serviceDataQuery,
        ScheduledSnapshotEvaluation snapshotEvaluation,
        ScheduledOutputPolicy outputPolicy,
        String deduplicationKeyTemplate) {
}
