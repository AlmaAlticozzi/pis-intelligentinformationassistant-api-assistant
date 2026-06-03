package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record AlertLocationUnderstandingLocation(
        String rawText,
        String normalizedText,
        AlertLocationRole role,
        AlertLocationRelation relationToMainEvent,
        boolean requiredCoverage,
        AlertLocationPolarity polarity,
        String logicalGroup,
        double confidence) {

    public AlertLocationUnderstandingLocation {
        rawText = nullToEmpty(rawText);
        normalizedText = nullToEmpty(normalizedText);
        role = role == null ? AlertLocationRole.GENERIC_LOCATION : role;
        relationToMainEvent = relationToMainEvent == null ? AlertLocationRelation.UNKNOWN : relationToMainEvent;
        polarity = polarity == null ? AlertLocationPolarity.INCLUDE : polarity;
        logicalGroup = nullToEmpty(logicalGroup);
        confidence = clamp(confidence);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static double clamp(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
