package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.repository.verification;

public record ScheduledServiceDataQueryTimeWindow(
        ScheduledServiceDataQueryWindowStartMode startMode,
        ScheduledServiceDataQueryWindowEndMode endMode,
        Integer lookaheadMinutes,
        boolean defaulted,
        String rawText) {
}
