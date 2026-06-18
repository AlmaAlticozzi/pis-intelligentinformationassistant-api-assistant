package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record AlertJourneyReferenceIntent(
        AlertJourneyReferenceKind kind,
        String rawText,
        String normalizedValue,
        boolean requiredCoverage,
        double confidence) {

    public AlertJourneyReferenceIntent {
        rawText = rawText == null ? "" : rawText.trim();
        normalizedValue = normalizedValue == null || normalizedValue.isBlank()
                ? rawText
                : normalizedValue.trim();
        requiredCoverage = true;
        if (confidence < 0.0) {
            confidence = 0.0;
        } else if (confidence > 1.0) {
            confidence = 1.0;
        }
    }
}
