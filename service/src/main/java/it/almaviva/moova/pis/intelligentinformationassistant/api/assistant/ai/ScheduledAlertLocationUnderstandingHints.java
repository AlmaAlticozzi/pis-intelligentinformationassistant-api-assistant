package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public record ScheduledAlertLocationUnderstandingHints(
        boolean containsAllLocationsExpression,
        boolean containsPlatformExpression,
        boolean containsOriginExpression,
        boolean containsTimetabledOriginExpression,
        boolean containsDestinationExpression,
        boolean containsTimetabledDestinationExpression,
        boolean containsRouteExpression,
        boolean containsCancelledStopExpression,
        boolean containsReplacementExpression) {

    private static final Pattern ALL_LOCATIONS = Pattern.compile(
            "\\b(tutte le localita|tutte le fermate|tutte le stazioni|all locations|all stop points|all stops|every stop point|every station)\\b");
    private static final Pattern PLATFORM = Pattern.compile(
            "\\b(binario|platform|track|quay|banchina|marciapiede|cambio di binario|platform change)\\b");
    private static final Pattern ORIGIN = Pattern.compile("\\b(origine|origin|from where|starts from|partenza da)\\b");
    private static final Pattern TIMETABLED_ORIGIN = Pattern.compile(
            "\\b(origine programmata|origine pianificata|scheduled origin|planned origin|timetabled origin)\\b");
    private static final Pattern DESTINATION = Pattern.compile("\\b(destinazione|destino|destination|to where|ends at)\\b");
    private static final Pattern TIMETABLED_DESTINATION = Pattern.compile(
            "\\b(destinazione programmata|destino programmato|scheduled destination|planned destination|timetabled destination)\\b");
    private static final Pattern ROUTE = Pattern.compile("\\b(passa|passano|passera|via|calls at|passes through|route includes)\\b");
    private static final Pattern CANCELLED = Pattern.compile("\\b(soppressa|soppresso|fermata soppressa|cancelled stop|suppressed stop|skipped stop)\\b");
    private static final Pattern REPLACEMENT = Pattern.compile("\\b(replacement|sostitutiv|sostituzione)\\b");

    public static ScheduledAlertLocationUnderstandingHints fromPrompt(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null) {
            return empty();
        }
        return new ScheduledAlertLocationUnderstandingHints(
                ALL_LOCATIONS.matcher(normalized).find(),
                PLATFORM.matcher(normalized).find(),
                ORIGIN.matcher(normalized).find(),
                TIMETABLED_ORIGIN.matcher(normalized).find(),
                DESTINATION.matcher(normalized).find(),
                TIMETABLED_DESTINATION.matcher(normalized).find(),
                ROUTE.matcher(normalized).find(),
                CANCELLED.matcher(normalized).find(),
                REPLACEMENT.matcher(normalized).find());
    }

    public static ScheduledAlertLocationUnderstandingHints empty() {
        return new ScheduledAlertLocationUnderstandingHints(false, false, false, false, false, false, false, false, false);
    }

    public String compactPromptSection() {
        return """
                Backend scheduled location hints:
                - containsAllLocationsExpression: %s
                - containsPlatformExpression: %s
                - containsOriginExpression: %s
                - containsTimetabledOriginExpression: %s
                - containsDestinationExpression: %s
                - containsTimetabledDestinationExpression: %s
                - containsRouteExpression: %s
                - containsCancelledStopExpression: %s
                - containsReplacementExpression: %s

                Treat these as backend observations. They are not a technicalSpecification and do not resolve point ids.
                Platform/binario observations must become nonLocationConstraints, not locations.
                """.formatted(
                containsAllLocationsExpression,
                containsPlatformExpression,
                containsOriginExpression,
                containsTimetabledOriginExpression,
                containsDestinationExpression,
                containsTimetabledDestinationExpression,
                containsRouteExpression,
                containsCancelledStopExpression,
                containsReplacementExpression);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
