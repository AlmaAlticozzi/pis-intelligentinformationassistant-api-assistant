package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record ScheduledAlertLocationUnderstandingResult(
        boolean hasLocations,
        String language,
        ScheduledAlertMonitoringScope monitoringScope,
        List<ScheduledAlertLocationMention> locations,
        List<ScheduledAlertNonLocationConstraint> nonLocationConstraints,
        List<String> warnings) {

    public ScheduledAlertLocationUnderstandingResult {
        language = language == null ? "" : language;
        monitoringScope = monitoringScope == null ? ScheduledAlertMonitoringScope.UNSPECIFIED : monitoringScope;
        locations = locations == null ? List.of() : List.copyOf(locations);
        nonLocationConstraints = nonLocationConstraints == null ? List.of() : List.copyOf(nonLocationConstraints);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        hasLocations = hasLocations && !locations.isEmpty();
    }

    public static ScheduledAlertLocationUnderstandingResult emptyWithWarnings(List<String> warnings) {
        return new ScheduledAlertLocationUnderstandingResult(
                false,
                "",
                ScheduledAlertMonitoringScope.UNSPECIFIED,
                List.of(),
                List.of(),
                warnings);
    }
}
