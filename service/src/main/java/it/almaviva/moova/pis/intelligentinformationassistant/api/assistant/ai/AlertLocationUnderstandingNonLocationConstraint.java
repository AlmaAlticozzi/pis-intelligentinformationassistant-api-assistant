package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import java.util.List;

public record AlertLocationUnderstandingNonLocationConstraint(
        AlertLocationNonLocationConstraintType type,
        String rawText,
        AlertJourneyReferenceKind journeyReferenceKind,
        String normalizedValue,
        List<String> normalizedValues,
        String entityHeadText,
        List<String> descriptorValueTexts,
        List<String> eventTypes,
        String direction,
        String status,
        String semanticRole,
        String evidenceText,
        AlertJourneyReferenceValueCombination valueCombination,
        boolean requiredCoverage,
        double confidence) {

    public AlertLocationUnderstandingNonLocationConstraint(
            AlertLocationNonLocationConstraintType type,
            String rawText) {
        this(type, rawText, null, "", List.of(), "", List.of(), List.of(), "", "", "", "",
                AlertJourneyReferenceValueCombination.SINGLE, true, 0.0);
    }

    public AlertLocationUnderstandingNonLocationConstraint(
            AlertLocationNonLocationConstraintType type,
            String rawText,
            AlertJourneyReferenceKind journeyReferenceKind,
            String normalizedValue,
            boolean requiredCoverage,
            double confidence) {
        this(type, rawText, journeyReferenceKind, normalizedValue, List.of(normalizedValue),
                "", List.of(normalizedValue), List.of(), "", "", "", "",
                AlertJourneyReferenceValueCombination.SINGLE, requiredCoverage, confidence);
    }

    public AlertLocationUnderstandingNonLocationConstraint(
            AlertLocationNonLocationConstraintType type,
            String rawText,
            AlertJourneyReferenceKind journeyReferenceKind,
            String normalizedValue,
            List<String> normalizedValues,
            AlertJourneyReferenceValueCombination valueCombination,
            boolean requiredCoverage,
            double confidence) {
        this(type, rawText, journeyReferenceKind, normalizedValue, normalizedValues,
                "", normalizedValues, List.of(), "", "", "", "", valueCombination, requiredCoverage, confidence);
    }

    public AlertLocationUnderstandingNonLocationConstraint(
            AlertLocationNonLocationConstraintType type,
            String rawText,
            AlertJourneyReferenceKind journeyReferenceKind,
            String normalizedValue,
            List<String> normalizedValues,
            String entityHeadText,
            List<String> descriptorValueTexts,
            AlertJourneyReferenceValueCombination valueCombination,
            boolean requiredCoverage,
            double confidence) {
        this(type, rawText, journeyReferenceKind, normalizedValue, normalizedValues,
                entityHeadText, descriptorValueTexts, List.of(), "", "", "", "",
                valueCombination, requiredCoverage, confidence);
    }

    public AlertLocationUnderstandingNonLocationConstraint {
        type = type == null ? AlertLocationNonLocationConstraintType.UNKNOWN : type;
        rawText = rawText == null ? "" : rawText;
        normalizedValue = normalizedValue == null ? "" : normalizedValue;
        entityHeadText = entityHeadText == null ? "" : entityHeadText.trim();
        descriptorValueTexts = descriptorValueTexts == null
                ? List.of()
                : descriptorValueTexts.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        eventTypes = eventTypes == null
                ? List.of()
                : eventTypes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase())
                .distinct()
                .toList();
        direction = direction == null ? "" : direction.trim().toUpperCase();
        status = status == null ? "" : status.trim().toUpperCase();
        semanticRole = semanticRole == null ? "" : semanticRole.trim().toUpperCase();
        evidenceText = evidenceText == null ? "" : evidenceText.trim();
        normalizedValues = normalizedValues == null
                ? List.of()
                : normalizedValues.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (normalizedValues.isEmpty() && !normalizedValue.isBlank()) {
            normalizedValues = List.of(normalizedValue);
        }
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
