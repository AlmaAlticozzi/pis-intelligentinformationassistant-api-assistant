package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record AlertJourneyReferenceIntent(
        AlertJourneyReferenceKind kind,
        String rawText,
        String normalizedValue,
        List<String> normalizedValues,
        AlertJourneyReferenceValueCombination valueCombination,
        boolean requiredCoverage,
        double confidence) {

    public AlertJourneyReferenceIntent(
            AlertJourneyReferenceKind kind,
            String rawText,
            String normalizedValue,
            boolean requiredCoverage,
            double confidence) {
        this(kind, rawText, normalizedValue, List.of(normalizedValue), AlertJourneyReferenceValueCombination.SINGLE,
                requiredCoverage, confidence);
    }

    public AlertJourneyReferenceIntent {
        rawText = rawText == null ? "" : rawText.trim();
        normalizedValue = normalizedValue == null || normalizedValue.isBlank()
                ? rawText
                : normalizedValue.trim();
        normalizedValues = normalizedValues == null || normalizedValues.isEmpty()
                ? List.of(normalizedValue)
                : normalizedValues.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (!normalizedValues.isEmpty()) {
            normalizedValue = normalizedValues.getFirst();
        }
        valueCombination = normalizedValues.size() > 1
                ? AlertJourneyReferenceValueCombination.ANY
                : AlertJourneyReferenceValueCombination.SINGLE;
        requiredCoverage = true;
        if (confidence < 0.0) {
            confidence = 0.0;
        } else if (confidence > 1.0) {
            confidence = 1.0;
        }
    }
}
