package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record ScheduledAlertReplacementConstraint(
        String rawText,
        ReplacementIntent replacementIntent,
        ReplacementType replacementType,
        boolean hasSpecificReplacementStop,
        String replacementStopRawText,
        Polarity polarity,
        String unsupportedReason,
        double confidence) {

    public enum ReplacementIntent {
        GENERIC_REPLACEMENT_SERVICE,
        HAS_REPLACEMENT_OBJECT,
        HAS_EXTERNAL_REPLACEMENT_OBJECT,
        REPLACEMENT_STOP,
        REPLACEMENT_TYPE,
        REPLACEMENT_STOP_WITH_TYPE,
        REPLACEMENT_SOURCE_ROUTE,
        UNKNOWN
    }

    public enum ReplacementType {
        ARRIVAL,
        DEPARTURE,
        ARRIVALDEPARTURE,
        UNSPECIFIED
    }

    public enum Polarity {
        INCLUDE,
        EXCLUDE
    }
}
