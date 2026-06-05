package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertMonitoringScope;

import java.util.List;

public record ScheduledServiceDataApiQueryContext(
        ScheduledAlertMonitoringScope monitoringScope,
        List<String> stopPoints,
        boolean requiresAllKnownStopPoints) {

    public ScheduledServiceDataApiQueryContext {
        monitoringScope = monitoringScope == null ? ScheduledAlertMonitoringScope.UNSPECIFIED : monitoringScope;
        stopPoints = stopPoints == null ? List.of() : List.copyOf(stopPoints);
    }
}
