package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public record AlertRouteUnderstandingHints(
        boolean containsPollingExpression,
        boolean containsCountOrReportExpression,
        boolean containsCardinalityThresholdExpression,
        boolean containsMultiMonitoredLocationExpression,
        boolean containsSnapshotStateExpression,
        boolean containsAllLocationsExpression,
        boolean containsPlatformExpression,
        boolean containsPlatformChangeExpression,
        boolean containsChangeCancellationExclusionExpression,
        boolean containsEventOccurrenceExpression,
        boolean containsUnsupportedWeatherExpression,
        boolean containsUnsupportedWifiOrOnboardFeatureExpression,
        boolean containsGenericQuestionExpression
) {

    private static final Pattern DIGIT_NUMBER = Pattern.compile("\\b\\d+\\b");
    private static final Pattern SMALL_NUMBER_WORD = Pattern.compile(
            "\\b(uno|una|due|tre|quattro|cinque|one|two|three|four|five)\\b");
    private static final Pattern VEHICLE_NOUN = Pattern.compile(
            "\\b(treni|treno|corse|corsa|servizi|servizio|bus|tram|trains|train|services|service|journeys|journey|buses)\\b");
    private static final Pattern QUANTIFIED_VEHICLE = Pattern.compile(
            "\\b(\\d+|due|tre|quattro|cinque|two|three|four|five)\\s+"
                    + "(?:\\w+\\s+){0,2}"
                    + "(treni|treno|corse|corsa|servizi|servizio|bus|tram|trains|train|services|service|journeys|journey|buses)\\b");
    private static final Pattern COUNT_OR_REPORT = Pattern.compile(
            "\\b(quanti|quante|numero|conteggio|count|how many|total|quantity|report|rapporto|dimmi quante|dimmi quanti)\\b");
    private static final Pattern THRESHOLD = Pattern.compile(
            "\\b(almeno|piu di|piu\\s+di|oltre|maggiore di|meno di|at least|more than|over|less than|fewer than)\\b");
    private static final Pattern SNAPSHOT_STATE = Pattern.compile(
            "\\b(ci sono|c[' ]?e|sono presenti|presenti|disponibili|available|there are|there is|present)\\b");
    private static final Pattern POLLING = Pattern.compile(
            "\\b(ogni|every|ciascun|periodicamente|periodic|minutes|minute|minuti|ore|hours)\\b");
    private static final Pattern PLATFORM = Pattern.compile(
            "\\b(binario|platform|track|quay|banchina|marciapiede)\\b");
    private static final Pattern PLATFORM_CHANGE = Pattern.compile(
            "\\b(cambio di binario|cambio binario|cambia binario|cambiato il binario|platform change|platform changed|track changed)\\b");
    private static final Pattern CHANGE_CANCELLATION_EXCLUSION = Pattern.compile(
            "\\b(cambio origine|origine cambiata|changed origin|origin changed|cambio destinazione|destinazione cambiata|changed destination|destination changed|cambio percorso|percorso cambiato|itinerario cambiato|changed path|route changed|path changed|corsa straordinaria|corsa aggiuntiva|extra journey|cancellata|cancellati|cancellazione|cancelled|cancellation|soppressa|soppresse|soppresso|soppressione|fermata soppressa|fermate soppresse|fermata cancellata|fermate cancellate|fermata saltata|salta la fermata|saltano|skipped stop|cancelled stop|suppressed stop|cancelled call|partial cancellation|cancellazione parziale|parzialmente cancellata|total exclusion|esclusione totale|time based exclusion|time-based exclusion|esclusione temporale)\\b");
    private static final Pattern EVENT_OCCURRENCE = Pattern.compile(
            "\\b(quando\\s+(?:una\\s+)?corsa\\s+cambia|quando\\s+viene\\s+cambiato\\s+il\\s+binario|quando\\s+(?:una\\s+)?corsa\\s+viene\\s+cancellata|quando\\s+cambia\\s+destinazione\\s+(?:una\\s+)?corsa|quando\\s+(?:una\\s+)?corsa\\s+sopprime\\s+la\\s+fermata|quando\\s+viene\\s+cancellata\\s+la\\s+fermata|when\\s+(?:a\\s+)?(?:train|journey|service)\\s+changes|when\\s+platform\\s+changes|when\\s+(?:a\\s+)?(?:train|journey|service)\\s+is\\s+cancelled)\\b");
    private static final Pattern ALL_LOCATIONS = Pattern.compile(
            "\\b(tutte le localita|tutte le fermate|tutte le stazioni|all locations|all stop points|every stop point|all stops|every station)\\b");
    private static final Pattern WEATHER = Pattern.compile(
            "\\b(piove|pioggia|meteo|weather|rain|raining|snow|nevica|temporale|vento|wind)\\b");
    private static final Pattern WIFI_OR_ONBOARD = Pattern.compile("\\b(wifi|wi-fi|wireless|a bordo|onboard)\\b");
    private static final Pattern GENERIC_QUESTION = Pattern.compile("\\b(quanto fa|2\\+2|what is|generic question)\\b");

    public static AlertRouteUnderstandingHints fromPrompt(String prompt) {
        String normalized = normalize(prompt);
        if (normalized == null) {
            return empty();
        }
        boolean hasVehicleNoun = VEHICLE_NOUN.matcher(normalized).find();
        boolean countOrReport = COUNT_OR_REPORT.matcher(normalized).find();
        boolean snapshotState = SNAPSHOT_STATE.matcher(normalized).find();
        boolean quantifiedVehicle = QUANTIFIED_VEHICLE.matcher(normalized).find();
        boolean platform = PLATFORM.matcher(normalized).find();
        boolean platformChange = PLATFORM_CHANGE.matcher(normalized).find();
        boolean changeCancellationExclusion = CHANGE_CANCELLATION_EXCLUSION.matcher(normalized).find();
        boolean eventOccurrence = EVENT_OCCURRENCE.matcher(normalized).find();
        boolean allLocations = ALL_LOCATIONS.matcher(normalized).find();
        boolean threshold = THRESHOLD.matcher(normalized).find();
        boolean cardinality = threshold || (hasVehicleNoun && quantifiedVehicle);
        boolean multiLocation = snapshotState
                && (normalized.contains(" e ") || normalized.contains(" and "))
                && (normalized.contains(" a ") || normalized.contains(" at "));

        return new AlertRouteUnderstandingHints(
                POLLING.matcher(normalized).find(),
                countOrReport,
                cardinality,
                multiLocation,
                snapshotState || countOrReport,
                allLocations,
                platform,
                platformChange,
                changeCancellationExclusion,
                eventOccurrence,
                WEATHER.matcher(normalized).find(),
                WIFI_OR_ONBOARD.matcher(normalized).find(),
                GENERIC_QUESTION.matcher(normalized).find());
    }

    public static AlertRouteUnderstandingHints empty() {
        return new AlertRouteUnderstandingHints(false, false, false, false, false, false, false, false, false, false, false, false, false);
    }

    public boolean stronglyIndicatesAggregateSnapshot() {
        return (containsCardinalityThresholdExpression && containsSnapshotStateExpression)
                || (containsMultiMonitoredLocationExpression && containsCardinalityThresholdExpression)
                || containsCountOrReportExpression;
    }

    public String compactPromptSection() {
        return """
                Backend route hints:
                - containsPollingExpression: %s
                - containsCountOrReportExpression: %s
                - containsCardinalityThresholdExpression: %s
                - containsMultiMonitoredLocationExpression: %s
                - containsSnapshotStateExpression: %s
                - containsAllLocationsExpression: %s
                - containsPlatformExpression: %s
                - containsPlatformChangeExpression: %s
                - containsChangeCancellationExclusionExpression: %s
                - containsEventOccurrenceExpression: %s
                - containsUnsupportedWeatherExpression: %s
                - containsUnsupportedWifiOrOnboardFeatureExpression: %s
                - containsGenericQuestionExpression: %s

                Treat these as backend observations. They are not a technicalSpecification.
                If aggregate snapshot/cardinality hints are true, prefer SCHEDULED_INTERPRETER over EVENT_INTERPRETER.
                Platform expressions are ServiceData event constraints and do not imply SCHEDULED_INTERPRETER by themselves.
                A number attached to a platform/binario/track/quay expression is a platform value/property, not a count of journeys.
                """.formatted(
                containsPollingExpression,
                containsCountOrReportExpression,
                containsCardinalityThresholdExpression,
                containsMultiMonitoredLocationExpression,
                containsSnapshotStateExpression,
                containsAllLocationsExpression,
                containsPlatformExpression,
                containsPlatformChangeExpression,
                containsChangeCancellationExclusionExpression,
                containsEventOccurrenceExpression,
                containsUnsupportedWeatherExpression,
                containsUnsupportedWifiOrOnboardFeatureExpression,
                containsGenericQuestionExpression);
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
