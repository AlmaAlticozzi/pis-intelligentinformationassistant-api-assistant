package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

import it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai.ScheduledAlertMonitoringScope;

import java.util.List;

public record ScheduledServiceDataLocationContext(
        ScheduledAlertMonitoringScope monitoringScope,
        List<ScheduledServiceDataResolvedLocation> monitoredLocations,
        List<ScheduledServiceDataResolvedLocation> filterLocations,
        List<ScheduledServiceDataResolvedLocation> excludedLocations,
        List<String> serviceDataApiStopPoints,
        boolean requiresAllKnownStopPoints,
        boolean hasUnresolvedRequiredMonitoredLocations,
        List<String> unresolvedRequiredMonitoredLocationTexts,
        List<String> warnings,
        ScheduledServiceDataApiQueryContext apiQueryContext) {

    public ScheduledServiceDataLocationContext {
        monitoringScope = monitoringScope == null ? ScheduledAlertMonitoringScope.UNSPECIFIED : monitoringScope;
        monitoredLocations = monitoredLocations == null ? List.of() : List.copyOf(monitoredLocations);
        filterLocations = filterLocations == null ? List.of() : List.copyOf(filterLocations);
        excludedLocations = excludedLocations == null ? List.of() : List.copyOf(excludedLocations);
        serviceDataApiStopPoints = serviceDataApiStopPoints == null ? List.of() : List.copyOf(serviceDataApiStopPoints);
        unresolvedRequiredMonitoredLocationTexts = unresolvedRequiredMonitoredLocationTexts == null
                ? List.of()
                : List.copyOf(unresolvedRequiredMonitoredLocationTexts);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        apiQueryContext = apiQueryContext == null
                ? new ScheduledServiceDataApiQueryContext(monitoringScope, serviceDataApiStopPoints, requiresAllKnownStopPoints)
                : apiQueryContext;
    }
}
