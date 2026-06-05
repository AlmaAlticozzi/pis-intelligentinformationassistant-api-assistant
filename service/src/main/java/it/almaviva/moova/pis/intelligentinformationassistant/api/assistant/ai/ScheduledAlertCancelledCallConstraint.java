package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record ScheduledAlertCancelledCallConstraint(
        String rawText,
        boolean hasSpecificCancelledStop,
        String cancelledStopRawText,
        Polarity polarity,
        boolean genericAnyCancelledCallRequested,
        double confidence) {

    public enum Polarity {
        INCLUDE,
        EXCLUDE
    }
}
