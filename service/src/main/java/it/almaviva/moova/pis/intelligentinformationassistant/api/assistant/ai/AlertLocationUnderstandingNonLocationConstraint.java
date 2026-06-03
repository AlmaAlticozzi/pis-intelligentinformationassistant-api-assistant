package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

public record AlertLocationUnderstandingNonLocationConstraint(
        AlertLocationNonLocationConstraintType type,
        String rawText) {

    public AlertLocationUnderstandingNonLocationConstraint {
        type = type == null ? AlertLocationNonLocationConstraintType.UNKNOWN : type;
        rawText = rawText == null ? "" : rawText;
    }
}
