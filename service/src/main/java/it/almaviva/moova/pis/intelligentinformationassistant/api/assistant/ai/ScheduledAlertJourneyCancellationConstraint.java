package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record ScheduledAlertJourneyCancellationConstraint(
        String rawText,
        CancellationIntent cancellationIntent,
        Direction direction,
        Polarity polarity,
        double confidence) {

    public enum CancellationIntent {
        GENERIC_JOURNEY_CANCELLATION,
        ARRIVAL_JOURNEY_CANCELLATION,
        ARRIVAL_ONLY_JOURNEY_CANCELLATION,
        DEPARTURE_JOURNEY_CANCELLATION,
        DEPARTURE_ONLY_JOURNEY_CANCELLATION
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
