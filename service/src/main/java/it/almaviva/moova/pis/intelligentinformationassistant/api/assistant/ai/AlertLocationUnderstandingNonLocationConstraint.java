package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record AlertLocationUnderstandingNonLocationConstraint(
        AlertLocationNonLocationConstraintType type,
        String rawText,
        AlertJourneyReferenceKind journeyReferenceKind,
        String normalizedValue,
        boolean requiredCoverage,
        double confidence) {

    public AlertLocationUnderstandingNonLocationConstraint(
            AlertLocationNonLocationConstraintType type,
            String rawText) {
        this(type, rawText, null, "", true, 0.0);
    }

    public AlertLocationUnderstandingNonLocationConstraint {
        type = type == null ? AlertLocationNonLocationConstraintType.UNKNOWN : type;
        rawText = rawText == null ? "" : rawText;
        normalizedValue = normalizedValue == null ? "" : normalizedValue;
        requiredCoverage = true;
        if (confidence < 0.0) {
            confidence = 0.0;
        } else if (confidence > 1.0) {
            confidence = 1.0;
        }
    }
}
