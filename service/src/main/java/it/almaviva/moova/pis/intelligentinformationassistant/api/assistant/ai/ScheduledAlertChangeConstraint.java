package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record ScheduledAlertChangeConstraint(
        String rawText,
        ChangeIntent changeIntent,
        Direction direction,
        Polarity polarity,
        double confidence) {

    public enum ChangeIntent {
        CHANGED_ORIGIN,
        CHANGED_DESTINATION,
        CHANGED_PATH,
        EXTRA_JOURNEY,
        PLATFORM_CHANGED,
        GENERIC_CHANGE,
        GENERIC_CANCELLATION,
        PARTIAL_CANCELLATION,
        ARRIVAL_CANCELLATION,
        DEPARTURE_CANCELLATION,
        TOTAL_EXCLUSION,
        TIME_BASED_EXCLUSION,
        UNKNOWN
    }

    public enum Direction {
        ARRIVAL,
        DEPARTURE,
        UNSPECIFIED
    }

    public enum Polarity {
        INCLUDE,
        EXCLUDE
    }
}
