package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public record ScheduledOutputPolicy(
        ScheduledOutputEmitMode emit,
        boolean includeCount,
        boolean includeMatchingJourneys) {
}
