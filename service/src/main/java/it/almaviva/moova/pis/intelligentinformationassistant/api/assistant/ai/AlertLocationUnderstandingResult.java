package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record AlertLocationUnderstandingResult(
        boolean hasLocations,
        String language,
        AlertLocationUnderstandingMainEvent mainEvent,
        List<AlertLocationUnderstandingLocation> locations,
        List<AlertLocationUnderstandingNonLocationConstraint> nonLocationConstraints,
        List<String> warnings) {

    public AlertLocationUnderstandingResult {
        language = language == null ? "" : language;
        mainEvent = mainEvent == null ? AlertLocationUnderstandingMainEvent.unknown() : mainEvent;
        locations = locations == null ? List.of() : List.copyOf(locations);
        nonLocationConstraints = nonLocationConstraints == null ? List.of() : List.copyOf(nonLocationConstraints);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        hasLocations = hasLocations && !locations.isEmpty();
    }

    public static AlertLocationUnderstandingResult empty() {
        return new AlertLocationUnderstandingResult(
                false,
                "",
                AlertLocationUnderstandingMainEvent.unknown(),
                List.of(),
                List.of(),
                List.of());
    }

    public static AlertLocationUnderstandingResult emptyWithWarnings(List<String> warnings) {
        return new AlertLocationUnderstandingResult(
                false,
                "",
                AlertLocationUnderstandingMainEvent.unknown(),
                List.of(),
                List.of(),
                warnings);
    }
}
