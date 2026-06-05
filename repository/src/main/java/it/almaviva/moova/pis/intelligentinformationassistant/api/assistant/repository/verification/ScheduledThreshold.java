package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public record ScheduledThreshold(
        ScheduledThresholdOperator operator,
        Number value) {
}
