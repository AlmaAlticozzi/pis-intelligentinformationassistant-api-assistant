package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.service.location;

public record AlertLocationMention(
        String rawText,
        AlertLocationSemanticRole semanticRole,
        double confidence) {
}
