package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public record ScheduledSchedulePolicy(
        Integer frequencySeconds,
        boolean defaulted,
        String rawText) {
}
