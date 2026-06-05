package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record ScheduledAlertNonLocationConstraint(
        ScheduledAlertNonLocationConstraintType type,
        String rawText) {

    public ScheduledAlertNonLocationConstraint {
        type = type == null ? ScheduledAlertNonLocationConstraintType.UNKNOWN : type;
        rawText = rawText == null ? "" : rawText;
    }
}
