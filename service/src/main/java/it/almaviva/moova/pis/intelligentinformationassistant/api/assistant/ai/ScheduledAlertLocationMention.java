package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record ScheduledAlertLocationMention(
        String rawText,
        String normalizedText,
        ScheduledAlertLocationRole role,
        ScheduledAlertLocationRelation relationToSnapshot,
        boolean requiredForApiQuery,
        boolean requiredCoverage,
        ScheduledAlertLocationPolarity polarity,
        String logicalGroup,
        double confidence) {

    public ScheduledAlertLocationMention {
        rawText = rawText == null ? "" : rawText;
        normalizedText = normalizedText == null ? "" : normalizedText;
        role = role == null ? ScheduledAlertLocationRole.UNKNOWN_LOCATION_ROLE : role;
        relationToSnapshot = relationToSnapshot == null ? ScheduledAlertLocationRelation.UNKNOWN : relationToSnapshot;
        polarity = polarity == null ? ScheduledAlertLocationPolarity.INCLUDE : polarity;
        logicalGroup = logicalGroup == null ? "" : logicalGroup;
        confidence = Math.max(0.0, Math.min(1.0, confidence));
    }
}
